/**
 * J_CustomTripTrackerGenerator
 */	
public class J_CustomTripTrackerGenerator {
	public static Map<OL_CustomTripTrackerValueTypes, List<Double>> getCustomTripTrackerValues(Map<OL_Days, Double> tripStartTime_hourOfDay, Map<OL_Days, Double> tripEndTime_hourOfDay, Map<OL_Days, Double> tripTravelDistance_km) {
		
		//Validate
	    if(!checkIfCustomTripInputsAreValid(tripStartTime_hourOfDay, tripEndTime_hourOfDay, tripTravelDistance_km)) {
	        throw new IllegalArgumentException("Invalid trip inputs.");
	    }
	    
	    List<Double> startTimes_h = new ArrayList<>();
	    List<Double> endTimes_h = new ArrayList<>();
	    List<Double> distances_km = new ArrayList<>();

	    // Collect start/end hours per active day (in week order = already sorted within each list)
	    List<Double> startHours = new ArrayList<>();
	    List<Double> startDistances = new ArrayList<>();
	    List<Double> endHours = new ArrayList<>();
	    for(OL_Days day : getOrderedDaysList()) {
	        if(!tripStartTime_hourOfDay.containsKey(day)) continue;
	        double offset = getOffSetSinceStartOfWeek_h(day);
	        startHours.add(tripStartTime_hourOfDay.get(day) + offset);
	        startDistances.add(tripTravelDistance_km.get(day));
	        endHours.add(tripEndTime_hourOfDay.get(day) + offset);
	    }

	    int n = startHours.size();
	    if(n > 0) {
	        // Wrap-around exists if the first end happens before the first start chronologically
	        boolean hasWrap = endHours.get(0) < startHours.get(0);

	        if(hasWrap) {
	            // Last start pairs with first end (+168) — split at the 168h boundary.
	            double lastStart = startHours.get(n - 1);
	            double lastDistance = startDistances.get(n - 1);
	            double firstEnd = endHours.get(0);
	            double totalDuration_h = (168.0 - lastStart) + firstEnd;

	            // Start-of-week segment (wrap remainder)
	            startTimes_h.add(0.0);
	            endTimes_h.add(firstEnd);
	            distances_km.add(lastDistance * firstEnd / totalDuration_h);

	            // Middle pairs: start[i] with end[i+1]
	            for(int i = 0; i < n - 1; i++) {
	                startTimes_h.add(startHours.get(i));
	                endTimes_h.add(endHours.get(i + 1));
	                distances_km.add(startDistances.get(i));
	            }

	            // End-of-week segment
	            startTimes_h.add(lastStart);
	            endTimes_h.add(168.0);
	            distances_km.add(lastDistance * (168.0 - lastStart) / totalDuration_h);
	        } else {
	            // No wrap: each start[i] pairs with end[i]
	            for(int i = 0; i < n; i++) {
	                startTimes_h.add(startHours.get(i));
	                endTimes_h.add(endHours.get(i));
	                distances_km.add(startDistances.get(i));
	            }
	        }
	    }

	    Map<OL_CustomTripTrackerValueTypes, List<Double>> result = new HashMap<>();
	    result.put(OL_CustomTripTrackerValueTypes.STARTTIME_H, startTimes_h);
	    result.put(OL_CustomTripTrackerValueTypes.ENDTIME_H, endTimes_h);
	    result.put(OL_CustomTripTrackerValueTypes.DISTANCE_KM, distances_km);
	    return result;
	}
	
	public static boolean checkIfCustomTripInputsAreValid(Map<OL_Days, Double> tripStartTime_hourOfDay, Map<OL_Days, Double> tripEndTime_hourOfDay, Map<OL_Days, Double> tripTravelDistance_km) {
	    Boolean allOvernight = null;
	    for(OL_Days day : getOrderedDaysList()) {
	        if(!tripStartTime_hourOfDay.containsKey(day)) continue;
	        double s = tripStartTime_hourOfDay.get(day);
	        double e = tripEndTime_hourOfDay.get(day);
	        if(s == e) return false;
	        boolean isOvernight = s > e;
	        if(allOvernight == null) allOvernight = isOvernight;
	        else if(allOvernight != isOvernight) return false;
	    }
	    return true;
	}
	
	private static double getOffSetSinceStartOfWeek_h(OL_Days day) {
		switch(day) {
		    case MONDAY:
		        return 0;
		    case TUESDAY:
		        return 24;
		    case WEDNESDAY:
		        return 48;
		    case THURSDAY:
		        return 72;
		    case FRIDAY:
		        return 96;
		    case SATURDAY:
		        return 120;
		    case SUNDAY:
		        return 144;
		    default:
		    	throw new RuntimeException("Day found that should not exist.");
		}
	}
	
	public static List<OL_Days> getOrderedDaysList(){
		return List.of(
			    OL_Days.MONDAY,
			    OL_Days.TUESDAY,
			    OL_Days.WEDNESDAY,
			    OL_Days.THURSDAY,
			    OL_Days.FRIDAY,
			    OL_Days.SATURDAY,
			    OL_Days.SUNDAY
			);
	}
}