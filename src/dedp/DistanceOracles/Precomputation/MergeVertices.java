package dedp.DistanceOracles.Precomputation;

import dedp.DistanceOracles.EdgeLabelProcessor;
import dedp.DistanceOracles.QuadTree;
import dedp.structures.Edge;
import dedp.structures.Vertex;

import java.io.*;
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
        public  int max_depth=16;
        public  int merge_level = 16;
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
    class EdgeInfo{//lightweright class merely used to store edge information
        long from;
        long to;
        float weight;
        int label;
        //boolean isDirected;
    }
    class VertexInfo{
        long id;
        int latitude;
        int longitude;
    }
    public static String fileName = "./Graph_Source/ID.tmp";
    public static String verticesResultName = "./Graph_Source/ID_ver_final.txt";
    public static String edgesResultName = "./Graph_Source/ID_edge_final.txt";
    private int maxLat;
    private int minLat;
    private int maxLon;
    private int minLon;
    private double latRange;
    private double lonRange;
    private int noromalizeLat(int lat){
        double z1 = (double)(lat-minLat);
        z1 = z1/latRange;
        int finalLat = (int)(z1*Math.pow(2.0,16));
        return finalLat;
    }
    private int normalizeLon(int lon){
        double z2 = (double)(lon-minLon);
        z2 = z2/lonRange;
        int finalLon = (int)(z2*Math.pow(2.0,16));
        return finalLon;
    }
    private void normalize( ArrayList<Vertex> vSet){
        maxLat++;
        maxLon++;
        latRange = maxLat-minLat;
        lonRange = maxLon-minLon;
        int finalLat;
        int finalLon;
        Vertex v;
        for(int i=0; i<vSet.size();i++){
            v = vSet.get(i);
            finalLat =noromalizeLat(v.latitude);
            finalLon =normalizeLon(v.longitude) ;
            v.latitude=finalLat;
            v.longitude=finalLon;
        }
        maxLat = noromalizeLat(maxLat);
        maxLon = normalizeLon(maxLon);
        minLat = noromalizeLat(minLat);
        minLon = normalizeLon(minLon);
    }
    public void loadAndMerge() throws IOException {
        File f =new File(fileName);
        //File ver =new File(verticesResultName);
        //File edge = new File(verticesResultName);
        FileWriter vWriter = new FileWriter(verticesResultName);
        FileWriter eWriter = new FileWriter(edgesResultName);
        BufferedReader reader = new BufferedReader(new FileReader(f));
        String line;
        ArrayList<Vertex> vSet = new ArrayList<>();
        maxLat = Integer.MIN_VALUE;
        minLat = Integer.MAX_VALUE;
        maxLon = Integer.MIN_VALUE;
        minLon = Integer.MAX_VALUE;
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
            if(rawLongitude>maxLon){
                maxLon=rawLongitude;
            }
            if(rawLongitude<minLon){
                minLon=rawLongitude;
            }
            Vertex v = new Vertex();
            v.setID(id);
            v.setCoordinates(rawLatitude,rawLongitude);
            vSet.add(v);
            if(id==271449){
                break;
            }
        }
        System.out.println("max lat is "+maxLat);
        System.out.println("min lat is "+minLat);
        System.out.println("max lon is "+maxLon);
        System.out.println("min long is "+minLon);
        //now let's normalize vertices.
        normalize(vSet);
        MiniQuadtree tree = new MiniQuadtree(vSet,maxLat,minLat,minLon, maxLon,0);
        HashMap<Integer,Integer> merge = new HashMap<>();
        tree.mergeVertices(merge);
        //int result = merge.get(129889);
        //System.out.println(result);
        //now we process the edges
        long key=1;
        long fromID=-1, toID=-1;
        float weight;
        int label;
        ArrayList<EdgeInfo>edges = new ArrayList<>();
        boolean flag = false;
        while((line=reader.readLine())!=null){
            String[]fields = line.split("\\s+");
            if(fields.length==1){
                continue;
            }
            if(!flag){
                flag=true;
                fromID=Long.parseLong(fields[0]);
                toID=Long.parseLong(fields[1]);
                if(merge.containsKey((int)fromID)){
                    fromID = merge.get((int)fromID);
                }
                if(merge.containsKey((int)toID)){
                    toID = merge.get((int)toID);
                }
            }else{
                flag=false;
                weight = Float.parseFloat(fields[1]);
                label = Integer.parseInt(fields[2]);
                if(fromID==toID){
                    continue;
                }
                //todo: for test set all labels to 1
                    EdgeInfo e = new EdgeInfo();
                    e.from=fromID;
                    e.to = toID;
                    e.label=label;
                    e.weight = weight;
                    edges.add(e);
                    String edgeLine = String.valueOf(fromID)+","+String.valueOf(toID)+","+String.valueOf(label)+","+String.valueOf(weight)+"\n";
                    eWriter.write(edgeLine);

                key++;
            }
        }
        eWriter.close();
        for(int i=0; i<vSet.size();i++){
            Vertex v = vSet.get(i);
            if(merge.containsKey((int)v.getID())){
                continue;
            }
            String vertexLine = String.valueOf(v.getID())+","+String.valueOf(v.latitude)+","+String.valueOf(v.longitude)+"\n";
            vWriter.write(vertexLine);
        }
        vWriter.close();
        //now we have all the edges stored;
    }
    public static void main(String[]args) throws IOException {
        MergeVertices preprocess = new MergeVertices();
        preprocess.loadAndMerge();
    }
}
