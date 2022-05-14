package dedp.Test;

import dedp.DistanceOracles.*;
import dedp.indexes.edgedisjoint.ConnectedComponent;
import dedp.indexes.edgedisjoint.PartitionVertex;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class MortonCodeTest {

    public static void testMorton(){
        int la1 = Parser.normalizeLat(4346734);
        System.out.println("la1 is "+la1);
        System.out.println(Integer.toBinaryString(la1));
        int lo1 = Parser.normalizeLon(-11626393);
        System.out.println("lo1 is "+lo1);
        System.out.println(Integer.toBinaryString(lo1));
        int la2 = Parser.normalizeLat(4349594);
        System.out.println("la2 is "+la2);
        System.out.println(Integer.toBinaryString(la2));
        int lo2 = Parser.normalizeLon(-11625891);
        System.out.println("lo2 is "+lo2);
        System.out.println(Integer.toBinaryString(lo2));
        MortonCode mc1 = new MortonCode(la1,lo1,32,true);
        MortonCode mc2 = new MortonCode(la2,lo2,32,true);
        mc1.printBit();
        mc2.printBit();
    }

    public static void main(String[] args) throws Exception {
        EDP_DO_Test t = new EDP_DO_Test();
        t.loadGraph(30000);
      /*  System.out.println(Parser.max_lat);
        System.out.println(Parser.min_lat);
        System.out.println(Parser.max_long);
        System.out.println(Parser.min_long);*/
        //MortonCodeTest.testMorton();
        ConnectedComponent cc = t.index.partitions[1].ConnectedComponents.getConnectedComponent(3);
        QuadTree tree = t.index.partitions[1].ConnectedComponents.getConnectedComponent(3).tree;
        int from=-1;
        int to=-1;
        PartitionVertex v1=null;
        PartitionVertex v2=null;
        Random generator = new Random();
        while(true){
            //from = ThreadLocalRandom.current().nextInt(0, 30000 + 1);
            //to = ThreadLocalRandom.current().nextInt(0, 30000 + 1);
            from = 18173;
            Object[] vecs = (cc.bridgeVertices.values().toArray());

            if(cc.vertices.containsKey(from)){
                v1 = cc.vertices.get(from);
                //v2 = cc.vertices.get(to);
                v2 = (PartitionVertex) vecs[generator.nextInt(vecs.length)];
                System.out.println("from is "+from);
                System.out.println("to is "+v2.getId());
                break;
            }
        }
        QuadTree forU=tree, forV = tree;
        for(int i=0;i<11;i++){
            forU = forU.containingBlock(v1);
            forV = forV.containingBlock(v2);
        }
        HashSet<Integer> set1= new HashSet<>();
        HashSet<Integer> set2= new HashSet<>();
        forU.copy(set1);
        forV.copy(set2);
        //System.out.println(set1.contains(from));
        //System.out.println(set2.contains(to));
        forU.getMC().printBit();
        v1.morton().printBit();
        System.out.println();
        forV.getMC().printBit();
        v2.morton().printBit();
        SearchKey key = new SearchKey(forU.getMC(), forV.getMC(), forU.getLevel());
        key.printBit();
        SearchKey toCompare = new SearchKey(v1.morton(),v2.morton());
        toCompare.printBit();
        System.out.println("v1's latitide is "+v1.latitude+" v1's longitude is "+v1.longitude);
        for(int i=0; i<32; i++){
            if(key.equals(toCompare)){
                System.out.println("got it at "+(32-i));
            }
            toCompare.shift();
        }
    }
}
