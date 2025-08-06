double f_createGISRegionOutline()
{/*ALCODESTART::1726584205769*/
// Create neighborhoods and draw them
for (Neighbourhood_data NBH : c_neighbourhood_data) {	
	GIS_Object area = energyModel.add_pop_GIS_Objects();
	
	area.p_id = NBH.districtname();
	area.p_GISObjectType = OL_GISObjectType.valueOf(NBH.neighbourhoodtype());

	//Create gisregion
	area.gisRegion = zero_Interface.f_createGISObject(f_createGISObjectsTokens(NBH.polygon(), area.p_GISObjectType));
	
	//Style area 
	zero_Interface.f_styleSimulationAreas(area);
	zero_Interface.c_GISNeighborhoods.add(area);
}


/*ALCODEEND*/}

double f_createGridConnections()
{/*ALCODESTART::1726584205771*/
//Energy production sites
f_createSolarParks();
f_createWindFarms();

//Other infra assets
f_createChargingStations();
f_createElectrolysers();
f_createBatteries();

//Consumers
f_createCompanies();
f_createHouses();
/*ALCODEEND*/}

double f_configureEngine_default()
{/*ALCODESTART::1726584205773*/
//Set basic input files
energyModel.p_truckTripsCsv = inputCSVtruckTrips;
energyModel.p_householdTripsCsv = inputCSVhouseholdTrips;
energyModel.p_cookingPatternCsv = inputCSVcookingActivities;

//Initialize specific slider GC
f_initializeSpecificSliderGC();

//Actors
f_createActors();

//Grid nodes
f_createGridNodes();

//Grid connections
f_createGridConnections();

//Additional GIS objects
f_createAdditionalGISObjects();

//Initialize the engine
energyModel.p_runStartTime_h = v_simStartHour_h;
energyModel.p_runEndTime_h = v_simStartHour_h + v_simDuration_h;
energyModel.f_initializeEngine();

/*ALCODEEND*/}

double f_createGridNodes()
{/*ALCODESTART::1726584205775*/
//double latitude_c;
//double longitude_c;
OL_GridNodeType nodeType;
GISRegion gisregion;

// Grid operator (for now only one in the area)
GridOperator Grid_Operator = findFirst(energyModel.pop_gridOperators, p->p.p_actorID.equals(project_data.grid_operator())) ;

for (GridNode_data GN_data : c_gridNode_data) {
	//    if no scope selected, or if node has 'all scopes' in input file or if the node specific scope is selected (exists in the arrayList)       
	if( settings.subscopesToSimulate() == null || settings.subscopesToSimulate().isEmpty() || GN_data.subscope() == null || settings.subscopesToSimulate().indexOf(GN_data.subscope()) > -1 ){ 
		if (GN_data.status()) {
			GridNode GN = energyModel.add_pop_gridNodes();
			GN.p_gridNodeID = GN_data.gridnode_id();
			c_gridNodeIDsInScope.add(GN.p_gridNodeID);
			
			// Check wether transformer capacity is known or estimated
			GN.p_capacity_kW = GN_data.capacity_kw();
			GN.p_originalCapacity_kW = GN.p_capacity_kW;	
			GN.p_realCapacityAvailable = GN_data.is_capacity_available();
			
			// Basic GN information
			//GN.p_nodeStatus = GN_data.status();
			GN.p_description = GN_data.description();
			String nodeTypeString = GN_data.type();
			
			// Connect
			GN.p_parentNodeID = GN_data.parent_node_id(); // Needs to be manually defined in the excel file of the nodes!
			GN.p_ownerGridOperator = Grid_Operator;
			
			//Define node type
			switch (nodeTypeString) {
			    case "LVLV":
			        GN.p_nodeType = OL_GridNodeType.LVLV;
			        GN.p_energyCarrier = OL_EnergyCarriers.ELECTRICITY;
			        break;
			    case "MVLV":
			        GN.p_nodeType = OL_GridNodeType.MVLV;
			        GN.p_energyCarrier = OL_EnergyCarriers.ELECTRICITY;
			        break;
			    case "SUBMV":
			        GN.p_nodeType = OL_GridNodeType.SUBMV;
			        GN.p_energyCarrier = OL_EnergyCarriers.ELECTRICITY;
			        zero_Interface.b_gridLoopsAreDefined = true;
			        break;
			    case "MVMV":
			        GN.p_nodeType = OL_GridNodeType.MVMV;
			        GN.p_energyCarrier = OL_EnergyCarriers.ELECTRICITY;
			        break;
			    case "HVMV":
			        GN.p_nodeType = OL_GridNodeType.HVMV;
			        GN.p_energyCarrier = OL_EnergyCarriers.ELECTRICITY;
			        break;
			    default:
			        traceln("There is a gridnode in your input file with an incorrect node type");
			        break;
			}
			
			//Define GN location
			GN.p_latitude = GN_data.latitude();
			GN.p_longitude = GN_data.longitude();
			GN.setLatLon(GN.p_latitude, GN.p_longitude);
			
			//Create gis region
			GN.gisRegion = zero_Interface.f_createGISObject(f_createGISNodesTokens(GN));
			zero_Interface.f_styleGridNodes(GN);
			
			//Grid operator nodes
			Grid_Operator.c_electricityGridNodes.add(GN);
			
			
			//Gridnode service area
			if (GN_data.service_area_polygon() != null){
				//Create service area gis object
				//GIS_Object serviceArea = f_createGISObject(GN.p_gridNodeID + ": service area", GN.p_latitude, GN.p_longitude, GN_data.service_area_polygon());
				GISRegion serviceArea = zero_Interface.f_createGISObject(f_createGISObjectsTokens(GN_data.service_area_polygon(), OL_GISObjectType.GN_SERVICE_AREA));
				
				//Add to hashmap
				zero_Interface.c_GISNetplanes.add( serviceArea );
			}
			
			//Gridnode profile
			if(GN_data.profile_data_kWh() != null){
				f_addGridNodeProfile(GN, GN_data.profile_data_kWh());
			}
		}
	}
}


/*ALCODEEND*/}

double[] f_createGISObjectsTokens(String RegionCoords,OL_GISObjectType GISObjectType)
{/*ALCODESTART::1726584205777*/
if (RegionCoords.startsWith("MultiPolygon")){
	RegionCoords = RegionCoords.replace("MultiPolygon (((","");
	RegionCoords = RegionCoords.replace(","," ");
	RegionCoords = RegionCoords.replace(")))","");
}
else if(RegionCoords.startsWith("MULTIPOLYGON")){
	RegionCoords = RegionCoords.replace("MULTIPOLYGON (((","");
	RegionCoords = RegionCoords.replace(","," ");
	RegionCoords = RegionCoords.replace(")))","");
}
else if(RegionCoords.startsWith("Poly")){
	RegionCoords = RegionCoords.replace("Polygon ((","");
	RegionCoords = RegionCoords.replace(","," ");
	RegionCoords = RegionCoords.replace("))","");
}
else if(RegionCoords.startsWith("POLYGON")){
	RegionCoords = RegionCoords.replace("POLYGON ((","");
	RegionCoords = RegionCoords.replace(","," ");
	RegionCoords = RegionCoords.replace("))","");
}
else if(RegionCoords.startsWith("MultiLineString")){
	RegionCoords = RegionCoords.replace("MultiLineString ((","");
	RegionCoords = RegionCoords.replace(","," ");
	RegionCoords = RegionCoords.replace("))","");
}
else if(RegionCoords.startsWith("LineString")){
	RegionCoords = RegionCoords.replace("LineString (","");
	RegionCoords = RegionCoords.replace(","," ");
	RegionCoords = RegionCoords.replace(")","");
}
else {
	traceln("GIS coordinaten in de excel data die niet starten met Multi of Poly");
}



if(RegionCoords.contains(")(") || RegionCoords.contains(") (") || RegionCoords.contains(")  (")){
	if (GISObjectType == OL_GISObjectType.ANTI_LAYER){
		RegionCoords = RegionCoords.replace(")("," "); // Combine all polies into one!
		RegionCoords = RegionCoords.replace(") ("," "); // Combine all polies into one!
	}
	else{
		RegionCoords = RegionCoords.split("\\)")[0];
	}
}

RegionCoords = RegionCoords.replace("  "," ");
String delims = " ";
String[] tokens;
tokens = RegionCoords.split(delims);

int nbCoords = tokens.length;
double[] GISCoords = new double[nbCoords];
		
for (int i=0; i<nbCoords; i++) {
	if (i % 2 == 0) { // latitudes
		GISCoords[i]=Double.parseDouble(tokens[i+1]);
	} else { // longitudes
		GISCoords[i]=Double.parseDouble(tokens[i-1]);
	}
}
return GISCoords;
/*ALCODEEND*/}

double f_importExcelTablesToDB()
{/*ALCODESTART::1726584205779*/
//inputHouseholdTrips.readFile();
//inputCookingActivities.readFile();
//inputTruckTrips.readFile();

//inputCSVcookingActivities.readFile();
//inputCSVcookingActivities.

if(settings.reloadDatabase()){
	
	//Get the database names that are selected for reloading
	List<String[]> databaseNames = project_data.databaseNames();
	
	//Get the model database
	ModelDatabase modelDB = getEngine().getModelDatabase();
	
	//Loop over all databases
	for(String[] databaseName : databaseNames){
	
		//Create the file path string
		String filePathString = "data_" + project_data.project_name() + "/" + databaseName[0] + "_" + project_data.project_name() + ".xlsx";
		
		//If file exists, load it into the database
		File f = new File(filePathString);
		if(f.exists() && !f.isDirectory()) { 			
			Database myFileDatabase = new Database(this, databaseName[1], filePathString);
			modelDB.importFromExternalDB(myFileDatabase.getConnection(), databaseName[1], databaseName[1], true, false);
			traceln("Database %s has been updated.", databaseName[1]);	
		} else { // if file does not exist, clear the database to make sure there are no wrong values in the simulation!
			executeStatement("DELETE FROM " + databaseName[1] + " c");
			traceln("File not found, database %s has been cleared!", databaseName[1]);
		}
	}

	//Overwrite specific database values
	f_overwriteSpecificDatabaseValues();
}
/*ALCODEEND*/}

double f_createSolarParks()
{/*ALCODESTART::1726584205785*/
ConnectionOwner owner;
GCEnergyProduction solarpark;

List<String> existing_actors = new ArrayList();
List<String> existing_solarFields = new ArrayList();

for (Solarfarm_data dataSolarfarm : f_getSolarfarmsInSubScope(c_solarfarm_data)) { // MOET NOG CHECK OF ZONNEVELD ACTOR AL BESTAAT OP, zo ja --> Zonneveld koppelen aan elkaar en niet 2 GC en 2 actoren maken.
	
	if (!existing_solarFields.contains( dataSolarfarm.gc_id() )) {
		solarpark = energyModel.add_EnergyProductionSites();
		
		solarpark.set_p_gridConnectionID( dataSolarfarm.gc_id() );
		solarpark.set_p_name( dataSolarfarm.gc_name() );
		
		//Check wether it can be changed using sliders
		solarpark.p_isSliderGC = dataSolarfarm.isSliderGC();
		
		//Grid Capacity
		solarpark.v_liveConnectionMetaData.physicalCapacity_kW = dataSolarfarm.connection_capacity_kw();
		if ( dataSolarfarm.connection_capacity_kw() > 0 ) {
			solarpark.v_liveConnectionMetaData.physicalCapacityKnown = true;
		}
		if ( dataSolarfarm.contracted_feed_in_capacity_kw() != null) {
			solarpark.v_liveConnectionMetaData.contractedFeedinCapacity_kW = dataSolarfarm.contracted_feed_in_capacity_kw();
			solarpark.v_liveConnectionMetaData.contractedFeedinCapacityKnown = true;
		}
		else {
			solarpark.v_liveConnectionMetaData.contractedFeedinCapacity_kW = dataSolarfarm.connection_capacity_kw();
			solarpark.v_liveConnectionMetaData.contractedFeedinCapacityKnown = false;
		}
		
		
		//solarpark.set_p_heatingType( OL_GridConnectionHeatingType.NONE );	
		solarpark.set_p_ownerID( dataSolarfarm.owner_id() );	
		solarpark.set_p_parentNodeElectricID( dataSolarfarm.gridnode_id() );
		
		solarpark.v_isActive = dataSolarfarm.initially_active() ;
		
		//Add EA
		f_addEnergyProduction(solarpark, OL_EnergyAssetType.PHOTOVOLTAIC, "Solar farm" , dataSolarfarm.capacity_electric_kw());
		
		
		if (!existing_actors.contains(solarpark.p_ownerID)){ // check if owner exists already, if not, create new owner.
			owner = energyModel.add_pop_connectionOwners();
			
			owner.set_p_actorID( dataSolarfarm.owner_id());
			owner.set_p_actorType( OL_ActorType.CONNECTIONOWNER );
			owner.set_p_connectionOwnerType( OL_ConnectionOwnerType.SOLARFARM_OP );
			owner.b_dataSharingAgreed = true;
			existing_actors.add(owner.p_actorID);
		}
		else { // Owner exists already: add new GC to existing owner
			owner = findFirst(energyModel.f_getConnectionOwners(), p -> p.p_actorID.equals(dataSolarfarm.owner_id()));
		}
		
		solarpark.set_p_owner( owner );
		
		existing_solarFields.add(solarpark.p_gridConnectionID);		
	}
	else { // solarpark and its owner exist already, only create new gis building which is added to the park
		solarpark = findFirst(energyModel.EnergyProductionSites, p -> p.p_gridConnectionID.equals(dataSolarfarm.gc_id()) );
		owner = solarpark.p_owner;		

	}
	
	if (dataSolarfarm.polygon() != null) {
		//Create GIS object for the solar park
		GIS_Object area = f_createGISObject( dataSolarfarm.gc_name(), dataSolarfarm.latitude(), dataSolarfarm.longitude(), dataSolarfarm.polygon(), OL_GISObjectType.SOLARFARM);
		
		//Add to collections
		area.c_containedGridConnections.add(solarpark);
		solarpark.c_connectedGISObjects.add(area);
		
		//Style building
		area.set_p_defaultFillColor( zero_Interface.v_solarParkColor );
		area.set_p_defaultLineColor( zero_Interface.v_solarParkLineColor );
		area.set_p_defaultLineWidth( zero_Interface.v_energyAssetLineWidth);
		zero_Interface.f_styleAreas(area);
	}
}

/*ALCODEEND*/}

double f_createBatteries()
{/*ALCODESTART::1726584205787*/
for (Battery_data dataBattery : f_getBatteriesInSubScope(c_battery_data)) { // MOET NOG CHECK OF battery ACTOR AL BESTAAT OP, zo ja --> battery koppelen aan elkaar en niet 2 GC en 2 actoren maken.
	
	ConnectionOwner owner = energyModel.add_pop_connectionOwners();
	GCGridBattery gridbattery = energyModel.add_GridBatteries();
	
	//Owner parameters
	owner.set_p_actorID( dataBattery.owner_id() );
	owner.set_p_actorType( OL_ActorType.CONNECTIONOWNER );
	owner.set_p_connectionOwnerType( OL_ConnectionOwnerType.BATTERY_OP );
	owner.b_dataSharingAgreed = true;
	
	//GC parameters
	gridbattery.set_p_gridConnectionID( dataBattery.gc_id () );
	gridbattery.set_p_name( dataBattery.gc_name() );
	gridbattery.set_p_ownerID( dataBattery.owner_id() );
	gridbattery.set_p_owner( owner );	
	gridbattery.v_liveConnectionMetaData.physicalCapacity_kW = dataBattery.connection_capacity_kw();
	
	//Check wether it can be changed using sliders
	gridbattery.p_isSliderGC = dataBattery.isSliderGC();
	
	//Grid Capacity
	gridbattery.v_liveConnectionMetaData.physicalCapacity_kW = dataBattery.connection_capacity_kw();
	if ( dataBattery.connection_capacity_kw() > 0 ) {
		gridbattery.v_liveConnectionMetaData.physicalCapacityKnown = true;
	}
	if ( dataBattery.contracted_delivery_capacity_kw() != null ) {
		gridbattery.v_liveConnectionMetaData.contractedDeliveryCapacity_kW = dataBattery.contracted_delivery_capacity_kw();
		gridbattery.v_liveConnectionMetaData.contractedDeliveryCapacityKnown = true;
	}
	else {
		gridbattery.v_liveConnectionMetaData.contractedDeliveryCapacity_kW = dataBattery.connection_capacity_kw();
		gridbattery.v_liveConnectionMetaData.contractedDeliveryCapacityKnown = false;
	}
	if ( dataBattery.contracted_feed_in_capacity_kw() != null ) {
		gridbattery.v_liveConnectionMetaData.contractedFeedinCapacity_kW = dataBattery.contracted_feed_in_capacity_kw();
		gridbattery.v_liveConnectionMetaData.contractedFeedinCapacityKnown = true;
	}
	else {
		gridbattery.v_liveConnectionMetaData.contractedFeedinCapacity_kW = dataBattery.connection_capacity_kw();
		gridbattery.v_liveConnectionMetaData.contractedFeedinCapacityKnown = false;	
	}
	
	gridbattery.set_p_parentNodeElectricID( dataBattery.gridnode_id() );
	//gridbattery.set_p_heatingType( OL_GridConnectionHeatingType.NONE );	
	
	switch (dataBattery.operation_mode()) {
		case PRICE:
			gridbattery.p_batteryAlgorithm = new J_BatteryManagementPrice(gridbattery);
			break;
		case PEAK_SHAVING_PARENT_NODE:
			J_BatteryManagementPeakShaving batteryAlgorithm = new J_BatteryManagementPeakShaving(gridbattery);
			GridNode gn = findFirst(energyModel.pop_gridNodes, x -> x.p_gridNodeID.equals(gridbattery.p_parentNodeElectricID));
			if (gn == null) {
				throw new RuntimeException("Could not find GridNode with ID: " + gridbattery.p_parentNodeElectricID + " for GCGridBattery");
			}
			batteryAlgorithm.setTarget(gn);
			gridbattery.p_batteryAlgorithm = batteryAlgorithm;
			break;
		case PEAK_SHAVING_COOP:
			// target agent is still null, should be set at the moment of coop creation
			batteryAlgorithm = new J_BatteryManagementPeakShaving(gridbattery);
			batteryAlgorithm.setTargetType( OL_ResultScope.ENERGYCOOP );
			gridbattery.p_batteryAlgorithm = batteryAlgorithm;
			break;
		default:
			throw new RuntimeException("Battery Operation Mode: " + dataBattery.operation_mode() + " is not supported for GCGridBattery.");
	}
	
	//Get initial state
	gridbattery.v_isActive = dataBattery.initially_active();
	
	if (dataBattery.polygon() != null) {
		//Create gis object for the battery
		GIS_Object area =  f_createGISObject( dataBattery.gc_name(), dataBattery.latitude(), dataBattery.longitude(), dataBattery.polygon(), OL_GISObjectType.BATTERY);

		//Add to collections
		gridbattery.c_connectedGISObjects.add(area);
		area.c_containedGridConnections.add(gridbattery);
			
		//Style building
		area.set_p_defaultFillColor( zero_Interface.v_batteryColor );
		area.set_p_defaultLineColor( zero_Interface.v_batteryLineColor );
		area.set_p_defaultLineWidth( zero_Interface.v_energyAssetLineWidth);
		zero_Interface.f_styleAreas(area);
	}
	//Create energy asset for the battery
	f_addStorage(gridbattery, dataBattery.capacity_electric_kw(), dataBattery.storage_capacity_kwh(), OL_EnergyAssetType.STORAGE_ELECTRIC );	
	
}
/*ALCODEEND*/}

double f_createElectrolysers()
{/*ALCODESTART::1726584205789*/
ConnectionOwner owner;
List<String> existing_actors = new ArrayList();


for (Electrolyser_data dataElectrolyser : f_getElectrolysersInSubScope(c_electrolyser_data)) {
	GCEnergyConversion H2Electrolyser = energyModel.add_EnergyConversionSites();

	H2Electrolyser.set_p_gridConnectionID( dataElectrolyser.gc_id() );
	H2Electrolyser.set_p_name( dataElectrolyser.gc_name() );
	//H2Electrolyser.set_p_heatingType( OL_GridConnectionHeatingType.NONE );	
	H2Electrolyser.set_p_ownerID( dataElectrolyser.owner_id() );	
	H2Electrolyser.set_p_parentNodeElectricID( dataElectrolyser.gridnode_id() );
	
	//Grid Capacity
	H2Electrolyser.v_liveConnectionMetaData.physicalCapacity_kW = dataElectrolyser.connection_capacity_kw();
	if ( dataElectrolyser.connection_capacity_kw() > 0 ) {
		H2Electrolyser.v_liveConnectionMetaData.physicalCapacityKnown = true;
	}
	if ( dataElectrolyser.contracted_delivery_capacity_kw() != null ) {
		H2Electrolyser.v_liveConnectionMetaData.contractedDeliveryCapacity_kW = dataElectrolyser.contracted_delivery_capacity_kw();
		H2Electrolyser.v_liveConnectionMetaData.contractedDeliveryCapacityKnown = true;
	}
	else {
		H2Electrolyser.v_liveConnectionMetaData.contractedDeliveryCapacity_kW = dataElectrolyser.connection_capacity_kw();
		H2Electrolyser.v_liveConnectionMetaData.contractedDeliveryCapacityKnown = false;
	}
	if ( dataElectrolyser.contracted_feed_in_capacity_kw() != null ) {
		H2Electrolyser.v_liveConnectionMetaData.contractedFeedinCapacity_kW = dataElectrolyser.contracted_feed_in_capacity_kw();
		H2Electrolyser.v_liveConnectionMetaData.contractedFeedinCapacityKnown = true;
	}
	else {
		H2Electrolyser.v_liveConnectionMetaData.contractedFeedinCapacity_kW = dataElectrolyser.connection_capacity_kw();
		H2Electrolyser.v_liveConnectionMetaData.contractedFeedinCapacityKnown = false;	
	}


	
	H2Electrolyser.v_isActive = dataElectrolyser.initially_active();	
	H2Electrolyser.p_minProductionRatio = dataElectrolyser.min_production_ratio();

	//Electrolyser operation mode
	H2Electrolyser.set_p_electrolyserOperationMode( dataElectrolyser.default_operation_mode());
	
	//Create EA for the electrolyser GC
	J_EAConversionElectrolyser h2ElectrolyserEA = new J_EAConversionElectrolyser(H2Electrolyser, dataElectrolyser.capacity_electric_kw(), dataElectrolyser.conversion_efficiency(), energyModel.p_timeStep_h, OL_ElectrolyserState.STANDBY, dataElectrolyser.load_change_time_s(), dataElectrolyser.start_up_time_shutdown_s(), dataElectrolyser.start_up_time_standby_s(), dataElectrolyser.start_up_time_idle_s());
	
	if (!existing_actors.contains(H2Electrolyser.p_ownerID)){ // check if owner exists already, if not, create new owner.
		owner = energyModel.add_pop_connectionOwners();
		
		owner.set_p_actorID( H2Electrolyser.p_ownerID );
		owner.set_p_actorType( OL_ActorType.CONNECTIONOWNER );
		owner.set_p_connectionOwnerType( OL_ConnectionOwnerType.ELECTROLYSER_OP );
		owner.b_dataSharingAgreed = true;
	}
	else { // Owner exists already: add new GC to existing owner
		owner = findFirst(energyModel.f_getConnectionOwners(), p -> p.p_actorID.equals(dataElectrolyser.owner_id()));
	}
	
	H2Electrolyser.set_p_owner( owner );

	if (dataElectrolyser.polygon() != null) {			
		//Create GIS object for the electrolyser
		GIS_Object area = f_createGISObject( dataElectrolyser.gc_name(), dataElectrolyser.latitude(), dataElectrolyser.longitude(), dataElectrolyser.polygon(), OL_GISObjectType.ELECTROLYSER);

		//Add to collections
		area.c_containedGridConnections.add(H2Electrolyser);
		H2Electrolyser.c_connectedGISObjects.add(area);
	
		
		//Style building
		area.set_p_defaultFillColor( zero_Interface.v_electrolyserColor );
		area.set_p_defaultLineColor( zero_Interface.v_electrolyserLineColor );
		area.set_p_defaultLineWidth( zero_Interface.v_energyAssetLineWidth);
		zero_Interface.f_styleAreas(area);
	}
}
/*ALCODEEND*/}

double f_createWindFarms()
{/*ALCODESTART::1726584205791*/
ConnectionOwner owner;
GCEnergyProduction windfarm;

List<String> existing_actors = new ArrayList();
List<String> existing_windFarms = new ArrayList();

for (Windfarm_data dataWindfarm : f_getWindfarmsInSubScope(c_windfarm_data)) {
	if (!existing_windFarms.contains(dataWindfarm.gc_id())) { // Check if windfarm exists already, if not, create new windfarm GC + turbine
		windfarm = energyModel.add_EnergyProductionSites();

		windfarm.set_p_gridConnectionID( dataWindfarm.gc_id() );
		windfarm.set_p_name( dataWindfarm.gc_name() );

		//Check wether it can be changed using sliders
		windfarm.p_isSliderGC = dataWindfarm.isSliderGC();
	
		//Grid capacity
		windfarm.v_liveConnectionMetaData.physicalCapacity_kW = dataWindfarm.connection_capacity_kw();
		if ( dataWindfarm.connection_capacity_kw() > 0 ) {
			windfarm.v_liveConnectionMetaData.physicalCapacityKnown = true;
		}
		if ( dataWindfarm.contracted_feed_in_capacity_kw() != null) {
			windfarm.v_liveConnectionMetaData.contractedFeedinCapacity_kW = dataWindfarm.contracted_feed_in_capacity_kw();
			windfarm.v_liveConnectionMetaData.contractedFeedinCapacityKnown = true;
		}
		else {
			windfarm.v_liveConnectionMetaData.contractedFeedinCapacity_kW = dataWindfarm.connection_capacity_kw();
			windfarm.v_liveConnectionMetaData.contractedFeedinCapacityKnown = false;
		}
		
		//windfarm.set_p_heatingType( OL_GridConnectionHeatingType.NONE );	
		windfarm.set_p_ownerID( dataWindfarm.owner_id() );	
		windfarm.set_p_parentNodeElectricID( dataWindfarm.gridnode_id() );
		
		//Get initial state
		windfarm.v_isActive = dataWindfarm.initially_active();
		
		//Create EA for the windturbine GC
		f_addEnergyProduction(windfarm, OL_EnergyAssetType.WINDMILL, "Windmill onshore", dataWindfarm.capacity_electric_kw());
		
		if (!existing_actors.contains(windfarm.p_ownerID)){ // check if owner exists already, if not, create new owner.
			owner = energyModel.add_pop_connectionOwners();
			
			owner.set_p_actorID( windfarm.p_ownerID );
			owner.set_p_actorType( OL_ActorType.CONNECTIONOWNER );
			owner.set_p_connectionOwnerType( OL_ConnectionOwnerType.WINDFARM_OP );
			owner.b_dataSharingAgreed = true;
		}
		else { // Owner exists already: add new GC to existing owner
			owner = findFirst(energyModel.f_getConnectionOwners(), p -> p.p_actorID.equals(dataWindfarm.owner_id()));
		}
		
		windfarm.set_p_owner( owner );
		
		existing_windFarms.add(windfarm.p_gridConnectionID);	
	}
	else { // winfarm and its owner exist already, only create new gis building which is added to the farm
		windfarm = findFirst(energyModel.EnergyProductionSites, p -> p.p_gridConnectionID.equals(dataWindfarm.gc_id()) );
		owner = windfarm.p_owner;		

	}
	
	//Create GIS object for the windfarm
	if (dataWindfarm.polygon() != null) {
		GIS_Object area = f_createGISObject( dataWindfarm.gc_name(), dataWindfarm.latitude(), dataWindfarm.longitude(), dataWindfarm.polygon(), OL_GISObjectType.WINDFARM );
		
		//Add to collections
		area.c_containedGridConnections.add(windfarm);
		windfarm.c_connectedGISObjects.add(area);
		
		//Style building
		area.set_p_defaultFillColor( zero_Interface.v_windFarmColor );
		area.set_p_defaultLineColor( zero_Interface.v_windFarmLineColor );
		area.set_p_defaultLineWidth( zero_Interface.v_energyAssetLineWidth);
		zero_Interface.f_styleAreas(area);
	}
}
/*ALCODEEND*/}

