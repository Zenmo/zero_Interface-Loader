void e_calculateEnergyBalance()
{/*ALCODESTART::1658497469833*/
// Trigger timestep in energymodel for continuous simulation ('interactive mode')
energyModel.f_runTimestep();

// This function colors the trafos according to the amount of congestion. (maybe move this function call somewhere else?)
if ( b_updateLiveCongestionColors ){
	for (GridNode gn : energyModel.pop_gridNodes) {
		if ((v_clickedObjectType != OL_GISObjectType.GRIDNODE || v_clickedGridNode != gn) && gn.p_energyCarrier == OL_EnergyCarriers.ELECTRICITY) {
			f_setColorsBasedOnCongestion_gridnodes(gn, true);
		}
	}
}

// Update the NFATO values in the selected companyUI
if(c_selectedGridConnections.size() > 0 && c_selectedGridConnections.get(0).v_enableNFato){
	if(uI_Company.v_NFATO_active){
		uI_Company.f_getNFATOValues();
	}
}

// Get the weather info
f_getWeatherInfo();
/*ALCODEEND*/}

void e_setStartView()
{/*ALCODESTART::1743509682728*/
f_setStartView();
/*ALCODEEND*/}

void e_setScenarioToCustom()
{/*ALCODESTART::1761119827697*/
String[] scenarioOptions = f_getScenarioOptions();

int customOptionIndex = 0;
for(String scenarioOption : scenarioOptions){
	if(scenarioOption.equals("Custom")){
		break;
	}
	customOptionIndex++;
}

rb_scenarios.setValue(customOptionIndex, true);

b_changeToCustomScenario = false;
e_setScenarioToCustom.restart();

/*ALCODEEND*/}

