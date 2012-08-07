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
import static org.junit.matchers.JUnitMatchers.hasItem;
import io.BoneConnectionPool;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;

import module.duplicateViewer.DatabaseHandler;
import module.duplicateViewer.Entry;

import org.dbunit.Assertion;
import org.dbunit.DatabaseTestCase;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.CompositeTable;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.ext.mysql.MySqlDataTypeFactory;
import org.dbunit.util.fileloader.FlatXmlDataFileLoader;
import org.junit.Before;
import org.junit.Test;

import config.DefaultMySQLconnection;

public class DatabaseHandlerTest extends DatabaseTestCase {
	final String DataBaseHandler_PATH = "/dbData/DatabaseHandlerTestData.xml";
	final String DuplicateDelete_PATH = "/dbData/DuplicateDeleteTestData.xml";
	final int NUM_OF_DUPLICATES = 4;
	final String INDEX_TABLE = "fileindex", DNW_TABLE = "dnw", DUPLICATE_TABLE = "fileduplicate";
	
	DatabaseHandler dbHandler;
	BoneConnectionPool bcp;
	Entry expectedDuplicate, expectedOriginal;
	
	@Before
	@Override
	public void setUp() throws Exception {
		super.setUp();
		bcp = new BoneConnectionPool(new DefaultMySQLconnection("127.0.0.1", 3306, "test", "test", "test"), 10);
		bcp.startPool();
		HashMap<String, Path> tagMap = generateTagList("DL_CLIENT", "LOCATION_A");
		dbHandler = new DatabaseHandler(bcp, tagMap);
		
		expectedDuplicate = new Entry("40", Paths.get("\\mutated\\custard\\is\\dangerous\\", "squirrel.jpg"));
		expectedOriginal = new Entry("40", Paths.get("\\mutated\\custard\\is\\dangerous\\", "foo.png"));
	}
	
	private HashMap<String, Path> generateTagList(String... tags){
		HashMap<String, Path> tagMap = new HashMap<>();
		char driveLetter = 'C';
		
		for(String tag : tags){
			tagMap.put(tag, Paths.get(driveLetter + ":\\"));
			driveLetter++;
		}
		
		return tagMap;
	}
	
	@Test
	public void testDeleteFromDuplicates() throws Exception {
		Entry toDelete = new Entry("40", null);
		
		dbHandler.deleteFromDuplicates(toDelete);
		
		Assertion.assertEquals(getCompositeFileTable(INDEX_TABLE, DuplicateDelete_PATH), getDatabaseTable(INDEX_TABLE));
		Assertion.assertEquals(getCompositeFileTable(DUPLICATE_TABLE, DuplicateDelete_PATH), getDatabaseTable(DUPLICATE_TABLE));
	}

	@Test
	public void testLoadDuplicates() {
		LinkedList<Entry> duplicates = dbHandler.getDuplicates();
		
		assertThat(duplicates, hasItem(expectedDuplicate));
		assertThat(duplicates, hasItem(expectedOriginal));
		assertThat(duplicates.size(), is(2));
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
	
	private IDataSet getFileDataSet(String fileName) {
		IDataSet expectedDataSet = new FlatXmlDataFileLoader().load(fileName);
		return expectedDataSet;
	}
	
	private ITable getDatabaseTable(String tableName) throws SQLException, Exception{
		IDataSet databaseDataSet = getConnection().createDataSet();
		return databaseDataSet.getTable(tableName);
	}
	
	private ITable getCompositeFileTable(String tableName, String... fileName) throws MalformedURLException, DataSetException{
		ITable compositeTable = null;
		
		for(String file : fileName){
			if(compositeTable == null){
				compositeTable = getFileTable(tableName, file);
				continue;
			}
			
			ITable table = getFileTable(tableName, file);
			compositeTable = new CompositeTable(compositeTable, table);
		}
		return compositeTable;
	}
	
	private ITable getFileTable(String tableName, String fileName) throws MalformedURLException, DataSetException{
		IDataSet expectedDataSet = getFileDataSet(fileName);
		return expectedDataSet.getTable(tableName);
	}
}
