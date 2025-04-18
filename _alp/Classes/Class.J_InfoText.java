/**
 * J_InfoText
 */	
public class J_InfoText implements Serializable {

	
	public String lorumIpsum = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui	officia deserunt mollit anim id est laborum.";
    public String householdRooftopPV = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui	officia deserunt mollit anim id est laborum.";
	
    /**
     * Default constructor
     */
    public J_InfoText() {
    }
    
    //public Pair<String, Integer> getLorumIpsum(int width_ch, String descriptionText) {
    	//return this.restrictWidth(descriptionText, width_ch);
    //}

    public Pair<String, Integer> restrictWidth( String txt, int width_ch ) {
    	StringBuilder output = new StringBuilder();
    	int remainingTextSize = txt.length();
    	int currentIndex = 0;
    	int lines = 0;
    	while (remainingTextSize > width_ch) {
    		int i = 0;
    		while (!Character.isWhitespace(txt.charAt(currentIndex + width_ch - i))) {
	    		i++;
	    		if (i > width_ch) {
	    			throw new RuntimeException("Impossible to format string to fit within width.");
	    		}
    		}
			output.append(txt.substring(currentIndex, currentIndex + width_ch - i));
    		output.append('\n');
    		currentIndex += width_ch - i + 1;
    		remainingTextSize -= width_ch - i + 1;
    		lines++;
    	}
    	output.append(txt.substring(currentIndex, txt.length()));
		lines++;
    	return new Pair(output.toString(), lines);
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