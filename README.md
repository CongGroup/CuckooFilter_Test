# CuckooFilter_Test
A simple yet standard cuckoo filter implemented in Java

/* Constructors */  
CuckooFilter()  
CuckooFilter(int numOfBuckets, int bucketSize, int fingerprintSize, int maxRepTrial) 


/* Cuckoo filter core functions */  
// To insert an item, return true on successful insertion  
boolean insert(String item)  

// To check the existence of an item, return true on its existence  
boolean contain(String item)

// To delete an item, return true on successful deletion  
// WARNING: deletion of item that is not previously inserted may lead to false negative  
boolean delete(String item)

/* Stats & Info */  
int numOfItems()  
double loadFactor()  

Note that this is a toy version with following constraints:  
1. fingerprint size is currently hard-coded to 8-bit for the efficient use of primitive data type, arbitrary fingerprint length (at the cost of efficiency) will be supported later  
2. item type is now restricted to String, generic item type will be supported later


Feel free to leave comments/suggestions, enjoy it:wink::wink::wink:
