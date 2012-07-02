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
import javax.swing.JTextField;

public abstract class MaintenanceModule {
	private JTextArea logArea;
	private ConnectionPool pool;
	private JTextField path, status;
	
	/**
	 * Specify a container where modules can add additional components
	 * to allow more settings.
	 * @param container container where components should be added
	 */
	public abstract void optionPanel(Container container);
	
	/**
	 * Start the module. All parameters must be validated.
	 */
	public abstract void start();
	
	/**
	 * Run module as a Thread
	 */
	public void startWorker(){
		new ModuleWorker(this).start();
	}
	
	/**
	 * Abort the modules operation.
	 */
	public abstract void Cancel();
	
	/**
	 * Specify a textarea where modules can output information for the user.
	 * @param logArea for displaying information to the user
	 */
	public final void setLog(JTextArea logArea){
		this.logArea = logArea;
	}
	
	/**
	 * Specify a connection pool for database operations.
	 * @param pool to be used for database communication
	 */
	public final void setConnectionPool(ConnectionPool pool){
		this.pool = pool;
	}
	
	public ConnectionPool getConnectionPool(){
		return this.pool;
	}
	
	/**
	 * Path of a file or directory on which the module will carry out
	 * it's operation.
	 * @param path the path the module should use
	 */
	public final void setPath(String path){
		this.path.setText(path);
	}
	
	public String getPath(){
		return this.path.getText();
	}
	
	public void setPathField(JTextField txt){
		this.path = txt;
	}
	
	public void setStatusField(JTextField status){
		this.status = status;
	}
	
	public void setStatus(String msg){
		status.setText(msg);
	}
	
	/**
	 * Add a message to the log window.
	 * @param msg message to add
	 */
	@Deprecated
	public final void log(String msg){
		if(logArea != null){
			logArea.append(msg+System.lineSeparator());
		}
	}
	
	public final void info(String msg){
		if(logArea != null){
			logArea.append("[INF] "+msg+System.lineSeparator());
		}
	}
	
	public final void warning(String msg){
		if(logArea != null){
			logArea.append("[WRN] "+msg+System.lineSeparator());
		}
	}
	
	public final void error(String msg){
		if(logArea != null){
			logArea.append("[ERR] "+msg+System.lineSeparator());
		}
	}
	
	class ModuleWorker extends Thread {
		MaintenanceModule module;
		
		public ModuleWorker(MaintenanceModule module) {
			super("ModuleWorker");
			this.module = module;
		}
		
		@Override
		public void run() {
			module.start();
			super.run();
		}
	}
}