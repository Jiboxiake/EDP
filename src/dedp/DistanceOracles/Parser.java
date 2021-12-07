package dedp.DistanceOracles;
//TODO: modify range and omit common bits.
public class Parser {
    private static final int max_lat=90;
    private static final int min_lat=-90;
    private static final int max_long=180;
    private static final int min_long=-180;
    public static int normalizeLat(double latitude){
        double z=(latitude-(double)min_lat)/((double)max_lat-(double)min_lat);
        int result = (int)(2147483647.0*z);
        return result;
    }
    public static int normalizeLon(double longitude){
        double z=(longitude-(double)min_long)/((double)max_long-(double)min_long);
        int result = (int)(2147483647.0*z);
        return result;
    }
}