double[] f_createGISNodesTokens(GridNode GN)
{/*ALCODESTART::1726584205793*/
double scaling_factor_LVLV = zero_Interface.v_LVLVNodeSize;
double scaling_factor_MVLV = zero_Interface.v_MVLVNodeSize;
double scaling_factor_MVMV = zero_Interface.v_MVMVNodeSize;
double scaling_factor_HVMV = zero_Interface.v_HVMVNodeSize;

int nb_GISCoords;
String node_shape = "TRIANGLE";
double scaling_factor_gridnode = 0;

switch( GN.p_nodeType ) {
		case LVLV:
		nb_GISCoords = 6;
		node_shape = "TRIANGLE";
		scaling_factor_gridnode = scaling_factor_LVLV;		
		break;
		case MVLV:
		nb_GISCoords = 6;
		node_shape = "TRIANGLE";
		scaling_factor_gridnode = scaling_factor_MVLV;
		break;
		case SUBMV:
		nb_GISCoords = 6;
		node_shape = "TRIANGLE";
		scaling_factor_gridnode = scaling_factor_MVLV;	
		break;
		case MVMV:
		nb_GISCoords = 6;
		node_shape = "TRIANGLE";
		scaling_factor_gridnode = scaling_factor_MVMV;	
		break;
		case HVMV:
		nb_GISCoords = 6;
		node_shape = "TRIANGLE";
		scaling_factor_gridnode = scaling_factor_HVMV;	
		break;
		case HT:
		nb_GISCoords = 6;	
		break;
		case MT:
		nb_GISCoords = 6;	
		break;
		case LT:
		nb_GISCoords = 6;	
		break;
		case LT5thgen:
		nb_GISCoords = 6;	
		break;
		default:
		nb_GISCoords = 6;
}

double[] GISCoords = new double[nb_GISCoords];


switch(node_shape){

	case "TRIANGLE":
		//latitudes
		GISCoords[0]=GN.p_latitude;
		GISCoords[2]=GN.p_latitude - scaling_factor_gridnode*0.00001;
		GISCoords[4]=GN.p_latitude - scaling_factor_gridnode*0.00001;
		
		//longitudes
		GISCoords[1]=GN.p_longitude;
		GISCoords[3]=GN.p_longitude + scaling_factor_gridnode*0.00001;
		GISCoords[5]=GN.p_longitude - scaling_factor_gridnode*0.00001;
		break;
	case "DIAMOND":
		//latitudes
		GISCoords[0]=GN.p_latitude;
		GISCoords[2]=GN.p_latitude - scaling_factor_gridnode*0.00001;
		GISCoords[4]=GN.p_latitude;
		GISCoords[6]=GN.p_latitude + scaling_factor_gridnode*0.00001;
		
		//longitudes
		GISCoords[1]=GN.p_longitude;
		GISCoords[3]=GN.p_longitude + scaling_factor_gridnode*0.00001;
		GISCoords[5]=GN.p_longitude + scaling_factor_gridnode*0.00001*2;
		GISCoords[7]=GN.p_longitude + scaling_factor_gridnode*0.00001;
		break;
	/*
	case "CIRCLE":
			// if you want Circle coordinates -->
		//x = r * cos(t) + a
		//y = r * sin(t) + b
		//t is an angle between 0 and 2π (more steps is more circle points, about 10-12 should suffice. --> adjust nb_GISCoords accordingly
		// r is the radius: 0.000009 degrees in latitude is about 1 meter 
		// a and b are latitude and longitude
		break;
	*/
}

return GISCoords;






/*ALCODEEND*/}

double f_createEnergyActors()
{/*ALCODESTART::1726584205795*/
// Create the grid operator
GridOperator GO = energyModel.add_pop_gridOperators();

GO.p_actorID = project_data.grid_operator();
GO.p_actorType = OL_ActorType.OPERATORGRID;
GO.p_hasCongestionPricing = project_data.hasCongestionPricing() != null ? project_data.hasCongestionPricing() : false;


// Create the energy coop
if (project_data.energy_coop() != null && !project_data.energy_coop().equals("None")){
	
	EnergyCoop EC = energyModel.add_pop_energyCoops();
	
	EC.p_actorID = project_data.energy_coop();
	EC.p_actorType = OL_ActorType.COOPENERGY;
	EC.p_gridOperator = GO;
	//EC.p_CoopParent = EC.p_actorID; // WAT BETEKENT COOP PARENT??
}


// Energy supplier
if (project_data.energy_supplier() != null && !project_data.energy_supplier().equals("None")){
	
	EnergySupplier ES = energyModel.add_pop_energySuppliers(); 
	
	ES.p_actorID = project_data.energy_supplier();
	ES.p_actorType = OL_ActorType.SUPPLIERENERGY;
}


/*ALCODEEND*/}

double f_createGenericCompanies()
{/*ALCODESTART::1726584205799*/
//Initialize variables
List<GCUtility> generic_company_GCs = new ArrayList();
HashMap<GridConnection, Double> map_GC_to_installedBuildingPV = new HashMap();

//Get buildings in scope
List<Building_data> buildingDataGenericCompanies = f_getBuildingsInSubScope(c_companyBuilding_data);

//Add generic companies to the legend if in model
if(buildingDataGenericCompanies.size()>0){
	zero_Interface.c_modelActiveDefaultGISBuildings.add(OL_GISBuildingTypes.DEFAULT_COMPANY);
}

//Loop over the remaining buildings in c_CompanyBuilding_data (Survey buildings have been removed from this collection)
for (Building_data genericCompany : buildingDataGenericCompanies) {
	
	GCUtility companyGC = findFirst(generic_company_GCs, GC -> GC.p_gridConnectionID.equals(genericCompany.address_id()));
	
	if(companyGC == null){
		//Create new companyGC
		companyGC = energyModel.add_UtilityConnections();
		
		//Update counter and collections
		v_numberOfCompaniesNoSurvey++;
		generic_company_GCs.add(companyGC);
		map_GC_to_installedBuildingPV.put(companyGC, 0.0);
		
		//Set parameters for the Grid Connection
		companyGC.p_gridConnectionID = genericCompany.address_id();
		
		// Check that is needed until connectioncapacity is no longer in 'Panden' excel
		if (genericCompany.contracted_capacity_kw() == null || genericCompany.contracted_capacity_kw() <= 0) {
			companyGC.v_liveConnectionMetaData.physicalCapacity_kW = avgc_data.p_avgUtilityConnectionCapacity_kW;
			companyGC.v_liveConnectionMetaData.contractedDeliveryCapacity_kW = avgc_data.p_avgUtilityConnectionCapacity_kW;
			companyGC.v_liveConnectionMetaData.contractedFeedinCapacity_kW = avgc_data.p_avgUtilityConnectionCapacity_kW;
		}
		else{
			companyGC.v_liveConnectionMetaData.contractedDeliveryCapacity_kW = genericCompany.contracted_capacity_kw();
			companyGC.v_liveConnectionMetaData.contractedFeedinCapacity_kW = companyGC.v_liveConnectionMetaData.contractedDeliveryCapacity_kW;
			companyGC.v_liveConnectionMetaData.physicalCapacity_kW = companyGC.v_liveConnectionMetaData.contractedDeliveryCapacity_kW;
		}
		
		companyGC.v_liveConnectionMetaData.contractedDeliveryCapacityKnown = false;
		companyGC.v_liveConnectionMetaData.contractedFeedinCapacityKnown = false;

		
		
		//set GC Adress
		companyGC.p_address = new J_Address();
		companyGC.p_address.setStreetName(genericCompany.streetname());
		if (genericCompany.house_number() == null) {
			companyGC.p_address.setHouseNumber(0);
		}
		else {
			companyGC.p_address.setHouseNumber(genericCompany.house_number());
		}
		companyGC.p_address.setHouseLetter(genericCompany.house_letter());
		companyGC.p_address.setHouseAddition(genericCompany.house_addition());
		companyGC.p_address.setPostalcode(genericCompany.postalcode());
		companyGC.p_address.setCity(genericCompany.city());
		
		
		//Set location of GC
	 	companyGC.p_latitude = genericCompany.latitude(); 
	 	companyGC.p_longitude = genericCompany.longitude();
	 	companyGC.setLatLon(companyGC.p_latitude, companyGC.p_longitude);  
	 	
	 	
		//Connect GC to grid node
		companyGC.p_parentNodeElectricID = genericCompany.gridnode_id ();
		
		// Create new actor and assign GC to that
		ConnectionOwner COC = energyModel.add_pop_connectionOwners(); // Create Connection owner company
			
		COC.p_actorID = genericCompany.address_id();
		COC.p_actorType = OL_ActorType.CONNECTIONOWNER;
		COC.p_connectionOwnerType = OL_ConnectionOwnerType.COMPANY;
		COC.p_detailedCompany = false;
		COC.b_dataSharingAgreed = true;
		
		companyGC.p_owner = COC;
		companyGC.p_ownerID = COC.p_actorID;
	}
	
	//Check wheter this building already exists
	GIS_Building existingBuilding = findFirst(energyModel.pop_GIS_Buildings, gisBuilding -> gisBuilding.p_id.equals(genericCompany.building_id()));
	
	if(existingBuilding == null){//Create new GIS building and connect
		GIS_Building b = f_createGISBuilding( genericCompany, companyGC );

		companyGC.p_roofSurfaceArea_m2 += b.p_roofSurfaceArea_m2;
		map_GC_to_installedBuildingPV.put(companyGC, map_GC_to_installedBuildingPV.get(companyGC) + (genericCompany.pv_installed_kwp() != null ? genericCompany.pv_installed_kwp() : 0));
		
		//Style building
		b.p_defaultFillColor = zero_Interface.v_companyBuildingColor;
		b.p_defaultLineColor = zero_Interface.v_companyBuildingLineColor;
		zero_Interface.f_styleAreas(b);
	}
	else{// Connect with existing building
		//Redistribute the PV installed
		List<GridConnection> currentConnectedGCWithBuilding_notDetailed = findAll(existingBuilding.c_containedGridConnections, gc -> !gc.p_owner.p_detailedCompany);
		int currentAmountOfConnectedGCWithBuilding_notDetailed = currentConnectedGCWithBuilding_notDetailed.size();

		double buildingPV = genericCompany.pv_installed_kwp() != null ? genericCompany.pv_installed_kwp() : 0;
		double newPVDistributionForAllAttachedGC_kW = buildingPV/(currentAmountOfConnectedGCWithBuilding_notDetailed+1);
		double deltaPV_earlierConnectedGC_kW = newPVDistributionForAllAttachedGC_kW - (buildingPV/currentAmountOfConnectedGCWithBuilding_notDetailed);
		
		for(GridConnection earlierConnectedGC : currentConnectedGCWithBuilding_notDetailed){
			map_GC_to_installedBuildingPV.put(earlierConnectedGC, map_GC_to_installedBuildingPV.get(earlierConnectedGC) + deltaPV_earlierConnectedGC_kW);	
			if(map_GC_to_installedBuildingPV.get(earlierConnectedGC) < 0){
				new RuntimeException("Negative installed PV for GC: " + earlierConnectedGC.p_gridConnectionID + " after redistribution of PV on the building. This should never be possible!");
			}
		}
		
		map_GC_to_installedBuildingPV.put(companyGC, map_GC_to_installedBuildingPV.get(companyGC) + newPVDistributionForAllAttachedGC_kW);
		
		//Connect to the existing building
		f_connectGCToExistingBuilding(companyGC, existingBuilding, genericCompany);
	}
	
	companyGC.p_floorSurfaceArea_m2 += genericCompany.address_floor_surface_m2();
	v_totalFloorAreaAnonymousCompanies_m2 += genericCompany.address_floor_surface_m2();
	
}

//Amount of generic companies created
traceln("Number of companies created without survey: " + v_numberOfCompaniesNoSurvey);

//Create EA after all buildings and connections have been made -> needed because total surfaces are unkown before that
for (GridConnection GCcompany : generic_company_GCs ) {
	
	//create the energy assets for each GC
	f_iEAGenericCompanies(GCcompany, map_GC_to_installedBuildingPV.get(GCcompany));
}
/*ALCODEEND*/}

GIS_Building f_createGISBuilding(Building_data buildingData,GridConnection parentGC)
{/*ALCODESTART::1726584205801*/
GIS_Building b = energyModel.add_pop_GIS_Buildings();

b.p_id = buildingData.building_id();
b.p_GISObjectType = OL_GISObjectType.BUILDING;
b.p_buildingYear = buildingData.build_year();	
b.p_status = buildingData.status();
b.p_useType = buildingData.purpose();	

// Adres data
b.p_annotation = buildingData.annotation();


//If building is the first building in a cluster (means it has a pancluster_nr), add it to the list of buidling clusters
if(buildingData.pandcluster_nr() != null && buildingData.pandcluster_nr() > 0) { // && 
	energyModel.c_GISBuildingClusters.add(b);
	b.p_pandcluster_nr = buildingData.pandcluster_nr();
}

//Create gisregion
b.gisRegion = zero_Interface.f_createGISObject(f_createGISObjectsTokens(buildingData.polygon(), b.p_GISObjectType));

//Use the first point of the polygon as lat lon
double[] gisregion_points = b.gisRegion.getPoints(); // get all points of the gisArea of the building in the format lat1,lon1,lat2,lon2, etc.

//position and coordinates
b.p_latitude = gisregion_points[0];
b.p_longitude = gisregion_points[1];

//Set latlon
b.setLatLon(b.p_latitude, b.p_longitude);


//Define roof surface area (with Null checks and gisregion area as back up)
b.p_roofSurfaceArea_m2 = buildingData.polygon_area_m2() != null ? buildingData.polygon_area_m2() : b.gisRegion.area();

//Define floor surface area (with Null check, and make it 0 if unkown, else counting errors due to order of loadin of building data)
b.p_floorSurfaceArea_m2 = buildingData.address_floor_surface_m2() != null ? buildingData.address_floor_surface_m2() : 0;

//Add to collections
b.c_containedGridConnections.add(parentGC);
parentGC.c_connectedGISObjects.add(b);


return b;
/*ALCODEEND*/}

double f_addElectricityDemandProfile(GridConnection parentGC,double yearlyElectricityDemand_kWh,Double pvPower_kW,boolean hasQuarterlyData,String profileName)
{/*ALCODESTART::1726584205803*/
if ( hasQuarterlyData == true ) { // Add quarterly electricity data pattern if available 
	
	//Initialize the arrays
	List<Double> yearlyElectricityFeedin_kWh_list = null; 
	List<Double> yearlyElectricityProduction_kWh_list = null;
	
	//Check for PV, and if so: check for additional quarterhourly values
	if(pvPower_kW != null && pvPower_kW > 0){
		//Check for feedin values availability, if available: fill the list, if not, list = null;
		try {
			if(selectFirstValue(Double.class, "SELECT " + profileName + "_feedin FROM comp_elec_consumption LIMIT 1;") != null){
		  		yearlyElectricityFeedin_kWh_list = selectValues(double.class, "SELECT " + profileName + "_feedin FROM comp_elec_consumption;");			
			}
		}
		catch(Exception e) {
			//Do nothing, cause initialized with null;
		}
		
		//Check for bruto production values availability, if available: fill the list, if not, list = null;
		try {
			if(selectFirstValue(Double.class, "SELECT " + profileName + "_production FROM comp_elec_consumption LIMIT 1;") != null){
		  		yearlyElectricityProduction_kWh_list = selectValues(double.class, "SELECT " + profileName + "_production FROM comp_elec_consumption;");
			}
		}
		catch(Exception e) {
			//Do nothing, cause initialized with null;
		}
	}
	
	//Get the delivery values
	List<Double> yearlyElectricityDelivery_kWh_list = selectValues(double.class, "SELECT " + profileName + "_demand FROM comp_elec_consumption;");
	
	
	//Convert lists into arrays
	double[] yearlyElectricityDelivery_kWh_array = (yearlyElectricityDelivery_kWh_list != null) ? yearlyElectricityDelivery_kWh_list.stream().mapToDouble(d -> max(0,d)).toArray() : null;
	double[] yearlyElectricityFeedin_kWh_array = 	(yearlyElectricityFeedin_kWh_list != null) ? yearlyElectricityFeedin_kWh_list.stream().mapToDouble(d -> max(0,d)).toArray() : null;
	double[] yearlyElectricityProduction_kWh_array = (yearlyElectricityProduction_kWh_list != null) ? yearlyElectricityProduction_kWh_list.stream().mapToDouble(d -> max(0,d)).toArray() : null;
	

	//Preprocess and add the profiles
	f_createPreprocessedElectricityProfile_PV(parentGC, yearlyElectricityDelivery_kWh_array, yearlyElectricityFeedin_kWh_array, yearlyElectricityProduction_kWh_array, pvPower_kW, null);

} 

else { // Add regular electricity and consumption profiles
	J_EAConsumption profile = new J_EAConsumption(parentGC, OL_EnergyAssetType.ELECTRICITY_DEMAND, profileName, yearlyElectricityDemand_kWh, OL_EnergyCarriers.ELECTRICITY, energyModel.p_timeStep_h, null);
}
/*ALCODEEND*/}

double f_createGISParcels()
{/*ALCODESTART::1726584205807*/
//Add GISObject type to the legenda
if(c_parcel_data.size()>0){
	zero_Interface.c_modelActiveSpecialGISObjects.add(OL_GISObjectType.PARCEL);
}

for (Parcel_data dataParcel : c_parcel_data) {
		
	GIS_Parcel parcel = energyModel.add_pop_GIS_Parcels();
	
	parcel.set_p_latitude( dataParcel.latitude() );
	parcel.set_p_longitude( dataParcel.longitude() );
	parcel.setLatLon(parcel.p_latitude, parcel.p_longitude);	
	parcel.set_p_id( dataParcel.parcel_id() );
	parcel.set_p_GISObjectType(OL_GISObjectType.PARCEL);
	
	//Building + styling the gisregion and putting it on the map
	GISRegion gisregion = zero_Interface.f_createGISObject(f_createGISObjectsTokens( dataParcel.polygon(), parcel.p_GISObjectType));
	parcel.gisRegion = gisregion;
	
	parcel.set_p_defaultFillColor( zero_Interface.v_parcelColor );
	parcel.set_p_defaultLineColor( zero_Interface.v_parcelLineColor );
	parcel.set_p_defaultLineStyle( LineStyle.LINE_STYLE_DASHED );
	zero_Interface.f_styleAreas(parcel);
}
/*ALCODEEND*/}

double f_addEnergyProduction(GridConnection parentGC,OL_EnergyAssetType asset_type,String asset_name,double installedPower_kW)
{/*ALCODESTART::1726584205809*/
double assetCapacity_kW				= 0;
double timestep_h 					= energyModel.p_timeStep_h;
J_ProfilePointer profilePointer = null;
OL_EnergyCarriers energyCarrier = OL_EnergyCarriers.ELECTRICITY;
switch (asset_type){

case PHOTOVOLTAIC: 
	energyCarrier = OL_EnergyCarriers.ELECTRICITY;
	profilePointer = energyModel.pp_PVProduction35DegSouth_fr;
	assetCapacity_kW = installedPower_kW;
	break;

case WINDMILL:
	energyCarrier = OL_EnergyCarriers.ELECTRICITY;
	profilePointer=energyModel.pp_windProduction_fr;
	assetCapacity_kW = installedPower_kW;
	break;

case PHOTOTHERMAL: //NOT USED YET
	energyCarrier = OL_EnergyCarriers.HEAT;
	profilePointer = energyModel.pp_PVProduction35DegSouth_fr; // Voor nu om te testen! Misschien valt dit wel te gebruiken met bepaalde efficientie factor!
	assetCapacity_kW = installedPower_kW;
	break;
}

J_EAProduction production_asset = new J_EAProduction(parentGC, asset_type, asset_name, energyCarrier, assetCapacity_kW, timestep_h, profilePointer);


/*ALCODEEND*/}

GIS_Object f_createGISObject(String name,double latitude,double longitude,String polygon,OL_GISObjectType GISObjectType)
{/*ALCODESTART::1726584205811*/
GIS_Object area = energyModel.add_pop_GIS_Objects();

area.p_id = name;
area.p_GISObjectType = GISObjectType;

//position and coordinates
area.p_latitude = latitude;
area.p_longitude = longitude;
area.setLatLon(area.p_latitude, area.p_longitude);		

//Create gisregion
area.gisRegion = zero_Interface.f_createGISObject(f_createGISObjectsTokens(polygon, area.p_GISObjectType));

//Add GISObject type to the legenda
zero_Interface.c_modelActiveSpecialGISObjects.add(area.p_GISObjectType);

return area;

/*ALCODEEND*/}

double f_createSurveyCompanies_Zorm()
{/*ALCODESTART::1726584205815*/
//Get the survey data
List<com.zenmo.zummon.companysurvey.Survey> surveys = f_getSurveys();
traceln("Size of survey List: %s", surveys.size());

//Get the building data
try{
	map_buildingData_Vallum = com.zenmo.vallum.PandKt.fetchBagPanden(surveys);
}
catch (Exception e){ //if api of bag is down, leave bag buildings empty and display error message
	zero_Interface.f_setErrorScreen("BAG API is offline, het is mogelijk dat bepaalde panden niet zijn ingeladen!");
}


traceln("Companies that filled in the survey:");
for (var survey : surveys) {
	
	traceln(survey.getCompanyName());
	
	//Create connection owner
	ConnectionOwner survey_owner = energyModel.add_pop_connectionOwners();
	survey_owner.p_actorID = survey.getCompanyName();
	survey_owner.p_actorType = OL_ActorType.CONNECTIONOWNER;
	survey_owner.p_connectionOwnerType = OL_ConnectionOwnerType.COMPANY;
	survey_owner.p_detailedCompany = true;
	survey_owner.b_dataSharingAgreed = survey.getDataSharingAgreed();
		
	for (var address : survey.getAddresses()) {
		
		//Update number of survey companies (locations)
		v_numberOfSurveyCompanies++;
		
        for (var gridConnection: address.getGridConnections()) {

		 	//Check if it has (or will have) a direct connection with the grid (either gas or electric), if not: skip this gc.
		 	boolean hasNaturalGasConnection = (gridConnection.getNaturalGas().getHasConnection() != null)? gridConnection.getNaturalGas().getHasConnection() : false;	 	
		 	boolean hasExpansionRequest = (gridConnection.getElectricity().getGridExpansion().getHasRequestAtGridOperator() != null ) ? gridConnection.getElectricity().getGridExpansion().getHasRequestAtGridOperator() : false;
		 	
		 	if (!gridConnection.getElectricity().getHasConnection() && !hasExpansionRequest && !hasNaturalGasConnection){
				traceln("surveyGC with sequence: " + gridConnection.getSequence() + " is not created, as it has no connection to the grid, future grid connection or current gas connection.");	
			 	continue;
		 	}
		 	
		 	//Create GC
		 	GCUtility companyGC = energyModel.add_UtilityConnections();		  
		 	
			//Set parameters for the Grid Connection
			companyGC.p_ownerID = survey.getCompanyName();
		 	companyGC.p_gridConnectionID = gridConnection.getSequence().toString() ;

		 	//Find actor and connect GC 
			companyGC.p_owner = survey_owner;	
			
			//Adress data
			companyGC.p_address = new J_Address();
			companyGC.p_address.setStreetName(address.getStreet().substring(0,1).toUpperCase() + address.getStreet().substring(1).toLowerCase());
		 	companyGC.p_address.setHouseNumber(address.getHouseNumber());
		 	companyGC.p_address.setHouseLetter(address.getHouseLetter().equals("") ? null : address.getHouseLetter());
		 	companyGC.p_address.setHouseAddition(address.getHouseNumberSuffix().equals("") ? null : address.getHouseNumberSuffix());
		 	companyGC.p_address.setPostalcode(address.getPostalCode().equals("") ? null : address.getPostalCode().toUpperCase().replaceAll("\\s",""));
		 	companyGC.p_address.setCity(address.getCity().substring(0,1).toUpperCase() + address.getCity().substring(1).toLowerCase());

			//Get attached building info
			List<Building_data> buildings = f_getSurveyGCBuildingData(companyGC, gridConnection);
			
			//Total new additional floor/roof surface area
		 	double totalNewFloorSurfaceAreaGC_m2 = 0;
		 	double totalNewRoofSurfaceAreaGC_m2 = 0;
			
			//Create the GIS buildings
			for (Building_data buildingData : buildings) {
				GIS_Building gisBuilding = findFirst(energyModel.pop_GIS_Buildings, b -> b.p_id.equals(buildingData.building_id())); // Check if building already exists in engine
				if (gisBuilding != null) {
				    // Connect GC to existing building in engine
				    f_connectGCToExistingBuilding(companyGC, gisBuilding, buildingData);
				}
				else{ 
					gisBuilding = f_createGISBuilding( buildingData, companyGC);				
				}
				
				//Set name of building
				if(gisBuilding.p_annotation == null){
					gisBuilding.p_annotation = companyGC.p_ownerID;
				}
				
				//Accumulate surface areas
				totalNewFloorSurfaceAreaGC_m2 += buildingData.address_floor_surface_m2();
				totalNewRoofSurfaceAreaGC_m2 += gisBuilding.p_roofSurfaceArea_m2;
				
				//Set trafo ID
				companyGC.p_parentNodeElectricID = buildingData.gridnode_id();
				
				//Style building
				gisBuilding.p_defaultFillColor = zero_Interface.v_detailedCompanyBuildingColor;
				gisBuilding.p_defaultLineColor = zero_Interface.v_detailedCompanyBuildingLineColor;
				zero_Interface.f_styleAreas(gisBuilding);
				
			}      
			
			//Add (combined) building data to GC (latitude and longitude + area)
			companyGC.p_floorSurfaceArea_m2 += totalNewFloorSurfaceAreaGC_m2;
			companyGC.p_roofSurfaceArea_m2 += totalNewRoofSurfaceAreaGC_m2;
			

			if(!companyGC.c_connectedGISObjects.isEmpty()){
				companyGC.p_longitude = companyGC.c_connectedGISObjects.get(0).p_longitude; // Get longitude of first building (only used to get nearest trafo)
				companyGC.p_latitude = companyGC.c_connectedGISObjects.get(0).p_latitude; // Get latitude of first building (only used to get nearest trafo)
				
				if(buildings.isEmpty()){ //GC will not have gotten a gridnode assigned,
					for (var PID : gridConnection.getPandIds() ) {
						Building_data surveyBuildingData = findFirst(c_surveyCompanyBuilding_data, b -> b.building_id().equals(PID.getValue()));
						if(surveyBuildingData != null){
							companyGC.p_parentNodeElectricID = surveyBuildingData.gridnode_id();
							break;
						}
					}
				}
			}
			else{
				traceln("Gridconnection %s with owner %s has no buildings!!!", companyGC.p_gridConnectionID, companyGC.p_ownerID);
			}
			
			//Set lat lon
			companyGC.setLatLon(companyGC.p_latitude, companyGC.p_longitude); 
			
			//Energy asset initialization
			f_iEASurveyCompanies_Zorm(companyGC, gridConnection); 
        }
    }
}

//If survey companies are present, add to the ui legend
if(v_numberOfSurveyCompanies>0){
	//Add to the legend
	zero_Interface.c_modelActiveDefaultGISBuildings.add(OL_GISBuildingTypes.DETAILED_COMPANY);

	//Pass the number of survey companies to interface for the dynamic legend
	zero_Interface.v_numberOfSurveyCompanies = v_numberOfSurveyCompanies;
}
/*ALCODEEND*/}

