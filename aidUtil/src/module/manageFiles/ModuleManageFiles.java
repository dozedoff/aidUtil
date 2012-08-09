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
package module.manageFiles;

import hash.HashMaker;
import io.AidDAO;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;

import net.miginfocom.swing.MigLayout;

import module.MaintenanceModule;
import time.StopWatch;
import file.BinaryFileReader;
import file.FileUtil;

public class ModuleManageFiles extends MaintenanceModule {
	final String BLACKLISTED_TAG = "WARNING-";
	final String BLACKLISTED_DIR = "CHECK";
	final String DNW_DIR = "DNW";
	
	final int WORKERS = 2; // number of threads hashing data and communicating with the DB
	final int FILE_QUEUE_SIZE = 100; // setting this too high will probably result in "out of memory" errors

	LinkedList<Path> pendingFiles = new LinkedList<>();
	LinkedList<Path> blacklistedDir = new LinkedList<>();
	LinkedBlockingQueue<FileData> dataQueue = new LinkedBlockingQueue<>(FILE_QUEUE_SIZE);

	Thread worker[] = new Thread[WORKERS], producer, etaTracker;
	StopWatch stopWatch = new StopWatch();
	StopWatch dirWalkStopwatch = new StopWatch();
	volatile int statHashed = 0;
	int statSkipped = 0;
	String locationTag = null;
	String drive = null;
	
	// GUI
	JPanel panelBlacklist = new JPanel();
	JPanel panelDnw = new JPanel();
	JPanel panelIndex = new JPanel();
	
	JCheckBox blMoveTagged = new JCheckBox("Move only (no hashing)");
	JCheckBox blCheck = new JCheckBox("Check for blacklisted");
	JCheckBox indexSkip = new JCheckBox("Skip index files");
	JCheckBox indexPrune = new JCheckBox("Prune index");
	
	JCheckBox dnwCheck = new JCheckBox("Check for DNW");
	JRadioButton dnwMove = new JRadioButton("Move DNW");
	JRadioButton dnwDelete = new JRadioButton("Delete DNW");
	JRadioButton dnwLog = new JRadioButton("Log DNW");
	ButtonGroup dnwGroup = new ButtonGroup();
	
	JCheckBox indexCheck = new JCheckBox("Index files");
	
	JProgressBar progressBar = new JProgressBar();
	
	// stats
	int statBlocked, statDir;
	boolean stop = false;
	String duration;
	
	public ModuleManageFiles() {
		super();
		setModuleName("Manage files");
	}
	
