/**
 * Neighbourhood_data
 */	
@lombok.Builder
@lombok.Value
@lombok.experimental.Accessors(fluent = true)
public class Neighbourhood_data {
	String districtcode;
	String districtname;
	String neighbourhoodcode;
	String neighbourhoodtype; // OL van maken
	double latitude;
	double longitude;
	String polygon;
}