List<com.zenmo.zummon.companysurvey.Survey> f_getSurveys()
{/*ALCODESTART::1726584205819*/
//Connect with API to database
Vallum vallum = new Vallum(user.PROJECT_CLIENT_ID(), user.PROJECT_CLIENT_SECRET());


List<com.zenmo.zummon.companysurvey.Survey> surveys = new ArrayList();


String[] zorm_project_names;
if(project_data.zorm_project_names() != null){
	zorm_project_names = project_data.zorm_project_names();
}
else{
	zorm_project_names = new String[]{project_data.project_name()};
}

surveys = vallum.getEnabledSurveysByProjectNames(zorm_project_names);

//Clear user data
user = null;

return surveys;
/*ALCODEEND*/}

double f_createActors()
{/*ALCODESTART::1726584205821*/
//Create specific actors like Grid operator, energy supplier, energy coop
f_createEnergyActors();
/*ALCODEEND*/}

OL_GridConnectionHeatingType f_getHeatingTypeSurvey(GridConnection engineGC,com.zenmo.zummon.companysurvey.GridConnection surveyGC)
{/*ALCODESTART::1726584205825*/
// Storing the survey heating types
c_surveyHeatingTypes.put(engineGC, surveyGC.getHeat().getHeatingTypes());
// This function determines a heatingType for the GC, which will be passed on to the function that determines the heating management
OL_GridConnectionHeatingType heatingType = null;

if (surveyGC.getHeat().getHeatingTypes().size() > 1) {
	// We currently only recognize a couple of combinations that we assume are meant as hybrid heatpumps
	if (surveyGC.getHeat().getHeatingTypes().size() == 2) {
		if (surveyGC.getHeat().getHeatingTypes().contains(HeatingType.GAS_BOILER)) {
			if (surveyGC.getHeat().getHeatingTypes().contains(HeatingType.HYBRID_HEATPUMP) || surveyGC.getHeat().getHeatingTypes().contains(HeatingType.ELECTRIC_HEATPUMP)) {
				heatingType = OL_GridConnectionHeatingType.HYBRID_HEATPUMP;
			}
		}
	}
}
else if (surveyGC.getHeat().getHeatingTypes().size() == 1) {
	// We translate the survey enum to the OL_GridConnectionHeatingType
	switch(surveyGC.getHeat().getHeatingTypes().get(0)) {
		case GAS_BOILER:
			heatingType = OL_GridConnectionHeatingType.GAS_BURNER;
			break;
		case HYBRID_HEATPUMP:
			heatingType = OL_GridConnectionHeatingType.HYBRID_HEATPUMP;
			break;
		case ELECTRIC_HEATPUMP:
			heatingType = OL_GridConnectionHeatingType.ELECTRIC_HEATPUMP;
			break;
		case DISTRICT_HEATING:
			heatingType = OL_GridConnectionHeatingType.DISTRICTHEAT;
			break;
		case COMBINED_HEAT_AND_POWER:
			heatingType = OL_GridConnectionHeatingType.GAS_CHP;
			break;	
		case OTHER:
			heatingType = OL_GridConnectionHeatingType.CUSTOM;
			break;
		default:
			throw new RuntimeException("Incorrect heating: " + surveyGC.getHeat().getHeatingTypes().get(0) + " detected for '" + engineGC.p_ownerID + "'");
	}
}
else if (surveyGC.getHeat().getHeatingTypes().size() == 0) {
	// There is no heating type in the survey, but there is gas consumption (either yearly total or hourly values)
	if ( (surveyGC.getNaturalGas().getAnnualDelivery_m3() != null && surveyGC.getNaturalGas().getAnnualDelivery_m3() > 0)
	 || ( surveyGC.getNaturalGas().getHourlyDelivery_m3() != null && surveyGC.getNaturalGas().getHourlyDelivery_m3().hasNumberOfValuesForOneYear() ) ) {
		// We assume that all gas consumption is to heat the building(s)
		heatingType = OL_GridConnectionHeatingType.GAS_BURNER;
	}
	else {
		heatingType = OL_GridConnectionHeatingType.NONE;
	}
}
if (heatingType == null) {
	heatingType = OL_GridConnectionHeatingType.CUSTOM;
}

return heatingType;
/*ALCODEEND*/}

double f_addElectricVehicle(GridConnection parentGC,OL_EnergyAssetType vehicle_type,boolean isDefaultVehicle,double annualTravelDistance_km,double maxChargingPower_kW)
{/*ALCODESTART::1726584205827*/
double storageCapacity_kWh 		= 0;
double energyConsumption_kWhpkm = 0;
double capacityElectricity_kW 	= 0;
double stateOfCharge_fr  		= 1; // Initial state of charge
double timestep_h				= energyModel.p_timeStep_h;
double vehicleScaling 			= 1.0;

switch(vehicle_type){
	
	/*
	case ELECTRIC_VEHICLE_COMUTERS: // ??? Hoe laad je andere laadprofielen in.?? Deze moet ander laadprofiel dan de Standaard Electric_vehicle 
		capacityElectricity_kW	= avgc_data.p_avgEVMaxChargePowerCar_kW;
		storageCapacity_kWh		= avgc_data.p_avgEVStorageCar_kWh;
		energyConsumption_kWhpkm = avgc_data.p_avgEVEnergyConsumptionCar_kWhpkm;
	break;
	*/
	
	case ELECTRIC_VEHICLE:
		capacityElectricity_kW	= avgc_data.p_avgEVMaxChargePowerCar_kW;
		storageCapacity_kWh		= avgc_data.p_avgEVStorageCar_kWh;
		energyConsumption_kWhpkm = avgc_data.p_avgEVEnergyConsumptionCar_kWhpkm;
	break;
	
	case ELECTRIC_VAN:
		capacityElectricity_kW 	= avgc_data.p_avgEVMaxChargePowerVan_kW;
		storageCapacity_kWh		= avgc_data.p_avgEVStorageVan_kWh;
		energyConsumption_kWhpkm = avgc_data.p_avgEVEnergyConsumptionVan_kWhpkm;
	break;
	
	case ELECTRIC_TRUCK:
		capacityElectricity_kW	= avgc_data.p_avgEVMaxChargePowerTruck_kW;
		storageCapacity_kWh		= avgc_data.p_avgEVStorageTruck_kWh;
		energyConsumption_kWhpkm = avgc_data.p_avgEVEnergyConsumptionTruck_kWhpkm;
	break;
	
}

if (!isDefaultVehicle && maxChargingPower_kW != 0){
	capacityElectricity_kW	= maxChargingPower_kW;
}


//Create the EV vehicle energy asset with the set parameters + links
J_EAEV electricVehicle = new J_EAEV(parentGC, capacityElectricity_kW, storageCapacity_kWh, stateOfCharge_fr, timestep_h, energyConsumption_kWhpkm, vehicleScaling, vehicle_type, null);	

if (!isDefaultVehicle && annualTravelDistance_km > 1000){
		electricVehicle.tripTracker.setAnnualDistance_km(annualTravelDistance_km);
}
else if (vehicle_type == OL_EnergyAssetType.ELECTRIC_VAN){
		electricVehicle.tripTracker.setAnnualDistance_km(avgc_data.p_avgAnnualTravelDistanceVan_km);
}

/*ALCODEEND*/}

double f_addDieselVehicle(GridConnection parentGC,OL_EnergyAssetType vehicle_type,Boolean isDefaultVehicle,double annualTravelDistance_km)
{/*ALCODESTART::1726584205829*/
double energyConsumption_kWhpkm = 0;
double vehicleScaling = 1.0;

//Diesel car
switch (vehicle_type){
	
	case DIESEL_VEHICLE:
		energyConsumption_kWhpkm = roundToDecimal(uniform(0.7, 1.3),2) * avgc_data.p_avgDieselConsumptionCar_kWhpkm;
	break;
	
	case DIESEL_VAN:
		energyConsumption_kWhpkm = avgc_data.p_avgDieselConsumptionVan_kWhpkm;
		v_nbCreatedVans++;
	break;
	
	case DIESEL_TRUCK:
		energyConsumption_kWhpkm = avgc_data.p_avgDieselConsumptionTruck_kWhpkm;
	break;
}

//Create EA
J_EADieselVehicle dieselVehicle = new J_EADieselVehicle(parentGC, energyConsumption_kWhpkm, energyModel.p_timeStep_h, vehicleScaling, vehicle_type, null);

//Set annual travel distance
if (!isDefaultVehicle && annualTravelDistance_km > 1000){
		dieselVehicle.tripTracker.setAnnualDistance_km(annualTravelDistance_km);
}
else if (vehicle_type == OL_EnergyAssetType.DIESEL_VAN){
		dieselVehicle.tripTracker.setAnnualDistance_km(avgc_data.p_avgAnnualTravelDistanceVan_km);
}
/*ALCODEEND*/}

double f_addStorage(GridConnection parentGC,double storagePower_kw,double storageCapacity_kWh,OL_EnergyAssetType storageType)
{/*ALCODESTART::1726584205831*/
J_EAStorage storage = null;

switch (storageType){

	case STORAGE_ELECTRIC:
	
		storage = new J_EAStorageElectric(parentGC, storagePower_kw, storageCapacity_kWh, 0, energyModel.p_timeStep_h);
		//traceln("Battery with StorageCapacity_kWh: %s", storageCapacity_kWh);
	break;
	
	case STORAGE_HEAT:
		double lossFactor_WpK = 0; // For now no losses, waiting for new setup heating assets before this can be implemented correctly (its JEA_Building depedent)
		double minTemperature_degC = avgc_data.p_avgMinHeatBufferTemperature_degC;
		double maxTemperature_degC = avgc_data.p_avgMaxHeatBufferTemperature_degC;
		double initialTemperature_degC = (minTemperature_degC + maxTemperature_degC) / 2;
		double setTemperature_degC = initialTemperature_degC;
		//double storageCapacity_kg = (storageCapacity_kWh*3.6e6)/(avgc_data.p_waterHeatCapacity_JpkgK * (maxTemperature_degC - minTemperature_degC));
		//double heatCapacity_JpK = avgc_data.p_waterHeatCapacity_JpkgK * storageCapacity_kg;
		//in short ->
		double heatCapacity_JpK = storageCapacity_kWh*3.6e6 / (maxTemperature_degC - minTemperature_degC); 
		new J_EAStorageHeat(parentGC, storageType, storagePower_kw, lossFactor_WpK, energyModel.p_timeStep_h, initialTemperature_degC, minTemperature_degC, maxTemperature_degC, setTemperature_degC, heatCapacity_JpK, OL_AmbientTempType.AMBIENT_AIR );
			
	break;
	
	case STORAGE_GAS:
	
	break;
	
	default:
	
	return;
}

/*ALCODEEND*/}

double f_iEAGenericCompanies(GridConnection companyGC,Double pv_installed_kwp)
{/*ALCODESTART::1726584205833*/
//Create current & future scenario parameter list
J_scenario_Current current_scenario_list = new J_scenario_Current();
zero_Interface.c_scenarioMap_Current.put(companyGC, current_scenario_list);

J_scenario_Future future_scenario_list = new J_scenario_Future();
zero_Interface.c_scenarioMap_Future.put(companyGC, future_scenario_list);

//Set parent
current_scenario_list.setParentAgent(companyGC);
future_scenario_list.setParentAgent(companyGC);

//Add current grid capacity to current (and future, feedin, physical, as no data on plans so assumption it is/stays the same) scenario list
current_scenario_list.setCurrentContractDeliveryCapacity_kW(companyGC.v_liveConnectionMetaData.contractedDeliveryCapacity_kW);
future_scenario_list.setRequestedContractDeliveryCapacity_kW(companyGC.v_liveConnectionMetaData.contractedDeliveryCapacity_kW);
current_scenario_list.setCurrentContractFeedinCapacity_kW(companyGC.v_liveConnectionMetaData.contractedFeedinCapacity_kW);
future_scenario_list.setRequestedContractFeedinCapacity_kW(companyGC.v_liveConnectionMetaData.contractedFeedinCapacity_kW);
current_scenario_list.setCurrentPhysicalConnectionCapacity_kW(companyGC.v_liveConnectionMetaData.physicalCapacity_kW);
future_scenario_list.setRequestedPhysicalConnectionCapacity_kW(companyGC.v_liveConnectionMetaData.physicalCapacity_kW);

//Basic heating and electricity demand profiles
if (companyGC.p_floorSurfaceArea_m2 > 0){
	
	if(v_remainingElectricityDelivery_kWh > 0){
		//Buidling Base electricity load
		double Remaining_electricity_demand_kWh_p_m2_yr = v_remainingElectricityDelivery_kWh / v_totalFloorAreaAnonymousCompanies_m2;
		double yearlyElectricityDemand_kWh = Remaining_electricity_demand_kWh_p_m2_yr * companyGC.p_floorSurfaceArea_m2;
		
		//Add base load profile
		f_addElectricityDemandProfile(companyGC, yearlyElectricityDemand_kWh, null, false, "default_office_electricity_demand_fr");
	}
	
	if(v_remainingGasConsumption_m3 > 0){
		//Building Gas demand profile (purely heating)
		double Remaining_gas_demand_m3_p_m2_yr = v_remainingGasConsumption_m3/v_totalFloorAreaAnonymousCompanies_m2;
		double yearlyGasDemand_m3 = Remaining_gas_demand_m3_p_m2_yr*companyGC.p_floorSurfaceArea_m2;
		double ratioGasUsedForHeating = 1;
		
		//Add heat demand profile
		OL_GridConnectionHeatingType heatingType = avgc_data.p_avgCompanyHeatingMethod;
		double maxHeatOutputPower_kW = f_createHeatProfileFromAnnualGasTotal(companyGC, heatingType, yearlyGasDemand_m3, ratioGasUsedForHeating);
		f_addHeatAsset(companyGC, heatingType, maxHeatOutputPower_kW);
		f_addHeatManagement(companyGC, heatingType, false);
		
		//Set current scenario heating type
		current_scenario_list.setCurrentHeatingType(heatingType);
		future_scenario_list.setPlannedHeatingType(heatingType);		
	}
}


//Production asset (PV) ??????????????????????????????????????????? willen we die toevoegen aan generieke bedrijven?
if(pv_installed_kwp != null && pv_installed_kwp > 0){
	f_addEnergyProduction(companyGC, OL_EnergyAssetType.PHOTOVOLTAIC, "Rooftop Solar", pv_installed_kwp);
}


//Battery with capacity 0 (initialize the slider)
f_addStorage(companyGC, 0, 0, OL_EnergyAssetType.STORAGE_ELECTRIC);

//add to scenario: current & future
current_scenario_list.setCurrentBatteryPower_kW(0f);
current_scenario_list.setCurrentBatteryCapacity_kWh(0f);
future_scenario_list.setPlannedBatteryPower_kW(0f);
future_scenario_list.setPlannedBatteryCapacity_kWh(0f);

	
//Transport (total remaining cars, vans and trucks (total as defined in project selection - survey company usage)

//Vans
if(v_remainingNumberOfCars > 0){
	int nbCars = 0;
	for (int k = 0; k< ceil((double)v_remainingNumberOfCars/(double)v_numberOfCompaniesNoSurvey); k++){
		f_addDieselVehicle(companyGC, OL_EnergyAssetType.DIESEL_VEHICLE, true, 0);
		v_remainingNumberOfCars--;
		nbCars++;
	}
	
	//Set current scenario cars
	current_scenario_list.setCurrentDieselCars(nbCars);
	//Set planned scenario cars
	future_scenario_list.setPlannedEVCars(0);
}

//Vans
if(v_remainingNumberOfVans > 0){
	int nbVans = 0;
	for (int k = 0; k< ceil((double)v_remainingNumberOfVans/(double)v_numberOfCompaniesNoSurvey); k++){
		f_addDieselVehicle(companyGC, OL_EnergyAssetType.DIESEL_VAN, true, 0);
		v_remainingNumberOfVans--;
		nbVans++;
	}
	
	//Set current scenario vans
	current_scenario_list.setCurrentDieselVans(nbVans);
	//Set planned scenario vans
	future_scenario_list.setPlannedEVVans(0);
}

//Trucks
if (v_remainingNumberOfTrucks > 0){
	int nbTrucks=0;
	for (int k = 0; k< ceil((double)v_remainingNumberOfTrucks/(double)v_numberOfCompaniesNoSurvey); k++){
		f_addDieselVehicle(companyGC, OL_EnergyAssetType.DIESEL_TRUCK, true, 0);
		v_remainingNumberOfTrucks--;
		nbTrucks++;
	}
	
	//Set current scenario trucks
	current_scenario_list.setCurrentDieselTrucks(nbTrucks);
	//Set planned scenario trucks
	future_scenario_list.setPlannedEVTrucks(0);
}
/*ALCODEEND*/}

double f_createRemainingBuildings()
{/*ALCODESTART::1726584205835*/
//If remaining buildings in model, add to legend
if(c_remainingBuilding_data.size()>0){
	zero_Interface.c_modelActiveDefaultGISBuildings.add(OL_GISBuildingTypes.REMAINING);
}

for (Building_data remainingBuilding_data : c_remainingBuilding_data) {
	
	GIS_Building building = energyModel.add_pop_GIS_Buildings();
	building.p_id = remainingBuilding_data.building_id();
	building.p_longitude = remainingBuilding_data.longitude();
	building.p_latitude = remainingBuilding_data.latitude();
	building.setLatLon(building.p_latitude, building.p_longitude);		
	building.p_GISObjectType = OL_GISObjectType.REMAINING_BUILDING;
	
	//Building + styling the gisregion and putting it on the map		
	building.gisRegion = zero_Interface.f_createGISObject(f_createGISObjectsTokens(remainingBuilding_data.polygon(), building.p_GISObjectType));
	
	building.p_defaultFillColor = zero_Interface.v_restBuildingColor;
	building.p_defaultLineColor = zero_Interface.v_restBuildingLineColor;
	zero_Interface.f_styleAreas(building);
}
/*ALCODEEND*/}

double f_addTransportHydrogen(GridConnection parentGC,OL_EnergyAssetType vehicle_type,boolean isDefaultVehicle,double annualTravelDistance_km)
{/*ALCODESTART::1726584205837*/
double energyConsumption_kWhpkm = 0;
double vehicleScaling = 1.0;

//Hydrogen car
switch (vehicle_type){

	case HYDROGEN_VEHICLE:
		energyConsumption_kWhpkm = avgc_data.p_avgHydrogenConsumptionCar_kWhpkm;
	break;
	
	case HYDROGEN_VAN:
		energyConsumption_kWhpkm = avgc_data.p_avgHydrogenConsumptionVan_kWhpkm;
	break;
	
	case HYDROGEN_TRUCK:
		energyConsumption_kWhpkm = avgc_data.p_avgHydrogenConsumptionTruck_kWhpkm;
	break;
}

//Create EA
J_EAHydrogenVehicle hydrogenVehicle = new J_EAHydrogenVehicle(parentGC, energyConsumption_kWhpkm, energyModel.p_timeStep_h, vehicleScaling, vehicle_type, null);

//Set annual travel distance
if (!isDefaultVehicle && annualTravelDistance_km > 1000){
		hydrogenVehicle.tripTracker.setAnnualDistance_km(annualTravelDistance_km);
}
else if (vehicle_type == OL_EnergyAssetType.HYDROGEN_VAN){
		hydrogenVehicle.tripTracker.setAnnualDistance_km(avgc_data.p_avgAnnualTravelDistanceVan_km);
}
/*ALCODEEND*/}

double f_addChargingDemandProfile(GCPublicCharger GC,String profileName)
{/*ALCODESTART::1726584205845*/
J_EAProfile profile = new J_EAProfile(GC, OL_EnergyCarriers.ELECTRICITY, null, OL_ProfileAssetType.CHARGING, energyModel.p_timeStep_h);		
profile.energyAssetName = "charging profile";
List<Double> quarterlyEnergyDemand_kWh = selectValues(double.class, "SELECT " + profileName + " FROM charging_profiles;");			
profile.a_energyProfile_kWh = quarterlyEnergyDemand_kWh.stream().mapToDouble(d -> max(0,d)).map( d -> d / 4).toArray();
/*ALCODEEND*/}

GISRegion f_createGISRegionChargingStation(double lat,double lon)
{/*ALCODESTART::1726584205847*/
//create shape Coords
int nb_points = 6;
double[] GISCoords = new double[nb_points * 2];

for (int i=0; i < nb_points ; i++){
	double size = 0.00004;
	GISCoords[i * 2] = size * cos( i * ( 2 * Math.PI ) / nb_points) + lat;
	GISCoords[i * 2 + 1] = 1.64 * size * sin( i * ( 2 * Math.PI ) / nb_points) + lon;
}

//Create the region
GISRegion gisregion = zero_Interface.f_createGISObject( GISCoords );

return gisregion;

/*ALCODEEND*/}

double f_createChargingStations()
{/*ALCODESTART::1726584205849*/
//Initialize parameters
int laadpaal_nr = 1;
int laadstation_nr = 1;

//Loop over charging stations
for (Chargingstation_data dataChargingStation : f_getChargingstationsInSubScope(c_chargingstation_data)){

	GCPublicCharger chargingStation = energyModel.add_PublicChargers();

	chargingStation.set_p_gridConnectionID( dataChargingStation.gc_id());
	chargingStation.set_p_name( dataChargingStation.gc_name() );
	
	//Electric Capacity
	if (dataChargingStation.connection_capacity_kw() != null) {
		// Assume the connection capacity is both physical and contracted.
		chargingStation.v_liveConnectionMetaData.physicalCapacity_kW = dataChargingStation.connection_capacity_kw();
		chargingStation.v_liveConnectionMetaData.contractedDeliveryCapacity_kW = dataChargingStation.connection_capacity_kw();
		chargingStation.v_liveConnectionMetaData.physicalCapacityKnown = true;
		chargingStation.v_liveConnectionMetaData.contractedDeliveryCapacityKnown = true;
	}
	
	//chargingStation.set_p_heatingType( OL_GridConnectionHeatingType.NONE );
	
	//Set parent node
	chargingStation.p_parentNodeElectricID = dataChargingStation.gridnode_id();
	
	//Is active at start?
	chargingStation.v_isActive = dataChargingStation.initially_active();

	//Set charging attitude: MAX_POWER should always be the starting case for charge stations to prevent more charging than possible
	chargingStation.set_p_chargingAttitudeVehicles(OL_ChargingAttitude.MAX_SPREAD);
			
	//Create and connect owner
	ConnectionOwner owner = energyModel.add_pop_connectionOwners();

	chargingStation.set_p_ownerID( dataChargingStation.owner_id());				
	owner.set_p_actorID( chargingStation.p_ownerID );
	owner.set_p_actorType( OL_ActorType.CONNECTIONOWNER );
	owner.set_p_connectionOwnerType( OL_ConnectionOwnerType.CHARGEPOINT_OP );
	owner.b_dataSharingAgreed = true;
	
	chargingStation.set_p_owner( owner );
	
	
	//Check if centre or single
	chargingStation.p_isChargingCentre = dataChargingStation.is_charging_centre();
	if (chargingStation.p_isChargingCentre) {
		
		if (chargingStation.p_ownerID == null){
			chargingStation.p_ownerID = "Publiek laadstation " + laadstation_nr;
			laadstation_nr++;
		}

		chargingStation.set_p_nbOfChargers( dataChargingStation.number_of_chargers() );
		chargingStation.set_p_maxChargingPower_kW( dataChargingStation.power_per_charger_kw() );
		
		//If check on connection capacity to prevent more charging than possible
		if(chargingStation.v_liveConnectionMetaData.contractedDeliveryCapacity_kW > chargingStation.p_nbOfChargers*chargingStation.p_maxChargingPower_kW){
			chargingStation.v_liveConnectionMetaData.contractedDeliveryCapacity_kW = chargingStation.p_nbOfChargers*chargingStation.p_maxChargingPower_kW;
		}
		
		//Set vehicle type
		chargingStation.p_chargingVehicleType = dataChargingStation.vehicle_type();		
		
		//Create vehicles that charge at the charging centre
		if(chargingStation.p_chargingVehicleType == OL_EnergyAssetType.CHARGER){
			List<J_ChargingSession> chargerProfile = f_getChargerProfile();
			boolean V1GCapable = randomTrue(avgc_data.p_v1gProbability);
			boolean V2GCapable = randomTrue(avgc_data.p_v2gProbability);
			new J_EACharger(chargingStation, chargingStation.p_maxChargingPower_kW, energyModel.p_timeStep_h, chargerProfile, V1GCapable, V2GCapable);
		}
		else{
			for(int k = 0; k < chargingStation.p_nbOfChargers*avgc_data.p_avgVehiclesPerChargePoint; k++ ){
				f_addElectricVehicle(chargingStation, chargingStation.p_chargingVehicleType, true, 0, chargingStation.p_maxChargingPower_kW);
			}
		}
		
		
		if (dataChargingStation.polygon() != null) {
			//Create EA GIS object (building) for the charging centre
			GIS_Object area = f_createGISObject( dataChargingStation.gc_name(), dataChargingStation.latitude(), dataChargingStation.longitude(), dataChargingStation.polygon(), OL_GISObjectType.CHARGER );
			
			//Set gis object type
			area.p_GISObjectType = OL_GISObjectType.CHARGER;
			
			//Add to collections
			area.c_containedGridConnections.add(chargingStation);
			chargingStation.c_connectedGISObjects.add(area);
			
			//Style building
			area.set_p_defaultFillColor( zero_Interface.v_chargingStationColor );
			area.set_p_defaultLineColor( zero_Interface.v_chargingStationLineColor );
			area.set_p_defaultLineWidth( zero_Interface.v_energyAssetLineWidth);
			zero_Interface.f_styleAreas(area);
		}
		else{
			traceln("No gisobject created for charge centre: " + chargingStation.p_name);
		}
	}
	else {

		if (chargingStation.p_ownerID == null){
			chargingStation.p_ownerID = "Publieke laadpaal " + laadpaal_nr;
			laadpaal_nr++;
		}
		
		//Set charging power
		chargingStation.set_p_maxChargingPower_kW( dataChargingStation.power_per_charger_kw() );
		
		//Set vehicle type
		chargingStation.p_chargingVehicleType = dataChargingStation.vehicle_type();		
		
		//Create vehicles that charge at the charging station
		if(chargingStation.p_chargingVehicleType == OL_EnergyAssetType.CHARGER){
			List<J_ChargingSession> chargerProfile = f_getChargerProfile();
			boolean V1GCapable = randomTrue(avgc_data.p_v1gProbability);
			boolean V2GCapable = randomTrue(avgc_data.p_v2gProbability);
			new J_EACharger(chargingStation, chargingStation.p_maxChargingPower_kW, energyModel.p_timeStep_h, chargerProfile, V1GCapable, V2GCapable);
		}
		else{
			for(int k = 0; k < avgc_data.p_avgVehiclesPerChargePoint; k++ ){
				f_addElectricVehicle(chargingStation, chargingStation.p_chargingVehicleType, true, 0, chargingStation.p_maxChargingPower_kW);
			}
		}
		
		
		//Create GIS object for the chargingStation			
		GIS_Object area = energyModel.add_pop_GIS_Objects();

		//position and coordinates
		area.p_latitude = dataChargingStation.latitude();
		area.p_longitude = dataChargingStation.longitude();
		area.setLatLon(area.p_latitude, area.p_longitude);		
		
		//Create gisregion
		area.gisRegion = f_createGISRegionChargingStation( area.p_latitude, area.p_longitude );	
		
		//Set area type
		area.p_GISObjectType = OL_GISObjectType.CHARGER;
	
		chargingStation.c_connectedGISObjects.add(area);
		area.c_containedGridConnections.add(chargingStation);
		if(chargingStation.v_isActive){
			area.set_p_defaultFillColor( zero_Interface.v_chargingStationColor );
			area.set_p_defaultLineColor( zero_Interface.v_chargingStationLineColor );
		}
		else{
			area.set_p_defaultFillColor( zero_Interface.v_newChargingStationColor );
			area.set_p_defaultLineColor( zero_Interface.v_newChargingStationLineColor );
		}
		zero_Interface.f_styleAreas(area);
	
		zero_Interface.c_modelActiveSpecialGISObjects.add(area.p_GISObjectType);
	}	
}


/*ALCODEEND*/}

