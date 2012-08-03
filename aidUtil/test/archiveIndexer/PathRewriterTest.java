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

import java.nio.file.Paths;

import module.archiveIndexer.PathRewriter;

import org.junit.Before;
import org.junit.Test;

public class PathRewriterTest {
	final static String TEMP_FOLDER_PATH = "C:\\temp\\";
	final static String SOURCE1_PATH = "C:\\temp\\foobar.txt";
	final static String SOURCE2_PATH = "C:\\temp\\foo\\bar\\foobar.txt";
	final static String ARCHIVE_PATH = "D:\\zip\\other\\testArchive.7z";
	final static String REWRITTEN1_PATH = "D:\\zip\\other\\testArchive.7z\\foobar.txt";
	final static String REWRITTEN2_PATH = "D:\\zip\\other\\testArchive.7z\\foo\\bar\\foobar.txt";
	
	PathRewriter reWriter;
	
	@Before
	public void setUp() {
		reWriter = new PathRewriter(Paths.get(TEMP_FOLDER_PATH));
	}
	
	@Test
	public void testReWritePathRoot() {
		assertThat(reWriter.reWritePath(Paths.get(SOURCE1_PATH), Paths.get(ARCHIVE_PATH)), is(Paths.get(REWRITTEN1_PATH)));
	}
	
	@Test
	public void testReWritePathSubDirectory() {
		assertThat(reWriter.reWritePath(Paths.get(SOURCE2_PATH), Paths.get(ARCHIVE_PATH)), is(Paths.get(REWRITTEN2_PATH)));
	}

}
