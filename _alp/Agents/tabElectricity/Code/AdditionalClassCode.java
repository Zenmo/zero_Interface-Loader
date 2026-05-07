// Page navigation
public ShapeGroup getGroupPageIndicator() {
	return this.gr_pageIndicator;
}
public List<ShapeGroup> getLoadedPages() {
	return this.c_loadedPageGroups;
}
public int getCurrentPageIndex() {
	return this.v_currentPageIndex;
}

// Slider groups
public ShapeGroup getGroupElectricityDemandSliders_Households() {
	return this.gr_electricitySliders_households;
}

public ShapeGroup getGroupElectricityDemandSliders_Companies() {
	return this.gr_electricitySliders_companies;
}

public ShapeGroup getGroupElectricityDemandSliders_Collective() {
	return this.gr_electricitySliders_collective;
}

//Households
public ShapeSlider getSliderHouseholdRooftopPV_pct(){
	return this.sl_householdRooftopPV_pct;
}

public ShapeSlider getSliderHouseholdBatteries_pct(){
	return this.sl_householdBatteries_pct;
}

public ShapeSlider getSliderHouseholdElectricCooking_pct(){
	return this.sl_householdElectricCooking_pct;
}

public ShapeSlider getSliderHouseholdElectricityDemandIncrease_pct(){
	return this.sl_householdElectricityDemandIncrease_pct;
}

//Companies
public ShapeSlider getSliderCompaniesElectricityDemandReduction_pct() {
	return this.sl_companiesElectricityDemandReduction_pct;
}

public ShapeSlider getSliderCompaniesRooftopPV_pct(){
	return this.sl_companiesRooftopPV_pct;
}

//Collective
public ShapeSlider getSliderLargeScalePV_ha(){
	return this.sl_largeScalePV_ha;
}

public ShapeSlider getSliderLargeScaleWind(){
	return this.sl_largeScaleWind_MW;
}

public ShapeSlider getSliderGridBatteries(){
	return this.sl_gridBatteries_kWh;
}