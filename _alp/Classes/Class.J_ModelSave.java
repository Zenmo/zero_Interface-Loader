/**
 * J_ModelSave
 */	
public class J_ModelSave implements Serializable {
	
	public EnergyModel energyModel;
	public ArrayList<GridNode> c_gridNodes = new ArrayList<GridNode>();
	public ArrayList<GIS_Object> c_GISObjects = new ArrayList<GIS_Object>();
	
	public ArrayList<GCUtility> c_orderedPVSystemsCompanies;
	public ArrayList<GCHouse> c_orderedPVSystemsHouses;
	public ArrayList<J_EA> c_orderedVehicles;
	public ArrayList<GCUtility> c_orderedHeatingSystemsCompanies;
	public ArrayList<GCHouse> c_orderedHeatingSystemsHouses;
	public ArrayList<J_EADieselVehicle> c_orderedActiveVehiclesPublicParking;
	public ArrayList<J_EADieselVehicle> c_orderedNonActiveVehiclesPublicParking;
	public ArrayList<J_EAVehicle> c_orderedVehiclesPrivateParking;
	public ArrayList<GIS_Object> c_orderedParkingSpaces;
	
	public ArrayList<J_EAChargePoint> c_orderedV1GChargers;
	public ArrayList<J_EAChargePoint> c_orderedV2GChargers;
	public ArrayList<GCPublicCharger> c_orderedPublicChargers;

    /**
     * Default constructor
     */
    public J_ModelSave() {
    }

	@Override
	public String toString() {
		return super.toString();
	}

	/**
	 * This number is here for model snapshot storing purpose<br>
	 * It needs to be changed when this class gets changed
	 */ 
	private static final long serialVersionUID = 1L;

}