package dedp.DistanceOracles;

import dedp.exceptions.ObjectNotFoundException;
import dedp.indexes.edgedisjoint.Partition;
import dedp.indexes.edgedisjoint.PartitionVertex;

import java.util.HashMap;
import java.util.Map;
//todo: implement well-separated pairs
public class QuadTree {
    public static int max_depth=20;
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
    private HashMap <Integer, PartitionVertex> vertices;

    public QuadTree(int top_bound, int bottom_bound, int left_bound, int right_bound, QuadTree parent, int level, HashMap <Integer, PartitionVertex> vertices){
        this.top_bound=top_bound;
        this.bottom_bound=bottom_bound;
        this.left_bound=left_bound;
        this.right_bound=right_bound;
        this.level=level;
        this.parent=parent;
        setMorton();
        this.vertices=vertices;
        if(level<max_depth && vertices.size()>0){
            horizontal = (top_bound-bottom_bound)/2+bottom_bound;
            vertical = (right_bound-left_bound)/2+left_bound;
            HashMap <Integer, PartitionVertex> TL=new HashMap<>();
            HashMap <Integer, PartitionVertex> TR=new HashMap<>();
            HashMap <Integer, PartitionVertex> BL=new HashMap<>();
            HashMap <Integer, PartitionVertex> BR=new HashMap<>();
            for(Map.Entry<Integer, PartitionVertex> set: vertices.entrySet()){
                PartitionVertex v = set.getValue();
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

    public QuadTree( HashMap <Integer, PartitionVertex> vertices){
        this(Parser.normalizeLat(90.0), Parser.normalizeLat(-90.0), Parser.normalizeLon(-180.0), Parser.normalizeLon(180.0), null,0,vertices);
    }

    public boolean contain(PartitionVertex v){
        return vertices.containsKey(v.getId());
    }

    public int getLevel(){
        return level;
    }
    public PartitionVertex getVertex(Integer vID)throws ObjectNotFoundException {
        PartitionVertex toReturn=vertices.get(vID);
        if(toReturn==null){
            throw new ObjectNotFoundException("Vertex with mc: "+mc.morton+" not found");
        }
        return toReturn;
    }


    public HashMap<Integer, PartitionVertex> copy(){
        return null;
    }

    public MortonCode getMC(){
        return mc;
    }

    public QuadTree containingBlock(PartitionVertex v){
        assert(!vertices.isEmpty());
        if(vertices.size()==1){

        }
        if(NW!=null){
            if(NW.contain(v)){
                return NW;
            }
        }
        if(NE!=null){
            if(NE.contain(v)){
                return NE;
            }
        }
        if(SW!=null){
            if(SW.contain(v)){
                return SW;
            }
        }
        if(SE!=null){
            if(SE.contain(v)){
                return SE;
            }
        }
        assert(false);
        return null;
    }

    public PartitionVertex remove(Integer id){
        if(vertices.containsKey(id)){
            PartitionVertex toReturn=vertices.get(id);
            vertices.remove(id);
            return toReturn;
        }
        return null;
    }
    //return whether the quadtree's all vertices are removed
    public boolean isEmpty(){
        return vertices.isEmpty();
    }

    private int classifier (int top, int hor, int bot, int left, int ver, int right, PartitionVertex v){
        int x = v.longitude;
        int y = v.latitude;
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

    public void insert(PartitionVertex v){
        this.vertices.put(v.getId(), v);
        if(level<max_depth) {
           /* if(this.vertices.size()==1){
                    NW=new QuadTree(top_bound,horizontal+1,left_bound, vertical, this, level+1, new HashMap <Integer, PartitionVertex>());
                    NE=new QuadTree(top_bound, horizontal+1, vertical+1, right_bound, this, level+1, new HashMap <Integer, PartitionVertex>());
                    SW=new QuadTree(horizontal, bottom_bound, left_bound, vertical, this, level+1, new HashMap <Integer, PartitionVertex>());
                    SE=new QuadTree(horizontal, bottom_bound, vertical+1, right_bound, this, level+1, new HashMap <Integer, PartitionVertex>());
            }*/
            int quadrant = classifier(top_bound, horizontal, bottom_bound, left_bound, vertical, right_bound, v);
            if(quadrant==1){
                if(NW==null)
                    NW=new QuadTree(top_bound,horizontal+1,left_bound, vertical, this, level+1, new HashMap <Integer, PartitionVertex>());
                NW.insert(v);
            }else if(quadrant==2){
                if(NE==null)
                    NE=new QuadTree(top_bound, horizontal+1, vertical+1, right_bound, this, level+1, new HashMap <Integer, PartitionVertex>());
                NE.insert(v);
            }else if(quadrant==3){
                if(SW==null)
                    SW=new QuadTree(horizontal, bottom_bound, left_bound, vertical, this, level+1, new HashMap <Integer, PartitionVertex>());
                SW.insert(v);
            }else{
                assert(quadrant==4);
                if(SE==null)
                    SE=new QuadTree(horizontal, bottom_bound, vertical+1, right_bound, this, level+1, new HashMap <Integer, PartitionVertex>());
                SE.insert(v);
            }
        }
    }
    /*
    will delete in current level and in lower levels
     */
    public void delete(PartitionVertex v){
        this.vertices.remove(mc);
        if(level<max_depth) {
            int quadrant = classifier(top_bound, horizontal, bottom_bound, left_bound, vertical, right_bound, v);
            if(quadrant==1){
                NW.delete(v);
                if(NW.size()==0){
                    assert(NW.allNull());
                    this.NW=null;
                }
            }else if(quadrant==2){
                NE.delete(v);
                if(NE.size()==0){
                    assert(NE.allNull());
                    this.NE=null;
                }
            }else if(quadrant==3){
                SW.delete(v);
                if(SW.size()==0){
                    assert(SW.allNull());
                    this.SW=null;
                }
            }else{
                assert(quadrant==4);
                SE.delete(v);
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

    public HashMap<Integer, PartitionVertex> getVertices(){
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
       // MortonCode notEqual= new MortonCode(top_bound+1, right_bound+1, level, false);
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

    public boolean testMorton2() throws CloneNotSupportedException {
        boolean result=true;
        MortonCode copy;
        if(level<max_depth){
            if(NW!=null) {
                copy=NW.mc.shallowCopy();
                copy.shift();
                result = result && this.mc.exactlyEquals(copy) && NW.testMorton2();
                if(!this.mc.exactlyEquals(copy)){
                    mc.printBit();
                    NW.mc.printBit();
                   // copy.printBit();
                    System.out.println("Complain 1");
                }
            }
            if(NE!=null) {
                copy=NE.mc.shallowCopy();
                copy.shift();
                result = result && this.mc.exactlyEquals(copy) && NE.testMorton2();
                if(!this.mc.exactlyEquals(copy)){
                    mc.printBit();
                    NE.mc.printBit();
                  //  copy.printBit();
                    System.out.println("Complain 2");
                }
            }
            if(SW!=null) {
                copy=SW.mc.shallowCopy();
                copy.shift();
                result = result && this.mc.exactlyEquals(copy)&& SW.testMorton2();
                if(!this.mc.exactlyEquals(copy)){
                    mc.printBit();
                    SW.mc.printBit();
                 //   copy.printBit();
                    System.out.println("Complain 3");
                }
            }
            if(SE!=null) {
                copy=SE.mc.shallowCopy();
                copy.shift();
                result = result && this.mc.exactlyEquals(copy) && SE.testMorton2();
                if(!this.mc.exactlyEquals(copy)){
                    mc.printBit();
                    SE.mc.printBit();
                 //   copy.printBit();
                    System.out.println("Complain 4");
                }
            }
        }
     /*   if(!result){
            mc.printBit();
            System.out.println("Complain");
        }*/
        return result;
    }

}
