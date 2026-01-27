import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;


/**
 * Settings
 */	

@Data
@Builder(toBuilder = true)
@Accessors(fluent = true)
public class Settings {
		
	//Simulation settings (Time)
	@Builder.Default
	double timeStep_h = 0.25; //Length of the simulation timestep in Hr	
	@Builder.Default
	int summerWeekNumber = 18; //Week number of the year where data will be stored for to display a 'default' summerweek
	@Builder.Default
	int winterWeekNumber = 49; //Week number of the year where data will be stored for to display a 'default' winterweek
	Double simDuration_h; // If filled in it will be set as the (rapid) sim duration instead of the default setting (8760 - p_timeStep_h) IF experiment end time is not set!.	
	
	
	//Simulation settings (Startup)
	boolean reloadDatabase; // Reloads the excels into the project database
	boolean createCurrentElectricityEA; // Create current Electric assets if a real electricity profile or total is present. 
										//--> Put on 'false' to prevent electric assets from being made on top of real 
										//electricity total or profile, to prevent wrong energy totals.
	boolean runHeadlessAtStartup;// Runs year simulation on starting of the model
	
	
	//Simulation settings (UI)
	Boolean showKPISummary;		// Setting used to active the KPI summary of the resultsUI. If on true, it will be shown after a year simulation
	ArrayList<String> subscopesToSimulate;	// Selected subscopes to simulate (used for larger models that have memory/speed problems).
	OL_RadioButtonSetup resultsUIRadioButtonSetup; // Selected (radiobuttons -> graphs) setup for the resultsUI. 
	boolean isPublicModel; // Boolean used to control if the model should be ran in a public version mode:
							// Connection owners where dataSharingAgreed = false, cant be clicked. Just like low level gridnodes.
	List<OL_MapOverlayTypes> activeMapOverlayTypes; //If defined it will override the default map overlay setup as defined in the generic interface/loader.
													//Add all map overlays to this list that you want to be usable in the simulation
}