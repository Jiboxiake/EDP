package dedp.DistanceOracles;
public class MortonCode {
    public static final long hex16 = 0x0000ffff0000ffffL;
    public static final long hex8 = 0x00ff00ff00ff00ffL;
    public static final long hex4 = 0x0f0f0f0f0f0f0f0fL;
    public static final long hex2 = 0x3333333333333333L;
    public static final long hex1 = 0x5555555555555555L;
    public static final int ihex8 = 0x00ff00ff;
    public static final int ihex4 = 0x0f0f0f0f;
    public static final int ihex2 = 0x33333333;
    public static final int ihex1 = 0x55555555;

    public static final int max_depth=16;
    public static int max_lat=Integer.MIN_VALUE;
    public static int min_lat=Integer.MAX_VALUE;
    public static int max_lon=Integer.MIN_VALUE;
    public static int min_lon=Integer.MAX_VALUE;
    public static double lat_range;
    public static double lon_range;
    public static boolean debug =false;
    public static int max = 65536;
    public static int min =0;
    //public long code;
    public int code;
    public short level;
    public  MortonCode(int latitude, int longitude, int level){
     /*   double z1 = (double)(latitude-min_lat);
        z1 = z1/lat_range;
        double z2 = (double)(longitude-min_lon);
        z2 = z2/lon_range;
        int y = (int)(z1*Math.pow(2.0,max_depth));
        int x= (int)(z2*Math.pow(2.0,max_depth));
        if(debug){
            System.out.println("y is "+y);
            System.out.println("x is "+x+"\n");
        }*/
       /* latitude = Math.abs(latitude);
        longitude = Math.abs(longitude);*/

        int y= interleave(latitude);
        int x= interleave(longitude);
        code= ((x<<1)|y);
        code= code >>>((max_depth-level)*2);
        this.level=(short)level;
    }
    public static void reset(){
        max_lat=Integer.MIN_VALUE;
        min_lat=Integer.MAX_VALUE;
        max_lon=Integer.MIN_VALUE;
        min_lon=Integer.MAX_VALUE;
        lat_range=0.0;
        lon_range=0.0;
        max = 65536;
        min =0;
    }
    public MortonCode(MortonCode mc, int commonPrefx,int level){
        this.code=mc.code;
        this.level=(short)level;
        this.code=this.code>>>(32-commonPrefx);

    }
    public MortonCode(int code,int level){
        this.code=code;
        this.level=(short)level;

    }
    public static long get4DMorton(long lc1, long lc2, int level){
       // System.out.println(Integer.toBinaryString(code1));
       // System.out.println(Long.toBinaryString(lc1));
        lc1=interleave(lc1);
        lc2= interleave(lc2);
        //long code = (longlon<<1)|longlat;
        //System.out.println(Long.toBinaryString(code));
        return ((lc2<<1)|lc1);//>>>((max_depth-level)*4)
    }

    /*
    x := (x | (x << 16)) & X'0000ffff0000ffff' ::bigint;
     	x := (x | (x << 8)) &  X'00ff00ff00ff00ff' ::bigint;
     	x := (x | (x << 4)) &  X'0f0f0f0f0f0f0f0f' ::bigint;
     	x := (x | (x << 2)) &  X'3333333333333333' ::bigint;
    	x := (x | (x << 1)) &  X'5555555555555555' ::bigint;
     */
    private static long interleave(long l){
        l = (l|(l<<16))&hex16;
        l = (l|(l<<8))&hex8;
        l = (l|(l<<4))&hex4;
        l = (l|(l<<2))&hex2;
        l = (l|(l<<1))&hex1;
        return l;
    }
    private static int interleave(int i){
        i=(i|(i<<8))&ihex8;
        i=(i|(i<<4))&ihex4;
        i=(i|(i<<2))&ihex2;
        i=(i|(i<<1))&ihex1;
        return i;
    }
    public static void main(String args[]){
        //System.out.println(Integer.toBinaryString(32768));
        //System.out.println(Long.toBinaryString(hex16));
    /*   long result = Morton.getMorton(43593749,-116411134,20);
        System.out.println(Long.toBinaryString(result));
        long result2 = Morton.getMorton(43588256,-116400148,20);
        System.out.println(Long.toBinaryString(result2));
        long result3 = Morton.getMorton(43582763,-116389160,20);
        System.out.println(Long.toBinaryString(result3));*/
        MortonCode.feedLat(0);
        MortonCode.feedLat(9);
        MortonCode.feedLon(9);
        MortonCode.feedLon(0);
        MortonCode.finishLoading();
        MortonCode m1 = new MortonCode(6,7,10);
        MortonCode m2 = new MortonCode(3,6,10);
        System.out.println(Integer.toBinaryString(m1.code));
        System.out.println(Integer.toBinaryString(m2.code));
        SearchKey k = new SearchKey(m1,m2);
        System.out.println(Long.toBinaryString(k.mc)+" has level "+(int)k.level);
        k.shift();
        System.out.println(Long.toBinaryString(k.mc)+" has level "+(int)k.level);
    }
    public static void feedLon(int lon){
        if(lon>max_lon){
            max_lon=lon;
        }
        if(lon<min_lon){
            min_lon=lon;
        }
    }

    public static void feedLat(int lat){
        if(lat>max_lat){
            max_lat=lat;
        }
        if(lat<min_lat){
            min_lat=lat;
        }
    }
    public void printBit(){
        System.out.println(Integer.toBinaryString(this.code));
    }
    public static void finishLoading(){
        max_lat++;
        max_lon++;
        lat_range = (double)(max_lat-min_lat);
        lon_range = (double)(max_lon-min_lon);
    }
    @Override
    public boolean equals(Object o){
        if(this.code==((MortonCode)o).code&&this.level==((MortonCode)o).level){
            return true;
        }
        return false;
    }
    public static int normalizeLat(int raw){
        double z1 = (double)(raw-min_lat);
        z1 = z1/lat_range;
        int result = (int)(z1*Math.pow(2.0,max_depth));
        return result;
    }
    public static int normalizeLon(int raw){
        double z1 = (double)(raw-min_lon);
        z1 = z1/lon_range;
        int result = (int)(z1*Math.pow(2.0,max_depth));
        return result;
    }
}
