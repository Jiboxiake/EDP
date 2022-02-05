package dedp.algorithms.hybridtraversal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;

import dedp.DistanceOracles.DOQueueEntry;
import dedp.DistanceOracles.Global;
import dedp.DistanceOracles.HybridDOEDPIndex;
import dedp.common.Constants;
import dedp.common.Helper;
import dedp.exceptions.ObjectNotFoundException;
import dedp.indexes.edgedisjoint.*;
import dedp.structures.DistanceFromSource;
import dedp.structures.Edge;
import dedp.structures.Graph;
import dedp.structures.SPResult;
import dedp.structures.Vertex;

public class DOTraversal {
    public static SPResult shortestDistanceWithDO(HybridDOEDPIndex index, int source, int destination, List<Integer> labelIDs) throws Exception {
        SPResult result = new SPResult();
        result.Distance = -1;
        result.NumberOfExploredEdges = 0;
        result.NumberOfExploredNodes = 0;
        result.NumberOfHybridEdgesExplored = 0;
        PriorityQueue<DOQueueEntry> q = new PriorityQueue<>();
        Map<Integer, Map<Integer, DOQueueEntry>> partitionToDistMap = new HashMap<Integer, Map<Integer, DOQueueEntry>>();
        for (int label : labelIDs) {
            partitionToDistMap.put(label, new HashMap<Integer, DOQueueEntry>());
        }
        Map<Integer, DOQueueEntry> distMap = null;
        Map<Integer, DOQueueEntry> lblDistMap = null;
        HashMap<Integer, HashMap<Integer, ArrayList<PartitionEdge>>> partitionVertexBridgeEdges = new HashMap<>();
        PartitionEdge e;
        DOQueueEntry lblDist = null;
        Partition currentPartition = null;
        PartitionVertex u = null;
        PartitionVertex destVertex = null;
        PartitionVertex toVertex = null;
        DOQueueEntry uDist = new DOQueueEntry(source);
        uDist.setPartitionId(index.PlainGraph.getVertex(source), labelIDs);
        if (uDist.PartitionId != DistFromSource.NoPartitionExistsId) {
            q.add(uDist);
            partitionToDistMap.get(uDist.PartitionId).put(source, uDist);
            result.NumberOfExploredNodes++;
        }
        DOQueueEntry toDist = null;
        float bestDistanceSoFar = Float.POSITIVE_INFINITY;
        float weight = 0;
        List<Integer> otherHomes = null;
        float newDistance = 0;
        boolean furtherExplore = false;
        int uId, toVertexId;
        long totalStartTime = System.nanoTime();
        while (!q.isEmpty()) {
            Global.addVertex();
            result.NumberOfExploredNodes++;
            uDist = q.poll();
            currentPartition = index.getPartition(uDist.PartitionId);
            distMap = partitionToDistMap.get(uDist.PartitionId);
            u = currentPartition.getVertex(uDist.VertexId);
            uId = uDist.VertexId;
            destVertex = currentPartition.getVertex(destination);
            if (uId == destination) {
                result.Distance = uDist.Distance;
                result.PathLength = uDist.PathLength;
                break;
            }
            //if the destination vertex is in the current partition and in the same CC
            if (destVertex != null && currentPartition.inTheSameComponent(u, destVertex)) {
                weight = currentPartition.getEdgeWeightDO(uId, destination);
                newDistance = uDist.Distance + weight;
                //if we observe better distance than the best so far
                if (newDistance < bestDistanceSoFar) {
                    toDist = distMap.get(destination);
                    if (toDist == null) {
                        toDist = new DOQueueEntry(destination);
                        toDist.VertexId = destination;
                        toDist.PartitionId = currentPartition.Label;
                        toDist.Distance = newDistance;
                        toDist.PotentialDistance = toDist.Distance;
                        toDist.OutEdgeIdToProcess = 0;
                        q.add(toDist);
                        distMap.put(destination, toDist);
                    }
                    //just like Dij, we find better distance for destination
                    else if (toDist.Distance > newDistance) {
                        toDist.Distance = newDistance;
                        toDist.PotentialDistance = newDistance;
                        toDist.OutEdgeIdToProcess = 0;
                        q.remove(toDist); //remove if it exists
                        q.add(toDist);
                    }
                    bestDistanceSoFar = newDistance;
                }
            }
            //if Traversal reaches a bridge vertex
            if (u.isBridge()) {
                otherHomes = Helper.intersection(u.OtherHomes, labelIDs);
                for (int otherHome : otherHomes) {
                    result.NumberOfHybridEdgesExplored++;
                    lblDistMap = partitionToDistMap.get(otherHome);
                    lblDist = lblDistMap.get(uId);
                    if (lblDist == null) {
                        lblDist = new DOQueueEntry(uId);
                        lblDist.VertexId = uId;
                        lblDist.PartitionId = otherHome;
                        lblDist.Distance = uDist.Distance;
                        lblDist.PotentialDistance = uDist.Distance;
                        lblDist.OutEdgeIdToProcess = 0;
                        q.add(lblDist);
                        lblDistMap.put(lblDist.VertexId, lblDist);
                    } else if (lblDist.Distance > uDist.Distance) {
                        lblDist.Distance = uDist.Distance;
                        lblDist.PotentialDistance = uDist.Distance;
                        lblDist.OutEdgeIdToProcess = 0;
                        lblDist.OutEdgeIdToProcess = 0;
                        q.remove(lblDist); //remove if it exists
                        q.add(lblDist);
                    }
                }
            }
            //Now we try to add the bridge edges
            //first we check if this is the first time we meet this vertex
            if (uDist.first) {
                uDist.setFirst();
                HashMap<Integer, ArrayList<PartitionEdge>> partitionBridgeMap;
                if (!partitionVertexBridgeEdges.containsKey(currentPartition.Label)) {
                    partitionBridgeMap = new HashMap<>();
                    partitionVertexBridgeEdges.put(currentPartition.Label, partitionBridgeMap);
                } else {
                    partitionBridgeMap = partitionVertexBridgeEdges.get(currentPartition.Label);
                }
                //if it is already under bridge computation and first time
                if (u.underBridgeComputation) {
                    //check lock/concurrency of bridge list, towards the end of computation
                    ArrayList<PartitionEdge> bridgeList = u.thread.getBridgeEdgeList();
                    partitionBridgeMap.put(u.getId(), bridgeList);
                    for (int i = 0; ; i++) {
                        u.lock.lock();
                        //todo: check synchronization here
                        if (u.numOfBridgeEdgesComputed-1 <=i ) {
                            u.bridgeEdgeAdded.await();
                        }
                        u.lock.unlock();
                        PartitionEdge edge = bridgeList.get(i);
                        boolean continue_check = dealBridgeEdge(result,partitionToDistMap,labelIDs, index, destination, bestDistanceSoFar, currentPartition, uDist, distMap, q, i, edge);
                        if(!continue_check){
                            break;
                        }
                    }
                } else {//if u is not currently under bridge edge computation
                    ArrayList<PartitionEdge>bridgeList = new ArrayList<>();
                    ConnectedComponent cc =currentPartition.ConnectedComponents.getConnectedComponent(u.ComponentId);
                    boolean hitDO=cc.checkBridgeDO(u,bridgeList);
                    if(hitDO){
                        uDist.setDO();
                        partitionBridgeMap.put(u.getId(), bridgeList);
                        //just read from the bridgelist is enough
                        for(int i=0; ;i++){
                            e=bridgeList.get(i);
                            boolean continue_search=dealBridgeEdge(result,partitionToDistMap, labelIDs,index,destination,bestDistanceSoFar,currentPartition,uDist,distMap,q,i,e);

                        }
                    }else{
                        //we must start bridge edge computation
                    }

                }

            } else {

            }

        }
        return result;
    }
    public static boolean dealBridgeEdge(SPResult result,Map<Integer, Map<Integer, DOQueueEntry>> partitionToDistMap,List<Integer> labelIDs,HybridDOEDPIndex index, int destination, float bestDistanceSoFar, Partition currentPartition,DOQueueEntry uDist, Map<Integer, DOQueueEntry> distMap, PriorityQueue<DOQueueEntry> q, int i, PartitionEdge e){

        if(e == null)
        {
            return false;
        }
        PartitionVertex toVertex = e.getTo();//get a specific bridge vertex
        int toVertexId = toVertex.getId();
        //reach the max number of edges, in our case don't worry as degree of a vertex will be low.
        if(i == index.MaxToExplore /*&& (i < toBridgeEdgesSizeMinusOne)*/)
        {
            if(toVertexId != destination)
            {
                uDist.OutEdgeIdToProcess = i;
                uDist.PotentialDistance = uDist.Distance + e.getWeight();
                q.add(uDist);
                return false;
            }
        }

        DOQueueEntry toDist = distMap.get(toVertexId);
        float newDistance = uDist.Distance + e.getWeight();
        //if going to bridge vertex is already more expensive than current explored path to dest, we ignore it
        //or if the explored path to the bridge vertex is less expensive.
        if(newDistance >= bestDistanceSoFar || (toDist != null && (toDist.Distance <= newDistance)))
        {
            return true;
        }//else, if the new distance is better
        result.NumberOfExploredEdges++;
        List<Integer> otherHomes = Helper.intersection(toVertex.OtherHomes, labelIDs);
        boolean furtherExplore = false;
        if(otherHomes.size() > 0)
        {
            //update the distance to the toVertex if we have a shorter way
            if(toDist == null)
            {
                toDist = new DOQueueEntry(toVertexId);
                //toDist.VertexId = toVertexId;
                toDist.PartitionId = currentPartition.Label;
                toDist.Distance = newDistance;
                toDist.PotentialDistance = newDistance;
                q.add(toDist);
                distMap.put(toDist.VertexId, toDist);
            }
            else if(toDist.Distance > newDistance)
            {
                toDist.Distance = newDistance;
                toDist.PotentialDistance = newDistance;
                q.remove(toDist); //remove if it exists
                q.add(toDist);
            }
            Global.bridge_added();
            furtherExplore = true;
        }
        if(furtherExplore)
        {
            for(int otherHome : otherHomes)
            {
                //get the distance map of the other partition
                Map<Integer, DOQueueEntry>lblDistMap = partitionToDistMap.get(otherHome);
                DOQueueEntry lblDist = lblDistMap.get(toVertexId);
                if(lblDist == null)
                {
                    lblDist = new DOQueueEntry(toVertexId);
                   // lblDist.VertexId = toVertexId;
                    lblDist.PartitionId = otherHome;
                    lblDist.Distance = newDistance;
                    lblDist.PotentialDistance = newDistance;
                    q.add(lblDist);
                    lblDistMap.put(lblDist.VertexId, lblDist);
                    Global.bridge_vertices_added();
                }
                else if(lblDist.Distance > newDistance)
                {
                    lblDist.Distance = newDistance;
                    lblDist.PotentialDistance = newDistance;
                    q.remove(lblDist); //remove if it exists
                    q.add(lblDist);
                }
            }
        }
        return true;
    }

}
