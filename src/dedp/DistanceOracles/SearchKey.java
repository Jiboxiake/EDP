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
        interleave(m1,m2);

    }

    public void interleave(MortonCode m1, MortonCode m2){
        int turn=0;
        long m1copy = m1.morton;
        long m2copy = m2.morton;
        for(int i=0; i<128; i++){
            boolean set = false;
            if(turn%2==0){
                long check = m1copy&1;
                if(check==1){
                    set=true;
                }
                m1copy= m1copy>>1;
            }else{
                long check = m2copy&1;
                if(check==1){
                    set=true;
                }
                m2copy= m2copy>>1;
            }
            if(set)
                key.set(i);
            turn++;
        }
    }
    @Override
    public int hashCode(){
        return key.hashCode();
    }
    @Override
    public boolean equals(Object o){
        BitSet k1 = (BitSet) (((SearchKey)o).key.clone());
        BitSet k2 = (BitSet) (((SearchKey)o).key.clone());
        k1.and(key);
        return (k1.equals(k2)||k1.equals(key));
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

        System.out.println( s.reverse() );
    }
}