double f_createInterface()
{/*ALCODESTART::1726584205853*/
//OVERRIDE THE zero_Interface parameter here
//zero_Interface = YOUR INTERFACE;

//Set parameters/pointers in the interface
zero_Interface.energyModel = energyModel;
zero_Interface.uI_Results.energyModel = energyModel;
zero_Interface.p_selectedProjectType = project_data.project_type();
zero_Interface.settings = settings;
/*ALCODEEND*/}

double f_createGISCables()
{/*ALCODESTART::1726584205859*/
//Create cables
for (Cable_data dataCable : c_cable_data) {
	if(dataCable.line().contains("Multi")){
		continue;
	}
	zero_Interface.f_createGISLine(f_createGISObjectsTokens(dataCable.line(), dataCable.type()), dataCable.type());
}
/*ALCODEEND*/}

double f_createPreprocessedElectricityProfile_PV(GridConnection parentGC,double[] yearlyElectricityDelivery_kWh,double[] yearlyElectricityFeedin_kWh,double[] yearlyElectricityProduction_kWh,Double pvPower_kW,double[] yearlyHeatPumpElectricityConsumption_kWh)
{/*ALCODESTART::1726584205861*/
//Create the profile 
J_EAProfile profile = new J_EAProfile(parentGC, OL_EnergyCarriers.ELECTRICITY, null, OL_ProfileAssetType.ELECTRICITYBASELOAD, energyModel.p_timeStep_h);		
profile.setStartTime_h(v_simStartHour_h);
profile.energyAssetName = parentGC.p_ownerID + " custom profile";
double extraConsumption_kWh = 0;

//Initialize parameters		
double nettDelivery_kWh;

//Preprocessing and adding new array to the J_EAProfile
if (yearlyElectricityProduction_kWh != null && yearlyElectricityFeedin_kWh != null) { // When delivery, feedin and production profiles are available
	double[] yearlyElectricityConsumption_kWh = new double[yearlyElectricityDelivery_kWh.length];
	for (int i = 0; i < yearlyElectricityDelivery_kWh.length; i++) {	
		yearlyElectricityConsumption_kWh[i] = yearlyElectricityDelivery_kWh[i] - yearlyElectricityFeedin_kWh[i] + yearlyElectricityProduction_kWh[i];
		extraConsumption_kWh += -min(yearlyElectricityConsumption_kWh[i],0);
		yearlyElectricityConsumption_kWh[i] = max(0,yearlyElectricityConsumption_kWh[i]);
	}
	profile.a_energyProfile_kWh = yearlyElectricityConsumption_kWh;
	nettDelivery_kWh = Arrays.stream(yearlyElectricityDelivery_kWh).sum() - Arrays.stream(yearlyElectricityFeedin_kWh).sum();
	//traceln("Calculating consumption profile on delivery, feedin and production profiles for company %s with %s kWp PV", parentGC.p_gridConnectionID, pvPower_kW);
} else if (pvPower_kW != null && pvPower_kW > 0) { // When only delivery, feedin profiles are available, in addition to PV power, make explicit consumption and production arrays using delivery profile and PV installed power [kW]
	double[] yearlyElectricityConsumption_kWh = new double[yearlyElectricityDelivery_kWh.length];
	if (yearlyElectricityFeedin_kWh != null) { // Terugleveringsdata beschikbaar
		//traceln("Estimating electricity consumption based on delivery and feedin profiles with pv power estimate for company %s with %s kWp PV", parentGC.p_gridConnectionID, pvPower_kW);
		double addedConsumption_kWh = 0;
		for (int i = 0; i < yearlyElectricityDelivery_kWh.length; i++) {
			double pvPowerEstimate_kW = pvPower_kW * energyModel.pp_PVProduction35DegSouth_fr.getValue(v_simStartHour_h+i*0.25);
			double estimatedConsumption_kWh = yearlyElectricityDelivery_kWh[i] + max(0, pvPowerEstimate_kW*0.25 - yearlyElectricityFeedin_kWh[i]);
			addedConsumption_kWh += max(0, pvPowerEstimate_kW*0.25 - yearlyElectricityFeedin_kWh[i]);
			yearlyElectricityConsumption_kWh[i] = max(0,estimatedConsumption_kWh);
		}
		//traceln("Added electricity consumed compared to delivery profile: %s MWh", addedConsumption_kWh/1000);
	} else { // Zonder terugleveringsdata, alleen afname
		traceln("Estimating electricity consumption based on delivery profile and pv power for company %s with %s kWp PV", parentGC.p_gridConnectionID, pvPower_kW);
		double pvPowerEstimate_kW = 0;
		double estimatedConsumption_kWh = 0;
		double addedConsumption_kWh = 0;
		for (int i = 0; i < yearlyElectricityDelivery_kWh.length; i++) {
			pvPowerEstimate_kW = pvPower_kW * energyModel.pp_PVProduction35DegSouth_fr.getValue(v_simStartHour_h+i*0.25);
			
			if (yearlyElectricityDelivery_kWh[i] != 0) { // Only update consumption if delivery is non-zero, otherwise hold previously estimated consumption constant
				estimatedConsumption_kWh = yearlyElectricityDelivery_kWh[i] + pvPowerEstimate_kW*0.25;
				//addedConsumption_kWh += pvPowerEstimate_kW * 0.25;
			} else {
				estimatedConsumption_kWh = min(pvPowerEstimate_kW*0.25, estimatedConsumption_kWh); // Take minimum to prevent to much consumption when there is not enough sun in model sun, compared to real sun data!!
				
				//addedConsumption_kWh += max(0, estimatedConsumption_kWh - pvPowerEstimate_kW*0.25);//* 0.25;
			}
			yearlyElectricityConsumption_kWh[i] = max(0,estimatedConsumption_kWh);
		}
		//traceln("Added electricity consumed compared to delivery profile: %s MWh", addedConsumption_kWh/1000);
	}
	profile.a_energyProfile_kWh = yearlyElectricityConsumption_kWh;
	nettDelivery_kWh = Arrays.stream(yearlyElectricityDelivery_kWh).sum();
} else { // No PV production
	profile.a_energyProfile_kWh = yearlyElectricityDelivery_kWh;
	nettDelivery_kWh = Arrays.stream(yearlyElectricityDelivery_kWh).sum();
}
//traceln(Arrays.stream(profile.a_energyProfile_kWh).sum() + " kWh per jaar at grid connection " + parentGC.p_ownerID + " " + profileName);
v_remainingElectricityDelivery_kWh -= nettDelivery_kWh;

if (extraConsumption_kWh > 1) {
	traceln("Preprocessing of delivery and production data led to negative consumption of: %s kWh", extraConsumption_kWh);
	traceln("Consumption profile was capped to 0kW");
}
 
if(yearlyHeatPumpElectricityConsumption_kWh != null){
	for(int i = 0; i < yearlyHeatPumpElectricityConsumption_kWh.length; i++){
		yearlyHeatPumpElectricityConsumption_kWh[i] = max(0,yearlyHeatPumpElectricityConsumption_kWh[i]);
	}
	double[] preProcessedDefaultConsumptionProfile = new double[profile.a_energyProfile_kWh.length];
	for(int i = 0; i < preProcessedDefaultConsumptionProfile.length; i++){
		preProcessedDefaultConsumptionProfile[i] = max(0,profile.a_energyProfile_kWh[i] - yearlyHeatPumpElectricityConsumption_kWh[i]);
	}
	profile.a_energyProfile_kWh = preProcessedDefaultConsumptionProfile;
	
	J_EAProfile profileHeatPumpElectricityConsumption = new J_EAProfile(parentGC, OL_EnergyCarriers.ELECTRICITY, yearlyHeatPumpElectricityConsumption_kWh, OL_ProfileAssetType.HEATPUMP_ELECTRICITY_CONSUMPTION, energyModel.p_timeStep_h);		
	profileHeatPumpElectricityConsumption.setStartTime_h(v_simStartHour_h);
	profileHeatPumpElectricityConsumption.energyAssetName = parentGC.p_ownerID + " custom heat pump electricity consumption profile";
}
/*ALCODEEND*/}

double f_startUpLoader_default()
{/*ALCODESTART::1726584205865*/
traceln("---------------------------");
traceln("======= STARTING UP =======");
traceln("---------------------------");
traceln("");

double startTime = System.currentTimeMillis();
v_timeOfModelStart_ms = startTime;
//v_hourOfYearStart= avgc_data.hourOfYearPerMonth[getMonth()] + (getDayOfMonth()-1)*24;

//Send avgc data to engine
avgc_data.f_setAVGC_data();

// Set default heating strategies
f_setDefaultHeatingStrategies();

//Import excel data to the anylogic database
f_importExcelTablesToDB();

//Fill the record collections
f_readDatabase();

//Weather market data
f_setEngineProfiles();

//Create the project interface
f_createInterface();

//Project specific styling (Needs to happen before configuring the engine)
zero_Interface.f_projectSpecificStyling();

// Populate the model
f_configureEngine_default();

//send the GIS map centre location to the Interface 
zero_Interface.map_centre_latitude = project_data.map_centre_latitude();
zero_Interface.map_centre_longitude = project_data.map_centre_longitude();
zero_Interface.map_scale = project_data.map_scale();

//Start up of the User Interface (Needs to happen after configuring the engine)
zero_Interface.f_UIStartup();

//Ending of the start up
double endTime = System.currentTimeMillis();
double duration = endTime - startTime;
v_modelStartUpDuration_s = roundToDecimal(duration / 1000, 3);

traceln(" ");
traceln("*** Start up finished *** ");
traceln("*** Start up duration: "+ v_modelStartUpDuration_s + " s ***");
traceln(" ");


//Simulate full year simulation for initial KPIs
if( settings.runHeadlessAtStartup() ){
	energyModel.f_runRapidSimulation(); // Do a full year run to have KPIs right away!
	zero_Interface.gr_simulateYearScreenSmall.setVisible(false);
	zero_Interface.gr_loadIconSmall.setVisible(false);
	zero_Interface.b_resultsUpToDate = true;
	zero_Interface.uI_Results.f_enableNonLivePlotRadioButtons(true);
}
else {
		zero_Interface.f_resetSettings();
}

//Clear all data record collections after loader is done
f_clearDataRecords();
/*ALCODEEND*/}

double f_readDatabase()
{/*ALCODESTART::1726584205867*/
//Override this function and:
//Fill the data parameters and collections using your own functions here
/*ALCODEEND*/}

double f_clearDataRecords()
{/*ALCODESTART::1726584205869*/
//Function used to clear all data record collections after the loader is done
//-> no longer needed, by clearing it is send to the garbage collector -> saves memory

/*
genericProfiles_data = null;
c_GridNode_data.clear();
c_SurveyCompanyBuilding_data.clear();
c_GenericCompanyBuilding_data.clear();
c_HouseBuilding_data.clear();
c_remainingBuilding_data.clear();
c_Solarfarm_data.clear();
c_Windfarm_data.clear();
c_Electrolyser_data.clear();
c_Battery_data.clear();
c_Chargingstation_data.clear();
c_Neighbourhood_data.clear();
c_Parcel_data.clear();
c_Cable_data_LV.clear();
c_Cable_data_MV.clear();
*/

/*ALCODEEND*/}

double f_createSurveyCompanies_Excel()
{/*ALCODESTART::1726584205871*/
traceln("Survey companies excel should be overridden with your own code");
/*ALCODEEND*/}

double f_createCompanies()
{/*ALCODESTART::1726584205873*/
//Create survey companies based on survey inload structure
switch(project_data.survey_type()){
	
	case ZORM:
		f_createSurveyCompanies_Zorm();
		break;
	
	case EXCEL:
		f_createSurveyCompanies_Excel();
		break;
}

//Create generic companies
f_createGenericCompanies();
/*ALCODEEND*/}

double f_createAdditionalGISObjects()
{/*ALCODESTART::1726584205881*/
//Area outlines
f_createGISRegionOutline();

//Parking spaces
f_createParkingSpots();

//Parcels
f_createGISParcels();

//Remaining buildings (no GC or EA)
f_createRemainingBuildings();

//Cables
f_createGISCables();
/*ALCODEEND*/}

double f_overwriteSpecificDatabaseValues()
{/*ALCODESTART::1727792666396*/
//SHOULD BE OVERRIDDEN

//Function used to overwrite specific database values after the databases have been loaded in
//Usecases for example are adjusting floor surface area to 0, to cause no consumption, etc.
// --> without the loss of data in your database sources itself

//Example code
/*
//Adjust building floor surface area to 0 to cause no consumption (empty buildings)

List<String> buildingIDS = new ArrayList<String>();

// Define the building ids that need to have no consumption
String buildingID_XXXX = "XXXX";

buildingIDS.add(buildingID_XXXX);


for(String buildingID : buildingIDS){
	update(buildings)
  	.where(buildings.building_id.eq(buildingID))
  	.set(buildings.cumulative_floor_surface_m2, 0.0)
  	.execute();
}
*/
/*ALCODEEND*/}

double[] f_convertFloatArrayToDoubleArray(float[] floatArray)
{/*ALCODESTART::1728035809860*/
if (floatArray == null) {
        return null;
}
double[] doubleArray = new double[floatArray.length];
for (int i = 0; i < floatArray.length; i++) {
    doubleArray[i] = floatArray[i];
}
return doubleArray;
/*ALCODEEND*/}

double f_getPreprocessedElectricityConsumptionTotal(GridConnection parentGC,double annualElectricityDelivery_kWh,Integer annualElectricityFeedin_kWh,Integer annualElectricityProduction_kWh,Double pvPower_kW)
{/*ALCODESTART::1728039545686*/
double annualElectricityConsumption_kWh = 0;
if(annualElectricityFeedin_kWh != null){
	if(annualElectricityProduction_kWh != null){
		annualElectricityConsumption_kWh = annualElectricityDelivery_kWh - annualElectricityFeedin_kWh + annualElectricityProduction_kWh;
	}
	else if(pvPower_kW != null && pvPower_kW > 0){
		annualElectricityConsumption_kWh = annualElectricityDelivery_kWh - annualElectricityFeedin_kWh + pvPower_kW*avgc_data.p_avgFullLoadHoursPV_hr;
	}
	else{//Geen opwek
		annualElectricityConsumption_kWh = annualElectricityDelivery_kWh;
	}
}
else if(pvPower_kW != null && pvPower_kW > 0){
	//WAT HIER?? Voor nu consumption = delivery
	//Maar: moet preprocessing van standaard profiel worden, waar gelijktijdigheid gecheckt wordt!
	annualElectricityConsumption_kWh = annualElectricityDelivery_kWh;
}
else{
	annualElectricityConsumption_kWh = annualElectricityDelivery_kWh;
}
return annualElectricityConsumption_kWh;

/*ALCODEEND*/}

double f_createCustomPVAsset(GridConnection parentGC,double[] yearlyElectricityProduction_kWh,Double pvPower_kW)
{/*ALCODESTART::1732112209863*/
if (yearlyElectricityProduction_kWh.length != 35040) {
	traceln("Skipping creation of PV asset: need 35040 data points, got %d", yearlyElectricityProduction_kWh.length);
	return;
}

// Generate custom PV production asset using production data!
double[] a_arguments = IntStream.range(0, 35040).mapToDouble(i -> v_simStartHour_h + i*0.25).toArray(); // time axis

// From kWh/quarter to normalized power
double totalProduction_kWh = Arrays.stream(yearlyElectricityProduction_kWh).sum();
double fullLoadHours_h = totalProduction_kWh / pvPower_kW;
double[] a_normalizedPower_fr = Arrays.stream(yearlyElectricityProduction_kWh).map(i -> 4 * i / totalProduction_kWh * fullLoadHours_h ).toArray();

//traceln("Full load hours of a_normalizedPower_fr %s: ", Arrays.stream(a_normalizedPower_fr).sum()/4);
//traceln("Max of a_normalizedPower_fr %s: ", Arrays.stream(a_normalizedPower_fr).max());

TableFunction tf_customPVproduction_fr = new TableFunction(a_arguments, a_normalizedPower_fr, TableFunction.InterpolationType.INTERPOLATION_LINEAR, 2, TableFunction.OutOfRangeAction.OUTOFRANGE_REPEAT, 0.0);
J_ProfilePointer profilePointer = new J_ProfilePointer((parentGC.p_ownerID + "_PVproduction") , tf_customPVproduction_fr);
energyModel.f_addProfile(profilePointer);
J_EAProduction production_asset = new J_EAProduction(parentGC, OL_EnergyAssetType.PHOTOVOLTAIC, (parentGC.p_ownerID + "_rooftopPV"), OL_EnergyCarriers.ELECTRICITY, (double)pvPower_kW, energyModel.p_timeStep_h, profilePointer);

traceln("Custom PV asset added to GC: " + parentGC.p_ownerID);
//traceln("Custom PV asset added to %s with installed power %s kW and %s full load hours!", parentGC.p_ownerID, pvPower_kW, fullLoadHours_h);

/*ALCODEEND*/}

