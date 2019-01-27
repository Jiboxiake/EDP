package dedp.common;

import java.util.HashMap;
import java.util.Map;

public class BytesValue 
{

	public byte[] value;
	
	public BytesValue(byte[] bytes)
	{
		this.value = bytes;
	}
	
	
	@Override
    public int hashCode() 
	{
        return HashCode.hash(123456, this.value);
    }
 
    @Override
    public boolean equals(Object object) 
    {
    	boolean isEqual = true;
    	byte[] obj = ((BytesValue)object).value;
        if (this.value == obj)
        {
            isEqual = true;
        }
        else if (obj == null)
        {
            isEqual = false;
        }
        else if (this.value.length != obj.length)
        {
            isEqual = false;
        }
        else
        {
        	for(int i = 0; i < this.value.length; i++)
        	{
        		if(this.value[i] != obj[i])
        		{
        			isEqual = false;
        			break;
        		}
        	}
        }
        return isEqual;
    }
	
	public static void main(String[] args) 
	{
		Map<BytesValue, Float> distanceMap = new HashMap<BytesValue, Float>();
		byte[] b1 = new byte[]{1, 2};
		byte[] b2 = new byte[]{2, 1};
		BytesValue v1 = new BytesValue(b1);
		BytesValue v2 = new BytesValue(b2);
		distanceMap.put(v2, 57f);
		distanceMap.put(v1, 56f);
		System.out.println(distanceMap.get(new BytesValue(new byte[] {1, 3})));
		//System.out.println(v1.equals(v2));
	}

}
