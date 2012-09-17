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
package module.archiveIndexer;

import java.sql.SQLException;

import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;

import io.AidDAO;
import io.AidTables;
import io.ConnectionPool;
import io.dao.DuplicateDAO;
import io.dao.IndexDAO;
import io.tables.DuplicateRecord;
import io.tables.IndexRecord;
import file.FileInfo;

public class DatabaseHandler {
	private final static String ARCHIVE_LOCATION_TAG = "ARCHIVE";
	private ConnectionSource cSource;
	private IndexDAO indexDao;
	private DuplicateDAO duplicateDao;
	
	public DatabaseHandler(ConnectionPool connPool) throws SQLException {
		this.cSource = connPool.getConnectionSource();
		indexDao = DaoManager.createDao(cSource, IndexRecord.class);
		duplicateDao = DaoManager.createDao(cSource, DuplicateRecord.class); 
	}

	//FIXME: do not add identical hash & path file to duplicate
	public void addIndex(FileInfo fileInfo){
		String fileHash = fileInfo.getHash();
		
		if(! isIndexed(fileInfo)){
			indexDao.create(new IndexRecord(info, location));
			return;
		}
		
		if(isArchived(fileHash)){
			sql.addDuplicate(fileHash, fileInfo.getFilePath().toString(), fileInfo.getSize(), ARCHIVE_LOCATION_TAG);
		}else{
			moveAndInsertIndex(fileInfo);
		}
	}
	
	private boolean isArchived(String fileHash){
		String locDbTag = sql.getLocationById(fileHash);
		return locDbTag.equalsIgnoreCase(ARCHIVE_LOCATION_TAG);
	}
	
	private boolean isIndexed(FileInfo info) throws SQLException {
		IndexRecord index = indexDao.queryForId(info.getHash());

		if (index != null) {
			return true;
		} else {
			return false;
		}
	}
	
	private void moveAndInsertIndex(FileInfo info){
		sql.moveIndexToDuplicate(info.getHash());
		sql.addIndex(info, ARCHIVE_LOCATION_TAG);
	}
	
	public void addDnw(FileInfo fileInfo){
		sql.update(fileInfo.getHash(), AidTables.Dnw);
	}
}
