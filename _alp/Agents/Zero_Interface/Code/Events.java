void e_calculateEnergyBalance()
{/*ALCODESTART::1658497469833*/
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

// Update the NFATO values in the selected companyUI
if(c_selectedGridConnections.size() > 0 && c_selectedGridConnections.get(0).v_enableNFato){
	if(c_companyUIs.get(v_connectionOwnerIndexNr).v_NFATO_active){
		c_companyUIs.get(v_connectionOwnerIndexNr).f_getNFATOValues();
	}
}

// Get the weather info
f_getWeatherInfo();
/*ALCODEEND*/}

void e_setStartView()
{/*ALCODESTART::1743509682728*/
f_setStartView();
/*ALCODEEND*/}

