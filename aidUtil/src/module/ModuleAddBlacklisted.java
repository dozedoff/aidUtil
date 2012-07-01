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

import hash.DirectoryHasher;
import hash.HashMaker;
import io.MySQL;
import io.MySQLtables;

import java.awt.Container;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.LinkedBlockingQueue;

import module.ModuleMarkBlocked.HashedFile;
import module.ModuleMarkBlocked.ImageFilter;

import time.StopWatch;
import file.BinaryFileReader;
import file.FileInfo;

public class ModuleAddBlacklisted extends MaintenanceModule {
	StopWatch stopWatch = new StopWatch();
	MySQL sql;
	
	int statHashed = 0;
	boolean stop = false;
	
	@Override
	public void optionPanel(Container container) {
		// TODO Auto-generated method stub

	}

	@Override
	public void start() {
		stop = false;
		statHashed = 0;
		
		sql = new MySQL(getConnectionPool());
		
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
	}

	@Override
	public void Cancel() {
		// TODO Auto-generated method stub

	}
	
	class FileHasher extends SimpleFileVisitor<Path>{
		BinaryFileReader bfr = new BinaryFileReader();
		HashMaker hm = new HashMaker();
		
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
				String hash = hm.hash(bfr.getViaDataInputStream(file.toFile()));
				statHashed++;
				
				sql.update(hash, MySQLtables.Block);
			return super.visitFile(file, attrs);
		}
	}
}
