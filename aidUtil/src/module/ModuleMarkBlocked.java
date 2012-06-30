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
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingDeque;

import file.BinaryFileReader;

public class ModuleMarkBlocked extends MaintenanceModule {
	LinkedBlockingDeque<HashedFile> hashedFiles = new LinkedBlockingDeque<>();
	DBWorker worker;
	
	int statHashed, statBlocked;
	
	@Override
	public void optionPanel(Container container) {
		// TODO Auto-generated method stub

	}

	@Override
	public void start() {
		// reset stats
		statHashed = 0;
		statBlocked = 0;
		
		File f = new File(getPath());
		
		if((! f.exists()) || (! f.isDirectory())){	
			log("[ERR] Target Directory is invalid.");
			return;
		}
		
		log("[INF] Starting worker thread...");
		if(worker != null && worker.isAlive()){
			log("[ERR] Worker is already running!");
			return;
		}
		
		worker = new DBWorker();
		worker.start();
		
		log("[INF] Walking directories...");
		try {
			Files.walkFileTree( f.toPath(), new FileHasher());
		} catch (IOException e) {
			log("[ERR] File walk failed");
			e.printStackTrace();
		}
		
		log("[INF] Finished hashing files");
		
		while(! hashedFiles.isEmpty()){
			log("[INF] Wating for queue to clear...");
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {}
		}
		
		log("[INF] Stopping worker thread...");
		worker.interrupt();
		
		log("[INF] Blocked files marking done. " + statHashed +" files hashed, " + statBlocked +" blacklisted files found.");
	}

	@Override
	public void Cancel() {
		// TODO Auto-generated method stub

	}
	
	class FileHasher extends SimpleFileVisitor<Path>{
		BinaryFileReader bfr = new BinaryFileReader();
		HashMaker hm = new HashMaker();
		
		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)throws IOException {
			if(! file.getFileName().toString().startsWith("WARNING-")){
				String hash = hm.hash(bfr.get(file.toFile()));
				statHashed++;
				
				synchronized (hashedFiles) {
					hashedFiles.add(new HashedFile(hash, file));
					hashedFiles.notify();
				}
			}
			return super.visitFile(file, attrs);
		}
	}
	
	private void renameFile(HashedFile hf){
		// create filename with prefix WARNING-{hash}-
		StringBuilder sb = new StringBuilder();
		sb.append("WARNING-");
		sb.append(hf.getHash());
		sb.append("-");
		sb.append(hf.getFile().getFileName().toString());
		
		// rename file (ex. from JavaDoc)
		try {
			Files.move(hf.getFile(), hf.getFile().resolveSibling(sb.toString()));
		} catch (IOException e) {
			log("[ERR] could not move file " + hf.getFile().toString() + " ("+ e.getMessage() + ")");
			e.printStackTrace();
		}
		log("[INF] "+sb.toString());
	}
	
	class HashedFile {
		String hash;
		Path file;
		public HashedFile(String hash, Path file) {
			this.hash = hash;
			this.file = file;
		}
		public String getHash() {
			return hash;
		}
		public Path getFile() {
			return file;
		}
	}
	
	class DBWorker extends Thread {
		LinkedList<HashedFile> workList = new LinkedList<>();
		MySQL sql = new MySQL(getConnectionPool());
		
		public DBWorker(){
			super("DBWorker");
		}
		
		@Override
		public void run() {
			while(! isInterrupted()){
				
				synchronized (hashedFiles) {
					// wait for work
					while(hashedFiles.isEmpty() && (! isInterrupted())){
						try {
							hashedFiles.wait();
						} catch (InterruptedException e) {
							interrupt(); // IE causes interrupted flag to clear
						}
					}
				}
				
				if(isInterrupted()){
					break;
				}
				
				hashedFiles.drainTo(workList); // get work
				
				for(HashedFile hf : workList){
					// see if any files are blacklisted
					if(sql.isBlacklisted(hf.getHash())){
						statBlocked++;
						renameFile(hf);
					}
				}
				
				workList.clear();
			}
		}
	}
}
