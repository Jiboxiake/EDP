package dedp.Test;

import dedp.DistanceOracles.EDP_DO_Test;
import dedp.DistanceOracles.MortonCode;
import dedp.DistanceOracles.Parser;
import dedp.DistanceOracles.SearchKey;

public class ParserTest {
    public static void main(String[] args) throws Exception {
        EDP_DO_Test t = new EDP_DO_Test();
        t.loadGraph(30000);
        MortonCode.debug=true;
        int a = -1485148551;
        int b = -1478085714;
        MortonCode mc1 = new MortonCode(a,16);
        MortonCode mc2 = new MortonCode(b,16);
        SearchKey k = new SearchKey(mc1,mc2);
        k.printBit();
        int top = 49001058;
        int horizontal = 47247799;
        int bot = 45494541;
        int left = -114137173;
        int vertical = -112590438;
        int right = -111043702;
    /*    System.out.println(Integer.toBinaryString(59873796));
        System.out.println(Integer.toBinaryString(59873454));*/
        //MortonCode mc1 = new MortonCode(59873796,16);
        //MortonCode mc1 = new MortonCode(b,a,16);
  /*      MortonCode mc2 = new MortonCode(top-1,right-1,1);
        MortonCode mc3 = new MortonCode(horizontal,vertical,1);
        MortonCode mc4 = new MortonCode(bot,left,1);*/
        /*mc1.printBit();
        mc2.printBit();
        mc3.printBit();
        mc4.printBit();*/
  /*      System.out.println(Integer.toBinaryString(65535));
        System.out.println(Integer.toBinaryString(49151));
        System.out.println(Integer.toBinaryString(32768));*/
    }
}
