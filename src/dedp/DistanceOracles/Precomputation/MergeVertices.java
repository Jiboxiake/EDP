package dedp.DistanceOracles.Precomputation;

import dedp.DistanceOracles.QuadTree;
import dedp.structures.Vertex;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class MergeVertices {
    class MiniQuadtree{//a quadtree with fewer functions
        public int top;
        public int bottom;
        public int left;
        public int right;
        public int level;
        public int horizontal;
        public int vertical;
        public MiniQuadtree NW;
        public MiniQuadtree NE;
        public MiniQuadtree SW;
        public MiniQuadtree SE;
        public ArrayList<Vertex> vertices;
        public static int max_depth=16;
        public static int merge_level = 16;
        public MiniQuadtree(ArrayList<Vertex> vertices, int top, int bottom, int left, int right,int level){
            this.level = level;
            this.top = top;
            this.bottom=bottom;
            this.left=left;
            this.right=right;
            this.horizontal = (top-bottom)/2+bottom;
            this.vertical = (right-left)/2+left;
            ArrayList<Vertex> TL = new ArrayList<>();
            ArrayList<Vertex> TR = new ArrayList<>();
            ArrayList<Vertex> BL = new ArrayList<>();
            ArrayList<Vertex> BR = new ArrayList<>();
            this.vertices=new ArrayList<>();
            Vertex v = null;
            for(int i=0; i<vertices.size();i++){
                v = vertices.get(i);
                if(this.level==max_depth){
                    this.vertices.add(v);
                }else{
                    this.vertices=null;
                }
                int quadrant = classifier(v);
                if(quadrant==1){
                    TL.add(v);
                }else if(quadrant==2){
                    TR.add(v);
                }else if(quadrant==3){
                    BL.add(v);
                }else{
                    assert(quadrant==4);
                    BR.add(v);
                }
            }
            if(level<max_depth && vertices.size()>0){
                NW = new MiniQuadtree(TL,top,horizontal,left,vertical,level+1);
                NE = new MiniQuadtree(TR,top,horizontal,vertical,right,level+1);
                SW = new MiniQuadtree(BL,horizontal,bottom,left,vertical,level+1);
                SE=new MiniQuadtree(BR, horizontal, bottom, vertical, right,level+1);
            }else{
                NW = null;
                NE = null;
                SW = null;
                SE = null;
            }
        }
        public void mergeVertices(HashMap<Integer,Integer>map){
            if(this.level<merge_level){
                if(NE!=null){
                    NE.mergeVertices(map);
                }
                if(NW!=null){
                    NW.mergeVertices(map);
                }
                if(SW!=null){
                    SW.mergeVertices(map);
                }
                if(SE!=null){
                    SE.mergeVertices(map);
                }
                return;
            }else{
                //so now we do the merge, but first we care about level 16
                if(max_depth==merge_level){
                    if(this.vertices!=null&&this.vertices.size()>=2){
                        Vertex leader = this.vertices.get(0);
                        for(int i=1; i<this.vertices.size();i++){
                            Vertex member = this.vertices.get(i);
                            map.put((int)member.getID(),(int)leader.getID());
                        }
                    }
                }
            }
        }
        private int classifier(Vertex v){
            int x = v.longitude;
            int y = v.latitude;
            boolean isTop;
            boolean isRight;
            if (y < this.top && y >= this.horizontal) {
                isTop = true;
            } else if (y < this.horizontal && y >= this.bottom) {
                isTop = false;
            } else {
                throw new RuntimeException("error, out of bounds again");
            }
            if (x >= this.left && x < this.vertical) {
                isRight = false;
            } else if (x >= this.vertical && x < this.right) {
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
    }
    public static String fileName = "./Graph_Source/ID.tmp";
    public static String resultName = "./Graph_Source/ID_final.txt";
    public void loadAndMerge() throws IOException {
        File f =new File(fileName);
        BufferedReader reader = new BufferedReader(new FileReader(f));
        String line;
        ArrayList<Vertex> vSet = new ArrayList<>();
        int maxLat = Integer.MIN_VALUE;
        int minLat = Integer.MAX_VALUE;
        int maxLong = Integer.MIN_VALUE;
        int minLong = Integer.MAX_VALUE;
        while((line=reader.readLine())!=null){
            String[]fields = line.split("\\s+");
            if(fields.length==1){
                continue;
            }
            int id =Integer.parseInt(fields[0]);
            int rawLongitude = Math.abs(Integer.parseInt(fields[1]));
            int rawLatitude = Math.abs(Integer.parseInt(fields[2]));
            if(rawLatitude>maxLat){
                maxLat=rawLatitude;
            }
            if(rawLatitude<minLat){
                minLat = rawLatitude;
            }
            if(rawLongitude>maxLong){
                maxLong=rawLongitude;
            }
            if(rawLongitude<minLong){
                minLong=rawLongitude;
            }
            Vertex v = new Vertex();
            v.setID(id);
            v.setCoordinates(rawLatitude,rawLongitude);
            vSet.add(v);
            if(id==271449){
                break;
            }
        }

    }
}
