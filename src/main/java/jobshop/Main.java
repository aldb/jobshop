package jobshop;

import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


import jobshop.solvers.*;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;


public class Main {

	
    /** All solvers available in this program */
    private static HashMap<String, Solver> solvers;
    static {
        solvers = new HashMap<>();
        solvers.put("basic", new BasicSolver());
        solvers.put("random", new RandomSolver());
        solvers.put("SPT", new GreedySolver(0));
        solvers.put("LRPT", new GreedySolver(1));
        solvers.put("EST_SPT", new GreedySolver(2));
        solvers.put("EST_LRPT", new GreedySolver(3));
        solvers.put("descent", new DescentSolver());
        solvers.put("Tabou(10,10)", new TabouSolver(10,10));
        solvers.put("Tabou(20,5)", new TabouSolver(20,5));
        solvers.put("Tabou(30,30)", new TabouSolver(30,30));
        solvers.put("Tabou(40,40)", new TabouSolver(40,40));
        solvers.put("Tabou(50,50)", new TabouSolver(50,50));
        solvers.put("Tabou(20,1)", new TabouSolver(20,1));
        solvers.put("Tabou(50,5)", new TabouSolver(50,5));
        solvers.put("Tabou(50,10)", new TabouSolver(50,10));
        solvers.put("Tabou(50,15)", new TabouSolver(50,15));
        solvers.put("Tabou(50,20)", new TabouSolver(50,20));
        solvers.put("Tabou(50,25)", new TabouSolver(50,25));
        solvers.put("Tabou(50,30)", new TabouSolver(50,30));
        solvers.put("Tabou(50,40)", new TabouSolver(50,40));
        solvers.put("Tabou(50,50)", new TabouSolver(50,50));
        

    }


    public static void main(String[] args) {
        ArgumentParser parser = ArgumentParsers.newFor("jsp-solver").build()
                .defaultHelp(true)
                .description("Solves jobshop problems.");

        parser.addArgument("-t", "--timeout")
                .setDefault(1L)
                .type(Long.class)
                .help("Solver timeout in seconds for each instance");
        parser.addArgument("--solver")
                .nargs("+")
                .required(true)
                .help("Solver(s) to use (space separated if more than one)");

        parser.addArgument("--instance")
                .nargs("+")
                .required(true)
                .help("Instance(s) to solve (space separated if more than one)");

        Namespace ns = null;
        try {
            ns = parser.parseArgs(args);
        } catch (ArgumentParserException e) {
            parser.handleError(e);
            System.exit(1);
        }

        PrintStream output = System.out;

        long solveTimeMs = ns.getLong("timeout") * 1000;

        List<String> solversToTest = ns.getList("solver");
        for(String solverName : solversToTest) {
            if(!solvers.containsKey(solverName)) {
                System.err.println("ERROR: Solver \"" + solverName + "\" is not avalaible.");
                System.err.println("       Available solvers: " + solvers.keySet().toString());
                System.err.println("       You can provide your own solvers by adding them to the `Main.solvers` HashMap.");
                System.exit(1);
            }
        }
        List<String> instancePrefixes = ns.getList("instance");
        List<String> instances = new ArrayList<>();
        for(String instancePrefix : instancePrefixes) {
            List<String> matches = BestKnownResult.instancesMatching(instancePrefix);
            if(matches.isEmpty()) {
                System.err.println("ERROR: instance prefix \"" + instancePrefix + "\" does not match any instance.");
                System.err.println("       available instances: " + Arrays.toString(BestKnownResult.instances));
                System.exit(1);
            }
            instances.addAll(matches);
        }

        float[] runtimes = new float[solversToTest.size()];
        float[] distances = new float[solversToTest.size()];

        try {
            output.print(  "                         ");
            for(String s : solversToTest)
                output.printf("%-30s", s);
            output.println();
            output.print("instance size  best      ");
            for(String s : solversToTest) {
                output.print("runtime makespan ecart        ");
            }
            output.println();


            for(String instanceName : instances) {
                int bestKnown = BestKnownResult.of(instanceName);


                Path path = Paths.get("instances/", instanceName);
                Instance instance = Instance.fromFile(path);

                output.printf("%-8s %-5s %4d      ",instanceName, instance.numJobs +"x"+instance.numTasks, bestKnown);

                for(int solverId = 0 ; solverId < solversToTest.size() ; solverId++) {
                    String solverName = solversToTest.get(solverId);
                    Solver solver = solvers.get(solverName);
                    long start = System.currentTimeMillis();
                    long deadline = System.currentTimeMillis() + solveTimeMs;
                    Result result = solver.solve(instance, deadline);
                    long runtime = System.currentTimeMillis() - start;

                    if(!result.schedule.isValid()) {
                        System.err.println("ERROR: solver returned an invalid schedule");
                        System.exit(1);
                    }

                    assert result.schedule.isValid();
                    int makespan = result.schedule.makespan();
                    float dist = 100f * (makespan - bestKnown) / (float) bestKnown;
                    runtimes[solverId] += (float) runtime / (float) instances.size();
                    distances[solverId] += dist / (float) instances.size();

                    output.printf("%7d %8s %5.1f        ", runtime, makespan, dist);
                    output.flush();
                }
                output.println();

            }


            output.printf("%-8s %-5s %4s      ", "AVG", "-", "-");
            for(int solverId = 0 ; solverId < solversToTest.size() ; solverId++) {
                output.printf("%7.1f %8s %5.1f        ", runtimes[solverId], "-", distances[solverId]);
            }



        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
