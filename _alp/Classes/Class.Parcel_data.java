/**
 * Parcel_data
 */	
@lombok.Builder
@lombok.Value
@lombok.experimental.Accessors(fluent = true)
public class Parcel_data {
		String parcel_id;
		String name;
		String streetname;
		Integer house_number;
		String house_letter;
		String house_addition;
		String postalcode;
		String city;
		double polygon_area_m2;
		double latitude;
		double longitude;
		String polygon;
}