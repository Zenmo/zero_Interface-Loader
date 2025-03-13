double f_createGISRegionOutline()
{/*ALCODESTART::1726584205769*/
// Create neighborhoods and draw them
for (Neighbourhood_data NBH : c_Neighbourhood_data) {	
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
f_generateSolarParks();
f_generateWindFarms();

//Other infra assets
f_generateElectrolysers();
f_generateBatteries();
f_createChargingStations();

//Consumers
switch(project_data.project_type()){

case BUSINESSPARK:
	f_createConsumerGC_businesspark();
	break;
	
case RESIDENTIAL:
	f_createConsumerGC_Residential();
	break;
}
/*ALCODEEND*/}

double f_configureEngine_default()
{/*ALCODESTART::1726584205773*/
//Set basic input files
energyModel.p_truckTripsExcel = inputTruckTrips;
energyModel.p_householdTripsExcel = inputHouseholdTrips;
energyModel.p_cookingPatternExcel = inputCookingActivities;


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

for (GridNode_data GN_data : c_GridNode_data) {
	//    if no scope selected, or if node has 'all scopes' in input file or if the node specific scope is selected (exists in the arrayList)       
	if( settings.subscopesToSimulate() == null || settings.subscopesToSimulate().isEmpty() || GN_data.subscope() == null || settings.subscopesToSimulate().indexOf(GN_data.subscope()) > -1 ){ 
		if (GN_data.status()) {
			GridNode GN = energyModel.add_pop_gridNodes();
			GN.p_gridNodeID = GN_data.trafo_id();
			c_gridNodeIDsInScope.add(GN.p_gridNodeID);
			
			// Check wether transformer capacity is known or estimated
			GN.p_capacity_kW = GN_data.capacity_kw();	
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
			
			zero_Interface.c_GISNodes.add(GN.gisRegion);
			Grid_Operator.c_electricityGridNodes.add(GN);
			
			
			//Gridnode service area
			if (GN_data.service_area_polygon() != null){
				//Create service area gis object
				//GIS_Object serviceArea = f_createGISObject(GN.p_gridNodeID + ": service area", GN.p_latitude, GN.p_longitude, GN_data.service_area_polygon());
				GISRegion serviceArea = zero_Interface.f_createGISObject(f_createGISObjectsTokens(GN_data.service_area_polygon(), OL_GISObjectType.GN_SERVICE_AREA));
				
				//Add to hashmap
				zero_Interface.c_GISNetplanes.add( serviceArea );
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
else {
	traceln("GIS coordinaten in de excel data die niet starten met Multi of Poly");
}



if(RegionCoords.contains(")(") || RegionCoords.contains(") (")){
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

double f_createHousesFromDatabase()
{/*ALCODESTART::1726584205781*/
List<Building_data> scopedBuilding_data = f_getScopedBuildingList(c_HouseBuilding_data);
	
traceln("Aantal panden met woonfunctie in BAG data: " + scopedBuilding_data.size());

int i = 0;
for (Building_data dataBuilding : scopedBuilding_data) {
	
	GCHouse GCH = energyModel.add_Houses();
	ConnectionOwner	COH = energyModel.add_pop_connectionOwners();
	
	//Set parameters for the Grid Connection
 	GCH.p_gridConnectionID = dataBuilding.address_id();
	GCH.p_ownerID = "Woonhuis" + Integer.toString(i);	//aanname : huiseigenaar is eigenbaas

	GCH.p_purposeBAG = dataBuilding.purpose();
	
	//pand gegevens
	GCH.p_heatingType = avgc_data.p_avgHouseHeatingMethod ;
	GCH.p_floorSurfaceArea_m2 = dataBuilding.cumulative_floor_surface_m2();
	GCH.p_bouwjaar = dataBuilding.build_year();
	
	//Nageisoleerd
	if (dataBuilding.energy_label() != null) {
		GCH.p_nageisoleerd = dataBuilding.energy_label();
	}
	else {
		if (GCH.p_bouwjaar < 1980) {
			GCH.p_nageisoleerd = "D";
		}
		else if (GCH.p_bouwjaar < 2000) {
			GCH.p_nageisoleerd = "C";
		}
		else {
			GCH.p_nageisoleerd = "B";
		}
	}
	
	//aansluiting gegevens
	GCH.p_physicalConnectionCapacity_kW = avgc_data.p_avgHouseConnectionCapacity_kW;
	GCH.p_contractedDeliveryCapacity_kW = avgc_data.p_avgHouseConnectionCapacity_kW;
	GCH.p_contractedFeedinCapacity_kW = avgc_data.p_avgHouseConnectionCapacity_kW;
	
	//adres gegevens
	GCH.p_address = new J_Address();
	GCH.p_address.setStreetName(dataBuilding.streetname());
	GCH.p_address.setHouseNumber(dataBuilding.house_number());
	GCH.p_address.setHouseLetter(dataBuilding.house_letter());
	GCH.p_address.setHouseAddition(dataBuilding.house_addition());
	GCH.p_address.setPostalcode(dataBuilding.postalcode());
	GCH.p_address.setCity(dataBuilding.city());
	
	//locatie
	GCH.p_longitude = dataBuilding.longitude();
	GCH.p_latitude = dataBuilding.latitude();
	GCH.setLatLon(GCH.p_latitude, GCH.p_longitude);
	
	//Connect GC to grid node
	//GridNode myParentNodeElectric;
	GCH.p_parentNodeElectricID = dataBuilding.trafo_id();
	

	
	//Set parameters for the Actor: ConnectionOwner
	COH.p_actorID = GCH.p_ownerID;
	COH.p_actorType = OL_ActorType.CONNECTIONOWNER;
	COH.p_connectionOwnerType = OL_ConnectionOwnerType.HOUSEHOLD;
	COH.p_detailedCompany = false;
	
	GCH.p_owner = COH;
	
	//Create GIS building
	GIS_Building b;
	int pandClusterNr = dataBuilding.pandcluster_nr();
	if( pandClusterNr == 0 ){	
		b = f_createGISBuilding( dataBuilding, GCH );
	}
	else {
		b = randomWhere(energyModel.c_GISBuildingClusters, x -> x.p_pandcluster_nr == pandClusterNr);
		if (b == null){
			b = f_createGISBuilding( dataBuilding, GCH );
		}
		else {
			b.c_containedGridConnections.add(GCH);
			GCH.c_connectedGISObjects.add(b);
		}
	}
	
	//Style building
	b.p_defaultFillColor = zero_Interface.v_houseBuildingColor;
	b.p_defaultLineColor = zero_Interface.v_houseBuildingLineColor;
	zero_Interface.f_styleAreas(b);
	
	//Instantiate energy assets
	double jaarlijksElectriciteitsVerbruik;
	double jaarlijksGasVerbruik;
	try {
		jaarlijksElectriciteitsVerbruik = dataBuilding.electricity_consumption_kwhpa();
	}
	catch (NullPointerException e){
		jaarlijksElectriciteitsVerbruik = Double.valueOf(uniform_discr(1200, 4500)); // Hardcoded??
	}
	try {
		jaarlijksGasVerbruik = dataBuilding.gas_consumption_kwhpa();
	}
	catch (NullPointerException e){
		jaarlijksGasVerbruik =  Double.valueOf(uniform_discr(600, 2000)); // Hardcoded??
	}
	
	f_addEnergyAssetsToHouses(GCH, jaarlijksElectriciteitsVerbruik, jaarlijksGasVerbruik );	
	f_setHouseHeatingPreferences(GCH);
	i ++;
}	

/*ALCODEEND*/}

A_SubTenant f_createSubtenant(com.zenmo.zummon.companysurvey.Survey survey,com.zenmo.zummon.companysurvey.Address address)
{/*ALCODESTART::1726584205783*/
A_SubTenant subtenant = energyModel.add_pop_subTenants();

subtenant.p_actorID = survey.getCompanyName();
			
//Adress data
subtenant.p_address = new J_Address();
subtenant.p_address.setStreetName(address.getStreet().substring(0,1).toUpperCase() + address.getStreet().substring(1).toLowerCase());
subtenant.p_address.setHouseNumber(address.getHouseNumber());
subtenant.p_address.setHouseLetter(address.getHouseLetter().equals("") ? null : address.getHouseLetter());
subtenant.p_address.setHouseAddition(address.getHouseNumberSuffix().equals("") ? null : address.getHouseNumberSuffix());
subtenant.p_address.setPostalcode(address.getPostalCode().equals("") ? null : address.getPostalCode().toUpperCase().replaceAll("\\s",""));
subtenant.p_address.setCity(address.getCity().substring(0,1).toUpperCase() + address.getCity().substring(1).toLowerCase());
				
return subtenant;


/*ALCODEEND*/}

double f_generateSolarParks()
{/*ALCODESTART::1726584205785*/
ConnectionOwner owner;
GCEnergyProduction solarpark;

List<String> existing_actors = new ArrayList();
List<String> existing_solarFields = new ArrayList();


for (Solarfarm_data dataSolarfarm : c_Solarfarm_data) { // MOET NOG CHECK OF ZONNEVELD ACTOR AL BESTAAT OP, zo ja --> Zonneveld koppelen aan elkaar en niet 2 GC en 2 actoren maken.
	
	if (!existing_solarFields.contains( dataSolarfarm.gc_id() )) {
		solarpark = energyModel.add_EnergyProductionSites();
		
		solarpark.set_p_gridConnectionID( dataSolarfarm.gc_id() );
		solarpark.set_p_name( dataSolarfarm.gc_name() );
		
		//Grid Capacity
		solarpark.set_p_physicalConnectionCapacity_kW( dataSolarfarm.connection_capacity_kw() );
		if ( dataSolarfarm.connection_capacity_kw() > 0 ) {
			solarpark.b_isRealPhysicalCapacityAvailable = true;
		}
		if ( dataSolarfarm.contracted_feed_in_capacity_kw() != null) {
			solarpark.set_p_contractedFeedinCapacity_kW( dataSolarfarm.contracted_feed_in_capacity_kw() );
			solarpark.b_isRealFeedinCapacityAvailable = true;
		}
		else {
			solarpark.set_p_contractedFeedinCapacity_kW( dataSolarfarm.connection_capacity_kw() );
			solarpark.b_isRealFeedinCapacityAvailable = false;
		}
		
		
		solarpark.set_p_heatingType( OL_GridConnectionHeatingType.NONE );	
		solarpark.set_p_ownerID( dataSolarfarm.owner_id() );	
		solarpark.set_p_parentNodeElectricID( dataSolarfarm.trafo_id() );
		
		solarpark.v_isActive = dataSolarfarm.initially_active() ;
		
		//Add EA
		f_addEnergyProduction(solarpark, OL_EnergyAssetType.PHOTOVOLTAIC, "Solar farm" , dataSolarfarm.capacity_electric_kw());
		
		
		if (!existing_actors.contains(solarpark.p_ownerID)){ // check if owner exists already, if not, create new owner.
			owner = energyModel.add_pop_connectionOwners();
			
			owner.set_p_actorID( dataSolarfarm.owner_id());
			owner.set_p_actorType( OL_ActorType.CONNECTIONOWNER );
			owner.set_p_connectionOwnerType( OL_ConnectionOwnerType.SOLARFARM_OP );

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

double f_generateBatteries()
{/*ALCODESTART::1726584205787*/
for (Battery_data dataBattery : c_Battery_data) { // MOET NOG CHECK OF battery ACTOR AL BESTAAT OP, zo ja --> battery koppelen aan elkaar en niet 2 GC en 2 actoren maken.
	
	ConnectionOwner owner = energyModel.add_pop_connectionOwners();
	GCGridBattery gridbattery = energyModel.add_GridBatteries();
	
	//Owner parameters
	owner.set_p_actorID( dataBattery.owner_id() );
	owner.set_p_actorType( OL_ActorType.CONNECTIONOWNER );
	owner.set_p_connectionOwnerType( OL_ConnectionOwnerType.BATTERY_OP );
	
	//GC parameters
	gridbattery.set_p_gridConnectionID( dataBattery.gc_id () );
	gridbattery.set_p_name( dataBattery.gc_name() );
	gridbattery.set_p_ownerID( dataBattery.owner_id() );
	gridbattery.set_p_owner( owner );	
	gridbattery.set_p_physicalConnectionCapacity_kW( dataBattery.connection_capacity_kw() );
	
	if(gridbattery.p_gridConnectionID.equals("SLIDER_GB")){
		gridbattery.p_isSliderGC = true;
	}
	
	//Grid Capacity
	gridbattery.set_p_physicalConnectionCapacity_kW( dataBattery.connection_capacity_kw() );
	if ( dataBattery.connection_capacity_kw() > 0 ) {
		gridbattery.b_isRealPhysicalCapacityAvailable = true;
	}
	if ( dataBattery.contracted_delivery_capacity_kw() != null ) {
		gridbattery.set_p_contractedDeliveryCapacity_kW( dataBattery.contracted_delivery_capacity_kw() );
		gridbattery.b_isRealDeliveryCapacityAvailable = true;
	}
	else {
		gridbattery.set_p_contractedDeliveryCapacity_kW( dataBattery.connection_capacity_kw() );
		gridbattery.b_isRealDeliveryCapacityAvailable = false;
	}
	if ( dataBattery.contracted_feed_in_capacity_kw() != null ) {
		gridbattery.set_p_contractedFeedinCapacity_kW( dataBattery.contracted_feed_in_capacity_kw() );
		gridbattery.b_isRealFeedinCapacityAvailable = true;
	}
	else {
		gridbattery.set_p_contractedFeedinCapacity_kW( dataBattery.connection_capacity_kw() );
		gridbattery.b_isRealFeedinCapacityAvailable = false;	
	}
	
	gridbattery.set_p_parentNodeElectricID( dataBattery.trafo_id() );	
	gridbattery.set_p_heatingType( OL_GridConnectionHeatingType.NONE );	
	gridbattery.set_p_batteryOperationMode( OL_BatteryOperationMode.valueOf(dataBattery.default_operation_mode()) );
	
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

double f_generateElectrolysers()
{/*ALCODESTART::1726584205789*/
ConnectionOwner owner;
List<String> existing_actors = new ArrayList();


for (Electrolyser_data dataElectrolyser : c_Electrolyser_data) {
	GCEnergyConversion H2Electrolyser = energyModel.add_EnergyConversionSites();

	H2Electrolyser.set_p_gridConnectionID( dataElectrolyser.gc_id() );
	H2Electrolyser.set_p_name( dataElectrolyser.gc_name() );
	H2Electrolyser.set_p_heatingType( OL_GridConnectionHeatingType.NONE );	
	H2Electrolyser.set_p_ownerID( dataElectrolyser.owner_id() );	
	H2Electrolyser.set_p_parentNodeElectricID( dataElectrolyser.trafo_id() );
	
	//Grid Capacity
	H2Electrolyser.set_p_physicalConnectionCapacity_kW( dataElectrolyser.connection_capacity_kw() );
	if ( dataElectrolyser.connection_capacity_kw() > 0 ) {
		H2Electrolyser.b_isRealPhysicalCapacityAvailable = true;
	}
	if ( dataElectrolyser.contracted_delivery_capacity_kw() != null ) {
		H2Electrolyser.set_p_contractedDeliveryCapacity_kW( dataElectrolyser.contracted_delivery_capacity_kw() );
		H2Electrolyser.b_isRealDeliveryCapacityAvailable = true;
	}
	else {
		H2Electrolyser.set_p_contractedDeliveryCapacity_kW( dataElectrolyser.connection_capacity_kw() );
		H2Electrolyser.b_isRealDeliveryCapacityAvailable = false;
	}
	if ( dataElectrolyser.contracted_feed_in_capacity_kw() != null ) {
		H2Electrolyser.set_p_contractedFeedinCapacity_kW( dataElectrolyser.contracted_feed_in_capacity_kw() );
		H2Electrolyser.b_isRealFeedinCapacityAvailable = true;
	}
	else {
		H2Electrolyser.set_p_contractedFeedinCapacity_kW( dataElectrolyser.connection_capacity_kw() );
		H2Electrolyser.b_isRealFeedinCapacityAvailable = false;	
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

double f_generateWindFarms()
{/*ALCODESTART::1726584205791*/
ConnectionOwner owner;
GCEnergyProduction windfarm;

List<String> existing_actors = new ArrayList();
List<String> existing_windFarms = new ArrayList();

for (Windfarm_data dataWindfarm : c_Windfarm_data) {
	if (!existing_windFarms.contains(dataWindfarm.gc_id())) { // Check if windfarm exists already, if not, create new windfarm GC + turbine
		windfarm = energyModel.add_EnergyProductionSites();

		windfarm.set_p_gridConnectionID( dataWindfarm.gc_id() );
		windfarm.set_p_name( dataWindfarm.gc_name() );
		if(windfarm.p_gridConnectionID.equals("SLIDER_WF")){
			windfarm.p_isSliderGC = true;
		}
		
		//Grid capacity
		windfarm.set_p_physicalConnectionCapacity_kW( dataWindfarm.connection_capacity_kw() );
		if ( dataWindfarm.connection_capacity_kw() > 0 ) {
			windfarm.b_isRealPhysicalCapacityAvailable = true;
		}
		if ( dataWindfarm.contracted_feed_in_capacity_kw() != null) {
			windfarm.set_p_contractedFeedinCapacity_kW( dataWindfarm.contracted_feed_in_capacity_kw() );
			windfarm.b_isRealFeedinCapacityAvailable = true;
		}
		else {
			windfarm.set_p_contractedFeedinCapacity_kW( dataWindfarm.connection_capacity_kw() );
			windfarm.b_isRealFeedinCapacityAvailable = false;
		}
		
		windfarm.set_p_heatingType( OL_GridConnectionHeatingType.NONE );	
		windfarm.set_p_ownerID( dataWindfarm.owner_id() );	
		windfarm.set_p_parentNodeElectricID( dataWindfarm.trafo_id() );
		
		//Get initial state
		windfarm.v_isActive = dataWindfarm.initially_active();
		
		//Create EA for the windturbine GC
		f_addEnergyProduction(windfarm, OL_EnergyAssetType.WINDMILL, "Windmill onshore", dataWindfarm.capacity_electric_kw());
		
		if (!existing_actors.contains(windfarm.p_ownerID)){ // check if owner exists already, if not, create new owner.
			owner = energyModel.add_pop_connectionOwners();
			
			owner.set_p_actorID( windfarm.p_ownerID );
			owner.set_p_actorType( OL_ActorType.CONNECTIONOWNER );
			owner.set_p_connectionOwnerType( OL_ConnectionOwnerType.WINDFARM_OP );
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
		scaling_factor_gridnode = scaling_factor_MVLV;		
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

double f_setEngineProfiles()
{/*ALCODESTART::1726584205797*/
List<Double> argumentsList = genericProfiles_data.argumentsList();

// Various demand profiles

List<Double> houseEdemandList = genericProfiles_data.houseEdemandList();
List<Double> houseHDHWdemandList = genericProfiles_data.houseHDHWdemandList();
List<Double> buildingEdemandList = genericProfiles_data.buildingEdemandList();
List<Double> buildingHeatDemandList = genericProfiles_data.buildingHeatDemandList();
List<Double> industrySteelEdemandList = genericProfiles_data.industrySteelEdemandList();
List<Double> industrySteelHdemandList = genericProfiles_data.industrySteelHdemandList();
List<Double> industryOtherEdemandList = genericProfiles_data.industryOtherEdemandList();
List<Double> industryOtherHdemandList = genericProfiles_data.industryOtherHdemandList();
List<Double> logisticsFleetEdemandList = genericProfiles_data.logisticsFleetEdemandList();     
 
// Weather data
List<Double> windList = genericProfiles_data.windList();
List<Double> solarList = genericProfiles_data.solarList();
List<Double> tempList = genericProfiles_data.tempList();
List<Double> epexList = genericProfiles_data.epexList(); 

double[] a_arguments = new double[argumentsList.size()];
double[] a_windValues = new double[argumentsList.size()];
double[] a_solarValues = new double[argumentsList.size()];
double[] a_tempValues = new double[argumentsList.size()];
double[] a_houseEdemand = new double[argumentsList.size()];
double[] a_houseDHWdemand = new double[argumentsList.size()];
double[] a_buildingEdemand = new double[argumentsList.size()];
double[] a_buildingHeatDemand = new double[argumentsList.size()];
double[] a_industrySteelEdemand = new double[argumentsList.size()];
double[] a_industrySteelHdemand = new double[argumentsList.size()];
double[] a_industryOtherEdemand = new double[argumentsList.size()];
double[] a_industryOtherHdemand = new double[argumentsList.size()];
double[] a_logisticsFleetEdemand = new double[argumentsList.size()];  
double[] a_epexValues = new double[argumentsList.size()];
            
for(int i = 0; i < argumentsList.size(); i++) {
       a_arguments[i] = argumentsList.get(i);
       a_windValues[i] = windList.get(i);
       a_solarValues[i] = solarList.get(i);
       a_tempValues[i] = tempList.get(i);
       a_houseEdemand[i] = houseEdemandList.get(i);
       a_houseDHWdemand[i] = houseHDHWdemandList.get(i);
       a_buildingEdemand[i] = buildingEdemandList.get(i);
       a_buildingHeatDemand[i] = buildingHeatDemandList.get(i);
       a_industrySteelEdemand[i] = industrySteelEdemandList.get(i);
       a_industrySteelHdemand[i] = industrySteelHdemandList.get(i);
       a_industryOtherEdemand[i] = industryOtherEdemandList.get(i);
       a_industryOtherHdemand[i] = industryOtherHdemandList.get(i);
       a_logisticsFleetEdemand[i] = logisticsFleetEdemandList.get(i);        
       a_epexValues[i] = epexList.get(i);
}


J_ProfilePointer profilePointer;
// 'ambient' conditions:
energyModel.tf_ambientTemperature_degC.setArgumentsAndValues(a_arguments, a_tempValues);
energyModel.tf_dayAheadElectricityPricing_eurpMWh.setArgumentsAndValues(a_arguments, a_epexValues);
profilePointer = new J_ProfilePointer("Day ahead electricity pricing [eur/MWh]", energyModel.tf_dayAheadElectricityPricing_eurpMWh);
energyModel.f_addProfile(profilePointer);
energyModel.pp_dayAheadElectricityPricing_eurpMWh = profilePointer;

energyModel.tf_p_wind_e_normalized.setArgumentsAndValues(a_arguments, a_windValues);
profilePointer = new J_ProfilePointer("normalized onshore wind production", energyModel.tf_p_wind_e_normalized);
energyModel.f_addProfile(profilePointer);
energyModel.pp_windOnshoreProduction = profilePointer;

energyModel.tf_p_solar_e_normalized.setArgumentsAndValues(a_arguments, a_solarValues);
profilePointer = new J_ProfilePointer("normalized_PV_production", energyModel.tf_p_solar_e_normalized);
energyModel.f_addProfile(profilePointer);
energyModel.pp_solarPVproduction = profilePointer;

// Consumption profiles:

//energyModel.tf_p_house_e_demand_other.setArgumentsAndValues(a_arguments, a_houseEdemand);
TableFunction tf_p_house_e_demand_other = new TableFunction(a_arguments, a_houseEdemand, TableFunction.InterpolationType.INTERPOLATION_LINEAR, 2, TableFunction.OutOfRangeAction.OUTOFRANGE_REPEAT, 0.0);
profilePointer = new J_ProfilePointer("house_other_electricity_demand", tf_p_house_e_demand_other);
energyModel.f_addProfile(profilePointer);

//energyModel.tf_p_house_h_demand_hot_water.setArgumentsAndValues(a_arguments, a_houseDHWdemand);
TableFunction tf_p_house_h_demand_hot_water = new TableFunction(a_arguments, a_houseDHWdemand, TableFunction.InterpolationType.INTERPOLATION_LINEAR, 2, TableFunction.OutOfRangeAction.OUTOFRANGE_REPEAT, 0.0);
profilePointer = new J_ProfilePointer("house_hot_water_demand", tf_p_house_h_demand_hot_water);
energyModel.f_addProfile(profilePointer);

//energyModel.tf_p_building_e_demand_other.setArgumentsAndValues(a_arguments, a_buildingEdemand);
TableFunction tf_p_building_e_demand_other = new TableFunction(a_arguments, a_buildingEdemand, TableFunction.InterpolationType.INTERPOLATION_LINEAR, 2, TableFunction.OutOfRangeAction.OUTOFRANGE_REPEAT, 0.0);
profilePointer = new J_ProfilePointer("Office_other_electricity", tf_p_building_e_demand_other);
energyModel.f_addProfile(profilePointer);

//energyModel.tf_p_building_h_demand.setArgumentsAndValues(a_arguments, a_buildingHeatDemand);
TableFunction tf_p_building_h_demand = new TableFunction(a_arguments, a_buildingHeatDemand, TableFunction.InterpolationType.INTERPOLATION_LINEAR, 2, TableFunction.OutOfRangeAction.OUTOFRANGE_REPEAT, 0.0);
profilePointer = new J_ProfilePointer("Building_heat_demand", tf_p_building_h_demand);
energyModel.f_addProfile(profilePointer);

//energyModel.tf_p_industry_other_e_demand.setArgumentsAndValues(a_arguments, a_industryOtherEdemand);
TableFunction tf_p_industry_other_e_demand = new TableFunction(a_arguments, a_industryOtherEdemand, TableFunction.InterpolationType.INTERPOLATION_LINEAR, 2, TableFunction.OutOfRangeAction.OUTOFRANGE_REPEAT, 0.0);
profilePointer = new J_ProfilePointer("Industry_other_electricity", tf_p_industry_other_e_demand);
energyModel.f_addProfile(profilePointer);

//energyModel.tf_p_industry_other_h_demand.setArgumentsAndValues(a_arguments, a_industryOtherHdemand);
TableFunction tf_p_industry_other_h_demand = new TableFunction(a_arguments, a_industryOtherHdemand, TableFunction.InterpolationType.INTERPOLATION_LINEAR, 2, TableFunction.OutOfRangeAction.OUTOFRANGE_REPEAT, 0.0);
profilePointer = new J_ProfilePointer("Industry_other_heat", tf_p_industry_other_h_demand);
energyModel.f_addProfile(profilePointer);

//energyModel.tf_p_industry_steel_e_demand.setArgumentsAndValues(a_arguments, a_industrySteelEdemand);
TableFunction tf_p_industry_steel_e_demand = new TableFunction(a_arguments, a_industrySteelEdemand, TableFunction.InterpolationType.INTERPOLATION_LINEAR, 2, TableFunction.OutOfRangeAction.OUTOFRANGE_REPEAT, 0.0);
profilePointer = new J_ProfilePointer("Industry_steel_electricity", tf_p_industry_steel_e_demand);
energyModel.f_addProfile(profilePointer);

//energyModel.tf_p_industry_steel_h_demand.setArgumentsAndValues(a_arguments, a_industrySteelHdemand);
TableFunction tf_p_industry_steel_h_demand = new TableFunction(a_arguments, a_industrySteelHdemand, TableFunction.InterpolationType.INTERPOLATION_LINEAR, 2, TableFunction.OutOfRangeAction.OUTOFRANGE_REPEAT, 0.0);
profilePointer = new J_ProfilePointer("Industry_steel_heat", tf_p_industry_steel_h_demand);
energyModel.f_addProfile(profilePointer);

//energyModel.tf_p_logistics_fleet_e_hgv.setArgumentsAndValues(a_arguments, a_logisticsFleetEdemand);
TableFunction tf_p_logistics_fleet_e_hgv = new TableFunction(a_arguments, a_logisticsFleetEdemand, TableFunction.InterpolationType.INTERPOLATION_LINEAR, 2, TableFunction.OutOfRangeAction.OUTOFRANGE_REPEAT, 0.0);
profilePointer = new J_ProfilePointer("LOGISTICS_FLEET_HGV_E", tf_p_logistics_fleet_e_hgv);
energyModel.f_addProfile(profilePointer);

//
double[] a_flatProfile = new double[a_arguments.length];
for (int i = 0; i<a_arguments.length; i++){
	a_flatProfile[i] = 1;
}
TableFunction tf_p_flat_profile = new TableFunction(a_arguments, a_flatProfile, TableFunction.InterpolationType.INTERPOLATION_LINEAR, 2, TableFunction.OutOfRangeAction.OUTOFRANGE_REPEAT, 0.0);
profilePointer = new J_ProfilePointer("FLAT_PROFILE", tf_p_flat_profile);
energyModel.f_addProfile(profilePointer);





/*ALCODEEND*/}

double f_createGenericCompanies()
{/*ALCODESTART::1726584205799*/
//Initialize variables
List<GridConnection> generic_company_GCs = new ArrayList();


for (Building_data genericCompany : c_GenericCompanyBuilding_data) {

	//Create new companyGC
	GCUtility companyGC = energyModel.add_UtilityConnections();
	
	//Update counter and collection	 
	v_numberOfCompaniesNoSurvey++;
	generic_company_GCs.add(companyGC);
	
	//Set parameters for the Grid Connection
	companyGC.p_gridConnectionID = genericCompany.address_id();
	
	// Check that is needed until connectioncapacity is no longer in 'Panden' excel
	if (genericCompany.contracted_capacity_kw() == null || genericCompany.contracted_capacity_kw() <= 0) {
		companyGC.p_physicalConnectionCapacity_kW = avgc_data.p_avgUtilityConnectionCapacity_kW;
		companyGC.p_contractedDeliveryCapacity_kW = avgc_data.p_avgUtilityConnectionCapacity_kW;
		companyGC.p_contractedFeedinCapacity_kW = avgc_data.p_avgUtilityConnectionCapacity_kW;
	}
	else{
		companyGC.p_contractedDeliveryCapacity_kW = genericCompany.contracted_capacity_kw();
		companyGC.p_contractedFeedinCapacity_kW = companyGC.p_contractedDeliveryCapacity_kW;
		companyGC.p_physicalConnectionCapacity_kW = companyGC.p_contractedDeliveryCapacity_kW;
	}
	
	companyGC.b_isRealDeliveryCapacityAvailable = false;
	companyGC.b_isRealFeedinCapacityAvailable = false;

	companyGC.p_heatingType = avgc_data.p_avgCompanyHeatingMethod; // Assuming all avg companies have GASBURNER.
	
	
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
	companyGC.p_parentNodeElectricID = genericCompany.trafo_id ();
	
	// Create new actor and assign GC to that
	ConnectionOwner COC = energyModel.add_pop_connectionOwners(); // Create Connection owner company
		
	COC.p_actorID = genericCompany.address_id();
	COC.p_actorType = OL_ActorType.CONNECTIONOWNER;
	COC.p_connectionOwnerType = OL_ConnectionOwnerType.COMPANY;
	COC.p_detailedCompany = false;
	
	companyGC.p_owner = COC;
	companyGC.p_ownerID = COC.p_actorID;
	
	
	//Create GIS object and connect
	GIS_Building b = f_createGISBuilding( genericCompany, companyGC );
	v_totalFloorAreaAnonymousCompanies_m2 += b.p_floorSurfaceArea_m2;
	
	//Style building
	b.p_defaultFillColor = zero_Interface.v_companyBuildingColor;
	b.p_defaultLineColor = zero_Interface.v_companyBuildingLineColor;
	zero_Interface.f_styleAreas(b);
}

//Amount of generic companies created
traceln("Number of companies created without survey: " + v_numberOfCompaniesNoSurvey);
v_remainingNumberOfCompaniesNoSurvey = v_numberOfCompaniesNoSurvey;

//Create EA after all grid connections have been made -> needed because total surfaces are unkown before that
for (GridConnection GCcompany : generic_company_GCs ) {
	

	//Calculating total floor area of all buildings attached to the GC
	for (int k = 0; k < GCcompany.c_connectedGISObjects.size(); k++ ){
		GIS_Building b = (GIS_Building)GCcompany.c_connectedGISObjects.get(k);
		GCcompany.p_floorSurfaceArea_m2 += b.p_floorSurfaceArea_m2;
		GCcompany.p_roofSurfaceArea_m2 += b.p_roofSurfaceArea_m2;
	}
	
	//create the energy assets for each GC
	f_iEAGenericCompanies(GCcompany);

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

/*
if (buildingData.latitude() == null|| buildingData.longitude() == null){
	
	//Use the first point of the polygon as lat lon
	double[] gisregion_points = b.gisRegion.getPoints(); // get all points of the gisArea of the building in the format lat1,lon1,lat2,lon2, etc.
	
	//position and coordinates
	b.p_latitude = gisregion_points[0];
	b.p_longitude = gisregion_points[1];
}
else{
	//position and coordinates
	b.p_latitude = buildingData.latitude();
	b.p_longitude = buildingData.longitude();
}
*/

//Use the first point of the polygon as lat lon
double[] gisregion_points = b.gisRegion.getPoints(); // get all points of the gisArea of the building in the format lat1,lon1,lat2,lon2, etc.

//position and coordinates
b.p_latitude = gisregion_points[0];
b.p_longitude = gisregion_points[1];
//Set latlon
b.setLatLon(b.p_latitude, b.p_longitude);


//Define surface area (with Null checks)
if (buildingData.cumulative_floor_surface_m2() == null && buildingData.polygon_area_m2() == null){
	b.p_floorSurfaceArea_m2 = b.gisRegion.area();
	b.p_roofSurfaceArea_m2 = b.gisRegion.area();
}
else if (buildingData.cumulative_floor_surface_m2() == null){
	b.p_roofSurfaceArea_m2 = buildingData.polygon_area_m2();
	b.p_floorSurfaceArea_m2 = b.p_roofSurfaceArea_m2;
}
else if(buildingData.polygon_area_m2() == null){
	b.p_floorSurfaceArea_m2 = buildingData.cumulative_floor_surface_m2();
	b.p_roofSurfaceArea_m2 = b.gisRegion.area();
}
else{
	b.p_floorSurfaceArea_m2 = buildingData.cumulative_floor_surface_m2();
	b.p_roofSurfaceArea_m2 = buildingData.polygon_area_m2();
}

//Add to collections
zero_Interface.c_GISBuildingShapes.add(b.gisRegion);
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
	f_createPreprocessedElectricityProfile(parentGC, yearlyElectricityDelivery_kWh_array, yearlyElectricityFeedin_kWh_array, yearlyElectricityProduction_kWh_array, pvPower_kW);

} 

else { // Add regular electricity and consumption profiles
	J_EAConsumption profile = new J_EAConsumption(parentGC, OL_EnergyAssetType.ELECTRICITY_DEMAND, profileName, yearlyElectricityDemand_kWh, OL_EnergyCarriers.ELECTRICITY, energyModel.p_timeStep_h, null);
}
/*ALCODEEND*/}

double f_addHeatDemandProfile(GridConnection parentGC,double yearlyGasDemand_m3,boolean hasHourlyGasData,double ratioGasUsedForHeating,String profileName)
{/*ALCODESTART::1726584205805*/
J_EAConsumption heatDemand;
double yearlyDemandHeat_kWh = 0;
double profileTimeStep_hr = 1;
double maxPowerGasburner = 0;

if(hasHourlyGasData){
	J_EAProfile profile = new J_EAProfile(parentGC, OL_EnergyCarriers.HEAT, null, OL_ProfileAssetType.HEATDEMAND ,profileTimeStep_hr);		
	profile.energyAssetName = parentGC.p_ownerID + " custom heat profile";
	
	List<Double> hourlyGasDemand_kWh = selectValues(double.class, "SELECT " + profileName + "_demand FROM comp_gas_consumption;");
	List<Double> hourlyHeatDemand_kWh = new ArrayList<Double>();
	for (int i = 0; i < hourlyGasDemand_kWh.size(); i++) {
		double gasHeatingValue_timestep_kWh = hourlyGasDemand_kWh.get(i) * avgc_data.p_gas_kWhpm3 * avgc_data.p_avgEfficiencyGasBurner * ratioGasUsedForHeating;
		yearlyDemandHeat_kWh += gasHeatingValue_timestep_kWh;
    	hourlyHeatDemand_kWh.add(i, gasHeatingValue_timestep_kWh);
    	
    	//Keep track of max value
    	if((gasHeatingValue_timestep_kWh/energyModel.p_timeStep_h) > maxPowerGasburner){
    		maxPowerGasburner = gasHeatingValue_timestep_kWh/energyModel.p_timeStep_h;
    	}
	}	
	profile.a_energyProfile_kWh = hourlyHeatDemand_kWh.stream().mapToDouble(d -> max(0,d)).toArray();
	
	//Update v_remainingGasConsumption_m3
	v_remainingGasConsumption_m3 -= yearlyDemandHeat_kWh/(avgc_data.p_gas_kWhpm3 * avgc_data.p_avgEfficiencyGasBurner * ratioGasUsedForHeating);
}
else{
	
	
	if (parentGC.p_heatingType == null || parentGC.p_heatingType == OL_GridConnectionHeatingType.NONE || parentGC.p_floorSurfaceArea_m2 == 0 ){ 
		//traceln("NO FLOOR SURFACE OR HEATING TYPE DETECTED");
		return;
	}
	
	//Determine heatdemand
	yearlyDemandHeat_kWh = yearlyGasDemand_m3 * avgc_data.p_gas_kWhpm3 * avgc_data.p_avgEfficiencyGasBurner;
	
	if (yearlyDemandHeat_kWh <= 0 ){// If heat demand = 0, make estimation based on floor surface area
		yearlyDemandHeat_kWh = avgc_data.p_avgCompanyGasConsumption_m3pm2*parentGC.p_floorSurfaceArea_m2 * avgc_data.p_gas_kWhpm3 * avgc_data.p_avgEfficiencyGasBurner;
		//traceln("NO HEAT DEMAND DETECTED: ESTIMATION MADE BASED ON FLOOR SURFACE AREA!");
	}
	
	//Determine heatdemand
	heatDemand = new J_EAConsumption(parentGC, OL_EnergyAssetType.HEAT_DEMAND, profileName, yearlyDemandHeat_kWh, OL_EnergyCarriers.HEAT, energyModel.p_timeStep_h, null);
	
	//Calculate required thermal power
	if(genericProfiles_data.buildingHeatDemandList_maximum() != null){
		maxPowerGasburner = yearlyDemandHeat_kWh*genericProfiles_data.buildingHeatDemandList_maximum();
	}
	else{
		maxPowerGasburner = yearlyDemandHeat_kWh / 8760 * 10;
	}
}

//Initialize parameters
double capacityElectric_kW;
double efficiency;
double baseTemperature_degC;
double outputTemperature_degC;
String ambientTempType;
double sourceAssetHeatPower_kW;
double belowZeroHeatpumpEtaReductionFactor;

switch (parentGC.p_heatingType){ // HOE gaan we om met meerdere heating types in survey???

	case GASBURNER:
		J_EAConversionGasBurner gasBurner = new J_EAConversionGasBurner(parentGC, maxPowerGasburner , avgc_data.p_avgEfficiencyGasBurner, energyModel.p_timeStep_h, 90);
	break;
	
	case HYBRID_HEATPUMP:
		
		//Add primary heating asset (heatpump) (if its not part of the basic profile already
		if(!parentGC.v_hasQuarterHourlyValues || settings.createCurrentElectricityEA()){
			capacityElectric_kW = maxPowerGasburner / 3; //-- /3, want is hybride, dus kleiner
			efficiency = zero_Interface.energyModel.avgc_data.p_avgEfficiencyHeatpump;
			baseTemperature_degC = zero_Interface.energyModel.v_currentAmbientTemperature_degC;
			outputTemperature_degC = zero_Interface.energyModel.avgc_data.p_avgOutputTemperatureHeatpump_degC;
			ambientTempType = "AIR";
			sourceAssetHeatPower_kW = 0;
			belowZeroHeatpumpEtaReductionFactor = 1;
			
			J_EAConversionHeatPump heatPumpHybrid = new J_EAConversionHeatPump(parentGC, capacityElectric_kW, efficiency, energyModel.p_timeStep_h, outputTemperature_degC, baseTemperature_degC, sourceAssetHeatPower_kW, belowZeroHeatpumpEtaReductionFactor );

			zero_Interface.energyModel.c_ambientAirDependentAssets.add(heatPumpHybrid);
		}
		
		//Add secondary heating asset (gasburner)
		efficiency = zero_Interface.energyModel.avgc_data.p_avgEfficiencyGasBurner;
		outputTemperature_degC = zero_Interface.energyModel.avgc_data.p_avgOutputTemperatureGasBurner_degC;
		
		J_EAConversionGasBurner gasBurnerHybrid = new J_EAConversionGasBurner(parentGC, maxPowerGasburner, efficiency, energyModel.p_timeStep_h, outputTemperature_degC);
		parentGC.p_secondaryHeatingAsset = gasBurnerHybrid;
				
	 
	break;
	
	case ELECTRIC_HEATPUMP:

		//Add primary heating asset (heatpump)
		capacityElectric_kW = maxPowerGasburner;
		efficiency = zero_Interface.energyModel.avgc_data.p_avgEfficiencyHeatpump;
		baseTemperature_degC = zero_Interface.energyModel.v_currentAmbientTemperature_degC;
		outputTemperature_degC = zero_Interface.energyModel.avgc_data.p_avgOutputTemperatureHeatpump_degC;
		ambientTempType = "AIR";
		sourceAssetHeatPower_kW = 0;
		belowZeroHeatpumpEtaReductionFactor = 1;
		
		J_EAConversionHeatPump heatPumpElectric = new J_EAConversionHeatPump(parentGC, capacityElectric_kW, efficiency, energyModel.p_timeStep_h, outputTemperature_degC, baseTemperature_degC, sourceAssetHeatPower_kW, belowZeroHeatpumpEtaReductionFactor );		
	break;
	
	default:
		traceln("HEATING TYPE NOT FOUND FOR GC ");
		traceln(parentGC);
}


/*ALCODEEND*/}

double f_createGISParcels()
{/*ALCODESTART::1726584205807*/
for (Parcel_data dataParcel : c_Parcel_data) {
		
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
//String asset_name					= "Production Asset";
double capacityElectric_kW			= 0;
double capacityHeat_kW				= 0;
double yearlyProductionMethane_kWh 	= 0;
double yearlyProductionHydrogen_kWh = 0;
double timestep_h 					= energyModel.p_timeStep_h;
double outputTemperature_degC 		= 0;
J_ProfilePointer profilePointer = null;

switch (asset_type){

case PHOTOVOLTAIC: 
	//asset_name = "Solar Panels";
	profilePointer = energyModel.pp_solarPVproduction;
	capacityElectric_kW = installedPower_kW;
	//traceln("Installing PV for %s with power %s", parentGC.p_ownerID, capacityElectric_kW);
	
break;

case WINDMILL:
	//asset_name = "Windmill onshore";'
	profilePointer=energyModel.pp_windOnshoreProduction;
	capacityElectric_kW = installedPower_kW;
break;

case PHOTOTHERMAL: //NOT USED YET
	//asset_name = "PVT";
	capacityElectric_kW = installedPower_kW*0.5;//??????
	capacityHeat_kW	= installedPower_kW*0.5; // ????????
	outputTemperature_degC = 60; // ??????
break;
}

J_EAProduction production_asset = new J_EAProduction(parentGC, asset_type, asset_name, capacityElectric_kW , capacityHeat_kW, yearlyProductionMethane_kWh, yearlyProductionHydrogen_kWh, timestep_h, outputTemperature_degC, profilePointer);


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

//zero_Interface.c_GISBuildingShapes.add(b.gisRegion);		

return area;

/*ALCODEEND*/}

double f_addEnergyAssetsToHouses(GCHouse house,double jaarlijksGasVerbruik,double jaarlijksElectriciteitsVerbruik)
{/*ALCODESTART::1726584205813*/
//Add generic electricity demand profile 
f_addElectricityDemandProfile(house, jaarlijksElectriciteitsVerbruik, null, false, "House_other_electricity");

//Woonwijk specifiek

if (project_data.project_type() == OL_ProjectType.RESIDENTIAL){
	f_addBuildingHeatModel(house, house.p_floorSurfaceArea_m2, C);
	
	//temporary hardcode household gasburner initialisatie (should be seperate function).
	house.p_heatingType = OL_GridConnectionHeatingType.GASBURNER;
	J_EAConversionGasBurner gasBurner = new J_EAConversionGasBurner(house, 20, 0.99, energyModel.p_timeStep_h, 90);
	
}
else{
	f_addHeatDemandProfile(house, jaarlijksGasVerbruik, false, 1, "Building_heat_demand");
}

if( randomTrue ( 0.1 )){
	double installedRooftopSolar_kW = Double.valueOf( uniform_discr(3,6));
	f_addEnergyProduction(house, OL_EnergyAssetType.PHOTOVOLTAIC, "Residential Solar", installedRooftopSolar_kW );
}

if (randomTrue( 0.05)){
	//f_addElectricVehicle(house, OL_EnergyAssetType.ELECTRIC_VEHICLE, true, 0, 0);
}
else{
	//f_addDieselVehicle(house, OL_EnergyAssetType.DIESEL_VEHICLE, true, 0);
}

/*ALCODEEND*/}

double f_createSurveyCompanies_Zorm()
{/*ALCODESTART::1726584205815*/
traceln("Companies that filled in the survey:");

//Initialize parameters
List<A_SubTenant> subTenants = new ArrayList<A_SubTenant>();

//Get the survey data
List<com.zenmo.zummon.companysurvey.Survey> surveys = f_getSurveys();

//Get the building data
try{
	map_buildingData_Vallum = com.zenmo.vallum.PandKt.fetchBagPanden(surveys);
}
catch (Exception e){ //if api of bag is down, leave bag buildings empty and display error message
	zero_Interface.f_setErrorScreen("BAG API is offline, het is mogelijk dat \n bepaalde panden niet zijn ingeladen!");
}


traceln("Size of survey List: %s", surveys.size());

for (var survey : surveys) {

	
	for (var address : survey.getAddresses()) {
		
		//Update number of survey companies (locations)
		v_numberOfSurveyCompanies++;
		
        for (var gridConnection: address.getGridConnections()) {

	 		//Find the survey_owner (if it already exists)
	 		ConnectionOwner survey_owner = findFirst(energyModel.pop_connectionOwners, CO -> CO.p_actorID.equals(survey.getCompanyName()));
	 		
		 	//Check if it has (or will have) a direct connection with the grid (either gas or electric), if not: create subtenant	
		 	boolean hasNaturalGasConnection = (gridConnection.getNaturalGas().getHasConnection() != null)? gridConnection.getNaturalGas().getHasConnection() : false;	 	
		 	if (!gridConnection.getElectricity().getHasConnection() && !gridConnection.getElectricity().getGridExpansion().getHasRequestAtGridOperator() && !hasNaturalGasConnection){
				subTenants.add(f_createSubtenant(survey, address));	
			 	continue;
		 	}
		 	else if(survey_owner == null){// Connection owner does not exist yet: create and initialize new one
				survey_owner = energyModel.add_pop_connectionOwners();
				survey_owner.p_actorID = survey.getCompanyName();
				survey_owner.p_actorType = OL_ActorType.CONNECTIONOWNER;
				survey_owner.p_connectionOwnerType = OL_ConnectionOwnerType.COMPANY;
				survey_owner.p_detailedCompany = true;
		 	}
			
		 	//Create GC
		 	GCUtility companyGC = energyModel.add_UtilityConnections();		  
		 	
			//Set parameters for the Grid Connection
			companyGC.p_ownerID = survey.getCompanyName();
		 	companyGC.p_gridConnectionID = gridConnection.getSequence().toString() ;

			//Adress data
			companyGC.p_address = new J_Address();
			companyGC.p_address.setStreetName(address.getStreet().substring(0,1).toUpperCase() + address.getStreet().substring(1).toLowerCase());
		 	companyGC.p_address.setHouseNumber(address.getHouseNumber());
		 	companyGC.p_address.setHouseLetter(address.getHouseLetter().equals("") ? null : address.getHouseLetter());
		 	companyGC.p_address.setHouseAddition(address.getHouseNumberSuffix().equals("") ? null : address.getHouseNumberSuffix());
		 	companyGC.p_address.setPostalcode(address.getPostalCode().equals("") ? null : address.getPostalCode().toUpperCase().replaceAll("\\s",""));
		 	companyGC.p_address.setCity(address.getCity().substring(0,1).toUpperCase() + address.getCity().substring(1).toLowerCase());
			
		 	//Find actor and connect GC 
			companyGC.p_owner = survey_owner;	 	
		 	
		 	//Find buildings, and connect 
		 	double totalFloorSurfaceAreaGC_m2 = 0;
		 	double totalRoofSurfaceAreaGC_m2 = 0;
			
			//Get attached building info
		 	List<Building_data> buildings = new ArrayList<Building_data>();
			if ( gridConnection.getPandIds() != null && !gridConnection.getPandIds().isEmpty()) {
				for (var PID : gridConnection.getPandIds() ) {
					Building_data building = findFirst(c_GenericCompanyBuilding_data, b -> b.building_id().equals(PID.getValue()));
					if (building!=null) {
						buildings.add(building);
						c_GenericCompanyBuilding_data.remove(building);
						c_SurveyCompanyBuilding_data.add(building);
						
						//Set trafo ID
						companyGC.p_parentNodeElectricID = building.trafo_id();
					}
					else if(findFirst(energyModel.pop_GIS_Buildings, b -> b.p_id.equals(PID.getValue())) != null){
						GIS_Building gisbuilding = findFirst(energyModel.pop_GIS_Buildings, b -> b.p_id.equals(PID.getValue()));
						if(gisbuilding != null){
							
							//Building already exists: add existing building to GC c_gisObjects and don't make a new one.
							companyGC.c_connectedGISObjects.add(gisbuilding);

							//Accumulate surface areas
							totalFloorSurfaceAreaGC_m2 += gisbuilding.p_floorSurfaceArea_m2; //Gaat dan dubbel met vorige GC die het pand maakte!
							totalRoofSurfaceAreaGC_m2 += gisbuilding.p_roofSurfaceArea_m2; //Gaat dan dubbel met vorige GC die het pand maakte!
						}
					}
					else if (map_buildingData_Vallum != null && !map_buildingData_Vallum.isEmpty()){
						Building_data customBuilding = f_createBuildingData_Vallum(companyGC, PID.getValue());
						buildings.add(customBuilding);
						c_VallumBuilding_data.add(customBuilding);
					}
				}
			} else {
				traceln("Survey %s has no building in zorm", survey.getId());
				buildings = findAll(c_SurveyCompanyBuilding_data, b -> b.gc_id() != null && b.gc_id().equals(companyGC.p_gridConnectionID));
				if(buildings == null){
					traceln("Survey %s has also no manual connection with building in excel", survey.getId());
				}
			}
	
			//Create the GIS buildings
			for (Building_data building : buildings) {
				GIS_Building b = f_createGISBuilding( building, companyGC);				
				
				//Set name of building
				if(b.p_annotation == null){
					b.p_annotation = companyGC.p_ownerID;
				}
				
				//Accumulate surface areas
				totalFloorSurfaceAreaGC_m2 += b.p_floorSurfaceArea_m2;
				totalRoofSurfaceAreaGC_m2 += b.p_roofSurfaceArea_m2;
				
				//Set trafo ID
				companyGC.p_parentNodeElectricID = building.trafo_id();
				
				//Style building
				b.p_defaultFillColor = zero_Interface.v_detailedCompanyBuildingColor;
				b.p_defaultLineColor = zero_Interface.v_detailedCompanyBuildingLineColor;
				zero_Interface.f_styleAreas(b);
				
			}      
			
			//Add (combined) building data to GC (latitude and longitude + area)
			companyGC.p_floorSurfaceArea_m2 = totalFloorSurfaceAreaGC_m2;
			companyGC.p_roofSurfaceArea_m2 = totalRoofSurfaceAreaGC_m2;
			

			if(!companyGC.c_connectedGISObjects.isEmpty()){
				companyGC.p_longitude = companyGC.c_connectedGISObjects.get(0).p_longitude; // Get longitude of first building (only used to get nearest trafo)
				companyGC.p_latitude = companyGC.c_connectedGISObjects.get(0).p_latitude; // Get latitude of first building (only used to get nearest trafo)
			}
			else{
				traceln("Gridconnection %s with owner %s has no buildings!!!", companyGC.p_gridConnectionID, companyGC.p_ownerID);
				companyGC.p_longitude = 0;
				companyGC.p_latitude = 0;
			}
			
			//Set lat lon
			companyGC.setLatLon(companyGC.p_latitude, companyGC.p_longitude); 
			
			//Energy asset initialization
			f_iEASurveyCompanies_Zorm(companyGC, gridConnection); 
        }
    }
}

//Add created subtenants to main tenant(should happen after the other companies have been created)
for(A_SubTenant subtenant : subTenants){

	//Find grid connection that feeds the subtenant (achter de meter)
	GridConnection GC = findFirst(energyModel.f_getGridConnections(), 
	GCU -> GCU.p_address != null && GCU.p_address.getAddress().equals(subtenant.p_address.getAddress()));
	
	if (GC != null){
		subtenant.p_mainTenantID = GC.p_ownerID;
		subtenant.p_connectedGridConnection = GC;
		
		ConnectionOwner owner = findFirst(energyModel.pop_connectionOwners, p -> p.p_actorID.equals(GC.p_ownerID));
		owner.c_subTenants.add(subtenant);
	}
	else {
		traceln("Subtenant '" + subtenant.p_actorID + "' at " + subtenant.p_address.getAddress()+ ", does not have a main tenant");
	}
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

double f_setHeatingTypeSurvey(GridConnection companyGC,com.zenmo.zummon.companysurvey.GridConnection gridConnection,boolean hasHourlyGasData)
{/*ALCODESTART::1726584205825*/
int i = 0;

if(gridConnection.getHeat().getHeatingTypes().size() == 0){
	
	if (gridConnection.getNaturalGas().getAnnualDelivery_m3() != null && gridConnection.getNaturalGas().getAnnualDelivery_m3() > 0) {
		//if (gridConnection.getNaturalGas().getAnnualDelivery_m3() > 0) {
			companyGC.p_heatingType = OL_GridConnectionHeatingType.GASBURNER;// None for now.
			companyGC.c_heatingTypes.add(OL_GridConnectionHeatingType.GASBURNER);
			traceln("Gas consumption detected for '" + companyGC.p_ownerID + "', setting heating type to GASBURNER");			
		/*} else {
			companyGC.p_heatingType = OL_GridConnectionHeatingType.NONE;// None for now.
			traceln("no or incorrect heating type detected for '" + companyGC.p_ownerID + "'");
		}*/
	} else {
		companyGC.p_heatingType = OL_GridConnectionHeatingType.NONE;// None for now.
		traceln("no heating type in surveydata, and no gas consumption detected for: '" + companyGC.p_ownerID + "'");
	}
}		

while (i < gridConnection.getHeat().getHeatingTypes().size()){

	var Heating_Type = gridConnection.getHeat().getHeatingTypes().get(i);

	//Heating type Mixed toevoegen! Dus geen collection
	switch (Heating_Type){
		
		case GAS_BOILER:
			companyGC.p_heatingType = OL_GridConnectionHeatingType.GASBURNER;
			companyGC.c_heatingTypes.add(OL_GridConnectionHeatingType.GASBURNER);
			break;

		case HYBRID_HEATPUMP:
			companyGC.p_heatingType = OL_GridConnectionHeatingType.HYBRID_HEATPUMP;
			companyGC.c_heatingTypes.add(OL_GridConnectionHeatingType.HYBRID_HEATPUMP);
			break;

		case ELECTRIC_HEATPUMP:
			companyGC.p_heatingType = OL_GridConnectionHeatingType.ELECTRIC_HEATPUMP;
			companyGC.c_heatingTypes.add(OL_GridConnectionHeatingType.ELECTRIC_HEATPUMP);
			break;
		
		case DISTRICT_HEATING:
			companyGC.p_heatingType = OL_GridConnectionHeatingType.DISTRICTHEAT;
			companyGC.c_heatingTypes.add(OL_GridConnectionHeatingType.DISTRICTHEAT);
			break;
			
		case OTHER:
			companyGC.p_heatingType = OL_GridConnectionHeatingType.NONE;// Other is not supported by the model so: NONE.
			companyGC.c_heatingTypes.add(OL_GridConnectionHeatingType.OTHER);
			break;
			
		default:
			companyGC.p_heatingType = OL_GridConnectionHeatingType.NONE;
			traceln("no or incorrect heating type detected for '" + companyGC.p_ownerID + "'");
			//companyGC.c_heatingTypes.add(OL_GridConnectionHeatingType.NONE);
	}
	i++;
}

//Set correct primary heating method (p_heatingType) (needed for now, till model can support multiple heating types)
if (companyGC.c_heatingTypes.size()>1){
	
	if (hasHourlyGasData && !companyGC.c_heatingTypes.contains(OL_GridConnectionHeatingType.GASBURNER)){
		companyGC.p_heatingType = OL_GridConnectionHeatingType.GASBURNER;
		return;
	}
	
	if(companyGC.c_heatingTypes.contains(OL_GridConnectionHeatingType.HYBRID_HEATPUMP)){
		companyGC.p_heatingType = OL_GridConnectionHeatingType.HYBRID_HEATPUMP;
		return;
	}
	else if(companyGC.c_heatingTypes.contains(OL_GridConnectionHeatingType.GASBURNER) && (companyGC.c_heatingTypes.contains(OL_GridConnectionHeatingType.ELECTRIC_HEATPUMP))){
		companyGC.p_heatingType = OL_GridConnectionHeatingType.HYBRID_HEATPUMP;
		return;
	}
	else if (companyGC.c_heatingTypes.contains(OL_GridConnectionHeatingType.ELECTRIC_HEATPUMP)){
		companyGC.p_heatingType = OL_GridConnectionHeatingType.ELECTRIC_HEATPUMP;
		return;
	}
	else if(companyGC.c_heatingTypes.contains(OL_GridConnectionHeatingType.GASBURNER)){
		companyGC.p_heatingType = OL_GridConnectionHeatingType.GASBURNER;
		return;
	} 
	

}



/*
//If multiple heating types: Set heating type mixed
if (companyGC.c_heatingTypes.size()>1 && !companyGC.c_heatingTypes.contains(OL_GridConnectionHeatingType.OTHER)|| companyGC.c_heatingTypes.size()>2){
	traceln("Has multiple heating types (excluding OTHER)");
	traceln(companyGC.c_heatingTypes);
	//Set heating type mixed
	companyGC.p_heatingType = OL_GridConnectionHeatingType.MIXED;
}
*/
/*ALCODEEND*/}

double f_addElectricVehicle(GridConnection parentGC,OL_EnergyAssetType vehicle_type,boolean isDefaultVehicle,double annualTravelDistance_km,double maxChargingPower_kW)
{/*ALCODESTART::1726584205827*/
double storageCapacity_kWh 		= 0;
double energyConsumption_kWhpkm = 0;
double capacityElectricity_kW 	= 0;
double stateOfCharge_r  		= 1; // Initial state of charge
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
J_EAEV electricVehicle = new J_EAEV(parentGC, capacityElectricity_kW, storageCapacity_kWh, stateOfCharge_r, timestep_h, energyConsumption_kWhpkm, vehicleScaling, vehicle_type, null);	

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
		energyConsumption_kWhpkm = avgc_data.p_avgDieselConsumptionCar_kWhpkm;
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
	
	break;
	
	case STORAGE_GAS:
	
	break;
	
	default:
	
	return;
}

/*ALCODEEND*/}

double f_iEAGenericCompanies(GridConnection companyGC)
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
current_scenario_list.setCurrentContractDeliveryCapacity_kW(companyGC.p_contractedDeliveryCapacity_kW);
future_scenario_list.setRequestedContractDeliveryCapacity_kW(companyGC.p_contractedDeliveryCapacity_kW);
current_scenario_list.setCurrentContractFeedinCapacity_kW(companyGC.p_contractedDeliveryCapacity_kW);
future_scenario_list.setRequestedContractFeedinCapacity_kW(companyGC.p_contractedDeliveryCapacity_kW);
current_scenario_list.setCurrentPhysicalConnectionCapacity_kW(companyGC.p_contractedDeliveryCapacity_kW);
future_scenario_list.setRequestedPhysicalConnectionCapacity_kW(companyGC.p_contractedDeliveryCapacity_kW);

//Basic heating and electricity demand profiles
if (companyGC.p_floorSurfaceArea_m2 > 0){
	
	if(v_remainingElectricityDelivery_kWh > 0){
		//Buidling Base electricity load
		double Remaining_electricity_demand_kWh_p_m2_yr = v_remainingElectricityDelivery_kWh / v_totalFloorAreaAnonymousCompanies_m2;
		double yearlyElectricityDemand_kWh = Remaining_electricity_demand_kWh_p_m2_yr * companyGC.p_floorSurfaceArea_m2;
		//Add base load profile
		f_addElectricityDemandProfile(companyGC, yearlyElectricityDemand_kWh, null, false, "Office_other_electricity");
	}
	
	if(v_remainingGasConsumption_m3 > 0){
		//Building Gas demand profile (purely heating)
		double Remaining_gas_demand_m3_p_m2_yr = v_remainingGasConsumption_m3/v_totalFloorAreaAnonymousCompanies_m2;
		double yearlyGasDemand_m3 = Remaining_gas_demand_m3_p_m2_yr*companyGC.p_floorSurfaceArea_m2;
		double ratioGasUsedForHeating = 1;
		//Add heat demand profile
		f_addHeatDemandProfile(companyGC, yearlyGasDemand_m3, false, ratioGasUsedForHeating, "Building_heat_demand");
	}
	//Set current scenario heating type
	current_scenario_list.setCurrentHeatingType(companyGC.p_heatingType);
}


//Production asset (PV) ??????????????????????????????????????????? willen we die toevoegen aan generieke bedrijven?
//f_addEnergyProduction(companyGC, OL_EnergyAssetType.PHOTOVOLTAIC, avgc_data.p_avgUtilityPVPower_kWp);



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
	for (int k = 0; k< ceil((double)v_remainingNumberOfCars/(double)v_remainingNumberOfCompaniesNoSurvey); k++){
		f_addDieselVehicle(companyGC, OL_EnergyAssetType.DIESEL_VEHICLE, true, 0);
		v_remainingNumberOfCars--;
		nbCars++;
	}
	
	//Set current scenario trucks
	current_scenario_list.setCurrentDieselCars(nbCars);
}

//Vans
if(v_remainingNumberOfVans > 0){
	int nbVans = 0;
	for (int k = 0; k< ceil((double)v_remainingNumberOfVans/(double)v_remainingNumberOfCompaniesNoSurvey); k++){
		f_addDieselVehicle(companyGC, OL_EnergyAssetType.DIESEL_VAN, true, 0);
		v_remainingNumberOfVans--;
		nbVans++;
	}
	
	//Set current scenario trucks
	current_scenario_list.setCurrentDieselVans(nbVans);
}

//Trucks
if (v_remainingNumberOfTrucks > 0){
	int nbTrucks=0;
	for (int k = 0; k< ceil((double)v_remainingNumberOfTrucks/(double)v_remainingNumberOfCompaniesNoSurvey); k++){
		f_addDieselVehicle(companyGC, OL_EnergyAssetType.DIESEL_TRUCK, true, 0);
		v_remainingNumberOfTrucks--;
		nbTrucks++;
	}
	
	//Set current scenario trucks
	current_scenario_list.setCurrentDieselTrucks(nbTrucks);
}
/*ALCODEEND*/}

double f_createRemainingBuildings()
{/*ALCODESTART::1726584205835*/
for (Building_data remainingBuilding_data : c_remainingBuilding_data) {
	
	GIS_Building building = energyModel.add_pop_GIS_Buildings();
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

double f_iEASurveyCompanies_Excel()
{/*ALCODESTART::1726584205839*/
/*
double ratioGasUsedForHeating = 1;

String idString = companyGC.p_gridConnectionID;
int id = 0;
try {
	id = roundToInt(Double.valueOf(idString));
}
catch (NumberFormatException e) {
	// Probably one of the template GCs?
}

J_scenario_Current current_scenario_list = new J_scenario_Current();
zero_Interface.c_scenarioMap_Current.put(companyGC, current_scenario_list);

J_scenario_Future future_scenario_list = new J_scenario_Future();
zero_Interface.c_scenarioMap_Future.put(companyGC, future_scenario_list);

//Add current grid capacity to current scenario list
current_scenario_list.setCurrentContractDeliveryCapacity_kW(companyGC.p_contractedDeliveryCapacity_kW);

//Add future grid capacity to future scenario list (FOR NOW IT STAYS THE SAME, NO DATA)
future_scenario_list.setRequestedContractDeliveryCapacity_kW(companyGC.p_contractedDeliveryCapacity_kW);

//Electricity consumption
//traceln("Company connection id: %s", selectFrom(comp_connections).where(comp_connections.detail_id.eq(idString)).firstResult(comp_connections.name));
//traceln("id uit pandentabel id: %s", id);


//if( selectFrom(comp_connections).where(comp_connections.gc_id.eq(idString)).list().get(0).get(comp_connections.has_quarterhourly_values)) { // Check wetehr there is 15-min data available.
//if( selectFrom(comp_connections).where(comp_connections.detail_id.eq(id)).kwartierwaardes.equals("ja")){ // Check wetehr there is 15-min data available.
if (row.get( comp_connections.has_quarterhourly_values )) {	
	f_addElectricityDemandProfile(companyGC, 0, null, true, "ccid" + id);
} else { // If not: Use yearly consumption and generic profiles
	f_addElectricityDemandProfile(companyGC, row.get( comp_connections.electricity_consumption_k_wh_year ), null, false, "Office_other_electricity");
}

v_remainingElectricityDelivery_kWh -= row.get( comp_connections.electricity_consumption_k_wh_year );



//Gas consumption
double gasConsumption_m3 = row.get( comp_connections.gas_consumption_m3_year );

//selectFrom(comp_connections).where(comp_connections.gc_id.eq(idString)).firstResult(comp_connections.gas_consumption_m3_year);
//traceln("gasConsumption_m3: %s",gasConsumption_m3);
//traceln("companyGC.p_floorSurfaceArea_m2: %s", companyGC.p_floorSurfaceArea_m2);
if (gasConsumption_m3 > 0) {
	if (companyGC.p_ownerID.equals("Applied Medical (2/2)")) {
		gasConsumption_m3 *= 0.25; // Most of the gas consumption is not for heating
	}
	f_addHeatDemandProfile(companyGC, gasConsumption_m3, false, ratioGasUsedForHeating);
}

v_remainingGasConsumption_m3 -= gasConsumption_m3;

//PV panels
int installedPV_kW = row.get( comp_connections.pv_installed_kw );
//(int) selectFrom(comp_connections).where(comp_connections.gc_id.eq(idString)).firstResult(comp_connections.pv_installed_kw);
if( installedPV_kW > 0){
	f_addEnergyProduction(companyGC, OL_EnergyAssetType.PHOTOVOLTAIC, "solar panels", installedPV_kW);
}

current_scenario_list.setCurrentPV_kW(installedPV_kW);
future_scenario_list.setPlannedPV_year( row.get( comp_connections.pv_investment_year ) );
//selectFrom(comp_connections).where(comp_connections.gc_id.eq(idString)).firstResult(comp_connections.pv_investment_year));
future_scenario_list.setPlannedPV_kW( row.get( comp_connections.pv_investment_kw ) );
//selectFrom(comp_connections).where(comp_connections.gc_id.eq(idString)).firstResult(comp_connections.pv_investment_kw));

//Vehicles
//Trucks
int nbTrucks = row.get( comp_connections.nb_trucks );
//selectFrom(comp_connections).where(comp_connections.gc_id.eq(idString)).firstResult(comp_connections.nb_trucks);
int nbEVTrucks = row.get( comp_connections.nb_etrucks );
//selectFrom(comp_connections).where(comp_connections.gc_id.eq(idString)).firstResult(comp_connections.nb_etrucks);
	
if (nbTrucks > 0) {
	for (int i = 0; i<nbEVTrucks; i++) {
		f_addElectricVehicle(companyGC, OL_EnergyAssetType.ELECTRIC_TRUCK, true, 0, 0);
		v_remainingAmountOfTrucks--;		
		
	}
	for (int i = 0; i<(nbTrucks-nbEVTrucks); i++) {
		f_addDieselVehicle(companyGC, OL_EnergyAssetType.DIESEL_TRUCK, true, 0);	
		v_remainingAmountOfTrucks--;	
	}
}
current_scenario_list.setCurrentDieselTrucks(nbTrucks);
current_scenario_list.setCurrentEVTrucks(nbEVTrucks);
future_scenario_list.setPlannedEVTrucks( row.get( comp_connections.nb_planned_etrucks ) );
//selectFrom(comp_connections).where(comp_connections.gc_id.eq(idString)).firstResult(comp_connections.nb_planned_etrucks));

//Vans
int nbVans =  row.get( comp_connections.nb_vans );
//selectFrom(comp_connections).where(comp_connections.gc_id.eq(idString)).firstResult(comp_connections.nb_vans);
int nbEVans =  row.get( comp_connections.nb_evans );
//selectFrom(comp_connections).where(comp_connections.gc_id.eq(idString)).firstResult(comp_connections.nb_evans);
	
if (nbVans>0) {
	for (int i = 0; i<nbEVans; i++) {
		f_addElectricVehicle(companyGC, OL_EnergyAssetType.ELECTRIC_VAN, true, 0, 0);
		v_remainingNumberOfVans--;
		
	}
	for (int i = 0; i<(nbVans-nbEVans); i++) {
		f_addDieselVehicle(companyGC, OL_EnergyAssetType.DIESEL_VAN, true, 0);	
		v_remainingNumberOfVans--;
	}
}

current_scenario_list.setCurrentDieselVans(nbVans);
current_scenario_list.setCurrentEVVans(nbEVans);
future_scenario_list.setPlannedEVVans( row.get( comp_connections.nb_planned_evans ) );
//selectFrom(comp_connections).where(comp_connections.gc_id.eq(idString)).firstResult(comp_connections.nb_planned_evans));

//Cars	
int nbCars = row.get( comp_connections.nb_cars );
//selectFrom(comp_connections).where(comp_connections.gc_id.eq(idString)).firstResult(comp_connections.nb_cars);
int nbEVs = row.get( comp_connections.nb_evs );
//selectFrom(comp_connections).where(comp_connections.gc_id.eq(idString)).firstResult(comp_connections.nb_evs);

if (nbCars>0) {
	for (int i = 0; i<nbEVs; i++) {
		f_addElectricVehicle(companyGC, OL_EnergyAssetType.ELECTRIC_VEHICLE, true, 0, 0);
	}
	for (int i = 0; i<(nbCars-nbEVs); i++) {
		f_addDieselVehicle(companyGC, OL_EnergyAssetType.DIESEL_VEHICLE, true, 0);	
	}
}

current_scenario_list.setCurrentDieselCars(nbCars);
current_scenario_list.setCurrentEVCars(nbEVs);
future_scenario_list.setPlannedEVCars( row.get( comp_connections.nb_planned_evs ) );
//selectFrom(comp_connections).where(comp_connections.gc_id.eq(idString)).firstResult(comp_connections.nb_planned_evs));


*/
/*ALCODEEND*/}

double f_addBuildingHeatModel(GridConnection parentGC,double floorArea_m2,OL_IsolationLevelHouse isolationLevel)
{/*ALCODESTART::1726584205841*/
double maxPowerHeat_kW = 100; 				//Dit is hoeveel vermogen het huis kan afgeven/opnemen, mag willekeurige waarden hebben. Wordt alleen gebruikt in rekenstap van ratio of capacity
double lossfactor_WpK; 						//Dit is wat bepaalt hoeveel warmte het huis verliest/opneemt per tijdstap per delta_T 
double initialTemp = uniform_discr(15,22); 	//starttemperatuur
double heatCapacity_JpK; 					//hoeveel lucht zit er in je huis dat je moet verwarmen?
double solarAbsorptionFactor_m2; 			//hoeveel m2 effectieve ramen zijn er die opwarmen met zonneinstraling

switch (isolationLevel){
	case A:
		lossfactor_WpK = 0.20 * floorArea_m2;
	break;
	case B:
		lossfactor_WpK = 0.35 * floorArea_m2;
	break;
	case C:
		lossfactor_WpK = 0.55 * floorArea_m2;
	break;
	case D:
		lossfactor_WpK = 0.75 * floorArea_m2;
	break;
	default:
		lossfactor_WpK = uniform (0.4, 0.8) * floorArea_m2;
	break;
}

solarAbsorptionFactor_m2 = floorArea_m2 * 0.01; //solar irradiance [W/m2] 

heatCapacity_JpK = floorArea_m2 * 500000;

parentGC.p_BuildingThermalAsset = new J_EABuilding( parentGC, maxPowerHeat_kW, lossfactor_WpK, energyModel.p_timeStep_h, initialTemp, heatCapacity_JpK, solarAbsorptionFactor_m2 );
/*ALCODEEND*/}

double f_createChargingStationsScale()
{/*ALCODESTART::1726584205843*/
List<Tuple> initialList = selectFrom(chargepoints).list();
List<Tuple> scopedList = new ArrayList<>();

for (Tuple tuple : initialList) {
	for (int i = 0; i < c_gridNodeIDsInScope.size() - 1; i++){
		if (tuple.get(chargepoints.trafo_id).equals( c_gridNodeIDsInScope.get(i)) ){
			scopedList.add(tuple);
		}
	}	
}

traceln("Aantal laadstations in input data (huidig + toekomstig): " + scopedList.size());

//Create CPO agent
ConnectionOwner	CPO;
CPO = energyModel.add_pop_connectionOwners();
CPO.p_actorID = "CPO";
CPO.p_actorType = OL_ActorType.CONNECTIONOWNER;
CPO.p_connectionOwnerType = OL_ConnectionOwnerType.CHARGEPOINT_OP;

for (Tuple row : scopedList){
 	GCPublicCharger charger = energyModel.add_PublicChargers();
	charger.set_p_gridConnectionID( row.get( chargepoints.gc_id ) );
	charger.set_p_name( row.get( chargepoints.gc_name ) );
	charger.set_p_ownerID(CPO.p_actorID );
	charger.set_p_floorSurfaceArea_m2( 1 ); 
	charger.set_p_parentNodeElectricID( row.get( chargepoints.trafo_id ) );
	charger.v_isActiveCharger = row.get(chargepoints.exists_already);
	//	charger.v_isPaused = !row.get( chargepoints.initially_active );
	charger.set_p_latitude( row.get( chargepoints.latitude ) );
	charger.set_p_longitude( row.get( chargepoints.longitude ) );
	charger.setLatLon(charger.p_latitude, charger.p_longitude);
	
	//TODO: set Connection capacity (Can't be 0 anymore in engine) 1 assumed now, wrong so: FIX THIS.
	charger.set_p_physicalConnectionCapacity_kW(1);
	
	//THIS IS A QUICK FIX FOR SCALE. THAT USES THE nbOfChargers TO DETERMINE WHEN TO TURN IT ON OR OFF (TO AVOID ADDING PARAMETERS FOR A QUICK FIX)
	charger.p_nbOfChargers = uniform_discr(1,4) / 4.0;
	 
	GIS_Object area = energyModel.add_pop_GIS_Objects();
		
	//position and coordinates
	area.set_p_latitude( row.get( chargepoints.latitude ) );
	area.set_p_longitude( row.get( chargepoints.longitude ) );
	area.setLatLon( area.p_latitude, area.p_longitude );		
	
	//Create gisregion
	area.gisRegion = f_createGISRegionChargingStation( area.p_latitude, area.p_longitude );	
	
	//Set gis object type
	area.p_GISObjectType = OL_GISObjectType.CHARGER;
	
	//Add to collections			
	charger.c_connectedGISObjects.add(area);
	area.c_containedGridConnections.add(charger);
	
	//zero_Interface.uI_Tabs.tabElectricity.p_nbChargersInDatabase ++;
	//TODO: load the profile from excel column pofile_data
	charger.p_chargingProfileName = "cs_" + uniform_discr(1, 50);
	if (charger.v_isActiveCharger){
		f_addChargingDemandProfile( charger, charger.p_chargingProfileName );
		charger.p_isInitialCharger = true;
		//zero_Interface.uI_Tabs.tabElectricity.v_currentNbChargers ++;
		zero_Interface.c_fixedPublicChargers.add(charger);
				
		//Style building
		area.set_p_defaultFillColor( zero_Interface.v_chargingStationColor );
		area.set_p_defaultLineColor( zero_Interface.v_chargingStationLineColor );
		traceln("adding active charger");
	}
	else {
		area.gisRegion.setVisible(false);
		zero_Interface.c_inactivePublicChargers.add(charger);
		
		//Style building
		area.set_p_defaultFillColor( zero_Interface.v_newChargingStationColor );
		area.set_p_defaultLineColor( zero_Interface.v_newChargingStationLineColor );
	}
	zero_Interface.f_styleAreas(area);
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
for (Chargingstation_data dataChargingStation : c_Chargingstation_data){

	GCPublicCharger chargingStation = energyModel.add_PublicChargers();

	chargingStation.set_p_gridConnectionID( dataChargingStation.gc_id());
	chargingStation.set_p_name( dataChargingStation.gc_name() );
	
	//Electric Capacity
	if (dataChargingStation.connection_capacity_kw() != null) {
		// Assume the connection capacity is both physical and contracted.
		chargingStation.set_p_physicalConnectionCapacity_kW( dataChargingStation.connection_capacity_kw() );
		chargingStation.set_p_contractedDeliveryCapacity_kW( dataChargingStation.connection_capacity_kw() );
		chargingStation.b_isRealPhysicalCapacityAvailable = true;
		chargingStation.b_isRealDeliveryCapacityAvailable = true;
	}
	
	chargingStation.set_p_heatingType( OL_GridConnectionHeatingType.NONE );
	
	//Set parent node
	chargingStation.p_parentNodeElectricID = dataChargingStation.trafo_id();
	
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
	chargingStation.set_p_owner( owner );
	
	
	//Check if centre or single
	if (dataChargingStation.is_charging_centre()) {
	
		if (chargingStation.p_ownerID == null){
			chargingStation.p_ownerID = "Publiek laadstation " + laadstation_nr;
			laadstation_nr++;
		}

		chargingStation.set_p_nbOfChargers( dataChargingStation.number_of_chargers() );
		chargingStation.set_p_maxChargingPower_kW( dataChargingStation.power_per_charger_kw() );
		
		//If check on connection capacity to prevent more charging than possible
		if(chargingStation.p_contractedDeliveryCapacity_kW > chargingStation.p_nbOfChargers*chargingStation.p_maxChargingPower_kW){
			chargingStation.p_contractedDeliveryCapacity_kW = chargingStation.p_nbOfChargers*chargingStation.p_maxChargingPower_kW;
		}
		
		//Set vehicle type
		chargingStation.p_chargingVehicleType = dataChargingStation.vehicle_type();		
		
		//Create vehicles that charge at the charging centre
		for(int k = 0; k < chargingStation.p_nbOfChargers*avgc_data.p_avgVehiclesPerChargePoint; k++ ){
			f_addElectricVehicle(chargingStation, chargingStation.p_chargingVehicleType, true, 0, chargingStation.p_maxChargingPower_kW);
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
			area.set_p_defaultFillColor( zero_Interface.v_chargingStationColor_centre );
			area.set_p_defaultLineColor( zero_Interface.v_chargingStationLineColor_centre );
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
			
			//Create vehicles that charge at the charging centre
			for(int k = 0; k < avgc_data.p_avgVehiclesPerChargePoint; k++ ){
				f_addElectricVehicle(chargingStation, chargingStation.p_chargingVehicleType, true, 0, chargingStation.p_maxChargingPower_kW);
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
			
			area.set_p_defaultFillColor( zero_Interface.v_chargingStationColor );
			area.set_p_defaultLineColor( zero_Interface.v_chargingStationLineColor );
			zero_Interface.f_styleAreas(area);
	}
	
}

/*ALCODEEND*/}

double f_createCompaniesFromDatabase()
{/*ALCODESTART::1726584205851*/
List<Building_data> scopedBuilding_data = f_getScopedBuildingList(c_GenericCompanyBuilding_data);

traceln("Aantal bedrijven in BAG data (geen woonfunctie): " + scopedBuilding_data.size());

int i = 0;
for (Building_data dataBuilding : scopedBuilding_data) {
	GCUtility company = energyModel.add_UtilityConnections();
	
	//Set parameters for the Grid Connection
 	company.p_gridConnectionID = dataBuilding.address_id();
	company.p_ownerID = "Bedrijf " + Integer.toString(i);	//aanname : huiseigenaar is eigenbaas
	company.p_purposeBAG = dataBuilding.purpose();
	company.p_heatingType = avgc_data.p_avgHouseHeatingMethod ;
	company.p_floorSurfaceArea_m2 = dataBuilding.cumulative_floor_surface_m2();
	//company.p_bouwjaar = dataBuilding.bouwjaar();
	company.p_physicalConnectionCapacity_kW = 50.0; //// HARDCODED?????
	company.p_contractedDeliveryCapacity_kW = 50.0; //// HARDCODED?????
	company.p_contractedFeedinCapacity_kW = 50.0; //// HARDCODED?????
	
	//adres gegevens
	company.p_address = new J_Address();
	company.p_address.setStreetName(dataBuilding.streetname());
	company.p_address.setHouseNumber(dataBuilding.house_number());
	company.p_address.setHouseLetter(dataBuilding.house_letter());
	company.p_address.setHouseAddition(dataBuilding.house_addition());
	company.p_address.setPostalcode(dataBuilding.postalcode());
	company.p_address.setCity(dataBuilding.city());

	//locatie
	company.p_longitude = dataBuilding.longitude();
	company.p_latitude = dataBuilding.latitude();
	company.setLatLon(company.p_latitude, company.p_longitude);
	
	//Connect GC to grid node
	company.p_parentNodeElectricID = dataBuilding.trafo_id();
		
	//Create and set owner
	ConnectionOwner	companyOwner = energyModel.add_pop_connectionOwners();
	companyOwner.p_actorID = company.p_ownerID;
	companyOwner.p_actorType = OL_ActorType.CONNECTIONOWNER;
	companyOwner.p_connectionOwnerType = OL_ConnectionOwnerType.COMPANY;
	companyOwner.p_detailedCompany = false;
	company.p_owner = companyOwner;
	
	//Create GIS building
	GIS_Building b;
	int pandClusterNr = dataBuilding.pandcluster_nr();
	if( pandClusterNr == 0 ){	
		b = f_createGISBuilding( dataBuilding, company );
	}
	else {
		b = randomWhere(energyModel.c_GISBuildingClusters, x -> x.p_pandcluster_nr == pandClusterNr);
		if (b == null){
			b = f_createGISBuilding( dataBuilding, company );
		}
		else {
			b.c_containedGridConnections.add(company);
			company.c_connectedGISObjects.add(b);
		}
	}
	
	//Style building
	b.p_defaultFillColor = zero_Interface.v_companyBuildingColor;
	b.p_defaultLineColor = zero_Interface.v_companyBuildingLineColor;
	zero_Interface.f_styleAreas(b);
	
	//Instantiate energy assets
	double jaarlijksElectriciteitsVerbruik;
	double jaarlijksGasVerbruik;
	try {
		jaarlijksElectriciteitsVerbruik = dataBuilding.electricity_consumption_kwhpa();
		jaarlijksGasVerbruik = dataBuilding.gas_consumption_kwhpa();
	}
	catch (NullPointerException e){
		jaarlijksElectriciteitsVerbruik = Double.valueOf(uniform_discr(5000, 10000)); // HARDCODED???
		jaarlijksGasVerbruik =  Double.valueOf(uniform_discr(600, 2000)); //// HARDCODED?????
	}
	f_addElectricityDemandProfile(company, jaarlijksElectriciteitsVerbruik, null, false, "Office_other_electricity");

	i++;
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

List<Building_data> f_getScopedBuildingList(List<Building_data> initialList)
{/*ALCODESTART::1726584205857*/
List<Building_data> scopedList = new ArrayList<>();

for (Building_data dataBuilding : initialList) {
	for (int i = 0; i < c_gridNodeIDsInScope.size() - 1; i++){
		if (dataBuilding.trafo_id().equals( c_gridNodeIDsInScope.get(i)) ){
			scopedList.add(dataBuilding);
		}
	}	
}

return scopedList;
/*ALCODEEND*/}

double f_createGISCables()
{/*ALCODESTART::1726584205859*/

//LV cables
//Initialize the array with gisroutes 
GISRoute[] gisroutesLV = new GISRoute[c_Cable_data_LV.size()];
int i = 0;


for (Cable_data dataCableLV : c_Cable_data_LV) {
	gisroutesLV[i] = zero_Interface.f_createGISLine(f_createGISObjectsTokens(dataCableLV.line(), OL_GISObjectType.LV_CABLE), "LVGrid");
	i++;
}

//Create LV network
//zero_Interface.f_createGISNetwork(gisroutesLV, "LVGrid");


//MV cables
//Initialize the array with gisroutes 
GISRoute[] gisroutesMV = new GISRoute[c_Cable_data_MV.size()];
int k = 0;


for (Cable_data dataCableMV : c_Cable_data_MV) {
	gisroutesMV[k] = zero_Interface.f_createGISLine(f_createGISObjectsTokens(dataCableMV.line(), OL_GISObjectType.MV_CABLE), "MVGrid");
	k++;
}

//Create MV network
//zero_Interface.f_createGISNetwork(gisroutesMV, "MVGrid");

/*ALCODEEND*/}

double f_createPreprocessedElectricityProfile(GridConnection parentGC,double[] yearlyElectricityDelivery_kWh,double[] yearlyElectricityFeedin_kWh,double[] yearlyElectricityProduction_kWh,Double pvPower_kW)
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
			double pvPowerEstimate_kW = pvPower_kW * energyModel.tf_p_solar_e_normalized(v_simStartHour_h+i*0.25);
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
			pvPowerEstimate_kW = pvPower_kW * energyModel.tf_p_solar_e_normalized(v_simStartHour_h+i*0.25);
			
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
 
if (v_remainingElectricityDelivery_kWh < 0){
	traceln("v_remainingElectricityDelivery_kWh became negative at GC: %s", parentGC);
}
/*ALCODEEND*/}

double f_setHouseHeatingPreferences(GCHouse house)
{/*ALCODESTART::1726584205863*/
house.v_nightTempSetpoint_degC = uniform_discr(12,18); // HARDCODED??
house.v_dayTempSetpoint_degC = uniform_discr(18, 24); // HARDCODED??
house.v_heatingOn_time = uniform_discr(5,10) + uniform_discr(0,4) / 4.0; // HARDCODED??
house.v_heatingOff_time = uniform_discr(21,23); // HARDCODED??
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
traceln("Companies that filled in the survey:");

/*
	// Find all the buildings that have specified a gridconnection id.
	List<Tuple> buildingRows = selectFrom(buildings)
		.where(buildings.gc_id.isNotNull())
		.list();
	
	// Loop through the comp_connections
	List<Tuple> connectionRows = selectFrom(comp_connections).list();
	
	for (Tuple connectionRow : connectionRows) {	
		
		if (connectionRow.get(comp_connections.is_template)) {
			// Templates are descriptions of generic companies, each row in the buildings excel represents one such company.
			for (Tuple buildingRow : buildingRows) {
				if (buildingRow.get( buildings.gc_id ).equals(connectionRow.get(comp_connections.gc_id))) {
					GCUtility companyGC = energyModel.add_UtilityConnections();					
					
					companyGC.p_gridConnectionID = buildingRow.get( buildings.adress_id );
					// For excel-inputs we use annotation for the company name. These were added by hand, in the future we will probably change this.
					companyGC.p_ownerID = buildingRow.get( buildings.annotation);
					companyGC.p_physicalConnectionCapacity_kW = connectionRow.get(comp_connections.connection_capacity_demand);
					companyGC.p_contractedDeliveryCapacity_kW = connectionRow.get(comp_connections.connection_capacity_demand);
					companyGC.p_contractedFeedinCapacity_kW = connectionRow.get(comp_connections.connection_capacity_supply);

					// Finding the ConnectionOwner
					ConnectionOwner COC = findFirst(energyModel.pop_connectionOwners, p -> p.p_actorID.equals(companyGC.p_ownerID) );
					if (COC == null) {
						traceln("GC with id %s has no COC", companyGC.p_gridConnectionID );
						COC = energyModel.add_pop_connectionOwners();
						COC.p_actorID = companyGC.p_ownerID;
						COC.p_actorType = OL_ActorType.CONNECTIONOWNER;
						COC.p_connectionOwnerType = OL_ConnectionOwnerType.COMPANY;
						COC.p_detailedCompany = false;
					}
					companyGC.p_owner = COC;
					
					// Create a building
					GIS_Building b = f_createGISBuilding(buildingRow, companyGC);
					
					companyGC.p_floorSurfaceArea_m2 = b.p_floorSurfaceArea_m2;
					companyGC.p_roofSurfaceArea_m2 = b.p_roofSurfaceArea_m2;

					// Style the building
					b.p_defaultFillColor = zero_Interface.v_companyBuildingColor;
					b.p_defaultLineColor = zero_Interface.v_companyBuildingLineColor;
					zero_Interface.f_styleAreas(b);
	
					// Trafo data
					companyGC.set_p_parentNodeElectricID( buildingRow.get ( buildings.trafo_id ) );				
					companyGC.p_longitude = companyGC.c_connectedGISObjects.get(0).p_longitude;
					companyGC.p_latitude = companyGC.c_connectedGISObjects.get(0).p_latitude;
					companyGC.setLatLon(companyGC.p_latitude, companyGC.p_longitude);  
				
					// Adress data
					companyGC.p_streetName = buildingRow.get( buildings.streetname);
				 	companyGC.p_houseNumber = buildingRow.get( buildings.house_number);
				 	companyGC.p_houseLetter = buildingRow.get( buildings.house_letter);
				 	companyGC.p_houseAddition = buildingRow.get( buildings.house_addition);
				 	companyGC.p_postalcode = buildingRow.get( buildings.postalcode);
				 	companyGC.p_city = buildingRow.get( buildings.city);
			 									 	
					// Heating type
					companyGC.p_heatingType = OL_GridConnectionHeatingType.GASBURNER;
					
					//Calculate remaining floor surface area for the unknown company calculations
					v_remainingFloorArea_m2 -= companyGC.p_floorSurfaceArea_m2;
					
					// Instantiate the energy assets based on the comp_connection excel
					f_iEASurveyCompanies_Excel(connectionRow, companyGC);
					
				}
			}
		}
		else {
			// The row in the comp_connections represents one single company
			GCUtility companyGC = energyModel.add_UtilityConnections();
			
			companyGC.p_gridConnectionID = connectionRow.get(comp_connections.gc_id);
			companyGC.p_ownerID = connectionRow.get( comp_connections.name );
			companyGC.p_physicalConnectionCapacity_kW = connectionRow.get(comp_connections.connection_capacity_demand);
			companyGC.p_contractedDeliveryCapacity_kW = connectionRow.get(comp_connections.connection_capacity_demand);
			companyGC.p_contractedFeedinCapacity_kW = connectionRow.get(comp_connections.connection_capacity_supply);
			
			// Finding the connection owner
			ConnectionOwner COC = findFirst(energyModel.pop_connectionOwners, p -> p.p_actorID.equals(companyGC.p_ownerID) );
			if (COC == null) {
				throw new IllegalStateException("Detailed company " + companyGC.p_ownerID + " has no connection owner.");
			}
			companyGC.p_owner = COC;
			
			// The non template companies can have multiple buildings
			double totalFloorSurfaceAreaGC_m2 = 0;
			double totalRoofSurfaceAreaGC_m2 = 0;
			
			for (Tuple buildingRow : buildingRows) {
				if (buildingRow.get( buildings.gc_id ).equals(companyGC.p_gridConnectionID)) {
					GIS_Building b = f_createGISBuilding( buildingRow, companyGC);				

					totalFloorSurfaceAreaGC_m2 += b.p_floorSurfaceArea_m2;
					totalRoofSurfaceAreaGC_m2 += b.p_roofSurfaceArea_m2;
					companyGC.p_parentNodeElectricID = buildingRow.get(buildings.trafo_id);
					
					//Style building
					if (COC.p_detailedCompany) {
						b.p_defaultFillColor = zero_Interface.v_detailedCompanyBuildingColor;
						b.p_defaultLineColor = zero_Interface.v_detailedCompanyBuildingLineColor;
					}
					else {
						b.p_defaultFillColor = zero_Interface.v_companyBuildingColor;
						b.p_defaultLineColor = zero_Interface.v_companyBuildingLineColor;
					}
					zero_Interface.f_styleAreas(b);
					
					//Adress data (Keeps overwriting this info each time a new building is found, ends with data from last building)
					companyGC.p_streetName = buildingRow.get( buildings.streetname );
				 	companyGC.p_houseNumber = buildingRow.get( buildings.house_number );
				 	companyGC.p_houseLetter = buildingRow.get( buildings.house_letter );
				 	companyGC.p_houseAddition = buildingRow.get( buildings.house_addition );
				 	companyGC.p_postalcode = buildingRow.get( buildings.postalcode );
				 	companyGC.p_city = buildingRow.get( buildings.city );
				 	
				 	// Set Trafo ID
				 	companyGC.p_parentNodeElectricID = buildingRow.trafo_id();
				 }
			}
				
			//Add (combined) building data to GC (latitude and longitude + area)
			companyGC.p_floorSurfaceArea_m2 = totalFloorSurfaceAreaGC_m2;
			companyGC.p_roofSurfaceArea_m2 = totalRoofSurfaceAreaGC_m2;
			
			if (companyGC.c_connectedGISObjects.size() == 0) {
				throw new IllegalStateException("Detailed company " + companyGC.p_ownerID + " has no building.");
			}
			companyGC.p_longitude = companyGC.c_connectedGISObjects.get(0).p_longitude; // Get longitude of first building (only used to get nearest trafo)
			companyGC.p_latitude = companyGC.c_connectedGISObjects.get(0).p_latitude; // Get latitude of first building (only used to get nearest trafo)
			companyGC.setLatLon(companyGC.p_latitude, companyGC.p_longitude);  
			
			// Heating type
			companyGC.p_heatingType = OL_GridConnectionHeatingType.GASBURNER;
					
			//Calculate remaining floor surface area for the unknown company calculations
			v_remainingFloorArea_m2 -= totalFloorSurfaceAreaGC_m2;
			
			// Instantiate the energy assets based on the comp_connection excel
			f_iEASurveyCompanies_Excel(connectionRow, companyGC);
		}
	}
}
*/
/*ALCODEEND*/}

double f_createConsumerGC_businesspark()
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

double f_createConsumerGC_Residential()
{/*ALCODESTART::1726584205879*/
//Create houses
f_createHousesFromDatabase();

//Create companies
f_createCompaniesFromDatabase();

//Charge stations?
//f_createChargingStationsScale();

/*ALCODEEND*/}

double f_createAdditionalGISObjects()
{/*ALCODESTART::1726584205881*/
//Area outlines
f_createGISRegionOutline();

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

double f_getPreprocessedConsumptionTotal(GridConnection parentGC,double annualElectricityDelivery_kWh,Integer annualElectricityFeedin_kWh,Integer annualElectricityProduction_kWh,Double pvPower_kW)
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

TableFunction tf_customPVproduction_fr = new TableFunction(a_arguments, a_normalizedPower_fr, TableFunction.InterpolationType.INTERPOLATION_LINEAR, 2, TableFunction.OutOfRangeAction.OUTOFRANGE_ERROR, 0.0);
J_ProfilePointer profilePointer = new J_ProfilePointer((parentGC.p_ownerID + "_PVproduction") , tf_customPVproduction_fr);
energyModel.f_addProfile(profilePointer);
J_EAProduction production_asset = new J_EAProduction(parentGC, OL_EnergyAssetType.PHOTOVOLTAIC, (parentGC.p_ownerID + "_rooftopPV"), (double)pvPower_kW, 0.0, 0.0, 0.0, energyModel.p_timeStep_h, 0.0, profilePointer);

traceln("Custom PV asset added to GC: " + parentGC.p_ownerID);
//traceln("Custom PV asset added to %s with installed power %s kW and %s full load hours!", parentGC.p_ownerID, pvPower_kW, fullLoadHours_h);

/*ALCODEEND*/}

double f_iEASurveyCompanies_Zorm(GridConnection companyGC,com.zenmo.zummon.companysurvey.GridConnection gridConnection)
{/*ALCODESTART::1732112244908*/
//Initialize boolean that sets the creation of currently existing electric (demand) EA
boolean createElectricEA = true;
final var targetYear = 2023;


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
companyGC.p_contractedDeliveryCapacity_kW = 0.0;
companyGC.p_contractedFeedinCapacity_kW = 0.0;
companyGC.p_physicalConnectionCapacity_kW = 0.0;

f_createDieselTractors(companyGC, gridConnection.getTransport().getAgriculture());

//Check for electricity connection and data
if (gridConnection.getElectricity().getHasConnection()){
	
	if ((gridConnection.getElectricity().getContractedConnectionCapacityKw() == null || 
		gridConnection.getElectricity().getContractedConnectionCapacityKw() <= 0) &&
		(gridConnection.getElectricity().getGrootverbruik().getPhysicalCapacityKw() == null || 
		gridConnection.getElectricity().getGrootverbruik().getPhysicalCapacityKw() <= 0)) {
		
		traceln("SURVEYOWNER HAS NOT FILLED IN CONNECTION CAPACITY!!! AVG values taken");
		companyGC.p_contractedDeliveryCapacity_kW = avgc_data.p_avgUtilityConnectionCapacity_kW;
		companyGC.p_contractedFeedinCapacity_kW = avgc_data.p_avgUtilityConnectionCapacity_kW;
		companyGC.p_physicalConnectionCapacity_kW = avgc_data.p_avgUtilityConnectionCapacity_kW;
		
		companyGC.b_isRealPhysicalCapacityAvailable = false;
		companyGC.b_isRealDeliveryCapacityAvailable = false;
		companyGC.b_isRealFeedinCapacityAvailable = false;
	}
	else{
	
		if(gridConnection.getElectricity().getContractedConnectionCapacityKw() != null && gridConnection.getElectricity().getContractedConnectionCapacityKw() > 0){
			companyGC.p_contractedDeliveryCapacity_kW = gridConnection.getElectricity().getContractedConnectionCapacityKw(); //Contracted connection capacity
			companyGC.b_isRealDeliveryCapacityAvailable = true;
		}
		else{
			companyGC.p_contractedDeliveryCapacity_kW = ((double)gridConnection.getElectricity().getGrootverbruik().getPhysicalCapacityKw()); //Contracted connection capacity
			companyGC.b_isRealDeliveryCapacityAvailable = false;
		}
		
		
		//Check if contract capacity feedin has been filled in: if not, make the same as contract delivery
		if(gridConnection.getElectricity().getGrootverbruik().getContractedConnectionSupplyCapacityKw() != null && gridConnection.getElectricity().getGrootverbruik().getContractedConnectionSupplyCapacityKw() > 0){
			companyGC.p_contractedFeedinCapacity_kW = ((double)gridConnection.getElectricity().getGrootverbruik().getContractedConnectionSupplyCapacityKw()); //Contracted connection capacity
			companyGC.b_isRealFeedinCapacityAvailable = true;
		}
		else{
			if(pvPower_kW != null){
				companyGC.p_contractedFeedinCapacity_kW = pvPower_kW;
			}
			else{
				companyGC.p_contractedFeedinCapacity_kW = 0.0;
			}
			companyGC.b_isRealFeedinCapacityAvailable = false;
		}
		
		//Check if physical capacity has been filled in: if not, make the same as maximum of contract delivery and feedin
		if(gridConnection.getElectricity().getGrootverbruik().getPhysicalCapacityKw() != null && gridConnection.getElectricity().getGrootverbruik().getPhysicalCapacityKw() > 0){
			companyGC.p_physicalConnectionCapacity_kW = (double)gridConnection.getElectricity().getGrootverbruik().getPhysicalCapacityKw(); //Contracted connection capacity
			companyGC.b_isRealPhysicalCapacityAvailable = true;
		}
		else{
			companyGC.p_physicalConnectionCapacity_kW = max(companyGC.p_contractedDeliveryCapacity_kW, companyGC.p_contractedFeedinCapacity_kW); //Contracted connection capacity
			companyGC.b_isRealPhysicalCapacityAvailable = false;
		}
	}
	
	//Add to current scenario list
	current_scenario_list.setCurrentContractDeliveryCapacity_kW(companyGC.p_contractedDeliveryCapacity_kW);
	current_scenario_list.setCurrentContractFeedinCapacity_kW(companyGC.p_contractedFeedinCapacity_kW);
	current_scenario_list.setCurrentPhysicalConnectionCapacity_kW(companyGC.p_physicalConnectionCapacity_kW);
	
	
	//Electricity consumption profile
	String profileName = "Office_other_electricity";
	
	//Check if quarter hourly values are available in vallum
	boolean createdTimeSeriesAssets = f_createElectricityTimeSeriesAssets(companyGC, gridConnection, "Insert company name here");
	
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
			yearlyElectricityConsumption_kWh = f_getPreprocessedConsumptionTotal(companyGC, yearlyElectricityDelivery_kWh, yearlyElectricityFeedin_kWh, yearlyElectricityProduction_kWh, pvPower_kW);
			
			//If no electricity consumption, determine the consumption based on average values and floor surface and connection capacity
			if(yearlyElectricityConsumption_kWh == 0){
				yearlyElectricityConsumption_kWh = avgc_data.p_avgCompanyElectricityConsumption_kWhpm2*companyGC.p_floorSurfaceArea_m2;
				
				//Check if it is within the contracted limits (peak should at least be 20% lower than contracted capacity
				if(genericProfiles_data.buildingEdemandList_maximum() != null && yearlyElectricityConsumption_kWh*genericProfiles_data.buildingEdemandList_maximum() > 0.8*companyGC.p_contractedDeliveryCapacity_kW){
					yearlyElectricityConsumption_kWh = 0.8*companyGC.p_contractedDeliveryCapacity_kW/genericProfiles_data.buildingEdemandList_maximum();
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
if(companyGC.p_contractedDeliveryCapacity_kW == 0 && companyGC.p_contractedFeedinCapacity_kW == 0 && companyGC.p_physicalConnectionCapacity_kW == 0){
	companyGC.v_isActive = false;
}
		
//Grid expansion request
future_scenario_list.setRequestedContractDeliveryCapacity_kW(companyGC.p_contractedDeliveryCapacity_kW);
if (gridConnection.getElectricity().getGridExpansion().getHasRequestAtGridOperator() != null && gridConnection.getElectricity().getGridExpansion().getHasRequestAtGridOperator()){
	future_scenario_list.setRequestedContractDeliveryCapacity_kW(((gridConnection.getElectricity().getGridExpansion().getRequestedKW() != null) ? gridConnection.getElectricity().getGridExpansion().getRequestedKW() : 0) + companyGC.p_contractedDeliveryCapacity_kW);
	future_scenario_list.setRequestedContractFeedinCapacity_kW(((gridConnection.getElectricity().getGridExpansion().getRequestedKW() != null) ? gridConnection.getElectricity().getGridExpansion().getRequestedKW() : 0) + companyGC.p_contractedDeliveryCapacity_kW);
	future_scenario_list.setRequestedPhysicalConnectionCapacity_kW(((gridConnection.getElectricity().getGridExpansion().getRequestedKW() != null) ? gridConnection.getElectricity().getGridExpansion().getRequestedKW() : 0) + companyGC.p_contractedDeliveryCapacity_kW);
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
	if (yearlyElectricityProduction_kWh_array != null && gridConnection.getSupply().getPvInstalledKwp() != null && gridConnection.getSupply().getPvInstalledKwp() > 0){
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
	future_scenario_list.setPlannedPV_kW(gridConnection.getSupply().getPvPlannedKwp()); 
	future_scenario_list.setPlannedPV_year(gridConnection.getSupply().getPvPlannedYear());
	//gridConnection.getSupply().getPvPlannedOrientation();
}

//Planned supply (Wind)
if (gridConnection.getSupply().getWindPlannedKw() != null && gridConnection.getSupply().getWindPlannedKw() > 0){
	future_scenario_list.setPlannedWind_kW(gridConnection.getSupply().getWindPlannedKw());
	// plannedWind_year // ???
}




////Gas
//Initialize variables (outside of gas loop needed for heating)
boolean hasHourlyGasData = false;
double yearlyGasConsumption_m3 = 0;
double ratioGasUsedForHeating = 1;
String heatProfileName = "Building_heat_demand";

if (gridConnection.getNaturalGas().getHasConnection() != null && gridConnection.getNaturalGas().getHasConnection()){
	
	yearlyGasConsumption_m3 = (gridConnection.getNaturalGas().getAnnualDemandM3() != null) ? gridConnection.getNaturalGas().getAnnualDemandM3() : 0; // Yearly electricity consumption (0 if value is null)

	//Check if hourly gas consumption values are available 
	try{
		if (selectFirstValue(Double.class, "SELECT " + "ccid" + gridConnection.getSequence().toString() + "_demand FROM comp_gas_consumption LIMIT 1;") != null){
			hasHourlyGasData = true;
			heatProfileName = "ccid" + companyGC.p_gridConnectionID;
		}
	}
	catch(Exception e) {
		//No hourly data available?
		//Update total Yearly gas consumption (if it is available it happens in the function where data is imported)
		v_remainingGasConsumption_m3 -= yearlyGasConsumption_m3;
		
	}
	
	//Determine how much gas goes towards heating
	ratioGasUsedForHeating = ((gridConnection.getNaturalGas().getPercentageUsedForHeating() != null) ? gridConnection.getNaturalGas().getPercentageUsedForHeating() : 100)/100;
}



	
////Heating
//Determine the current heating type
f_setHeatingTypeSurvey(companyGC, gridConnection, hasHourlyGasData);

//Set the heating demand profile
if(!createElectricEA && (companyGC.p_heatingType == OL_GridConnectionHeatingType.HYBRID_HEATPUMP || companyGC.p_heatingType == OL_GridConnectionHeatingType.ELECTRIC_HEATPUMP)){
	//Dont create additional Electric heating assets on top of Electricity profile
}
else{
	f_addHeatDemandProfile(companyGC, yearlyGasConsumption_m3, hasHourlyGasData, ratioGasUsedForHeating, heatProfileName);
}

//add to scenario: current
current_scenario_list.setCurrentHeatingType(companyGC.p_heatingType);




////Storage
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
companyGC.p_batteryOperationMode = OL_BatteryOperationMode.BALANCE;

//Aansturing toevoegen ?

//add to scenario: current
current_scenario_list.setCurrentBatteryPower_kW(battery_power_kW);
current_scenario_list.setCurrentBatteryCapacity_kWh(battery_capacity_kWh);
	
	
if (gridConnection.getStorage().getHasThermalStorage() != null && gridConnection.getStorage().getHasThermalStorage()){ // Check for thermal storage
	//gridConnection.getStorage().getThermalStorageKw()
	//J_EAStorageHeat(Agent parentAgent, OL_EAStorageTypes heatStorageType, double capacityHeat_kW, double lossFactor_WpK, double timestep_h, double initialTemperature_degC, double minTemperature_degC, double maxTemperature_degC, double setTemperature_degC, double heatCapacity_JpK, String ambientTempType ) {
	//J_EAStorageHeat(companyGC, OL_EAStorageTypes heatStorageType, double capacityHeat_kW, double lossFactor_WpK, double timestep_h, double initialTemperature_degC, double minTemperature_degC, double maxTemperature_degC, double setTemperature_degC, double heatCapacity_JpK, String ambientTempType ) {
	//Denk ook aan aansturing?!!
}

if (gridConnection.getStorage().getHasPlannedBattery() != null && gridConnection.getStorage().getHasPlannedBattery()){ // Check for planned battery
	future_scenario_list.setPlannedBatteryCapacity_kWh(gridConnection.getStorage().getPlannedBatteryCapacityKwh());
	future_scenario_list.setPlannedBatteryPower_kW(gridConnection.getStorage().getPlannedBatteryPowerKw());
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
		
		//Planned
		future_scenario_list.setPlannedEVCars((gridConnection.getTransport().getCars().getNumPlannedElectricCars() != null) ? gridConnection.getTransport().getCars().getNumPlannedElectricCars() : 0);
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
		future_scenario_list.setPlannedEVVans((gridConnection.getTransport().getVans().getNumPlannedElectricVans() != null) ? gridConnection.getTransport().getVans().getNumPlannedElectricVans() : 0);
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
		future_scenario_list.setPlannedEVTrucks((gridConnection.getTransport().getTrucks().getNumPlannedElectricTrucks() != null) ? gridConnection.getTransport().getTrucks().getNumPlannedElectricTrucks() : 0);
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

//Create a building_data record
Building_data building_data_record = Building_data.builder().

address_id("verblijfsobject." + PandID).
building_id("pand." + PandID).
streetname(companyGC.p_address.getStreetName()).
house_number(companyGC.p_address.getHouseNumber()).
house_letter(companyGC.p_address.getHouseLetter()).
house_addition(companyGC.p_address.getHouseAddition()).
postalcode(companyGC.p_address.getPostalcode()).
city(companyGC.p_address.getPostalcode()).
build_year(pand_data_vallum.getBouwjaar()).	
status(pand_data_vallum.getStatus()).
//purpose(row.get( buildings.purpose )).
//cumulative_floor_surface_m2(row.get( buildings.cumulative_floor_surface_m2 )).
//polygon_area_m2(row.get( buildings.polygon_area_m2 )).
annotation(companyGC.p_gridConnectionID).
//extra_info(row.get( buildings.extra_info )).
//trafo_id(row.get( buildings.trafo_id )).
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

for (int i = 0; i < numTractors; i++) {
    new J_EADieselTractor(companyGridConnection, annualDiesel_L / numTractors, customProfiles_data.getValuesArray(), energyModel.p_timeStep_h);
}
/*ALCODEEND*/}

boolean f_createElectricityTimeSeriesAssets(GridConnection gridConnection,com.zenmo.zummon.companysurvey.GridConnection gridConnectionSurvey,String companyName)
{/*ALCODESTART::1738248965949*/
var targetYear = 2023;
var electricitySurvey = gridConnectionSurvey.getElectricity();

double[] deliveryTimeSeries_kWh = f_timeSeriesToDoubleArray(electricitySurvey.getQuarterHourlyDelivery_kWh());
if (deliveryTimeSeries_kWh == null) {
	// delivery is the minimum we require to do anything with timeseries data
	return false;
}

double[] feedInTimeSeries_kWh = f_timeSeriesToDoubleArray(electricitySurvey.getQuarterHourlyFeedIn_kWh());
double[] productionTimeSeries_kWh = f_timeSeriesToDoubleArray(electricitySurvey.getQuarterHourlyProduction_kWh());

Double pvPower_kW = Optional.ofNullable(gridConnectionSurvey.getSupply().getPvInstalledKwp())
	.map(it -> (double) it)
	.orElse(null);

//Preprocess the arrays and create the consumption pattern
f_createPreprocessedElectricityProfile(gridConnection, deliveryTimeSeries_kWh, feedInTimeSeries_kWh, productionTimeSeries_kWh, pvPower_kW);

gridConnection.v_hasQuarterHourlyValues = true;

return true;
/*ALCODEEND*/}

double[] f_timeSeriesToDoubleArray(com.zenmo.zummon.companysurvey.TimeSeries timeSeries)
{/*ALCODESTART::1738572338816*/
var targetYear = 2023;
if (timeSeries == null) {
	return null;
}

if (!timeSeries.hasNumberOfValuesForOneYear()) {
	traceln("Time series has too few values for one year");
	return null;
}

return f_convertFloatArrayToDoubleArray(
	timeSeries.convertToQuarterHourly().getFullYearOrFudgeIt(targetYear)
);
/*ALCODEEND*/}

