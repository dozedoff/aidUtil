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
package module.archiveIndexer;

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
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

import module.MaintenanceModule;
import module.archiveIndexer.DatabaseWorker.OperationMode;
import app.Core;
import file.FileInfo;
import file.FileWalker;

public class ModuleArchiveIndexer extends MaintenanceModule {
	static final String APP_PATH_KEY = "7zipAppPath";
	
	private ArchiveUnpacker unpacker;
	private FileHasher hasher;
	private DatabaseHandler dbHandler;
	private DatabaseWorker dbWorker;
	
	JTextField tempPathField;
	JRadioButton indexMode, dnwMode;
	ButtonGroup modeGroup;
	
	LinkedList<Path> foundArchives;

	/**
	 * @wbp.parser.entryPoint
	 */
	@Override
	public void optionPanel(Container container) {
		JPanel archiveIndexerOptions = new JPanel();
		container.add(archiveIndexerOptions, "cell 0 0,alignx left,aligny top");
		
		archiveIndexerOptions.setLayout(new MigLayout("aligny top", "[][263.00]", "[][]"));
		setModuleName("Archive indexer");
		
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
		
		archiveIndexerOptions.add(new JLabel("Temp folder"),"cell 0 0,aligny top");
		archiveIndexerOptions.add(tempPathField, "cell 1 0,growx");
		archiveIndexerOptions.add(new JLabel("Mode"), "cell 0 1");
		archiveIndexerOptions.add(indexMode, "cell 1 1");
		archiveIndexerOptions.add(dnwMode, "cell 1 1");
	}

	@Override
	public void start() {
		Properties aidUtilSettings = new Properties();
		LinkedBlockingQueue<ArchiveFile> inputQueue, outputQueue;
		Path tempFolder, appPath;
		
		try {
			aidUtilSettings.load(Core.class.getResourceAsStream("aidUtil.properties"));
			info("Searching for archives...");
			foundArchives = ArchiveFinder.find(Paths.get(getPath()));
		} catch (IOException e) {
			// program
			error("Failed to load aidUtil.properties: " + e.getMessage());
			return;
		}
		
		info("Found " + foundArchives.size() + " archives.");
		
		appPath = Paths.get((String)aidUtilSettings.get(APP_PATH_KEY));
		unpacker = new ArchiveUnpacker(appPath);
		
		tempFolder = Paths.get(tempPathField.getText());
		
		inputQueue = new LinkedBlockingQueue<>();
		outputQueue = new LinkedBlockingQueue<>();
		hasher = new FileHasher(inputQueue, outputQueue);
		dbHandler = new DatabaseHandler(getConnectionPool());
		dbWorker = new DatabaseWorker(dbHandler, outputQueue, new PathRewriter(tempFolder), getOpMode());
		
		startThreads();
		
		LinkedList<Path> images;
		
		info("Starting to index archives...");
		
		for(Path archive : foundArchives){
			unpackArchive(archive, tempFolder);
			images = findImages(tempFolder);
			addFilesToQueue(inputQueue, archive, images);
			hashFiles();
			deleteFilesInTempDir(tempFolder);
		}
		
		info("Finished indexing, " + foundArchives.size() + " archives processed");
	}
	
	private void unpackArchive(Path archive, Path tempFolder) {
		try {
			info("Unpacking " + archive.getFileName().toString());
			unpacker.unpack(archive, tempFolder);
		} catch (Exception e) {
			error("Failed to unpack archive: " + e.getMessage());
		}
	}
	
	private LinkedList<Path> findImages(Path directory){
		LinkedList<Path> foundfiles = new LinkedList<>();
		
		try {
			foundfiles = FileWalker.getAllImages(directory);
		} catch (IOException e) {
			error("Failed to find images: " + e.getMessage());
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
		dbWorker.start();
	}
	
	private void stopThreads() {
		dbWorker.interrupt();
	}
	
	private void deleteFilesInTempDir(Path path) {
		try {
			FileDeleter.deleteAll(path);
		} catch (IOException e) {
			error("Failed to clear temp directory: " + e.getMessage());
		}
	}
	
	private void hashFiles() {
		try {
			hasher.hashFiles();
		} catch (InterruptedException e) {
			
		} catch (IOException e) {
			error("Failed to hash file: " + e.getMessage());
		}
	}
	
	@Override
	public void Cancel() {
		// TODO Auto-generated method stub

	}
}
