boolean f_setCapacitySharingContract(double[] weekCapacities,double[] weekendCapacities)
{/*ALCODESTART::1722256365452*/
double[] sharedWeekDeliveryCapacity_kW = new double[24];
double[] sharedWeekendDeliveryCapacity_kW = new double[24];
double[] sharedWeekFeedinCapacity_kW = new double[24];
double[] sharedWeekendFeedinCapacity_kW = new double[24];

switch (rb_capacitySharingDeliveryOrFeedin.getValue()) {
	case 0: // Delivery
		// Set the variables of the GCs
		for (int i = 0; i < 24; i++) {
			sharedWeekDeliveryCapacity_kW[i] = weekCapacities[i];
			sharedWeekendDeliveryCapacity_kW[i] = weekendCapacities[i];
		}
		break;
		
	case 1: // Feed In
		// Set the variables of the GCs
		for (int i = 0; i < 24; i++) {
			sharedWeekFeedinCapacity_kW[i] = weekCapacities[i];
			sharedWeekendFeedinCapacity_kW[i] = weekendCapacities[i];
		}
		break;
		
	case 2: // Both
		// Set the variables of the GCs
		for (int i = 0; i < 24; i++) {
			sharedWeekDeliveryCapacity_kW[i] = weekCapacities[i];
			sharedWeekendDeliveryCapacity_kW[i] = weekendCapacities[i];
			sharedWeekFeedinCapacity_kW[i] = weekCapacities[i];
			sharedWeekendFeedinCapacity_kW[i] = weekendCapacities[i];
		}	
		break;
		
	default:
		throw new IllegalStateException("Invalid Setting in rb_deliveryOrFeedin");
}


new J_GridCapacitySharingManager(v_recievingGC, v_sendingGC, sharedWeekDeliveryCapacity_kW, sharedWeekendDeliveryCapacity_kW, 
								 sharedWeekFeedinCapacity_kW, sharedWeekendFeedinCapacity_kW, zero_Interface.energyModel.p_timeVariables);
/*ALCODEEND*/}

double f_checkGISRegion(double clickx,double clicky)
{/*ALCODESTART::1722256365459*/
//Check if click was on Building
for ( GIS_Building b : zero_Interface.energyModel.pop_GIS_Buildings ){
	if( b.gisRegion != null && b.gisRegion.contains(clickx, clicky) ){
		if (b.gisRegion.isVisible()) {
			GridConnection GC = b.c_containedGridConnections.get(0);
			if (GC != null && GC != v_recievingGC) {
				// found a valid GC
				// Check if it is the first GC
				if (v_recievingGC == null) {
					t_capacitySharingRecievingGC.setText(GC.p_ownerID + " zal ontvangen");
					t_capacitySharingSendingGC.setText("Klik op een gebouw dat zijn capaciteit gaat afstaan");
					v_recievingGC = GC;
				}
				else {
					v_sendingGC = GC;
					t_capacitySharingSendingGC.setText(GC.p_ownerID + " zal leveren");
					//for (GIS_Building b : GC.c_connectedBuildings) {
						//b.gisRegion.setFillColor(v_selectionColorAddBuildings);
					//}
					// We found two buildings, return to the default clicking functionality
					b_inCapacitySharingSelectionMode = false;
				}
			}
		}
	}
}
/*ALCODEEND*/}

