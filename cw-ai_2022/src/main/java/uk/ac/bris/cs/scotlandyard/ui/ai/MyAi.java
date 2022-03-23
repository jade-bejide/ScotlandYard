package uk.ac.bris.cs.scotlandyard.ui.ai;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

import javax.annotation.Nonnull;


import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.graph.ImmutableValueGraph;
import io.atlassian.fugue.Pair;
import org.checkerframework.checker.nullness.Opt;
import uk.ac.bris.cs.scotlandyard.model.*;

import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.defaultDetectiveTickets;
import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.defaultMrXTickets;

public class MyAi implements Ai {

	@Nonnull @Override public String name() { return "Name me!"; }

	//get players
//	private List<Player> getPlayers(Board board) {
//		List<Piece> pieces = board.getPlayers().stream().toList(); //good riddance, immutables! (just kidding hehe)
//		return pieces.stream().map()
//	}

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

	private boolean searchList(Integer[] nodes, Integer x) {
		return Arrays.asList(nodes).contains(x);
	}

	private Pair<Integer, List<Integer>> shortestPathFromSourceToDestination(
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
				if(graph.edgeValue((Integer) node, currentNode).isPresent() && !searchList(visitedNodes, (Integer)node) && (Integer)node != source) {
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

//	private List<Move> countMoves(List<Integer> path, Board board) {
//		var x = board.getAvailableMoves()
//				.stream()
//				.filter(z -> z.commencedBy(player))
//				.map(y -> y.destination)
//				.toList();
//		if (x.contains(nextNode));
//		return null;
//	}
//
//	private Player getPlayerOnPiece(Piece p){
//		List<Player> players = new ArrayList<Player>(detectives); players.add(mrX);
//		List<Player> filter = players
//				.stream()
//				.filter(x -> x.piece() == p)
//				.toList(); //gets player (singleton list)
//		return filter.get(0);
//	}

	private Integer cumulativeDistance(Board board, Player mrX, List<Player> detectives) {
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

		return distancePerDetective.stream().map(x -> x * x).reduce(0, (x,y) -> x+y);
	}

	public Integer score(Board board) {
		//after calling minimax, for static evaluation we need to score elements:
		//distance from detectives (tickets away)
		//available moves


		//int distance = cumulativeDistance(board, mrY, myDetect);

		int distance = 0;
		System.out.println(distance);

		return distance;
	}
//	private static Move.SingleMove blankMove = new Move.SingleMove(Piece.MrX.MRX, 1 , ScotlandYard.Ticket.TAXI, 1);
//	//turnsThisRound - who's yet to take a turn this round (round = { mrx | detectives })
//  returns a list of moves which are best for for player(s) in the starting round
	private Pair<Integer, List<Move>> minimax(List<Piece> turnsThisRound, Integer depth, Board.GameState board){
		int selector = new Random().nextInt(turnsThisRound.size());
		Piece toMove = turnsThisRound.get(selector); //who's turn?
		//this stream decides which moves were done by the player moving this round
		List<Move> moves = board.getAvailableMoves().stream().filter(x -> x.commencedBy().equals(toMove)).toList();

		if(depth == 0) { return new Pair<Integer, List<Move>>(score(board), new ArrayList<Move>()); } //scores the current game state
		if(moves.size() == 0) { return new Pair<Integer, List<Move>>(score(board), new ArrayList<Move>());  } //someone has won

		List<Move> newPath = new ArrayList<Move>(); //keeps compiler smiling (choice is always initialised)
		int evaluation;

		//maximising player
		if(toMove.isMrX()) {
			turnsThisRound = board.getPlayers().stream().toList(); //now its all players
			evaluation = Integer.MIN_VALUE;
			for(Move move : moves){ //for all mrx's moves
				Pair<Integer, List<Move>> child = minimax(turnsThisRound, depth - 1, board.advance(move));
				if(evaluation <= child.left()){
					evaluation = child.left();
					newPath = child.right(); //sets the movement path in the gametree for a respective good route
					newPath.add(0, move); //prepend this move to the path
				}
			}
			return new Pair<Integer, List<Move>>(evaluation, newPath);
		}
		//minimising player
		else /*if(toMove.isDetective())*/ {
			turnsThisRound.remove(selector); //this current detective is not to have another turn
			//advances to mrx's round in a similar way to nextRemaining
			if (turnsThisRound.isEmpty()) turnsThisRound = List.of(Piece.MrX.MRX);
			evaluation = Integer.MAX_VALUE;
			for (Move move : moves) { //for all mrx's moves
				Pair<Integer, List<Move>> child = minimax(turnsThisRound, depth - 1, board.advance(move));
				if (evaluation >= child.left()) {
					evaluation = child.left();
					newPath = child.right(); //sets the movement path in the gametree for a respective good route
					newPath.add(0, move); //prepend this move to the path
				}
			}
			return new Pair<Integer, List<Move>>(evaluation, newPath);
		}
	}

	//i'll find you all the pieces currently yet to play in a round! (for the minimax method)
	private List<Piece> buildRemaining(Board.GameState board){
		List<Move> moves = board.getAvailableMoves().stream().toList();
		Set<Piece> pieces = new HashSet<Piece>(); //element-distinct set of people in remaining
		for(Move move : moves){
			pieces.add(move.commencedBy());
		}
		return pieces.stream().toList();
	}

	private Move minimaxer(Integer depth, Board board) {
		//build gamestate tree for all possible moves of all possible players
		//use static evaluation to see which outcome favours player
		//propagate up the tree to discover the best move
		List<Piece> piecesInPlay = buildRemaining(board); //who's left to take a turn in this round
		//sequence of moves taken from current game state that give the best outcome for the current round's players
		List<Move> path = minimax(buildRemaining(board), depth, board).right();
		if(piecesInPlay.equals(List.of(Piece.MrX.MRX))){
			return path.get(0);
		}else{
			return path
					.stream()
					.filter(x -> x.commencedBy().equals(piecesInPlay.get(0))) //moves which correspond to who's next to go
					.toList().get(0);
		}
	}

	@Nonnull @Override public Move pickMove(
			@Nonnull Board board,
			Pair<Long, TimeUnit> timeoutPair) {
		// returns a random move, replace with your own implementation
//		var moves = board.getAvailableMoves().asList();
//		return moves.get(new Random().nextInt(moves.size()));
		return minimaxer(1, board);

	}
}
