package dedp.structures;

import dedp.indexes.edgedisjoint.IndexEntry;

public class LRUManager
{

	//acts as a linked list for the index entries
	public LRUManager()
	{
		this.numOfElements = 0;
	}
	
	public long size()
	{
		return this.numOfElements;
	}
	
	public void addLast(IndexEntry entry)
	{
		if(numOfElements == 0)
		{
			entry.Next = entry.Previous = null;
			head = tail = entry;
		}
		else
		{
			entry.Next = null;
			entry.Previous = tail;
			tail.Next = entry;
			tail = entry;
		}
		this.numOfElements++;
	}
	
	public IndexEntry removeFirst()
	{
		IndexEntry entry = this.head;
		if(this.numOfElements > 0)
		{
			if(this.head.Next != null)
			{
				this.head.Next.Previous = null;
			}
			this.head = this.head.Next;
			this.numOfElements--;
		}
		if(this.numOfElements == 0)
		{
			this.head = this.tail = null;
		}
		return entry;
	}
	
	public void removeLast()
	{
		if(this.numOfElements > 0)
		{
			if(this.tail.Previous != null)
			{
				this.tail.Previous.Next = null;
			}
			this.tail = this.tail.Previous;
			this.numOfElements--;
		}
		if(this.numOfElements == 0)
		{
			this.head = this.tail = null;
		}
	}
	
	public void remove(IndexEntry entry)
	{
		if(entry == this.head)
		{
			removeFirst();
		}
		else if (entry == this.tail)
		{
			removeLast();
		}
		else if (entry.Next != null && entry.Previous != null)
		{
			entry.Previous.Next = entry.Next;
			entry.Next.Previous = entry.Previous;
			this.numOfElements--;
		}
	}
	
	@Override
	public String toString()
	{
		IndexEntry entry = this.head;
		String output = "Size = " + this.size() + "; ";
		while(entry != null)
		{
			output += entry.toString() + "; ";
			entry = entry.Next;
		}
		return output;
	}
	
	protected IndexEntry head = null;
	protected IndexEntry tail = null;
	protected long numOfElements;
	
	public static void main(String[] args) 
	{
		IndexEntry e1 = new IndexEntry(1, 1);
		IndexEntry e2 = new IndexEntry(1, 2);
		IndexEntry e3 = new IndexEntry(2, 1);
		IndexEntry e4 = new IndexEntry(2, 2);
		IndexEntry e5 = new IndexEntry(3, 1);
		
		LRUManager manager = new LRUManager();
		//manager.addLast(e1);
		//manager.addLast(e2);
		manager.addLast(e3);
		
		System.out.println(manager);
		
		manager.removeFirst();
		System.out.println(manager);
	}

}