boolean f_checkCapacitySharingContract(double[] weekCapacities,double[] weekendCapacities)
{/*ALCODESTART::1722256365466*/
if (v_recievingGC == null || v_sendingGC == null || v_recievingGC == v_sendingGC) {
	throw new IllegalStateException("Invalid Capacity sharing contract settings, Please select two (different) gridconnections.");
}
 
    
double[] weekTestDelivery = new double[24];
double[] weekendTestDelivery = new double[24];
double[] weekTestFeedin = new double[24];
double[] weekendTestFeedin = new double[24];

double maxSharedDeliveryCapacity_kW;
double maxSharedFeedinCapacity_kW;
		
switch (rb_capacitySharingDeliveryOrFeedin.getValue()) {
	case 0: // Delivery
		for (int i = 0; i < 24; i++) {
			weekTestDelivery[i] = weekCapacities[i] - v_sendingGC.v_liveConnectionMetaData.getSharedDeliveryCapacityAtHourOfWeekDay_kW(i);
			weekendTestDelivery[i] = weekendCapacities[i] - v_sendingGC.v_liveConnectionMetaData.getSharedDeliveryCapacityAtHourOfWeekendDay_kW(i);
		}
		maxSharedDeliveryCapacity_kW = max(max(weekTestDelivery), max(weekendTestDelivery));

		// Check if sendingGC has enough capacity with the original connection capacity
		if ( maxSharedDeliveryCapacity_kW > v_sendingGC.v_liveConnectionMetaData.getDefaultContractedDeliveryCapacity_kW() ) {
			zero_Interface.f_setErrorScreen("Invalid Capacity Sharing settings, " + v_sendingGC.p_ownerID + " does not have a delivery capacity of " + maxSharedDeliveryCapacity_kW + " kW available", 0, 0);
			return false;
		}
		return true;	
	case 1: // Feed In
		for (int i = 0; i < 24; i++) {
			weekTestFeedin[i] = weekCapacities[i] - v_sendingGC.v_liveConnectionMetaData.getSharedFeedinCapacityAtHourOfWeekDay_kW(i);
			weekendTestFeedin[i] = weekendCapacities[i] - v_sendingGC.v_liveConnectionMetaData.getSharedFeedinCapacityAtHourOfWeekendDay_kW(i);
		}
		maxSharedFeedinCapacity_kW = max(max(weekTestFeedin), max(weekendTestFeedin));
		
		// Check if gc2 has enough capacity with the original connection capacity
		if ( maxSharedFeedinCapacity_kW > v_sendingGC.v_liveConnectionMetaData.getDefaultContractedFeedinCapacity_kW() ) {
			zero_Interface.f_setErrorScreen("Invalid Non-Firm ATO Settings, " + v_sendingGC.p_ownerID + " does not have a feed in capacity of " + maxSharedFeedinCapacity_kW + " kW available", 0, 0);
			return false;
		}
		return true;		
	case 2: // Both
		for (int i = 0; i < 24; i++) {
			weekTestDelivery[i] = weekCapacities[i] - v_sendingGC.v_liveConnectionMetaData.getSharedDeliveryCapacityAtHourOfWeekDay_kW(i);
			weekendTestDelivery[i] = weekendCapacities[i] - v_sendingGC.v_liveConnectionMetaData.getSharedDeliveryCapacityAtHourOfWeekendDay_kW(i);
		}
		for (int i = 0; i < 24; i++) {
			weekTestFeedin[i] = weekCapacities[i] - v_sendingGC.v_liveConnectionMetaData.getSharedFeedinCapacityAtHourOfWeekDay_kW(i);
			weekendTestFeedin[i] = weekendCapacities[i] - v_sendingGC.v_liveConnectionMetaData.getSharedFeedinCapacityAtHourOfWeekendDay_kW(i);
		}
		
		maxSharedDeliveryCapacity_kW = max(max(weekTestDelivery), max(weekendTestDelivery));
		maxSharedFeedinCapacity_kW = max(max(weekTestFeedin), max(weekendTestFeedin));
		
		// Check if sendingGC has enough capacity with the original connection capacity
		if ( maxSharedDeliveryCapacity_kW > v_sendingGC.v_liveConnectionMetaData.getDefaultContractedDeliveryCapacity_kW() ) {
			zero_Interface.f_setErrorScreen("Invalid Non-Firm ATO Settings, " + v_sendingGC.p_ownerID + " does not have a delivery capacity of " + maxSharedDeliveryCapacity_kW + " kW available", 0, 0);
			return false;
		}
		else if ( maxSharedFeedinCapacity_kW > v_sendingGC.v_liveConnectionMetaData.getDefaultContractedFeedinCapacity_kW() ) {
			zero_Interface.f_setErrorScreen("Invalid Non-Firm ATO Settings, " + v_sendingGC.p_ownerID + " does not have a feed in capacity of " + maxSharedFeedinCapacity_kW + " kW available", 0, 0);
			return false;
		}
		return true;			
	default:
		throw new IllegalStateException("Invalid Setting in rb_deliveryOrFeedin");
}
/*ALCODEEND*/}

