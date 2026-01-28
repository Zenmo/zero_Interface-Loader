boolean f_setNFATO(double[] weekCapacities,double[] weekendCapacities)
{/*ALCODESTART::1722256365452*/
GridConnection gc1 = v_nfatoFirstGC;
GridConnection gc2 = v_nfatoSecondGC;

// Reset the GC Capacities if they already had a NF-ATO
gc1.f_nfatoSetConnectionCapacity(true, zero_Interface.energyModel.p_timeVariables);
gc2.f_nfatoSetConnectionCapacity(true, zero_Interface.energyModel.p_timeVariables);

switch (rb_deliveryOrFeedin.getValue()) {
	case 0: // Delivery
		// Set the variables of the GCs
		for (int i = 0; i < 24; i++) {
			gc1.v_nfatoWeekDeliveryCapacity_kW[i] += weekCapacities[i];
			gc2.v_nfatoWeekDeliveryCapacity_kW[i] += -weekCapacities[i];
			gc1.v_nfatoWeekendDeliveryCapacity_kW[i] += weekendCapacities[i];
			gc2.v_nfatoWeekendDeliveryCapacity_kW[i] += -weekendCapacities[i];
		}
		break;
		
	case 1: // Feed In
		// Set the variables of the GCs
		for (int i = 0; i < 24; i++) {
			gc1.v_nfatoWeekFeedinCapacity_kW[i] += weekCapacities[i];
			gc2.v_nfatoWeekFeedinCapacity_kW[i] += -weekCapacities[i];
			gc1.v_nfatoWeekendFeedinCapacity_kW[i] += weekendCapacities[i];
			gc2.v_nfatoWeekendFeedinCapacity_kW[i] += -weekendCapacities[i];
		}
		break;
		
	case 2: // Both
		// Set the variables of the GCs
		for (int i = 0; i < 24; i++) {
			gc1.v_nfatoWeekDeliveryCapacity_kW[i] += weekCapacities[i];
			gc2.v_nfatoWeekDeliveryCapacity_kW[i] += -weekCapacities[i];
			gc1.v_nfatoWeekendDeliveryCapacity_kW[i] += weekendCapacities[i];
			gc2.v_nfatoWeekendDeliveryCapacity_kW[i] += -weekendCapacities[i];
			
			gc1.v_nfatoWeekFeedinCapacity_kW[i] += weekCapacities[i];
			gc2.v_nfatoWeekFeedinCapacity_kW[i] += -weekCapacities[i];
			gc1.v_nfatoWeekendFeedinCapacity_kW[i] += weekendCapacities[i];
			gc2.v_nfatoWeekendFeedinCapacity_kW[i] += -weekendCapacities[i];
		}	
		break;
		
	default:
		throw new IllegalStateException("Invalid Setting in rb_deliveryOrFeedin");
}

// Update the Connection Capacity if it is needed at the current time
gc1.f_nfatoSetConnectionCapacity(false, zero_Interface.energyModel.p_timeVariables);
gc2.f_nfatoSetConnectionCapacity(false, zero_Interface.energyModel.p_timeVariables);

gc1.v_enableNFato = true;
gc2.v_enableNFato = true;
/*ALCODEEND*/}

