package dedp.DistanceOracles;

import dedp.algorithms.hybridtraversal.DOTraversal;
import dedp.algorithms.hybridtraversal.HybridTraversal;
import dedp.exceptions.DuplicateEntryException;
import dedp.exceptions.ObjectNotFoundException;
import dedp.structures.Graph;
import dedp.structures.SPResult;
import dedp.structures.Vertex;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

public class EDP_DO_Test {
    private Graph g;
    private HybridDOEDPIndex index;
    public void loadGraph() throws Exception {
        g=new Graph();

        String pathName = "./Graph_Source/ID.tmp";
        try{
            File f =new File(pathName);
            BufferedReader reader = new BufferedReader(new FileReader(f));
            String line;
            while((line=reader.readLine())!=null){
                //271449
                String[]fields = line.split("\\s+");
                if(fields.length==1){
                    continue;
                }
                int id =Integer.parseInt(fields[0]);
                int rawLongitude = Integer.parseInt(fields[1]);
                int rawLatitude = Integer.parseInt(fields[2]);
                float latitude = ((float)rawLatitude)/1000000;
                float longitude = ((float)rawLongitude)/1000000;
                Vertex v = new Vertex();
                v.setID((long)id);
                //todo: change parser
                v.setCoordinates(latitude, longitude);
                Parser.feedLat(latitude);
                Parser.feedLon(longitude);
                g.addVertex(v);
                if(id==271449){
                    break;
                }
            }
            boolean flag = false;
            long key=1;
            long fromID=-1, toID=-1;
            float weight;
            int label;
            boolean isDirected=false;
            while((line=reader.readLine())!=null){
                String[]fields = line.split("\\s+");
                if(fields.length==1){
                    continue;
                }
                if(!flag){
                    flag=true;
                    fromID=Long.parseLong(fields[0]);
                    toID=Long.parseLong(fields[1]);
                }else{
                    flag=false;
                    weight = Float.parseFloat(fields[1]);
                    EdgeLabelProcessor.insert(Integer.parseInt(fields[2]));
                    label = EdgeLabelProcessor.translate(Integer.parseInt(fields[2]));
                    //todo: for test set all labels to 1
                    g.addEdge(key, fromID, toID, weight, label, isDirected, false);
                    key++;
                }

            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (DuplicateEntryException e) {
            e.printStackTrace();
        } catch (ObjectNotFoundException e) {
            e.printStackTrace();
        }
        g.printStats();
        index= HybridDOEDPIndex.buildIndex(g, null, false);
        index.isDirected=false;

       /* for(int i=0; i<index.partitions.length;i++){
            for(int j=0; j<index.partitions[i].ConnectedComponents.getConnectedComponentsCount(); j++){
                index.partitions[i].printCCs();
            }
        }*/

    }

    public void loadTest() throws Exception {
        g=new Graph();
        String pathName1 = "./Graph_Source/test.txt";
        String pathName2 = "./Graph_Source/edges.txt";
        File f =new File(pathName1);
        BufferedReader reader = new BufferedReader(new FileReader(f));
        String line;
        while((line=reader.readLine())!=null){
            String[]fields = line.split("\\s+");
            if(fields.length==1){
                continue;
            }
            int id =Integer.parseInt(fields[0]);
            int rawLongitude = Integer.parseInt(fields[1]);
            int rawLatitude = Integer.parseInt(fields[2]);
            float latitude = ((float)rawLatitude)/1000000;
            float longitude = ((float)rawLongitude)/1000000;
            Vertex v = new Vertex();
            v.setID(id);
            //todo: change parser
            v.setCoordinates(latitude, longitude);
            g.addVertex(v);
        }
        f =new File(pathName2);
        reader = new BufferedReader(new FileReader(f));
        boolean flag = false;
        int key=0;
        int fromID=-1, toID=-1;
        float weight;
        int label;
        while((line=reader.readLine())!=null){
            String[]fields = line.split("\\s+");
            if(fields.length==1){
                continue;
            }
            if(!flag){
                flag=true;
                fromID=Integer.parseInt(fields[0]);
                toID=Integer.parseInt(fields[1]);
            }else{
                flag=false;
                weight = Float.parseFloat(fields[1]);
                EdgeLabelProcessor.insert(Integer.parseInt(fields[2]));
                label = EdgeLabelProcessor.translate(Integer.parseInt(fields[2]));
                //todo: for test set all labels to 1
                g.addEdge(key, fromID, toID, weight, label, false, false);
                key++;
            }
            index= HybridDOEDPIndex.buildIndex(g, null, false);
            index.isDirected=false;
        }
    }

    public void test() throws Exception {
        ArrayList<Integer>list = new ArrayList<>();
       for(int i=0; i<g.LabelsIDs.size();i++){
            list.add(i);
        }
       // list.add(2);
        SPResult r=HybridTraversal.shortestDistanceWithEdgeDisjointDistanceOracle(index, 345, 21312, list);
        //System.out.println("Shortest distance = " + r.Distance);
        Global.printResult();
    }
    public void test2() throws Exception {
        ArrayList<Integer>list = new ArrayList<>();
        for(int i=0; i<g.LabelsIDs.size();i++){
            list.add(i);
        }
        SPResult r=HybridTraversal.shortestDistanceWithEdgeDisjointDistanceOracle(index, 9, 1, list);
        Global.printResult();
    }
    //todo: check garbage collection, implement LRU regarding bridge edges
    public static void main(String[] args) throws Exception {
        EDP_DO_Test t = new EDP_DO_Test();
        t.loadGraph();
        ArrayList<Integer>list = new ArrayList<>();
        for(int i=0; i<t.g.LabelsIDs.size()/4;i++){
            list.add(i);
        }
        System.out.println("total number of do threads are "+Global.total_do_threads);
        System.out.println("total partition vertex number is "+Global.total_partition_vertex);
        System.out.println("total partition edge number is "+Global.total_partition_edge);
        int i =0;
       // ExecutorService pool = Executors.newFixedThreadPool(5);
        while(i<10){
            i++;
            int from = ThreadLocalRandom.current().nextInt(1, 271450/300 + 1);
            int to = ThreadLocalRandom.current().nextInt(271450/5*4 + 1, 271450+1);
            EDP_DO_Test_Thread th = new EDP_DO_Test_Thread();
           // th.setParameter(i,from, to, list, t.index);
           // pool.execute(th);
           //SPResult r=HybridTraversal.shortestDistanceWithEdgeDisjointDistanceOracle(t.index, from, to, list);
            SPResult r= DOTraversal.shortestDistanceWithDO(t.index, from, to, list);
            System.out.println("Shortest distance = " + r.Distance);
           /* for(int j=0; j<r.list.size();j++){
                r.list.get(j).join();
            }*/
            System.out.println("query "+i+" finished");
        }
     /*   pool.shutdown();
        while(!pool.isTerminated()){

        }*/
        Global.printResult();
        //t.test();
    }
}
