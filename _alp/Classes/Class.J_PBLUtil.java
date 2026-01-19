/**
 * J_PBLUtil
 */	
public class J_PBLUtil {
	
	//Convert input int or string into OL_PBL_DwellingType option
	public static OL_PBL_DwellingType getPBLDwellingTypeOption(int value) {
		return getPBLDwellingTypeOption(String.valueOf(value));
	}
	public static OL_PBL_DwellingType getPBLDwellingTypeOption(String value) {
		return OL_PBL_DwellingType.valueOf(J_PBLUtil.getOLTypeString(value));
	}
	
	//Convert input buildYear into ConstructionPeriod option (spaceHeating)
	public static int getConstructionPeriodOption_spaceHeatingAndResidents(int buildYear) {
		if (buildYear <= 1929){ return 0;}
		if (buildYear <= 1945){ return 1;}
		if (buildYear <= 1964){ return 2;}
		if (buildYear <= 1974){ return 3;}
		if (buildYear <= 1991){ return 4;}
		if (buildYear <= 1995){ return 5;}
		if (buildYear <= 1999){ return 6;}
		if (buildYear <= 2005){ return 7;}
		if (buildYear <= 2010){ return 8;}
		if (buildYear <= 2014){ return 9;}
		return 10; //Anything above 2014 = 10
	}
	//Convert input buildYear into ConstructionPeriod option (dhw and cooking)
	public static int getConstructionPeriodOption_DHWAndCooking(int buildYear) {
		if (buildYear <= 1930){ return 0;}
		if (buildYear <= 1959){ return 1;}
		if (buildYear <= 1980){ return 2;}
		if (buildYear <= 1995){ return 3;}
		return 4; //Anything above 1995 = 4
	}
	
	//Convert input int or string into OL_PBL_OwnershipType option
	public static OL_PBL_OwnershipType getPBLOwnershipTypeOption(int value) {
		return getPBLOwnershipTypeOption(String.valueOf(value));
	}
	public static OL_PBL_OwnershipType getPBLOwnershipTypeOption(String value) {
		return OL_PBL_OwnershipType.valueOf(J_PBLUtil.getOLTypeString(value));
	}
	
	//Convert input string of energylabel into OL_GridConnectionEnergyLabel option	
	public static OL_GridConnectionEnergyLabel getEnergyLabelOption(String energyLabel) {
		if(energyLabel == null || energyLabel.equals("x") || energyLabel.equals("")) {
			return OL_GridConnectionEnergyLabel.UNKNOWN;
		}
		else if(energyLabel.startsWith("A+")) { //For now all A+, A++, etc. are converted into A
			return OL_GridConnectionEnergyLabel.A;
		}
		else {
			return OL_GridConnectionEnergyLabel.valueOf(energyLabel);
		}
	}
	
	//Convert input string of insulationlabel into OL_GridConnectionInsulationLabel option	
	public static OL_GridConnectionInsulationLabel getInsulationLabelOption(String insulationLabel) {
		return convertEnergyToInsulationLabel(getEnergyLabelOption(insulationLabel));
	}
	
	//Get the most suited regression population
	public static int getPBLRegressionPopulation(OL_GridConnectionInsulationLabel insulationLabel, OL_PBL_DwellingType dwellingType) {
		if (insulationLabel == null || insulationLabel == OL_GridConnectionInsulationLabel.NONE || insulationLabel == OL_GridConnectionInsulationLabel.UNKNOWN){ 
		    return 3;
		} 
		else if(dwellingType == OL_PBL_DwellingType.TYPE_1){//If detached house
			return 2;
		}
		else {
			return 1;
		}
	}
	
	//Get the TNO surface code based on floor surface
	public static int getTNOFloorSurfaceCode(double floorSurfaceArea_m2) {
		if (floorSurfaceArea_m2 < 75){ return 1;}
		if (floorSurfaceArea_m2 < 100){ return 2;}
		if (floorSurfaceArea_m2 < 125){ return 3;}
		if (floorSurfaceArea_m2 < 150){ return 4;}
		return 5; // floorSurfaceArea_m2 >= 150 -> 5
	}
	
	private static String getOLTypeString(String number) {
		return "TYPE_" + number;
	}
	
	public static OL_GridConnectionEnergyLabel convertInsulationToEnergyLabel(OL_GridConnectionInsulationLabel insulationLabel) {
		return OL_GridConnectionEnergyLabel.valueOf(insulationLabel.toString());
	}
	public static OL_GridConnectionInsulationLabel convertEnergyToInsulationLabel(OL_GridConnectionEnergyLabel energyLabel) {
		return OL_GridConnectionInsulationLabel.valueOf(energyLabel.toString());
	}
}