	/**
	 * @wbp.parser.entryPoint
	 */
	@Override
	public void optionPanel(Container container) {
		container.setLayout(new MigLayout());
		JPanel manageFilesOptions = new JPanel();
		manageFilesOptions.setLayout(new MigLayout("", "[367px][65px]", "[56px][56px][62px]"));
		panelBlacklist.setLayout(new MigLayout("", "[123px][137px]", "[23px]"));
		
		panelBlacklist.add(blCheck, "cell 0 0,alignx left,aligny top");
		panelBlacklist.add(blMoveTagged, "cell 1 0,alignx left,aligny top");
		panelBlacklist.setBorder(BorderFactory.createTitledBorder("Blacklist"));
		manageFilesOptions.add(panelBlacklist, "cell 0 0,growx,aligny center");
		panelDnw.setLayout(new MigLayout("", "[99px][79px][83px][69px]", "[23px]"));
		
		panelDnw.add(dnwCheck, "cell 0 0,alignx left,aligny top");
		
		// disable buttons when not in use
		dnwCheck.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(dnwCheck.isSelected()){
					setDnwRadioEnable(true);
				}else{
					setDnwRadioEnable(false);
				}
			}
		});
		
		dnwGroup.add(dnwMove);
		dnwGroup.add(dnwLog);
		dnwGroup.add(dnwDelete);
		dnwLog.setSelected(true);
		
		setDnwRadioEnable(false);
		
		panelDnw.add(dnwMove, "cell 1 0,alignx left,aligny top");
		panelDnw.add(dnwDelete, "cell 2 0,alignx left,aligny top");
		panelDnw.add(dnwLog, "cell 3 0,alignx left,aligny top");
		panelDnw.setBorder(BorderFactory.createTitledBorder("DNW"));
		manageFilesOptions.add(panelDnw, "cell 0 1,alignx left,aligny center");
		panelIndex.setLayout(new MigLayout("", "[75px][95px][83px]", "[23px]"));
		
		panelIndex.add(indexCheck, "cell 0 0,alignx left,aligny top");
		panelIndex.add(indexSkip, "cell 1 0,alignx left,aligny top");
		panelIndex.add(indexPrune, "cell 2 0,alignx left,aligny top");
		
		indexSkip.setToolTipText("If the filepath is found, the file will not be hashed");
		indexPrune.setToolTipText("Delete index entries with invalid paths");
		panelIndex.setBorder(BorderFactory.createTitledBorder("Index"));
		manageFilesOptions.add(panelIndex, "cell 0 2,growx,aligny center");
		
		progressBar.setPreferredSize(new Dimension(200, 30));
		progressBar.setStringPainted(true);
		manageFilesOptions.add(progressBar, "cell 0 3,growx,aligny center");
		
		container.add(manageFilesOptions, "cell 0 0,alignx left,aligny top");
	}
	
	private void setDnwRadioEnable(boolean enable){
		final JRadioButton dnwRadio[] = {dnwMove, dnwDelete, dnwLog};
		
		for(JRadioButton r : dnwRadio){
			r.setEnabled(enable);
		}
	}

	@Override
	public void start() {
		// reset stats
		statHashed = 0;
		statBlocked = 0;
		statDir = 0;
		statSkipped = 0;
		stopWatch.reset();
		dirWalkStopwatch.reset();
		duration = "--:--:--";
		
		locationTag = null;
		
		// reset stop flag
		stop = false;
		
		// clear lists
		blacklistedDir.clear();
		pendingFiles.clear();
		dataQueue.clear();
		
		File f = new File(getPath());
		
		if((! f.exists()) || (! f.isDirectory())){	
			error("Target Directory is invalid.");
			return;
		}
		
		locationTag = checkTag(f.toPath()); //TODO replace this with util.LocationTag
		// this should be much safer than using a predefined props file
		drive = Paths.get(getPath()).getRoot().toString(); 
		
		
		if(locationTag == null){
			return;
		}
		
		stopWatch.start();
		
		if(indexPrune.isSelected()){
			pruneIndex();
		}
		
		info("Walking directories...");
		dirWalkStopwatch.start();
		try {
			// go find those files...
			Files.walkFileTree( f.toPath(), new ImageVisitor(loadIndexedFiles(locationTag)));
		} catch (IOException e) {
			error("File walk failed");
			e.printStackTrace();
		}
		dirWalkStopwatch.stop();
		
		info("Walked directories in " + dirWalkStopwatch.getTime() + " skipped " + statSkipped + " files");
		
		lookForBlacklisted();

		if(blMoveTagged.isSelected()){
			info("Moving blacklisted directories...");
			moveBlacklisted();
		}
		
		stopWatch.stop();
		
		info("File processing done. " + statHashed +" files hashed, " + statBlocked +" blacklisted files found, " + statDir + " blacklisted Directories moved.");
		info("Mark blacklisted run duration - " + stopWatch.getTime());
		
		setStatus("Finished");
	}
	
	/**
	 * Start the threads needed for hashing files and database lookups.
	 * Will wait for the threads to die before returning.
	 */
	private void lookForBlacklisted(){
		info("Starting worker thread...");
		
		producer = new DataProducer();
		producer.start();
		
		etaTracker = new EtaTracker();
		etaTracker.start();

		progressBar.setMaximum(pendingFiles.size());
		
		for(Thread t : worker){
			t = new DBWorker();
			t.start();
		}
		
		while(! pendingFiles.isEmpty()){
			try {
				// display some stats while chewing through those files
				setStatus("Time remaining:  " + duration);
				progressBar.setValue(statHashed);
				Thread.sleep(2000);
			} catch (InterruptedException e) {}
		}
	
		info("Wating for worker threads to finish...");
		try{
			producer.join();
			
			// wait for threads to clear the worklists
			for(Thread t : worker){
				if(t != null){
					try{t.join();}catch(InterruptedException ie){}
				}
			}
		}catch(InterruptedException e){}
		
		etaTracker.interrupt(); // don't need this anymore since we are finished
	}

	@Override
	public void Cancel() {
		stop = true;
		if(producer != null){
			producer.interrupt();
		}
		
		pendingFiles.clear();
		
		for(Thread t : worker){
			if(t != null){
				t.interrupt();
			}
		}
	}
	
	class ImageVisitor extends SimpleFileVisitor<Path>{
		final String[] ignoredDir = {BLACKLISTED_DIR,DNW_DIR, "$RECYCLE.BIN", "System Volume Information"};
		final ArrayList<Path> ignoredPaths = new ArrayList<>(ignoredDir.length);
		final ArrayList<String> indexed;
		
		ImageFilter imgFilter = new ImageFilter();
		boolean skip = false;
		
		public ImageVisitor(ArrayList<String> indexed){
			this.indexed = indexed;
			
			for(String s : ignoredDir){
				ignoredPaths.add(Paths.get(getPath()).getRoot().resolve(s));
			}
			
			if(indexSkip.isSelected()){
				skip = true;
			}
		}
		
		
		@Override
		public FileVisitResult preVisitDirectory(Path arg0, BasicFileAttributes arg1) throws IOException {
			// don't go there...
			if(ignoredPaths.contains(arg0)){
				return FileVisitResult.SKIP_SUBTREE;
			}
			
			setStatus("Scanning " + arg0.toString());
			return super.preVisitDirectory(arg0, arg1);
		}
		
		@Override
		public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
			if(stop){
				return FileVisitResult.TERMINATE;
			}
			
			return super.postVisitDirectory(dir, exc);
		}
		
		@Override
		public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
			error("Could not read file: " + exc.getMessage());
			return FileVisitResult.CONTINUE;
		}
		
		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)throws IOException {
			String filename = file.getFileName().toString();

			if(! filename.startsWith(BLACKLISTED_TAG) && imgFilter.accept(null, filename)){
				if(skip){
					if(Collections.binarySearch(indexed, FileUtil.removeDriveLetter(file).toString().toLowerCase()) >= 0){
						statSkipped++;
						return FileVisitResult.CONTINUE;
					}
				}

				pendingFiles.add(file);
			}else if (filename.startsWith(BLACKLISTED_TAG)){
				addBlacklisted(file.getParent());
			}
			return super.visitFile(file, attrs);
		}
	}
	
	private void renameFile(Path path, String hash){
		// create filename with prefix WARNING-{hash}-
		StringBuilder sb = new StringBuilder();
		sb.append(BLACKLISTED_TAG);
		sb.append(hash);
		sb.append("-");
		sb.append(path.getFileName().toString());
		
		// rename file (ex. from JavaDoc)
		try {
			Files.move(path, path.resolveSibling(sb.toString()));
		} catch (IOException e) {
			error("Could not move file " + path.toString() + " ("+ e.getMessage() + ")");
			e.printStackTrace();
		}
		warning("Blacklisted file found in " + path.getParent().toString());
	}
	
	private void addBlacklisted(Path path){
		if(! blacklistedDir.contains(path)){
			blacklistedDir.add(path);
		}
	}
	
	private void moveBlacklisted(){
		for(Path p : blacklistedDir){
			try {
				FileUtil.moveFileWithStructure(p, p.getRoot().resolve(BLACKLISTED_DIR));
				statDir++;
			} catch (IOException e) {
				error("Could not move directory " + p.toString() + " ("+ e.getMessage() + ")");
			}
		}
	}
	
	private void pruneIndex(){
		int pruned = 0;
		int counter = 0;
		AidDAO sql = new AidDAO(getConnectionPool());
		StopWatch swPrune = new StopWatch();
		
		ArrayList<String> index = loadIndexedFiles(locationTag);
		
		swPrune.start();
		info("Pruning index...");
		setStatus("Pruning index...");
		progressBar.setMaximum(index.size());
		
		for(String s : index){
			if(stop){
				break;
			}
			
			Path path = Paths.get(drive).resolve(s);
			
			if(! Files.exists(path)){
				sql.deleteIndexByPath(s);
				sql.deleteDuplicateByPath(s);
				pruned++;
			}
			
			counter++;
			progressBar.setValue(counter);
		}
		swPrune.stop();
		info("Pruned " + pruned + " entries from the index in " + swPrune.getTime());
	}
	
	private ArrayList<String> loadIndexedFiles(String location){
			ArrayList<String> indexed;
			StopWatch swLoad = new StopWatch();
			
			swLoad.start();
			info("Fetching file list from DB...");
			indexed = new AidDAO(getConnectionPool()).getLocationFilelist(location);
			info("Loaded "+indexed.size()+" index entries from the DB");
			info("Performing sort...");
			Collections.sort(indexed); // sort the list so we can use binary search
			swLoad.stop();
			
			info("Indexed list ready...(" + swLoad.getTime() +")");
			
			return indexed;
	}
	
	class DataProducer extends Thread {
		BinaryFileReader bfr = new BinaryFileReader();
		
		public DataProducer(){
			super("Data Producer");
		}
		
		@Override
		public void run() {
			while(! isInterrupted() && (! pendingFiles.isEmpty())){
				Path p = pendingFiles.remove();

				try {
					try {
						dataQueue.put(new FileData(p, bfr.get(p.toFile())));
					} catch (InterruptedException e) {
						interrupt();
					}
				} catch (IOException e) {
					error("Failed to read " + e.getMessage());
				}
			}
		}
	}
	
	class FileData{
		final byte[] data;
		final Path file;
		
		public FileData(Path file, byte[] data) {
			this.file = file;
			this.data = data;
		}
	}
	
	class DBWorker extends Thread{
		AidDAO sql = new AidDAO(getConnectionPool());
		HashMaker hm = new HashMaker();
		
		public DBWorker(){
			super("DB Worker");
		}
		
		@Override
		public void run() {
			boolean blc = blCheck.isSelected();
			boolean index = indexCheck.isSelected();
			boolean dnw = dnwCheck.isSelected();
			
			while((! isInterrupted()) && (producer.isAlive() || (! dataQueue.isEmpty()))){
				String hash;
				FileData fd;
				
				try {
					fd = dataQueue.take();
					
					hash = hm.hash(fd.data);
					// track stats
					statHashed++;

					// see if any files are blacklisted
					if(blc && sql.isBlacklisted(hash)){
						statBlocked++;
						renameFile(fd.file, hash);
						addBlacklisted(fd.file.getParent());
						continue;
					}
					
					// see if there are any DNW files
					if(dnw && sql.isDnw(hash)){
						if(dnwLog.isSelected()){
							info("Found DNW " + fd.file.toString());
						}else if(dnwDelete.isSelected()){
							try {
								Files.delete(fd.file);
								info("Deleted DNW " + fd.file.toString());
							} catch (IOException e) {
								error("Failed to delete DNW " + fd.file.toString());
							}
						}else if(dnwMove.isSelected()){
							try {
								FileUtil.moveFileWithStructure(fd.file, fd.file.getRoot().resolve(DNW_DIR));
								info("Moved DNW " + fd.file.toString() + " to " + fd.file.getRoot().resolve(DNW_DIR).toString());
							} catch (IOException e) {
								error("Failed to move DNW " + fd.file.toString());
							}
						}
						
						continue;
					}
					
					//index the file
					if(index){
						File f = fd.file.toFile();
						if(sql.isHashed(hash)){
							String path = sql.getPath(hash);
							if(path == null || (! path.equals(f.toString().toLowerCase()))){
								sql.addDuplicate(hash, f.toString(), f.length(), locationTag);
							}
						}else{
							if(! sql.addIndex(hash, f.toString(), f.length(), locationTag)){
								error("Failed to add index for " + f.toString() + " - " + hash);
							}
						}
					}
				} catch (InterruptedException e) {
					interrupt();
				}
			}
		}
	}
	
	/**
	 * This thread calculates the estimated time remaining until
	 * the task is complete.
	 */
	class EtaTracker extends Thread {
		int before, after;
		final int POLL_INTERVALL = 5;
		final int WINDOW_SIZE = 12;
		
		LinkedList<Integer> window = new LinkedList<>();
		
		public EtaTracker(){
			super("EtaTracker");
			
			// init window
			for(int i=0; i<WINDOW_SIZE; i++){
				window.add(0);
			}
		}
		
		@Override
		public void run() {
			while(! isInterrupted()){
				try {
					pollDelta();
					calcTime();
					updateGUI();
				} catch (InterruptedException e) {
					interrupt();
				}
				
			}
			
			updateGUI();
		}
		
		private void pollDelta() throws InterruptedException{
			before = pendingFiles.size();
			sleep(POLL_INTERVALL * 1000);
			after = pendingFiles.size();
			
			int delta = before - after;
			
			// invalid value
			if(delta <= 0){
				duration = "--:--:--";
				return;
			}
			
			window.pop();
			window.add(delta);
		}
		
		private void updateGUI() {
			// display some stats while chewing through those files
			setStatus("Time remaining:  " + duration);
			progressBar.setValue(statHashed);
		}

		private void calcTime(){
			int count = 0, mean = 0;
			
			for(int i : window){
				if(i > 0){
					mean += i;
					count++;
				}
			}
			
			mean = mean / count;
			
			int seconds = (pendingFiles.size() / mean) * POLL_INTERVALL;
			
			int hours = seconds / (60*60);
			seconds =  seconds - (hours * 60 * 60);
			int minutes = seconds / 60;
			seconds = seconds - (minutes * 60);
			
			final String template = "%1$02d:%2$02d:%3$02d"; //  hours:minutes:seconds , with leading zero if necessary 
			
			duration = String.format(template, hours,minutes,seconds);
		}
	}
	
	class ImageFilter implements FilenameFilter {

		@Override
		public boolean accept(File dir, String name) {
			if(name == null){
				return false;
			}
			
			int extIndex = name.lastIndexOf(".");
			
			// has no extension
			if(extIndex == -1){
				return false;
			}
			
			String ext = name.substring(extIndex+1);
			
			if(ext.equals("jpg") || ext.equals("png") || ext.equals("gif")){
				return true;
			}
			return false;
		}
		
	}
}
