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
package com.github.dozedoff.aidUtil.duplicateViewer;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.LinkedList;

import org.junit.Before;
import org.junit.Test;

import com.github.dozedoff.aidUtil.module.duplicateViewer.DuplicateGroup;
import com.github.dozedoff.aidUtil.module.duplicateViewer.Entry;
import com.github.dozedoff.aidUtil.module.duplicateViewer.GroupListCreator;

public class GroupListCreatorTest {
	LinkedList<DuplicateGroup> groups;
	LinkedList<Entry> entries;
	
	@Before
	public void setUp() throws Exception {
		groups = new LinkedList<>();
		entries = new LinkedList<>();
	}

	@Test
	public void testCreateListDistinct() {
		entries.add(createMockEntryWithHash("1"));
		entries.add(createMockEntryWithHash("2"));
		entries.add(createMockEntryWithHash("3"));
		
		groups = GroupListCreator.createList(entries);
		
		assertThat(groups.size(), is(3));
	}
	
	@Test
	public void testCreateListIdentical() {
		entries.add(createMockEntryWithHash("1"));
		entries.add(createMockEntryWithHash("1"));
		entries.add(createMockEntryWithHash("1"));
		
		groups = GroupListCreator.createList(entries);
		
		assertThat(groups.size(), is(1));
	}
	
	@Test
	public void testCreateListMixed() {
		entries.add(createMockEntryWithHash("1"));
		entries.add(createMockEntryWithHash("1"));
		entries.add(createMockEntryWithHash("2"));
		entries.add(createMockEntryWithHash("2"));
		entries.add(createMockEntryWithHash("3"));
		entries.add(createMockEntryWithHash("4"));
		
		groups = GroupListCreator.createList(entries);
		
		assertThat(groups.size(), is(4));
	}
	
	private Entry createMockEntryWithHash(String hashValue) {
		Entry mockEntry = mock(Entry.class);
		when(mockEntry.getHash()).thenReturn(hashValue);
		return mockEntry;
	}
}
