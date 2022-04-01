package dedp.Test;

import dedp.DistanceOracles.EdgeLabelProcessor;
import dedp.DistanceOracles.Global;
import dedp.DistanceOracles.HybridDOEDPIndex;
import dedp.DistanceOracles.Parser;
import dedp.exceptions.DuplicateEntryException;
import dedp.exceptions.ObjectNotFoundException;
import dedp.indexes.edgedisjoint.ConnectedComponent;
import dedp.indexes.edgedisjoint.Partition;
import dedp.indexes.edgedisjoint.PartitionEdge;
import dedp.indexes.edgedisjoint.PartitionVertex;
import dedp.structures.Graph;
import dedp.structures.Vertex;

import java.io.*;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

public class BridgeThreadTest {

    public Graph g;
    public HybridDOEDPIndex index;
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
                v.setID(id);
                //todo: change parser
                v.setCoordinates(Parser.normalizeLat(latitude), Parser.normalizeLon(longitude));
                g.addVertex(v);
                if(id==271449){
                    break;
                }
            }
            boolean flag = false;
            int key=0;
            int fromID=-1, toID=-1;
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
                    fromID=Integer.parseInt(fields[0]);
                    toID=Integer.parseInt(fields[1]);
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

      /*  for(int i=0; i<index.partitions.length;i++){
            index.partitions[i].printStats();
        }*/

    }

    public void test1() throws Exception {
        Partition p = index.partitions[1];
        //p.conditionPrintCCs(100);
        ConnectedComponent cc = p.ConnectedComponents.getConnectedComponent(0);
        Random generator = new Random();
        Object[] values = cc.vertices.values().toArray();
        cc.print();
        ArrayList<PartitionVertex> list = new ArrayList<>();
        for(int i=0;i<20;i++) {
            Object randomValue = values[generator.nextInt(values.length)];
            PartitionVertex v = (PartitionVertex) randomValue;
            list.add(v);
            ArrayList<PartitionEdge> bridgeList = new ArrayList<>();
            boolean b=cc.checkBridgeDO(v, bridgeList);
            if(b){
                System.out.println(i+ "got checked");
            }
            v.lock.lock();
            //todo: check synchronization here
            while (v.numOfBridgeEdgesComputed <cc.bridgeVertices.size()) {
                if(v.isBridge()&&v.numOfBridgeEdgesComputed+1==cc.bridgeVerticesSize()){
                    break;
                }
                v.bridgeEdgeAdded.await();
            }
            System.out.println(i);
            //System.out.println(v.numOfBridgeEdgesComputed);
            //System.out.println((v.numOfBridgeEdgesComputed <cc.bridgeVertices.size()&&!(v.isBridge()&&v.numOfBridgeEdgesComputed+1<cc.bridgeVerticesSize())));
            if(v.numOfBridgeEdgesComputed<cc.bridgeVerticesSize()-10){
                System.out.println(v.numOfBridgeEdgesComputed+" "+ cc.bridgeVertices.size());
                System.out.println((v.isBridge()&&v.numOfBridgeEdgesComputed+1<cc.bridgeVerticesSize()));
                System.out.println("");
            }
            if(v.isBridge()){
                if(bridgeList.size()!=cc.bridgeVerticesSize()-1){
                    throw new Exception("error happend at "+i);
                }
            }else{
                if(bridgeList.size()!=cc.bridgeVerticesSize()){
                    throw new Exception("error happend at "+i);
                }
            }
            v.lock.unlock();

        }
        for(int i=0; i<list.size();i++){
            list.get(i).thread.join();
        }
        list.clear();
        for(int i=0;i<20;i++) {
            Object randomValue = values[generator.nextInt(values.length)];
            PartitionVertex v = (PartitionVertex) randomValue;
            list.add(v);
            ArrayList<PartitionEdge> bridgeList = new ArrayList<>();
            boolean b = cc.checkBridgeDO(v, bridgeList);
            if(b){
                System.out.println(i+ " got checked");
            }
            v.lock.lock();
            //todo: check synchronization here
            while (v.numOfBridgeEdgesComputed <cc.bridgeVertices.size()) {
                if(v.isBridge()&&v.numOfBridgeEdgesComputed+1==cc.bridgeVerticesSize()){
                    break;
                }
                v.bridgeEdgeAdded.await();
            }
            System.out.println(i);
            //System.out.println(v.numOfBridgeEdgesComputed);
            //System.out.println((v.numOfBridgeEdgesComputed <cc.bridgeVertices.size()&&!(v.isBridge()&&v.numOfBridgeEdgesComputed+1<cc.bridgeVerticesSize())));
            if(v.numOfBridgeEdgesComputed<cc.bridgeVerticesSize()-10){
                System.out.println(v.numOfBridgeEdgesComputed+" "+ cc.bridgeVertices.size());
                System.out.println((v.isBridge()&&v.numOfBridgeEdgesComputed+1<cc.bridgeVerticesSize()));
                System.out.println("");
            }
            if(v.isBridge()){
                if(bridgeList.size()!=cc.bridgeVerticesSize()-1){
                    throw new Exception("error happend at "+i);
                }
            }else{
                if(bridgeList.size()!=cc.bridgeVerticesSize()){
                    throw new Exception("error happend at "+i);
                }
            }
            v.lock.unlock();

        }
        for(int i=0; i<list.size();i++){
            if(list.get(i).thread!=null)
            list.get(i).thread.join();
        }
        Global.printResult();
        //System.out.println(bridgeList);
    }

    //todo: try to see if bridge edges are computed
    public static void main(String[] args) throws Exception {
        BridgeThreadTest t = new BridgeThreadTest();
        t.loadGraph();
        t.test1();
    }
}
