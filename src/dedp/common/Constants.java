package dedp.common;

import dedp.server.Server.ModeOfOperation;

public class Constants 
{
	public static String HostName = "localhost";//"localhost";
	public static int PortNumber = 123; //8889;
	//public static int LabeledPortNumber = 9999;
	
	
	


	//public static String ToBridgeEdgesBaseName = "D:\\Research\\RoadNetwork\\Implementation\\Data\\colored\\dc_directed\\edge_partition\\tobridge";
	//public static String ContractedGraphBaseName = "D:\\Research\\RoadNetwork\\Implementation\\Data\\colored\\dc_directed\\edge_partition\\graph";
	//public static String GraphFileName = "D:\\Research\\RoadNetwork\\Implementation\\Data\\colored\\DC_directed.csv";
	
	//public static String ToBridgeEdgesBaseName = "D:\\Research\\RoadNetwork\\Implementation\\Data\\colored\\test\\tobridge";
	//public static String ContractedGraphBaseName = "D:\\Research\\RoadNetwork\\Implementation\\Data\\colored\\test\\graph";
	//public static String GraphFileName = "D:\\Research\\RoadNetwork\\Implementation\\Data\\colored\\test\\test.csv";
	
	//public static String GraphFileName = "D:\\Research\\RoadNetwork\\Implementation\\Data\\colored\\test\\test2.csv";
	
	//public static String ContractedGraphBaseName = "D:\\Research\\RoadNetwork\\Implementation\\Data\\colored\\tiger_original\\edgedisjoint\\graph";
	//public static String GraphFileName = "D:\\Research\\RoadNetwork\\Implementation\\Data\\colored\\tiger_original\\original.csv";
	
	//public static String ToBridgeEdgesBaseName = "D:\\Research\\RoadNetwork\\Implementation\\Data\\colored\\bio\\index\\tobridge";
	//public static String ContractedGraphBaseName = "D:\\Research\\RoadNetwork\\Implementation\\Data\\colored\\bio\\index\\graph";
	//public static String GraphFileName = "D:\\Research\\RoadNetwork\\Implementation\\Data\\colored\\bio\\BioGrid.csv";
	//public static String ToBridgeEdgesBaseName = "D:\\Research\\Datasets\\random\\index\\tobridge";
	//public static String ContractedGraphBaseName = "D:\\Research\\Datasets\\random\\index\\graph";
	//public static String GraphFileName = "D:\\Research\\Datasets\\random\\graph1.csv";
	
	//texas 
	//public static String ToBridgeEdgesBaseName = "D:\\Research\\Datasets\\challenge9\\tiger\\texas\\index\\tobridge";
	//public static String ContractedGraphBaseName = "D:\\Research\\Datasets\\challenge9\\tiger\\texas\\index\\graph";
	//public static String GraphFileName = "D:\\Research\\Datasets\\challenge9\\tiger\\texas\\TX.csv";
	

	//CA 
	//public static String ToBridgeEdgesBaseName = "D:\\Research\\Datasets\\challenge9\\tiger\\CA\\index\\tobridge";
	//public static String ContractedGraphBaseName = "D:\\Research\\Datasets\\challenge9\\tiger\\CA\\index\\graph";
	//public static String GraphFileName = "D:\\Research\\Datasets\\challenge9\\tiger\\CA\\CA.csv";
	
	//dblp
	public static String ToBridgeEdgesBaseName;// = "D:\\Research\\Datasets\\dblp\\index\\tobridge";
	public static String ContractedGraphBaseName;// = "D:\\Research\\Datasets\\dblp\\index\\graph";
	public static String GraphFileName;// = "D:\\Research\\Datasets\\dblp\\dblpgraph.csv";
	public static String QueryFileBaseName;
	public static ModeOfOperation Mode;// = ModeOfOperation.EdgeDisjointIndex;
	public static int[] QueryLabels = new int[] {0,1};
	public static int NumOfQueries;
	public static int MinHops;
	public static int NumOfRandomSources = 100;
	public static int NumOfClientRequests = 100;
	public static int CHLRLimit = 100;
	public static int RunLength = 5;
	public static boolean SaveIndexEntriesToDisk = false;
	public static long CacheSize = -1;
	public static long NoCacheLimit = -1;
	//public static String GraphFileName = "D:\\Research\\RoadNetwork\\Implementation\\Data\\colored\\DC_undirected.csv";
	
	
	public static boolean Debug = false;
}
