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
import static org.junit.Assert.assertTrue;
import static org.junit.matchers.JUnitMatchers.hasItem;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import module.duplicateViewer.DuplicateGroup;
import module.duplicateViewer.Entry;

import org.junit.Before;
import org.junit.Test;

public class DuplicateGroupTest {
	DuplicateGroup dupeGroup;
	final String TEST_HASH = "12345";
	
	@Before
	public void setup() {
		dupeGroup = new DuplicateGroup(TEST_HASH);
	}

	@Test
	public void testGetSize() {
		for(int i=0; i < 3; i++){
			dupeGroup.addEntry(mock(Entry.class));
		}
		
		assertThat(dupeGroup.getSize(), is(3));
	}

	@SuppressWarnings("deprecation")
	@Test
	public void testGetEntries() {
		Entry dupeMock = mock(Entry.class);
		dupeGroup.addEntry(dupeMock);
		
		assertThat(dupeGroup.getEntries(), hasItem(dupeMock));
	}

	@Test
	public void testGetGroupId() {
		int currentId = dupeGroup.getGroupId();
		dupeGroup = new DuplicateGroup(TEST_HASH);
		
		assertThat(dupeGroup.getGroupId(), is(currentId+1));
	}

	@Test
	public void testAreAllSelected() {
		
		for(int i=0; i < 3; i++){
			dupeGroup.addEntry(createMockEntryWithSetSelected(true));
		}
		
		assertThat(dupeGroup.areAllSelected(), is(true));
	}
	
	@Test
	public void testAreNotAllSelected() {
		for(int i=0; i < 3; i++){
			dupeGroup.addEntry(createMockEntryWithSetSelected(true));
		}
		dupeGroup.addEntry(createMockEntryWithSetSelected(false));
		
		assertThat(dupeGroup.areAllSelected(), is(false));
	}
	
	@Test
	public void testAreAllSelectedWithNoneSelected() {
		assertThat(dupeGroup.areAllSelected(), is(false));
	}
	
	private Entry createMockEntryWithSetSelected(boolean setSelected) {
		Entry mockEntry = mock(Entry.class);
		when(mockEntry.isSelected()).thenReturn(setSelected);
		
		return mockEntry;
	}

	@Test
	public void testHasOnlyOneEntry() {
		dupeGroup.addEntry(mock(Entry.class));
		
		assertThat(dupeGroup.hasOnlyOneEntry(), is(true));
	}
	
	@Test
	public void testHasMoreThanOneEntry() {
		dupeGroup.addEntry(mock(Entry.class));
		dupeGroup.addEntry(mock(Entry.class));
		
		assertThat(dupeGroup.hasOnlyOneEntry(), is(false));
	}
	
	@Test
	public void testHasOnlyOneEntryWhenEmpty() {
		assertThat(dupeGroup.hasOnlyOneEntry(), is(false));
	}

	@Test
	public void testIsEmpty() {
		assertThat(dupeGroup.isEmpty(), is(true));
	}
	
	@Test
	public void testIsNotEmpty() {
		dupeGroup.addEntry(mock(Entry.class));
		
		assertThat(dupeGroup.isEmpty(), is(false));
	}
	
	@Test
	public void testGetSelected() {
		addSelectedToGroup(4);
		addNotSelectedToGroup(5);
		
		assertThat(dupeGroup.getSelected().size(), is(4));
	}
	
	@Test
	public void testGetNotSelected() {
		addSelectedToGroup(4);
		addNotSelectedToGroup(5);;
		
		assertThat(dupeGroup.getNotSelected().size(), is(5));
	}
	
	private void addSelectedToGroup(int amount) {
		for (int i = 0; i < amount; i++) {
			Entry mockEntry = mock(Entry.class);
			when(mockEntry.isSelected()).thenReturn(true);
			dupeGroup.addEntry(mockEntry);
		}
	}

	private void addNotSelectedToGroup(int amount) {
		for (int i = 0; i < amount; i++) {
			Entry mockEntry = mock(Entry.class);
			when(mockEntry.isSelected()).thenReturn(false);
			dupeGroup.addEntry(mockEntry);
		}
	}
	
	@Test
	public void testEqualNull() {
		assertThat(dupeGroup.equals(null), is(false));
	}
	
	@Test
	public void tesEqualtWrongClass() {
		assertThat(dupeGroup.equals(new String()), is(false));
	}
	
	@Test
	public void testEqualWrongHashValue() {
		DuplicateGroup nonEqualGroup = new DuplicateGroup("54321");
		assertThat(dupeGroup.equals(nonEqualGroup), is(false));
	}
	
	@Test
	public void testEqualSameHashValue() {
		DuplicateGroup nonEqualGroup = new DuplicateGroup(TEST_HASH);
		assertThat(dupeGroup.equals(nonEqualGroup), is(true));
	}
	
	@Test
	public void testNoValidImagePath() throws IOException {
		Path file1 = Paths.get("foobar/");
		Path file2 = Paths.get("foo/");;
		
		dupeGroup = new DuplicateGroup("1");
		dupeGroup.addEntry(new Entry("1", file1));
		dupeGroup.addEntry(new Entry("1", file2));
		
		assertThat(dupeGroup.getImagepath(), is(file1));
	}
	
	@Test
	public void testValidImagePath() throws IOException {
		Path file1 = Paths.get("foobar/");
		Path file2 = Files.createTempFile("DuplicatGroupTestFile2", null);
		
		dupeGroup = new DuplicateGroup("1");
		dupeGroup.addEntry(new Entry("1", file1));
		dupeGroup.addEntry(new Entry("1", file2));
		
		assertThat(dupeGroup.getImagepath(), is(file2));
	}
	
	@Test
	public void testInvalidatedImagePath() throws IOException {
		Path file1 = Files.createTempFile("DuplicatGroupTestFile1", null);
		Path file2 = Files.createTempFile("DuplicatGroupTestFile2", null);
		
		dupeGroup = new DuplicateGroup("1");
		dupeGroup.addEntry(new Entry("1", file1));
		dupeGroup.addEntry(new Entry("1", file2));
		
		assertThat(dupeGroup.getImagepath(), is(file1));
		
		assertTrue(Files.deleteIfExists(file1));
		
		assertThat(dupeGroup.getImagepath(), is(file2));
	}
	
	@Test
	public void testEmptyGroupImagePath() throws IOException {
		dupeGroup = new DuplicateGroup("1");
		
		assertThat(dupeGroup.getImagepath(), is(Paths.get("empty")));
	}
}
