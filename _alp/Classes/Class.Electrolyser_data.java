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
		Double min_production_ratio;
		Double idle_consumption_power_ratio;
		Double start_up_time_shutdown_h;
		Double start_up_time_standby_h;
		Double start_up_time_idle_h;
		Double load_change_time_h;
		double latitude;
		double longitude;
		String polygon;
}