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
package misc;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;

public class BinarySearchTest {
	ArrayList<String> data = new ArrayList<>();
	String[] values = {"d:\\temp1\\a.txt", "d:\\temp2\\B.txt","d:\\temp2\\a.txt","d:\\temp2\\C.txt"};
	
	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void test() {
		for(String s : values){
			data.add(s);
		}
		
		System.out.println("Unsorted: ");
		for(String s : data){
			System.out.println(s);
		}
		
		Collections.sort(data);
		System.out.println("Sorted: ");
		for(String s : data){
			System.out.println(s);
		}
		
		int searchResult = Collections.binarySearch(data, values[0]);
		
		assertTrue("Result was " + searchResult,searchResult >= 0);
		
		searchResult = Collections.binarySearch(data, "derp");
		
		assertTrue("Result was " + searchResult,searchResult < 0);
	}

}
