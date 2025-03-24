/**
 * Electrolyser_data
 */	
@lombok.Builder
@lombok.Value
@lombok.experimental.Accessors(fluent = true)
public class Electrolyser_data {
		String gc_id;
		String gc_name;
		String owner_id;
		String streetname;
		Integer house_number;
		String house_letter;
		String house_addition;
		String postalcode;
		String city;
		String gridnode_id;
		boolean initially_active;
		double capacity_electric_kw;
		double connection_capacity_kw;
		Double contracted_delivery_capacity_kw;
		Double contracted_feed_in_capacity_kw;
		OL_ElectrolyserOperationMode default_operation_mode;
		double conversion_efficiency;
		double min_production_ratio;
		double idle_consumption_power_ratio;
		double start_up_time_shutdown_s;
		double start_up_time_standby_s;
		double start_up_time_idle_s;
		double load_change_time_s;
		double latitude;
		double longitude;
		String polygon;
}