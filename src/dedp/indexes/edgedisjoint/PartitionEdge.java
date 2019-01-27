package dedp.indexes.edgedisjoint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class PartitionEdge implements Comparable<PartitionEdge>
{

	public void setWeight(float weight)
	{
		this.weight = weight;
	}
	
	public float getWeight()
	{
		return this.weight;
	}
	
	public void setId(int id)
	{ 
		this.id = id;
	}
	
	public int getId()
	{
		return this.id;
	}
	
	public void setFrom(PartitionVertex from)
	{ 
		this.from = from;
	}
	
	public PartitionVertex getFrom()
	{
		return this.from;
	}
	
	public void setTo(PartitionVertex to)
	{ 
		this.to = to;
	}
	
	public PartitionVertex getTo()
	{
		return this.to;
	}
	
	public void setLabel(int label)
	{ 
		this.label = label;
	}
	
	public int getLabel()
	{
		return this.label;
	}
	
	public int PathLength = 0;
	
	protected int id;
	protected PartitionVertex from;
	protected PartitionVertex to;
	protected float weight;
	protected int label;
	

	@Override
	public int compareTo(PartitionEdge e) {
		//return Integer.compare(this.label, e.label);
		return Float.compare(this.weight, e.weight);
	}
	
	public static void main(String[] args)
	{
		ArrayList<PartitionEdge> lst = new ArrayList<PartitionEdge>();
		PartitionEdge e = new PartitionEdge();
		e.setWeight(5.6f);
		lst.add(e);
		e = new PartitionEdge();
		e.setWeight(4.7f);
		lst.add(e);
		Collections.sort(lst);
		HashMap<Integer, ArrayList<PartitionEdge>> map = new HashMap<Integer, ArrayList<PartitionEdge>>();
		map.put(1, lst);
		lst = map.get(1);
		for(int i = 0; i < lst.size(); i++)
		{
			System.out.println(lst.get(i).getWeight());
		}
	}
	
}
