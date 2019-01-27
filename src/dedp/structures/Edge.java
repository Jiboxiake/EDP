package dedp.structures;

import java.util.ArrayList;
import java.util.List;



public class Edge implements Comparable<Edge>
{

	
	public void setWeight(float weight)
	{
		this.weight = weight;
	}
	
	public float getWeight()
	{
		return this.weight;
	}
	
	public void setID(long id)
	{ 
		this.id = id;
	}
	
	public long getID()
	{
		return this.id;
	}
	
	public void setFrom(Vertex from)
	{ 
		this.from = from;
	}
	
	public Vertex getFrom()
	{
		return this.from;
	}
	
	public void setTo(Vertex to)
	{ 
		this.to = to;
	}
	
	public Vertex getTo()
	{
		return this.to;
	}
	
	public void setLabelsSet(List<Integer> labels)
	{ 
		this.Labels.clear();
		for(int l : labels)
		{
			this.Labels.add(l);
		}
		if(labels.size() > 0)
			this.label = labels.get(0);
	}
	
	public void setLabel(int label)
	{ 
		this.label = label;
		this.Labels.clear(); this.Labels.add(label);
	}
	
	public int getLabel()
	{
		return this.label;
	}
	
	public List<Integer> getLabelsSet()
	{
		return this.Labels;
	}
	
	protected long id;
	protected Vertex from;
	protected Vertex to;
	protected float weight;
	protected int label;
	
	public ArrayList<Integer> Labels = new ArrayList<Integer>();
	
	public static void main(String[] args) 
	{
		// TODO Auto-generated method stub

	}

	@Override
	public int compareTo(Edge e) {
		//return Integer.compare(this.label, e.label);
		return Float.compare(this.getWeight(), e.getWeight());
	}
	
}
