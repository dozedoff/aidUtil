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

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

public class ArchiveFinder {
	LinkedList<Path> archives;
	
	public static LinkedList<Path> find(Path directory) throws IOException{
		LinkedList<Path> archives = new LinkedList<>();
		Files.walkFileTree(directory, new ArchiveVisitor(archives));
		return archives;
	}
}

class ArchiveFilter implements FilenameFilter {
	final String[] vaildExtensions = {"7z", "XZ", "BZIP2", "GZIP", "TAR", "ZIP", "WIM", "ARJ", "CAB", "CHM", "CPIO", "CramFS", "DEB", "DMG", "FAT", "HFS", "ISO", "LZH", "LZMA", "MBR", "MSI", "NSIS", "NTFS", "RAR", "RPM", "SquashFS", "UDF", "VHD", "XAR", "Z"};
	final ArrayList<String> vaildArchiveExtensions = new ArrayList<>(vaildExtensions.length);

	public ArchiveFilter(){
		for(String validExtension : vaildExtensions){
			vaildArchiveExtensions.add(validExtension.toLowerCase());
		}
		
		Collections.sort(vaildArchiveExtensions);
	}
	
	@Override
	public boolean accept(File dir, String filename) {
		int extensionIndex = filename.lastIndexOf(".") + 1;
		String fileExtension = filename.substring(extensionIndex).toLowerCase();
		
		if(extensionIndex == -1){
			return false;
		}
		
		if(Collections.binarySearch(vaildArchiveExtensions, fileExtension) >= 0){
			return true;
		}
		
		return false;
	}
}

class ArchiveVisitor extends SimpleFileVisitor<Path> {
	ArchiveFilter archiveFilter = new ArchiveFilter();
	LinkedList<Path> archiveList;
	
	public ArchiveVisitor(LinkedList<Path> archiveList) {
		this.archiveList = archiveList;
	}

	@Override
	public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) throws IOException {
		if(archiveFilter.accept(null, file.getFileName().toString())){
			archiveList.add(file);
		}
		
		return FileVisitResult.CONTINUE;
	}
}