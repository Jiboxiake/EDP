package dedp.DistanceOracles;

import java.util.HashMap;
import java.util.Random;

public class MortonCodeTest {
    public static void main(String[] args) {
        int lat = 10;
        int lon = 7;
        MortonCode mc = new MortonCode(lat, lon, 0, true);
        mc.printBit();
        int lat2 = 65536;
        int lon2 = 65536;
        MortonCode mc2 = new MortonCode(lat2, lon2, 61, false);
        mc2.printBit();
        SearchKey key = new SearchKey(mc, mc2, 32);
        //key.printBit();
        key.shift();
        //key.printBit();
        HashMap<MortonCode, Node> nodes=new HashMap<>();
        Random r = new Random();
        QuadTree.setMax_depth(20);
        Double minLat = -90.0;
        Double maxLat = 90.0;
        Double minLon=-180.0;
        Double maxLon=180.0;
        for(int i=0; i<100; i++){
            double latitude = (Math.random() * ((maxLat - minLat) + 1)) + minLat;   // This Will Create A Random Number Inbetween Your Min And Max.
            double latRound = Math.round(latitude * 100.0) / 100.0;
            double longitude =  (Math.random() * ((maxLon - minLon) + 1)) + minLon;
            double lonRound = Math.round(longitude*100.0)/100.0;
            int latInt = Parser.normalizeLat(latRound);
            int lonInt = Parser.normalizeLon(lonRound);
            nodes.put(new MortonCode(latInt, lonInt, 0, true), new Node(lonInt, latInt));
        }
        QuadTree t = new QuadTree(Parser.normalizeLat(90.0), Parser.normalizeLat(-90.0), Parser.normalizeLon(-180.0), Parser.normalizeLon(180.0), null,0, nodes);
       // t.info();
        System.out.println(t.testMorton());
    }
}
