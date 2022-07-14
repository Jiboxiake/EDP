package dedp.DistanceOracles;

import dedp.DistanceOracles.Precomputation.DiameterResult;
import dedp.DistanceOracles.Precomputation.PrecomputationResultDatabase;
import dedp.DistanceOracles.Precomputation.allDiameter.DiameterRepPointPair;
import dedp.exceptions.ObjectNotFoundException;
import dedp.indexes.edgedisjoint.Partition;
import dedp.indexes.edgedisjoint.PartitionVertex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
//todo: implement well-separated pairs
public class QuadTree {
    public static int max_depth=14;
    public static int nextID = 0;
    public static final int initial_depth = 2;
    private QuadTree parent;
    public int id;
    public QuadTree NW;
    public QuadTree NE;
    public QuadTree SW;
    public QuadTree SE;
    private int level;
    int size;
    public int representativePoint;
    private float diameter;
    private MortonCode mc;
    private int top_bound;
    private int bottom_bound;
    private int left_bound;
    private int right_bound;
    private int vertical;
    private int horizontal;
    //todo: set reference to the original Node in EDP.
   // private HashMap <Integer, PartitionVertex> vertices;
    private HashMap <Integer, PartitionVertex> vertices;

    public QuadTree(int top_bound, int bottom_bound, int left_bound, int right_bound, QuadTree parent, int level, HashMap <Integer, PartitionVertex> vertices){
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
        if(vertices.size()==0||vertices.size()==1){
            this.diameter=0;
        }
        if(vertices.size()==1){
            for(Map.Entry<Integer, PartitionVertex> set: vertices.entrySet()){
                this.representativePoint = set.getKey();
            }
        }else{
            this.representativePoint = -1;
        }
        //this.vertices=vertices;
        //this.vertices= new HashSet<Integer>(vertices.size());
        this.vertices = new HashMap<>(vertices.size());
        HashMap <Integer, PartitionVertex> TL=new HashMap<>();
        HashMap <Integer, PartitionVertex> TR=new HashMap<>();
        HashMap <Integer, PartitionVertex> BL=new HashMap<>();
        HashMap <Integer, PartitionVertex> BR=new HashMap<>();
        this.horizontal = (top_bound-bottom_bound)/2+bottom_bound;
        this.vertical = (right_bound-left_bound)/2+left_bound;
        //setMorton();
   /*     if((this.right_bound-this.left_bound)%2==0){
            this.vertical--;
        }else{

        }*/

        for(Map.Entry<Integer, PartitionVertex> set: vertices.entrySet()){
            //we only store at the max depth
            if(level==max_depth) {
                this.vertices.put(set.getKey(),set.getValue());
            }else{
                this.vertices=null;
            }
            PartitionVertex v = set.getValue();
            //check boundaries against what we set
            int quadrant = classifier( v);
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
            NW=new QuadTree(top_bound,horizontal,left_bound, vertical, this, level+1, TL);
            NE=new QuadTree(top_bound, horizontal, vertical, right_bound, this, level+1, TR);
            SW=new QuadTree(horizontal, bottom_bound, left_bound, vertical, this, level+1, BL);
            SE=new QuadTree(horizontal, bottom_bound, vertical, right_bound, this, level+1, BR);
        }else{
            NW = null;
            NE = null;
            SW = null;
            SE = null;
        }
        if(this.level==initial_depth){
            setMorton();
            //testMorton();
        }
    }


    public QuadTree( HashMap <Integer, PartitionVertex> vertices){
        //this(Parser.normalizeLat(Parser.max_lat), Parser.normalizeLat(Parser.min_lat), Parser.normalizeLon(Parser.min_long), Parser.normalizeLon(Parser.max_long), null,0,vertices);
        this(MortonCode.max, MortonCode.min, MortonCode.min, MortonCode.max, null,0,vertices);

    }

