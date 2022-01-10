package dedp.DistanceOracles;

import dedp.indexes.edgedisjoint.PartitionVertex;

import java.util.HashMap;
import java.util.Random;

public class MortonCodeTest {
    public static void main(String[] args) throws CloneNotSupportedException {
       /* int lat = 121342140;
        int lon = 741324124;*/
        int lat =1073741824;
        int lon=0;
        MortonCode mc = new MortonCode(lat, lon, 0, true);
        mc.printBit();
        int lat2 =13712314;
        int lon2 = 41313183;
        MortonCode mc2 = new MortonCode(lat2, lon2, 0, true);

        MortonCode block1=new MortonCode(lat+920, lon+1456, 10, true);
        MortonCode block2=new MortonCode(lat2+138, lon2+134, 10, true);
        //mc2.printBit();
        SearchKey key = new SearchKey(mc, mc2, 32);
        key.printBit();
        SearchKey key2 = new SearchKey(block1, block2, 16);
        key2.printBit();
        System.out.println("equal or not: "+key.equals(key2));
        //TODO: let hash map work
        HashMap<SearchKey, Integer> testMap= new HashMap<>();
        testMap.put(key2, 100);
        while(true) {
            if(testMap.containsKey(key))
                break;
            key.shift();
        }
        System.out.println("We get " + testMap.get(key));
        //key.printBit();
        key.shift();
        //key.printBit();
        HashMap<Integer, PartitionVertex> nodes=new HashMap<>();
        Random r = new Random();
        QuadTree.setMax_depth(20);
        Double minLat = -90.0;
        Double maxLat = 90.0;
        Double minLon=-180.0;
        Double maxLon=180.0;
        for(int i=0; i<100000; i++){
            double latitude = (Math.random() * ((maxLat - minLat) + 1)) + minLat;   // This Will Create A Random Number Inbetween Your Min And Max.
            double latRound = Math.round(latitude * 100.0) / 100.0;
            double longitude =  (Math.random() * ((maxLon - minLon) + 1)) + minLon;
            double lonRound = Math.round(longitude*100.0)/100.0;
            int latInt = Parser.normalizeLat(latRound);
            int lonInt = Parser.normalizeLon(lonRound);
            PartitionVertex vertex = new PartitionVertex();
            vertex.setId(i);
            vertex.setCoordinates(latInt, lonInt);
            nodes.put(i, vertex);
        }
        QuadTree t = new QuadTree(Parser.normalizeLat(90.0), Parser.normalizeLat(-90.0), Parser.normalizeLon(-180.0), Parser.normalizeLon(180.0), null,0, nodes);
       // t.info();
        System.out.println(t.testMorton2());
    }
}
