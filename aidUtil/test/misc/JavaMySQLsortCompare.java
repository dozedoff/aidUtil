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

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;

import io.AidDAO;
import io.BoneConnectionPool;
import io.ConnectionPool;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import sun.misc.JavaLangAccess;

import app.Core;

public class JavaMySQLsortCompare {
	static ConnectionPool connPool;
	ArrayList<String> dbData, javaSorted;
	
	@BeforeClass
	static public void before() throws Exception{
			// load database properties
			Properties dbProps = new Properties();
			dbProps.load(Core.class.getResourceAsStream("db.properties"));
			
			// create connection pool
			connPool = new BoneConnectionPool(dbProps,5);
			connPool.startPool();
	}
	
	@AfterClass
	static public void after(){
		if(connPool != null){
			connPool.stopPool();
		}
	}
	
	@Test
	public void test() {
		dbData = new AidDAO(connPool).getLocationFilelist("EXT HD 02");
		javaSorted = new ArrayList<>(dbData.size());

		
		// clone
		for(int i = 0; i<dbData.size(); i++){
			javaSorted.add(i, new String(dbData.get(i))); 
		}
		
		// guard condition
		assertTrue(dbData.equals(javaSorted));
		
		// sort
		Collections.sort(javaSorted);
		
		assertTrue(dbData.equals(javaSorted));
	}

}
