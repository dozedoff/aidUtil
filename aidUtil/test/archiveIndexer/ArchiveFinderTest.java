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
import static org.junit.matchers.JUnitMatchers.hasItems;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;

import module.archiveIndexer.ArchiveFinder;

import org.junit.Before;
import org.junit.Test;

public class ArchiveFinderTest {
	Path tempDir;
	LinkedList<Path> archives = new LinkedList<>();
	LinkedList<Path> foundArchives;
	
	@Before
	public void setUp() throws Exception {
		createTestDirectory();
	}

	@Test
	public void testFindArchives() throws IOException {
		foundArchives = ArchiveFinder.find(tempDir);
		assertThat(foundArchives.size(), is(2));
		assertThat(foundArchives, hasItems(archives.toArray(new Path[0])));
	}
	
	@Test(expected=NoSuchFileException.class)
	public void testInvalidPath() throws IOException{
		ArchiveFinder.find(Paths.get("non-existant"));
	}
	
	private void createTestDirectory() throws IOException{
		tempDir = Files.createTempDirectory("ArchiveFinderTest");
		Path arch1 = tempDir.resolve("foo.rar");
		archives.add(arch1);
		Files.createFile(arch1);
		Files.createFile(tempDir.resolve("test.txt"));
		Path subdir1 = tempDir.resolve("sub1");
		Files.createDirectories(subdir1);
		Files.createFile(subdir1.resolve("cat.jpg"));
		Path arch2 = Files.createFile(subdir1.resolve("bar.7Z"));
		archives.add(arch2);
	}

}
