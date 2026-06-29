/**
 * J_CustomTripTrackerGenerator
 */	
public class J_CustomTripTrackerGenerator {
	public static List<J_ActivityTrackerTrips.TripRecord> getCustomTripTrackerRecords(boolean[][] weeklyTravelMatrix, double weeklyTravelDistance_km) {
		
		//Validate
	    if(!checkIfCustomTripInputsAreValid(weeklyTravelMatrix, weeklyTravelDistance_km)) {
	        throw new IllegalArgumentException("Invalid trip inputs.");
	    }
	    
	    List<J_ActivityTrackerTrips.TripRecord> customTripTrackerRecords = new ArrayList<>();
	    
	    // Count total hours traveled (every true cell) across the whole week
	    int totalHoursTraveled = 0;
	    for (int day = 0; day < weeklyTravelMatrix.length; day++) {
	        for (int hour = 0; hour < weeklyTravelMatrix[day].length; hour++) {
	            if (weeklyTravelMatrix[day][hour]) {
	                totalHoursTraveled++;
	            }
	        }
	    }

	    // Distance covered per hour of travel
	    double distancePerHour_km = (totalHoursTraveled == 0)
	            ? 0.0
	            : weeklyTravelDistance_km / totalHoursTraveled;

	    // Walk the week as one continuous 168-hour timeline, collecting contiguous trips
	    boolean inTrip = false;
	    double tripStart_h = 0.0;

	    for (int absoluteHour = 0; absoluteHour < 7 * 24; absoluteHour++) {
	        int day = absoluteHour / 24;
	        int hour = absoluteHour % 24;
	        boolean driving = weeklyTravelMatrix[day][hour];

	        if (driving && !inTrip) {
	            // Trip begins at the start of this hour
	            inTrip = true;
	            tripStart_h = absoluteHour;
	        } else if (!driving && inTrip) {
	            // Previous hour was the last driving hour; trip ends here
	            inTrip = false;
	            double tripEnd_h = absoluteHour;
	            double distance_km = (tripEnd_h - tripStart_h) * distancePerHour_km;
	            customTripTrackerRecords.add(new J_ActivityTrackerTrips.TripRecord(tripStart_h, tripEnd_h, distance_km));
	        }
	    }

	    // Close a trip that runs to the very end of the week
	    if (inTrip) {
	        double tripEnd_h = 7 * 24;
            double distance_km = (tripEnd_h - tripStart_h) * distancePerHour_km;
            customTripTrackerRecords.add(new J_ActivityTrackerTrips.TripRecord(tripStart_h, tripEnd_h, distance_km));
	    }

	    return customTripTrackerRecords;
	}
	
	public static boolean checkIfCustomTripInputsAreValid(boolean[][] weeklyTravelMatrix, double weeklyTravelDistance_km) {
	    if(weeklyTravelDistance_km <= 0) {
	    	return false;
	    }
	    if (weeklyTravelMatrix.length != 7 || weeklyTravelMatrix[0].length != 24) {
	    	return false;
	    }
	    return true;
	}
	
    public record StoredTripConfiguration(
            boolean[][] buttonConfigurationMatrix,
            double weeklyTravelDistance_km,
            boolean dailyDistinctionEnabled,
            Set<OL_Days> activeDays) {
    }
}