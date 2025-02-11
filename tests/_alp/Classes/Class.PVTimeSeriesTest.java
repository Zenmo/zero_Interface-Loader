
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


import org.junit.jupiter.api.Test;
import zerointerfaceloader.Zero_Loader;
import zero_engine.GridConnection;

class PVTimeSeriesTest {
    @Test
    void testAssetCreation() {
    	var loader = new Zero_Loader();
    	var gridConnection = new GridConnection();
    	
    	loader.f_createCustomPVAsset(gridConnection, new double[] {1.2}, 40.0);

        assertEquals(1, gridConnection.c_profileAssets.size());
        
        double sum = Arrays.stream(gridConnection.c_profileAssets.get(0).a_energyProfile_kWh).sum();
        assertEquals(1.2, sum, 0.01);
    }
}
