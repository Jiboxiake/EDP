package dedp.DistanceOracles.Analytical;

import java.util.ArrayList;
import java.util.Collections;

public class ConnectedComponentAnalyzer {
    public static ArrayList<CCInfoCOntainer>information_container = new ArrayList();

    public static void insert(CCInfoCOntainer o){
        information_container.add(o);
        Collections.sort(information_container, Collections.reverseOrder());
    }
    public static void print(int num){
        for(int i=0; i<num; i++){
            System.out.println(information_container.get(i));
        }
    }
}
