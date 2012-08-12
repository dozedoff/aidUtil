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

import static org.junit.Assert.assertFalse;
import io.AidDAO;
import io.BoneConnectionPool;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class MySQLTransaction {
	BoneConnectionPool pool;
	
	@Before
	public void setUp() throws Exception {
		connect();
	}
	
	private void connect() throws Exception {
			Class.forName("com.mysql.jdbc.Driver");
			pool = new BoneConnectionPool("jdbc:mysql://localhost/test","test", "test",3);
			pool.startPool();
	}
	
	@After
	public void tearDown() {
		pool.stopPool();
	}
	
	@Test
	public void testTransaction() throws Exception {
		Connection cn = pool.getConnection();
		
		try{
			cn.setAutoCommit(false);
			Statement stmt = cn.createStatement();
			
			stmt.executeUpdate("INSERT INTO `fileindex` (id, dir, filename, size, location) VALUES (333,1,1,1,1)");
		}catch(SQLException se){
		}finally{
			cn.rollback();
			cn.commit();
			cn.setAutoCommit(true);
			cn.close();
		}
		
		AidDAO sql = new AidDAO(pool);
		
		assertFalse(sql.isHashed("333"));
		
	}
}