double f_iEASurveyCompanies_Zorm(GridConnection companyGC,com.zenmo.zummon.companysurvey.GridConnection gridConnection)
{/*ALCODESTART::1732112244908*/
//Initialize boolean that sets the creation of currently existing electric (demand) EA
boolean createElectricEA = true;
final int targetYear = 2023;


//Create current scenario parameter list
J_scenario_Current current_scenario_list = new J_scenario_Current();
zero_Interface.c_scenarioMap_Current.put(companyGC, current_scenario_list);

//Create future scenario parameter list
J_scenario_Future future_scenario_list = new J_scenario_Future();
zero_Interface.c_scenarioMap_Future.put(companyGC, future_scenario_list);



//Get PV power (used for preprocessing and estimating grid capacity if unknown)
Double pvPower_kW = (gridConnection.getSupply().getPvInstalledKwp() != null) ? new Double(gridConnection.getSupply().getPvInstalledKwp()) : null;


////Electricity (connection and consumption)
//Initialize contract capacity with 0 for when companies fill in survey already but currently have no connection yet
companyGC.v_liveConnectionMetaData.contractedDeliveryCapacity_kW = 0.0;
companyGC.v_liveConnectionMetaData.contractedFeedinCapacity_kW = 0.0;
companyGC.v_liveConnectionMetaData.physicalCapacity_kW = 0.0;

f_createDieselTractors(companyGC, gridConnection.getTransport().getAgriculture());

//Check for electricity connection and data
if (gridConnection.getElectricity().getHasConnection()){
	
	if ((gridConnection.getElectricity().getContractedConnectionCapacityKw() == null || 
		gridConnection.getElectricity().getContractedConnectionCapacityKw() <= 0) &&
		(gridConnection.getElectricity().getGrootverbruik().getPhysicalCapacityKw() == null || 
		gridConnection.getElectricity().getGrootverbruik().getPhysicalCapacityKw() <= 0)) {
		
		traceln("SURVEYOWNER HAS NOT FILLED IN CONNECTION CAPACITY!!! AVG values taken");
		companyGC.v_liveConnectionMetaData.contractedDeliveryCapacity_kW = avgc_data.p_avgUtilityConnectionCapacity_kW;
		companyGC.v_liveConnectionMetaData.contractedFeedinCapacity_kW = avgc_data.p_avgUtilityConnectionCapacity_kW;
		companyGC.v_liveConnectionMetaData.physicalCapacity_kW = avgc_data.p_avgUtilityConnectionCapacity_kW;
		
		companyGC.v_liveConnectionMetaData.physicalCapacityKnown = false;
		companyGC.v_liveConnectionMetaData.contractedDeliveryCapacityKnown = false;
		companyGC.v_liveConnectionMetaData.contractedFeedinCapacityKnown = false;
	}
	else{
	
		if(gridConnection.getElectricity().getContractedConnectionCapacityKw() != null && gridConnection.getElectricity().getContractedConnectionCapacityKw() > 0){
			companyGC.v_liveConnectionMetaData.contractedDeliveryCapacity_kW = gridConnection.getElectricity().getContractedConnectionCapacityKw(); //Contracted connection capacity
			companyGC.v_liveConnectionMetaData.contractedDeliveryCapacityKnown = true;
		}
		else{
			companyGC.v_liveConnectionMetaData.contractedDeliveryCapacity_kW = ((double)gridConnection.getElectricity().getGrootverbruik().getPhysicalCapacityKw()); //Contracted connection capacity
			companyGC.v_liveConnectionMetaData.contractedDeliveryCapacityKnown = false;
		}
		
		
		//Check if contract capacity feedin has been filled in: if not, make the same as pv capacity
		if(gridConnection.getElectricity().getGrootverbruik().getContractedConnectionSupplyCapacityKw() != null && gridConnection.getElectricity().getGrootverbruik().getContractedConnectionSupplyCapacityKw() > 0){
			companyGC.v_liveConnectionMetaData.contractedFeedinCapacity_kW = ((double)gridConnection.getElectricity().getGrootverbruik().getContractedConnectionSupplyCapacityKw()); //Contracted connection capacity
			companyGC.v_liveConnectionMetaData.contractedFeedinCapacityKnown = true;
		}
		else{
			if(pvPower_kW != null){
				companyGC.v_liveConnectionMetaData.contractedFeedinCapacity_kW = pvPower_kW;
			}
			else{
				companyGC.v_liveConnectionMetaData.contractedFeedinCapacity_kW = 0.0;
			}
			companyGC.v_liveConnectionMetaData.contractedFeedinCapacityKnown = false;
		}
		
		//Check if physical capacity has been filled in: if not, make the same as maximum of contract delivery and feedin
		if(gridConnection.getElectricity().getGrootverbruik().getPhysicalCapacityKw() != null && gridConnection.getElectricity().getGrootverbruik().getPhysicalCapacityKw() > 0){
			companyGC.v_liveConnectionMetaData.physicalCapacity_kW = (double)gridConnection.getElectricity().getGrootverbruik().getPhysicalCapacityKw(); //Contracted connection capacity
			companyGC.v_liveConnectionMetaData.physicalCapacityKnown = true;
		}
		else{
			companyGC.v_liveConnectionMetaData.physicalCapacity_kW = max(companyGC.v_liveConnectionMetaData.contractedDeliveryCapacity_kW, companyGC.v_liveConnectionMetaData.contractedFeedinCapacity_kW); //Contracted connection capacity
			companyGC.v_liveConnectionMetaData.physicalCapacityKnown = false;
		}
	}
	
	//Add to current scenario list
	current_scenario_list.setCurrentContractDeliveryCapacity_kW(companyGC.v_liveConnectionMetaData.contractedDeliveryCapacity_kW);
	current_scenario_list.setCurrentContractFeedinCapacity_kW(companyGC.v_liveConnectionMetaData.contractedFeedinCapacity_kW);
	current_scenario_list.setCurrentPhysicalConnectionCapacity_kW(companyGC.v_liveConnectionMetaData.physicalCapacity_kW);
	
	
	//Electricity consumption profile
	String profileName = "default_office_electricity_demand_fr";
	
	//Check if quarter hourly values are available in vallum
	boolean createdTimeSeriesAssets = f_createElectricityTimeSeriesAssets(companyGC, gridConnection);
	
	if(createdTimeSeriesAssets){
		if(!settings.createCurrentElectricityEA()){//input boolean: Dont create current electric energy assets if electricity profile or total is known.
			createElectricEA = false;
		}
	}
	else{ //(!createdTimeSeriesAssets) { // 
		double yearlyElectricityConsumption_kWh  = 0;
		try { // Check if quarterly hour values are available in excel database
			if(selectFirstValue(Double.class, "SELECT " + "ccid" + gridConnection.getSequence().toString() + "_demand FROM comp_elec_consumption LIMIT 1;") != null){
		  		companyGC.v_hasQuarterHourlyValues = true;
				profileName = "ccid" + companyGC.p_gridConnectionID;
				
				//Check if solar was already producing in simualtion year (Check for now: if year production = 0 , no solar yet, if year production = null, no data: so assume there was solar already)
				if(gridConnection.getElectricity().getAnnualElectricityProduction_kWh() != null && gridConnection.getElectricity().getAnnualElectricityProduction_kWh  () == 0){
					pvPower_kW = null;
				}
		
				if (!settings.createCurrentElectricityEA()){//input boolean: Dont create current electric energy assets if electricity profile is present.
					createElectricEA = false;
				}
			}
		}
		catch(Exception e) {
			//Data not available, do nothing and leave v_hasQuarterHourlyValues on false.
		}
		
		if(companyGC.v_hasQuarterHourlyValues == false){//Calculate yearly consumption based on yearly delivery (and yearly feedin, production or solarpanels if available)
			//Get totals
			double yearlyElectricityDelivery_kWh = (gridConnection.getElectricity().getAnnualElectricityDemandKwh() != null) ? gridConnection.getElectricity().getAnnualElectricityDemandKwh() : 0; // Yearly electricity consumption (0 if value is null)
			Integer yearlyElectricityFeedin_kWh = gridConnection.getElectricity().getAnnualElectricityFeedIn_kWh();
			Integer yearlyElectricityProduction_kWh = gridConnection.getElectricity().getAnnualElectricityProduction_kWh();
			
			//Calculate consumption
			yearlyElectricityConsumption_kWh = f_getPreprocessedElectricityConsumptionTotal(companyGC, yearlyElectricityDelivery_kWh, yearlyElectricityFeedin_kWh, yearlyElectricityProduction_kWh, pvPower_kW);
			
			//If no electricity consumption, determine the consumption based on average values and floor surface and connection capacity
			if(yearlyElectricityConsumption_kWh == 0){
				yearlyElectricityConsumption_kWh = avgc_data.p_avgCompanyElectricityConsumption_kWhpm2*companyGC.p_floorSurfaceArea_m2;
				
				//Check if it is within the contracted limits (peak should at least be 20% lower than contracted capacity
				if(yearlyElectricityConsumption_kWh*defaultProfiles_data.getDefaultOfficeElectricityDemandProfileMaximum_fr() > 0.8*companyGC.v_liveConnectionMetaData.contractedDeliveryCapacity_kW){
					yearlyElectricityConsumption_kWh = 0.8*companyGC.v_liveConnectionMetaData.contractedDeliveryCapacity_kW/defaultProfiles_data.getDefaultOfficeElectricityDemandProfileMaximum_fr();
				}
				 
			}
			
			//Update total Yearly electricity consumption (only when no timestep data available, cause when thats avaiable, it happens in the preprocessing function)
			if (yearlyElectricityDelivery_kWh != 0){
				v_remainingElectricityDelivery_kWh -= yearlyElectricityDelivery_kWh;
			}
			else{
				v_remainingElectricityDelivery_kWh -= yearlyElectricityConsumption_kWh;
			}
		}
		
		//Add base electricity demand profile (with profile if available, with generic pattern if only yearly data is available)
		f_addElectricityDemandProfile(companyGC, yearlyElectricityConsumption_kWh, pvPower_kW, companyGC.v_hasQuarterHourlyValues, profileName);
	}
}

//If everything is 0 set the GC as non active
if(companyGC.v_liveConnectionMetaData.contractedDeliveryCapacity_kW == 0 && companyGC.v_liveConnectionMetaData.contractedFeedinCapacity_kW == 0 && companyGC.v_liveConnectionMetaData.physicalCapacity_kW == 0){
	companyGC.v_isActive = false;
}
		
//Grid expansion request
if (gridConnection.getElectricity().getGridExpansion().getHasRequestAtGridOperator() != null && gridConnection.getElectricity().getGridExpansion().getHasRequestAtGridOperator()){
	future_scenario_list.setRequestedContractDeliveryCapacity_kW(((gridConnection.getElectricity().getGridExpansion().getRequestedKW() != null) ? gridConnection.getElectricity().getGridExpansion().getRequestedKW() : 0) + companyGC.v_liveConnectionMetaData.contractedDeliveryCapacity_kW);
	future_scenario_list.setRequestedContractFeedinCapacity_kW(((gridConnection.getElectricity().getGridExpansion().getRequestedKW() != null) ? gridConnection.getElectricity().getGridExpansion().getRequestedKW() : 0) + companyGC.v_liveConnectionMetaData.contractedFeedinCapacity_kW);
	future_scenario_list.setRequestedPhysicalConnectionCapacity_kW(max(companyGC.v_liveConnectionMetaData.physicalCapacity_kW, max(future_scenario_list.getRequestedContractDeliveryCapacity_kW(), future_scenario_list.getRequestedContractFeedinCapacity_kW())));
}
else{
	future_scenario_list.setRequestedContractDeliveryCapacity_kW(companyGC.v_liveConnectionMetaData.contractedDeliveryCapacity_kW);
	future_scenario_list.setRequestedContractFeedinCapacity_kW(companyGC.v_liveConnectionMetaData.contractedFeedinCapacity_kW);
	future_scenario_list.setRequestedPhysicalConnectionCapacity_kW(companyGC.v_liveConnectionMetaData.physicalCapacity_kW);
}


////Supply (pv, wind, etc.)
if (gridConnection.getSupply().getHasSupply() != null && gridConnection.getSupply().getHasSupply()){
	//gridConnection.getElectricity().getAnnualElectricityProductionKwh() // Staat niet meer in het formulier!
	
	double[] yearlyElectricityProduction_kWh_array = null;
	
	var quarterHourlyProduction_kWh = gridConnection.getElectricity().getQuarterHourlyProduction_kWh();
	if (quarterHourlyProduction_kWh != null && quarterHourlyProduction_kWh.hasNumberOfValuesForOneYear()) {
		yearlyElectricityProduction_kWh_array = f_convertFloatArrayToDoubleArray(quarterHourlyProduction_kWh.convertToQuarterHourly().getFullYearOrFudgeIt(targetYear));
	}
	
	if(yearlyElectricityProduction_kWh_array == null && gridConnection.getSupply().getPvInstalledKwp() != null && gridConnection.getSupply().getPvInstalledKwp() > 0){
		try {
			if(selectFirstValue(Double.class, "SELECT " + "ccid" + companyGC.p_gridConnectionID + "_production FROM comp_elec_consumption LIMIT 1;") != null){
		  		List<Double> yearlyElectricityProduction_kWh_list = selectValues(double.class, "SELECT " + "ccid" + companyGC.p_gridConnectionID + "_production FROM comp_elec_consumption;");
				yearlyElectricityProduction_kWh_array = (yearlyElectricityProduction_kWh_list != null) ? yearlyElectricityProduction_kWh_list.stream().mapToDouble(d -> max(0,d)).toArray() : null;
			}
		}
		catch(Exception e) {
		}
	}
	if (yearlyElectricityProduction_kWh_array != null && gridConnection.getSupply().getPvInstalledKwp() != null && gridConnection.getSupply().getPvInstalledKwp() > 0 && !gridConnection.getHeat().getHeatingTypes().contains(com.zenmo.zummon.companysurvey.HeatingType.COMBINED_HEAT_AND_POWER)){
		f_createCustomPVAsset(companyGC, yearlyElectricityProduction_kWh_array, (double)gridConnection.getSupply().getPvInstalledKwp()); // Create custom PV asset when production data is available!
		current_scenario_list.setCurrentPV_kW(gridConnection.getSupply().getPvInstalledKwp());
	} else if (gridConnection.getSupply().getPvInstalledKwp() != null && gridConnection.getSupply().getPvInstalledKwp() > 0){			
		//gridConnection.getSupply().getPvOrientation(); // Wat doen we hier mee????? Nog niets!
		f_addEnergyProduction(companyGC, OL_EnergyAssetType.PHOTOVOLTAIC, "Rooftop Solar", gridConnection.getSupply().getPvInstalledKwp());
		
		//add to scenario: current
		current_scenario_list.setCurrentPV_kW(gridConnection.getSupply().getPvInstalledKwp());
		//current_scenario_list.currentPV_orient = gridConnection.getSupply().getPvOrientation();
	} 	
	//Wind
	if (gridConnection.getSupply().getWindInstalledKw() != null && gridConnection.getSupply().getWindInstalledKw() > 0){
		f_addEnergyProduction(companyGC, OL_EnergyAssetType.WINDMILL, "Wind mill", gridConnection.getSupply().getWindInstalledKw());

		//add to scenario: current
		current_scenario_list.setCurrentWind_kW(gridConnection.getSupply().getWindInstalledKw());
	}
}

//Planned supply (PV)
if (gridConnection.getSupply().getPvPlanned() != null && gridConnection.getSupply().getPvPlanned()){
	future_scenario_list.setPlannedPV_kW(current_scenario_list.getCurrentPV_kW() + (gridConnection.getSupply().getPvPlannedKwp() != null ? gridConnection.getSupply().getPvPlannedKwp() : 0)); 
	future_scenario_list.setPlannedPV_year(gridConnection.getSupply().getPvPlannedYear());
	//gridConnection.getSupply().getPvPlannedOrientation();
}
else{
	future_scenario_list.setPlannedPV_kW(current_scenario_list.getCurrentPV_kW());
}

//Planned supply (Wind)
if (gridConnection.getSupply().getWindPlannedKw() != null && gridConnection.getSupply().getWindPlannedKw() > 0){
	future_scenario_list.setPlannedWind_kW(current_scenario_list.getCurrentWind_kW() + (gridConnection.getSupply().getWindPlannedKw() != null ? gridConnection.getSupply().getWindPlannedKw() : 0));
	// plannedWind_year // ???
}
else{
	future_scenario_list.setPlannedWind_kW(current_scenario_list.getCurrentWind_kW());
}

////Heating and gas
OL_GridConnectionHeatingType heatingType = f_heatingSurveyCompany(companyGC, gridConnection);

//add heating type to scenario: current and future
current_scenario_list.setCurrentHeatingType(heatingType);
future_scenario_list.setPlannedHeatingType(heatingType);


// Electric Storage
Float battery_power_kW = 0f;
Float battery_capacity_kWh = 0f;

if (gridConnection.getStorage().getHasBattery() != null && gridConnection.getStorage().getHasBattery() && createElectricEA){ // Check if battery present and if electric demand EA should be created
	if (gridConnection.getStorage().getBatteryPowerKw() != null){
		battery_power_kW = gridConnection.getStorage().getBatteryPowerKw();
	}
	if (gridConnection.getStorage().getBatteryCapacityKwh() != null){
		battery_capacity_kWh = gridConnection.getStorage().getBatteryCapacityKwh();	
	}
}
// Elke survey company krijgt hoe dan ook een batterij EA (ook als op dit moment nog geen batterij aanwezig is, maar dan is capaciteit gewoon 0)
f_addStorage(companyGC, battery_power_kW, battery_capacity_kWh, OL_EnergyAssetType.STORAGE_ELECTRIC);
companyGC.p_batteryAlgorithm = new J_BatteryManagementSelfConsumption(companyGC);

//add to scenario: current
current_scenario_list.setCurrentBatteryCapacity_kWh(battery_capacity_kWh);
current_scenario_list.setCurrentBatteryPower_kW(battery_power_kW);

	
	
if (gridConnection.getStorage().getHasThermalStorage() != null && gridConnection.getStorage().getHasThermalStorage()){ // Check for thermal storage
	//gridConnection.getStorage().getThermalStorageKw()
	//J_EAStorageHeat(Agent parentAgent, OL_EAStorageTypes heatStorageType, double capacityHeat_kW, double lossFactor_WpK, double timestep_h, double initialTemperature_degC, double minTemperature_degC, double maxTemperature_degC, double setTemperature_degC, double heatCapacity_JpK, String ambientTempType ) {
	//J_EAStorageHeat(companyGC, OL_EAStorageTypes heatStorageType, double capacityHeat_kW, double lossFactor_WpK, double timestep_h, double initialTemperature_degC, double minTemperature_degC, double maxTemperature_degC, double setTemperature_degC, double heatCapacity_JpK, String ambientTempType ) {
	//Denk ook aan aansturing?!!
}

if (gridConnection.getStorage().getHasPlannedBattery() != null && gridConnection.getStorage().getHasPlannedBattery()){ // Check for planned battery
	future_scenario_list.setPlannedBatteryCapacity_kWh((gridConnection.getStorage().getPlannedBatteryCapacityKwh() != null ? gridConnection.getStorage().getPlannedBatteryCapacityKwh() : 0) + current_scenario_list.getCurrentBatteryCapacity_kWh());
	future_scenario_list.setPlannedBatteryPower_kW((gridConnection.getStorage().getPlannedBatteryPowerKw() != null ? gridConnection.getStorage().getPlannedBatteryPowerKw() : 0) + current_scenario_list.getCurrentBatteryPower_kW());
}
else{
future_scenario_list.setPlannedBatteryCapacity_kWh(current_scenario_list.getCurrentBatteryCapacity_kWh());
future_scenario_list.setPlannedBatteryPower_kW(current_scenario_list.getCurrentBatteryPower_kW());
}



////Transport

//Cars of comuters and visitors 
int nbDailyCarVisitors_notNull = (gridConnection.getTransport().getNumDailyCarVisitors() != null) ? gridConnection.getTransport().getNumDailyCarVisitors() : 0;
int nbDailyCarCommuters_notNull = (gridConnection.getTransport().getNumDailyCarAndVanCommuters() != null) ? gridConnection.getTransport().getNumDailyCarAndVanCommuters() : 0;

if (nbDailyCarCommuters_notNull + nbDailyCarVisitors_notNull > 0){	
	
	int nbEVCarsComute = (gridConnection.getTransport().getNumCommuterAndVisitorChargePoints() != null) ? gridConnection.getTransport().getNumCommuterAndVisitorChargePoints() : 0; // Wat doen we hier mee????
	int nbDieselCarsComute = gridConnection.getTransport().getNumDailyCarAndVanCommuters() + nbDailyCarVisitors_notNull - nbEVCarsComute;

	boolean isDefaultVehicle = true;
	double maxChargingPower_kW 		= avgc_data.p_avgEVMaxChargePowerCar_kW;	
	
	for (int i = 0; i< nbDieselCarsComute; i++){
		f_addDieselVehicle(companyGC, OL_EnergyAssetType.DIESEL_VEHICLE, isDefaultVehicle, 0);
	}
	
	
	//check if charge power is filled in
	if (gridConnection.getTransport().getCars().getPowerPerChargePointKw() != null){
		maxChargingPower_kW 		= gridConnection.getTransport().getCars().getPowerPerChargePointKw();		
		isDefaultVehicle			= false;
	}
	
	if (createElectricEA){ // Check if electric demand EA should be created
		for (int j = 0; j< nbEVCarsComute; j++){
			f_addElectricVehicle(companyGC, OL_EnergyAssetType.ELECTRIC_VEHICLE, isDefaultVehicle, 0, maxChargingPower_kW);
		}
	}
	
	//add to scenario: current
	current_scenario_list.setCurrentEVCars(nbEVCarsComute);
	current_scenario_list.setCurrentDieselCars(nbDieselCarsComute);
	
	//Initialize future cars
	future_scenario_list.setPlannedEVCars(current_scenario_list.getCurrentEVCars());

}


//Business vehicles
if (gridConnection.getTransport().getHasVehicles() != null && gridConnection.getTransport().getHasVehicles()){

	//Cars
	if (gridConnection.getTransport().getCars().getNumCars() != null && gridConnection.getTransport().getCars().getNumCars() != 0){
		
		
		//Update v_remaningAmount of cars (company owned only)
		v_remainingNumberOfCars -= gridConnection.getTransport().getCars().getNumCars();
		
		//gridConnection.getTransport().getCars().getNumChargePoints(); // Wat doen we hier mee????????
		
		Integer nbEVCars = gridConnection.getTransport().getCars().getNumElectricCars();
		if (nbEVCars == null) {
		    nbEVCars = 0;
		}
		int nbDieselCars = gridConnection.getTransport().getCars().getNumCars() - nbEVCars;

		
		boolean isDefaultVehicle		= true;
		double annualTravelDistance_km 	= 0;
		double maxChargingPower_kW 		= avgc_data.p_avgEVMaxChargePowerCar_kW;		
		
		//check if annual travel distance is filled in
		if (gridConnection.getTransport().getCars().getAnnualTravelDistancePerCarKm() != null){
			annualTravelDistance_km 	= gridConnection.getTransport().getCars().getAnnualTravelDistancePerCarKm();
			isDefaultVehicle			= false;
		}
		
		//create diesel vehicle
		for (int i = 0; i< nbDieselCars; i++){
		f_addDieselVehicle(companyGC, OL_EnergyAssetType.DIESEL_VEHICLE, isDefaultVehicle, annualTravelDistance_km);
		}
		
		//check if charge power is filled in
		if (gridConnection.getTransport().getCars().getPowerPerChargePointKw() != null){
			maxChargingPower_kW 		= gridConnection.getTransport().getCars().getPowerPerChargePointKw();		
			isDefaultVehicle			= false;		
		}
		
		//create EV
		if (createElectricEA){ // Check if electric demand EA should be created
			for (int j = 0; j< nbEVCars; j++){
			f_addElectricVehicle(companyGC, OL_EnergyAssetType.ELECTRIC_VEHICLE, isDefaultVehicle, annualTravelDistance_km, maxChargingPower_kW);
			}
		}
		
		//add to scenario: current
		current_scenario_list.setCurrentEVCars(((current_scenario_list.getCurrentEVCars() != null) ? current_scenario_list.getCurrentEVCars() : 0) + nbEVCars);
		current_scenario_list.setCurrentDieselCars(((current_scenario_list.getCurrentDieselCars() != null) ? current_scenario_list.getCurrentDieselCars() : 0) + nbDieselCars);
		current_scenario_list.setCurrentEVCarChargePower_kW(maxChargingPower_kW);
		
		//Update Planned cars
		future_scenario_list.setPlannedEVCars((gridConnection.getTransport().getCars().getNumPlannedElectricCars() != null ? gridConnection.getTransport().getCars().getNumPlannedElectricCars() : 0) + current_scenario_list.getCurrentEVCars());
		future_scenario_list.setPlannedHydrogenCars((gridConnection.getTransport().getCars().getNumPlannedHydrogenCars() != null) ? gridConnection.getTransport().getCars().getNumPlannedHydrogenCars() : 0);
		
	}
	
	
	//Vans
	if (gridConnection.getTransport().getVans().getNumVans() != null && gridConnection.getTransport().getVans().getNumVans() != 0){
		
		//Update v_remaningAmount of vans
		v_remainingNumberOfVans -= gridConnection.getTransport().getVans().getNumVans();
		
		
		//gridConnection.getTransport().getVans().getNumChargePoints(); // Wat doen we hier mee????????
		
		Integer nbEVVans = gridConnection.getTransport().getVans().getNumElectricVans();	
		if (nbEVVans == null) {
		    nbEVVans = 0;
		}	
		int nbDieselVans = gridConnection.getTransport().getVans().getNumVans() - nbEVVans;

		boolean isDefaultVehicle		= true;
		double annualTravelDistance_km 	= 0;
		double maxChargingPower_kW 		= avgc_data.p_avgEVMaxChargePowerVan_kW;		
		
		//check if annual travel distance is filled in
		if (gridConnection.getTransport().getVans().getAnnualTravelDistancePerVanKm() != null){
			annualTravelDistance_km 	= gridConnection.getTransport().getVans().getAnnualTravelDistancePerVanKm();
			isDefaultVehicle			= false;
		}
		
		//create diesel vehicles
		for (int i = 0; i< nbDieselVans; i++){
			f_addDieselVehicle(companyGC, OL_EnergyAssetType.DIESEL_VAN, isDefaultVehicle, annualTravelDistance_km);
		}
		
		//check if charge power is filled in
		if (gridConnection.getTransport().getVans().getPowerPerChargePointKw() != null){
			maxChargingPower_kW 		= gridConnection.getTransport().getVans().getPowerPerChargePointKw();	
			isDefaultVehicle			= false;		
		}
		
		//create electric vehicles
		if (createElectricEA){ // Check if electric demand EA should be created
			for (int j = 0; j< nbEVVans; j++){
				f_addElectricVehicle(companyGC, OL_EnergyAssetType.ELECTRIC_VAN, isDefaultVehicle, annualTravelDistance_km, maxChargingPower_kW);
			}
		}
		
		//add to scenario: current
		current_scenario_list.setCurrentEVVans(nbEVVans);
		current_scenario_list.setCurrentDieselVans(nbDieselVans);
		current_scenario_list.setCurrentEVVanChargePower_kW(maxChargingPower_kW);
		
		//Planned
		future_scenario_list.setPlannedEVVans((gridConnection.getTransport().getVans().getNumPlannedElectricVans() != null ? gridConnection.getTransport().getVans().getNumPlannedElectricVans() : 0) + current_scenario_list.getCurrentEVVans());
		future_scenario_list.setPlannedHydrogenVans((gridConnection.getTransport().getVans().getNumPlannedHydrogenVans() != null) ? gridConnection.getTransport().getVans().getNumPlannedHydrogenVans() : 0);
	}
	
		
	
	//Trucks
	if (gridConnection.getTransport().getTrucks().getNumTrucks() != null && gridConnection.getTransport().getTrucks().getNumTrucks() != 0){
		
		//Update v_remaningAmount of trucks
		v_remainingNumberOfTrucks -= gridConnection.getTransport().getTrucks().getNumTrucks();


		//gridConnection.getTransport().getTrucks().getNumChargePoints(); // Wat doen we hier mee????????
		
		
		Integer nbEVTrucks = gridConnection.getTransport().getTrucks().getNumElectricTrucks();		
		if (nbEVTrucks == null) {
		    nbEVTrucks = 0;
		}
		int nbDieselTrucks = gridConnection.getTransport().getTrucks().getNumTrucks() - nbEVTrucks;
	
		boolean isDefaultVehicle		= true;
		double annualTravelDistance_km = 0;
		double maxChargingPower_kW = avgc_data.p_avgEVMaxChargePowerTruck_kW;
		
		//check if annual travel distance is filled in
		if (gridConnection.getTransport().getTrucks().getAnnualTravelDistancePerTruckKm() != null){
			annualTravelDistance_km 	= gridConnection.getTransport().getTrucks().getAnnualTravelDistancePerTruckKm();
			isDefaultVehicle			= false;
		}
		
		//create diesel vehicles
		for (int i = 0; i< nbDieselTrucks; i++){
			f_addDieselVehicle(companyGC, OL_EnergyAssetType.DIESEL_TRUCK, isDefaultVehicle, annualTravelDistance_km);
		}
		
		//check if charge power is filled in
		if (gridConnection.getTransport().getTrucks().getPowerPerChargePointKw() != null){
			maxChargingPower_kW 		= gridConnection.getTransport().getTrucks().getPowerPerChargePointKw();
			isDefaultVehicle			= false;		
		}
		
		//create electric vehicles
		if (createElectricEA){ // Check if electric demand EA should be created
			for (int j = 0; j< nbEVTrucks; j++){
			f_addElectricVehicle(companyGC, OL_EnergyAssetType.ELECTRIC_TRUCK, isDefaultVehicle, annualTravelDistance_km, maxChargingPower_kW);
			}
		}
		
		//add to scenario: current
		current_scenario_list.setCurrentEVTrucks(nbEVTrucks);
		current_scenario_list.setCurrentDieselTrucks(nbDieselTrucks);
		current_scenario_list.setCurrentEVTruckChargePower_kW(maxChargingPower_kW);
		
		//Planned
		future_scenario_list.setPlannedEVTrucks((gridConnection.getTransport().getTrucks().getNumPlannedElectricTrucks() != null ? gridConnection.getTransport().getTrucks().getNumPlannedElectricTrucks() : 0) + current_scenario_list.getCurrentEVTrucks());
		future_scenario_list.setPlannedHydrogenTrucks((gridConnection.getTransport().getTrucks().getNumPlannedHydrogenTrucks() != null) ? gridConnection.getTransport().getTrucks().getNumPlannedHydrogenTrucks() : 0);
	}
	
	
	//Other
	if (Objects.nonNull(gridConnection.getTransport().getOtherVehicles().getHasOtherVehicles())){
	
	// Wat doen we hier mee???
	
	}
}

/*ALCODEEND*/}

Building_data f_createBuildingData_Vallum(GridConnection companyGC,String PandID)
{/*ALCODESTART::1737741603780*/
com.zenmo.bag.Pand pand_data_vallum = map_buildingData_Vallum.get(PandID);

//Calculate surface area
GISRegion gisRegion = zero_Interface.f_createGISObject(f_createGISObjectsTokens(pand_data_vallum.getGeometry().toString(), OL_GISObjectType.BUILDING));
double surfaceArea_m2 = gisRegion.area();
gisRegion.remove();


//Create a building_data record
Building_data building_data_record = Building_data.builder().

address_id("verblijfsobject." + PandID).
building_id(PandID).
streetname(companyGC.p_address.getStreetName()).
house_number(companyGC.p_address.getHouseNumber()).
house_letter(companyGC.p_address.getHouseLetter()).
house_addition(companyGC.p_address.getHouseAddition()).
postalcode(companyGC.p_address.getPostalcode()).
city(companyGC.p_address.getPostalcode()).
build_year(pand_data_vallum.getBouwjaar()).	
status(pand_data_vallum.getStatus()).
//purpose(row.get( buildings.purpose )).
address_floor_surface_m2(surfaceArea_m2).
polygon_area_m2(surfaceArea_m2).
annotation(companyGC.p_owner.p_actorID).
//extra_info(row.get( buildings.extra_info )).
//gridnode_id(row.get( buildings.gridnode_id )).
//latitude(row.get( buildings.latitude )).
//longitude(row.get( buildings.longitude )).
polygon(pand_data_vallum.getGeometry().toString()).
build();

return building_data_record;
/*ALCODEEND*/}

double f_createDieselTractors(GridConnection companyGridConnection,com.zenmo.zummon.companysurvey.Agriculture agricultureSurveyData)
{/*ALCODESTART::1737712184349*/
final double annualDiesel_L = Optional.ofNullable(agricultureSurveyData.getAnnualDieselUsage_L()).orElse(0.0);
final int numTractors = Optional.ofNullable(agricultureSurveyData.getNumTractors()).orElse(annualDiesel_L > 0.0 ? 1 : 0);

if (numTractors > 0 && annualDiesel_L <= 0.0) {
    // TODO: this should be in Tractor constructor
    throw new RuntimeException("Tractor diesel usage missing for " + companyGridConnection.p_gridConnectionID);
}

CustomProfile_data tractorProfile = findFirst(c_customProfiles_data, profile -> profile.customProfileID().equals("TractorProfile")); ///????

for (int i = 0; i < numTractors; i++) {
	if(tractorProfile == null){
		throw new RuntimeException("Trying to make a tractor, without having loaded in a tractor profile for GC: " + companyGridConnection.p_gridConnectionID);
	}
    new J_EADieselTractor(companyGridConnection, annualDiesel_L / numTractors, tractorProfile.getValuesArray(), energyModel.p_timeStep_h);
}
/*ALCODEEND*/}

boolean f_createElectricityTimeSeriesAssets(GridConnection gridConnection,com.zenmo.zummon.companysurvey.GridConnection gridConnectionSurvey)
{/*ALCODESTART::1738248965949*/
var electricitySurvey = gridConnectionSurvey.getElectricity();

double[] deliveryTimeSeries_kWh = f_timeSeriesToQuarterHourlyDoubleArray(electricitySurvey.getQuarterHourlyDelivery_kWh());
if (deliveryTimeSeries_kWh == null) {
	// delivery is the minimum we require to do anything with timeseries data
	return false;
}

double[] feedInTimeSeries_kWh = f_timeSeriesToQuarterHourlyDoubleArray(electricitySurvey.getQuarterHourlyFeedIn_kWh());
double[] productionTimeSeries_kWh = f_timeSeriesToQuarterHourlyDoubleArray(electricitySurvey.getQuarterHourlyProduction_kWh());

Double pvPower_kW = Optional.ofNullable(gridConnectionSurvey.getSupply().getPvInstalledKwp())
	.map(it -> (double) it)
	.orElse(null);

double[] heatPumpElectricityTimeSeries_kWh = f_timeSeriesToQuarterHourlyDoubleArray(gridConnectionSurvey.getHeat().getHeatPumpElectricityConsumptionTimeSeries_kWh());

//Preprocess the arrays and create the consumption pattern
f_createPreprocessedElectricityProfile_PV(gridConnection, deliveryTimeSeries_kWh, feedInTimeSeries_kWh, productionTimeSeries_kWh, pvPower_kW, heatPumpElectricityTimeSeries_kWh);

gridConnection.v_hasQuarterHourlyValues = true;

return true;
/*ALCODEEND*/}

