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
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import module.DatabaseWorker.OperationMode;

import file.FileInfo;
import file.FileWalker;

public class ModuleArchiveIndexer extends MaintenanceModule {
	static final String APP_PATH_KEY = "7zipAppPath";
	
	private ArchiveUnpacker unpacker;
	private FileDeleter deleter;
	private FileHasher hasher;
	private DatabaseHandler dbHandler;
	private DatabaseWorker dbWorker;
	
	JTextField tempPathField;
	JRadioButton indexMode, dnwMode;
	ButtonGroup modeGroup;
	
	LinkedList<Path> foundArchives;

	@Override
	public void optionPanel(Container container) {
		setModuleName("Archive indexer");
		container.add(new JLabel("Temp folder"));
		
		tempPathField = new JTextField(20);
		tempPathField.setToolTipText("Folder to use for unpacking archives");
		
		indexMode = new JRadioButton("Index");
		indexMode.setToolTipText("Add files to the file index");
		dnwMode = new JRadioButton("DNW");
		dnwMode.setToolTipText("Add files to the DNW list");
		
		modeGroup = new ButtonGroup();
		modeGroup.add(indexMode);
		modeGroup.add(dnwMode);
		
		indexMode.setSelected(true);
	}

	@Override
	public void start() {
		Properties aidUtilSettings = new Properties();
		LinkedBlockingQueue<ArchiveFile> inputQueue, outputQueue;
		Path tempFolder, appPath;
		
		try {
			aidUtilSettings.load(ModuleArchiveIndexer.class.getResourceAsStream("aidUtil.properties"));
			foundArchives = ArchiveFinder.find(Paths.get(getPath()));
		} catch (IOException e) {
			error(e.getMessage());
			return;
		}
		
		appPath = Paths.get((String)aidUtilSettings.get(APP_PATH_KEY));
		unpacker = new ArchiveUnpacker(appPath);
		
		tempFolder = Paths.get(tempPathField.getText());
		
		inputQueue = new LinkedBlockingQueue<>();
		outputQueue = new LinkedBlockingQueue<>();
		hasher = new FileHasher(inputQueue, outputQueue);
		dbHandler = new DatabaseHandler(getConnectionPool());
		dbWorker = new DatabaseWorker(dbHandler, inputQueue, new PathRewriter(tempFolder), getOpMode());
		
		startThreads();
		
		for(Path archive : foundArchives){
			unpackArchive(archive, tempFolder);
			LinkedList<Path> images = findImages(tempFolder);
			addFilesToQueue(inputQueue, archive, images);
		}
	}
	
	private void unpackArchive(Path archive, Path tempFolder) {
		try {
			unpacker.unpack(archive, tempFolder);
		} catch (Exception e) {
			error(e.getMessage());
		}
	}
	
	private LinkedList<Path> findImages(Path directory){
		LinkedList<Path> foundfiles = new LinkedList<>();
		
		try {
			foundfiles = FileWalker.getAllImages(directory);
		} catch (IOException e) {
			error(e.getMessage());
		}
		
		return foundfiles;
	}
	
	private void addFilesToQueue(LinkedBlockingQueue<ArchiveFile> inputQueue, Path archive, List<Path> images) {
			for(Path file : images){
				FileInfo info = new FileInfo(file);
				ArchiveFile archiveFile = new ArchiveFile(info, archive);
				inputQueue.add(archiveFile);
			}
	}
	
	private OperationMode getOpMode(){
		if(indexMode.isSelected()){
			return OperationMode.AddToIndex;
		}
		
		if(dnwMode.isSelected()){
			return OperationMode.AddToDNW;
		}
		
		return OperationMode.AddToIndex;
	}
	
	private void startThreads() {
		hasher.start();
		dbWorker.start();
	}
	
	@Override
	public void Cancel() {
		// TODO Auto-generated method stub

	}
}
