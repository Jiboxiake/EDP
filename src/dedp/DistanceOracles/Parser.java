package dedp.DistanceOracles;
//TODO: modify range and omit common bits.
public class Parser {
    public static float max_lat=-90;
    public static float min_lat=90;
    public static float max_long=-180;
    public static float min_long=180;
    public static int normalizeLat(float latitude){
        //assert(latitude>=min_lat&&latitude<=max_lat);
        float z=(latitude-min_lat)/(max_lat-min_lat);
        double interM=(1073741824*z);
        int result = (int)interM;//todo: try to change it by a multiple of 2
        result <<=1;
        return result;
    }
    public static int normalizeLon(float longitude){
        //assert(longitude>=min_long&&longitude<=max_long);
        float z=(longitude-min_long)/(max_long-min_long);
        double interM=(1073741824*z);
        int result = (int)interM;//todo: try to change it by a multiple of 2
        result <<=1;
        return result;
    }
    public static void feedLat(float lat){
        if(lat<min_lat){
            min_lat=lat;
        }
        if(lat>max_lat){
            max_lat=lat;
        }
    }
    public static void feedLon(float lon){
        if(lon<min_long){
            min_long=lon;
        }
        if(lon>max_long){
            max_long=lon;
        }
    }
}