double[][] f_constructCapacitySharingArrays()
{/*ALCODESTART::1722256365474*/
// Construct arrays from slider values
// Week
int weekStart_h = (int) sl_capacitySharingWeekStartTime.getValue();
int weekEnd_h = (int) sl_capacitySharingWeekEndTime.getValue();
int weekCapacity_kW = (int) sl_capacitySharingWeekCapacity.getValue();

double[] weekCapacities = new double[24];
double[] weekendCapacities = new double[24];

if (weekStart_h < weekEnd_h) {
	for (int i = 0; i < weekStart_h; i++) {
		weekCapacities[i] = 0;
	}
	for (int j = weekStart_h; j < weekEnd_h; j ++) {
		weekCapacities[j] = weekCapacity_kW;
	}
	for (int k = weekEnd_h; k < 24; k ++) {
		weekCapacities[k] = 0;
	}
}
else { // If the start time is higher than the end time we share capacity at night
	for (int i = 0; i < weekEnd_h; i++) {
		weekCapacities[i] = weekCapacity_kW;
	}
	for (int j = weekEnd_h; j < weekStart_h ; j ++) {
		weekCapacities[j] = 0;
	}
	for (int k = weekStart_h; k < 24; k ++) {
		weekCapacities[k] = weekCapacity_kW;
	}
}

if (b_capacitySharingContractWeekendDistinction) {
	// repeat above code for weekend
	int weekendStart_h = (int) sl_capacitySharingWeekendStartTime.getValue();
	int weekendEnd_h = (int) sl_capacitySharingWeekendEndTime.getValue();
	int weekendCapacity_kW = (int) sl_capacitySharingWeekendCapacity.getValue();
	
	
	if (weekendStart_h < weekendEnd_h) {
		for (int i = 0; i < weekendStart_h; i++) {
			weekendCapacities[i] = 0;
		}
		for (int j = weekendStart_h; j < weekendEnd_h; j ++) {
			weekendCapacities[j] = weekendCapacity_kW;
		}
		for (int k = weekendEnd_h; k < 24; k ++) {
			weekendCapacities[k] = 0;
		}
	}
	else { // If the start time is higher than the end time we share capacity at night
		for (int i = 0; i < weekendEnd_h; i++) {
			weekendCapacities[i] = weekendCapacity_kW;
		}
		for (int j = weekendEnd_h; j < weekendStart_h; j ++) {
			weekendCapacities[j] = 0;
		}
		for (int k = weekendStart_h; k < 24; k ++) {
			weekendCapacities[k] = weekendCapacity_kW;
		}
	}
}
else {
	// no distinction means the settings are the same during the weekend
	weekendCapacities = weekCapacities;
}

double[][] arr = {weekCapacities, weekendCapacities};
return arr;
/*ALCODEEND*/}

double f_resetCapacitySharingSettings()
{/*ALCODESTART::1722256365483*/
t_capacitySharingRecievingGC.setText("Klik op een gebouw dat capaciteit gaat ontvangen");
t_capacitySharingSendingGC.setText("");
v_recievingGC = null;
v_sendingGC = null;
b_inCapacitySharingSelectionMode = false;
/*ALCODEEND*/}

double f_setTab(OL_CustomScenarioTabs selectedTabType)
{/*ALCODESTART::1722256998182*/
if (selectedTabType == OL_CustomScenarioTabs.NFATO) {
	gr_nfatoSettings.setVisible(true);
	gr_hubSliders.setVisible(false);
}
else {
	gr_nfatoSettings.setVisible(false);
	gr_hubSliders.setVisible(true);
}
/*ALCODEEND*/}

double f_updateSliders_EHub()
{/*ALCODESTART::1754923608234*/
//Function that can be used to update sliders/buttons to the engine state
//--> empty for now
/*ALCODEEND*/}

