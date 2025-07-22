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
		boolean isPublicModel; // Boolean used to control if the model should be ran in a public version mode:
								// Connection owners where dataSharingAgreed = false, cant be clicked. Just like low level gridnodes.
		List<OL_MapOverlayTypes> activeMapOverlayTypes; //If defined it will override the default map overlay setup as defined in the generic interface/loader.
														//Add all map overlays to this list that you want to be usable in the simulation

}