double[] f_timeSeriesToQuarterHourlyDoubleArray(com.zenmo.zummon.companysurvey.TimeSeries timeSeries)
{/*ALCODESTART::1738572338816*/
int targetYear = 2024;
if (timeSeries == null) {
	return null;
}

if (!timeSeries.hasNumberOfValuesForOneYear()) {
	traceln("Time series has too few values for one year");
	return null;
}

return f_convertFloatArrayToDoubleArray(timeSeries.convertToQuarterHourly().getFullYearOrFudgeIt(targetYear));
/*ALCODEEND*/}

double f_connectGCToExistingBuilding(GridConnection connectingGC,GIS_Building existingBuilding,Building_data connectingBuildingData)
{/*ALCODESTART::1742915722586*/
//Get the total roof surface of the building
double buildingRoofSurface = existingBuilding.p_roofSurfaceArea_m2;

//Building roof surface removal from all earlier connected GC (so excluding the new one!)
int currentAmountOfConnectedGCWithBuilding = existingBuilding.c_containedGridConnections.size();
for(GridConnection earlierConnectedGC : existingBuilding.c_containedGridConnections){
	earlierConnectedGC.p_roofSurfaceArea_m2 -= buildingRoofSurface/currentAmountOfConnectedGCWithBuilding;
	
	if(earlierConnectedGC.p_roofSurfaceArea_m2 < 0){
		new RuntimeException("Negative roofsurface for GC: " + earlierConnectedGC.p_gridConnectionID + " after removal of earlier distributed building roofsurface. This should never be possible!");
	}
}

//Connect new GC to the building now
existingBuilding.c_containedGridConnections.add(connectingGC);
connectingGC.c_connectedGISObjects.add(existingBuilding);


//Adding the newly distributed roof surfaces to the gc (now including the new one!)
int newAmountOfConnectedGCWithBuilding = currentAmountOfConnectedGCWithBuilding + 1;
for(GridConnection connectedGC : existingBuilding.c_containedGridConnections){
	connectedGC.p_roofSurfaceArea_m2 += buildingRoofSurface/newAmountOfConnectedGCWithBuilding;
}

//Also add the new connecting building data address floor surface
existingBuilding.p_floorSurfaceArea_m2 += connectingBuildingData.address_floor_surface_m2();
/*ALCODEEND*/}

double f_addHeatAsset(GridConnection parentGC,OL_GridConnectionHeatingType heatAssetType,double maxHeatOutputPower_kW)
{/*ALCODESTART::1745336570663*/
//Initialize parameters
double inputCapacityElectric_kW;
double efficiency;
double baseTemperature_degC;
double outputTemperature_degC;
OL_AmbientTempType ambientTempType;
double sourceAssetHeatPower_kW;
double belowZeroHeatpumpEtaReductionFactor;

switch (heatAssetType){ // There is always only one heatingType, If there are many assets the type is CUSTOM

	case GAS_BURNER:
		J_EAConversionGasBurner gasBurner = new J_EAConversionGasBurner(parentGC, maxHeatOutputPower_kW , avgc_data.p_avgEfficiencyGasBurner_fr, energyModel.p_timeStep_h, 90);
		break;
	
	case HYBRID_HEATPUMP:
	
		//Add primary heating asset (heatpump) (if its not part of the basic profile already
		inputCapacityElectric_kW = maxHeatOutputPower_kW / 3; //-- /3, kan nog kleiner want is hybride zodat gasbrander ook bij springt, dus kleiner MOETEN aanname voor hoe klein onderzoeken
		efficiency = zero_Interface.energyModel.avgc_data.p_avgEfficiencyHeatpump;
		baseTemperature_degC = zero_Interface.energyModel.v_currentAmbientTemperature_degC;
		outputTemperature_degC = zero_Interface.energyModel.avgc_data.p_avgOutputTemperatureHeatpump_degC;
		ambientTempType = OL_AmbientTempType.AMBIENT_AIR;
		sourceAssetHeatPower_kW = 0;
		belowZeroHeatpumpEtaReductionFactor = 1;
		
		J_EAConversionHeatPump heatPumpHybrid = new J_EAConversionHeatPump(parentGC, inputCapacityElectric_kW, efficiency, energyModel.p_timeStep_h, outputTemperature_degC, baseTemperature_degC, sourceAssetHeatPower_kW, belowZeroHeatpumpEtaReductionFactor, ambientTempType);

		zero_Interface.energyModel.c_ambientDependentAssets.add(heatPumpHybrid);
		
		//Add secondary heating asset (gasburner)
		efficiency = zero_Interface.energyModel.avgc_data.p_avgEfficiencyGasBurner;
		outputTemperature_degC = zero_Interface.energyModel.avgc_data.p_avgOutputTemperatureGasBurner_degC;
		
		J_EAConversionGasBurner gasBurnerHybrid = new J_EAConversionGasBurner(parentGC, maxHeatOutputPower_kW, efficiency, energyModel.p_timeStep_h, outputTemperature_degC);		
		break;
	
	case ELECTRIC_HEATPUMP:
		//Add primary heating asset (heatpump)
		inputCapacityElectric_kW = maxHeatOutputPower_kW; // Could be a lot smaller due to high cop
		efficiency = zero_Interface.energyModel.avgc_data.p_avgEfficiencyHeatpump;
		baseTemperature_degC = zero_Interface.energyModel.v_currentAmbientTemperature_degC;
		outputTemperature_degC = zero_Interface.energyModel.avgc_data.p_avgOutputTemperatureHeatpump_degC;
		ambientTempType = OL_AmbientTempType.AMBIENT_AIR;
		sourceAssetHeatPower_kW = 0;
		belowZeroHeatpumpEtaReductionFactor = 1;
		
		new J_EAConversionHeatPump(parentGC, inputCapacityElectric_kW, efficiency, energyModel.p_timeStep_h, outputTemperature_degC, baseTemperature_degC, sourceAssetHeatPower_kW, belowZeroHeatpumpEtaReductionFactor, ambientTempType );		
		break;

	case GAS_CHP:
		
		double outputCapacityElectric_kW = (maxHeatOutputPower_kW/avgc_data.p_avgEfficiencyCHP_thermal_fr) * avgc_data.p_avgEfficiencyCHP_electric_fr;
		outputTemperature_degC = avgc_data.p_avgOutputTemperatureCHP_degC;
		efficiency = avgc_data.p_avgEfficiencyCHP_thermal_fr + avgc_data.p_avgEfficiencyCHP_electric_fr;
		
		new J_EAConversionGasCHP(parentGC, outputCapacityElectric_kW, maxHeatOutputPower_kW, efficiency, energyModel.p_timeStep_h, outputTemperature_degC );
		break;

	case DISTRICTHEAT:
		
		outputTemperature_degC = avgc_data.p_avgOutputTemperatureDistrictHeatingDeliverySet_degC;
		efficiency = avgc_data.p_avgEfficiencyDistrictHeatingDeliverySet_fr;
		
		new J_EAConversionHeatDeliverySet(parentGC, maxHeatOutputPower_kW, efficiency, energyModel.p_timeStep_h, outputTemperature_degC);
		
		//Add GC to heat grid
		GridNode heatgrid = findFirst(energyModel.pop_gridNodes, node -> node.p_energyCarrier == OL_EnergyCarriers.HEAT);
		if(heatgrid == null){
			heatgrid = f_createHeatGridNode();
		}
		parentGC.p_parentNodeHeatID = heatgrid.p_gridNodeID;	
		break;
		
	case CUSTOM:
		f_addCustomHeatAsset(parentGC, maxHeatOutputPower_kW);
		break;
		
	default:
		traceln("HEATING TYPE NOT FOUND FOR GC: " + parentGC);
}

/*ALCODEEND*/}

GridNode f_createHeatGridNode()
{/*ALCODESTART::1747300761144*/
GridNode GN_heat = energyModel.add_pop_gridNodes();
GN_heat.p_gridNodeID = "Heatgrid";

// Check wether transformer capacity is known or estimated
GN_heat.p_capacity_kW = 1000000;	
GN_heat.p_realCapacityAvailable = false;

// Basic GN information
GN_heat.p_description = "Warmtenet";

/*
//Owner
GN_heat.p_ownerGridOperator = Grid_Operator;
*/

//Define node type
GN_heat.p_nodeType = OL_GridNodeType.HT;
GN_heat.p_energyCarrier = OL_EnergyCarriers.HEAT;

//Define GN location
GN_heat.p_latitude = 0;
GN_heat.p_longitude = 0;
GN_heat.setLatLon(GN_heat.p_latitude, GN_heat.p_longitude);

//Create gis region
/*
GN.gisRegion = zero_Interface.f_createGISObject(f_createGISNodesTokens(GN));
zero_Interface.f_styleGridNodes(GN);
zero_Interface.c_GISNodes.add(GN.gisRegion);
*/

return GN_heat;
/*ALCODEEND*/}

double f_addSliderSolarfarm(String gridNodeID)
{/*ALCODESTART::1747829476305*/
c_solarfarm_data.add(0, Solarfarm_data.builder().
isSliderGC(true).

gc_id("Slider solarfarm").
gc_name("Slider solarfarm").
owner_id("Slider solarfarm owner").
streetname(null).
house_number(null).
house_letter(null).
house_addition(null).
postalcode(null).
city(null).
gridnode_id(gridNodeID).
initially_active(false).

capacity_electric_kw(0.0).
connection_capacity_kw(0.0).
contracted_delivery_capacity_kw(0.0).
contracted_feed_in_capacity_kw(0.0).

latitude(0).
longitude(0).
polygon(null).
build());
/*ALCODEEND*/}

double f_addSliderWindfarm(String gridNodeID)
{/*ALCODESTART::1747829476307*/
c_windfarm_data.add(0, Windfarm_data.builder().
isSliderGC(true).

gc_id("Slider windfarm").
gc_name("Slider windfarm").
owner_id("Slider windfarm owner").
streetname(null).
house_number(null).
house_letter(null).
house_addition(null).
postalcode(null).
city(null).
gridnode_id(gridNodeID).
initially_active(false).

capacity_electric_kw(0.0).
connection_capacity_kw(0.0).
contracted_delivery_capacity_kw(0.0).
contracted_feed_in_capacity_kw(0.0).

latitude(0).
longitude(0).
polygon(null).
build());

/*ALCODEEND*/}

double f_addSliderBattery(String gridNodeID)
{/*ALCODESTART::1747829476311*/
c_battery_data.add(0, Battery_data.builder().
isSliderGC(true).

gc_id("Slider battery").
gc_name("Slider battery").
owner_id("Slider battery owner").
streetname(null).
house_number(null).
house_letter(null).
house_addition(null).
postalcode(null).
city(null).
gridnode_id(gridNodeID).
initially_active(false).

capacity_electric_kw(0.0).
connection_capacity_kw(0.0).
contracted_delivery_capacity_kw(0.0).
contracted_feed_in_capacity_kw(0.0).

storage_capacity_kwh(0.0).
operation_mode(OL_BatteryOperationMode.PEAK_SHAVING_PARENT_NODE).
latitude(0).
longitude(0).
polygon(null).
build());
/*ALCODEEND*/}

double f_initializeSpecificSliderGC()
{/*ALCODESTART::1747830228830*/
//Create slider GC data packages for assetGC that do not have a sliderGC data package yet 
Solarfarm_data sliderSolarfarm_data = findFirst(c_solarfarm_data, sf_data -> sf_data.isSliderGC());
Windfarm_data sliderWindfarm_data = findFirst(c_windfarm_data, wf_data -> wf_data.isSliderGC());
Battery_data sliderBattery_data = findFirst(c_battery_data, bat_data -> bat_data.isSliderGC());

//Get top gridnode id
GridNode_data topGridNode = findFirst(c_gridNode_data, node_data -> node_data.type().equals("HVMV"));
if ( topGridNode == null ) {
	throw new RuntimeException("Unable to find top GridNode of type HVMV to create slider assets.");
}
String topGridNodeID = topGridNode.gridnode_id();


if(sliderSolarfarm_data == null){
	f_addSliderSolarfarm(topGridNodeID);
}
if(sliderWindfarm_data == null){
	f_addSliderWindfarm(topGridNodeID);
}
if(project_data.project_type() == OL_ProjectType.RESIDENTIAL){
	for(GridNode_data nodeData : c_gridNode_data){
		f_addSliderBattery(nodeData.gridnode_id());
	}
}
else{
	if(sliderBattery_data == null){
		f_addSliderBattery(topGridNodeID);
	}
}
/*ALCODEEND*/}

J_ProfilePointer f_createEngineProfile(String profileID,double[] arguments,double[] values)
{/*ALCODESTART::1749125189323*/
TableFunction tf_profile = new TableFunction(arguments, values, TableFunction.InterpolationType.INTERPOLATION_LINEAR, 2, TableFunction.OutOfRangeAction.OUTOFRANGE_REPEAT, 0.0);
J_ProfilePointer profilePointer = new J_ProfilePointer(profileID, tf_profile);	
energyModel.f_addProfile(profilePointer);
return profilePointer;
/*ALCODEEND*/}

double f_setEngineProfiles()
{/*ALCODESTART::1749138089965*/
//Profile Arguments
double[] a_arguments_hr = ListUtil.doubleListToArray(defaultProfiles_data.arguments_hr());

//Weather data
double[] a_ambientTemperatureProfile_degC = ListUtil.doubleListToArray(defaultProfiles_data.ambientTemperatureProfile_degC());
double[] a_PVProductionProfile35DegSouth_fr = ListUtil.doubleListToArray(defaultProfiles_data.PVProductionProfile35DegSouth_fr());
double[] a_PVProductionProfile15DegEastWest_fr = ListUtil.doubleListToArray(defaultProfiles_data.PVProductionProfile15DegEastWest_fr());
double[] a_windProductionProfile_fr = ListUtil.doubleListToArray(defaultProfiles_data.windProductionProfile_fr());

//EPEX data
double[] a_epexProfile_eurpMWh = ListUtil.doubleListToArray(defaultProfiles_data.epexProfile_eurpMWh()); 

//Various demand data
double[] a_defaultHouseElectricityDemandProfile_fr = ListUtil.doubleListToArray(defaultProfiles_data.defaultHouseElectricityDemandProfile_fr());
double[] a_defaultHouseHotWaterDemandProfile_fr = ListUtil.doubleListToArray(defaultProfiles_data.defaultHouseHotWaterDemandProfile_fr());
double[] a_defaultHouseCookingDemandProfile_fr = ListUtil.doubleListToArray(defaultProfiles_data.defaultHouseCookingDemandProfile_fr());
double[] a_defaultOfficeElectricityDemandProfile_fr = ListUtil.doubleListToArray(defaultProfiles_data.defaultOfficeElectricityDemandProfile_fr());
double[] a_defaultBuildingHeatDemandProfile_fr = ListUtil.doubleListToArray(defaultProfiles_data.defaultBuildingHeatDemandProfile_fr());

//Create Weather engine profiles
energyModel.pp_ambientTemperature_degC = f_createEngineProfile("ambient_temperature_degC", a_arguments_hr, a_ambientTemperatureProfile_degC);
energyModel.pp_PVProduction35DegSouth_fr = f_createEngineProfile("pv_production_south_fr", a_arguments_hr, a_PVProductionProfile35DegSouth_fr);
energyModel.pp_PVProduction15DegEastWest_fr = f_createEngineProfile("pv_production_eastwest_fr", a_arguments_hr, a_PVProductionProfile15DegEastWest_fr);
energyModel.pp_windProduction_fr = f_createEngineProfile("wind_production_fr", a_arguments_hr, a_windProductionProfile_fr);

//Create Epex engine profile
energyModel.pp_dayAheadElectricityPricing_eurpMWh = f_createEngineProfile("epex_price_eurpMWh", a_arguments_hr, a_epexProfile_eurpMWh);

//Create Consumption engine profiles:
f_createEngineProfile("default_house_electricity_demand_fr", a_arguments_hr, a_defaultHouseElectricityDemandProfile_fr);
f_createEngineProfile("default_house_hot_water_demand_fr", a_arguments_hr, a_defaultHouseHotWaterDemandProfile_fr);
f_createEngineProfile("default_house_cooking_demand_fr", a_arguments_hr, a_defaultHouseCookingDemandProfile_fr);
f_createEngineProfile("default_office_electricity_demand_fr", a_arguments_hr, a_defaultOfficeElectricityDemandProfile_fr);
f_createEngineProfile("default_building_heat_demand_fr", a_arguments_hr, a_defaultBuildingHeatDemandProfile_fr);


//Create custom engine profiles
for(CustomProfile_data customProfile : c_customProfiles_data){
	f_createEngineProfile(customProfile.customProfileID(), customProfile.getArgumentsArray(), customProfile.getValuesArray());
}
/*ALCODEEND*/}

double f_addGridNodeProfile(GridNode gridnode,double[] profile_data_kWh)
{/*ALCODESTART::1749628581470*/
//Create gridconnection where the profile is attached to
GridConnection GC_GridNode_profile = energyModel.add_pop_gridConnections();

//Set GC id
GC_GridNode_profile.p_gridConnectionID = "GridNode " + gridnode.p_gridNodeID + " profile GC";

//Set gridnode as parent
GC_GridNode_profile.p_parentNodeElectricID = gridnode.p_gridNodeID;

//Set capacity same as gridnode
GC_GridNode_profile.v_liveConnectionMetaData.physicalCapacity_kW = gridnode.p_capacity_kW;
GC_GridNode_profile.v_liveConnectionMetaData.contractedDeliveryCapacity_kW = gridnode.p_capacity_kW;
GC_GridNode_profile.v_liveConnectionMetaData.contractedFeedinCapacity_kW = gridnode.p_capacity_kW;

GC_GridNode_profile.v_liveConnectionMetaData.contractedDeliveryCapacityKnown = false;
GC_GridNode_profile.v_liveConnectionMetaData.contractedFeedinCapacityKnown = false;
GC_GridNode_profile.v_liveConnectionMetaData.physicalCapacityKnown = false;

//Set lat lon same as gridnode
GC_GridNode_profile.p_latitude = gridnode.p_latitude; // Get latitude of first building (only used to get nearest trafo)
GC_GridNode_profile.p_longitude = gridnode.p_longitude; // Get longitude of first building (only used to get nearest trafo)

if(project_data.gridnode_profile_timestep_hr() == null){
	new RuntimeException("Trying to load in gridnode profiles, without specifying the timestep of the data in the project_data");
}

double profileTimestep_hr = project_data.gridnode_profile_timestep_hr();

//Add profile to the GC
J_EAProfile profile = new J_EAProfile(GC_GridNode_profile, OL_EnergyCarriers.ELECTRICITY, profile_data_kWh, OL_ProfileAssetType.ELECTRICITYBASELOAD, profileTimestep_hr);	
profile.setStartTime_h(v_simStartHour_h);
profile.energyAssetName = "GridNode " + gridnode.p_gridNodeID + " profile";

//Set boolean has profile data true
gridnode.p_hasProfileData = true;
c_gridNodeIDsWithProfiles.add(gridnode.p_gridNodeID);
/*ALCODEEND*/}

J_ChargingSession f_createChargingSession(String chargingSessionData)
{/*ALCODESTART::1749648772203*/
String[] chargingSessionInfo = chargingSessionData.split("/"); 

int startIndex = Integer.parseInt(chargingSessionInfo[0]);
int endIndex = Integer.parseInt(chargingSessionInfo[1]);
double chargingDemand_kWh = Double.parseDouble(chargingSessionInfo[2]);
double batteryCap_kWh = Double.parseDouble(chargingSessionInfo[3]);
double chargingPower_kW = Double.parseDouble(chargingSessionInfo[5]);
int socket = Integer.parseInt(chargingSessionInfo[6]);

return new J_ChargingSession(startIndex, endIndex, chargingDemand_kWh, batteryCap_kWh, chargingPower_kW, socket, 0.25);
/*ALCODEEND*/}

List<J_ChargingSession>  f_createNewChargerProfile(ChargerProfile_data chargerProfileData)
{/*ALCODESTART::1749649169603*/
// example: 2/54/50.3/72.1/21.8/10.8/2
List<String> chargerProfileDataValues = chargerProfileData.valuesList();
List<J_ChargingSession> chargerProfile = new ArrayList<J_ChargingSession>();		

for(int i = 0; i < chargerProfileDataValues.size(); i++){
	chargerProfile.add(f_createChargingSession(chargerProfileDataValues.get(i)));
}

return chargerProfile;
/*ALCODEEND*/}

List<J_ChargingSession>  f_getChargerProfile()
{/*ALCODESTART::1749649390125*/
List<J_ChargingSession> chargerProfile;
int randomIndex;

if(c_chargerProfiles_data.size()>0){
	randomIndex = uniform_discr(0, c_chargerProfiles_data.size() - 1);
	chargerProfile = f_createNewChargerProfile(c_chargerProfiles_data.get(randomIndex));
	c_chargerProfiles_data.remove(randomIndex);
	energyModel.c_chargerProfiles.add(chargerProfile);
}
else{
	randomIndex = uniform_discr(0, energyModel.c_chargerProfiles.size() - 1);
	chargerProfile = energyModel.c_chargerProfiles.get(randomIndex);
}

return chargerProfile;
/*ALCODEEND*/}

double f_addCookingAsset(GCHouse gc,OL_EnergyAssetType CookingType,double yearlyCookingDemand_kWh)
{/*ALCODESTART::1749726189312*/
switch(CookingType){

	case ELECTRIC_HOB:
		new J_EAConsumption(gc, OL_EnergyAssetType.ELECTRIC_HOB, "default_house_cooking_demand_fr", yearlyCookingDemand_kWh, OL_EnergyCarriers.ELECTRICITY, energyModel.p_timeStep_h, null);
		gc.p_cookingMethod = OL_HouseholdCookingMethod.ELECTRIC;
		traceln("Initial conditions of cooking has Electric cooking. TODO make sure the v_nbHousesWIthECooking is set correctly in electricity tab");
		break;
		
	case GAS_PIT:
		new J_EAConsumption(gc, OL_EnergyAssetType.GAS_PIT, "default_house_cooking_demand_fr", yearlyCookingDemand_kWh, OL_EnergyCarriers.METHANE, energyModel.p_timeStep_h, null);
		gc.p_cookingMethod = OL_HouseholdCookingMethod.GAS;
		break;
}
/*ALCODEEND*/}

double f_addHotWaterDemand(GCHouse houseGC,double surface_m2)
{/*ALCODESTART::1749726279652*/
int aantalBewoners;
if( surface_m2 > 150){
	aantalBewoners = uniform_discr(2,6);
}
else if (surface_m2 > 50){
	aantalBewoners = uniform_discr(1,4);
}
else {
	aantalBewoners = uniform_discr(1,2);
}
 
double yearlyHWD_kWh = aantalBewoners * 600;  //12 * surface_m2 * 3 ; Tamelijk willekeurige formule om HWD te schalen tussen 600 - 2400 kWh bij 50m2 tot 200m2, voor een quickfix

//TEST
yearlyHWD_kWh += 0;

J_EAConsumption hotwaterDemand = new J_EAConsumption( houseGC, OL_EnergyAssetType.HOT_WATER_CONSUMPTION, "default_house_hot_water_demand_fr", yearlyHWD_kWh, OL_EnergyCarriers.HEAT, energyModel.p_timeStep_h, null);

//traceln("yearlyHWD_kWh "+ yearlyHWD_kWh);
if( surface_m2 > 200){
	//traceln("House created with " + surface_m2 + "m2 surace area, will have large hot water demand");
}
if (surface_m2 < 25){
	//traceln("House created with " + surface_m2 + "m2 surace area, will have low hot water demand");
}
/*ALCODEEND*/}

double f_addBuildingHeatModel(GridConnection parentGC,double floorArea_m2)
{/*ALCODESTART::1749727623536*/
double maxPowerHeat_kW = 1000; 				//Dit is hoeveel vermogen het huis kan afgeven/opnemen, mag willekeurige waarden hebben. Wordt alleen gebruikt in rekenstap van ratio of capacity
double lossfactor_WpK; 						//Dit is wat bepaalt hoeveel warmte het huis verliest/opneemt per tijdstap per delta_T
double initialTemp = uniform_discr(15,22); 	//starttemperatuur
double heatCapacity_JpK; 					//hoeveel lucht zit er in je huis dat je moet verwarmen?
double solarAbsorptionFactor_m2; 	//hoeveel m2 effectieve dak en muur oppervlakte er is dat opwarmt door zonneinstraling
 
switch (parentGC.p_energyLabel){
	case A:
		lossfactor_WpK = 0.35 * floorArea_m2;
	break;
	case B:
		lossfactor_WpK = 0.45 * floorArea_m2;
	break;
	case C:
		lossfactor_WpK = 0.65 * floorArea_m2;
	break;
	case D:
		lossfactor_WpK = 0.85 * floorArea_m2;
	break;
	default:
		lossfactor_WpK = uniform (0.4, 1.2) * floorArea_m2;
	break;
}

lossfactor_WpK = roundToDecimal(lossfactor_WpK,2);
solarAbsorptionFactor_m2 = floorArea_m2 * 0.1; //solar irradiance [W/m2]
 
heatCapacity_JpK = floorArea_m2 * 50000;
 
 
parentGC.p_BuildingThermalAsset = new J_EABuilding( parentGC, maxPowerHeat_kW, lossfactor_WpK, energyModel.p_timeStep_h, initialTemp, heatCapacity_JpK, solarAbsorptionFactor_m2 );
energyModel.c_ambientDependentAssets.add( parentGC.p_BuildingThermalAsset );
 
double delayHeatReleaseInteriorHeatsink_hr = 0;
double lossToExteriorFromInteriorHeatSink_fr;
if(randomTrue(0.2)){
	delayHeatReleaseInteriorHeatsink_hr = 3;
}
else {
	delayHeatReleaseInteriorHeatsink_hr = 0.5;
}

parentGC.p_BuildingThermalAsset.addInteriorHeatBuffer(delayHeatReleaseInteriorHeatsink_hr);

double delayHeatReleaseRoofAndWall_hr = 8.0;
parentGC.p_BuildingThermalAsset.addExteriorHeatBuffer(delayHeatReleaseRoofAndWall_hr);

/*ALCODEEND*/}

List<Building_data> f_getBuildingsInSubScope(List<Building_data> initialBuildingList)
{/*ALCODESTART::1749728889982*/
List<Building_data> scopedBuildingList = new ArrayList<>();

if(settings.subscopesToSimulate() == null || settings.subscopesToSimulate().size() == 0){
	scopedBuildingList.addAll(initialBuildingList);
}
else{
	for (Building_data dataBuilding : initialBuildingList) {
		for (int i = 0; i < c_gridNodeIDsInScope.size() ; i++){
			if (dataBuilding.gridnode_id().equals( c_gridNodeIDsInScope.get(i))){
				scopedBuildingList.add(dataBuilding);
			}
		}	
	}
}
return scopedBuildingList;
/*ALCODEEND*/}

