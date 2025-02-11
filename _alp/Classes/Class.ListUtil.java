
public class ListUtil {
    private ListUtil() {
    }

	static double[] doubleListToArray(List<Double> list) {
		 double[] array = new double[list.size()];
		 for (int i = 0; i < array.length; i++) {
			 array[i] = list.get(i);
		 }
		 return array;
	}
}












