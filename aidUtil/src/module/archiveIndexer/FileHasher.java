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

import file.BinaryFileReader;
import file.FileInfo;
import hash.HashMaker;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.LinkedBlockingQueue;

public class FileHasher {
	LinkedBlockingQueue<ArchiveFile> inputQueue, outputQueue;
	HashMaker hashMaker = new HashMaker();
	BinaryFileReader binaryFileReader = new BinaryFileReader();
	
	public FileHasher(LinkedBlockingQueue<ArchiveFile> inputQueue, LinkedBlockingQueue<ArchiveFile> outputQueue) {
		this.inputQueue = inputQueue;
		this.outputQueue = outputQueue;
	}
	
	public void hashFiles() throws InterruptedException, IOException{
		FileInfo currentFile;
		ArchiveFile archiveFile;
		
		while(! inputQueue.isEmpty()){
			archiveFile = inputQueue.take();
			currentFile = archiveFile.getFileInfo();
			currentFile.setHash(hashFile(currentFile.getFilePath()));
			currentFile.setSize(Files.size(currentFile.getFilePath()));
			outputQueue.put(archiveFile);
		}
	}
	
	private String hashFile(Path filepath) throws IOException {
		return hashMaker.hash(binaryFileReader.get(filepath.toFile()));
	}
}
