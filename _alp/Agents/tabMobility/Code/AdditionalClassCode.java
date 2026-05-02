// Page navigation
public ShapeGroup getGroupPageIndicator() {
	return this.gr_pageIndicator;
}

public List<OL_UITabPages> getLoadedPages() {
	return this.c_loadedPages;
}

public int getCurrentPageIndex() {
	return this.v_currentPageIndex;
}

// Slider groups
public ShapeGroup getGroupMobilitySliders_Households() {
	return this.gr_mobilitySliders_households;
}

public ShapeGroup getGroupMobilitySliders_Companies() {
	return this.gr_mobilitySliders_companies;
}

//Household sliders
public ShapeSlider getSliderHouseholdPrivateEVs_pct(){
	return this.sl_householdPrivateEVs_pct;
}

public ShapeSlider getSliderHouseholdEVsThatSupportV2G_pct(){
	return this.sl_householdEVsThatSupportV2G_pct;
}

public ShapeSlider getSliderHouseholdPublicChargers_pct(){
	return this.sl_householdPublicChargers_pct;
}

public ShapeSlider getSliderHouseholdChargersThatSupportV1G_pct(){
	return this.sl_householdChargersThatSupportV1G_pct;
}

public ShapeSlider getSliderHouseholdChargersThatSupportV2G_pct(){
	return this.sl_householdChargersThatSupportV2G_pct;
}

// Company sliders
public ShapeSlider getSliderCompaniesMobilityDemandReduction_pct () {
	return this.sl_companiesMobilityDemandReduction_pct;
}

public ShapeSlider getSliderCompaniesElectricTrucks_pct() {
	return this.sl_companiesElectricTrucks_pct;
}

public ShapeSlider getSliderCompaniesHydrogenTrucks_pct() {
	return this.sl_companiesHydrogenTrucks_pct;
}

public ShapeSlider getSliderCompaniesFossilFuelTrucks_pct() {
	return this.sl_companiesFossilFuelTrucks_pct;
}

public ShapeSlider getSliderCompaniesElectricVans_pct() {
	return this.sl_companiesElectricVans_pct;
}

public ShapeSlider getSliderCompaniesFossilFuelVans_pct() {
	return this.sl_companiesFossilFuelVans_pct;
}

public ShapeSlider getSliderCompaniesElectricCars_pct() {
	return this.sl_companiesElectricCars_pct;
}

public ShapeSlider getSliderCompaniesFossilFuelCars_pct() {
	return this.sl_companiesFossilFuelCars_pct;
}