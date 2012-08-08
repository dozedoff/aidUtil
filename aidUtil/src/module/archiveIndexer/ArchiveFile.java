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

import file.FileInfo;

public class ArchiveFile {
	private final Path archivePath;
	private final FileInfo fileInfo;

	public ArchiveFile(FileInfo fileInfo, Path archivePath) {
		this.fileInfo = fileInfo;
		this.archivePath = archivePath;
	}

	public Path getArchivePath() {
		return archivePath;
	}

	public FileInfo getFileInfo() {
		return fileInfo;
	}
}