    public boolean contain(PartitionVertex v){
        //return vertices.contains(v.getId());
        return (v.latitude>=bottom_bound&&v.latitude<=top_bound&&v.longitude>=left_bound&&v.longitude<=right_bound);
    }
    public QuadTree getParent(){
        return parent;
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
    public synchronized void setDiameter(float newDia, int represenID){
        if(newDia>this.diameter){
            this.representativePoint = represenID;
            this.diameter=newDia;
        }
    }
    public boolean testDiameter(){
        if(this.level>=initial_depth){
        if(this.size>1&&this.diameter<=0){
            System.out.println("error");
        }else if(this.size==1&&this.diameter!=0){
            System.out.println("error");
        }
        }
        boolean result=true;
        if(this.vertices!=null&&this.vertices.size()>0){
            result = this.representativePoint>=0;
            if(this.level<max_depth&&this.level>=initial_depth){
                int rep1=-1;
                int rep2=-1;
                int rep3 = -1;
                int rep4 = -1;
                if(NW!=null){
                    rep1 = NW.representativePoint;
                }
                if(NE!=null){
                    rep2 = NE.representativePoint;
                }
                if(SW!=null){
                    rep3 = SW.representativePoint;
                }
                if(SE!=null){
                    rep4 = SE.representativePoint;
                }
                result = rep1==this.representativePoint || rep2 ==this.representativePoint ||rep3 == this.representativePoint ||rep4 == this.representativePoint;
            }
        }
        if(this.level==max_depth&&this.size>1){
            result = false;
        }
        if(this.level<initial_depth){
            result &= true;
        }else{
            if(this.diameter>0&&this.size>0){
                result &= true;
            }else if(this.diameter==0&&this.size<=1){
                result &= true;
            }
            else{
                result &= false;
            }
        }
        if(!result){
            this.printVertices();
            System.out.println("error at diameter");
        }
        if(this.NW!=null){
            result &= NW.testDiameter();
        }
        if(this.NE!=null){
            result &= NE.testDiameter();
        }
        if(this.SW!=null){
            result &= SW.testDiameter();
        }
        if(this.SE!=null){
            result &= SE.testDiameter();
        }
        return result;
    }
    /*public HashSet<Integer> copy(){
        return new HashSet<>(vertices);
    }*/
    public void copy(HashSet<Integer> verSet){
        //base case
        if(this.level==max_depth){
            for(Map.Entry<Integer,PartitionVertex> e:vertices.entrySet()){
                verSet.add(e.getKey());
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
        int quadrant = classifier( v);
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

    public void getAllInitialLevelBlocks (ArrayList<QuadTree> list){
        if(this.level==initial_depth){
            if(this.size>0){
                list.add(this);
            }
            return;
        }
        if(NW!=null){
            NW.getAllInitialLevelBlocks(list);
        }
        if(NE!=null){
            NE.getAllInitialLevelBlocks(list);
        }
        if(SW!=null){
            SW.getAllInitialLevelBlocks(list);
        }
        if(SE!=null){
            SE.getAllInitialLevelBlocks(list);
        }
    }

    public void remove(PartitionVertex v){
        if(vertices.containsKey(v.getId())){
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

    private int classifier (PartitionVertex v) {
        int x = v.longitude;
        int y = v.latitude;
        //assert(x<=top&&x>=bot);
        //assert(y>=left&&y<=right);
        boolean isTop;
        boolean isRight;
        /*   boolean isTop = y>=(hor+1);
        boolean isRight = x>=ver+1;
        if(isTop){
            if(isRight)
                return 2;
            return 1;
        }else{
            if(isRight)
                return 4;
            return 3;
        }*/
        if (y < this.top_bound && y >= this.horizontal) {
            isTop = true;
        } else if (y < this.horizontal && y >= this.bottom_bound) {
            isTop = false;
        } else {
            throw new RuntimeException("error, out of bounds again");
        }
        if (x >= this.left_bound && x < this.vertical) {
            isRight = false;
        } else if (x >= this.vertical && x < this.right_bound) {
            isRight = true;
        } else {
            throw new RuntimeException("error, out of bounds again");
        }
        if (isTop) {
            if (isRight)
                return 2;
            return 1;
        } else {
            if (isRight)
                return 4;
            return 3;

        }
    }

    public void insert(PartitionVertex v){
        this.vertices.put(v.getId(),v);
        if(level<max_depth) {
           /* if(this.vertices.size()==1){
                    NW=new QuadTree(top_bound,horizontal+1,left_bound, vertical, this, level+1, new HashMap <Integer, PartitionVertex>());
                    NE=new QuadTree(top_bound, horizontal+1, vertical+1, right_bound, this, level+1, new HashMap <Integer, PartitionVertex>());
                    SW=new QuadTree(horizontal, bottom_bound, left_bound, vertical, this, level+1, new HashMap <Integer, PartitionVertex>());
                    SE=new QuadTree(horizontal, bottom_bound, vertical+1, right_bound, this, level+1, new HashMap <Integer, PartitionVertex>());
            }*/
            int quadrant = classifier( v);
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
            int quadrant = classifier( v);
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

    /*public HashSet<Integer> getVertices(){
        return vertices;
    }*/

    public static void setMax_depth(int max){
        max_depth=max;
    }

    private MortonCode setMorton(){
        int morton =0;
        int longest_common_prefix =32;
        if(this.level==max_depth){
            if(this.size>0){
                for(Map.Entry<Integer,PartitionVertex>set:vertices.entrySet()) {
                /*    for (Map.Entry<Integer, PartitionVertex> e : vertices.entrySet()) {
                        int result = set.getValue().morton().code ^ e.getValue().morton().code;
                        if (Integer.numberOfLeadingZeros(result) < longest_common_prefix) {
                            longest_common_prefix = Integer.numberOfLeadingZeros(result);
                        }
                    }
                    if(longest_common_prefix%2==1){
                        longest_common_prefix--;
                    }*/
                    //we hope longest common prefix to be 32 here
                    this.mc = new MortonCode(set.getValue().morton(),28,this.level);
                    return this.mc;
                }
            }
            return null;
        }else{
            if(this.size>0){
                int temResult = 0;
                MortonCode mc1=null;
                MortonCode mc2=null;
                MortonCode mc3=null;
                MortonCode mc4=null;
                //ask children to do it
                if(this.NW!=null){
                    if(this.NW.size>0){
                        mc1= this.NW.setMorton();
                        temResult = mc1.code;
                    }
                }
                if(this.NE!=null){
                    if(this.NE.size>0){
                        mc2= this.NE.setMorton();
                        temResult=mc2.code;
                    }
                }
                if(this.SW!=null){
                    if(this.SW.size>0){
                        mc3= this.SW.setMorton();
                        temResult=mc3.code;
                    }
                }
                if(this.SE!=null){
                    if(this.SE.size>0){
                        mc4=  this.SE.setMorton();
                        temResult=mc4.code;
                    }
                }
                if(mc1!=null){
                    int result = temResult^mc1.code;
                    if(Integer.numberOfLeadingZeros(result)<longest_common_prefix){
                        longest_common_prefix = Integer.numberOfLeadingZeros(result);
                    }
                }
                if(mc2!=null){
                    int result = temResult^mc2.code;
                    if(Integer.numberOfLeadingZeros(result)<longest_common_prefix){
                        longest_common_prefix = Integer.numberOfLeadingZeros(result);
                    }
                }
                if(mc3!=null){
                    int result = temResult^mc3.code;
                    if(Integer.numberOfLeadingZeros(result)<longest_common_prefix){
                        longest_common_prefix = Integer.numberOfLeadingZeros(result);
                    }
                }
                if(mc4!=null){
                    int result = temResult^mc4.code;
                    if(Integer.numberOfLeadingZeros(result)<longest_common_prefix){
                        longest_common_prefix = Integer.numberOfLeadingZeros(result);
                    }
                }
                if(longest_common_prefix%2==1){
                    longest_common_prefix--;
                }
                if(longest_common_prefix!=32&&longest_common_prefix!=30){
                    System.out.println("error in morton");
                }
                if(longest_common_prefix==32){
                    longest_common_prefix=30;
                }
                temResult>>>=(32-longest_common_prefix);
                this.mc = new MortonCode(temResult,this.level);
                //theroratically, the longest common prefix should always be 30
                return this.mc;
            }
            return null;
        }
        //mc=new MortonCode(horizontal,vertical,this.level);

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
        return size;
    }



    public void loadDiameter(HashMap<Integer, DiameterRepPointPair> diameterMap){
        if(diameterMap.containsKey(this.id)){
            DiameterRepPointPair pair = diameterMap.get(id);
            this.diameter = pair.diamter;
            this.representativePoint = pair.vertexID;
            //this.diameter = diameterMap.get(id);
        }
        if(NW!=null){
            NW.loadDiameter(diameterMap);
        }
        if(NE!=null){
            NE.loadDiameter(diameterMap);
        }
        if(SW!=null){
            SW.loadDiameter(diameterMap);
        }
        if(SE!=null){
            SE.loadDiameter(diameterMap);
        }
        if(this.size==1){
            this.diameter=0;
        }
    }


    public void output(){
        if(this.size==0){
            return;
        }
        if(this.diameter!=-1) {
            PrecomputationResultDatabase.insert(new DiameterResult(this.id, this.diameter, this.representativePoint));
        }
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

    public boolean testMorton(){
        MortonCode mc1 = new MortonCode(this.top_bound-1,this.right_bound-1,this.level);
        MortonCode mc2 = new MortonCode(this.horizontal,this.vertical,this.level);
        MortonCode mc3 = new MortonCode(this.bottom_bound,this.left_bound,this.level);
        boolean result = mc1.equals(mc2)&&mc2.equals(mc3);
        if(!result){
            mc1.printBit();
            mc2.printBit();
            mc3.printBit();
            Global.debug=true;
        }
        if(this.NE!=null){
            result&=NE.testMorton();
        }
        if(this.NW!=null){
            result&=NW.testMorton();
        }
        if(this.SW!=null){
            result&=SW.testMorton();
        }
        if(this.SE!=null){
            result&=SE.testMorton();
        }
        return result;
    }

    public void printVertices(){
        if(this.level!=max_depth){
            if(NW!=null){
                NW.printVertices();
            }
            if(NE!=null){
                NE.printVertices();
            }
            if(SW!=null){
                SW.printVertices();
            }
            if(SE!=null){
                SE.printVertices();
            }
        }else{
            if(this.size>0){
                for(Map.Entry<Integer, PartitionVertex> set:this.vertices.entrySet()){
                    System.out.println("Vertex ID is "+set.getKey());
                }
            }
        }
    }
}
