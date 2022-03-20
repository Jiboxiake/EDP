package dedp.DistanceOracles;

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
    }
}
