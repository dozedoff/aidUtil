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
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import time.StopWatch;
import util.LocationTag;
import file.BinaryFileReader;
import file.FileUtil;

public class ModuleManageBlacklisted extends MaintenanceModule {
	final String BLACKLISTED_TAG = "WARNING-";
	final String BLACKLISTED_DIR = "CHECK";
	
	final int WORKERS = 2; // number of threads hashing data and communicating with the DB
	final int FILE_QUEUE_SIZE = 100; // setting this too high will probably result in "out of memory" errors

	LinkedList<Path> pendingFiles = new LinkedList<>();
	LinkedList<Path> blacklistedDir = new LinkedList<>();
	LinkedBlockingQueue<FileData> dataQueue = new LinkedBlockingQueue<>(FILE_QUEUE_SIZE);

	Thread worker[] = new Thread[WORKERS], producer, etaTracker;
	StopWatch stopWatch = new StopWatch();
	volatile int statHashed = 0;
	String locationTag = null;
	
	// GUI
	JPanel panelBlacklist = new JPanel();
	JPanel panelDnw = new JPanel();
	JPanel panelIndex = new JPanel();
	
	JCheckBox blMoveTagged = new JCheckBox("Move only (no hashing)");
	JCheckBox blCheck = new JCheckBox("Check for blacklisted");
	
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
	
	public ModuleManageBlacklisted() {
		super();
		moduleName = "Manage Blacklisted";
	}
	
	@Override
	public void optionPanel(Container container) {
		panelBlacklist.add(blCheck);
		panelBlacklist.add(blMoveTagged);
		panelBlacklist.setBorder(BorderFactory.createTitledBorder("Blacklist"));
		container.add(panelBlacklist);
		
		panelDnw.add(dnwCheck);

		
		//TODO not working...
		// disable buttons when not in use
		dnwCheck.addChangeListener(new ChangeListener() {
			JRadioButton buttons[] = {dnwMove, dnwDelete, dnwLog};
			@Override
			public void stateChanged(ChangeEvent arg0) {
				if(dnwCheck.isSelected()){
					for(JRadioButton b : buttons){
						b.setEnabled(true);
					}
				}else{
					for(JRadioButton b : buttons){
						b.setEnabled(true);
					}
				}
			}
		});
		
		dnwGroup.add(dnwMove);
		dnwGroup.add(dnwLog);
		dnwGroup.add(dnwDelete);
		dnwLog.setSelected(true);
		
		panelDnw.add(dnwMove);
		panelDnw.add(dnwDelete);
		panelDnw.add(dnwLog);
		panelDnw.setBorder(BorderFactory.createTitledBorder("DNW"));
		container.add(panelDnw);
		
		panelIndex.add(indexCheck);
		panelIndex.setBorder(BorderFactory.createTitledBorder("Index"));
		container.add(panelIndex);
		
		progressBar.setPreferredSize(new Dimension(200, 30));
		progressBar.setStringPainted(true);
		container.add(progressBar);
		
		container.repaint();
	}

	@Override
	public void start() {
		// reset stats
		statHashed = 0;
		statBlocked = 0;
		statDir = 0;
		stopWatch.reset();
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
		
		locationTag = checkTag(f.toPath());
		
		if(locationTag == null){
			return;
		}
		
		stopWatch.start();
		
		info("Walking directories...");
		try {
			// go find those files...
			Files.walkFileTree( f.toPath(), new ImageVisitor());
		} catch (IOException e) {
			error("File walk failed");
			e.printStackTrace();
		}
		
		lookForBlacklisted();

		if(blMoveTagged.isSelected()){
			info("Moving blacklisted directories...");
			moveBlacklisted();
		}
		
		stopWatch.stop();
		
		info("Blocked files marking done. " + statHashed +" files hashed, " + statBlocked +" blacklisted files found, " + statDir + " blacklisted Directories moved.");
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
		producer.interrupt();
		pendingFiles.clear();
		
		for(Thread t : worker){
			if(t != null){
				t.interrupt();
			}
		}
	}
	
	class ImageVisitor extends SimpleFileVisitor<Path>{
		ImageFilter imgFilter = new ImageFilter();
		
		@Override
		public FileVisitResult preVisitDirectory(Path arg0, BasicFileAttributes arg1) throws IOException {
			// don't go there...
			if((arg0.getFileName() != null) && arg0.getFileName().toString().equals("$Recycle.Bin")){
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
				FileUtil.moveDirectory(p, p.getRoot().resolve(BLACKLISTED_DIR));
				statDir++;
			} catch (IOException e) {
				error("Could not move directory " + p.toString() + " ("+ e.getMessage() + ")");
			}
		}
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
							info("Found DNW " + fd.toString());
						}else if(dnwDelete.isSelected()){
							try {
								Files.delete(fd.file);
								info("Deleted DNW " + fd.file.toString());
							} catch (IOException e) {
								error("Failed to delete DNW " + fd.file.toString());
							}
						}else if(dnwMove.isSelected()){
							try {
								Files.move(fd.file, fd.file.getRoot().resolve("DNW").resolve(fd.file.getFileName()));
								info("Moved DNW " + fd.file.toString() + " to " + fd.file.getRoot().resolve("DNW").toString());
							} catch (IOException e) {
								error("Failed to move DNW " + fd.file.toString());
							}
						}
						
						continue;
					}
					
					//index the file
					if(index){
						File f = fd.file.toFile();
						sql.addIndex(hash, f.toString(), f.length(), locationTag);
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
