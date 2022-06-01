package dedp.Test;

import dedp.DistanceOracles.EDP_DO_Test;
import dedp.DistanceOracles.MortonCode;
import dedp.DistanceOracles.Parser;
import dedp.DistanceOracles.SearchKey;

public class MortonTest2 {
    public static void main(String[] args) throws Exception {

    /*    EDP_DO_Test t = new EDP_DO_Test();
        t.loadGraph(30000);
        MortonCode mc1 = new MortonCode(2067622,11);
        MortonCode mc2 = new MortonCode(3478420,11);

        MortonCode mc3 = new MortonCode(2117245122,16);
        MortonCode mc4 = new MortonCode(-733064424,16);
        SearchKey key = new SearchKey(mc1,mc2);
        SearchKey key2 = new SearchKey(mc3,mc4);
        mc1.printBit();
        mc3.printBit();
        mc2.printBit();
        mc4.printBit();
        key.printBit();
        key2.printBit();*/
        int x = -12345;
        System.out.println(Integer.toBinaryString(x));
        long y = (long)x;
        System.out.println(y);
        y<<=32;
        y>>>=32;
        System.out.println(y);
        System.out.println(Long.toBinaryString(y));

    }
}
