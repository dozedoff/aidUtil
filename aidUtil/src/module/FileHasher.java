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

import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;

import file.BinaryFileReader;
import file.FileInfo;

public class FileHasher extends Thread{
	LinkedBlockingQueue<FileInfo> inputQueue, outputQueue;
	HashMaker hashMaker = new HashMaker();
	BinaryFileReader binaryFileReader = new BinaryFileReader();
	
	public FileHasher(LinkedBlockingQueue<FileInfo> inputQueue, LinkedBlockingQueue<FileInfo> outputQueue) {
		super("FileHasher");
		this.inputQueue = inputQueue;
		this.outputQueue = outputQueue;
	}

	@Override
	public void run() {
		try {
			hashFiles();
		} catch (InterruptedException e) {
			interrupt();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void hashFiles() throws InterruptedException, IOException{
		FileInfo currentFile;
		while(! interrupted()){
			currentFile = inputQueue.take();
			currentFile.setHash(hashMaker.hash(binaryFileReader.get(currentFile.getFile())));
			outputQueue.put(currentFile);
		}
	}
}
