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
package archiveIndexer;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.nio.file.Path;
import java.nio.file.Paths;

import module.archiveIndexer.ArchiveFile;
import module.archiveIndexer.PathRewriter;

import org.junit.Before;
import org.junit.Test;

import file.FileInfo;

public class PathRewriterTest {
	Path TEMP_FOLDER_PATH, SOURCE1_PATH, SOURCE2_PATH, ARCHIVE_PATH, REWRITTEN1_PATH, REWRITTEN2_PATH;
	PathRewriter reWriter;
	
	@Before
	public void setUp() {
		TEMP_FOLDER_PATH = Paths.get("C:\\temp\\");
		SOURCE1_PATH = Paths.get("C:\\temp\\foobar.txt");
		SOURCE2_PATH = Paths.get("C:\\temp\\foo\\bar\\foobar.txt");
		ARCHIVE_PATH = Paths.get("D:\\zip\\other\\testArchive.7z");
		REWRITTEN1_PATH = Paths.get("D:\\zip\\other\\testArchive.7z\\foobar.txt");
		REWRITTEN2_PATH = Paths.get("D:\\zip\\other\\testArchive.7z\\foo\\bar\\foobar.txt");
		
		reWriter = new PathRewriter(TEMP_FOLDER_PATH);
	}
	
	@SuppressWarnings("deprecation")
	@Test
	public void testReWritePathRoot() {
		assertThat(reWriter.reWritePath(SOURCE1_PATH, ARCHIVE_PATH), is(REWRITTEN1_PATH));
	}
	
	@SuppressWarnings("deprecation")
	@Test
	public void testReWritePathSubDirectory() {
		assertThat(reWriter.reWritePath(SOURCE2_PATH, ARCHIVE_PATH), is(REWRITTEN2_PATH));
	}
	
	@Test
	public void testReWritePathRootArchiveFile() {
		ArchiveFile archive = createArchiveFile(SOURCE1_PATH);
		reWriter.reWritePath(archive);
		
		assertThat(archive.getFilePath(), is(REWRITTEN1_PATH));
	}
	
	@Test
	public void testReWritePathSubDirectoryArchiveFile() {
		ArchiveFile archive = createArchiveFile(SOURCE2_PATH);
		reWriter.reWritePath(archive);
		
		assertThat(archive.getFilePath(), is(REWRITTEN2_PATH));
	}

	private ArchiveFile createArchiveFile(Path sourcePath) {
		FileInfo info = new FileInfo(sourcePath);
		ArchiveFile archive = new ArchiveFile(info, ARCHIVE_PATH);
		return archive;
	}
}
