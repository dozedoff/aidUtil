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

import io.ConnectionPool;

import java.awt.Container;

import javax.swing.JTextArea;

public interface MaintenanceModule {
	/**
	 * Specify a container where modules can add additional components
	 * to allow more settings.
	 * @param container container where components should be added
	 */
	public void optionPanel(Container container);
	
	/**
	 * Start the module. All parameters must be validated.
	 */
	public void start();
	
	/**
	 * Abort the modules operation.
	 */
	public void Cancel();
	
	/**
	 * Specify a textarea where modules can output information for the user.
	 * @param logArea for displaying information to the user
	 */
	public void setLog(JTextArea logArea);
	
	/**
	 * Specify a connection pool for database operations.
	 * @param pool to be used for database communication
	 */
	public void setConnectionPool(ConnectionPool pool);
}
