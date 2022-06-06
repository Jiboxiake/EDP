package dedp.algorithms.hybridtraversal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;

import dedp.DistanceOracles.*;
import dedp.common.Constants;
import dedp.common.Helper;
import dedp.exceptions.ObjectNotFoundException;
import dedp.indexes.edgedisjoint.*;
import dedp.structures.DistanceFromSource;
import dedp.structures.Edge;
import dedp.structures.Graph;
import dedp.structures.SPResult;
import dedp.structures.Vertex;
//todo: garbage collection after a search.
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
        HashMap<Integer, HashMap<Integer, HybridBridgeEdgeList>> partitionVertexBridgeEdges = new HashMap<>();
        PartitionEdge e;
        DOQueueEntry lblDist = null;
        Partition currentPartition = null;
        PartitionVertex u = null;
        PartitionVertex destVertex = null;
        PartitionVertex toVertex = null;
        //for bridge edges
        HashMap<Integer, HybridBridgeEdgeList> bridgePerPartition = null;
        HybridBridgeEdgeList bridgeEdgeList=null;
        ConnectedComponent cc =null;

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
        while (!q.isEmpty()) {
            Global.addVertex();

            result.NumberOfExploredNodes++;
           // System.out.println(result.NumberOfExploredNodes);
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
                if(weight<=0){
                    throw new RuntimeException("weight between 2 vertices in the same cc is non-positive\n");
                }
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
            if(u==null){
                System.out.println(currentPartition.getVertex(uDist.VertexId));
            }
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
                        q.remove(lblDist); //remove if it exists
                        q.add(lblDist);
                    }
                }
            }
            //Now we try to add the bridge edges
            //first we check if this is the first time we meet this vertex
            cc = currentPartition.ConnectedComponents.getConnectedComponent(u.ComponentId);
            if(!partitionVertexBridgeEdges.containsKey(u.Label)){
                bridgePerPartition = new HashMap<>();
                partitionVertexBridgeEdges.put(u.Label,bridgePerPartition);
            }else{
                bridgePerPartition=partitionVertexBridgeEdges.get(u.Label);
            }
            if(bridgePerPartition.containsKey(u.getId())){
                bridgeEdgeList = bridgePerPartition.get(u.getId());
            }else{
                bridgeEdgeList = cc.getBridgeEdgeList(u);
                bridgePerPartition.put(u.getId(), bridgeEdgeList);
            }
            int countOfBridgeEdges = 0;
            for(int i=uDist.OutEdgeIdToProcess;;i++){
                e=bridgeEdgeList.getEdge();
                if(e==null){
                    break;
                }
                toVertex=e.getTo();
                if(currentPartition.getVertex(toVertex.getId())==null){
                    throw new ObjectNotFoundException("vertex "+toVertex.getId()+" is missing in partition "+currentPartition.Label+"\n");
                }
                toVertexId = toVertex.getId();
                if(countOfBridgeEdges == index.MaxToExplore /*&& (i < toBridgeEdgesSizeMinusOne)*/)
                {
                    if(toVertexId != destination)
                    {
                        uDist.OutEdgeIdToProcess = i;
                        uDist.PotentialDistance = uDist.Distance + e.getWeight();
                        q.add(uDist);
                        break;
                    }
                }
                toDist = distMap.get(toVertexId);
                newDistance = uDist.Distance + e.getWeight();
                //if going to bridge vertex is already more expensive than current explored path to dest, we ignore it
                //or if the explored path to the bridge vertex is less expensive.
                if(newDistance >= bestDistanceSoFar || (toDist != null && (toDist.Distance <= newDistance)))//this ensures we should never go back to the traversed vertices
                {
                    continue;
                }//else, if the new distance is better
                countOfBridgeEdges++;
                result.NumberOfExploredEdges++;
                otherHomes = Helper.intersection(toVertex.OtherHomes, labelIDs);
                furtherExplore = false;
                if(otherHomes.size() > 0)
                {
                    //update the distance to the toVertex if we have a shorter way
                    if(toDist == null)
                    {
                        toDist = new DOQueueEntry(toVertexId);
                        toDist.PartitionId = currentPartition.Label;
                        toDist.Distance = newDistance;
                        toDist.PotentialDistance = newDistance;
                       // q.add(toDist);
                        distMap.put(toDist.VertexId, toDist);
                    }
                    else if(toDist.Distance > newDistance)
                    {
                        toDist.Distance = newDistance;
                        toDist.PotentialDistance = newDistance;
                       // q.remove(toDist); //remove if it exists
                       // q.add(toDist);
                    }
                    Global.bridge_added();
                    furtherExplore = true;
                }
                if(furtherExplore)
                {
                    for(int otherHome : otherHomes)
                    {
                        //get the distance map of the other partition
                        lblDistMap = partitionToDistMap.get(otherHome);
                        lblDist = lblDistMap.get(toVertexId);
                        if(lblDist == null)
                        {
                            lblDist = new DOQueueEntry(toVertexId);
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
            }
        }


        //todo: garbage collection thread(empty all vertices' bridge lists if no one else is using it).
        return result;
    }
    public static boolean dealBridgeEdge(SPResult result,Map<Integer, Map<Integer, DOQueueEntry>> partitionToDistMap,List<Integer> labelIDs,HybridDOEDPIndex index, int destination, float bestDistanceSoFar, Partition currentPartition,DOQueueEntry uDist, Map<Integer, DOQueueEntry> distMap, PriorityQueue<DOQueueEntry> q, int bridgeEdgeFound,int i, PartitionEdge e){

        if(e == null)
        {
            return false;
        }
        PartitionVertex toVertex = e.getTo();//get a specific bridge vertex
        int toVertexId = toVertex.getId();
        //reach the max number of edges, in our case don't worry as degree of a vertex will be low.
        if(bridgeEdgeFound == index.MaxToExplore /*&& (i < toBridgeEdgesSizeMinusOne)*/)
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
