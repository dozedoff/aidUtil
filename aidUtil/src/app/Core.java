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
