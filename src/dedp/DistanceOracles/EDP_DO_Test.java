package dedp.DistanceOracles;

import dedp.DistanceOracles.Analytical.ConnectedComponentAnalyzer;
import dedp.DistanceOracles.MonochromeDO.DOLoader;
import dedp.DistanceOracles.Precomputation.DiameterLoader;
import dedp.DistanceOracles.Precomputation.EDP_DO_Precomputation;
import dedp.DistanceOracles.Precomputation.PrecomputationResultDatabase;
import dedp.algorithms.Dijkstra;
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
    public Graph g;
    public static boolean precomputeDO = false;
    public HybridDOEDPIndex index;
    public void loadGraph(int bound) throws Exception {
        g=new Graph();
        QuadTree.nextID=0;
        MortonCode.reset();
        String vName = "./Graph_Source/ID_ver_final.txt";
        String eName = "./Graph_Source/ID_edge_final.txt";
        try{
            File vFile =new File(vName);
            File eFile = new File(eName);
            BufferedReader vReader = new BufferedReader(new FileReader(vFile));
            BufferedReader eReader = new BufferedReader(new FileReader(eFile));
            String line;
            while((line=vReader.readLine())!=null){
                //271449
                String[]fields = line.split(",");
                int id =Integer.parseInt(fields[0]);
                int rawLongitude = Math.abs(Integer.parseInt(fields[1]));
                int rawLatitude = Math.abs(Integer.parseInt(fields[2]));
                //int latitude = (rawLatitude)/1000;
                //int longitude = (rawLongitude)/1000;
                Vertex v = new Vertex();
                v.setID((long)id);
                //todo: change parser
                v.setCoordinates(rawLatitude,rawLongitude);
                //v.setCoordinates(latitude, longitude);
                if(id<bound){
                    MortonCode.feedLat(rawLatitude);
                    MortonCode.feedLon(rawLongitude);
                    g.addVertex(v);
                }
            }
            vReader.close();
            long key=1;
            long fromID=-1, toID=-1;
            float weight;
            int label;
            boolean isDirected=false;
            while((line=eReader.readLine())!=null){
                String[]fields = line.split(",");
                    fromID=Long.parseLong(fields[0]);
                    toID=Long.parseLong(fields[1]);
                EdgeLabelProcessor.insert(Integer.parseInt(fields[2]));
                label = EdgeLabelProcessor.translate(Integer.parseInt(fields[2]));
                    weight = Float.parseFloat(fields[3]);
                    if(fromID<bound&&toID<bound){
                        g.addEdge(key, fromID, toID, weight, label, isDirected, false);
                    }
                    key++;
            }
            eReader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (DuplicateEntryException e) {
            e.printStackTrace();
        } catch (ObjectNotFoundException e) {
            e.printStackTrace();
        }
        MortonCode.finishLoading();
        g.printStats();
        index= HybridDOEDPIndex.buildIndex(g, null, false);
        index.isDirected=false;
        System.out.println("total bridge vertices are "+Global.total_bridge_vertices);

       /* for(int i=0; i<index.partitions.length;i++){
            for(int j=0; j<index.partitions[i].ConnectedComponents.getConnectedComponentsCount(); j++){
                index.partitions[i].printCCs();
            }
        }*/

    }

    public void testQuadtree(){

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
            v.setCoordinates(rawLatitude, rawLongitude);
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
        //DistanceOracle.setParameter(0.05);
        EDP_DO_Test t = new EDP_DO_Test();
        t.loadGraph(300000);//set a bound on how many vertices we want
        ArrayList<Integer>list = new ArrayList<>();
        for(int i=0; i<t.g.LabelsIDs.size()/2;i++){
            list.add(i);
        }
        System.out.println("total number of do threads are "+Global.total_do_threads);
        System.out.println("total partition vertex number is "+Global.total_partition_vertex);
        System.out.println("total partition edge number is "+Global.total_partition_edge);
        System.out.println("Max lat is "+MortonCode.max_lat);
        System.out.println("Min lat is "+MortonCode.min_lat);
        System.out.println("Max lon is "+MortonCode.max_lon);
        System.out.println("Min lon is "+MortonCode.min_lon);
        int j = EdgeLabelProcessor.EDPLabelToRawLabel.get(1);
        //ConnectedComponentAnalyzer.print(30);
        int i =0;
        EDP_DO_Precomputation pre = new EDP_DO_Precomputation(t.index);
        File diameterFile = new File(PrecomputationResultDatabase.fileName);
        if(diameterFile.exists()){
            //System.out.println("esixts");
            DiameterLoader loader = new DiameterLoader(t.index, diameterFile);
            loader.load();
            loader=null;
        }else {
            pre.start_preprocessing();
            return;
        }
        if(precomputeDO){
            DOLoader.DOLoad(t.index);
        }
        FileWriter myWriter = new FileWriter("result.txt");
        long startTime = System.nanoTime();
        double total=0;
        double max_err=-100;
        int from, to=-1;
        float error=0;
        float error2=0;
        while(i<1000) {
            i++;
            String result="";
            //int from = ThreadLocalRandom.current().nextInt(0, 271450 + 1);
            //int to = ThreadLocalRandom.current().nextInt(0, 271450 + 1);

            from = ThreadLocalRandom.current().nextInt(0, 30000 + 1);
            to = ThreadLocalRandom.current().nextInt(0, 30000 + 1);
            //from = 15649;
            //to= 17155;
            if(!t.g.containsVertex(from)||!t.g.containsVertex(to)) {
                continue;
            }

            //EDP_DO_Test_Thread th = new EDP_DO_Test_Thread();
            SPResult r = DOTraversal.shortestDistanceWithDO(t.index, from, to, list);
            SPResult rr = Dijkstra.shortestDistance(t.g,from,to,list);
            if(r.Distance==rr.Distance&&r.Distance==-1){
                //System.out.println("Source "+from+" destination "+to+" cannot reach each other");
                result+="Source "+from+" destination "+to+" cannot reach each other\n";
            }else{
                error = Math.abs(100*(r.Distance-rr.Distance)/rr.Distance);
                if(error>max_err){
                    max_err=error;
                }
                total+=error;
                //System.out.println("EDP: Source is "+from+" destination is "+to+" Shortest distance = " + r.Distance);
                result+="EDP: Source is "+from+" destination is "+to+" Shortest distance = " + r.Distance+"\n";
                //System.out.println("Dijkstra: Source is "+from+" destination is "+to+" Shortest distance = " + rr.Distance);
                result+="Dijkstra: Source is "+from+" destination is "+to+" Shortest distance = " + rr.Distance+"\n";
                //System.out.println("error is "+error+"%");
                result+="error is "+error+"%\n";
            }
            for(int z=0; z<Global.list.size();z++){
                Global.list.get(z).join();
            }
            Global.list.clear();
            r = DOTraversal.shortestDistanceWithDO(t.index, from, to, list);
            error2 = Math.abs(100*(r.Distance-rr.Distance)/rr.Distance);
            if(error2>max_err){
                max_err=error2;
            }
            //System.out.println("Second run EDP: Source is "+from+" destination is "+to+" Shortest distance = " + r.Distance);
            result+="Second run EDP: Source is "+from+" destination is "+to+" Shortest distance = " + r.Distance+"\n";
            //System.out.println("error 2 is "+error2+"%");
            result+="error 2 is "+error2+"%\n";
            total+=error2;

            //System.out.println(result);
            if(error>15||error2>15){
                myWriter.write(result);
            }
            error=0;
            error2=0;
            //Global.printResult();
        }
        String stats="";
        long endTime   = System.nanoTime();
        double totalTime = (double)(endTime - startTime)/1000000000;
        double avg_error = total/2000;
        stats+="avg error is "+avg_error+"%\n"+"max error is "+max_err+"%\n"+"Total time is "+totalTime+" seconds\n";
        myWriter.write(stats);
        myWriter.close();
        //System.out.println("avg error is "+avg_error+"%");
        //System.out.println("max error is "+max_err+"%");
        //System.out.println("Total time is "+totalTime+" seconds");
        Global.printResult();
    }
}
