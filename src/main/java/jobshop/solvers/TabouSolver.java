package jobshop.solvers;

import java.util.ArrayList;
import java.util.List;


import jobshop.Instance;
import jobshop.Result;
import jobshop.Schedule;
import jobshop.Solver;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Task;
import jobshop.solvers.DescentSolver.Block;
import jobshop.solvers.DescentSolver.Swap;

public class TabouSolver implements Solver {

	int maxIter;
	int dureeTaboo;
	
	 public TabouSolver(int maxIter, int dureeTaboo) {
	        this.maxIter= maxIter;
	        this.dureeTaboo=dureeTaboo;    
	    }
	 
	 /** A block represents a subsequence of the critical path such that all tasks in it execute on the same machine.
	     * This class identifies a block in a ResourceOrder representation.
	     *
	     * Consider the solution in ResourceOrder representation
	     * machine 0 : (0,1) (1,2) (2,2)
	     * machine 1 : (0,2) (2,1) (1,1)
	     * machine 2 : ...
	     *
	     * The block with : machine = 1, firstTask= 0 and lastTask = 1
	     * Represent the task sequence : [(0,2) (2,1)]
	     *
	     * */
	    static class Block {
	        /** machine on which the block is identified */
	        final int machine;
	        /** index of the first task of the block */
	        final int firstTask;
	        /** index of the last task of the block */
	        final int lastTask;

	        Block(int machine, int firstTask, int lastTask) {
	            this.machine = machine;
	            this.firstTask = firstTask;
	            this.lastTask = lastTask;
	        }
	    }

	    /**
	     * Represents a swap of two tasks on the same machine in a ResourceOrder encoding.
	     *
	     * Consider the solution in ResourceOrder representation
	     * machine 0 : (0,1) (1,2) (2,2)
	     * machine 1 : (0,2) (2,1) (1,1)
	     * machine 2 : ...
	     *
	     * The swam with : machine = 1, t1= 0 and t2 = 1
	     * Represent inversion of the two tasks : (0,2) and (2,1)
	     * Applying this swap on the above resource order should result in the following one :
	     * machine 0 : (0,1) (1,2) (2,2)
	     * machine 1 : (2,1) (0,2) (1,1)
	     * machine 2 : ...
	     */
	    static class Swap {
	        // machine on which to perform the swap
	        final int machine;
	        // index of one task to be swapped
	        final int t1;
	        // index of the other task to be swapped
	        final int t2;

	        Swap(int machine, int t1, int t2) {
	            this.machine = machine;
	            this.t1 = t1;
	            this.t2 = t2;
	        }

	        /** Apply this swap on the given resource order, returning a new solution. */
	        public ResourceOrder applyOn(ResourceOrder origin) {
	        	
	        	ResourceOrder order=origin.copy();
	            Task aux=order.tasksByMachine[this.machine][this.t1]; 
	            order.tasksByMachine[this.machine][this.t1]= order.tasksByMachine[this.machine][this.t2];
	            order.tasksByMachine[this.machine][this.t2]=aux;
	            return order;
	        }
	    }

	    static class Pair{
	    	ResourceOrder order;
	    	Swap swap;
	    	
	    	Pair(ResourceOrder order, Swap swap){
	    		this.order=order;
	    		this.swap=swap; 
	    	}
	    	
	    }
	 
	public Result solve(Instance instance, long deadline) {
		
		int currentIter=0; 
    	//init with GreedySolver
    	Solver solver=new GreedySolver(1);
    	Result result = solver.solve(instance, System.currentTimeMillis() + 10);
    	//the current best solution
    	ResourceOrder bestSolution= new ResourceOrder(result.schedule);
    	//the current solution
    	ResourceOrder curentSolution= new ResourceOrder(result.schedule);
    	//the table of tabou solution 
    	int [][] sTabou = new int [instance.numTasks*instance.numJobs][instance.numTasks*instance.numJobs];
    	//the current neighborhood
    	List<Pair> neighborhood=new ArrayList<Pair>();
    	
		while(deadline - System.currentTimeMillis() > 1 && currentIter<maxIter) {
			currentIter++;	
			
			List<Block> listOfBlock=blocksOfCriticalPath(curentSolution);
    		
    		for(Block block : listOfBlock )
    		{
    			List<Swap> listOfSwap= neighbors(block);
    			
        		for(Swap swap : listOfSwap )
        		{
        			neighborhood.add(new Pair(swap.applyOn(curentSolution),swap));
        		}
    			
    		}
    		
    		int minMakespan= Integer.MAX_VALUE;
    		Pair bestNeighbor= null;
    		
    		for(Pair n: neighborhood)
    		{
    			Schedule sch= n.order.toSchedule();
    			if(sch!=null) 
    			{
    				int localMakespan=sch.makespan();
        			int kSwap=sTabou[n.swap.machine*instance.numMachines+n.swap.t1][n.swap.machine*instance.numMachines+n.swap.t2];
        			if(localMakespan<minMakespan & currentIter>=kSwap )
        			{
        				minMakespan=localMakespan;
        				bestNeighbor=n;
        			}
    			}
    			
    		}
    		
    		if (bestNeighbor!= null)
    		{
        		sTabou[bestNeighbor.swap.machine*instance.numMachines+bestNeighbor.swap.t1][bestNeighbor.swap.machine*instance.numMachines+bestNeighbor.swap.t2]=currentIter+dureeTaboo;
        		curentSolution= bestNeighbor.order;
        		if (bestNeighbor.order.toSchedule().makespan()<bestSolution.toSchedule().makespan())
        		{
        			bestSolution=bestNeighbor.order;
        		}
    		}

    		
		}
		
		return new Result(instance, bestSolution.toSchedule(), Result.ExitCause.Blocked);
	}
	
	
	 /** Returns a list of all blocks of the critical path. */
    List<Block> blocksOfCriticalPath(ResourceOrder order) {
    	List<Block> list_block= new ArrayList<Block>();
    	List<Task> list_task= order.toSchedule().criticalPath();
    	
    	for (int m=0; m<order.instance.numMachines; m++)
    	{
    		int t1=0;
           	int t2=0;
           	
       		for (int j=0; j<order.instance.numJobs; j++)
        	{
       			
           		if (list_task.contains(order.tasksByMachine[m][j]))
           		{
           			
           			t2=j;
           			
           			
           			
           		}
           		else 
           		{	
           			if ((t2-t1)>0) 
           			{
	           			
	           			list_block.add(new Block(m,t1,t2));
	           			t1=j+1;
	           			t2=j+1;
	           			
           			}
           			else 
           			{
	           			t1=j+1;
	           			t2=j+1;
	           			
           			}
           		}
           		
           		
        	}
       		
       		if (t2-t1>0)
       		{	;
       			list_block.add(new Block(m,t1,t2));
       		}
    	}
    	
    	return list_block;
    	
    }

    /** For a given block, return the possible swaps for the Nowicki and Smutnicki neighborhood */
    List<Swap> neighbors(Block block) {
    	 List<Swap> list= new ArrayList<Swap>();
    	 if (1==(block.lastTask-block.firstTask) )
     	 {
    		 list.add(new Swap(block.machine,block.firstTask,block.lastTask));
     	 }
    	 else 
    	 {
    		 list.add(new Swap(block.machine,block.firstTask,block.firstTask+1));
    		 list.add(new Swap(block.machine,block.lastTask,block.lastTask-1));
    	 }
    	 
    	 return list;
    }



}
