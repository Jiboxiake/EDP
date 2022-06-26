package dedp.DistanceOracles.Precomputation.ALLDO;

import dedp.DistanceOracles.DistanceOracle;
import dedp.DistanceOracles.EDP_DO_Test;
import dedp.DistanceOracles.Precomputation.Preprocessing;
import dedp.DistanceOracles.QuadTree;
import dedp.DistanceOracles.SearchKey;
import dedp.algorithms.bidirectional.BidirectionalDijkstra;
import dedp.exceptions.ObjectNotFoundException;
import dedp.indexes.edgedisjoint.ConnectedComponent;
import dedp.indexes.edgedisjoint.Partition;
import dedp.indexes.edgedisjoint.PartitionVertex;
import dedp.structures.SPResult;

import java.util.*;

public class AllDOThread extends Thread{
    public EDP_DO_Test t;
    public ArrayList<AllDOWorkloadEntry> workloads = new ArrayList<>();
    QuadTree t1=null;
    QuadTree t2 = null;
    Partition p=null;
    SPResult result = null;
    ConnectedComponent cc;
    @Override
    public void run(){
        AllDOWorkloadEntry e = null;
        for(int i=0; i<workloads.size();i++){
            e = workloads.get(i);
            try {
                processPair(e);
            } catch (ObjectNotFoundException ex) {
                ex.printStackTrace();
            }
        }
    }
    private int pairing(int x, int y){//todo: change this to long
        int i=-1;
        int j=-1;
        if(x>y){
            i=x;
            j=y;
        }else if(x<y){
            i=y;
            j=x;
        }else{
            System.out.println("They should not be the same "+x+" "+y);
        }
        int result =(i+j-2)/2*(i+j-1)+i;
        return result;
    }
    public void processPair(AllDOWorkloadEntry e) throws ObjectNotFoundException {
        LinkedList<AllDOWorkloadEntry> list = new LinkedList<>();
        list.add(e);
        p = e.partition;
        ArrayList<Integer> labels = new ArrayList<>();
        labels.add(p.Label);
        cc =e.cc;
        HashMap<SearchKey,Float> partialDO = new HashMap<>();
        HashMap<SearchKey, Float> distanceMap = new HashMap<>();
        AllDOWorkloadEntry firstEntry = null;
        while(!list.isEmpty()){
            firstEntry = list.removeFirst();
            t1 = firstEntry.t1;
            t2 = firstEntry.t2;
            if(t1.id==t2.id){
                insertSelf(t1,list);
            }else if(t1.size()==0||t2.size()==0){
                continue;
            }
            else{
                int vid1 = t1.representativePoint;
                int vid2 = t2.representativePoint;
                //int pairKey = pairing(vid1,vid2);
                PartitionVertex v1 = cc.getVertex(vid1);
                PartitionVertex v2 = cc.getVertex(vid2);
                SearchKey pairKey = new SearchKey(v1.morton(),v2.morton());
                float distance;
                if(distanceMap.containsKey(pairKey)){
                    distance = distanceMap.get(pairKey);
                }else{
                    result = BidirectionalDijkstra.shortestDistance(t.g,vid1,vid2,labels);//todo:cache this result
                    PreprocessingGlobal.dijAdd();
                    distance = result.Distance;
                    distanceMap.put(pairKey,distance);
                }
                if(DistanceOracle.isWellSeparatedOpti(distance, t1, t2, null, null)){
                    SearchKey key = new SearchKey(t1.getMC(),t2.getMC());
                    //cc.addSingleDO(key,result.Distance);
                    partialDO.put(key,distance);
                    PreprocessingGlobal.doLevelAdd(t1.getLevel());
                }else{
                    insertToQueue(t1,t2,list);
                    PreprocessingGlobal.queueInsertionAdd();
                }
            }
        }
        cc.addDO(partialDO);
    }
    public void insertToQueue(QuadTree t1, QuadTree t2, LinkedList<AllDOWorkloadEntry> list){
        QuadTree NW1 = t1.NW;
        QuadTree NW2 = t2.NW;
        QuadTree NE1 = t1.NE;
        QuadTree NE2 = t2.NE;
        QuadTree SW1 = t1.SW;
        QuadTree SW2 = t2.SW;
        QuadTree SE1 = t1.SE;
        QuadTree SE2 = t2.SE;
        if(NW1!=null&&NW1.size()>0){
            pairInsert(NW1,NW2, NE2, SW2, SE2, list);
        }
        if(NE1!=null&&NE1.size()>0){
            pairInsert(NE1,NW2, NE2, SW2, SE2, list);
        }
        if(SW1!=null&&SW1.size()>0){
            pairInsert(SW1,NW2, NE2, SW2, SE2, list);
        }
        if(SE1!=null&&SE1.size()>0){
            pairInsert(SE1,NW2, NE2, SW2, SE2, list);
        }
    }
    private void pairInsert(QuadTree left, QuadTree NW2,  QuadTree NE2,  QuadTree SW2,  QuadTree SE2, LinkedList<AllDOWorkloadEntry> list){
        if(NW2!=null&&NW2.size()>0){
            AllDOWorkloadEntry entry = new AllDOWorkloadEntry();
            entry.cc= cc;
            entry.partition = p;
            entry.t1 = left;
            entry.t2 = NW2;
            list.addLast(entry);
        }
        if(NE2!=null&&NE2.size()>0){
            AllDOWorkloadEntry entry = new AllDOWorkloadEntry();
            entry.cc= cc;
            entry.partition = p;
            entry.t1 = left;
            entry.t2 = NE2;
            list.addLast(entry);
        }
        if(SW2!=null&&SW2.size()>0){
            AllDOWorkloadEntry entry = new AllDOWorkloadEntry();
            entry.cc= cc;
            entry.partition = p;
            entry.t1 = left;
            entry.t2 = SW2;
            list.addLast(entry);
        }
        if(SE2!=null&&SE2.size()>0){
            AllDOWorkloadEntry entry = new AllDOWorkloadEntry();
            entry.cc= cc;
            entry.partition = p;
            entry.t1 = left;
            entry.t2 = SE2;
            list.addLast(entry);
        }
    }

    private void insertSelf(QuadTree t1,LinkedList<AllDOWorkloadEntry> list){
        ArrayList<QuadTree> entryset = new ArrayList<>();
        if(t1.NW!=null&&t1.NW.size()>0){
            entryset.add(t1.NW);
        }
        if(t1.NE!=null&&t1.NE.size()>0){
            entryset.add(t1.NE);
        }
        if(t1.SW!=null&&t1.SW.size()>0){
            entryset.add(t1.SW);
        }
        if(t1.SE!=null&&t1.SE.size()>0){
            entryset.add(t1.SE);
        }
        for(int i=0; i<entryset.size();i++){
            QuadTree tree1 = entryset.get(i);
            for(int j=i; j<entryset.size();j++){
                QuadTree tree2 = entryset.get(j);
                AllDOWorkloadEntry entry = new AllDOWorkloadEntry();
                entry.cc= cc;
                entry.partition = p;
                entry.t1 = tree1;
                entry.t2 = tree2;
                list.addLast(entry);
            }
        }
    }
}
