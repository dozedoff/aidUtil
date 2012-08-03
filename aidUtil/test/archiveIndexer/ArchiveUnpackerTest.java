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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Properties;

import module.archiveIndexer.ArchiveUnpacker;
import module.archiveIndexer.UnpackException;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import app.Core;

public class ArchiveUnpackerTest {
	Path tempFolder;
	static Path testArchive, invalidArchive;
	final String[] expectedFilenames = {"foo.txt", "bar.jpg", "foobar.doc"};
	ArchiveUnpacker archiveUnpacker;
	static Properties settings = new Properties();
	
	@Rule
	public ExpectedException exception = ExpectedException.none();

	@BeforeClass
	public static void SetUpClass() throws Exception {
		testArchive = Paths.get(ArchiveUnpackerTest.class.getResource("test.7z").toURI());
		invalidArchive = Paths.get(ArchiveUnpackerTest.class.getResource("invalid.rar").toURI());
		settings.load(Core.class.getResourceAsStream("aidUtil.properties"));
	}
	
	@Before
	public void setUp() throws Exception {
		tempFolder = Files.createTempDirectory("ArchiveUnpackerTest");
		archiveUnpacker = new ArchiveUnpacker(Paths.get(settings.getProperty("7zipAppPath")));
	}

	@Test
	public void testUnpackArchive() throws IOException {
		assertTrue(Files.exists(testArchive));
		
		archiveUnpacker.unpack(testArchive, tempFolder);
		String[] unpackedFiles = tempFolder.toFile().list();
		
		assertThat(Arrays.asList(unpackedFiles), hasItems(expectedFilenames));
	}
	
	@Test
	public void testInvalidArchivePath() throws IOException {
		exception.expect(FileNotFoundException.class);
		
		archiveUnpacker.unpack(Paths.get("foobar"), tempFolder);
	}
	
	@Test
	public void testInvalidDestination() throws IOException {
		exception.expect(FileNotFoundException.class);
		
		archiveUnpacker.unpack(testArchive, Paths.get("foobar"));
	}
	
	@Test
	public void testInvalidArchive() throws UnpackException, IOException {
		exception.expect(UnpackException.class);
		exception.expectMessage("Fatal error");
		
		archiveUnpacker.unpack(invalidArchive, tempFolder);
	}
	
	@Test
	public void testInvalidAppPath() throws UnpackException, IOException{
		exception.expect(IOException.class);
		
		new ArchiveUnpacker(Paths.get("\\foobar\\")).unpack(testArchive, tempFolder);
	}
}
