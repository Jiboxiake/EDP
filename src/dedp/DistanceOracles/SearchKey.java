package dedp.DistanceOracles;

import java.util.BitSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SearchKey implements Comparable<SearchKey> {
    public long mc;
    public char level;//to save space
    public SearchKey(MortonCode mc1, MortonCode mc2){
        if(mc1.code> mc2.code){
            mc = MortonCode.get4DMorton(mc1.code,mc2.code,(int)mc1.level);
        }else if(mc1.code< mc2.code){
            mc = MortonCode.get4DMorton(mc2.code,mc1.code,(int)mc1.level);
        }else{
            System.out.println("error");
        }
        this.level = mc1.level;
    }
    public SearchKey(SearchKey key){
        this.mc=key.mc;
        this.level=key.level;
    }
    @Override
    public int hashCode(){
        return Long.hashCode(this.mc)+Character.hashCode(level);
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

