/**
 * MSRegion_data
 */	

@lombok.Builder
@lombok.Value
@lombok.experimental.Accessors(fluent = true)
public class MSRegion_data {
		String region_id;
		String description;
		Color color;
		String polygon;
}
