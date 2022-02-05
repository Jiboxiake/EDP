package dedp.DistanceOracles;


import java.util.List;

import dedp.structures.Edge;
import dedp.structures.Vertex;

public class DOQueueEntry implements Comparable<DOQueueEntry>
{
    public int VertexId;
    public int OutEdgeIdToProcess;
    public float Distance;
    public float PotentialDistance;
    public int PartitionId;
    public int PathLength;
    public boolean first;
    public boolean getDO;


    public DOQueueEntry(int VertexID){
        this.VertexId=VertexID;
        this.Distance=0;
        this.PotentialDistance=0;
        this.OutEdgeIdToProcess=0;
        this.PathLength=0;
        this.first=true;
        this.getDO=false;
    }

    public void setDO(){
        this.getDO=true;
    }

    public void setFirst(){
        this.first=false;
    }

    public void setPartitionId(Vertex vertex, List<Integer> labelIDs)
    {
        this.PartitionId = NoPartitionExistsId;
        for(Edge e : vertex.outEdges)
        {
            if(labelIDs.contains(e.getLabel()))
            {
                this.PartitionId = e.getLabel();
                break;
            }
        }
        if(this.PartitionId == NoPartitionExistsId)
        {
            //check the in edges
            for(Edge e : vertex.inEdges)
            {
                if(labelIDs.contains(e.getLabel()))
                {
                    this.PartitionId = e.getLabel();
                    break;
                }
            }
        }
    }

    public void setPartitionId_Forward(Vertex vertex, List<Integer> labelIDs)
    {
        this.PartitionId = NoPartitionExistsId;
        for(Edge e : vertex.outEdges)
        {
            if(labelIDs.contains(e.getLabel()))
            {
                this.PartitionId = e.getLabel();
                break;
            }
        }
    }

    public void setPartitionId_Backward(Vertex vertex, List<Integer> labelIDs)
    {
        this.PartitionId = NoPartitionExistsId;
        for(Edge e : vertex.inEdges)
        {
            if(labelIDs.contains(e.getLabel()))
            {
                this.PartitionId = e.getLabel();
                break;
            }
        }
    }



    @Override
    public int compareTo(DOQueueEntry d2)
    {
        //return Float.compare(this.Distance, d2.Distance);
        return Float.compare(this.PotentialDistance, d2.PotentialDistance);
    }

    public static final int NoPartitionExistsId = -1;
}