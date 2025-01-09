/**
 * Settings
 */	
@lombok.Builder
@lombok.Value
@lombok.experimental.Accessors(fluent = true)
public class Settings {
		
		//Simulation settings
		boolean reloadDatabase;
		boolean createCurrentElectricityEA;
		boolean runHeadlessAtStartup;
		Boolean showKPISummary;
		ArrayList<String> subscopesToSimulate;
		OL_RadioButtonSetup resultsUIRadioButtonSetup;
}