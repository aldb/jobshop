package jobshop.encodings;

import jobshop.Instance;
import jobshop.Result;
import jobshop.Schedule;
import jobshop.Solver;
import jobshop.solvers.BasicSolver;
import jobshop.solvers.GreedySolver;

import org.junit.Test;

import java.io.IOException;
import java.nio.file.Paths;

public class EncodingTests {
	
		@Test
    public void testResourceOrder() throws IOException {
        Instance instance = Instance.fromFile(Paths.get("instances/aaa1"));
        
        // build a solution that should be equal to the result of ResourceOrder
        ResourceOrder enc = new ResourceOrder(instance);
        
        enc.tasksByMachine[0][0] = new Task(0,0);
        enc.tasksByMachine[0][1] = new Task(1,1);
        enc.tasksByMachine[1][0] = new Task(1,0);
        enc.tasksByMachine[1][1] = new Task(0,1);
        enc.tasksByMachine[2][0] = new Task(0,2);
        enc.tasksByMachine[2][1] = new Task(1,2);
        
        Schedule sched = enc.toSchedule();
        System.out.println(sched.toString());
        assert sched.isValid();
        assert sched.makespan() == 12;
        System.out.println("ok");
       

    }

    @Test
    public void testJobNumbers() throws IOException {
        Instance instance = Instance.fromFile(Paths.get("instances/aaa1"));

        // numéro de jobs : 1 2 2 1 1 2 (cf exercices)
        JobNumbers enc = new JobNumbers(instance);
        enc.jobs[enc.nextToSet++] = 0;
        enc.jobs[enc.nextToSet++] = 1;
        enc.jobs[enc.nextToSet++] = 1;
        enc.jobs[enc.nextToSet++] = 0;
        enc.jobs[enc.nextToSet++] = 0;
        enc.jobs[enc.nextToSet++] = 1;

        Schedule sched = enc.toSchedule();
        // TODO: make it print something meaningful
        // by implementing the toString() method
        
       // System.out.println(sched.toString());
        assert sched.isValid();
        assert sched.makespan() == 12;



        // numéro de jobs : 1 1 2 2 1 2
        enc = new JobNumbers(instance);
        enc.jobs[enc.nextToSet++] = 0;
        enc.jobs[enc.nextToSet++] = 0;
        enc.jobs[enc.nextToSet++] = 1;
        enc.jobs[enc.nextToSet++] = 1;
        enc.jobs[enc.nextToSet++] = 0;
        enc.jobs[enc.nextToSet++] = 1;

        sched = enc.toSchedule();
        assert sched.isValid();
        assert sched.makespan() == 14;
    }

    @Test
    public void testBasicSolver() throws IOException {
        Instance instance = Instance.fromFile(Paths.get("instances/aaa1"));

        // build a solution that should be equal to the result of BasicSolver
        JobNumbers enc = new JobNumbers(instance);
        enc.jobs[enc.nextToSet++] = 0;
        enc.jobs[enc.nextToSet++] = 1;
        enc.jobs[enc.nextToSet++] = 0;
        enc.jobs[enc.nextToSet++] = 1;
        enc.jobs[enc.nextToSet++] = 0;
        enc.jobs[enc.nextToSet++] = 1;

        Schedule sched = enc.toSchedule();
        assert sched.isValid();
        assert sched.makespan() == 12;

        Solver solver = new BasicSolver();
        Result result = solver.solve(instance, System.currentTimeMillis() + 10);

        assert result.schedule.isValid();
        assert result.schedule.makespan() == sched.makespan(); // should have the same makespan
    }
    
    
    
    @Test
    public void testGreedySolver() throws IOException {
        Instance instance = Instance.fromFile(Paths.get("instances/aaa1"));


        // build a solution to compare to the result of GreedySolver
        ResourceOrder enc = new ResourceOrder(instance);
        enc.tasksByMachine[0][0] = new Task(0,0);
        enc.tasksByMachine[0][1] = new Task(1,1);
        enc.tasksByMachine[1][0] = new Task(1,0);
        enc.tasksByMachine[1][1] = new Task(0,1);
        enc.tasksByMachine[2][0] = new Task(0,2);
        enc.tasksByMachine[2][1] = new Task(1,2);
        Schedule sched = enc.toSchedule();
        assert sched.isValid();
        assert sched.makespan() == 12;
        
       
        
        Solver solver = new GreedySolver(0);
        Result result = solver.solve(instance, System.currentTimeMillis() + 10);
        System.out.println(result.schedule.toString());
        assert result.schedule.isValid();
        System.out.println("SPT:");
        System.out.println("makespan:");
        System.out.println(result.schedule.makespan());
        System.out.println("écart:");
        System.out.println(result.schedule.makespan()-sched.makespan());
    }
    
    @Test
    public void testGreedySolverLRPT() throws IOException {
        Instance instance = Instance.fromFile(Paths.get("instances/aaa1"));

        // build a solution that should be equal to the result of GreedySolver
        ResourceOrder enc = new ResourceOrder(instance);
        
        enc.tasksByMachine[0][0] = new Task(0,0);
        enc.tasksByMachine[0][1] = new Task(1,1);
        enc.tasksByMachine[1][0] = new Task(1,0);
        enc.tasksByMachine[1][1] = new Task(0,1);
        enc.tasksByMachine[2][0] = new Task(0,2);
        enc.tasksByMachine[2][1] = new Task(1,2);
        
        Schedule sched = enc.toSchedule();
        assert sched.isValid();
        assert sched.makespan() == 12;
        
       
        
        Solver solver = new GreedySolver(1);
        Result result = solver.solve(instance, System.currentTimeMillis() + 10);
        System.out.println(result.schedule.toString());
        assert result.schedule.isValid();
        System.out.println("LRPT:");
        System.out.println("makespan:");
        System.out.println(result.schedule.makespan());
        System.out.println("écart:");
        System.out.println(result.schedule.makespan()-sched.makespan());
    }
    


}
