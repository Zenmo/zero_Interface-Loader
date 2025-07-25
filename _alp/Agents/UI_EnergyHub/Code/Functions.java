double f_initializeEnergyHubDashboard()
{/*ALCODESTART::1753446461346*/
//Set map to correct layout
zero_Interface.rb_mapOverlay.setValue(zero_Interface.c_loadedMapOverlayTypes.indexOf(OL_MapOverlayTypes.DEFAULT),true);
zero_Interface.b_updateLiveCongestionColors = false;

for (GridConnection GC : c_selectedEnergyHubGC) { //Buildings that are grouped, select as well.
	for (GIS_Object object : GC.c_connectedGISObjects) { //Buildings that are grouped, select as well.
		object.gisRegion.setFillColor(zero_Interface.v_selectionColorAddBuildings);
	}
}
/*ALCODEEND*/}

