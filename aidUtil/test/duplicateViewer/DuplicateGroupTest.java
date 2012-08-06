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
import static org.junit.Assert.fail;
import static org.junit.matchers.JUnitMatchers.hasItem;
import static org.mockito.Mockito.*;
import module.duplicateViewer.DuplicateEntry;
import module.duplicateViewer.DuplicateGroup;

import org.junit.Before;
import org.junit.Test;

public class DuplicateGroupTest {
	DuplicateGroup dupeGroup;
	
	@Before
	public void setup() {
		dupeGroup = new DuplicateGroup();
	}

	@Test
	public void testGetSize() {
		for(int i=0; i < 3; i++){
			dupeGroup.addEntry(mock(DuplicateEntry.class));
		}
		
		assertThat(dupeGroup.getSize(), is(3));
	}

	@Test
	public void testGetEntries() {
		DuplicateEntry dupeMock = mock(DuplicateEntry.class);
		dupeGroup.addEntry(dupeMock);
		
		assertThat(dupeGroup.getEntries(), hasItem(dupeMock));
	}

	@Test
	public void testGetGroupId() {
		int currentId = dupeGroup.getGroupId();
		dupeGroup = new DuplicateGroup();
		
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
	
	private DuplicateEntry createMockEntryWithSetSelected(boolean setSelected) {
		DuplicateEntry mockEntry = mock(DuplicateEntry.class);
		when(mockEntry.isSelected()).thenReturn(setSelected);
		
		return mockEntry;
	}

	@Test
	public void testHasOnlyOneEntry() {
		dupeGroup.addEntry(mock(DuplicateEntry.class));
		
		assertThat(dupeGroup.hasOnlyOneEntry(), is(true));
	}
	
	@Test
	public void testHasMoreThanOneEntry() {
		dupeGroup.addEntry(mock(DuplicateEntry.class));
		dupeGroup.addEntry(mock(DuplicateEntry.class));
		
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
		dupeGroup.addEntry(mock(DuplicateEntry.class));
		
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
			DuplicateEntry mockEntry = mock(DuplicateEntry.class);
			when(mockEntry.isSelected()).thenReturn(true);
			dupeGroup.addEntry(mockEntry);
		}
	}

	private void addNotSelectedToGroup(int amount) {
		for (int i = 0; i < amount; i++) {
			DuplicateEntry mockEntry = mock(DuplicateEntry.class);
			when(mockEntry.isSelected()).thenReturn(false);
			dupeGroup.addEntry(mockEntry);
		}
	}
}
