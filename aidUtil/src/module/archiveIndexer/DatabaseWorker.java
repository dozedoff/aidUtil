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

import java.nio.file.Path;
import java.util.concurrent.LinkedBlockingQueue;


import file.FileInfo;

public class DatabaseWorker extends Thread{
	private final DatabaseHandler dbHandler;
	private final LinkedBlockingQueue<ArchiveFile> outputQueue;
	private final OperationMode mode;
	private final PathRewriter reWriter;
	
	public static enum OperationMode {AddToIndex, AddToDNW};
	
	public DatabaseWorker(DatabaseHandler dbHandler, LinkedBlockingQueue<ArchiveFile> outputQueue, PathRewriter reWriter, OperationMode mode) {
		super("Database worker");
		this.dbHandler = dbHandler;
		this.outputQueue = outputQueue;
		this.reWriter = reWriter;
		this.mode = mode;
	}
	
	@Override
	public void run() {
		try {
			doWork();
		} catch (InterruptedException e) {
			interrupt();
		}
	}
	
	private void doWork() throws InterruptedException {
		while(! isInterrupted()){
			ArchiveFile archiveFile = outputQueue.take();
			FileInfo currentFile = reWritePath(archiveFile);
			addToDatabse(currentFile);
		}
	}
	
	private void addToDatabse(FileInfo info){
		switch (mode) {
		case AddToIndex:
			dbHandler.addIndex(info);
			break;
			
		case AddToDNW:
			dbHandler.addDnw(info);
			break;
			
		default:
			break;
		}
	}
	
	private FileInfo reWritePath(ArchiveFile archiveFile){
		FileInfo info = archiveFile.getFileInfo();
		Path archivePath = archiveFile.getArchivePath();
		
		info.setFile(reWriter.reWritePath(info.getFilePath(), archivePath));
		
		return info;
	}
}
