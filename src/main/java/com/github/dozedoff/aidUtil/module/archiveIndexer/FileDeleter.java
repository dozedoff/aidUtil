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

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.logging.Logger;

public class FileDeleter {
	public static void deleteAll(Path directory) throws IOException{
		Files.walkFileTree(directory, new DeleteVisitor(directory));
	}
}

class DeleteVisitor extends SimpleFileVisitor<Path> {
	private static final Logger logger = Logger.getLogger(DeleteVisitor.class.getName());
	Path startingDirectory;

	public DeleteVisitor(Path startingDirectory) {
		this.startingDirectory = startingDirectory;
	}

	@Override
	public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
		delete(file);
		return FileVisitResult.CONTINUE;
	}
	
	@Override
	public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
		if(dir.equals(startingDirectory)){
			return FileVisitResult.CONTINUE;
		}
		
		delete(dir);
		return FileVisitResult.CONTINUE;
	}
	
	private void delete(Path path) {
		try {
			Files.deleteIfExists(path);
		} catch (IOException e) {
			logger.warning("Unable to delete file or directory " + e.getMessage());
		}
	}
}