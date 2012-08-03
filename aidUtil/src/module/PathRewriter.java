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

import java.nio.file.Path;

public class PathRewriter {
	Path tempDirectory;
	
	public PathRewriter(Path tempDirectory) {
		this.tempDirectory = tempDirectory;
	}

	public Path reWritePath(Path sourcePath, Path archivePath) {
		Path reWritten = null;
		
		Path relativeSource = tempDirectory.relativize(sourcePath);
		reWritten = archivePath.resolve(relativeSource);
		
		return reWritten;
	}
}
