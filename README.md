USAGE OF GOLDEN NODE

	FIRST PROCESS

	Run these on the first process :

	1) Create a Grid.
      
		Grid c = GridFactory.getGrid();
      
	2) Create a ReplicatedMemoryList as below :
			
		List<String> replicatedMemoryList = new ReplicatedMemoryList<String>(); 
			
	3) You can use the List above just as any other list implementation.
			
		replicatedMemoryList.add("This is the first entry");
		
	//4) If you would like to use this list within the grid. That is, if you want this list to be distributed first set //the owner, then set the name and then attach it to the grid as below :
			
		((ReplicatedMemoryList<String>) replicatedMemoryList).setOwnerId(c.getOwner().getId());
		((ReplicatedMemoryList<String>) replicatedMemoryList).setPublicName("list1");
		//c.attachObject((DistributedObject) replicatedMemoryList);
			
			
	5) Do some list operations :

		for (int i = 0; i < 10; i++) {
			replicatedMemoryList.add(new Integer(i).toString());
		}
		replicatedMemoryList.remove(0);
		System.out.println(replicatedMemoryList.size());

			
	7) Stop the grid 
			
		c.stop();
			  
			  
	SECOND PROCESS
	
	Run these on the second process.
	
	1) Create a Grid.
      
		Grid c = GridFactory.getGrid();
     
	2) Load DistributedObject which is going to be loaded by the first process.

		DistributedObject co = c.getDistributedObject("list1");
    					
	3) The object loaded from the previous step is a ClustedList. So, let's cast it and print its size.			
		System.out.println(((ReplicatedMememoryList) co).size());
    				
    
	
