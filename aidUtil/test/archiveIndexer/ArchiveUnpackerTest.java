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

import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.matchers.JUnitMatchers.hasItems;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import module.ArchiveUnpacker;

import org.junit.Before;
import org.junit.Test;

public class ArchiveUnpackerTest {
	Path tempFolder;
	final String[] expectedFilenames = {"foo.txt", "bar.jpg", "foobar.doc"};
	
	@Before
	public void setUp() throws Exception {
		tempFolder = Files.createTempDirectory("ArchiveUnpackerTest");
	}

	@Test
	public void testUnpackArchive() throws URISyntaxException, IOException {
		Path testArchive = Paths.get(ArchiveUnpackerTest.class.getResource("test.7z").toURI());

		assertTrue(Files.exists(testArchive));
		ArchiveUnpacker.unpack(testArchive, tempFolder);
		
		String[] unpackedFiles = tempFolder.toFile().list();
		
		assertThat(Arrays.asList(unpackedFiles), hasItems(expectedFilenames));
	}
}
