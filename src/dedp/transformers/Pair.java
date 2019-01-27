package dedp.transformers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.xml.crypto.dsig.keyinfo.KeyValue;

public class Pair 
{
	public Pair(int x, int y)
	{
		this.X = x;
		this.Y = y;
	}

	@Override
	public boolean equals(Object obj) 
	{
		Pair p = (Pair)obj;
		return ( (this.X == p.X && this.Y == p.Y) || (this.Y == p.X && this.X == p.Y));
	}
	
	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return (Integer.valueOf(X).hashCode()  + Integer.valueOf(Y).hashCode());
	}
	
	public int X, Y;
	
	
	public static void main(String[] args)
	{
		Pair p1 = new Pair(3, 2);
		Pair p2 = new Pair(2, 3);
		Pair p3 = new Pair(3, 1);
		
		
		HashMap<Pair, ArrayList<String>> map = new HashMap<Pair, ArrayList<String>>();
		ArrayList<String> lst = map.get(p1);
		if(lst == null)
		{
			System.out.println("Adding p1...");
			lst = new ArrayList<String>();
			lst.add("1");
			map.put(p1, lst);
		}
		else
		{
			lst.add("1");
		}
		lst = map.get(p2);
		if(lst == null)
		{
			System.out.println("Adding p2...");
			lst = new ArrayList<String>();
			lst.add("1");
			map.put(p2, lst);
		}
		else
		{
			lst.add("2");
		}
		lst = map.get(p3);
		if(lst == null)
		{
			System.out.println("Adding p3...");
			lst = new ArrayList<String>();
			lst.add("1");
			map.put(p3, lst);
		}
		else
		{
			lst.add("3");
		}
		for(Entry<Pair, ArrayList<String>> kv : map.entrySet())
		{
			System.out.println(kv.getKey().X + ", " + kv.getKey().Y + " : " + kv.getValue());
		}
		/*
		System.out.println(p1.equals(p2));
		System.out.println(p2.equals(p1));
		System.out.println(p1.equals(p3));
		System.out.println(p1.hashCode());
		System.out.println(p2.hashCode());
		System.out.println(p3.hashCode());
		*/
		ArrayList<String> strings = new ArrayList<String>();
		String s1 = "1";
		String s2 = "3";
		strings.add(s1);
		System.out.println(strings.contains(s2));
	}
}
