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

import file.BinaryFileReader;

public class ModuleMarkBlocked extends MaintenanceModule {
	
	
	
	@Override
	public void optionPanel(Container container) {
		// TODO Auto-generated method stub

	}

	@Override
	public void start() {
		
		File f = new File(getPath());
		
		if(f.exists() && f.isDirectory()){		
			try {
				Files.walkFileTree( f.toPath(), new FileWalk());
			} catch (IOException e) {
				log("[ERR] File walk failed");
				e.printStackTrace();
			}
		}else{
			log("[ERR] Target Directory is invalid.");
		}

	}

	@Override
	public void Cancel() {
		// TODO Auto-generated method stub

	}
	
	class FileWalk extends SimpleFileVisitor<Path>{
		BinaryFileReader bfr = new BinaryFileReader();
		HashMaker hm = new HashMaker();
		MySQL sql = new MySQL(getConnectionPool());
		
		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)throws IOException {
			if(! file.getFileName().toString().startsWith("WARNING-")){
				String hash = hm.hash(bfr.get(file.toFile()));
				if(sql.isBlacklisted(hash)){
					// create filename with prefix WARNING-{hash}-
					StringBuilder sb = new StringBuilder();
					sb.append("WARNING-");
					sb.append(hash);
					sb.append("-");
					sb.append(file.getFileName().toString());
					
					// rename file (ex. from JavaDoc)
					Files.move(file, file.resolveSibling(sb.toString()));
					log("[INF] "+sb.toString());
				}
			}
			return super.visitFile(file, attrs);
		}
	}

}
