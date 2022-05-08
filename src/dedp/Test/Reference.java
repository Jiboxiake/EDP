package dedp.Test;

import java.util.ArrayList;

public class Reference {
    public ArrayList<Integer> testList;
    public Reference(){
        testList=new ArrayList<>();
        testList.add(10);
    }
    public ArrayList copy(){
        return testList;
    }
    public static void main(String[] args) throws Exception {
        ArrayList<Integer> test=null;
        Reference r = new Reference();
        test=r.copy();
        r.testList.add(20);
        System.out.println(test.size());
    }
}
