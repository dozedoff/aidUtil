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
import java.nio.file.attribute.BasicFileAttributes;
import java.util.LinkedList;

public class ModuleMoveBlocked extends MaintenanceModule{
	final String BLOCKED_TAG = "WARNING-";
	
	boolean stop = false;
	LinkedList<Path> blockedDirectories = new LinkedList<>();
	
	@Override
	public void optionPanel(Container container) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void start() {
		stop = false;
		File f = new File(getPath());
		
		// check that directory exists
		if((! f.exists()) || (! f.isDirectory())){
			log(getPath()+" is not a valid directory");
			return;
		}
		
		Path startPath = f.toPath();
		
		try {
			Files.walkFileTree(startPath, new DirectoryFinder());
		} catch (IOException e) {
			log("Error while walking file tree: " + e.getMessage());
			e.printStackTrace();
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

}
