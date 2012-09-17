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
package archiveIndexer;

import static org.junit.Assert.*;

import file.FileInfo;
import io.AidDAO;
import io.BoneConnectionPool;

import java.io.File;
import java.net.MalformedURLException;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import module.archiveIndexer.DatabaseHandler;

import org.dbunit.Assertion;
import org.dbunit.DBTestCase;
import org.dbunit.DatabaseTestCase;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.CompositeDataSet;
import org.dbunit.dataset.CompositeTable;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.IRowValueProvider;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.RowFilterTable;
import org.dbunit.dataset.filter.IRowFilter;
import org.dbunit.ext.mysql.MySqlDataTypeFactory;
import org.dbunit.util.fileloader.FlatXmlDataFileLoader;
import org.junit.Before;
import org.junit.Test;

import config.DefaultMySQLconnection;

public class DatabaseHandlerTest extends DatabaseTestCase{
	final String DataBaseHandler_PATH = "/dbData/DatabaseHandlerTestData.xml";
	final String dnwAddData_PATH = "/dbData/DnwAddTestData.xml";
	final String indexAddArchiveData_PATH = "/dbData/IndexAddArchiveTestData.xml";
	final String indexAddArchiveExistingFileData_PATH = "/dbData/IndexAddArchiveExistingFileTestData.xml";
	final String indexAddArchiveExistingArchiveData_PATH = "/dbData/IndexAddArchiveExistingArchiveTestData.xml";
	final String INDEX_TABLE = "fileindex", DNW_TABLE = "dnw", DUPLICATE_TABLE = "fileduplicate";
			
	DatabaseHandler dbh;
	BoneConnectionPool bcp;
	
	@Before
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		bcp = new BoneConnectionPool(new DefaultMySQLconnection("127.0.0.1", 3306, "test", "test", "test"), 10);
		bcp.startPool();
		dbh = new DatabaseHandler(bcp);
	}

	@Test
	public void testAddDnw() throws Exception {
		FileInfo fi = new FileInfo(new File(""), "1");
		dbh.addDnw(fi);
		
		Assertion.assertEquals(getCompositeFileTable(DNW_TABLE, DataBaseHandler_PATH, dnwAddData_PATH), getDatabaseTable(DNW_TABLE));
		Assertion.assertEquals(getCompositeFileTable(INDEX_TABLE, DataBaseHandler_PATH, dnwAddData_PATH), getDatabaseTable(INDEX_TABLE));
	}
	
	@Test
	public void testAddIndexNewArchiveEntry() throws Exception {
		addIndex("5");
		
		Assertion.assertEquals(getCompositeFileTable(INDEX_TABLE, DataBaseHandler_PATH, indexAddArchiveData_PATH), getDatabaseTable(INDEX_TABLE));
	}
	
	@Test
	public void testAddIndexExistingFileNewArchiveEntry() throws Exception {
		addIndex("4");
		
		Assertion.assertEquals(getCompositeFileTable(INDEX_TABLE, indexAddArchiveExistingFileData_PATH), getDatabaseTable(INDEX_TABLE));
		Assertion.assertEquals(getCompositeFileTable(DUPLICATE_TABLE, indexAddArchiveExistingFileData_PATH), getDatabaseTable(DUPLICATE_TABLE));
	}
	
	@Test
	public void testAddIndexExistingArchiveNewArchiveEntry() throws Exception {
		addIndex("3");
		
		Assertion.assertEquals(getCompositeFileTable(INDEX_TABLE, DataBaseHandler_PATH, indexAddArchiveExistingArchiveData_PATH), getDatabaseTable(INDEX_TABLE));
		Assertion.assertEquals(getCompositeFileTable(DUPLICATE_TABLE, indexAddArchiveExistingArchiveData_PATH), getDatabaseTable(DUPLICATE_TABLE));
	}
	
	@Test
	public void testAddIndexExistingArchiveIdentical() throws Exception {
		addIndex("5");
		addIndex("5");
		
		Assertion.assertEquals(getCompositeFileTable(INDEX_TABLE, DataBaseHandler_PATH, indexAddArchiveData_PATH), getDatabaseTable(INDEX_TABLE));	
		Assertion.assertEquals(getCompositeFileTable(DUPLICATE_TABLE, DataBaseHandler_PATH), getDatabaseTable(DUPLICATE_TABLE));
	}
	
	private void addIndex(String id){
		FileInfo fi = new FileInfo(Paths.get("X:\\foo\\bar\\foo.png"), id);
		fi.setSize(23452345);
		dbh.addIndex(fi);
	}

	// ---------- Database Setup related methods ---------- //

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
	
	private ITable getDatabaseTable(String tableName) throws SQLException, Exception{
		IDataSet databaseDataSet = getConnection().createDataSet();
		return databaseDataSet.getTable(tableName);
	}

	private ITable getFileTable(String tableName, String fileName) throws MalformedURLException, DataSetException{
		IDataSet expectedDataSet = getFileDataSet(fileName);
		return expectedDataSet.getTable(tableName);
	}

	private IDataSet getFileDataSet(String fileName) {
		IDataSet expectedDataSet = new FlatXmlDataFileLoader().load(fileName);
		return expectedDataSet;
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
}
