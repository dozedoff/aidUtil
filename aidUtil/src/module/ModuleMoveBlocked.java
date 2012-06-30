/*  Copyright (C) 2012  Nicholas Wright
	
	part of 'AidUtil', a collection of maintenance tools for 'Aid'.

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package module;

import java.awt.Container;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.LinkedList;

public class ModuleMoveBlocked extends MaintenanceModule{
	final String BLOCKED_TAG = "WARNING-";
	final String BLOCKED_DIR = "CHECK";
	
	boolean stop = false;
	LinkedList<Path> blockedDirectories = new LinkedList<>();
	File blockedDirsPath;
	
	@Override
	public void optionPanel(Container container) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void start() {
		stop = false;
		blockedDirectories.clear();
		
		File f = new File(getPath());
		
		// check that directory exists
		if((! f.exists()) || (! f.isDirectory())){
			log(getPath()+" is not a valid directory");
			return;
		}
		
		Path startPath = f.toPath();
		log("Searching for blocked files...");
		try {
			Files.walkFileTree(startPath, new DirectoryFinder());
		} catch (IOException e) {
			log("Error while walking file tree: " + e.getMessage());
			e.printStackTrace();
		}
		
		blockedDirsPath = new File(f.getParent(),BLOCKED_DIR);
		
		String form;
		if(blockedDirectories.size() == 1){
			form = "directory";
		}else{
			form = "directories";
		}
		
		log("Found "+blockedDirectories.size() + " " + form + " with blocked files");
		if(! stop){
			log("moving " + form + "...");
			blockedDirsPath.mkdirs(); // create folder for blocked directories
			moveDirs();
			log("Finished moving " + form);
		}
	}
	
	private void moveDirs(){
		for(Path p : blockedDirectories){
			log("Moving directory " + p.toString());
			
			try {
				Files.walkFileTree(p, new DirectoryMover());
			} catch (IOException e) {
				log("Failed to move Directory " + p.toString() + " (" + e.getMessage()+")");
				e.printStackTrace();
			}
			
		}
	}

	@Override
	public void Cancel() {
		stop = true;
	}
	
	class DirectoryFinder extends SimpleFileVisitor<Path>{
		@Override
		public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {

			if(stop){
				return FileVisitResult.TERMINATE;
			}
			
			return super.postVisitDirectory(dir, exc);
		}
		
		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
			// find files with warning tags
			if(file.getFileName().toString().startsWith(BLOCKED_TAG)){
				Path directory = file.getParent();
				
				// usualy files that are in the root directory
				if(directory == null){
					log("Could not add directory for "+file.toString());
					return super.visitFile(file, attrs);
				}
				
				blockedDirectories.add(directory);
				return FileVisitResult.SKIP_SIBLINGS; // don't look at other files in directory
			}
			
			return super.visitFile(file, attrs);
		}
	}
	
	class DirectoryMover extends SimpleFileVisitor<Path>{
		Path currMoveDir;
		
		@Override
		public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
			if(stop){
				return FileVisitResult.TERMINATE;
			}
			
			File f = new File(blockedDirsPath,dir.getFileName().toString());
			f.mkdirs(); // create new directory with identical name in the blocked directory folder
			currMoveDir = f.toPath();
			return super.preVisitDirectory(dir, attrs);
		}
		
		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
			Files.move(file, currMoveDir.resolve(file.getFileName())); // move files
			return super.visitFile(file, attrs);
		}
		
		@Override
		public FileVisitResult postVisitDirectory(Path arg0, IOException arg1) throws IOException {
			Files.delete(arg0); // delete the source directory when done
			return super.postVisitDirectory(arg0, arg1);
		}
	}

}
