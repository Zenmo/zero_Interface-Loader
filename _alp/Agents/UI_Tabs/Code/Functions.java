double f_showCorrectTab()
{/*ALCODESTART::1722245473562*/
pop_tabElectricity_presentation.setVisible(false);
pop_tabHeating_presentation.setVisible(false);
pop_tabMobility_presentation.setVisible(false);
pop_tabEHub_presentation.setVisible(false);

switch (v_selectedTabType) {
	case ELECTRICITY:
		pop_tabElectricity_presentation.setVisible(true);
		break;
	case HEAT:
		pop_tabHeating_presentation.setVisible(true);
		break;
	case MOBILITY:
		pop_tabMobility_presentation.setVisible(true);
		break;
	case HUB:
	case NFATO:
		pop_tabEHub_presentation.setVisible(true);
		break;
}

/*ALCODEEND*/}

double f_setTab(EnergyDemandTab tab)
{/*ALCODESTART::1722259092945*/
v_selectedTabType = tab;
f_showCorrectTab();

/*ALCODEEND*/}

double f_initializeSliderGridConnections(List<GridConnection> gridConnections)
{/*ALCODESTART::1753881126492*/
c_utilityGridConnections.clear();
c_houseGridConnections.clear();
c_solarFarmGridConnections.clear();
c_windFarmGridConnections.clear();
c_gridBatteryGridConnections.clear();
c_chargerGridConnections.clear();


for(GridConnection GC : gridConnections){
	if(GC instanceof GCUtility){
		c_utilityGridConnections.add((GCUtility)GC);
	}
	else if(GC instanceof GCHouse){
		c_houseGridConnections.add((GCHouse)GC);		
	}
	else if(GC instanceof GCEnergyProduction){
		if(GC.v_liveAssetsMetaData.activeAssetFlows.contains(OL_AssetFlowCategories.pvProductionElectric_kW)){
			c_solarFarmGridConnections.add((GCEnergyProduction)GC);		
		}
		if(GC.v_liveAssetsMetaData.activeAssetFlows.contains(OL_AssetFlowCategories.windProductionElectric_kW)){
			c_windFarmGridConnections.add((GCEnergyProduction)GC);
		}
	}
	else if(GC instanceof GCGridBattery){
		c_gridBatteryGridConnections.add((GCGridBattery)GC);		
	}
	else if(GC instanceof GCPublicCharger){
		c_chargerGridConnections.add((GCPublicCharger)GC);		
	}
}
/*ALCODEEND*/}

double f_initializeSliderGCPointers(List<GridConnection> activeGridConnections,List<GridConnection> pausedGridConnections)
{/*ALCODESTART::1754908006859*/
v_sliderGridConnections = activeGridConnections;

if(pausedGridConnections != null){
	v_pausedSliderGridConnections = pausedGridConnections;
}
/*ALCODEEND*/}

double f_initializeUI_Tabs(List<GridConnection> activeGridConnections,List<GridConnection> pausedGridConnections)
{/*ALCODESTART::1754908356478*/
//Initialize the GridConnections
f_initializeSliderGCPointers(activeGridConnections, pausedGridConnections);

//Initialize the tabs
f_initializeActiveTabs();

//Set sliders to engine state of the gridconnections
f_updateSliders();

//Show correct tab
f_showCorrectTab();
/*ALCODEEND*/}

List<GCHouse> f_getSliderGridConnections_houses()
{/*ALCODESTART::1754919017244*/
List<GCHouse> houseGridConnections = new ArrayList<>();

for(GridConnection GC : v_sliderGridConnections){
	if(GC instanceof GCHouse){
		houseGridConnections.add((GCHouse)GC);		
	}
}

return houseGridConnections;
/*ALCODEEND*/}

List<GCUtility> f_getSliderGridConnections_utilities()
{/*ALCODESTART::1754922545766*/
List<GCUtility> utilityGridConnections = new ArrayList<>();

for(GridConnection GC : v_sliderGridConnections){
	if(GC instanceof GCUtility){
		utilityGridConnections.add((GCUtility)GC);
	}
}

return utilityGridConnections;
/*ALCODEEND*/}

List<GCGridBattery> f_getSliderGridConnections_gridBatteries()
{/*ALCODESTART::1754922546986*/
List<GCGridBattery> gridBatteryGridConnections = new ArrayList<>();

for(GridConnection GC : v_sliderGridConnections){
	if(GC instanceof GCGridBattery){
		gridBatteryGridConnections.add((GCGridBattery)GC);		
	}
}
if(v_pausedSliderGridConnections != null){
	for(GridConnection GC : v_pausedSliderGridConnections){
		if(GC instanceof GCGridBattery){
			gridBatteryGridConnections.add((GCGridBattery)GC);
		}
	}
}
return gridBatteryGridConnections;
/*ALCODEEND*/}

