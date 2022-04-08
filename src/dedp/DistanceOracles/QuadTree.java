package dedp.DistanceOracles;

import dedp.DistanceOracles.Precomputation.DiameterResult;
import dedp.DistanceOracles.Precomputation.PrecomputationResultDatabase;
import dedp.exceptions.ObjectNotFoundException;
import dedp.indexes.edgedisjoint.Partition;
import dedp.indexes.edgedisjoint.PartitionVertex;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
//todo: implement well-separated pairs
public class QuadTree {
    public static int max_depth=20;
    public static int nextID = 0;
    public static final int initial_depth = 4;
    private QuadTree parent;
    public int id;
    private QuadTree NW;
    private QuadTree NE;
    private QuadTree SW;
    private QuadTree SE;
    private int level;
    int size;
    private float diameter;
    private MortonCode mc;
    private float top_bound;
    private float bottom_bound;
    private float left_bound;
    private float right_bound;
    private float vertical;
    private float horizontal;
    //todo: set reference to the original Node in EDP.
   // private HashMap <Integer, PartitionVertex> vertices;
    private HashSet<Integer> vertices;

    public QuadTree(float top_bound, float bottom_bound, float left_bound, float right_bound, QuadTree parent, int level, HashMap <Integer, PartitionVertex> vertices){
        this.id = nextID++;
        this.size = vertices.size();
        this.top_bound=top_bound;
        this.bottom_bound=bottom_bound;
        this.left_bound=left_bound;
        this.right_bound=right_bound;
        this.level=level;
        this.parent=parent;
        //diameter not set yet
        this.diameter=-1;
        setMorton();
        //this.vertices=vertices;
        this.vertices= new HashSet<Integer>(vertices.size());
        HashMap <Integer, PartitionVertex> TL=new HashMap<>();
        HashMap <Integer, PartitionVertex> TR=new HashMap<>();
        HashMap <Integer, PartitionVertex> BL=new HashMap<>();
        HashMap <Integer, PartitionVertex> BR=new HashMap<>();
        horizontal = (top_bound-bottom_bound)/(float)2+bottom_bound;
        vertical = (right_bound-left_bound)/(float)2+left_bound;
        for(Map.Entry<Integer, PartitionVertex> set: vertices.entrySet()){
            //we only store at the max depth
            if(level==max_depth) {
                this.vertices.add(set.getKey());
            }else{
                this.vertices=null;
            }
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

        if(level<max_depth && vertices.size()>0){
            NW=new QuadTree(top_bound,horizontal+(float)0.000001,left_bound, vertical, this, level+1, TL);
            NE=new QuadTree(top_bound, horizontal+(float)0.000001, vertical+(float)0.000001, right_bound, this, level+1, TR);
            SW=new QuadTree(horizontal, bottom_bound, left_bound, vertical, this, level+1, BL);
            SE=new QuadTree(horizontal, bottom_bound, vertical+(float)0.000001, right_bound, this, level+1, BR);
        }else{
            NW = null;
            NE = null;
            SW = null;
            SE = null;
        }
    }


    public QuadTree( HashMap <Integer, PartitionVertex> vertices){
        //this(Parser.normalizeLat(Parser.max_lat), Parser.normalizeLat(Parser.min_lat), Parser.normalizeLon(Parser.min_long), Parser.normalizeLon(Parser.max_long), null,0,vertices);
        this(Parser.max_lat, Parser.min_lat, Parser.min_long, Parser.max_long, null,0,vertices);

    }

    public boolean contain(PartitionVertex v){
        //return vertices.contains(v.getId());
        return (v.latitude>=bottom_bound&&v.latitude<=top_bound&&v.longitude>=left_bound&&v.longitude<=right_bound);
    }

    public int getLevel(){
        return level;
    }
  /*  public PartitionVertex getVertex(Integer vID)throws ObjectNotFoundException {
        PartitionVertex toReturn=vertices.get(vID);
        if(toReturn==null){
            throw new ObjectNotFoundException("Vertex with mc: "+mc.morton+" not found");
        }
        return toReturn;
    }*/

    public synchronized float getDiameter(){
        return this.diameter;
    }
    public synchronized void setDiameter(float newDia){
        if(newDia>this.diameter){
            this.diameter=newDia;
        }
    }
    /*public HashSet<Integer> copy(){
        return new HashSet<>(vertices);
    }*/
    public void copy(HashSet<Integer> verSet){
        //base case
        if(this.level==max_depth){
            for(Integer e:vertices){
                verSet.add(e);
            }
        }else{
            if(NW!=null){
                NW.copy(verSet);
            }
            if(NE!=null){
                NE.copy(verSet);
            }
            if(SE!=null){
                SE.copy(verSet);
            }
            if(SW!=null){
                SW.copy(verSet);
            }
        }
    }

    public MortonCode getMC(){
        return mc;
    }

    public QuadTree containingBlock(PartitionVertex v){
    /*    assert(!vertices.isEmpty());
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
        return null;*/
        int quadrant = classifier(top_bound, horizontal, bottom_bound, left_bound, vertical, right_bound, v);
        if(quadrant==1){
            return NW;
        }else if(quadrant==2){
            return NE;
        }else if(quadrant==3){
            return SW;
        }else{
            assert(quadrant==4);
            return SE;
        }
    }

    public boolean reachMaxLevel(){
        return level==max_depth;
    }

   /* public void remove(Integer id){
        if(vertices.contains(id)){
            vertices.remove(id);
        }
    }*/
    public void remove(PartitionVertex v){
        if(vertices.contains(v.getId())){
            this.vertices.remove(v.getId());
            if(NW!=null){
                if(NW.contain(v)){
                    NW.remove(v);
                }
            }
            if(NE!=null){
                if(NE.contain(v)){
                    NE.remove(v);
                }
            }
            if(SW!=null){
                if(SW.contain(v)){
                 SW.remove(v);
                }
            }
            if(SE!=null){
                if(SE.contain(v)){
                    SE.remove(v);
                }
            }
        }
    }
    //return whether the quadtree's all vertices are removed
    public boolean isEmpty(){
        return vertices.isEmpty();
    }

    private int classifier (float top, float hor, float bot, float left, float ver, float right, PartitionVertex v){
        float x = v.longitude;
        float y = v.latitude;
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
        this.vertices.add(v.getId());
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

    public HashSet<Integer> getVertices(){
        return vertices;
    }

    public static void setMax_depth(int max){
        max_depth=max;
    }

    private void setMorton(){
        mc=new MortonCode(Parser.normalizeLat(bottom_bound), Parser.normalizeLon(left_bound), level, false);

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
        MortonCode toCompare=new MortonCode(Parser.normalizeLat(top_bound), Parser.normalizeLon(right_bound), level, false);
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

    public void output(){
        if(this.size==0){
            return;
        }
        PrecomputationResultDatabase.insert(new DiameterResult(this.id, this.diameter));
        if(NW!=null){
            NW.output();
        }
        if(NE!=null){
            NE.output();
        }
        if(SE!=null){
            SE.output();
        }
        if(SW!=null){
            SW.output();
        }
    }
}
