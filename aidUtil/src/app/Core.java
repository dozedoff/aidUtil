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
package app;

import java.util.ArrayList;

import module.MaintenanceModule;
import module.ModuleDnwCleanUp;

import gui.AidUtil;

public class Core {
	AidUtil aidUtil;
	
	public static void main(String args[]){
		new Core().startCore();
	}
	
	public void startCore(){
		//TODO create and start connection pool
		
		ArrayList<MaintenanceModule> modules = new ArrayList<>(10); //DEBUG
		modules.add(new ModuleDnwCleanUp()); //DEBUG
		
		aidUtil = new AidUtil(this,modules);
		aidUtil.setVisible(true);
	}
}
