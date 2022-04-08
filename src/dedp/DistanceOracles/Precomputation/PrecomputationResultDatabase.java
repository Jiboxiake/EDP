package dedp.DistanceOracles.Precomputation;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class PrecomputationResultDatabase {
    public static ArrayList<DiameterResult> results = new ArrayList<>();
    public static String fileName = "quadtree_diameter.txt";
    public static void insert(DiameterResult r){
        results.add(r);
    }

    public static  void print(int i){
        Collections.sort(results, Collections.reverseOrder());
        for(int j=0; j<i; j++){
            System.out.println(results.get(j));
        }
    }
    public static void output(){
        try {
            FileWriter myWriter = new FileWriter(fileName);
            for(int i=0; i<results.size();i++){
                int id = results.get(i).quadtreeID;
                float diameter = results.get(i).diameter;
                String result = String.valueOf(id)+","+String.valueOf(diameter)+"\n";
                myWriter.write(result);
            }
            myWriter.close();
            System.out.println("Preprocessing finished");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