List<GCPublicCharger> f_getSliderGridConnections_chargers()
{/*ALCODESTART::1754922547989*/
List<GCPublicCharger> chargerGridConnections = new ArrayList<>();

for(GridConnection GC : v_sliderGridConnections){
	if(GC instanceof GCPublicCharger){
		chargerGridConnections.add((GCPublicCharger)GC);		
	}
}

return chargerGridConnections;
/*ALCODEEND*/}

List<GCEnergyProduction> f_getSliderGridConnections_production()
{/*ALCODESTART::1754922591622*/
List<GCEnergyProduction> productionGridConnections = new ArrayList<>();

for(GridConnection GC : v_sliderGridConnections){
	if(GC instanceof GCEnergyProduction){
		productionGridConnections.add((GCEnergyProduction)GC);
	}
}
if(v_pausedSliderGridConnections != null){
	for(GridConnection GC : v_pausedSliderGridConnections){
		if(GC instanceof GCEnergyProduction){
			productionGridConnections.add((GCEnergyProduction)GC);
		}
	}
}
return productionGridConnections;
/*ALCODEEND*/}

double f_updateSliders()
{/*ALCODESTART::1754929564839*/
if(!pop_tabElectricity.isEmpty()){
	pop_tabElectricity.get(0).f_updateSliders_Electricity();
}
if(!pop_tabHeating.isEmpty()){
	pop_tabHeating.get(0).f_updateSliders_Heating();
}
if(!pop_tabMobility.isEmpty()){
	pop_tabMobility.get(0).f_updateSliders_Mobility();
}
if(!pop_tabEHub.isEmpty()){
	pop_tabEHub.get(0).f_updateSliders_EHub();
}
/*ALCODEEND*/}

List<GridConnection> f_getSliderGridConnections_consumption()
{/*ALCODESTART::1754986474226*/
List<GridConnection> consumptionGridConnections = new ArrayList<>();

for(GridConnection GC : v_sliderGridConnections){
	if(GC instanceof GCUtility){
		consumptionGridConnections.add(GC);
	}
	else if(GC instanceof GCHouse){
		consumptionGridConnections.add(GC);
	}
}

return consumptionGridConnections;
/*ALCODEEND*/}

List<GridConnection> f_getSliderGridConnections_all()
{/*ALCODESTART::1754991155553*/
List<GridConnection> gridConnections = new ArrayList<>(v_sliderGridConnections);

return gridConnections;
/*ALCODEEND*/}

List<GCPublicCharger> f_getPausedSliderGridConnections_chargers()
{/*ALCODESTART::1755013777878*/
List<GCPublicCharger> pausedChargerGridConnections = new ArrayList<>();

if(v_pausedSliderGridConnections != null){
	for(GridConnection GC : v_pausedSliderGridConnections){
		if(GC instanceof GCPublicCharger){
			pausedChargerGridConnections.add((GCPublicCharger)GC);		
		}
	}
}

return pausedChargerGridConnections;
/*ALCODEEND*/}

double f_initializeActiveTabs()
{/*ALCODESTART::1756302560139*/
if(!pop_tabElectricity.isEmpty()){
	pop_tabElectricity.get(0).f_initializeTab_Electricity();
}
if(!pop_tabHeating.isEmpty()){
	//pop_tabHeating.get(0).f_initializeTab_Heating();
}
if(!pop_tabMobility.isEmpty()){
	//pop_tabMobility.get(0).f_initializeTab_Mobility();
}
if(!pop_tabEHub.isEmpty()){
	//pop_tabEHub.get(0).f_initializeTab_EHub();
}
/*ALCODEEND*/}

List<GCPublicCharger> f_getAllSliderGridConnections_chargers()
{/*ALCODESTART::1758278620331*/
List<GCPublicCharger> chargerGridConnections = new ArrayList<>();

for(GridConnection GC : v_sliderGridConnections){
	if(GC instanceof GCPublicCharger){
		chargerGridConnections.add((GCPublicCharger)GC);		
	}
}
if(v_pausedSliderGridConnections != null){
	for(GridConnection GC : v_pausedSliderGridConnections){
		if(GC instanceof GCPublicCharger){
			chargerGridConnections.add((GCPublicCharger)GC);		
		}
	}
}

return chargerGridConnections;
/*ALCODEEND*/}

