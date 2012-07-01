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
import io.MySQL;
import io.MySQLtables;

import java.awt.Container;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;

import time.StopWatch;
import file.FileInfo;

public class ModuleAddBlacklisted extends MaintenanceModule {
	LinkedBlockingQueue<FileInfo> hashedFiles = new LinkedBlockingQueue<>();
	StopWatch stopWatch = new StopWatch();
	MySQL sql;
	
	int hashed = 0;
	
	@Override
	public void optionPanel(Container container) {
		// TODO Auto-generated method stub

	}

	@Override
	public void start() {
		sql = new MySQL(getConnectionPool());
		hashed = 0;
		File path = new File(getPath());
		
		if(!path.exists() || !path.isDirectory()){
			error("Invalid directory");
			return;
		}
		
		stopWatch.start();
		info("Hashing files...");
		DirectoryHasher dh = new DirectoryHasher(hashedFiles);
		try {
			dh.hashDirectory(getPath()); //TODO replace this
		} catch (IOException e) {
			error("Directory hashing failed: " + e.getMessage());
		}
		info("Hashing done.");
		
		info("Adding hashes to database...");
		hashed = hashedFiles.size();
		
		for(FileInfo fi : hashedFiles){
			sql.update(fi.getHash(),MySQLtables.Block);
		}
		stopWatch.stop();
		info("Finished processing " + hashed + " blacklisted files in " + stopWatch.getTime());
	}

	@Override
	public void Cancel() {
		// TODO Auto-generated method stub

	}
}
