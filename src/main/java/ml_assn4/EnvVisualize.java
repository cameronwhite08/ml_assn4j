package ml_assn4;

import burlap.behavior.policy.Policy;
import burlap.behavior.singleagent.auxiliary.StateReachability;
import burlap.behavior.singleagent.auxiliary.valuefunctionvis.ValueFunctionVisualizerGUI;
import burlap.behavior.valuefunction.ValueFunction;
import burlap.domain.singleagent.gridworld.GridWorldDomain;
import burlap.domain.singleagent.gridworld.GridWorldVisualizer;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.SADomain;
import burlap.shell.visual.VisualExplorer;
import burlap.statehashing.simple.SimpleHashableStateFactory;
import burlap.visualizer.Visualizer;

import java.util.List;

public class EnvVisualize {

    public static void gridWorld(SADomain domain, int[][] map, State initialState){
        //create visualizer and explorer
        Visualizer v = GridWorldVisualizer.getVisualizer(map);
        VisualExplorer exp = new VisualExplorer(domain, v, initialState);

        //set control keys to use w-s-a-d
        exp.addKeyAction("w", GridWorldDomain.ACTION_NORTH, "");
        exp.addKeyAction("s", GridWorldDomain.ACTION_SOUTH, "");
        exp.addKeyAction("a", GridWorldDomain.ACTION_WEST, "");
        exp.addKeyAction("d", GridWorldDomain.ACTION_EAST, "");

        exp.initGUI();
    }

    public static void gridWorldPolicy(SADomain domain, State initialState, ValueFunction valueFunction, Policy p, int width, int height){
        SimpleHashableStateFactory hashingFactory = new SimpleHashableStateFactory();
        List<State> allStates = StateReachability.getReachableStates(initialState, domain, hashingFactory);
        ValueFunctionVisualizerGUI gui = GridWorldDomain.getGridWorldValueFunctionVisualization(allStates, width, height, valueFunction, p);
        gui.initGUI();

    }
}
