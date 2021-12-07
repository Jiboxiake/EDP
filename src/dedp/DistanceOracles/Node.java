package dedp.DistanceOracles;

public class Node{
    int x;
    int y;
    MortonCode mc;
    public Node(int x, int y){
        this.x=x;
        this.y=y;
        this.mc=new MortonCode(x,y,32, true);
    }
}