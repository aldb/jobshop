package jobshop.solvers;
import java.util.Arrays;

import jobshop.Instance;
import jobshop.Result;
import jobshop.Schedule;
import jobshop.Solver;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Task;

public class GreedySolver implements Solver {
	
	int SPT=0;
	int LRPT=1;
	int EST_SPT=2;
	int EST_LRPT=3;
	int heuristique;
	
    public GreedySolver(int h) {
        this.heuristique= h;
        
    }
    public int remaningTime(Instance instance, int job, int task) {
    	int time=0;
    	for(int i=task ; i<instance.numTasks ; i++) 
    	{
    		time= time+instance.duration(job,i);
    	}
    	return time;
    }
    

    public Result solve(Instance instance, long deadline) {
    	int [] job_start=  new int[instance.numJobs];
    	int [] machine_start= new int[instance.numMachines];
    	
    	//ma solution que je doit remplir
        ResourceOrder sol = new ResourceOrder (instance);
        int[] nextByMachine = new int[instance.numMachines];
        //Je doit completer tasksByMachine[machine1][0]=tache 1 de la machine 1
        // pour ça je creer une liste de têche que je répartirais ensuite dans la matrice
        Task[] Realisable= new Task[instance.numJobs];
        int[] min_start= new int[instance.numJobs];
        int nbtask=0;
        //initialisation des tâche réalisable
		for(int j=0 ; j<instance.numJobs; j++) 
        {
			Realisable[j]=new Task(j,0);
			min_start[j]=-1;
			job_start[j]=0;
        }
		for(int j=0 ; j<instance.numMachines; j++) 
        {
			machine_start[j]=0;
        }
		
		
        while(nbtask<(instance.numTasks*instance.numJobs))
        {
	        //Parcours de la liste des tâche Réalisable
        	int min=Integer.MAX_VALUE;
        	int tp_min=Integer.MAX_VALUE;
        	int max=0; 
			Task task = null;
			int indice = 0; 
			
			
    		if (heuristique==EST_SPT||heuristique==EST_LRPT)
    		{
    			
    			int min_start_next=0; 
    			
    			
    			//Remplir min_start avec les tâches au plus tôt
    			//System.out.println(Realisable.length);
    			for(int i=0 ; i<Realisable.length ; i++) 
    	        {
    				Task Aux=Realisable[i];
    				int start_time = 0;
    				
    				if (Aux!=null)
    					{
    					start_time=Math.max(job_start[Aux.job],machine_start[instance.machine(Aux)]) ;
    					}
    				if (Aux!=null && tp_min>start_time)
		    		{
		    			
		    			tp_min=start_time;
		    			//System.out.println("new tp min:"+ tp_min);
		    			min_start[0]=i;
		    			min_start_next=1;
		    		} 
    				else if (Aux!=null && tp_min==start_time)
    				{
    					min_start[min_start_next]=i;
    					min_start_next++;
    				}
    	        }
    			
    			//System.out.println(Realisable[min_start[0]]+" "+Realisable[min_start[1]]+" "+" indice next"+min_start_next );
    			
    			
    			//déterminer la tâche a réaliser 
    			
    			if (heuristique==EST_SPT) 
    			{
    				for(int i=0 ; i<min_start_next ; i++) 
    		        {
    					Task Aux=Realisable[min_start[i]];
    					
    					if (Aux!=null && min>instance.duration(Aux))
    		    		{
    		    			min=instance.duration(Aux);
    		    			task=Aux;
    		    			indice=min_start[i];
    		    		} 
    		        }
    			}
    			
    			else if (heuristique==EST_LRPT)
	    		{
    				for(int i=0 ; i<min_start_next ; i++) 
			        {
    					Task Aux=Realisable[min_start[i]];
	    				if (Aux!=null && max<remaningTime(instance, Aux.job, Aux.task))
			    		{
			    			max=remaningTime(instance, Aux.job, Aux.task);
			    			task=Aux;
			    			indice=min_start[i];
			    		}
			        }
    			}
    			
    		}
    		
    		else if (heuristique==LRPT || heuristique==SPT)
    		{
    			for(int i=0 ; i<Realisable.length ; i++) 
    	        {
    	        	
    	    		Task Aux=Realisable[i];

    	    		
    	    		if (heuristique==SPT)
    	    		{
    	    			if (Aux!=null && min>instance.duration(Aux))
    		    		{
    		    			
    		    			min=instance.duration(Aux);
    		    			task=Aux;
    		    			indice=i;
    		    		} 
    	    		}
    	    		
    	    		if (heuristique==LRPT)
    	    		{
    	    			if (Aux!=null && max<remaningTime(instance, Aux.job, Aux.task))
    		    		{
    		    			max=remaningTime(instance, Aux.job, Aux.task);
    		    			task=Aux;
    		    			indice=i;
    		    		} 
    	    		}
    			
    		}
	        
	    		
	    		
	    		
	    		
	        }
	        
	        int m=instance.machine(task);
        	sol.tasksByMachine[m][nextByMachine[m]]=task;
        	nextByMachine[m]++;
	        nbtask++;
	        job_start[task.job]=job_start[task.job]+instance.duration(task);
	        machine_start[m]=machine_start[m]+instance.duration(task);
	        
	        
	        
	        if (task.task+1<instance.numTasks) 
	        {
	        	
	        	Realisable[indice]=new Task(task.job,task.task+1) ;
	        }
	        else 
	        {
	        	
	        	Realisable[indice]=null;
	        }
	        
        }
       
        return new Result(instance, sol.toSchedule(), Result.ExitCause.Blocked);
 
    }
    

	
   
     
}


