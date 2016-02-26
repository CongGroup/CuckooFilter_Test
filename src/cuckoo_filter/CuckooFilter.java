package cuckoo_filter;

import java.util.Random;

public class CuckooFilter
{
    // Default constructor
    public CuckooFilter()
    
    {
        m_numOfBuckets      = 1000000;
        m_bucketSize        = 4;
        //m_fingerprintSize 	= 12;
        m_maxRepTrial       = 500;
        
        m_buckets           = new byte[m_numOfBuckets][m_bucketSize];
        m_numOfItems       	= 0;
        
    };
    
    // Normal constructor
    public CuckooFilter(int numOfBuckets, int bucketSize, int fingerprintSize, int maxRepTrial)
    {
        m_numOfBuckets    	= numOfBuckets;
        m_bucketSize        = bucketSize;
        //m_fingerprintSize 	= fingerprintSize;
        m_maxRepTrial       = maxRepTrial;
        
        m_buckets           = new byte[m_numOfBuckets][m_bucketSize];
        m_numOfItems      	= 0;
    };
    
    
    /** Cuckoo filter common API
     *  insert : to insert an item to the filter
     *  contain: to check whether an item exists in the filter
     *  delete : to delete an item from the filter
     */
    public boolean insert(String item)
    {
        byte[] hv = HashUtil.rawSHA256(item);
        int firstIdx = indexByHash(hv);
        int altIdx = -1;
        byte fingerprint = fingerprintByHash(hv);
        int insertPos = -1;
        
        // Check fist bucket
        if( (insertPos=freeBucketPos(firstIdx)) != -1 )
        {
        	insertToBucket(firstIdx, insertPos, fingerprint);
        	return true;
        }
        // Check alternative bucket
        else if( (insertPos=freeBucketPos(altIdx=altIndex(firstIdx,fingerprint))) != -1 )
        {
        	insertToBucket(altIdx, insertPos, fingerprint);
            return true;
        }
        // Relocating existing items
        else
        {
        	int currentIndex = Math.random()<0.5 ? firstIdx : altIdx;
        	byte currentFP = fingerprint;
        	Random generator = new Random();
        	
	        for(int i=0; i<m_maxRepTrial; ++i)
	        {
	        	int victimPos = generator.nextInt(m_bucketSize);
	            byte victimFP = m_buckets[currentIndex][victimPos];
	            
	            // Replace victim fingerprint with fingerprint that is currently to insert
	            m_buckets[currentIndex][victimPos] = currentFP;
	            
	            // Try alternative for the victim
	            int victimAltIdx = (currentIndex ^ indexByHash(HashUtil.rawSHA256(victimFP)))%m_numOfBuckets;
	            // Alternative bucket available 
	            if((insertPos=freeBucketPos(victimAltIdx)) != -1)
        		{
	            	insertToBucket(victimAltIdx, insertPos, victimFP);
	            	return true;
        		}
	            // Not available
	            else
	            {
	            	currentIndex = victimAltIdx;
	            	currentFP = victimFP;
	            }
	        }
	        
	        // Reach maximum allowed trials, fail to insert the item
	        System.out.println("Fail to insert item " + item + " ! Maximum trials reached and no space available!");
	        return false;
        }
    };
    
    public boolean contain(String item)
    {
    	byte[] hv = HashUtil.rawSHA256(item);
    	int firstIdx = indexByHash(hv);
    	byte fingerprint = fingerprintByHash(hv);
    	
    	return fpPosInBucket(firstIdx, fingerprint)!=-1 || 
    		   fpPosInBucket(altIndex(firstIdx,fingerprint), fingerprint)!=-1;
    };
    
    // NOTICE: deletion of inexisting item may lead to false negative
    public boolean delete(String item)
    {
    	byte[] hv = HashUtil.rawSHA256(item);
    	int firstIdx = indexByHash(hv);
    	byte fingerprint = fingerprintByHash(hv);
    	int deletePos = -1;
    	
    	if( (deletePos=fpPosInBucket(firstIdx, fingerprint)) != -1 )
    	{
    		m_buckets[firstIdx][deletePos] = (byte)0;
    		--m_numOfItems;
    		return true;
    	}
    	else
    	{
    		int altIdx = altIndex(firstIdx, fingerprint);
    		if( (deletePos=fpPosInBucket(altIdx, fingerprint)) != -1 )
    		{
    			m_buckets[altIdx][deletePos] = (byte)0;
    			--m_numOfItems;
    			return true;
    		}
    		else
    		{
    			System.out.println("Fail to delete inexisting item!");
    			return false;
			}
    	}
    };
    
    
    /** Cuckoo filter info and statistics
     * 
     */
    public int numOfItems()
    {
    	return m_numOfItems;
    }
    
    public double loadFactor()
    {
    	return m_numOfItems*1.0/(m_numOfBuckets*m_bucketSize);
    }
    
    
    /** Helper functions
     * 
     */
    // get a positive index from raw hash byte array
    private int indexByHash(byte[] hash)
    {
    	// use lower 31 bits for that Java array supports up to 2^31(Integer.MAX_VALUE) size
    	int index = (hash[hash.length-4] & 0x7f) << 24;
        index |= (hash[hash.length-3] & 0xff) << 16;
        index |= (hash[hash.length-2] & 0xff) << 8;
        index |= (hash[hash.length-1] & 0xff) ;
        
        if(index < 0)
        	System.out.println("what the heck? index " + index + " hash " + (hash[hash.length-4]) + " " + (hash[hash.length-4]&0x7f));
        if(index%m_numOfBuckets < 0)
        	System.out.println("what the fuck?");
        return index%m_numOfBuckets;
    };
    
    // get a 1-byte fingerprint from raw hash byte array
    private byte fingerprintByHash(byte[] hash)
    {
    	// use the first byte
        return hash[0];
    }
    	
    // calculate alternative index for a fingerprint
    private int altIndex(int firstIndex, byte fingerprint)
    {
    	// the returned value is always positive since firstIndex and indexByHash are both positive
    	int altIdx = firstIndex ^ indexByHash(HashUtil.rawSHA256(fingerprint));
        return altIdx%m_numOfBuckets;
    }
    
    // insert (fingerprint of) item to specific position in specific bucket
    private void insertToBucket(int bucketIndex, int inBucketPos, byte fingerprint)
    {
    	m_buckets[bucketIndex][inBucketPos] = fingerprint;
    	m_numOfItems++;
    }
    
    // return position of fingerprint in a bucket, if no fingerprint found -1 is returned
    private int fpPosInBucket(int bucketIndex, byte fingerprint)
    {
    	for(int i=0; i<m_bucketSize; ++i)
            if(m_buckets[bucketIndex][i] == fingerprint)
                return i;
        return -1;
    }
    
 	// 0 is assumed to be the default "empty" fingerprint value
    private int freeBucketPos(int bucketIndex)
    {
    	return fpPosInBucket(bucketIndex, (byte)0);
    }
    
    /**
     *  Underlying data structures
     */
    private int          m_numOfBuckets;
    private int          m_bucketSize;
    //private int          m_fingerprintSize;
    private int          m_maxRepTrial;
    
    // Currently assume the fingerprint size to be fixed as 16 bit
    private byte[][]     m_buckets;
    private int          m_numOfItems;
}
