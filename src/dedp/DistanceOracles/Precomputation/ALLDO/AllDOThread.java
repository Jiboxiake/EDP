package dedp.DistanceOracles.Precomputation.ALLDO;

import dedp.DistanceOracles.DistanceOracle;
import dedp.DistanceOracles.EDP_DO_Test;
import dedp.DistanceOracles.QuadTree;
import dedp.DistanceOracles.SearchKey;
import dedp.algorithms.bidirectional.BidirectionalDijkstra;
import dedp.exceptions.ObjectNotFoundException;
import dedp.indexes.edgedisjoint.ConnectedComponent;
import dedp.indexes.edgedisjoint.Partition;
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
    public void processPair(AllDOWorkloadEntry e) throws ObjectNotFoundException {
        LinkedList<AllDOWorkloadEntry> list = new LinkedList<>();
        list.add(e);
        p = e.partition;
        ArrayList<Integer> labels = new ArrayList<>();
        labels.add(p.Label);
        cc =e.cc;
        HashMap<SearchKey,Float> partialDO = new HashMap<>();
        AllDOWorkloadEntry firstEntry = null;
        while(!list.isEmpty()){
            firstEntry = list.removeFirst();
            t1 = firstEntry.t1;
            t2 = firstEntry.t2;
            if(t1.id==t2.id){
                insertToQueue(t1,t2,list);
            }else if(t1.size()==0||t2.size()==0){
                continue;
            }
            else{
                int vid1 = t1.representativePoint;
                int vid2 = t2.representativePoint;
                result = BidirectionalDijkstra.shortestDistance(t.g,vid1,vid2,labels);
                if(DistanceOracle.isWellSeparatedOpti(result.Distance, t1, t2, null, null)){
                    SearchKey key = new SearchKey(t1.getMC(),t2.getMC());
                    //cc.addSingleDO(key,result.Distance);
                    partialDO.put(key,result.Distance);
                }else{
                    insertToQueue(t1,t2,list);
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
        if(NW1!=null){
            pairInsert(NW1,NW2, NE2, SW2, SE2, list);
        }
        if(NE1!=null){
            pairInsert(NE1,NW2, NE2, SW2, SE2, list);
        }
        if(SW1!=null){
            pairInsert(SW1,NW2, NE2, SW2, SE2, list);
        }
        if(SE1!=null){
            pairInsert(SE1,NW2, NE2, SW2, SE2, list);
        }
    }
    private void pairInsert(QuadTree left, QuadTree NW2,  QuadTree NE2,  QuadTree SW2,  QuadTree SE2, LinkedList<AllDOWorkloadEntry> list){
        if(NW2!=null){
            AllDOWorkloadEntry entry = new AllDOWorkloadEntry();
            entry.cc= cc;
            entry.partition = p;
            entry.t1 = left;
            entry.t2 = NW2;
            list.addLast(entry);
        }
        if(NE2!=null){
            AllDOWorkloadEntry entry = new AllDOWorkloadEntry();
            entry.cc= cc;
            entry.partition = p;
            entry.t1 = left;
            entry.t2 = NE2;
            list.addLast(entry);
        }
        if(SW2!=null){
            AllDOWorkloadEntry entry = new AllDOWorkloadEntry();
            entry.cc= cc;
            entry.partition = p;
            entry.t1 = left;
            entry.t2 = SW2;
            list.addLast(entry);
        }
        if(SE2!=null){
            AllDOWorkloadEntry entry = new AllDOWorkloadEntry();
            entry.cc= cc;
            entry.partition = p;
            entry.t1 = left;
            entry.t2 = SE2;
            list.addLast(entry);
        }
    }
}
