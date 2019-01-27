package dedp.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;

import javax.swing.ViewportLayout;

import dedp.algorithms.Dijkstra;
import dedp.algorithms.bidirectional.BidirectionalDijkstra;
import dedp.algorithms.bidirectional.BidirectionalHybrid;
import dedp.algorithms.hybridtraversal.HybridTraversal;
import dedp.common.Constants;
import dedp.common.Helper;
import dedp.exceptions.DuplicateEntryException;
import dedp.exceptions.ObjectNotFoundException;
import dedp.indexes.DisjointLabelsIndex;
import dedp.indexes.MonoContractions;
import dedp.indexes.PureMonoContractions;
import dedp.indexes.edgedisjoint.EdgeDisjointIndex;
import dedp.indexes.edgedisjoint.Partition;
import dedp.io.GraphFileIO;
import dedp.structures.Graph;
import dedp.structures.SPResult;

public class Server 
{

	public static Graph graph;
	
	public static int requestId = 1;
	
	public enum ModeOfOperation {EdgeDisjointIndex, BiHybridIndex, RiceIndex, Dijkstra, BiDijkstra}
	
	// Code removed as it references a bigger project
}
