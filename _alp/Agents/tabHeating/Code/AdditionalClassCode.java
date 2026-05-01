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
public ShapeGroup getGroupHeatDemandSliders_Households() {
	return this.gr_heatingSliders_households;
}

public ShapeGroup getGroupHeatDemandSliders_Companies() {
	return this.gr_heatingSliders_companies;
}

// Residential Tab Sliders
public ShapeSlider getSliderHouseholdGasBurner_pct() { 
	return this.sl_householdGasBurner_pct;
}

public ShapeSlider getSliderHouseholdHybridHeatpump_pct() { 
	return this.sl_householdHybridHeatpump_pct;
}

public ShapeSlider getSliderHouseholdElectricHeatPump_pct() { 
	return this.sl_householdElectricHeatPump_pct;
}

public ShapeSlider getSliderHouseholdAirco_pct() { 
	return this.sl_householdAirco_pct;
}

public ShapeSlider getSliderHouseholdHeatDemandReduction_pct() { 
	return this.sl_householdHeatDemandReduction_pct;
}

public ShapeSlider getSliderHouseholdRooftopPT_pct() { 
	return this.sl_householdRooftopPT_pct;
}

// Company Tab Sliders
public ShapeSlider getSliderCompaniesHeatDemandReduction_pct() { 
	return this.sl_companiesHeatDemandReduction_pct;
}

public ShapeSlider getSliderCompaniesGasBurner_pct() { 
	return this.sl_companiesGasBurner_pct;
}

public ShapeSlider getSliderCompaniesHybridHeatPump_pct() { 
	return this.sl_companiesHybridHeatPump_pct;
}

public ShapeSlider getSliderCompaniesElectricHeatPump_pct() { 
	return this.sl_companiesElectricHeatPump_pct;
}

public ShapeSlider getSliderCompaniesDistrictHeating_pct(){
	return this.sl_companiesDistrictHeating_pct;
}

public ShapeSlider getSliderCompaniesEBoiler_pct(){
	return this.sl_companiesEBoiler_pct;
}

public ShapeSlider getSliderCompaniesCustomHeating_pct(){
	return this.sl_companiesCustom_pct;
}

