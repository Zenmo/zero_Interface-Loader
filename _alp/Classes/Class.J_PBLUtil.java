/**
 * J_PBLUtil
 */	
public class J_PBLUtil {
	
	//Convert input int or string into OL_PBL_BuildingType option
	public static OL_PBL_BuildingType getPBLBuildingTypeOption(int value) {
		return getPBLBuildingTypeOption(String.valueOf(value));
	}
	public static OL_PBL_BuildingType getPBLBuildingTypeOption(String value) {
		return OL_PBL_BuildingType.valueOf(J_PBLUtil.getOLTypeString(value));
	}
	
	//Convert input constructionPeriod int or string into OL_PBL_ConstructionPeriod option
	public static OL_PBL_ConstructionPeriod getPBLConstructionPeriodOption(int value) {
		return getPBLConstructionPeriodOption(String.valueOf(value));
	}
	public static OL_PBL_ConstructionPeriod getPBLConstructionPeriodOption(String value) {
		return OL_PBL_ConstructionPeriod.valueOf(J_PBLUtil.getOLTypeString(value));
	}
	
	//Convert input buildYear into OL_PBL_ConstructionPeriod option
	public static OL_PBL_ConstructionPeriod convertBuildYearIntoConstructionPeriodOption(int buildYear) {
		if (buildYear <= 1929){ return OL_PBL_ConstructionPeriod.TYPE_0;}
		if (buildYear <= 1945){ return OL_PBL_ConstructionPeriod.TYPE_1;}
		if (buildYear <= 1964){ return OL_PBL_ConstructionPeriod.TYPE_2;}
		if (buildYear <= 1974){ return OL_PBL_ConstructionPeriod.TYPE_3;}
		if (buildYear <= 1991){ return OL_PBL_ConstructionPeriod.TYPE_4;}
		if (buildYear <= 1995){ return OL_PBL_ConstructionPeriod.TYPE_5;}
		if (buildYear <= 1999){ return OL_PBL_ConstructionPeriod.TYPE_6;}
		if (buildYear <= 2005){ return OL_PBL_ConstructionPeriod.TYPE_7;}
		if (buildYear <= 2010){ return OL_PBL_ConstructionPeriod.TYPE_8;}
		if (buildYear <= 2014){ return OL_PBL_ConstructionPeriod.TYPE_9;}
		return OL_PBL_ConstructionPeriod.TYPE_10; // 2021 and later -- Anything abouve 2014 = 10
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
	
	
	public static int getTNOFloorSurfaceCode(double floorSurfaceArea_m2) {
		if(floorSurfaceArea_m2 < 75){
			return 1;
		}
		else if(75 <= floorSurfaceArea_m2 && floorSurfaceArea_m2 < 100){
			return 2;
		}
		else if(100 <= floorSurfaceArea_m2 && floorSurfaceArea_m2 < 125){
			return 3;
		}
		else if(125 <= floorSurfaceArea_m2 && floorSurfaceArea_m2 < 150){
			return 4;
		}
		else{// if(floorSurfaceArea_m2 > 150){
			return 5;
		}
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