package ml_assn4;

import burlap.behavior.policy.EpsilonGreedy;
import burlap.behavior.policy.Policy;
import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.behavior.singleagent.learning.LearningAgentFactory;
import burlap.behavior.singleagent.learning.tdmethods.QLearning;
import burlap.behavior.singleagent.planning.Planner;
import burlap.behavior.singleagent.planning.stochastic.policyiteration.PolicyIteration;
import burlap.behavior.singleagent.planning.stochastic.valueiteration.ValueIteration;
import burlap.behavior.valuefunction.ValueFunction;
import burlap.domain.singleagent.gridworld.GridWorldDomain;
import burlap.domain.singleagent.gridworld.GridWorldRewardFunction;
import burlap.domain.singleagent.gridworld.GridWorldTerminalFunction;
import burlap.domain.singleagent.gridworld.state.GridAgent;
import burlap.domain.singleagent.gridworld.state.GridWorldState;
import burlap.mdp.auxiliary.DomainGenerator;
import burlap.mdp.auxiliary.common.ConstantStateGenerator;
import burlap.mdp.core.Domain;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.SADomain;
import burlap.mdp.singleagent.environment.SimulatedEnvironment;
import burlap.statehashing.simple.SimpleHashableStateFactory;
import ml_assn4.maze_generation.Maze;
import org.apache.commons.lang3.ArrayUtils;

import java.util.function.Function;

public class GridWorldSolver extends ProblemAttempt {

    State initialState;
    int w;
    int h;

    int mazeWdth = 10;
    int mazeHeight = 10;

    public GridWorldSolver() {
        super();
    }

    @Override
    protected void SetupExperiment() {
        super.SetupExperiment();
        initialState = new GridWorldState(
                new GridAgent(0, h-1)
        );
    }

    @Override
    public void visualizeProblem() {
        super.visualizeProblem();
        EnvVisualize.gridWorld((SADomain) createDomain(), ((GridWorldDomain)domainGenerator).getMap(), initialState);
    }

    @Override
    public void performExperiment(Function<Domain, LearningAgentFactory> agentFactory) {
        super.performExperiment(agentFactory);
        SADomain currentDomain = (SADomain) createDomain();

        SimpleHashableStateFactory hashingFactory = new SimpleHashableStateFactory();
        // value iteration
        Planner valuePlanner = new ValueIteration(currentDomain, 0.99, hashingFactory, 0.001, 100);
        Policy valuePolicy = valuePlanner.planFromState(initialState);
//        PolicyUtils.rollout(p, initialState, currentDomain.getModel()).write(outputPath + "vi");
        ml_assn4.EnvVisualize.gridWorldPolicy(currentDomain, initialState, (ValueFunction)valuePlanner, valuePolicy, w, h);

        // policy iteration
        Planner policyPlanner = new PolicyIteration(currentDomain, 0.99, hashingFactory, 0.001, 100, 100);
        Policy policyPolicy = policyPlanner.planFromState(initialState);
        EnvVisualize.gridWorldPolicy(currentDomain, initialState, (ValueFunction)policyPlanner, policyPolicy, w, h);

        // qlearning agent
        LearningAgentFactory qAgentFactory = agentFactory.apply(currentDomain);
        LearningAgent qAgent = qAgentFactory.generateAgent();
        Policy qPolicy = new EpsilonGreedy((QLearning)qAgent, 0.1);
        ((QLearning) qAgent).setLearningPolicy(qPolicy);

        //initial state generator
        final ConstantStateGenerator sg = new ConstantStateGenerator(initialState);

        //define learning environment
        SimulatedEnvironment env = new SimulatedEnvironment(currentDomain, sg);

        //run learning for 50 episodes
        for(int i = 0; i < 2500; i++){
            Episode e = qAgent.runLearningEpisode(env);

//            e.write(outputPath + "ql_" + i);
//            System.out.println(i + ": " + e.maxTimeStep());

            //reset environment for next learning episode
            env.resetEnvironment();
        }
        ml_assn4.EnvVisualize.gridWorldPolicy(currentDomain, initialState, (ValueFunction)qAgent, qPolicy, w, h);

//        ml_assn4.Plotter.plot(currentDomain, initialState, agentFactory.apply(currentDomain));
    }

    @Override
    DomainGenerator createDomainGenerator() {
        Maze m = new Maze(mazeWdth, mazeHeight);
        String mazeStr = m.toString();
        String[] mazeArrs = mazeStr.split("\n");
        mazeArrs = ArrayUtils.remove(mazeArrs, 0);
        mazeArrs = ArrayUtils.remove(mazeArrs, mazeArrs.length-1);

        w = mazeArrs[0].length()-2;
        h = mazeArrs.length;

        GridWorldDomain gridWorldGenerator = new GridWorldDomain(w, h); //11x11 grid world
        //stochastic transitions with 0.8 success rate
        gridWorldGenerator.setProbSucceedTransitionDynamics(0.8);

        for (int i = 0; i < mazeArrs.length; i++) {
            String rowString = mazeArrs[i].substring(1, mazeArrs[i].length()-1);
            for (int j = 0; j < rowString.length(); j++) {
                if(rowString.charAt(j) == '#'){
                    gridWorldGenerator.setCellWallState(i, j, 1);
                }
            }
        }

        GridWorldTerminalFunction tf = new GridWorldTerminalFunction();
        tf.markAsTerminalPosition(w-1, 0);

        GridWorldRewardFunction rf = new CustomGridWorldRewardFunction(w, h, -0.1);
        rf.setReward(w-1, 0, 5.);

        gridWorldGenerator.setTf(tf);
        gridWorldGenerator.setRf(rf);
        return gridWorldGenerator;
    }
}

class CustomGridWorldRewardFunction extends GridWorldRewardFunction {

    public CustomGridWorldRewardFunction(int width, int height, double initializingReward) {
        super(width, height, initializingReward);
    }

    @Override
    public double reward(State s, Action a, State sprime) {
        return super.reward(sprime, a, s);
    }
}
