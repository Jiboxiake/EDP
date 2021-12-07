package dedp.DistanceOracles;

import java.util.HashMap;
import java.util.Map;

public class QuadTree {
    public static int max_depth;

    private QuadTree parent;
    private QuadTree NW;
    private QuadTree NE;
    private QuadTree SW;
    private QuadTree SE;
    private int level;
    private MortonCode mc;
    private int top_bound;
    private int bottom_bound;
    private int left_bound;
    private int right_bound;
    private int vertical;
    private int horizontal;
    //todo: set reference to the original Node in EDP.
    private HashMap <MortonCode, Node> vertices;

    public QuadTree(int top_bound, int bottom_bound, int left_bound, int right_bound, QuadTree parent, int level, HashMap <MortonCode, Node> vertices){
        this.top_bound=top_bound;
        this.bottom_bound=bottom_bound;
        this.left_bound=left_bound;
        this.right_bound=right_bound;
        this.level=level;
        this.parent=parent;
        setMorton();
        this.vertices=(HashMap <MortonCode, Node>)vertices.clone();
        if(level<max_depth && vertices.size()>0){
            horizontal = (top_bound-bottom_bound)/2+bottom_bound;
            vertical = (right_bound-left_bound)/2+left_bound;
            HashMap <MortonCode, Node> TL=new HashMap<>();
            HashMap <MortonCode, Node> TR=new HashMap<>();
            HashMap <MortonCode, Node> BL=new HashMap<>();
            HashMap <MortonCode, Node> BR=new HashMap<>();
            for(Map.Entry<MortonCode, Node> set: vertices.entrySet()){
                Node v = set.getValue();
                //check boundaries against what we set
                int quadrant = classifier(top_bound, horizontal, bottom_bound, left_bound, vertical, right_bound, v);
                if(quadrant==1){
                    TL.put(set.getKey(),v);
                }else if(quadrant==2){
                    TR.put(set.getKey(),v);
                }else if(quadrant==3){
                    BL.put(set.getKey(),v);
                }else{
                    assert(quadrant==4);
                    BR.put(set.getKey(),v);
                }
            }
            NW=new QuadTree(top_bound,horizontal+1,left_bound, vertical, this, level+1, TL);
            NE=new QuadTree(top_bound, horizontal+1, vertical+1, right_bound, this, level+1, TR);
            SW=new QuadTree(horizontal, bottom_bound, left_bound, vertical, this, level+1, BL);
            SE=new QuadTree(horizontal, bottom_bound, vertical+1, right_bound, this, level+1, BR);
        }
    }

    private int classifier (int top, int hor, int bot, int left, int ver, int right, Node v){
        int x = v.x;
        int y = v.y;
        assert(x<=top&&x>=bot);
        assert(y>=left&&y<=right);
        boolean isTop = y>(hor+1);
        boolean isLeft = x<=ver;
        if(isTop){
            if(isLeft)
                return 1;
            return 2;
        }else{
            if(isLeft)
                return 3;
            return 4;
        }
    }

    public void insert(MortonCode mc, int x, int y){
        Node v = new Node(x,y);
        this.vertices.put(mc, v);
        if(level<max_depth) {
            int quadrant = classifier(top_bound, horizontal, bottom_bound, left_bound, vertical, right_bound, v);
            if(quadrant==1){
                NW.insert(mc, x, y);
            }else if(quadrant==2){
                NE.insert(mc, x, y);
            }else if(quadrant==3){
                SW.insert(mc, x,y);
            }else{
                assert(quadrant==4);
                SE.insert(mc,x,y);
            }
        }
    }
    /*
    will delete in current level and in lower levels
     */
    public void delete(MortonCode mc, int x, int y){
        Node v = new Node(x,y);
        this.vertices.remove(mc);
        if(level<max_depth) {
            int quadrant = classifier(top_bound, horizontal, bottom_bound, left_bound, vertical, right_bound, v);
            if(quadrant==1){
                NW.delete(mc, x, y);
                if(NW.size()==0){
                    assert(NW.allNull());
                    this.NW=null;
                }
            }else if(quadrant==2){
                NE.delete(mc,x,y);
                if(NE.size()==0){
                    assert(NE.allNull());
                    this.NE=null;
                }
            }else if(quadrant==3){
                SW.delete(mc,x,y);
                if(SW.size()==0){
                    assert(SW.allNull());
                    this.SW=null;
                }
            }else{
                assert(quadrant==4);
                SE.delete(mc,x,y);
                if(SE.size()==0){
                    assert(SE.allNull());
                    this.SE=null;
                }
            }
        }
    }

    public boolean allNull(){
        return NW==null && NE==null && SW==null && SE==null;
    }

    public HashMap<MortonCode, Node> getVertices(){
        return vertices;
    }

    public static void setMax_depth(int max){
        max_depth=max;
    }

    private void setMorton(){
        mc=new MortonCode(bottom_bound, left_bound, level, false);

    }
    public void info(){
        System.out.println("The current tree is at level "+level);
        System.out.println("The current top bound is "+top_bound);
        System.out.println("The current bottom bound is "+bottom_bound);
        System.out.println("The current left bound is "+left_bound);
        System.out.println("The current right bound is "+right_bound);
        System.out.println("the current vertex size is "+vertices.size());
        if(level<max_depth){
            NW.info();
            NE.info();
            SW.info();
            SE.info();
        }
    }

    public int size(){
        return vertices.size();
    }

    public boolean testMorton(){
        MortonCode toCompare=new MortonCode(top_bound, right_bound, level, false);
        boolean result=mc.equals(toCompare);
        if(result==false){
            System.out.println("lat is "+bottom_bound+" lon is "+left_bound);
            System.out.println("Original is "+Long.toBinaryString(mc.morton)+ " Length is "+Long.toBinaryString(mc.morton).length());
            System.out.println("New one is " +Long.toBinaryString(toCompare.morton)+" Length is "+Long.toBinaryString(toCompare.morton).length());
        }
        if(level<max_depth){
            if(NW!=null)
                result = result&&NW.testMorton();
            if(NE!=null)
                result=result&&NE.testMorton();
            if(SW!=null)
                result=result&&SW.testMorton();
            if(SE!=null)
                result=result&&SE.testMorton();
        }
        return result;

    }

}
