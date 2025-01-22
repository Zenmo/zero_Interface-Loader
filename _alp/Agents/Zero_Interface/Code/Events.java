void e_calculateEnergyBalance()
{/*ALCODESTART::1658497469833*/
if (v_timeStepsElapsed <= 1) {
	f_setStartView();
}
// Trigger timestep in energymodel for continuous simulation ('interactive mode')
energyModel.f_runTimestep();
v_timeStepsElapsed ++;

// This function colors the trafos according to the amount of congestion. (maybe move this function call somewhere else?)
if ( b_updateCongestionColors ){
	for (GridNode gn : energyModel.pop_gridNodes) {
		if (v_clickedObjectType != OL_GISObjectType.GRIDNODE || v_clickedGridNode != gn) {
			f_setGridNodeCongestionColor( gn );
		}
	}
}

// Update the live plot dataset of gcList
if (c_selectedGridConnections.size() > 0) {
	f_addTimeStepLiveDataSetsGC(uI_Results.v_gridConnection, c_selectedGridConnections);
	
	if (p_selectedProjectType == OL_ProjectType.BUSINESSPARK) {
		if (( b_multiSelect || b_EHubSelect ) && c_selectedGridConnections.size() >= 1) {
			for (int i = 0; i < c_selectedGridConnections.size(); i++) {
				f_addTimeStepLiveDataSetsGC(uI_Results.c_individualGridConnections.get(i), new ArrayList<GridConnection>(c_selectedGridConnections.subList(i, i+1)));
			}
			//f_addTimeStepTotalLiveDataSets();
			
			if (b_EHubSelect) {
				f_updateLiveEHubCapacityDataSets();
			}
		}
	}
}
else{
f_addTimeStepLiveDataSetsMain(uI_Results.v_area);
}

// Update the NFATO values in the selected companyUI
if(c_selectedGridConnections.size() > 0 && c_selectedGridConnections.get(0).v_enableNFato){
	if(c_companyUIs.get(v_connectionOwnerIndexNr).v_NFATO_active){
		c_companyUIs.get(v_connectionOwnerIndexNr).f_getNFATOValues();
	}
}

// Get the weather info
f_getWeatherInfo();
/*ALCODEEND*/}

