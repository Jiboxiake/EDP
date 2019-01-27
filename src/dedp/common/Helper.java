package dedp.common;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dedp.exceptions.DuplicateEntryException;
import dedp.exceptions.ObjectNotFoundException;
import dedp.indexes.edgedisjoint.Partition;
import dedp.io.GraphFileIO;
import dedp.server.Server.ModeOfOperation;
import dedp.structures.Graph;

public class Helper 
{
	public static Map<BytesValue, Float> distanceMap = new HashMap<BytesValue, Float>();
	
	public static void DebugMsg(String msg)
	{
		if(Constants.Debug)
		{
			System.out.println(msg);
		}
	}
	
	public static BytesValue getEdgeBytesRepresentative(long x, long y)
	{
		ByteBuffer buffer = ByteBuffer.allocate(16);
		/*
		if (x > y)
		{
			long temp = x;
			x = y;
			y = temp;
		}
		*/
	    buffer.putLong(x);
	    buffer.putLong(y);
	    return new BytesValue(buffer.array());
	}

	public static void main(String[] args) 
	{
		/*
		long x = 5678;
		long y = 12345;
		
		BytesValue buff = getEdgeBytesRepresentative(x, y);
		distanceMap.put(buff, 34f);
		BytesValue buff2 = getEdgeBytesRepresentative(12343, 5678);
		System.out.println(buff.hashCode());
		System.out.println(buff2.equals(buff));
		*/
		List<Integer> s1 = new ArrayList<Integer>();
		s1.add(2); s1.add(1); s1.add(3);
		Collections.sort(s1);
		List<Integer> s2 = new ArrayList<Integer>();
		s2.add(4); s2.add(3); s2.add(2);
		Collections.sort(s2);
		List<Integer> result = intersection(s1, s2);
		for(int i : result)
		{
			System.out.println(i);
		}
	}
	
	public static List<Integer> intersection(List<Integer> s1, List<Integer> s2)
	{
		ArrayList<Integer> result = new ArrayList<Integer>();
		int s1Size = s1.size(); 
		int s2Size = s2.size();
		/*
		List<Integer> smaller, bigger;
		if(s1.size() <= s2.size())
		{
			smaller = s1;
			bigger = s2;
		}
		else
		{
			smaller = s2;
			bigger = s1;
		}
		
		for(int i :smaller)
		{
			if(bigger.contains(i))
			{
				result.add(i);
			}
		}
		*/
		int i = 0, j = 0;
		int e1, e2;
		while(i < s1Size && j < s2Size)
		{
			e1 = s1.get(i);
			e2 = s2.get(j);
			if(e1 == e2)
			{
				result.add(e1);
				i++; j++;
			} 
			else if (e1 < e2)
			{
				i++;
			}
			else
			{
				j++;
			}
			
		}
		return result;
	}
	
