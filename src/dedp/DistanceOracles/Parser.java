package dedp.DistanceOracles;
//TODO: modify range and omit common bits.
public class Parser {
    public static final double max_lat=41.5;
    public static final double min_lat=40.4;
    public static final double max_long=-73.5;
    public static final double min_long=-74.5;
    public static int normalizeLat(double latitude){
        assert(latitude>=min_lat&&latitude<=max_lat);
        double z=(latitude-min_lat)/(max_lat-min_lat);
        int result = (int)(1073741823.0*z);
        return result;
    }
    public static int normalizeLon(double longitude){
        assert(longitude>=min_long&&longitude<=max_long);
        double z=(longitude-min_long)/(max_long-min_long);
        int result = (int)(1073741823.0*z);
        return result;
    }
}
