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
package module.manageLists;

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

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import module.MaintenanceModule;
import net.miginfocom.swing.MigLayout;
import time.StopWatch;

public class ModuleManageLists extends MaintenanceModule {
	StopWatch stopWatch = new StopWatch();
	AidDAO sql;
	
	private int statHashed = 0;
	private boolean stop = false;
	
	ButtonGroup optionGroup;
	JRadioButton listDnw = new JRadioButton("DNW"), listBlacklist = new JRadioButton("Blacklist"),
				listUnDnw = new JRadioButton("Un-DNW"), listUnBlacklist = new JRadioButton("Un-Blacklist");
	JRadioButton[] rbs = {listDnw, listBlacklist, listUnDnw, listUnBlacklist};
	private final JPanel mode = new JPanel();
	
	public ModuleManageLists(){
		super();
		setModuleName("Manage lists");
	}
	
	/**
	 * @wbp.parser.entryPoint
	 */
	@Override
	public void optionPanel(Container container) {
		JPanel manageListOptions = new JPanel();
		manageListOptions.setLayout(new MigLayout("", "[49px,grow][63px][67px][79px]", "[23px][grow]"));
		optionGroup = new ButtonGroup();
		
		for(JRadioButton rb : rbs){
			optionGroup.add(rb);
		}
		
		mode.setBorder(BorderFactory.createTitledBorder("Mode"));
		
		mode.setLayout(new MigLayout("", "[][][][]", "[]"));
		mode.add(listDnw, "cell 0 0");
		mode.add(listBlacklist, "cell 1 0");
		mode.add(listUnDnw, "cell 2 0");
		mode.add(listUnBlacklist, "cell 3 0");
		listDnw.setSelected(true);
		
		manageListOptions.add(mode, "cell 0 0 4 1,grow");
		container.add(manageListOptions, "cell 0 0,alignx left,aligny top");
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
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("Processed ");
		sb.append(statHashed);
		sb.append(" files in ");
		sb.append(stopWatch.getTime());
		
		info(sb.toString());
		enableAllOptions(true);
	}

	@Override
	public void Cancel() {
		stop = true;
	}
	
	private void enableAllOptions(boolean enable) {
		for(JRadioButton rb : rbs){
			rb.setEnabled(enable);
		}
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
				
				if(listDnw.isSelected()){
					sql.update(hash, AidTables.Dnw);
					sql.delete(AidTables.Block, hash);
					sql.delete(AidTables.Fileindex, hash);
					sql.delete(AidTables.Fileduplicate, hash);
				}else if(listBlacklist.isSelected()){
					sql.update(hash, AidTables.Block);
					sql.delete(AidTables.Dnw, hash);
					sql.delete(AidTables.Fileindex, hash);
					sql.delete(AidTables.Fileduplicate, hash);
				}else if(listUnDnw.isSelected()){
					sql.delete(AidTables.Dnw, hash);
				}else if(listUnBlacklist.isSelected()){
					sql.delete(AidTables.Block, hash);
				}else{
					error("Invalid mode");
					return FileVisitResult.TERMINATE;
				}
			return super.visitFile(file, attrs);
		}
	}
}
