package dedp.DistanceOracles;

import java.util.ArrayList;

public class Global {
    private static int DO_Count;
    private static int Dij_Count;
    private static int Bridge_requested;
    private static int Bridge_vertices_added;
    private static int failed_WSP;
    private static int WSP_found;
    private static int num_vertices;
    private static int bridge_do_count;
    private static int DO_hit_during_bridge_computation;
    public static int total_do_threads;
    public static int level_count[]= new int[33];
    public static int total_bridge_vertices=0;
    public static int total_partition_vertex=0;
    public static int total_partition_edge=0;
    public static int WSPD_Pass = 0;
    public static int WSPD_Fail = 0;
    public static ArrayList<BridgeEdgeThread> list = new ArrayList<>();
    public static boolean debug = false;
    public static int badDOResult = 0;
    public static double maxError = 0;

    public synchronized static void DO_hit(){
        DO_Count++;
    }
    public synchronized  static void Dij_exec(){
        Dij_Count++;
    }
    public synchronized static void bridge_added(){
        Bridge_requested++;
    }
    public synchronized static void bridge_vertices_added(){
        Bridge_vertices_added++;
    }
    public synchronized  static void addNotWellSeparated(){
        failed_WSP++;
    }
    public synchronized static void addWSP(){
        WSP_found++;
    }
    public synchronized static void addVertex(){
        num_vertices++;
    }
    public synchronized static void addBridge_do_count(){
        bridge_do_count++;
    }
    public synchronized  static void addDO_hit_during_bridge_computation(){DO_hit_during_bridge_computation++;}
    public synchronized static void add_total_do_threads(){total_do_threads++;}
    public synchronized  static void addLevel(int level){
        level_count[level]++;
    }
    public synchronized  static void addThread(BridgeEdgeThread t){
        Global.list.add(t);
    }
    public synchronized  static void addBadDOResult(){
        badDOResult++;
    }
    public static void printResult(){
        System.out.println("number of DO hit is: "+DO_Count);
        System.out.println("number of Dij executed is: "+Dij_Count);
        System.out.println("number of better bridge edges found is: "+Bridge_requested);
        System.out.println("number of bridge vertices of other partitions added is: "+Bridge_vertices_added);
        System.out.println("number of vertex explored is "+num_vertices);
        System.out.println("number of failed WSP is: "+failed_WSP);
        System.out.println("number of WSP found is: "+WSP_found);
        System.out.println("number of DO creation for bridge edges is "+bridge_do_count);
        System.out.println("number of bridge partial DO hit is "+DO_hit_during_bridge_computation);
        for(int i=0; i< level_count.length;i++){
           // System.out.println("DO generated "+level_count[i]+" DO keys at level "+i);
        }
    }
    public static void clearResult(){
        DO_Count=0;
        Dij_Count=0;
        Bridge_requested=0;
        Bridge_vertices_added=0;
        failed_WSP=0;
        WSP_found=0;
        num_vertices=0;
        bridge_do_count=0;
        DO_hit_during_bridge_computation=0;
        total_do_threads=0;
        total_partition_vertex=0;
        total_partition_edge=0;
    }
}