double f_checkGISRegion(double clickx,double clicky)
{/*ALCODESTART::1722256365459*/
//Check if click was on Building
for ( GIS_Building b : zero_Interface.energyModel.pop_GIS_Buildings ){
	if( b.gisRegion != null && b.gisRegion.contains(clickx, clicky) ){
		if (b.gisRegion.isVisible()) {
			GridConnection GC = b.c_containedGridConnections.get(0);
			if (GC != null && GC != v_nfatoFirstGC) {
				// found a valid GC
				// Check if it is the first GC
				if (v_nfatoFirstGC == null) {
					t_nfatoFirstBuilding.setText(GC.p_ownerID + " zal ontvangen");
					t_nfatoSecondBuilding.setText("Klik op een gebouw dat zijn capaciteit gaat afstaan");
					v_nfatoFirstGC = GC;
					//for (GIS_Building b : GC.c_connectedBuildings) {
						//b.gisRegion.setFillColor(v_selectionColorAddBuildings);
					//}
				}
				else {
					v_nfatoSecondGC = GC;
					t_nfatoSecondBuilding.setText(GC.p_ownerID + " zal leveren");
					//for (GIS_Building b : GC.c_connectedBuildings) {
						//b.gisRegion.setFillColor(v_selectionColorAddBuildings);
					//}
					// We found two buildings, return to the default clicking functionality
					b_NFATOListener = false;
				}
			}
		}
	}
}
/*ALCODEEND*/}

