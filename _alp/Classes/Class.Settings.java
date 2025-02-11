/**
 * Settings
 */	
@lombok.Builder
@lombok.Value
@lombok.experimental.Accessors(fluent = true)
public class Settings {
		
		//Simulation settings
		boolean reloadDatabase; // Reloads the excels into the project database
		boolean createCurrentElectricityEA; // Create current Electric assets if a real electricity profile or total is present. 
											//--> Put on 'false' to prevent electric assets from being made on top of real 
											//electricity total or profile, to prevent wrong energy totals.
		boolean runHeadlessAtStartup;// Runs year simulation on starting of the model
		Boolean showKPISummary;		// Setting used to active the KPI summary of the resultsUI. If on true, it will be shown after a year simulation
		ArrayList<String> subscopesToSimulate;	// Selected subscopes to simulate (used for larger models that have memory/speed problems).
		OL_RadioButtonSetup resultsUIRadioButtonSetup; // Selected (radiobuttons -> graphs) setup for the resultsUI. 
}