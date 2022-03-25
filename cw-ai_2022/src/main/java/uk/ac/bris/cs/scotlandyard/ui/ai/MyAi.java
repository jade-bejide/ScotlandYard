package uk.ac.bris.cs.scotlandyard.ui.ai;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

import javax.annotation.Nonnull;


import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.graph.ImmutableValueGraph;
import io.atlassian.fugue.Pair;
import org.checkerframework.checker.nullness.Opt;
import org.checkerframework.checker.units.qual.A;
import uk.ac.bris.cs.scotlandyard.model.*;

import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Ticket.*;
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

		populate(nodeDict, graph, source);

		int pos = 0;
		Integer[] visitedNodes = new Integer[200];
		Integer currentNode = source;
		ArrayList<Integer> endPoints = new ArrayList<>(); //places we know at least one distance to (we've calculated at least once)
		endPoints.add(source);

		//Track tickets
		while (!currentNode.equals(destination)) {
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
			pos++;
			endPoints.removeIf(Predicate.isEqual(currentNode)); //removes current node (its been fully visited)
			Integer localBestDist = Integer.MAX_VALUE; Integer d;
			for(Integer node : endPoints){// selects the shortest distance of all endpoints bar the ones already visited
				d = nodeDict.get(node).get(0);
				if(d < localBestDist){
					currentNode = node;
					localBestDist = d;
				}
			}

		}

		List<Integer> path = new ArrayList<Integer>();
		path.add(0, currentNode);
		while (!Objects.equals(currentNode, source)) {
			currentNode = nodeDict.get(currentNode).get(1);
			path.add(0, currentNode);
		}

		return new Pair<Integer, List<Integer>>(nodeDict.get(destination).get(0), path);//needs to include source
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
	//reconstructing the player tickets
	@Nonnull ImmutableMap<ScotlandYard.Ticket, Integer> getPlayerTickets(Board.GameState board, Piece piece) {
		ArrayList<ScotlandYard.Ticket> ticketTypes = new ArrayList<ScotlandYard.Ticket>(Arrays.asList(TAXI, BUS, UNDERGROUND));

		if (piece.isMrX()) ticketTypes.addAll(Arrays.asList(DOUBLE, SECRET));
		Map<ScotlandYard.Ticket, Integer> tickets = new HashMap<ScotlandYard.Ticket, Integer>();

		for (ScotlandYard.Ticket ticket : ticketTypes) {
			if (board.getPlayerTickets(piece).isPresent()) {
				tickets.put(ticket, board.getPlayerTickets(piece).get().getCount(ticket));
			}
		}

		System.out.println("Player: " + piece + "Ticket: " + tickets);

		return ImmutableMap.copyOf(tickets);
	}
	@Nonnull
	private List<Player> getPlayers(Board.GameState board) {
		List<Piece.Detective> detectives = board.getPlayers().stream().filter(Piece::isDetective).map(y -> (Piece.Detective)y).toList();
		List<Piece.MrX> mrXSingle = board.getPlayers().stream().filter(Piece::isMrX).map(y -> (Piece.MrX)y).limit(1).toList();

		List<Piece> pieces = new ArrayList<Piece>();
		pieces.add(mrXSingle.get(0));
		pieces.addAll(detectives);

		List<Player> players = new ArrayList<Player>();

		for (Piece piece : pieces) {

			if (piece.isDetective()) {
				boolean hasLocation = board.getDetectiveLocation((Piece.Detective)piece).isPresent();
				if (hasLocation) {
					int location = board.getDetectiveLocation((Piece.Detective)piece).get();
					Player newDetective = new Player(piece, getPlayerTickets(board, piece),location);
					players.add(newDetective);
				}

			}
			else {
				ImmutableList<LogEntry> log = board.getMrXTravelLog();
				int n = log.size();
				boolean mrXRound = board.getAvailableMoves().stream().anyMatch(x -> x.commencedBy() == Piece.MrX.MRX);
				//default location (no significance)
				int location = 1;
				if (n > 0) {
					LogEntry lastLog = log.get(n-1);
					boolean hasLocation = lastLog.location().isPresent();
					if (hasLocation) {
						location = lastLog.location().get();
					}
				} else if (mrXRound) {
					List<Move> grabMoves = board.getAvailableMoves().stream().limit(1).toList();
					Move grabMove = grabMoves.get(0);
					location = grabMove.source();
				}

				Player newMrX = new Player(piece, getPlayerTickets(board, piece), location);
				players.add(newMrX);


			}
		}

		return players;
	}

	private List<Player> getDetectives(Board.GameState board) {
		return getPlayers(board).stream().filter(Player::isDetective).toList();
	}

	private Player getMrX(Board.GameState board) {
		List<Player> mrXS = getPlayers(board).stream().filter(Player::isMrX).toList();
		return mrXS.get(0);
	}

	private Integer cumulativeDistance(Board.GameState board, Player mrX, List<Player> detectives) {
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



	public Integer score(Board.GameState board) {
		//after calling minimax, for static evaluation we need to score elements:
		//distance from detectives (tickets away)
		//available moves
		int distance = cumulativeDistance(board, getMrX(board), getDetectives(board));

		return distance;
	}

	private Piece getNextGo(List<Piece> whosLeft){
		int selector = new Random().nextInt(whosLeft.size());
		return whosLeft.get(selector); //who's turn is it in the tree level below this?
	}
//	private static Move.SingleMove blankMove = new Move.SingleMove(Piece.MrX.MRX, 1 , ScotlandYard.Ticket.TAXI, 1);
//	//whosLeft - who's yet to take a turn this round (round = { mrx | detectives })
//  returns a list of moves which are best for for player(s) in the starting round
	private Pair<Integer, List<Move>> minimax(List<Piece> whosLeft, Piece whosGo, int depth, Board.GameState board){
		//'whosGo' is whos turn it currently is
		List<Move> moves = board.getAvailableMoves().stream().filter(x -> x.commencedBy().equals(whosGo)).toList();
		//we've reached ample recursion depth
		if(depth == 0) { return new Pair<Integer, List<Move>>(score(board), new ArrayList<Move>()); }
		//someone has won
		if(moves.size() == 0) { return new Pair<Integer, List<Move>>(score(board), new ArrayList<Move>());  }

		//this stream decides which moves were done by the player moving this round

		List<Move> newPath = new ArrayList<Move>(); //keeps compiler smiling (choice is always initialised)
		int evaluation;

		//maximising player
		if(whosGo.isMrX()) {
			whosLeft = new ArrayList<Piece>(board.getPlayers()); //now its all players
			whosLeft.remove(Piece.MrX.MRX); //remove whos currently played from whos left to play
			Piece nextGo = getNextGo(whosLeft); //chooses random of whos left
			evaluation = Integer.MIN_VALUE;
			System.out.println(whosLeft.toString() + whosGo.toString());
			for(Move move : moves){ //for all mrx's moves
				//copy variables we pass to the next recursion level (pass by-ref messed whosLeft up)
				List<Piece> whosLeftCopy = new ArrayList<Piece>(whosLeft);
				Pair<Integer, List<Move>> child = minimax(whosLeftCopy, nextGo,depth - 1, board.advance(move)); //board.advance is causing issues that may be solved by deep copying gamestate
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
			whosLeft.remove(whosGo); //this current detective is not to have another turn this round
			if (whosLeft.isEmpty()) whosLeft = new ArrayList<Piece>(List.of(Piece.MrX.MRX));
			Piece nextGo = getNextGo(whosLeft); //chooses random of whos left
			evaluation = Integer.MAX_VALUE;
			System.out.println(whosLeft.toString() + whosGo.toString());
			for (Move move : moves) { //for all mrx's moves
				//copy variables we pass to the next recursion level (pass by-ref messed whosLeft up)
				List<Piece> whosLeftCopy = new ArrayList<Piece>(whosLeft);
				Pair<Integer, List<Move>> child = minimax(whosLeftCopy, nextGo,depth - 1, board.advance(move));
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
	private ArrayList<Piece> buildRemaining(Board.GameState board){
		List<Move> moves = board.getAvailableMoves().stream().toList();
		Set<Piece> pieces = new HashSet<Piece>(); //element-distinct set of people in remaining
		for(Move move : moves){
			pieces.add(move.commencedBy());
		}
		return new ArrayList<Piece>(pieces);
	}

	private Move minimaxer(Integer depth, Board.GameState board) {
		//build gamestate tree for all possible moves of all possible players
		//use static evaluation to see which outcome favours player
		//propagate up the tree to discover the best move
		List<Piece> piecesInPlay = buildRemaining(board); //who's left to take a turn in this round
		//sequence of moves taken from current game state that give the best outcome for the current round's players
		List<Move> path = minimax(piecesInPlay, piecesInPlay.get(0), depth, board).right(); //start on the first piece in remaining
		System.out.println(path);
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

		return minimaxer(3, (Board.GameState) board);


	}
}
