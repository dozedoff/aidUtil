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
import io.MySQL;

import java.awt.Container;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;

import time.StopWatch;
import file.BinaryFileReader;
import file.FileUtil;

public class ModuleMarkBlocked extends MaintenanceModule {
	LinkedBlockingDeque<Path> pendingFiles = new LinkedBlockingDeque<>();
	LinkedList<Path> blacklistedDir = new LinkedList<>();
	
	Thread worker , worker2, etaTracker;
	StopWatch stopWatch = new StopWatch();
	AtomicInteger statHashed = new AtomicInteger(0);
	
	// stats
	int statBlocked, statDir;
	boolean stop = false;
	String duration;
	
	@Override
	public void optionPanel(Container container) {
		info(ModuleMarkBlocked.class.getSimpleName() + "selected");
	}

	@Override
	public void start() {
		// reset stats
		statHashed.set(0);
		statBlocked = 0;
		statDir = 0;
		stopWatch.reset();
		duration = "--:--:--";
		
		// reset stop flag
		stop = false;
		
		// clear list
		blacklistedDir.clear();
		
		File f = new File(getPath());
		
		if((! f.exists()) || (! f.isDirectory())){	
			error("Target Directory is invalid.");
			return;
		}
		
		stopWatch.start();
		
		info("Walking directories...");
		try {
			// go find those files...
			Files.walkFileTree( f.toPath(), new FileHasher());
		} catch (IOException e) {
			error("File walk failed");
			e.printStackTrace();
		}
		
		info("Starting worker thread...");
		//TODO needs improving...
		if(worker != null && worker.isAlive()){
			error("Worker is already running!");
			return;
		}
		
		etaTracker = new EtaTracker();
		etaTracker.start();
		
		worker = new DBWorker();
		worker.start();
		
		worker2 = new DBWorker();
		worker2.start();
		
		while(! pendingFiles.isEmpty()){
			try {
				// display some stats while chewing through those files
				setStatus("Remaining: " + pendingFiles.size() + " / " + duration);
				Thread.sleep(2000);
			} catch (InterruptedException e) {}
		}
		
		info("Wating for worker threads to finish...");
		try{
			// wait for threads to clear the worklists
			worker.join();
			worker2.join();
		}catch(InterruptedException e){}
		
		etaTracker.interrupt(); // don't need this anymore since we are finished
		
		info("Moving blacklisted directories...");
		moveBlacklisted();
		
		stopWatch.stop();
		
		info("Blocked files marking done. " + statHashed +" files hashed, " + statBlocked +" blacklisted files found, " + statDir + " blacklisted Directories moved.");
		info("Mark blacklisted run duration - " + stopWatch.getTime());
		
		setStatus("Finished");
	}

	@Override
	public void Cancel() {
		stop = true;
		pendingFiles.clear();
	}
	
	class FileHasher extends SimpleFileVisitor<Path>{
		ImageFilter imgFilter = new ImageFilter();
		
		@Override
		public FileVisitResult preVisitDirectory(Path arg0, BasicFileAttributes arg1) throws IOException {
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
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)throws IOException {
			String filename = file.getFileName().toString();
			if(! filename.startsWith("WARNING-") && imgFilter.accept(null, filename)){
					pendingFiles.add(file);
			}else if (filename.startsWith("WARNING-")){
				addBlacklisted(file.getParent());
			}
			return super.visitFile(file, attrs);
		}
	}
	
	private void renameFile(Path path, String hash){
		// create filename with prefix WARNING-{hash}-
		StringBuilder sb = new StringBuilder();
		sb.append("WARNING-");
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
		info("Blacklisted file found in " + path.getParent().toString());
	}
	
	private void addBlacklisted(Path path){
		if(! blacklistedDir.contains(path)){
			blacklistedDir.add(path);
		}
	}
	
	private void moveBlacklisted(){
		for(Path p : blacklistedDir){
			try {
				FileUtil.moveDirectory(p, p.getRoot().resolve("CHECK"));
				statDir++;
			} catch (IOException e) {
				error("Could not move directory " + p.toString() + " ("+ e.getMessage() + ")");
			}
		}
	}
	
	class DBWorker extends Thread {
		LinkedList<Path> workList = new LinkedList<>();
		MySQL sql = new MySQL(getConnectionPool());
		
		BinaryFileReader bfr = new BinaryFileReader();
		HashMaker hm = new HashMaker();
		
		public DBWorker(){
			super("DBWorker");
		}
		
		@Override
		public void run() {
			while(! isInterrupted() && (! pendingFiles.isEmpty())){
				pendingFiles.drainTo(workList,200); // get work
				
				for(Path p : workList){
					String hash;
					try {
						// read & hash file
						hash = hm.hash(bfr.getViaDataInputStream(p.toFile()));
						
						// track stats
						statHashed.incrementAndGet();
						
						// see if any files are blacklisted
						if(sql.isBlacklisted(hash)){
							statBlocked++;
							renameFile(p, hash);
							addBlacklisted(p.getParent());
						}
					} catch (IOException e) {
						error("Failed to process " + p.toString() + " (" + e.getMessage() + ")");
						e.printStackTrace();
					}
				}
				
				workList.clear();
			}
		}
	}
	
	class EtaTracker extends Thread {
		int before, after;
		
		public EtaTracker(){
			super("EtaTracker");
		}
		
		@Override
		public void run() {
			while(! isInterrupted()){
				try {
					before = pendingFiles.size();
					sleep(20 * 1000);
					after = pendingFiles.size();
					
					if((before - after) <= 0){
						duration = "--:--:--";
					}
					
					int seconds = (pendingFiles.size() / (before - after)) * 20;
					
					int hours = seconds / (60*60);
					seconds =  seconds - (hours * 60 * 60);
					int minutes = seconds / 60;
					seconds = seconds - (minutes * 60);
					
					duration = hours + ":" + minutes + ":" + seconds;
				} catch (InterruptedException e) {
					interrupt();
				}
				
			}
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
