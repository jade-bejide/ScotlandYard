package uk.ac.bris.cs.scotlandyard.ui.ai;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

import javax.annotation.Nonnull;


import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.graph.ImmutableValueGraph;
import io.atlassian.fugue.Pair;
import uk.ac.bris.cs.scotlandyard.model.*;

public class MyAi implements Ai {

	@Nonnull @Override public String name() { return "Name me!"; }

	static Dictionary<Integer, ArrayList<Integer>> populate(Dictionary<Integer, ArrayList<Integer>> nodeDict,
															ImmutableValueGraph<Integer, ImmutableSet<ScotlandYard.Transport>> graph, Integer source){
		nodeDict.put(source, new ArrayList<Integer>(Arrays.asList(0, null)));
		for(Object n : graph.nodes()){
			if((Integer) n != source) {
				nodeDict.put((Integer) n, new ArrayList<Integer>(Arrays.asList(Integer.MAX_VALUE, null)));
			}
		}
		return nodeDict;
	}

	static boolean searchList(Integer[] nodes, Integer x) {
		return Arrays.asList(nodes).contains(x);
	}

	static Pair<Integer, List<Integer>> shortestPathFromSourceToDestination(
			ImmutableValueGraph<Integer, ImmutableSet<ScotlandYard.Transport>> graph,
			Integer source,
			Integer destination) {
		Dictionary<Integer, ArrayList<Integer>> nodeDict = new Hashtable<Integer, ArrayList<Integer>>();

		nodeDict = populate(nodeDict, graph, source);

		int pos = 0;
		Integer[] visitedNodes = new Integer[35];
		Integer currentNode = source;
		Integer min = Integer.MAX_VALUE;
		Integer bestNode = -1;
		ArrayList<Integer> endPoints = new ArrayList<>(); //places we know at least one distance to (we've calculated at least once)
		endPoints.add(source);

		//Track tickets
		while (currentNode != destination) {

			//getting successors
			Object[] succ = graph.successors(currentNode).toArray();
			for (Object node : succ) {
				if(graph.edgeValue((Integer) node, currentNode).isPresent() && !MyAi.searchList(visitedNodes, (Integer)node) && (Integer)node != source) {
					//each edge value is worth one (working version)
					Integer distance = (nodeDict.get(currentNode).get(0) + 1);

					//only update the distance if its shorter than our shortest to that node
					if(distance < nodeDict.get(node).get(0)) { nodeDict.put((Integer) node, new ArrayList<Integer>(Arrays.asList(distance, currentNode))); }
					//all endpoints can be distinct
					if(!endPoints.contains((Integer) node)) { endPoints.add((Integer) node); } //add this node to the endpoints list

				}
			}
			visitedNodes[pos] = currentNode;

			endPoints.removeIf(Predicate.isEqual(currentNode)); //removes current node (its been fully visited)
			Integer localBestDist = Integer.MAX_VALUE; Integer d;
			ArrayList<Integer> debugDistances = new ArrayList<Integer>();
			for(Integer node : endPoints){// selects the shortest distance of all endpoints bar the ones already visited
				d = nodeDict.get(node).get(0);
				debugDistances.add(d);
				if(d < localBestDist){
					currentNode = node;
					localBestDist = d;
				}
			}
			//currentNode = bestNode;
			pos++;
		}


		List<Integer> path = new ArrayList<Integer>();


		path.add(0, currentNode);
		while (true) {
			currentNode = nodeDict.get(currentNode).get(1);
			path.add(0, currentNode);
			if(Objects.equals(currentNode, source)) {
				System.out.println("Path: " +path);
				Pair<Integer, List<Integer>> pathDistance = new Pair<Integer, List<Integer>>(nodeDict.get(destination).get(0), path);
				return pathDistance;
			} //needs to include source
		}
	}

	private List<Move> countMoves(List<Integer> path, Board board) {
		var x = board.getAvailableMoves()
				.stream()
				.filter(z -> z.commencedBy(player))
				.map(y -> y.destination)
				.toList();
		if (x.contains(nextNode));
		return null;
	}

	private Integer cumulativeDistance(Board board, List<Player> detectives, Player mrX) {
		int min = Integer.MAX_VALUE;
		Integer mrXLocation = mrX.location();
		List<Integer> distancePerDetective = new ArrayList<>();
		for (Player detective : detectives) {
			Integer detectiveLocation = detective.location();
			var path = shortestPathFromSourceToDestination(board.getSetup().graph, detectiveLocation, mrXLocation);
			Integer distance = path.left();
			List<Integer> nodes = path.right(); //may want to use for whatever reason
			distancePerDetective.add(distance);
		}

		return distancePerDetective.stream().reduce(0, (x,y) -> x+y);
	}

	private Integer getDesiredDestination(Player mrX) {
		int newLocation = mrX.location();


	}

	private Integer score() {
		//score elements:
		//distance from detectives (tickets away)
		//available moves
		//then call minimax

		int distance = cumulativeDistance(null, null);

		return null;
	}

	private Move minimax(Player player) {
		return null;
	}

	@Nonnull @Override public Move pickMove(
			@Nonnull Board board,
			Pair<Long, TimeUnit> timeoutPair) {
		// returns a random move, replace with your own implementation
		var moves = board.getAvailableMoves().asList();
		return moves.get(new Random().nextInt(moves.size()));
	}
}
