/**
 * Settings
 */	
@lombok.Builder
@lombok.Value
@lombok.experimental.Accessors(fluent = true)
public class Settings {
		
		//Simulation settings
		boolean reloadDatabase;
		/** TODO: please comment what this means */
		boolean createCurrentElectricityEA;
		boolean runHeadlessAtStartup;
		Boolean showKPISummary;
		ArrayList<String> subscopesToSimulate;
		OL_RadioButtonSetup resultsUIRadioButtonSetup;
}