/**
 * GridNode_data
 */	
@lombok.Builder
@lombok.Value
@lombok.experimental.Accessors(fluent = true)
public class GridNode_data {
		String gridnode_id;
		String fid;
		boolean status;
		String type;
		String description;
		double latitude;
		double longitude;
		String parent_node_id;
		boolean is_capacity_available;
		double capacity_kw;
		String subscope;
		String service_area_polygon;
}