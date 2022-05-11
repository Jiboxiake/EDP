package dedp.DistanceOracles;
//TODO: modify range and omit common bits.
public class Parser {
    public static int max_lat=Integer.MIN_VALUE;
    public static int min_lat=Integer.MAX_VALUE;
    public static int max_long=Integer.MIN_VALUE;
    public static int min_long=Integer.MAX_VALUE;
    public static int normalizeLat(int latitude){
        //assert(latitude>=min_lat&&latitude<=max_lat);
        float z=((float)(latitude-min_lat)/(float)(max_lat-min_lat));
        double interM=(1073741824*z);
        int result = (int)interM;//todo: try to change it by a multiple of 2
        result <<=1;
        return result;
    }
    public static int normalizeLon(int longitude){
        //assert(longitude>=min_long&&longitude<=max_long);
        float z=(float)(longitude-min_long)/(float)(max_long-min_long);
        double interM=(1073741824*z);
        int result = (int)interM;//todo: try to change it by a multiple of 2
        result <<=1;
        return result;
    }
    public static void feedLat(int lat){
        if(lat<min_lat){
            min_lat=lat;
        }
        if(lat>max_lat){
            max_lat=lat;
        }
    }
    public static void feedLon(int lon){
        if(lon<min_long){
            min_long=lon;
        }
        if(lon>max_long){
            max_long=lon;
        }
    }
}
