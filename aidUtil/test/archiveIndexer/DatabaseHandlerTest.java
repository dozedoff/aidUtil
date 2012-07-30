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

import module.DatabaseHandler;

import org.dbunit.Assertion;
import org.dbunit.DBTestCase;
import org.dbunit.DatabaseTestCase;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.CompositeDataSet;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.IRowValueProvider;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.RowFilterTable;
import org.dbunit.dataset.filter.IRowFilter;
import org.dbunit.util.fileloader.FlatXmlDataFileLoader;
import org.junit.Before;
import org.junit.Test;

import config.DefaultMySQLconnection;

public class DatabaseHandlerTest extends DatabaseTestCase{
	final String DataBaseHandler_PATH = "/dbData/DatabaseHandlerTestData.xml";
	final String dnwAddData_PATH = "dbData/DnwAddTestData.xml";
	final String indexAddArchiveData_PATH = "dbData/IndexAddTestData.xml";
	final String indexAddArchiveExistingFileData_PATH = "dbData/IndexAddArchiveExistingFileTestData.xml";
	final String indexAddArchiveExistingArchiveData_PATH = "dbData/IndexAddArchiveExistingArchiveTestData.xml";
	final String INDEX_TABLE = "index", DNW_TABLE = "dnw";
			
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
		
		IDataSet expectedDataset = getCompositeFileDataset(DataBaseHandler_PATH, dnwAddData_PATH);
		IDataSet actualDataset = getDatabaseDataSet();
		
		Assertion.assertEquals(expectedDataset, actualDataset);
	}
	
	@Test
	public void testAddIndexNewArchiveEntry() throws Exception {
		addIndex("2");
		
		IDataSet expectedDataset = getCompositeFileDataset(DataBaseHandler_PATH, indexAddArchiveData_PATH);
		IDataSet actualDataset = getDatabaseDataSet();
		
		Assertion.assertEquals(expectedDataset, actualDataset);
	}
	
	@Test
	public void testAddIndexExistingFileNewArchiveEntry() throws Exception {
		addIndex("4");
		
		IDataSet expectedDataSet = getFileDataSet(indexAddArchiveExistingFileData_PATH);
		IDataSet actualDataSet = getDatabaseDataSet();
		
		Assertion.assertEquals(expectedDataSet.getTable("index"), actualDataSet.getTable("index"));
		Assertion.assertEquals(expectedDataSet.getTable("dnw"), actualDataSet.getTable("dnw"));
	}
	
	@Test
	public void testAddIndexExistingArchiveNewArchiveEntry() throws Exception {
		addIndex("4");
		
		IDataSet expectedDataSet = getCompositeFileDataset(DataBaseHandler_PATH, indexAddArchiveExistingArchiveData_PATH);
		IDataSet actualDataSet = getDatabaseDataSet();
		
		Assertion.assertEquals(expectedDataSet.getTable("index"), actualDataSet.getTable("index"));
		Assertion.assertEquals(expectedDataSet.getTable("dnw"), actualDataSet.getTable("dnw"));
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

		return new DatabaseConnection(jdbcConnection);
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
	
	private IDataSet getDatabaseDataSet() throws SQLException, Exception{
		return getConnection().createDataSet();
	}
	
	private ITable getCompositeFileTable(String tableName, String... fileName) throws DataSetException{
		return getCompositeFileDataset(fileName).getTable(tableName);
	}
	
	private IDataSet getCompositeFileDataset(String... fileName) throws DataSetException{
		IDataSet compositeDataSet = null;
		
		for(String file : fileName){
			if(compositeDataSet == null){
				compositeDataSet = getFileDataSet(file);
				continue;
			}
			
			IDataSet dataSet = getFileDataSet(file);
			compositeDataSet = new CompositeDataSet(compositeDataSet, dataSet);
		}
		return compositeDataSet;
	}
}
