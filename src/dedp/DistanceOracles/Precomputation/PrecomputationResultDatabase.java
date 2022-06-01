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
                if(diameter!=-1.0){
                    String result = String.valueOf(id)+","+String.valueOf(diameter)+"\n";
                    myWriter.write(result);
                }
            }
            myWriter.close();
            System.out.println("Preprocessing finished");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
/*

1101010001001110010100
11010100010011100101001100011000
0111111000110010100110
11010100010011100101001100011000
01111110001100101001100011000010
1111011101110000001101001011100100100111000
1011011101110100001001011010110001100011010010100101001010000100
2067622
3478420
*/
