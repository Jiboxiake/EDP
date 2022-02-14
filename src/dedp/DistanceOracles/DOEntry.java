package dedp.DistanceOracles;

public class DOEntry {
    public SearchKey key;
    public float distance;

    public DOEntry(SearchKey key, float distance){
        this.key=key;
        this.distance=distance;
    }
}