boolean f_checkNFATO(double[] weekCapacities,double[] weekendCapacities)
{/*ALCODESTART::1722256365466*/
GridConnection gc1 = v_nfatoFirstGC;
GridConnection gc2 = v_nfatoSecondGC;

if (gc1 == null || gc2 == null || gc1 == gc2) {
	throw new IllegalStateException("Invalid Non-Firm ATO Settings, Please select two gridconnections");
}

double[] weekTestDelivery = new double[24];
double[] weekendTestDelivery = new double[24];
double[] weekTestFeedin = new double[24];
double[] weekendTestFeedin = new double[24];

double maxDeliveryCapacity_kW;
double maxFeedinCapacity_kW;
		
switch (rb_deliveryOrFeedin.getValue()) {
	case 0: // Delivery
		for (int i = 0; i < 24; i++) {
			weekTestDelivery[i] = weekCapacities[i] - gc2.v_nfatoWeekDeliveryCapacity_kW[i];
			weekendTestDelivery[i] = weekendCapacities[i] - gc2.v_nfatoWeekendDeliveryCapacity_kW[i];
		}
		maxDeliveryCapacity_kW = max(max(weekTestDelivery), max(weekendTestDelivery));
		// Reset the GC Capacity in case they already had a NF-ATO
		gc2.f_nfatoSetConnectionCapacity(true, zero_Interface.energyModel.p_timeVariables);
		// Check if gc2 has enough capacity with the original connection capacity
		if ( maxDeliveryCapacity_kW > gc2.v_liveConnectionMetaData.contractedDeliveryCapacity_kW ) {
			// Restore previous NF-ATO
			gc2.f_nfatoSetConnectionCapacity(false, zero_Interface.energyModel.p_timeVariables);
			throw new IllegalStateException("Invalid Non-Firm ATO Settings, " + gc2.p_ownerID + " does not have a delivery capacity of " + maxDeliveryCapacity_kW + " kW available");
		}
		else {
			// Restore previous NF-ATO
			gc2.f_nfatoSetConnectionCapacity(false, zero_Interface.energyModel.p_timeVariables);
			return true;
		}
		
	case 1: // Feed In
		for (int i = 0; i < 24; i++) {
			weekTestFeedin[i] = weekCapacities[i] - gc2.v_nfatoWeekFeedinCapacity_kW[i];
			weekendTestFeedin[i] = weekendCapacities[i] - gc2.v_nfatoWeekendFeedinCapacity_kW[i];
		}
		maxFeedinCapacity_kW = max(max(weekTestFeedin), max(weekendTestFeedin));
		// Reset the GC Capacity in case they already had a NF-ATO
		gc2.f_nfatoSetConnectionCapacity(true, zero_Interface.energyModel.p_timeVariables);
		// Check if gc2 has enough capacity with the original connection capacity
		if ( maxFeedinCapacity_kW > gc2.v_liveConnectionMetaData.contractedFeedinCapacity_kW ) {
			// Restore previous NF-ATO
			gc2.f_nfatoSetConnectionCapacity(false, zero_Interface.energyModel.p_timeVariables);
			throw new IllegalStateException("Invalid Non-Firm ATO Settings, " + gc2.p_ownerID + " does not have a feed in capacity of " + maxFeedinCapacity_kW + " kW available");
		}
		else {
			// Restore previous NF-ATO
			gc2.f_nfatoSetConnectionCapacity(false, zero_Interface.energyModel.p_timeVariables);
			return true;
		}
		
	case 2: // Both
		for (int i = 0; i < 24; i++) {
			weekTestDelivery[i] = weekCapacities[i] - gc2.v_nfatoWeekDeliveryCapacity_kW[i];
			weekendTestDelivery[i] = weekendCapacities[i] - gc2.v_nfatoWeekendDeliveryCapacity_kW[i];
		}
		for (int i = 0; i < 24; i++) {
			weekTestFeedin[i] = weekCapacities[i] - gc2.v_nfatoWeekFeedinCapacity_kW[i];
			weekendTestFeedin[i] = weekendCapacities[i] - gc2.v_nfatoWeekendFeedinCapacity_kW[i];
		}
		
		maxDeliveryCapacity_kW = max(max(weekTestDelivery), max(weekendTestDelivery));
		maxFeedinCapacity_kW = max(max(weekTestFeedin), max(weekendTestFeedin));
		// Reset the GC Capacity in case they already had a NF-ATO
		gc2.f_nfatoSetConnectionCapacity(true, zero_Interface.energyModel.p_timeVariables);
		// Check if gc2 has enough capacity with the original connection capacity
		if ( maxDeliveryCapacity_kW > gc2.v_liveConnectionMetaData.contractedDeliveryCapacity_kW ) {
			// Restore previous NF-ATO
			gc2.f_nfatoSetConnectionCapacity(false, zero_Interface.energyModel.p_timeVariables);
			throw new IllegalStateException("Invalid Non-Firm ATO Settings, " + gc2.p_ownerID + " does not have a delivery capacity of " + maxDeliveryCapacity_kW + " kW available");
		}
		else if ( maxFeedinCapacity_kW > gc2.v_liveConnectionMetaData.contractedFeedinCapacity_kW ) {
			// Restore previous NF-ATO
			gc2.f_nfatoSetConnectionCapacity(false, zero_Interface.energyModel.p_timeVariables);
			throw new IllegalStateException("Invalid Non-Firm ATO Settings, " + gc2.p_ownerID + " does not have a feed in capacity of " + maxFeedinCapacity_kW + " kW available");
		}
		else {
			// Restore previous NF-ATO
			gc2.f_nfatoSetConnectionCapacity(false, zero_Interface.energyModel.p_timeVariables);
			return true;
		}
		
	default:
		throw new IllegalStateException("Invalid Setting in rb_deliveryOrFeedin");
}
/*ALCODEEND*/}

double[][] f_constructNFATOArrays()
{/*ALCODESTART::1722256365474*/
// Construct arrays from slider values
// Week
int weekStart_h = (int) sl_nfatoWeekStartTime.getValue();
int weekEnd_h = (int) sl_nfatoWeekEndTime.getValue();
int weekCapacity_kW = (int) sl_nfatoWeekCapacity.getValue();

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

if (b_nfatoWeekendDistinction) {
	// repeat above code for weekend
	int weekendStart_h = (int) sl_nfatoWeekendStartTime.getValue();
	int weekendEnd_h = (int) sl_nfatoWeekendEndTime.getValue();
	int weekendCapacity_kW = (int) sl_nfatoWeekendCapacity.getValue();
	
	
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

double f_resetNFATOSettings()
{/*ALCODESTART::1722256365483*/
t_nfatoFirstBuilding.setText("Klik op een gebouw dat capaciteit gaat ontvangen");
t_nfatoSecondBuilding.setText("");
v_nfatoFirstGC = null;
v_nfatoSecondGC = null;
b_NFATOListener = false;
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

