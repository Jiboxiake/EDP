package dedp.indexes.edgedisjoint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

import dedp.exceptions.ObjectNotFoundException;
import dedp.structures.DistanceFromSource;

public class BridgeEdgesComputationThread extends Thread
{
	@Override
	public void run() 
	{
		PriorityQueue<DistanceFromSource> q = new PriorityQueue<DistanceFromSource>();
		PartitionVertex u = null;
		DistanceFromSource uDist = new DistanceFromSource();
		uDist.VertexID = (long)this.fromVertexId;
		uDist.Distance = 0;
		uDist.PathLength = 0;
		q.add(uDist);
		Map<Long, DistanceFromSource> distMap = new HashMap<Long, DistanceFromSource>();
		distMap.put((long)fromVertexId, uDist);
		DistanceFromSource toDist = null;
		PartitionVertex sourceVertex = null;
		try {
			sourceVertex = partition.getVertex(this.fromVertexId);
		} catch (ObjectNotFoundException e3) {
			// TODO Auto-generated catch block
			e3.printStackTrace();
		}
		while(!q.isEmpty() && NonTerminated)
		{
			uDist = q.poll();
			try {
				u = partition.getVertex((int)uDist.VertexID);
			} catch (ObjectNotFoundException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}

			//add to bridge edge
			if(partition.isBridgeVertex((int)uDist.VertexID)&&uDist.VertexID!=sourceVertex.vertexId)
			{
				try
				{
					sourceVertex.lock.lock();
					//partition.addToBridgeEdge(this.fromVertexId, (int)toDist.VertexID, toDist.Distance);
					//
					partition.addToBridgeEdge(this.fromVertexId, (int)uDist.VertexID, uDist);
					sourceVertex.numOfBridgeEdgesComputed++;
					sourceVertex.bridgeEdgeAdded.signalAll();

				} catch (ObjectNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				finally
				{
					sourceVertex.lock.unlock();
				}
			}

			for(PartitionEdge e : u.outEdges) //here explore only direct monoedges and bridge edges only of the same color
			{
				PartitionVertex to = e.getTo();
				//get the distance of to node
				toDist = distMap.get((long)to.getId());
				if(toDist == null)
				{
					toDist = new DistanceFromSource();
					toDist.VertexID = (long)to.getId();
					toDist.Distance = uDist.Distance + e.getWeight();
					toDist.PathLength = uDist.PathLength + 1;
					q.add(toDist);
					distMap.put(toDist.VertexID, toDist);
			/*		if(partition.isBridgeVertex((int)toDist.VertexID))
					{
						try 
						{
							sourceVertex.lock.lock();
							//partition.addToBridgeEdge(this.fromVertexId, (int)toDist.VertexID, toDist.Distance);
							partition.addToBridgeEdge(this.fromVertexId, (int)toDist.VertexID, toDist);//todo: in DO we can keep creating DO from this source vertex.
							sourceVertex.numOfBridgeEdgesComputed++;
							sourceVertex.bridgeEdgeAdded.signalAll();
						} catch (ObjectNotFoundException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						finally
						{
							sourceVertex.lock.unlock();
						}
					}*/
				}
				else if(toDist.Distance > uDist.Distance + e.getWeight())
				{
					toDist.Distance = uDist.Distance + e.getWeight();
					toDist.PathLength = uDist.PathLength + 1;
					q.remove(toDist); //remove if it exists
					q.add(toDist);
				}
			}
		}
		try
		{
			sourceVertex.lock.lock();
			sourceVertex.allBridgeEdgesComputed = true;
			sourceVertex.bridgeEdgeAdded.signalAll();
		}
		finally
		{
			sourceVertex.lock.unlock();
		}
		try {
			if(NonTerminated)
			{
				//store to local disk
				//partition.addToBridgeIndexEntry(fromVertexId, partition.vertexToBridgeEdges.get(fromVertexId));
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) 
	{
		BridgeEdgesComputationThread brigeThread = new BridgeEdgesComputationThread();
		brigeThread.fromVertexId = 5;
		System.out.println("Main thread of main is: " + Thread.currentThread().getId());
		brigeThread.start();	
	}
	
	public int fromVertexId;
	public Partition partition;
	
	public volatile boolean NonTerminated = true;
}
