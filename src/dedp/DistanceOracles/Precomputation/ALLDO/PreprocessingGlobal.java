package dedp.DistanceOracles.Precomputation.ALLDO;

public class PreprocessingGlobal {
    public static int total_queue_insertion=0;
    public static int total_dij_run=0;
    public static int[] doLevel = new int[17];

    public synchronized  static void queueInsertionAdd(){
        total_queue_insertion++;
    }
    public synchronized static void dijAdd(){
        total_dij_run++;
    }
    public synchronized  static void doLevelAdd(int level){
        doLevel[level]++;
    }
}
