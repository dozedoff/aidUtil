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
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;

import module.FileDeleter;

import org.junit.Before;
import org.junit.Test;

public class FileDeleterTest {
	Path testDirectory, baseDirectory;
	
	@Before
	public void setUp() throws Exception {
		baseDirectory = Files.createTempDirectory("FileDeleterTest");
		testDirectory = createTestDirectory(baseDirectory);
	}

	@Test
	public void testDeleteAll() throws IOException {
		// sanity checks
		assertTrue(Files.exists(testDirectory));
		assertTrue(Files.exists(testDirectory.resolve("dir1").resolve("bar.txt")));
		
		FileDeleter.deleteAll(testDirectory);
		
		assertTrue(Files.exists(testDirectory));
		assertThat(testDirectory.toFile().list().length, is(0));
	}
	
	@Test(expected=IOException.class)
	public void testDeleteInvalidPath() throws IOException {
		FileDeleter.deleteAll(Paths.get("\\foobar\\"));
	}
	
	private Path createTestDirectory(Path baseDirectory) throws IOException{
		LinkedList<Path> directories = new LinkedList<>();
		LinkedList<Path> files = new LinkedList<>();
		
		Path testDir = baseDirectory.resolve("foobar");
		
		Path file1 = testDir.resolve("dir1").resolve("bar.txt");
		Path file2 = file1.getParent().resolve("dirA").resolve("foo.txt");
		Path file3 = testDir.resolve("dir2").resolve("dirD").resolve("dirE").resolve("foobar.txt");
		
		files.add(file1);
		files.add(file2);
		files.add(file3);
		
		directories.add(file2.getParent());
		directories.add(file3.getParent());
		
		for(Path dirPath : directories){
			Files.createDirectories(dirPath);
		}
		
		for(Path filePath : files){
			Files.createFile(filePath);
		}
		
		return testDir;
	}
}