	public static void setParametersFromFile(String fileName) throws IOException
	{
		BufferedReader reader = new BufferedReader(new FileReader(fileName));
		String line = "";
		String[] temp = null;
		String graphFileParamId = "raw", indexBaseParamId = "indexbase", 
				bridgeBaseParamId = "bridgebase", algorithmBaseParamId = "algorithm",
				hostParamId = "hostname", portParamId = "portnumber", labelParamId = "label", 
				queryBaseNameParamId = "qfile", queryNumParamId = "numofqueries", 
				minHopsParamId = "minhops", numOfRandomSourcesParamId = "randomsourcescount",
				numOfClientRequestsParamId = "numofclientrequests", chlrLimitParamId = "chlrlimit", runLengthParamId = "runlength";
		String edgeDisjointAlgName = "EdgeDisjointIndex", dijkstraAlgName = "Dijkstra", 
				biDijkstraAlgName = "BiDijkstra", riceAlgName = "RiceIndex", cacheSizeParamId = "cachesize";
		while((line = reader.readLine()) != null)
		{
			if(line.startsWith("\\"))
			{
				continue; //ignore comment
			}
			temp = line.split(",");
			if(temp[0].trim().equalsIgnoreCase(graphFileParamId))
			{
				Constants.GraphFileName = temp[1].trim();
			}
			else if(temp[0].trim().equalsIgnoreCase(indexBaseParamId))
			{
				Constants.ContractedGraphBaseName = temp[1].trim();
			}
			else if(temp[0].trim().equalsIgnoreCase(bridgeBaseParamId))
			{
				Constants.ToBridgeEdgesBaseName = temp[1].trim();
			}
			else if(temp[0].trim().equalsIgnoreCase(hostParamId))
			{
				Constants.HostName = temp[1].trim();
			}
			else if(temp[0].trim().equalsIgnoreCase(portParamId))
			{
				Constants.PortNumber = Integer.parseInt(temp[1].trim());
			}
			else if(temp[0].trim().equalsIgnoreCase(queryBaseNameParamId))
			{
				Constants.QueryFileBaseName = temp[1].trim();
			}
			else if(temp[0].trim().equalsIgnoreCase(queryNumParamId))
			{
				Constants.NumOfQueries = Integer.parseInt(temp[1].trim());
			}
			else if(temp[0].trim().equalsIgnoreCase(minHopsParamId))
			{
				Constants.MinHops = Integer.parseInt(temp[1].trim());
			}
			else if(temp[0].trim().equalsIgnoreCase(numOfRandomSourcesParamId))
			{
				Constants.NumOfRandomSources = Integer.parseInt(temp[1].trim());
			}
			else if(temp[0].trim().equalsIgnoreCase(numOfClientRequestsParamId))
			{
				Constants.NumOfClientRequests = Integer.parseInt(temp[1].trim());
			}
			else if(temp[0].trim().equalsIgnoreCase(chlrLimitParamId))
			{
				Constants.CHLRLimit = Integer.parseInt(temp[1].trim());
			}
			else if(temp[0].trim().equalsIgnoreCase(runLengthParamId))
			{
				Constants.RunLength = Integer.parseInt(temp[1].trim());
			}
			else if(temp[0].trim().equalsIgnoreCase(cacheSizeParamId))
			{
				Constants.CacheSize = Long.parseLong(temp[1].trim());
			}
			else if(temp[0].trim().equalsIgnoreCase(labelParamId))
			{
				String[] strLabels = temp[1].trim().split(";");
				Constants.QueryLabels = new int[strLabels.length];
				int index = 0;
				for(String strLabel : strLabels)
				{
					Constants.QueryLabels[index++] = Integer.parseInt(strLabel);
				}
			}
			else if(temp[0].trim().equalsIgnoreCase(algorithmBaseParamId))
			{
				if(temp[1].trim().equalsIgnoreCase(edgeDisjointAlgName))
				{
					Constants.Mode = ModeOfOperation.EdgeDisjointIndex;
				}
				else if(temp[1].trim().equalsIgnoreCase(riceAlgName))
				{
					Constants.Mode = ModeOfOperation.RiceIndex;
				}
				else if(temp[1].trim().equalsIgnoreCase(dijkstraAlgName))
				{
					Constants.Mode = ModeOfOperation.Dijkstra;
				}
				else if(temp[1].trim().equalsIgnoreCase(biDijkstraAlgName))
				{
					Constants.Mode = ModeOfOperation.BiDijkstra;
				}
			}
			
		}
		reader.close();
	}
	
	public static void printParamValues()
	{
		System.out.println("Algorithm: " + Constants.Mode);
		System.out.println("GraphFileName: " + Constants.GraphFileName);
		System.out.println("ContractedGraphBaseName: " + Constants.ContractedGraphBaseName);
		System.out.println("ToBridgeEdgesBaseName: " + Constants.ToBridgeEdgesBaseName);
		System.out.println("HostName: " + Constants.HostName);
		System.out.println("PortNumber: " + Constants.PortNumber);
		String labels = "";
		for(int label : Constants.QueryLabels)
		{
			labels += label + ";";
		}
		System.out.println("QueryLabels: " + labels);
		System.out.println("QueryFileBaseName: " + Constants.QueryFileBaseName);
		System.out.println("QuerySize: " + Constants.NumOfQueries);
		System.out.println("MinHops: " + Constants.MinHops);
		System.out.println("NumOfRandomSources: " + Constants.NumOfRandomSources);
		System.out.println("NumOfClientRequests: " + Constants.NumOfClientRequests);
		System.out.println("CHLR Limit: " + Constants.CHLRLimit);
		System.out.println("Cache Size: " + Constants.CacheSize);
		System.out.println("Run Length (used to compute avg. time): " + Constants.RunLength);
	}

}
