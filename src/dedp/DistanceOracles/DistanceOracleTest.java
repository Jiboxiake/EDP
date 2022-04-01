package dedp.DistanceOracles;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;


import dedp.algorithms.ConnectedComponentsComputation;
import dedp.exceptions.DuplicateEntryException;
import dedp.exceptions.ObjectNotFoundException;
import dedp.indexes.edgedisjoint.Partition;
import dedp.indexes.edgedisjoint.PartitionEdge;
import dedp.indexes.edgedisjoint.PartitionVertex;
import dedp.structures.Edge;
import dedp.structures.Graph;
import dedp.structures.Vertex;

import java.io.*;
import java.util.HashMap;
//class used to test distance oracle.
public class DistanceOracleTest {
    public Graph graph;
    //only want one edge label, and well separated
    public Partition partition;
    //load the test graph
    public void loadTestGraph() throws IOException {
        this.partition = new Partition(1);
        this.graph= new Graph();
        HashMap<Integer, PartitionVertex> vers= new HashMap<>();
       // FileInputStream vertices=null;
        BufferedReader reader=null;
       // FileInputStream edges = null;
        try {
            //read thhe vertices
            reader=new BufferedReader(new FileReader("DO_TestGraph_Vertices.txt"));
            String line;
            int i =1;
            while((line=reader.readLine())!=null){
                String[]fields = line.split("\t");
                int id =Integer.parseInt(fields[0]);
                assert(id==i);
                i++;
                int latitude = Integer.parseInt(fields[1]);
                int longitude = Integer.parseInt(fields[2]);
                PartitionVertex v = new PartitionVertex();
                v.setId(id);
                v.setCoordinates(Parser.normalizeLat(latitude),Parser.normalizeLon(longitude));
                v.morton();
                //graph.addVertex(v);
                vers.put(id, v);
                partition.addVertex(v);

            }
            reader=new BufferedReader(new FileReader("DO_TestGraph_Edges.txt"));
            i=1;
            while((line=reader.readLine())!=null){
                String[]fields = line.split("\t");
                int from = Integer.parseInt(fields[0]);
                int to = Integer.parseInt(fields[1]);
                int weight = Integer.parseInt(fields[2]);
                PartitionEdge e = new PartitionEdge();
                e.setId(i);
                i++;
                e.setFrom(vers.get(from));
                e.setTo(vers.get(to));
                e.setWeight(weight);
                e.setLabel(1);
                vers.get(from).addEdge(e);
                //graph.addEdge(e);
            }

        } catch (DuplicateEntryException e) {
            e.printStackTrace();
        } finally{
            reader.close();
        }
        partition.updateBridgeVertexes();
        ConnectedComponentsComputation connectedCompDiscoverer = new ConnectedComponentsComputation();
        connectedCompDiscoverer.buildSCC(partition);
        //partition.printCCs();
    }

    public void loadNYGraph() throws IOException {
        this.partition = new Partition(1);
        this.graph= new Graph();
        HashMap<Integer, PartitionVertex> vers= new HashMap<>();
        // FileInputStream vertices=null;
        BufferedReader reader=null;
        try{
            reader = new BufferedReader(new FileReader("USA-road-d.NY.co"));
            String line;
            int i =1;
            //we read all vertices
            while((line=reader.readLine())!=null){
                String[]fields = line.split("\\s+");
                if(!fields[0].equals("v")){
                    continue;
                }
                int id =Integer.parseInt(fields[1]);
                int rawLongitude = Integer.parseInt(fields[2]);
                int rawLatitude = Integer.parseInt(fields[3]);
                float latitude = ((float)rawLatitude)/1000000;
                float longitude = ((float)rawLongitude)/1000000;
                PartitionVertex v = new PartitionVertex();
                v.setId(id);
                v.setCoordinates(Parser.normalizeLat(latitude), Parser.normalizeLon(longitude));
                vers.put(id, v);
                assert(id==i);
                i++;
                v.morton();
                partition.addVertex(v);
            }
            //now we read edges
            reader = new BufferedReader(new FileReader("USA-road-d.NY.gr"));
            i=1;
            while((line=reader.readLine())!=null){
                String[]fields = line.split("\\s+");
                if(!fields[0].equals("a"))
                    continue;
                int fromID =Integer.parseInt(fields[1]);
                int toID = Integer.parseInt(fields[2]);
                int weight = Integer.parseInt(fields[3]);
                PartitionEdge e = new PartitionEdge();
                PartitionVertex from = vers.get(fromID);
                PartitionVertex to = vers.get(toID);
                e.setId(i);
                i++;
                e.setWeight(weight);
                e.setLabel(1);
                e.setFrom(from);
                e.setTo(to);
                from.addEdge(e);
            }
        } catch (DuplicateEntryException e) {
            e.printStackTrace();
        } finally{
            reader.close();
        }
        partition.updateBridgeVertexes();
        ConnectedComponentsComputation connectedCompDiscoverer = new ConnectedComponentsComputation();
        connectedCompDiscoverer.buildSCC(partition);
        partition.printCCs();
    }

    public void test() throws Exception {

        int i=0;
        ExecutorService pool = Executors.newFixedThreadPool(16);
        while(i<10000) {
            i++;
            int from = ThreadLocalRandom.current().nextInt(1, 264346/2 + 1);
            int to = ThreadLocalRandom.current().nextInt(264346/2 + 1, 264346 + 1);
            DOTestThread t = new DOTestThread();
            t.setParameter(i,from, to);
            t.setPartition(this.partition);
            pool.execute(t);
          /*  while(to==from){
                to = ThreadLocalRandom.current().nextInt(1, 264346 + 1);
            }*/

            //System.out.println(this.partition.getEdgeWeightDO(from, to));
        }
        pool.shutdown();
        while(!pool.isTerminated()){

        }

        Global.printResult();
        return;
        /*System.out.println("Distance from v1 to v6 is "+ this.partition.getEdgeWeightDO(1, 6));
        System.out.println("Distance from v1 to v7 is "+ this.partition.getEdgeWeightDO(1, 7));
        System.out.println("Distance from v2 to v5 is "+ this.partition.getEdgeWeightDO(2, 5));
        System.out.println("Distance from v1 to v3 is "+ this.partition.getEdgeWeightDO(1, 3));*/

    }

    public static void main(String[] args) throws Exception {
        DistanceOracleTest dot=new DistanceOracleTest();
        dot.loadNYGraph();
        dot.test();
        System.exit(1);
    }
}
