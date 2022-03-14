package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;
import javax.annotation.Nonnull;
import javax.swing.text.html.Option;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.*;
import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableSortedMap;
import uk.ac.bris.cs.scotlandyard.model.Board.GameState;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Factory;
import uk.ac.bris.cs.scotlandyard.model.Move.*;
import uk.ac.bris.cs.scotlandyard.model.Piece.*;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.*;


import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Ticket.DOUBLE;
import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Ticket.SECRET;

/**
 * cw-model
 * Stage 1: Complete this class
 */

public final class MyGameStateFactory implements Factory<GameState> {
	private final class MyGameState implements GameState {
		private GameSetup setup;
		private ImmutableSet<Piece> remaining;
		private final ImmutableList<LogEntry> log;
		private Player mrX;
		private final List<Player> detectives;
		private ImmutableSet<Move> moves;
		//may need to change after checking detectives
		private final ImmutableSet<Piece> winner = ImmutableSet.of();

		private void testNoOfPlayers() {
			int players = 0;
			if (this.mrX != null) players++;

			players += (int)this.detectives.stream().count();
		}
		private void proxy() {
			if (!mrX.isMrX()) throw new IllegalArgumentException("Mr X is empty");
			if (mrX.isDetective()) throw new IllegalArgumentException("Mr X has been swapped!");

			if (setup.graph.nodes().size() == 0) throw new IllegalArgumentException("Graph is empty!");
			if (setup.moves.isEmpty()) throw new IllegalArgumentException("Moves is empty!");
			if (this.mrX == null) throw new NullPointerException("Mr X not present!");
			if (this.remaining == null) throw new NullPointerException("Remaining players is empty!");
			if (this.detectives.isEmpty()) throw new NullPointerException("No detectives present!");

			if (this.detectives.contains(null)) throw new NullPointerException("Null detective is not allowed!");
			Player[] oneMrX = detectives.stream().filter(Player::isMrX).toArray(Player[]::new);
			if (oneMrX.length > 0) throw new IllegalArgumentException("Only one Mr X allowed!");

			//testNoOfPlayers();

			if (detectiveLoops.overlap(this.detectives)) throw new IllegalArgumentException("Overlap between detectives!");
			if (detectiveLoops.samePiece(this.detectives)) throw new IllegalArgumentException("Duplicate detectives!");
			if (detectiveLoops.secretTicket(this.detectives)) throw new IllegalArgumentException("Detective with secret ticket!");
			if (detectiveLoops.doubleTicket(this.detectives)) throw new IllegalArgumentException("Detective with double ticket!");
		}

		private final class detectiveLoops{ //(setup validation) (/stream/lined) (strategy pattern)
			private static boolean iterate(List<Player> detectives, Predicate<Player> p){
				return detectives.stream().anyMatch(p);
			}
			private static boolean iteratePairs(List<Player> detectives, Predicate<HashMap.Entry<Player, Player>> p){
				HashMap<Player, Player> pairs = new HashMap<Player, Player>();
				for(int i = 0; i < detectives.size(); i++){
					for(int j = i + 1; j < detectives.size(); j++){ //checks every pair exactly once3
						pairs.put(detectives.get(i), detectives.get(j));
					}
				}
				return pairs.entrySet()
						.stream()
						.anyMatch(p);
			}
			public static boolean samePiece(List<Player> detectives){
				return iteratePairs(detectives, (x -> x.getKey().piece() == x.getValue().piece()));
			}
			public static boolean overlap(List<Player> detectives){
				return iteratePairs(detectives, (x -> x.getKey().location() == x.getValue().location()));
			}
			public static boolean secretTicket(List<Player> detectives){
				return iterate(detectives, (x -> x.hasAtLeast(SECRET, 1)));
			}
			public static boolean doubleTicket(List<Player> detectives){
				return iterate(detectives, (x -> x.hasAtLeast(DOUBLE, 1)));
			}
		}

		private MyGameState(final GameSetup gs, final ImmutableSet<Piece> remaining, final ImmutableList<LogEntry> log, final Player mrX, final List<Player> detectives) {
			this.setup = gs;
			this.mrX = mrX;
			this.remaining = remaining;
			this.log = log;
			this.detectives = detectives;

			proxy();

			//getAvailableMoves

		}
		@Nonnull
		@Override
		public GameSetup getSetup() { return setup; }

		@Nonnull
		@Override
		public ImmutableSet<Piece> getPlayers() {
			Set<Piece> players = new HashSet<Piece>();
			for (Player detective : detectives) players.add(detective.piece());
			players.add(mrX.piece());

			ImmutableSet<Piece> gPlayers = ImmutableSet.copyOf(players);

			return gPlayers;
		}

//		private int findDestinationFromMoves(Move move, Player player){
//
//			ImmutableSet<Move> availableMoves = getAvailableMoves();
//			for(Move m : availableMoves){
//				if(m.)
//			}
//
//			return 0; //there is no zero node so this is an error code
//		}