double f_createHouses()
{/*ALCODESTART::1749728889984*/
List<Building_data> buildingDataHouses = f_getBuildingsInSubScope(c_houseBuilding_data);

traceln("Aantal panden met woonfunctie in BAG data: " + buildingDataHouses.size());

int i = 0;

//Add houses to the legend if in model
if(buildingDataHouses.size()>0){
	zero_Interface.c_modelActiveDefaultGISBuildings.add(OL_GISBuildingTypes.HOUSE);
}

for (Building_data houseBuildingData : buildingDataHouses) {
	GCHouse GCH = energyModel.add_Houses();
	ConnectionOwner	COH = energyModel.add_pop_connectionOwners();
	
	//Set parameters for the Grid Connection
 	GCH.p_gridConnectionID = houseBuildingData.address_id();
	GCH.p_ownerID = "Woonhuis" + Integer.toString(i);	//aanname : huiseigenaar is eigenbaas

	GCH.p_purposeBAG = houseBuildingData.purpose();
	
	//pand gegevens
	//GCH.p_heatingType = avgc_data.p_avgHouseHeatingMethod ;
	GCH.p_bouwjaar = houseBuildingData.build_year();
	GCH.p_eigenOprit = houseBuildingData.has_private_parking() != null ? houseBuildingData.has_private_parking() : false;
	
	//Nageisoleerd
	if (houseBuildingData.energy_label() != null) {
		GCH.p_energyLabel = houseBuildingData.energy_label();
	}
	else {
		if (GCH.p_bouwjaar < 1980) {
			GCH.p_energyLabel = OL_GridConnectionIsolationLabel.D;
		}
		else if (GCH.p_bouwjaar < 2000) {
			GCH.p_energyLabel = OL_GridConnectionIsolationLabel.C;
		}
		else if (GCH.p_bouwjaar < 2015) {
			GCH.p_energyLabel = OL_GridConnectionIsolationLabel.B;
		}
		else {
			GCH.p_energyLabel = OL_GridConnectionIsolationLabel.A;
		}
	}
	//aansluiting gegevens
	GCH.v_liveConnectionMetaData.physicalCapacity_kW = avgc_data.p_avgHouseConnectionCapacity_kW;
	GCH.v_liveConnectionMetaData.contractedDeliveryCapacity_kW = avgc_data.p_avgHouseConnectionCapacity_kW;
	GCH.v_liveConnectionMetaData.contractedFeedinCapacity_kW = avgc_data.p_avgHouseConnectionCapacity_kW;
	
	GCH.v_liveConnectionMetaData.physicalCapacityKnown = false;
	GCH.v_liveConnectionMetaData.contractedDeliveryCapacityKnown = false;
	GCH.v_liveConnectionMetaData.contractedFeedinCapacityKnown = false;
	
	// Address data
	GCH.p_address = new J_Address();
 	GCH.p_address.setStreetName( houseBuildingData.streetname());						 	
 	if (houseBuildingData.house_number() == null) {
 		GCH.p_address.setHouseNumber( 0 );
 	} else {
 		GCH.p_address.setHouseNumber( houseBuildingData.house_number()); 
 	}
 	GCH.p_address.setHouseLetter( houseBuildingData.house_letter());
 	GCH.p_address.setHouseAddition( houseBuildingData.house_addition());
 	GCH.p_address.setPostalcode( houseBuildingData.postalcode());					 	
 	GCH.p_address.setCity( houseBuildingData.city());

	//locatie
	GCH.p_longitude = houseBuildingData.longitude();
	GCH.p_latitude = houseBuildingData.latitude();
	GCH.setLatLon(GCH.p_latitude, GCH.p_longitude);
	
	//Connect GC to grid node
	GCH.p_parentNodeElectricID = houseBuildingData.gridnode_id();
		
	//Set parameters for the Actor: ConnectionOwner
	COH.p_actorID = GCH.p_ownerID;
	COH.p_actorType = OL_ActorType.CONNECTIONOWNER;
	COH.p_connectionOwnerType = OL_ConnectionOwnerType.HOUSEHOLD;
	COH.p_detailedCompany = false;
	GCH.p_owner = COH;
	
	
	//Check wheter this building already exists
	GIS_Building existingBuilding = findFirst(energyModel.pop_GIS_Buildings, gisBuilding -> gisBuilding.p_id.equals(houseBuildingData.building_id()));
	
	if(existingBuilding == null){//Create new GIS building and connect
		GIS_Building b = f_createGISBuilding( houseBuildingData, GCH );
		GCH.p_roofSurfaceArea_m2 = houseBuildingData.polygon_area_m2();

		//Style building
		b.p_defaultFillColor = zero_Interface.v_houseBuildingColor;
		b.p_defaultLineColor = zero_Interface.v_houseBuildingLineColor;
		zero_Interface.f_styleAreas(b);
	}
	else{// Connect with existing building
		f_connectGCToExistingBuilding(GCH, existingBuilding, houseBuildingData);
	}
	
	//Floor surface of GC
	GCH.p_floorSurfaceArea_m2 = houseBuildingData.address_floor_surface_m2();
	
	//Instantiate energy assets
	double jaarlijksElectriciteitsVerbruik;
	double jaarlijksGasVerbruik;
	try {
		jaarlijksElectriciteitsVerbruik = houseBuildingData.electricity_consumption_kwhpa();
	}
	catch (NullPointerException e){
		jaarlijksElectriciteitsVerbruik = Double.valueOf(uniform_discr(1200, 3800));
	}
	try {
		jaarlijksGasVerbruik = houseBuildingData.gas_consumption_kwhpa();
	}
	catch (NullPointerException e){
		jaarlijksGasVerbruik =  Double.valueOf(uniform_discr(600, 2000));
	}
	
	//GCH.p_initialPVpanels = houseBuildingData.pv_default();
	GCH.v_liveAssetsMetaData.initialPV_kW = houseBuildingData.pv_installed_kwp() != null ? houseBuildingData.pv_installed_kwp() : 0;
	GCH.v_liveAssetsMetaData.PVPotential_kW = GCH.v_liveAssetsMetaData.initialPV_kW > 0 ? GCH.v_liveAssetsMetaData.initialPV_kW : houseBuildingData.pv_potential_kwp(); // To prevent sliders from changing outcomes
	f_setHouseHeatingPreferences(GCH);
	// TODO: Above we load in data of gas use, but the houses always have a thermal model??
	f_addEnergyAssetsToHouses(GCH, jaarlijksElectriciteitsVerbruik );	
	
	i ++;
}

//Backup for when pv_potential kWp is null, needs to be after all houses have been made, so rooftop surface is distributed correctly
for(GCHouse GCH : energyModel.Houses){
	if(GCH.v_liveAssetsMetaData.PVPotential_kW == null){
		GCH.v_liveAssetsMetaData.PVPotential_kW = GCH.p_roofSurfaceArea_m2*avgc_data.p_avgRatioRoofPotentialPV*avgc_data.p_avgPVPower_kWpm2;
	}
}
	

/*ALCODEEND*/}

double f_addEnergyAssetsToHouses(GCHouse house,double jaarlijksElectriciteitsVerbruik)
{/*ALCODESTART::1749728889986*/
//Add generic electricity demand profile 
GridNode gn = randomWhere(energyModel.pop_gridNodes, x -> x.p_gridNodeID.equals( house.p_parentNodeElectricID));
if ( ! gn.p_hasProfileData ){
	f_addElectricityDemandProfile(house, jaarlijksElectriciteitsVerbruik, null, false, "default_house_electricity_demand_fr");
}

//Add building heat model and asset
f_addBuildingHeatModel(house, house.p_floorSurfaceArea_m2);
		
//house.p_heatingType = OL_GridConnectionHeatingType.GASBURNER;
double gasBurnerCapacity_kW = 50000;//40;//uniform_discr(3,5); 

//Add heat demand profile
OL_GridConnectionHeatingType heatingType = avgc_data.p_avgHouseHeatingMethod;
f_addHeatAsset(house, heatingType, gasBurnerCapacity_kW);
f_addHeatManagement(house, heatingType, false);

//Add hot water and cooking demand
f_addHotWaterDemand(house, house.p_floorSurfaceArea_m2);
double yearlyCookingDemand_kWh = uniform_discr(200,600);
f_addCookingAsset(house, OL_EnergyAssetType.GAS_PIT, yearlyCookingDemand_kWh);


//Add pv
double installedRooftopSolar_kW = house.v_liveAssetsMetaData.initialPV_kW != null ? house.v_liveAssetsMetaData.initialPV_kW : 0;
if (gn.p_hasProfileData){ //dont count production if there is measured data on Node
	installedRooftopSolar_kW = 0;
}

if (installedRooftopSolar_kW > 0) {
	f_addEnergyProduction(house, OL_EnergyAssetType.PHOTOVOLTAIC, "Residential Solar", installedRooftopSolar_kW );
}

//Oprit?
if( house.p_eigenOprit){
	if (randomTrue( 0.08)){
		f_addElectricVehicle(house, OL_EnergyAssetType.ELECTRIC_VEHICLE, true, 0, 0);
	}
	else{
		f_addDieselVehicle(house, OL_EnergyAssetType.DIESEL_VEHICLE, true, 0);
	}
}
else {
	f_addDieselVehicle(house, OL_EnergyAssetType.DIESEL_VEHICLE, true, 0);
}
/*ALCODEEND*/}

double f_setHouseHeatingPreferences(GCHouse house)
{/*ALCODESTART::1749728889988*/
if( randomTrue(0.5) ){ //50% kans op ochtend ritme
	house.v_nightTempSetpoint_degC = uniform_discr(12,18);
	house.v_dayTempSetpoint_degC = uniform_discr(18, 24);
	house.v_heatingOn_time = uniform_discr(5,10) + uniform_discr(0,4) / 4.0;
	house.v_heatingOff_time = uniform_discr(21,23);
	house.p_heatingKickinTreshold_degC = roundToDecimal(uniform(0,1),1);
}
else if (randomTrue(0.5) ){ // 25% kans op hele dag aan
	house.v_nightTempSetpoint_degC = uniform_discr(18,21);
	house.v_dayTempSetpoint_degC = house.v_nightTempSetpoint_degC;
	house.v_heatingOn_time = -1;
	house.v_heatingOff_time = 25;
	house.p_heatingKickinTreshold_degC = roundToDecimal(uniform(0, 1),1);
}
else { // 25% kans op smiddags/savonds aan
	house.v_nightTempSetpoint_degC = uniform_discr(12,18);
	house.v_dayTempSetpoint_degC = uniform_discr(18, 24);
	house.v_heatingOn_time = uniform_discr(14, 16) + uniform_discr(0,4) / 4.0;
	house.v_heatingOff_time = uniform_discr(21,23);
	house.p_heatingKickinTreshold_degC = roundToDecimal(uniform(0, 1),1);
}
/*ALCODEEND*/}

double f_createParkingSpots()
{/*ALCODESTART::1749729268458*/
List<GCEnergyProduction> carportGCList = new ArrayList<GCEnergyProduction>();

for (ParkingSpace_data dataParkingSpace : f_getParkingSpacesInSubScope(c_parkingSpace_data)){

	//Create parking gis object	
	GIS_Object parkingSpace = f_createGISObject(dataParkingSpace.parking_id(), dataParkingSpace.latitude(), dataParkingSpace.longitude(), dataParkingSpace.polygon(), OL_GISObjectType.PARKING);
	String parkingSpaceType = dataParkingSpace.type().toString().substring(0, 1).toUpperCase() + dataParkingSpace.type().toString().substring(1).toLowerCase();
	parkingSpace.p_annotation = "Parkeerplek: " + parkingSpaceType + ", " + dataParkingSpace.additional_info();
	
	//Set correct color and legend collection based on parking type
	switch(dataParkingSpace.type()){
		case PRIVATE:
		case DISABLED:
		case KISS_AND_RIDE:
			parkingSpace.p_defaultFillColor = zero_Interface.v_parkingSpaceColor_private;
			parkingSpace.p_defaultLineColor = zero_Interface.v_parkingSpaceLineColor_private;
			zero_Interface.c_modelActiveParkingSpaceTypes.add(OL_ParkingSpaceType.PRIVATE);
			break;
		case PUBLIC:
			parkingSpace.p_defaultFillColor = zero_Interface.v_parkingSpaceColor_public;
			parkingSpace.p_defaultLineColor = zero_Interface.v_parkingSpaceLineColor_public;
			zero_Interface.c_modelActiveParkingSpaceTypes.add(OL_ParkingSpaceType.PUBLIC);
			break;
		case ELECTRIC:
			parkingSpace.p_defaultFillColor = zero_Interface.v_parkingSpaceColor_electric;
			parkingSpace.p_defaultLineColor = zero_Interface.v_parkingSpaceLineColor_electric;
			zero_Interface.c_modelActiveParkingSpaceTypes.add(OL_ParkingSpaceType.ELECTRIC);		
			break;
	}
	
	//Add to ordered collection on the interface
	zero_Interface.c_orderedParkingSpaces.add(parkingSpace);

	//Style gis object
	parkingSpace.f_style(null, null, null, null);	
	
	//Get energyProduction GC	
	GCEnergyProduction carportGC = findFirst(carportGCList, gc -> gc.p_parentNodeElectricID.equals(dataParkingSpace.gridnode_id()));
	
	if(carportGC == null){ // If non existend -> Create one.
		carportGC = energyModel.add_EnergyProductionSites();
		carportGC.p_gridConnectionID = "Parking space gridconnection: " + dataParkingSpace.parking_id();
		carportGC.v_liveConnectionMetaData.physicalCapacity_kW = dataParkingSpace.pv_potential_kwp();
		carportGC.v_liveConnectionMetaData.contractedDeliveryCapacity_kW = 0.0;
		carportGC.v_liveConnectionMetaData.contractedFeedinCapacity_kW = dataParkingSpace.pv_potential_kwp();
		
		carportGC.v_liveConnectionMetaData.physicalCapacityKnown = false;
		carportGC.v_liveConnectionMetaData.contractedDeliveryCapacityKnown = false;
		carportGC.v_liveConnectionMetaData.contractedFeedinCapacityKnown = false;
		
		carportGC.p_parentNodeElectricID = dataParkingSpace.gridnode_id();

		carportGC.p_latitude = dataParkingSpace.latitude();		
		carportGC.p_longitude = dataParkingSpace.longitude();
		
		//Address
		carportGC.p_address = new J_Address();
		carportGC.p_address.setStreetName(dataParkingSpace.street());
		
		//CO
		ConnectionOwner COC = energyModel.add_pop_connectionOwners(); // Create Connection owner company
			
		COC.p_actorID = "Parking space connection owner: " + dataParkingSpace.parking_id();
		COC.p_actorType = OL_ActorType.CONNECTIONOWNER;
		COC.p_connectionOwnerType = OL_ConnectionOwnerType.PARKINGSPACE_OP;
		COC.p_detailedCompany = false;
		COC.b_dataSharingAgreed = true;
		
		carportGC.p_owner = COC;
		carportGC.p_ownerID = COC.p_actorID;
		
		//Add to collections
		parkingSpace.c_containedGridConnections.add(carportGC);
		carportGC.c_connectedGISObjects.add(parkingSpace);
		carportGCList.add(carportGC);
	}
	else{
		carportGC.v_liveConnectionMetaData.physicalCapacity_kW += dataParkingSpace.pv_potential_kwp();
		carportGC.v_liveConnectionMetaData.contractedFeedinCapacity_kW += dataParkingSpace.pv_potential_kwp();
		
		//Add to collections
		parkingSpace.c_containedGridConnections.add(carportGC);
		carportGC.c_connectedGISObjects.add(parkingSpace);
	}
	
	//Update pv potential of carport energy production site
	carportGC.v_liveAssetsMetaData.PVPotential_kW += dataParkingSpace.pv_potential_kwp() != null ? dataParkingSpace.pv_potential_kwp() : 0;
}
/*ALCODEEND*/}

List<Solarfarm_data> f_getSolarfarmsInSubScope(List<Solarfarm_data> initialSolarfarmsList)
{/*ALCODESTART::1749739602491*/
List<Solarfarm_data> scopedSolarfarmsList = new ArrayList<>();

if(settings.subscopesToSimulate() == null || settings.subscopesToSimulate().size() == 0){
	scopedSolarfarmsList.addAll(initialSolarfarmsList);
}
else{
	for (Solarfarm_data dataSolarfarm : initialSolarfarmsList) {
		for (int i = 0; i < c_gridNodeIDsInScope.size() ; i++){
			if (dataSolarfarm.gridnode_id().equals( c_gridNodeIDsInScope.get(i))){
				scopedSolarfarmsList.add(dataSolarfarm);
			}
		}	
	}
}
return scopedSolarfarmsList;
/*ALCODEEND*/}

List<Windfarm_data> f_getWindfarmsInSubScope(List<Windfarm_data> initialWindfarmsList)
{/*ALCODESTART::1750857080998*/
List<Windfarm_data> scopedWindfarmsList = new ArrayList<>();

if(settings.subscopesToSimulate() == null || settings.subscopesToSimulate().size() == 0){
	scopedWindfarmsList.addAll(initialWindfarmsList);
}
else{
	for (Windfarm_data dataWindfarm : initialWindfarmsList) {
		for (int i = 0; i < c_gridNodeIDsInScope.size() ; i++){
			if (dataWindfarm.gridnode_id().equals( c_gridNodeIDsInScope.get(i))){
				scopedWindfarmsList.add(dataWindfarm);
			}
		}	
	}
}
return scopedWindfarmsList;
/*ALCODEEND*/}

List<Chargingstation_data> f_getChargingstationsInSubScope(List<Chargingstation_data> initialChargingstationsList)
{/*ALCODESTART::1750857082460*/
List<Chargingstation_data> scopedChargingstationsList = new ArrayList<>();

if(settings.subscopesToSimulate() == null || settings.subscopesToSimulate().size() == 0){
	scopedChargingstationsList.addAll(initialChargingstationsList);
}
else{
	for (Chargingstation_data dataChargingstation : initialChargingstationsList) {
		for (int i = 0; i < c_gridNodeIDsInScope.size() ; i++){
			if (dataChargingstation.gridnode_id().equals( c_gridNodeIDsInScope.get(i))){
				scopedChargingstationsList.add(dataChargingstation);
			}
		}	
	}
}
return scopedChargingstationsList;
/*ALCODEEND*/}

List<Electrolyser_data> f_getElectrolysersInSubScope(List<Electrolyser_data> initialElectrolysersList)
{/*ALCODESTART::1750857083468*/
List<Electrolyser_data> scopedElectrolysersList = new ArrayList<>();

if(settings.subscopesToSimulate() == null || settings.subscopesToSimulate().size() == 0){
	scopedElectrolysersList.addAll(initialElectrolysersList);
}
else{
	for (Electrolyser_data dataElectrolyser : initialElectrolysersList) {
		for (int i = 0; i < c_gridNodeIDsInScope.size() ; i++){
			if (dataElectrolyser.gridnode_id().equals( c_gridNodeIDsInScope.get(i))){
				scopedElectrolysersList.add(dataElectrolyser);
			}
		}	
	}
}
return scopedElectrolysersList;
/*ALCODEEND*/}

List<ParkingSpace_data> f_getParkingSpacesInSubScope(List<ParkingSpace_data> initialParkingSpaceList)
{/*ALCODESTART::1750857084547*/
List<ParkingSpace_data> scopedParkingSpacesList = new ArrayList<>();

if(settings.subscopesToSimulate() == null || settings.subscopesToSimulate().size() == 0){
	scopedParkingSpacesList.addAll(initialParkingSpaceList);
}
else{
	for (ParkingSpace_data dataParkingSpace : initialParkingSpaceList) {
		for (int i = 0; i < c_gridNodeIDsInScope.size() ; i++){
			if (dataParkingSpace.gridnode_id().equals( c_gridNodeIDsInScope.get(i))){
				scopedParkingSpacesList.add(dataParkingSpace);
			}
		}	
	}
}
return scopedParkingSpacesList;
/*ALCODEEND*/}

List<Battery_data> f_getBatteriesInSubScope(List<Battery_data> initialBatteriesList)
{/*ALCODESTART::1750861476829*/
List<Battery_data> scopedBatteriesList = new ArrayList<>();

if(settings.subscopesToSimulate() == null || settings.subscopesToSimulate().size() == 0){
	scopedBatteriesList.addAll(initialBatteriesList);
}
else{
	for (Battery_data dataBattery : initialBatteriesList) {
		for (int i = 0; i < c_gridNodeIDsInScope.size() ; i++){
			if (dataBattery.gridnode_id().equals( c_gridNodeIDsInScope.get(i))){
				scopedBatteriesList.add(dataBattery);
			}
		}	
	}
}
return scopedBatteriesList;
/*ALCODEEND*/}

List<Building_data> f_getSurveyGCBuildingData(GridConnection companyGC,com.zenmo.zummon.companysurvey.GridConnection vallumGC)
{/*ALCODESTART::1752239414416*/
List<Building_data> connectedBuildingsData = new ArrayList<Building_data>();

if ( vallumGC.getPandIds() != null && !vallumGC.getPandIds().isEmpty()) {
	for (var PID : vallumGC.getPandIds() ) {
		List<Building_data> buildingsDataSameID = findAll(c_companyBuilding_data, b -> b.building_id().equals(PID.getValue()));
		Building_data connectedBuildingData = null;
		if(buildingsDataSameID.size() == 1){ // Only one building package with same id, so this building package belongs to this GC
			connectedBuildingData = buildingsDataSameID.get(0);
		}
		else{//Multiple building packages with this building id -> Find the right one based on address, if none are found: pick a package without address
			connectedBuildingData = findFirst(buildingsDataSameID, buildingData -> buildingData.house_number() != null && buildingData.house_number() == companyGC.p_address.getHouseNumber());
			if(connectedBuildingData == null){ //If no matching house numbers, find first object that has no house number. 
				connectedBuildingData = findFirst(buildingsDataSameID, buildingData -> buildingData.house_number() == null);
			}
		}
		
		if (connectedBuildingData != null) {
		    // Remove from company building data and add to survey
		    c_companyBuilding_data.remove(connectedBuildingData);
		    c_surveyCompanyBuilding_data.add(connectedBuildingData);
		    // Set trafo ID
		    companyGC.p_parentNodeElectricID = connectedBuildingData.gridnode_id();
		}
		else if (map_buildingData_Vallum != null && !map_buildingData_Vallum.isEmpty()) {
		        // Create new building package
		        connectedBuildingData = f_createBuildingData_Vallum(companyGC, PID.getValue());
		        c_vallumBuilding_data.add(connectedBuildingData);
		}
		
		if (connectedBuildingData != null) {
			connectedBuildingsData.add(connectedBuildingData);
		}
	}
} 
else {// No building connected in zorm? -> check if it is manually connected in excel (using gc_id column)
	connectedBuildingsData = findAll(c_companyBuilding_data, b -> b.gc_id() != null && b.gc_id().equals(companyGC.p_gridConnectionID));
	if(connectedBuildingsData == null){
		traceln("GC %s has no building in zorm and also no manual connection with building in excel", companyGC.p_gridConnectionID);
	}
	else{
		c_companyBuilding_data.removeAll(connectedBuildingsData);
	}
}

return connectedBuildingsData;
/*ALCODEEND*/}

double f_addHeatManagement(GridConnection engineGC,OL_GridConnectionHeatingType heatingType,boolean isGhost)
{/*ALCODESTART::1753784800216*/
if (isGhost) {
	engineGC.p_heatingManagement = new J_HeatingManagementGhost( engineGC, heatingType );
	return;
}
else {
	engineGC.f_addHeatManagementToGC(engineGC, heatingType, isGhost);
}
/*ALCODEEND*/}

J_ProfilePointer f_createEngineProfile1(String profileID,double[] arguments,double[] values,EnergyModel energyModel)
{/*ALCODESTART::1753349205424*/
TableFunction tf_profile = new TableFunction(arguments, values, TableFunction.InterpolationType.INTERPOLATION_LINEAR, 2, TableFunction.OutOfRangeAction.OUTOFRANGE_REPEAT, 0.0);
J_ProfilePointer profilePointer;
if (energyModel.f_findProfile(profileID)!=null) {
	profilePointer=energyModel.f_findProfile(profileID);
	profilePointer.setTableFunction(tf_profile);
} else {
	profilePointer = new J_ProfilePointer(profileID, tf_profile);	
	energyModel.f_addProfile(profilePointer);
}
return profilePointer;
/*ALCODEEND*/}

double f_setEngineInputDataAfterDeserialisation(EnergyModel deserializedEnergyModel)
{/*ALCODESTART::1753349205426*/
deserializedEnergyModel.p_truckTripsCsv = inputCSVtruckTrips;
deserializedEnergyModel.p_householdTripsCsv = inputCSVhouseholdTrips;
deserializedEnergyModel.p_cookingPatternCsv = inputCSVcookingActivities;
deserializedEnergyModel.avgc_data = energyModel.avgc_data;
/*ALCODEEND*/}

OL_GridConnectionHeatingType f_heatingSurveyCompany(GridConnection engineGC,com.zenmo.zummon.companysurvey.GridConnection surveyGC)
{/*ALCODESTART::1753799111185*/
// Set heatingType
OL_GridConnectionHeatingType heatingType = f_getHeatingTypeSurvey(engineGC, surveyGC);

if(heatingType == OL_GridConnectionHeatingType.CUSTOM){
	f_addCustomHeatingSetup(engineGC, surveyGC);
}
else{
	// Create building profiles, peakHeatConsumption_kW is null if there is no heat consumption
	Double peakHeatConsumption_kW = f_createSurveyHeatProfiles( engineGC, surveyGC, heatingType );
	
	// Create EA conversions
	if (peakHeatConsumption_kW != null) {
		f_addHeatAsset(engineGC, heatingType, peakHeatConsumption_kW);
	}
	
	if (surveyGC.getStorage() != null && surveyGC.getStorage().getHasThermalStorage() != null) {
		//if (surveyGC.getStorage().getThermalStorageKw() != null) {
			//double storagePower_kW = surveyGC.getStorage().getThermalStorageKw();
		//}
		// TODO: find a way to determine the storage capacity 
		// f_addStorage(parentGC, storagePower_kw, storageCapacity_kWh, storageType);
	}
	
	
	// Heating management (needs: heatingType & assets such as building thermal model or profiles, survey companies never have a thermal building mdoel)
	boolean isGhost = heatingType != OL_GridConnectionHeatingType.NONE && peakHeatConsumption_kW == null;
	
	//Add heating management
	f_addHeatManagement(engineGC, heatingType, isGhost);
}

return heatingType;
/*ALCODEEND*/}

Double f_createSurveyHeatProfiles(GridConnection engineGC,com.zenmo.zummon.companysurvey.GridConnection surveyGC,OL_GridConnectionHeatingType heatingType)
{/*ALCODESTART::1753801098736*/
////Gas and Heating
if (surveyGC.getNaturalGas().getHasConnection() != null && surveyGC.getNaturalGas().getHasConnection() ) {
	switch (heatingType) {
		case HYBRID_HEATPUMP:
			// Exception for hybrid heatpumps, when it will be a ghost asset make gas profile
			if (!settings.createCurrentElectricityEA() && (engineGC.v_hasQuarterHourlyValues || f_surveyHasGasData(surveyGC)) ) {
				f_createGasProfileFromSurvey( engineGC, surveyGC );
				return null;
			}
			else {
				// We know there is no data, hence we directly call the estimate function
				return f_createHeatProfileFromEstimates(engineGC);
			}
		case GAS_BURNER:
		case GAS_CHP:
			// heat consumption profiel
			return f_createHeatProfileFromGasSurvey( engineGC, surveyGC, heatingType );
		default:
			if (surveyGC.getNaturalGas().getPercentageUsedForHeating() != null && surveyGC.getNaturalGas().getPercentageUsedForHeating() != 0.0) {				
				// TODO: Find a solution to surveys filled in without heatingType that is not this hacky
				if (surveyGC.getNaturalGas().getEan().equals("123456789012345678")) {
					return null;
				}
				throw new RuntimeException("Gas data used for heating in survey, but no corresponding heating type");
			}
			else {
				f_createGasProfileFromSurvey( engineGC, surveyGC );
				return null;				
			}
	}
}
else if ( heatingType == OL_GridConnectionHeatingType.DISTRICTHEAT || heatingType == OL_GridConnectionHeatingType.LT_DISTRICTHEAT ) {
	return f_createHeatProfileFromSurvey(engineGC, surveyGC);
}
else if ( heatingType == OL_GridConnectionHeatingType.NONE ) {
	return null;
}
else {
	if(!settings.createCurrentElectricityEA() && engineGC.v_hasQuarterHourlyValues){
		if(heatingType == OL_GridConnectionHeatingType.HYBRID_HEATPUMP){
			return null; // Could create an estimated gas profile here: not done for now.
		}
		if(heatingType == OL_GridConnectionHeatingType.ELECTRIC_HEATPUMP){
			return null;
		}
	}
	return f_createHeatProfileFromEstimates(engineGC);
}
/*ALCODEEND*/}

