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

import file.BinaryFileReader;
import hash.HashMaker;
import io.AidDAO;
import io.AidTables;

import java.awt.Container;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;

import time.StopWatch;

public class ModuleManageLists extends MaintenanceModule {
	StopWatch stopWatch = new StopWatch();
	AidDAO sql;
	
	private int statHashed = 0;
	private boolean stop = false;
	
	ButtonGroup optionGroup;
	JRadioButton listDnw, listBlacklist, listArchive, listIndex;
	
	public ModuleManageLists(){
		super();
		moduleName = "Manage lists";
	}
	
	@Override
	public void optionPanel(Container container) {
		optionGroup = new ButtonGroup();
		
		listDnw = new JRadioButton("DNW");
		listArchive = new JRadioButton("Archive");
		listBlacklist = new JRadioButton("Blacklist");
		listIndex = new JRadioButton("Index");
		
		optionGroup.add(listBlacklist);
		optionGroup.add(listArchive);
		optionGroup.add(listDnw);
		optionGroup.add(listIndex);
		
		container.add(listIndex);
		container.add(listArchive);
		container.add(listDnw);
		container.add(listBlacklist);
		
		listIndex.setSelected(true);
	}

	@Override
	public void start() {
		enableAllOptions(false);
		stop = false;
		statHashed = 0;
		
		sql = new AidDAO(getConnectionPool());
		
		File path = new File(getPath());
		
		if(!path.exists() || !path.isDirectory()){
			error("Invalid directory");
			return;
		}
		
		stopWatch.start();
		info("Hashing files...");
		try {
			Files.walkFileTree(path.toPath(), new FileHasher());
		} catch (IOException e) {
			error("Directory hashing failed: " + e.getMessage());
		}
		
		stopWatch.stop();
		info("Finished processing " + statHashed + " blacklisted files in " + stopWatch.getTime());
		enableAllOptions(true);
	}

	@Override
	public void Cancel() {
		stop = true;
	}
	
	private void enableAllOptions(boolean enable) {
		listArchive.setEnabled(enable);
		listIndex.setEnabled(enable);
		listDnw.setEnabled(enable);
		listBlacklist.setEnabled(enable);
	}
	
	class FileHasher extends SimpleFileVisitor<Path>{
		BinaryFileReader bfr = new BinaryFileReader();
		HashMaker hm = new HashMaker();
		
		@Override
		public FileVisitResult preVisitDirectory(Path arg0, BasicFileAttributes arg1) throws IOException {
			if(stop){
				return FileVisitResult.TERMINATE;
			}
			
			setStatus("Scanning " + arg0.toString());
			return super.preVisitDirectory(arg0, arg1);
		}
		
		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)throws IOException {
				String hash = hm.hash(bfr.getViaDataInputStream(file.toFile()));
				statHashed++;
				
				if(listIndex.isSelected()){
					sql.addIndex(hash, file.toString(), file.toFile().length());
				}else if(listArchive.isSelected()){
					//TODO implement this
					error("Function not implemented");
					return FileVisitResult.TERMINATE;
				}else if(listDnw.isSelected()){
					sql.update(hash, AidTables.Dnw);
				}else if(listBlacklist.isSelected()){
					sql.update(hash, AidTables.Block);
				}else{
					error("Invalid mode");
					return FileVisitResult.TERMINATE;
				}
			return super.visitFile(file, attrs);
		}
	}
}