		@Nonnull
		@Override
		public GameState advance(Move move) {
			Player player = move.commencedBy();
			if(!getAvailableMoves().contains(move)) throw new IllegalArgumentException("Illegal move! " + move);
			return move.accept(new Visitor<GameState>(){ //our gamestate-making visitor
				public GameState visit(SingleMove singleMove){
					/* singlemove code */

				}
				public GameState visit(DoubleMove doubleMove){
					/* doublemove code */
					HashSet<DoubleMove> doubleMoves = makeDoubleMoves()
				}
			});


//			List<Player> players = new ArrayList<Player>(detectives); players.add(mrX);
//			List<Player> filter = players
//					.stream()
//					.filter(x -> x.piece() == move.commencedBy())
//					.toList(); //gets player (singleton list)
//			Player player = filter.get(0);
//			Map<ScotlandYard.Ticket, Integer> mutableTickets = player.tickets();
//			mutableTickets.remove(move.tickets()); //warning can be ignored because this method only deals with valid moves
//			player = new Player(player.piece(), mutableTickets, );
//			//gets player and its tickets
//			if(!player.equals(mrX)){
//				Map<ScotlandYard.Ticket, Integer> mrXTickets = mrX.tickets();
//				for(ScotlandYard.Ticket t : move.tickets()){
//					mrXTickets.put(t, mrXTickets.get(move.tickets()) + 1); //adds one to each ticket type
//				}
//				mrX = new Player(mrX.piece(), (ImmutableMap<ScotlandYard.Ticket, Integer>) mrXTickets, mrX.location()); //mrx receives ticket but stays still
//				//sets the correct player in detectives
//				for(int i = 0; i < detectives.size(); i++){
//					if(detectives.get(i).piece() == player.piece()) {
//						detectives.set(i, player); i = detectives.size(); //exit loop
//					}
//				}
//			}else{ mrX = player; } //sets mrX to discard one ticket

			return null;
		}

		@Nonnull
		@Override
		public Optional<Integer> getDetectiveLocation(Detective detective) {
			Player[] detectives = this.detectives.stream().filter(x -> x.piece() == detective).limit(1).toArray(Player[]::new);
			if (detectives.length == 0) return Optional.empty();
			else return Optional.of(detectives[0].location());

		}

		@Nonnull
		@Override
		public Optional<TicketBoard> getPlayerTickets(Piece piece) {
			Player player = null;
			if (piece == MrX.MRX) player = mrX;
			else {
				//grabs relevant detective player
				Player[] detectives = this.detectives.stream().filter(x -> x.piece().equals(piece)).limit(1).toArray(Player[]::new);
				if (detectives.length != 0) player = detectives[0];
			}
			if (player != null) {
				final Player fPlayer = player;
				return Optional.of(new TicketBoard() {
					final ImmutableMap<Ticket, Integer> playerTickets = fPlayer.tickets();
					@Override
					public int getCount(@Nonnull Ticket ticket) {
						return playerTickets.get(ticket);
					}
				});
			}
			return Optional.empty();
		}

		@Nonnull
		@Override
		public ImmutableList<LogEntry> getMrXTravelLog() { return log; }

		@Nonnull
		@Override
		public ImmutableSet<Piece> getWinner() {
			return this.winner;
		}

		private static Set<Move.SingleMove> makeSingleMoves(GameSetup setup, List<Player> detectives, Player player, int source) {
			Set<SingleMove> possibleMoves = new HashSet<SingleMove>();
			for (int destination : setup.graph.adjacentNodes(source)) {
				// TODO find out if destination is occupied by a detective
				//  if the location is occupied, don't add to the collection of moves to return
				boolean occupied = detectives.stream().anyMatch(x -> x.location() == destination);

				if (!occupied) {
					for (Transport t : setup.graph.edgeValueOrDefault(source, destination, ImmutableSet.of())) {
						boolean canTravel = player.tickets().entrySet()
								.stream()
								.filter(x -> x.getKey().equals(t.requiredTicket()))
								.limit(1)
								.anyMatch(x -> x.getValue() > 0);
						if (canTravel) {
							possibleMoves.add(new SingleMove(player.piece(), source, t.requiredTicket(), destination));
						}
					}

					if (player.tickets().containsKey(SECRET) && player.tickets().get(SECRET) > 0)
						possibleMoves.add(new SingleMove(player.piece(), source, SECRET, destination));
				}
			}
			return possibleMoves;
		}

		private static Set<Move.DoubleMove> makeDoubleMoves(GameSetup setup, List<Player> detectives, Player player, int source1) {
			//makeSingleMoves(setup, detectives, player, source1);
			return null;
		}

		@Nonnull
		@Override
		public ImmutableSet<Move> getAvailableMoves() {
			return null;
		}
	}

	@Nonnull @Override public GameState build(
			GameSetup setup,
			Player mrX,
			ImmutableList<Player> detectives
			) {
		return new MyGameState(setup, ImmutableSet.of(MrX.MRX), ImmutableList.of(), mrX, detectives);

	}

}
