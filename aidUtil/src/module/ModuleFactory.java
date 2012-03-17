package module;

public class ModuleFactory {
	public static MaintenanceModule createModule(String moduleName) throws Exception{
		return (MaintenanceModule) Class.forName(moduleName).newInstance();
	}
}
