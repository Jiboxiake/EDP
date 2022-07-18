package dedp.DistanceOracles;

import java.util.BitSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SearchKey implements Comparable<SearchKey> {
    public long mc;
    public short level;//to save space
    public SearchKey(MortonCode mc1, MortonCode mc2){
        int temp1 = mc1.code;
        int temp2 = mc2.code;
        long code1 = (long)temp1;
        long code2 = (long)temp2;
        code1<<=32;
        code1>>>=32;
        code2<<=32;
        code2>>>=32;
        if(code1> code2){
            mc = MortonCode.get4DMorton(code1,code2,(int)mc1.level);
            //System.out.println(1);
        }else if(code1< code2){
            mc = MortonCode.get4DMorton(code2,code1,(int)mc1.level);
            //System.out.println(1);
        }else{
            System.out.println("error");
        }
        this.level = mc1.level;
    }
    public SearchKey(SearchKey key){
        this.mc=key.mc;
        this.level=key.level;
    }
    public SearchKey (long key, short level){
        this.mc = key;
        this.level = level;
    }
    @Override
    public int hashCode(){
        return Long.hashCode(this.mc)+Short.hashCode(level);
    }
    @Override
    public int compareTo(SearchKey k){
        if(this.mc>k.mc){
            return 1;
        }else if(this.mc<k.mc){
            return -1;
        }else{
            if(this.level==k.level){
                return 0;
            }else if(this.level>k.level){
                return 1;
            }else{
                return -1;
            }
        }
    }
    public void printBit(){
        System.out.println(Long.toBinaryString(this.mc)+" level is "+(int)this.level);
    }
    @Override
    public boolean equals(Object o){
        if(this.mc==((SearchKey)o).mc&&this.level==((SearchKey)o).level){
            return true;
        }
        return false;
    }

    public boolean shift(){
        if(level<=0){
            return false;
        }else{
            this.mc>>>=4;
            this.level--;
            return true;
        }
    }
}