boolean f_surveyHasGasData(com.zenmo.zummon.companysurvey.GridConnection surveyGC)
{/*ALCODESTART::1753803212846*/
if ( surveyGC.getNaturalGas().getAnnualDelivery_m3() != null && surveyGC.getNaturalGas().getAnnualDelivery_m3() > 0) {
	return true;
}
else if (surveyGC.getNaturalGas().getHourlyDelivery_m3() != null && surveyGC.getNaturalGas().getHourlyDelivery_m3().hasNumberOfValuesForOneYear()) {
	return true;
}
else {
	return false;
}
/*ALCODEEND*/}

double f_createGasProfileFromGasTS(GridConnection engineGC,com.zenmo.zummon.companysurvey.GridConnection surveyGC)
{/*ALCODESTART::1753804393557*/
// Gas delivery profile in m3
double[] profile_m3 = f_timeSeriesToQuarterHourlyDoubleArray(surveyGC.getNaturalGas().getHourlyDelivery_m3());

//Calculate yearly gas delivery
double yearlyGasDelivery_m3 = Arrays.stream(profile_m3).sum();

// We assume all delivery is consumption and convert m3 to kWh
ZeroMath.arrayMultiply(profile_m3, avgc_data.p_gas_kWhpm3);
// Then we create the profile asset and name it
J_EAProfile j_ea = new J_EAProfile(engineGC, OL_EnergyCarriers.METHANE, profile_m3, OL_ProfileAssetType.METHANEDEMAND , energyModel.p_timeStep_h);
j_ea.energyAssetName = engineGC.p_ownerID + " custom gas profile";

if(engineGC.p_owner.p_detailedCompany){
	v_remainingGasConsumption_m3 -= yearlyGasDelivery_m3;
}
/*ALCODEEND*/}

double f_reconstructGridConnections(EnergyModel deserializedEnergyModel)
{/*ALCODESTART::1753449467888*/
ArrayList<GridConnection> allConnections = new ArrayList<>();
allConnections.addAll(deserializedEnergyModel.c_gridConnections);
allConnections.addAll(deserializedEnergyModel.c_pausedGridConnections);

for(GridConnection GC : allConnections){
	GC.energyModel = deserializedEnergyModel;
	if (GC instanceof GCHouse){
		//toMove.add(GC);
		f_reconstructAgent(GC, deserializedEnergyModel.Houses, deserializedEnergyModel);
	} else if (GC instanceof GCEnergyProduction) {
		f_reconstructAgent(GC, deserializedEnergyModel.EnergyProductionSites, deserializedEnergyModel);
	} else if (GC instanceof GCEnergyConversion) {
		f_reconstructAgent(GC, deserializedEnergyModel.EnergyConversionSites, deserializedEnergyModel);
	} else if (GC instanceof GCGridBattery) {
		f_reconstructAgent(GC, deserializedEnergyModel.GridBatteries, deserializedEnergyModel);
	} else if (GC instanceof GCNeighborhood) {
		f_reconstructAgent(GC, deserializedEnergyModel.Neighborhoods, deserializedEnergyModel);
	} else if (GC instanceof GCPublicCharger) {
		f_reconstructAgent(GC, deserializedEnergyModel.PublicChargers, deserializedEnergyModel);
	} else if (GC instanceof GCUtility) {
		f_reconstructAgent(GC, deserializedEnergyModel.UtilityConnections, deserializedEnergyModel);
	}
	GC.f_startAfterDeserialisation();
}


/*ALCODEEND*/}

double f_createHeatProfileFromAnnualGasTotal(GridConnection engineGC,OL_GridConnectionHeatingType heatingType,double yearlyGasDelivery_m3,double ratioGasUsedForHeating)
{/*ALCODESTART::1753883660006*/
// First check what the heat conversion efficiency is from gas
double gasToHeatEfficiency = f_getGasToHeatEfficiency(heatingType);
// Finally, multiply the gas delivery with the total conversion factor to get the heat consumption
double yearlyConsumptionHeat_kWh = yearlyGasDelivery_m3 * avgc_data.p_gas_kWhpm3 * gasToHeatEfficiency * ratioGasUsedForHeating;
// We assume the heat consumption follows a standard profile
String profileName = "default_building_heat_demand_fr";
J_ProfilePointer profilePointer = energyModel.f_findProfile(profileName);
new J_EAConsumption(engineGC, OL_EnergyAssetType.HEAT_DEMAND, profileName, yearlyConsumptionHeat_kWh, OL_EnergyCarriers.HEAT, energyModel.p_timeStep_h, profilePointer);

if(engineGC.p_owner.p_detailedCompany){
	v_remainingGasConsumption_m3 -= yearlyGasDelivery_m3;
}

return yearlyConsumptionHeat_kWh * max(profilePointer.getAllValues())/energyModel.p_timeStep_h;
/*ALCODEEND*/}

double f_createGasProfileFromAnnualGasTotal(GridConnection engineGC,double yearlyGasDelivery_m3)
{/*ALCODESTART::1753883738731*/
// We assume all delivery is consumption and convert m3 to kWh
double yearlyGasConsumption_kWh = yearlyGasDelivery_m3 * avgc_data.p_gas_kWhpm3;
// We assume the gas consumption follows a standard heat consumption profile
String profileName = "default_building_heat_demand_fr";
new J_EAConsumption(engineGC, OL_EnergyAssetType.METHANE_DEMAND, profileName, yearlyGasConsumption_kWh, OL_EnergyCarriers.METHANE, energyModel.p_timeStep_h, null);	 

if(engineGC.p_owner.p_detailedCompany){
	v_remainingGasConsumption_m3 -= yearlyGasDelivery_m3;
}
/*ALCODEEND*/}

double f_reconstructEnergyModel(EnergyModel energyModel)
{/*ALCODESTART::1753449467890*/
// Code Instead of Agent.goToPopulation() (which resets all parameters to default!)	
/*
try{ // Reflection trick to get to Agent.owner private field
	energyModel.forceSetOwner(energyModel, pop_energyModels);
} catch (Exception e) {
	e.printStackTrace();
}
*/

Agent root = this.getRootAgent();
energyModel.restoreOwner(root);

energyModel.setEngine(getEngine());	
energyModel.instantiateBaseStructure_xjal();
energyModel.setEnvironment(this.getEnvironment());

traceln("EnergyModel owner: %s", energyModel.getOwner());

energyModel.create();
energyModel.f_startAfterDeserialisation();
//energyModel.start(); // Why is this needed?
/*ALCODEEND*/}

double f_createGasProfileFromSurvey(GridConnection engineGC,com.zenmo.zummon.companysurvey.GridConnection surveyGC)
{/*ALCODESTART::1753884183970*/
if (surveyGC.getNaturalGas().getHourlyDelivery_m3() != null && surveyGC.getNaturalGas().getHourlyDelivery_m3().hasNumberOfValuesForOneYear()) {
	f_createGasProfileFromGasTS( engineGC, surveyGC );
}
else if (surveyGC.getNaturalGas().getAnnualDelivery_m3() != null && surveyGC.getNaturalGas().getAnnualDelivery_m3() > 0) {
	double yearlyGasDelivery_m3 = surveyGC.getNaturalGas().getAnnualDelivery_m3();
	f_createGasProfileFromAnnualGasTotal( engineGC, yearlyGasDelivery_m3 );
}
else {
	f_createGasProfileFromEstimates( engineGC );
}
/*ALCODEEND*/}

double f_createHeatProfileFromGasSurvey(GridConnection engineGC,com.zenmo.zummon.companysurvey.GridConnection surveyGC,OL_GridConnectionHeatingType heatingType)
{/*ALCODESTART::1753884186444*/
if (surveyGC.getNaturalGas().getHourlyDelivery_m3() != null && surveyGC.getNaturalGas().getHourlyDelivery_m3().hasNumberOfValuesForOneYear()) {
	return f_createHeatProfileFromGasTS( engineGC, surveyGC, heatingType );
}
else if ( surveyGC.getNaturalGas().getAnnualDelivery_m3() != null && surveyGC.getNaturalGas().getAnnualDelivery_m3() > 0) {
	double yearlyGasDelivery_m3 = surveyGC.getNaturalGas().getAnnualDelivery_m3();
	double ratioGasUsedForHeating = f_getRatioGasUsedForHeating(surveyGC);
	return f_createHeatProfileFromAnnualGasTotal( engineGC, heatingType, yearlyGasDelivery_m3, ratioGasUsedForHeating );
}
else {
	return f_createHeatProfileFromEstimates( engineGC );
}
/*ALCODEEND*/}

double f_createHeatProfileFromGasTS(GridConnection engineGC,com.zenmo.zummon.companysurvey.GridConnection surveyGC,OL_GridConnectionHeatingType heatingType)
{/*ALCODESTART::1753949286953*/
// Gas profile
double[] profile_m3 = f_timeSeriesToQuarterHourlyDoubleArray(surveyGC.getNaturalGas().getHourlyDelivery_m3());

double yearlyGasDelivery_m3 = Arrays.stream(profile_m3).sum();

// First check what the heat conversion efficiency is from gas
double gasToHeatEfficiency = f_getGasToHeatEfficiency(heatingType);
// Then check which part of the gas consumption is used for heating
double ratioGasUsedForHeating = f_getRatioGasUsedForHeating(surveyGC);
// Finally, multiply the gas profile with the total conversion factor to get the heat profile
ZeroMath.arrayMultiply(profile_m3, avgc_data.p_gas_kWhpm3 * gasToHeatEfficiency * ratioGasUsedForHeating);
// Then we create the profile asset and name it
J_EAProfile j_ea = new J_EAProfile(engineGC, OL_EnergyCarriers.HEAT, profile_m3, OL_ProfileAssetType.HEATDEMAND , energyModel.p_timeStep_h);
j_ea.energyAssetName = engineGC.p_ownerID + " custom building heat profile";

if(engineGC.p_owner.p_detailedCompany){
	v_remainingGasConsumption_m3 -= yearlyGasDelivery_m3;
}

return max(profile_m3)/energyModel.p_timeStep_h;
/*ALCODEEND*/}

double f_reconstructAgent(Agent agent,AgentArrayList pop,EnergyModel energyModel)
{/*ALCODESTART::1753449467892*/
/* // Code Instead of Agent.goToPopulation() (which resets many variables to default!)	
try{ // Reflection trick to get to Agent.owner private field
	if (agent instanceof GridNode) {
		((GridNode)agent).forceSetOwner(agent,pop);
	} else if (agent instanceof GridConnection) {
		((GridConnection)agent).forceSetOwner(agent,pop);
	} else if (agent instanceof Actor) {
		((Actor)agent).forceSetOwner(agent,pop);
	} else if (agent instanceof GIS_Object) {
		((GIS_Object)agent).forceSetOwner(agent,pop);
	}
} catch (Exception e) {
	e.printStackTrace();
}*/ 

agent.restoreOwner(energyModel); // simply sets agent.d = root, should replace reflection hack
agent.restoreCollection_xjal(pop); // simple sets agent.j = pop, should replace reflection hack

agent.setEngine(getEngine());	
agent.instantiateBaseStructure_xjal();
agent.setEnvironment(pop.getEnvironment());

pop._add(agent); // Add to the population	
//int popSize = pop.size(); 
//pop.callCreate(agent, popSize); // Update population object
agent.create();
/*ALCODEEND*/}

double f_getGasToHeatEfficiency(OL_GridConnectionHeatingType heatingType)
{/*ALCODESTART::1753951013582*/
switch (heatingType){
	case GAS_BURNER:
	case HYBRID_HEATPUMP:
		return avgc_data.p_avgEfficiencyGasBurner_fr;
	case GAS_CHP:
		return avgc_data.p_avgEfficiencyCHP_thermal_fr;
	default:
		throw new RuntimeException("Unable to find Gas to Heat efficiency of heatingType: " + heatingType);
}
/*ALCODEEND*/}

double f_getRatioGasUsedForHeating(com.zenmo.zummon.companysurvey.GridConnection surveyGC)
{/*ALCODESTART::1753951039103*/
if (surveyGC.getNaturalGas().getPercentageUsedForHeating() != null) {
	return surveyGC.getNaturalGas().getPercentageUsedForHeating() / 100;
}
else {
	return 1.0;
}
/*ALCODEEND*/}

double f_createGasProfileFromEstimates(GridConnection engineGC)
{/*ALCODESTART::1753955686832*/
double yearlyGasDelivery_m3 = engineGC.p_floorSurfaceArea_m2 * avgc_data.p_avgCompanyGasConsumption_m3pm2;
f_createGasProfileFromAnnualGasTotal(engineGC, yearlyGasDelivery_m3);

if(engineGC.p_owner.p_detailedCompany){
	v_remainingGasConsumption_m3 -= yearlyGasDelivery_m3;
}
/*ALCODEEND*/}

double f_createHeatProfileFromEstimates(GridConnection engineGC)
{/*ALCODESTART::1753961830063*/
double yearlyGasConsumption_m3 = engineGC.p_floorSurfaceArea_m2 * avgc_data.p_avgCompanyGasConsumption_m3pm2;
double yearlyHeatConsumption_kWh = yearlyGasConsumption_m3 * avgc_data.p_gas_kWhpm3;
return f_createHeatProfileFromAnnualHeatTotal( engineGC, yearlyHeatConsumption_kWh );
/*ALCODEEND*/}

double f_createHeatProfileFromHeatTS(GridConnection engineGC,com.zenmo.zummon.companysurvey.GridConnection surveyGC)
{/*ALCODESTART::1753964366889*/
// Heat profile
double[] profile = f_timeSeriesToQuarterHourlyDoubleArray(surveyGC.getHeat().getHeatDeliveryTimeSeries_kWh());
// We multiply by the delivery set efficiency to go from delivery to consumption
// TODO: Fix this for LT_DISTRICTHEAT, they have a different efficiency!
ZeroMath.arrayMultiply(profile, avgc_data.p_avgEfficiencyDistrictHeatingDeliverySet_fr);
// Then we create the profile asset and name it
J_EAProfile j_ea = new J_EAProfile(engineGC, OL_EnergyCarriers.HEAT, profile, OL_ProfileAssetType.HEATDEMAND , energyModel.p_timeStep_h);
j_ea.energyAssetName = engineGC.p_ownerID + " custom building heat profile";

return max(profile)/energyModel.p_timeStep_h;
/*ALCODEEND*/}

double f_reconstructGridConnections1(EnergyModel energyModel)
{/*ALCODESTART::1753449467894*/
// Code Instead of Agent.goToPopulation() (which resets many variables to default!)	
GC.energyModel = energyModel;
try{ // Reflection trick to get to Agent.owner private field
	GC.forceSetOwner(GC,pop);
} catch (Exception e) {
	e.printStackTrace();
}
	
traceln("GC owner: %s", GC.getOwner());
GC.setEngine(getEngine());	
GC.instantiateBaseStructure_xjal();
GC.setEnvironment(pop.getEnvironment());

pop._add(GC); // Add to the population
int popSize = pop.size(); 
pop.callCreate(GC, popSize); // Update population object

/*ALCODEEND*/}

double f_addMixins()
{/*ALCODESTART::1753451091785*/
v_objectMapper.addMixIn(Agent.class, AgentMixin.class);
v_objectMapper.addMixIn(AgentArrayList.class, IgnoreClassMixin.class);
v_objectMapper.addMixIn(EnergyModel.class, EnergyModelMixin.class);
v_objectMapper.addMixIn(Actor.class, ActorMixin.class);
v_objectMapper.addMixIn(DataSet.class, DataSetMixin.class);
v_objectMapper.addMixIn(TextFile.class, IgnoreClassMixin.class);
v_objectMapper.addMixIn(EnergyDataViewer.class, IgnoreClassMixin.class);

v_objectMapper.addMixIn(com.anylogic.engine.TableFunction.class, IgnoreClassMixin.class);
//objectMapper.addMixIn(com.anylogic.engine.TableFunction.class, TableFunctionMixin.class);
v_objectMapper.addMixIn(com.anylogic.engine.markup.GISRegion.class, IgnoreClassMixin.class);
v_objectMapper.addMixIn(com.anylogic.engine.presentation.ViewArea.class, IgnoreClassMixin.class);
v_objectMapper.addMixIn(com.anylogic.engine.AgentSpacePosition.class, IgnoreClassMixin.class);

// Weirdness regarding material handling toolbox	
v_objectMapper.addMixIn(com.anylogic.engine.AgentSpacePosition.class, IgnoreClassMixin.class);
v_objectMapper.addMixIn(com.anylogic.engine.markup.AbstractWall.class, IgnoreClassMixin.class);
v_objectMapper.addMixIn(com.anylogic.engine.markup.RailwayTrack.class, IgnoreClassMixin.class);
v_objectMapper.addMixIn(com.anylogic.engine.markup.PalletRack.class, IgnoreClassMixin.class);
v_objectMapper.addMixIn(com.anylogic.engine.markup.RoadNetwork.class, IgnoreClassMixin.class);
v_objectMapper.addMixIn(com.anylogic.engine.markup.AreaNode.class, IgnoreClassMixin.class);
v_objectMapper.addMixIn(com.anylogic.engine.markup.AbstractFluidMarkup.class, IgnoreClassMixin.class);
v_objectMapper.addMixIn(com.anylogic.engine.markup.Lift.class, IgnoreClassMixin.class);
v_objectMapper.addMixIn(com.anylogic.engine.markup.ConveyorNode.class, IgnoreClassMixin.class);
v_objectMapper.addMixIn(com.anylogic.engine.markup.Node.class, IgnoreClassMixin.class);

/*ALCODEEND*/}

double f_createHeatProfileFromAnnualHeatTotal(GridConnection engineGC,double yearlyConsumptionHeat_kWh)
{/*ALCODESTART::1753964605708*/
// We assume the heat consumption follows a standard profile
String profileName = "default_building_heat_demand_fr";
J_ProfilePointer profilePointer = energyModel.f_findProfile(profileName);
new J_EAConsumption(engineGC, OL_EnergyAssetType.HEAT_DEMAND, profileName, yearlyConsumptionHeat_kWh, OL_EnergyCarriers.HEAT, energyModel.p_timeStep_h, profilePointer);

return yearlyConsumptionHeat_kWh * max(profilePointer.getAllValues())/energyModel.p_timeStep_h;
/*ALCODEEND*/}

double f_createHeatProfileFromSurvey(GridConnection engineGC,com.zenmo.zummon.companysurvey.GridConnection surveyGC)
{/*ALCODESTART::1753964654729*/
if (surveyGC.getHeat() != null && surveyGC.getHeat().getHeatDeliveryTimeSeries_kWh() != null) {
	return f_createHeatProfileFromHeatTS(engineGC, surveyGC);
}
else if (surveyGC.getHeat() != null && surveyGC.getHeat().getAnnualDistrictHeatingDelivery_GJ() != null) {
	double yearlyHeatDelivery_kWh = surveyGC.getHeat().getAnnualDistrictHeatingDelivery_GJ()*277.777778 ;
	// TODO: Fix this for LT_DISTRICTHEAT, they have a different efficiency!
	double yearlyHeatConsumption_kWh = yearlyHeatDelivery_kWh * avgc_data.p_avgEfficiencyDistrictHeatingDeliverySet_fr;
	return f_createHeatProfileFromAnnualHeatTotal(engineGC, yearlyHeatConsumption_kWh);
}
else {
	return f_createHeatProfileFromEstimates(engineGC);
}
/*ALCODEEND*/}

double f_setDefaultHeatingStrategies()
{/*ALCODESTART::1753968816374*/
// Triples ( heatingType, hasThermalBuilding, hasHeatBuffer )
Triple<OL_GridConnectionHeatingType, Boolean, Boolean> triple = null;

triple = Triple.of(OL_GridConnectionHeatingType.GAS_BURNER, false, false);
energyModel.c_defaultHeatingStrategies.put( triple, J_HeatingManagementSimple.class );
triple = Triple.of(OL_GridConnectionHeatingType.GAS_BURNER, true, false);
energyModel.c_defaultHeatingStrategies.put( triple, J_HeatingManagementSimple.class );
triple = Triple.of(OL_GridConnectionHeatingType.GAS_BURNER, true, true);
energyModel.c_defaultHeatingStrategies.put( triple, J_HeatingManagementSimple.class );

triple = Triple.of(OL_GridConnectionHeatingType.ELECTRIC_HEATPUMP, false, false);
energyModel.c_defaultHeatingStrategies.put( triple, J_HeatingManagementSimple.class );
triple = Triple.of(OL_GridConnectionHeatingType.ELECTRIC_HEATPUMP, true, false);
energyModel.c_defaultHeatingStrategies.put( triple, J_HeatingManagementSimple.class );
triple = Triple.of(OL_GridConnectionHeatingType.ELECTRIC_HEATPUMP, true, true);
energyModel.c_defaultHeatingStrategies.put( triple, J_HeatingManagementSimple.class );

triple = Triple.of(OL_GridConnectionHeatingType.HYBRID_HEATPUMP, false, false);
energyModel.c_defaultHeatingStrategies.put( triple, J_HeatingManagementProfileHybridHeatPump.class );
triple = Triple.of(OL_GridConnectionHeatingType.HYBRID_HEATPUMP, true, false);
energyModel.c_defaultHeatingStrategies.put( triple, J_HeatingManagementBuildingHybridHeatPump.class );

triple = Triple.of(OL_GridConnectionHeatingType.DISTRICTHEAT, false, false);
energyModel.c_defaultHeatingStrategies.put( triple, J_HeatingManagementSimple.class );
triple = Triple.of(OL_GridConnectionHeatingType.DISTRICTHEAT, true, false);
energyModel.c_defaultHeatingStrategies.put( triple, J_HeatingManagementSimple.class );
triple = Triple.of(OL_GridConnectionHeatingType.DISTRICTHEAT, true, true);
energyModel.c_defaultHeatingStrategies.put( triple, J_HeatingManagementSimple.class );

triple = Triple.of(OL_GridConnectionHeatingType.LT_DISTRICTHEAT, false, false);
energyModel.c_defaultHeatingStrategies.put( triple, J_HeatingManagementSimple.class );
triple = Triple.of(OL_GridConnectionHeatingType.LT_DISTRICTHEAT, true, false);
energyModel.c_defaultHeatingStrategies.put( triple, J_HeatingManagementSimple.class );
triple = Triple.of(OL_GridConnectionHeatingType.LT_DISTRICTHEAT, true, true);
energyModel.c_defaultHeatingStrategies.put( triple, J_HeatingManagementSimple.class );

triple = Triple.of(OL_GridConnectionHeatingType.HYDROGENBURNER, false, false);
energyModel.c_defaultHeatingStrategies.put( triple, J_HeatingManagementSimple.class );
triple = Triple.of(OL_GridConnectionHeatingType.HYDROGENBURNER, true, false);
energyModel.c_defaultHeatingStrategies.put( triple, J_HeatingManagementSimple.class );
triple = Triple.of(OL_GridConnectionHeatingType.HYDROGENBURNER, true, true);
energyModel.c_defaultHeatingStrategies.put( triple, J_HeatingManagementSimple.class );
/*ALCODEEND*/}

double f_addCustomHeatingSetup(GridConnection engineGC,com.zenmo.zummon.companysurvey.GridConnection surveyGC)
{/*ALCODESTART::1754048849906*/
throw new RuntimeException("HeatingType is CUSTOM. You must override the function: f_addCustomHeatingSetup!");
/*ALCODEEND*/}

double f_reconstructActors(EnergyModel deserializedEnergyModel)
{/*ALCODESTART::1753712630322*/
for(Actor AC : deserializedEnergyModel.c_actors){
		
		if (AC instanceof ConnectionOwner) {
			((ConnectionOwner)AC).energyModel = deserializedEnergyModel;
			f_reconstructAgent(AC, deserializedEnergyModel.pop_connectionOwners, deserializedEnergyModel);
		} else if (AC instanceof EnergySupplier) {
			((EnergySupplier)AC).energyModel = deserializedEnergyModel;
			f_reconstructAgent(AC, deserializedEnergyModel.pop_energySuppliers, deserializedEnergyModel);
		} else if (AC instanceof EnergyCoop) {
			((EnergyCoop)AC).energyModel = deserializedEnergyModel;
			f_reconstructAgent(AC, deserializedEnergyModel.pop_energyCoops, deserializedEnergyModel);
			((EnergyCoop)AC).f_startAfterDeserialisation();
		} else if (AC instanceof GridOperator) {
			((GridOperator)AC).energyModel = deserializedEnergyModel;
			f_reconstructAgent(AC, deserializedEnergyModel.pop_gridOperators, deserializedEnergyModel);
		}
	}

/*ALCODEEND*/}

double f_addCustomHeatAsset(GridConnection parentGC,double maxHeatOutputPower_kW)
{/*ALCODESTART::1754050106254*/
throw new RuntimeException("HeatingType is CUSTOM. You must override the function: f_addCustomHeatAsset!");
/*ALCODEEND*/}

double f_reconstructGIS_Objects(EnergyModel deserializedEnergyModel,ArrayList<GIS_Object> c_GISObjects)
{/*ALCODESTART::1753712697685*/
for(GIS_Object GO : c_GISObjects){
	GO.gisRegion = c_GISregions.get(GO.p_id);
	
	if (GO instanceof GIS_Building) {
		((GIS_Building)GO).energyModel = deserializedEnergyModel;
		f_reconstructAgent(GO, deserializedEnergyModel.pop_GIS_Buildings, deserializedEnergyModel);
	} else if (GO instanceof GIS_Parcel) {
		((GIS_Parcel)GO).energyModel = deserializedEnergyModel;
		f_reconstructAgent(GO, deserializedEnergyModel.pop_GIS_Parcels, deserializedEnergyModel);
	} else {
		GO.energyModel = deserializedEnergyModel;
		f_reconstructAgent(GO, deserializedEnergyModel.pop_GIS_Objects, deserializedEnergyModel);
		//GO.f_startAfterDeserialisation();
	}
	GO.f_resetStyle();
}
/*ALCODEEND*/}

double f_reconstructGridNodes(EnergyModel deserializedEnergyModel,ArrayList<GridNode> c_gridNodes)
{/*ALCODESTART::1753712761420*/
for(GridNode GN : c_gridNodes){
	GN.energyModel = deserializedEnergyModel;
	f_reconstructAgent(GN, deserializedEnergyModel.pop_gridNodes, deserializedEnergyModel);
}

/*ALCODEEND*/}

