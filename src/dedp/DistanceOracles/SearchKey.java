package dedp.DistanceOracles;

import java.util.BitSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SearchKey {
    private BitSet key;
    private int level;

    public SearchKey(MortonCode m1, MortonCode m2, int level){
        key=new BitSet(128);
        this.level=level;
        if(m1.compareTo(m2)>0) {
            interleave(m1, m2);
        }else if (m1.compareTo(m2)<0){
            interleave(m2,m1);
        }else{
            //System.out.println("error at "+m1.morton);
            interleave(m1, m2);
        }

    }

    public SearchKey(SearchKey copy){
        this.level=copy.level;
        this.key = new BitSet(128);
        for(int i=0; i<127;i++){
            if(copy.key.get(i)){
                this.key.set(i);
            }
        }
    }

    public SearchKey(MortonCode m1, MortonCode m2){
        key=new BitSet(128);
        this.level=32;
        if(m1.compareTo(m2)>0) {
            interleave(m1, m2);
        }else if (m1.compareTo(m2)<0){
            interleave(m2,m1);
        }else{
            //System.out.println("error at "+m1.morton);
            interleave(m1, m2);
        }
    }
    //TODO: check correctness here
    public void interleave(MortonCode m1, MortonCode m2){
        int turn=0;
        long m1copy = m1.morton;
        long m2copy = m2.morton;
        int ignore = 127-4*level;
   /*     int ignore = 127-4*level;
        for(int i=0; i<128; i++){
            assert(turn==i);
            boolean set = false;
            if(turn%2==0){
                long check = m1copy&1;
                if(check==1&&turn>ignore){
                    set=true;
                }
                m1copy= m1copy>>1;
            }else{
                long check = m2copy&1;
                if(check==1&&turn>ignore){
                    set=true;
                }
                m2copy= m2copy>>1;
            }
            if(set)
                key.set(i);
            turn++;
        }*/
        for(int i=0; i<128; i++){
            boolean set = false;
            if(turn%2==0){
                if((m1copy&1)==1){
                    set=true;
                }
                m1copy=m1copy>>1;
            }else{
                if((m2copy&1)==1){
                    set=true;
                }
                m2copy=m2copy>>1;
            }
            if(set){
                //if(i>ignore)
                    key.set(i);
            }
            turn++;
        }
    }
    @Override
    public int hashCode(){
        return key.hashCode();
    }
    //todo: check if we need to have exact equal
    @Override
    public boolean equals(Object o){
      /*  BitSet k1 = (BitSet) (((SearchKey)o).key.clone());
        BitSet k2 = (BitSet) (((SearchKey)o).key.clone());
        k1.and(key);
        return (k1.equals(k2)||k1.equals(key));*/
        return (((SearchKey)o).key.equals(this.key));
    }
    public void shift(){
        int difference = 32-level;
        int starting_index=difference*4;
        for(int i=0; i<4; i++){
            key.clear(starting_index);
            starting_index++;
        }
        level--;
    }
    public void printBit(){
        StringBuilder s = new StringBuilder();
        for( int i = 0; i < key.length();  i++ )
        {
            s.append( key.get( i ) == true ? 1: 0 );
        }

        System.out.println( s.reverse()+" level is "+this.level );
    }
}
