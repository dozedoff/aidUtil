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
package com.github.dozedoff.aidUtil.module.archiveIndexer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.dozedoff.commonj.io.StreamGobbler;
import com.github.dozedoff.commonj.thread.ProcessWatchDog;
public class ArchiveUnpacker {
	private final Path sevenZipAppPath;
	private final long TIMEOUT = 3 * 60 * 1000;
	
	private Logger logger = LoggerFactory.getLogger(ArchiveUnpacker.class);
	
	public ArchiveUnpacker(Path sevenZipAppPath) {
		this.sevenZipAppPath = sevenZipAppPath;
	}

	public  void unpack(Path archive, Path tempFolder) throws IOException, UnpackException{
		if(! Files.exists(archive)){
			logger.warn("Could not find archive {}", archive);
			throw new FileNotFoundException("Source file not Found");
		}

		if(! Files.exists(tempFolder)){
			logger.warn("Could not find unpack destination folder {}", tempFolder);
			throw new FileNotFoundException("Destination folder not Found");
		}

		
		// -aos: skip if dest exists -y: answer yes to all -o: output directory -r: recursive -Pfoobar: use password foobar
		String separator = FileSystems.getDefault().getSeparator();
		String command = sevenZipAppPath + separator +"7z x \""+archive.toString()+"\" -aos -y -o\""+tempFolder.toString()+"\" -r -pfoobar";
		logger.info("Unpacking archive with command {}", sevenZipAppPath);
		
		Process process = Runtime.getRuntime().exec(command);
		ProcessWatchDog watchDog =  new ProcessWatchDog(command, process, TIMEOUT);
		
		watchDog.start();

		StreamGobbler sge = new StreamGobbler(process.getErrorStream());
		StreamGobbler sgo = new StreamGobbler(process.getInputStream());

		sge.start();
		sgo.start();
		
		try {sge.join();} catch (InterruptedException e) {}
		try {sgo.join();} catch (InterruptedException e) {}

		// catch App hangs
		
		try {process.waitFor();} catch (InterruptedException e) {}
		
		watchDog.interrupt();
		
		if(process.exitValue() != 0){
			if(sgo.getBuffer().contains("Can not open encrypted archive")){
				throw new UnpackException(UnpackException.INVALID_PASSWORD, archive);
			}
			
			logger.debug("7z output stream {}",sgo.getBuffer());
			logger.debug("7z error stream {}",sge.getBuffer());
			throw new UnpackException(process.exitValue(), archive);
		}
		
		logger.info("Unpacked archive {}", archive);
	}
}
