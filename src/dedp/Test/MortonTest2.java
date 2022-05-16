package dedp.Test;

import dedp.DistanceOracles.EDP_DO_Test;
import dedp.DistanceOracles.MortonCode;
import dedp.DistanceOracles.Parser;
import dedp.DistanceOracles.SearchKey;

public class MortonTest2 {
    public static void main(String[] args) throws Exception {
        /*long l1 = Long.parseLong("268019397621307392");
        long l2 = Long.parseLong("255501396747087872");
        MortonCode mc1 = new MortonCode(l1,32,true);
        MortonCode mc2 = new MortonCode(l2,32,true);
        mc1.printBit();
        mc2.printBit();
        SearchKey key = new SearchKey(mc1,mc2,32);
        key.printBit();*/
        EDP_DO_Test t = new EDP_DO_Test();
        t.loadGraph(30000);
        int lat1 = Parser.normalizeLat(43582909);
        int lon1 = Parser.normalizeLon(-116193694);
        int lat2 = Parser.normalizeLat(43582053);
        int lon2 = Parser.normalizeLon(-116194450);
        MortonCode mc1 = new MortonCode(lat1,lon1,32,true);
        MortonCode mc2 = new MortonCode(lat2,lon2,32,true);
        mc1.printBit();
        mc2.printBit();
    }
}
