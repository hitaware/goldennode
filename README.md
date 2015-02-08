USAGE

	FIRST PROCESS

	Run these on the first process :

	1) Create a Cluster. You can create a ReplicatedMemoryCluster as below :
      
          Cluster c = ClusterFactory.getCluster();
      
	2) Create a ClustedList as below :
			
	        List<String> clusteredList = new ClusteredList<String>(); 
			
	3) You can use the List above just as any other list implementation.
			
		clusteredList.add("This is the first entry");
		
	4) If you would like to use this list within the cluster. That is, if you want this list to be distributed first set the owner, then set the name and then attach it to the cluster as below :
			
		((ClusteredList<String>) clusteredList).setOwnerId(c.getOwner().getId());
		((ClusteredList<String>) clusteredList).setPublicName("list1");
		c.attachObject((ClusteredObject) clusteredList);
			
			
	5) Do some list operations :

  		for (int i = 0; i < 10; i++) {
  			clusteredList.add(new Integer(i).toString());
  		}
		clusteredList.remove(0);
		System.out.println(clusteredList.size());
		
	6) To make the list local again :
		
		c.detachObject((ClusteredObject) clusteredList);
			
	7) Stop the cluster 
			
		c.stop();
			  
			  
	SECOND PROCESS
	
	Run these on the second process.
	
	1) Create a Cluster. You can create a ReplicatedMemoryCluster as below :
      
		Cluster c = ClusterFactory.getCluster();
     
	2) Load ClusteredObject which is going to be loaded by the first process.

    	ClusteredObject co = c.getClusteredObject("list1");
    					
    3) The object loaded from the previous step is a ClustedList. So Let's cast it and print its size.			
    				
    	System.out.println(((ClusteredList) co).size());
    				
    
	
