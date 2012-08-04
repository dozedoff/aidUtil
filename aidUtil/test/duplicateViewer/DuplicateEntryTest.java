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
package duplicateViewer;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import module.duplicateViewer.DuplicateEntry;

import org.junit.Before;
import org.junit.Test;

public class DuplicateEntryTest {
	DuplicateEntry dupeEntry;

	@Before
	public void setUp() throws IOException {
		createEntryWithValidPath();
	}
	
	private void createEntryWithValidPath() throws IOException {
		Path vaildPath = createValidFile();
		createEntry(vaildPath);
	}

	private void createEntryWithInvalidPath() {
		Path invalidPath = Paths.get("not-valid");
		createEntry(invalidPath);
	}

	private void createEntry(Path filepath) {
		dupeEntry = new DuplicateEntry("12345", filepath, 0L);
		dupeEntry.setSelected(false);
	}
	
	private Path createValidFile() throws IOException {
		Path validFilePath = Files.createTempFile("DuplicateEntryTestFile", "txt");
		return validFilePath;
	}

	@Test
	public void testValidFileNotSelected() {
		assertThat(dupeEntry.isSelected(), is(false));
	}
	
	@Test
	public void testValidFileSelected() {
		dupeEntry.setSelected(true);
		assertThat(dupeEntry.isSelected(), is(true));
	}

	@Test
	public void testInvalidFileNotSelected() {
		createEntryWithInvalidPath();
		dupeEntry.setSelected(false);
		assertThat(dupeEntry.isSelected(), is(false));
	}
	
	@Test
	public void testInvalidFileSelected() {
		createEntryWithInvalidPath();
		dupeEntry.setSelected(true);
		assertThat(dupeEntry.isSelected(), is(false));
	}
}
