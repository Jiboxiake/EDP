package dedp.DistanceOracles;
//https://stackoverflow.com/questions/58979713/interleave-2-32-bit-integers-into-64-integer
public class MortonCode implements Comparable<MortonCode> {
    public long morton;
    private boolean isNode;
    private int level;//only the first 2*level bits matter here
    public MortonCode (int lat, int lon, int level, boolean isNode){
        this.level=level;
        this.isNode=isNode;
        //lat = (lat-37200000)*2;
        //lon = lon+122600000;
        morton = interleave(lat, lon);
        //morton<<=2;
        if(!isNode){
            int toRemove=64-2*level;
            morton= morton >> toRemove;
            morton= morton << toRemove;
        }else{
            this.level=32;
        }

    }

    public MortonCode(long morton, int level, boolean isNode){
        this.level=level;
        this.isNode=isNode;
        this.morton = morton;
        if(!isNode){
            int toRemove=64-2*level;
            this.morton= this.morton >> toRemove;
            this.morton=this.morton << toRemove;
        }
    }

    public static long interleave(int a, int b) {
        return (spaceOut(a) << 1) | spaceOut(b);
    }

   private static long spaceOut(int a) {
        long x = a          & 0x00000000FFFFFFFFL;
        x = (x | (x << 16)) & 0x0000FFFF0000FFFFL;
        x = (x | (x <<  8)) & 0x00FF00FF00FF00FFL;
        x = (x | (x <<  4)) & 0x0F0F0F0F0F0F0F0FL;
        x = (x | (x <<  2)) & 0x3333333333333333L;
        x = (x | (x <<  1)) & 0x5555555555555555L;
        return x;
    }

    public void shift(){
        level--;
        int toRemove=64-2*level;
        morton= morton >> toRemove;
        morton= morton << toRemove;
    }
    @Override
    public Object clone(){
        MortonCode copy = new MortonCode(morton,level,isNode);
        return copy;
    }

    public MortonCode shallowCopy() throws CloneNotSupportedException {
        return (MortonCode) this.clone();
    }

    public void printBit(){
        String bit=Long.toBinaryString(this.morton);
        System.out.println(bit);
    }

    @Override
    public int compareTo(MortonCode o) {
        return Long.compare(this.morton, o.morton);
    }
    //TODO: check correctness
    @Override
    public boolean equals(Object object){
        MortonCode o = (MortonCode) object;
        //They are equal if one is a prefix of another, namely a&b is either a or b
        long result = o.morton & this.morton;
        if(result==o.morton || this.morton==result)
            return true;
        return false;
    }

    public boolean exactlyEquals(MortonCode mc2){
        return this.morton==mc2.morton;
    }

    @Override
    public int hashCode(){
        return Long.hashCode(this.morton);
    }
}
