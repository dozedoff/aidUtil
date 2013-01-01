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
package com.github.dozedoff.aidUtil.archiveIndexer;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.LinkedBlockingQueue;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.github.dozedoff.aidUtil.module.archiveIndexer.ArchiveFile;
import com.github.dozedoff.aidUtil.module.archiveIndexer.FileHasher;
import com.github.dozedoff.commonj.file.FileInfo;

public class FileHasherTest {
	LinkedBlockingQueue<ArchiveFile> input , output;
	FileHasher fileHasher;
	static Path testFile;
	static ArchiveFile archiveFile;
	
	
	final String expectedTestHash = "4120B987CF940DAC04632C27FDC072479FD519016D1085DE1CC2A4980D3041BF";
	 
	
	@BeforeClass
	public static void before() throws Exception{
		testFile = Paths.get(ArchiveUnpackerTest.class.getResource("test.7z").toURI());
		archiveFile = new ArchiveFile(new FileInfo(testFile), null);
	}
	
	@Before
	public void setUp() throws Exception {
		input = new LinkedBlockingQueue<>();
		output = new LinkedBlockingQueue<>();
		fileHasher = new FileHasher(input,output);
	}
	
	@Test
	public void testHashFiles() throws Exception{
		input.add(archiveFile);
		
		fileHasher.hashFiles();
		
		assertThat(output.size(), is(1));
		assertThat(output.poll().getHash(), is(expectedTestHash));
	}
	
	@Test
	public void testFileSize() throws Exception {
		input.add(archiveFile);
		
		fileHasher.hashFiles();
		
		assertThat(output.poll().getSize(), is(150L));
	}
}
