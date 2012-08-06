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

import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.DriverManager;

import org.dbunit.DatabaseTestCase;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.ext.mysql.MySqlDataTypeFactory;
import org.dbunit.util.fileloader.FlatXmlDataFileLoader;
import org.junit.Test;

public class DatabaseHandlerTest extends DatabaseTestCase {
	final String DataBaseHandler_PATH = "/dbData/DatabaseHandlerTestData.xml";
	
	@Test
	public void testDeleteFromIndex() {
		fail("Not yet implemented");
	}

	@Test
	public void testDeleteFromDuplicates() {
		fail("Not yet implemented");
	}

	// Connection related methods
	
	@Override
	protected IDatabaseConnection getConnection() throws Exception {
		Class.forName("com.mysql.jdbc.Driver"); 
		
		Connection jdbcConnection = DriverManager.getConnection( "jdbc:mysql://localhost/test","test", "test"); 
		DatabaseConnection dbConn = new DatabaseConnection(jdbcConnection);
		
		dbConn.getConfig().setProperty("http://www.dbunit.org/properties/datatypeFactory", new MySqlDataTypeFactory());
		
		return dbConn;
	}

	@Override
	protected IDataSet getDataSet() throws Exception {
		IDataSet dataSet = new FlatXmlDataFileLoader().load(DataBaseHandler_PATH);

		return dataSet;
	}

}
