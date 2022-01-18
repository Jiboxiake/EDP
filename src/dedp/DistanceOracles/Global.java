package dedp.DistanceOracles;

public class Global {
    private static int DO_Count;
    private static int Dij_Count;

    public synchronized static void DO_hit(){
        DO_Count++;
    }
    public synchronized  static void Dij_exec(){
        Dij_Count++;
    }

    public static void printResult(){
        System.out.println("number of DO hit is: "+DO_Count);
        System.out.println("number of Dij executed is: "+Dij_Count);
    }
}
