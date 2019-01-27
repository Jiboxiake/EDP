package dedp.indexes.edgedisjoint;


public class IndexEntry //implements Comparable<IndexEntry>
{
	public int PartitionId;
	public long EntryRepresentative;
	//public long TimeStamp;
	
	public IndexEntry Next = null;
	public IndexEntry Previous = null;
	
	
	public IndexEntry(int partitionId, long entryRepresentative)
	{
		this.PartitionId = partitionId;
		this.EntryRepresentative = entryRepresentative;
		this.Next = this.Previous = null;
	}
	
	/*
	public IndexEntry(int partitionId, long entryRepresentative, long timeStamp)
	{
		this.PartitionId = partitionId;
		this.EntryRepresentative = entryRepresentative;
		this.TimeStamp = timeStamp;
	}
	*/
	
	@Override
    public boolean equals(Object object) 
    {
    	IndexEntry entry = (IndexEntry)object;
 
        return (this.PartitionId == entry.PartitionId && this.EntryRepresentative == entry.EntryRepresentative);
    }
	
	@Override
	public String toString()
	{
		return "(" + this.PartitionId + ", " + this.EntryRepresentative + ")";
	}
	
	/*
	@Override
	public int compareTo(IndexEntry i2) 
	{
		return Long.compare(this.TimeStamp, i2.TimeStamp);
	}
	*/
}
