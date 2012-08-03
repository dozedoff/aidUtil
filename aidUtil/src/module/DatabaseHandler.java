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
package module;

import io.AidDAO;
import io.AidTables;
import io.ConnectionPool;
import file.FileInfo;

public class DatabaseHandler {
	private final AidDAO sql;
	private final static String ARCHIVE_LOCATION_TAG = "ARCHIVE";
	
	public DatabaseHandler(ConnectionPool connPool) {
		this.sql = new AidDAO(connPool);
	}

	public void addIndex(FileInfo fileInfo){
		String fileHash = fileInfo.getHash();
		
		if(! sql.isHashed(fileHash)){
			sql.addIndex(fileInfo, ARCHIVE_LOCATION_TAG);
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
	
	private void moveAndInsertIndex(FileInfo info){
		sql.moveIndexToDuplicate(info.getHash());
		sql.addIndex(info, ARCHIVE_LOCATION_TAG);
	}
	
	public void addDnw(FileInfo fileInfo){
		sql.update(fileInfo.getHash(), AidTables.Dnw);
	}
}
