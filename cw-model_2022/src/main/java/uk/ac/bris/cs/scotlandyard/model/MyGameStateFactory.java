package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.*;

import javax.annotation.Nonnull;
import javax.swing.*;
import javax.swing.text.html.Option;

import java.util.*;
import javax.annotation.Nonnull;

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
		private final GameSetup setup;
		private final ImmutableSet<Piece> remaining;
		private final ImmutableList<LogEntry> log;
		private Player mrX;
		private final List<Player> detectives;
		private ImmutableSet<Move> moves;
		//may need to change after checking detectives
		private final ImmutableSet<Piece> winner = ImmutableSet.of();

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


		private ImmutableSet<Player> determineWinner() {
			Set<Player> winners = new HashSet<>();
			boolean anyDetectives = remaining.stream().anyMatch (x -> x.isDetective());
			boolean caught = detectives.stream().anyMatch(x -> x.location() == mrX.location());
			boolean stuck = getAvailableMoves().stream().anyMatch(x -> x.commencedBy().isMrX());

			if (log.size() == 24) winners.add(mrX);
			if (!anyDetectives) winners.add(mrX);

			if (caught) winners.addAll(detectives);
			if (stuck) winners.addAll(detectives);

			return ImmutableSet.copyOf(winners);
		}

		private MyGameState(final GameSetup gs, final ImmutableSet<Piece> remaining, final ImmutableList<LogEntry> log, final Player mrX, final List<Player> detectives) {
			this.setup = gs;
			this.mrX = mrX;
			this.remaining = remaining;
			this.log = log;
			this.detectives = detectives;

			proxy();

			//getAvailableMoves
			determineWinner();
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

			return ImmutableSet.copyOf(players);
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

		private Player getPlayerOnPiece(Piece p){
			List<Player> players = new ArrayList<Player>(detectives); players.add(mrX);
			List<Player> filter = players
					.stream()
					.filter(x -> x.piece() == p)
					.toList(); //gets player (singleton list)
			return filter.get(0);
		}

		private ImmutableSet<Piece> nextRemaining(ImmutableSet<Piece> remaining){
			List<Piece> copyOfRemaining = new ArrayList<Piece>(remaining);
			copyOfRemaining.remove(0);
			if(copyOfRemaining.equals(List.of(MrX.MRX))) copyOfRemaining = detectives.stream().map(Player::piece).toList();
			if(copyOfRemaining.isEmpty()) copyOfRemaining.add(mrX.piece());
			System.out.println();
			return ImmutableSet.copyOf(copyOfRemaining);
		}

		private ImmutableMap<Ticket, Integer> setTickets(Player p, Ticket t, int change){ //takes/gives a ticket to the given player of a given type
			HashMap<Ticket, Integer> ticketsMutable = new HashMap<Ticket, Integer>();
			for(HashMap.Entry<Ticket, Integer> e : p.tickets().entrySet()){
				if(e.getKey() == t) ticketsMutable.put(e.getKey(), e.getValue() + change); //changes the used-ticket count
				else ticketsMutable.put(e.getKey(), e.getValue());
			}
			return ImmutableMap.copyOf(ticketsMutable);
		}

		@Nonnull
		@Override
		public GameState advance(Move move) {
			Player player = getPlayerOnPiece(move.commencedBy());
			//if(!getAvailableMoves().contains(move)) throw new IllegalArgumentException("Illegal move! " + move);
			return move.accept(new Visitor<GameState>(){ //our gamestate-making visitor
				public GameState visit(SingleMove move){
					Ticket ticketUsed = ImmutableList.copyOf(move.tickets()).stream().limit(1).toList().get(0);
					/* singlemove code */
					if(player.piece() == MrX.MRX){ //if the player taking the move is a detective (black piece)
						boolean hidden = setup.moves.get(log.size()); //is this move hidden

						List<LogEntry> logMutable = new ArrayList<LogEntry>(log);
						logMutable.add(LogEntry.hidden(ticketUsed)); //finishes the state of the log
//						HashMap<Ticket, Integer> ticketsMutable = new HashMap<Ticket, Integer>();
//						for(HashMap.Entry<Ticket, Integer> e : mrX.tickets().entrySet()){
//							if(e.getKey() == ticketUsed) ticketsMutable.put(e.getKey(), e.getValue() - 1);
//							else ticketsMutable.put(e.getKey(), e.getValue());
//						} //adds all but the ticket used in the move
						Player mrXMutable = new Player(
								MrX.MRX,
								setTickets(mrX, ticketUsed, -1),
								move.destination
						); //moves mr x and changes his tickets
						//cycle to the next player and set the game state
						return new MyGameState(setup,  nextRemaining(remaining), ImmutableList.copyOf(logMutable), mrXMutable, detectives);
					}else{
						Player detectiveMutable = new Player( //detective moves to move destination and uses a ticket
								player.piece(),
								setTickets(player, ticketUsed, -1),
								move.destination
						);
						Player mrXMutable = new Player( //mrx does not move on his turn
								mrX.piece(),
								setTickets(mrX, ticketUsed, 1),
								mrX.location()
						);
						List<Player> detectivesMutable = new ArrayList<Player>(detectives);
						detectivesMutable.set(detectives.indexOf(player), detectiveMutable);
						return new MyGameState(setup, nextRemaining(remaining), log, mrXMutable, ImmutableList.copyOf(detectivesMutable));
					}
				}
				public GameState visit(DoubleMove move){
					/* doublemove code */
					GameState gs;
					//for jade

					//TODO
					if (move.commencedBy() != MrX.MRX) throw new IllegalArgumentException("Detectives can't make double moves!");

					//should use three tickets, double move and the associated moves used
					List<Ticket> ticketsUsed = new ArrayList<>();
					move.tickets().forEach(ticketsUsed::add);

					Map<Ticket, Integer>  newTicketSet = new HashMap<Ticket,Integer>();
					newTicketSet.putAll(mrX.tickets());

					List<LogEntry> newLog = new ArrayList<>();
					newLog.addAll(log);

					int newLocation = 0;

					int destination = 0;
					//start at index 1 to skip deduction of double ticket
					for (int i = 1; i < ticketsUsed.size(); i++) {
						Ticket ticket = ticketsUsed.get(i);
						if (i == 1) destination = move.destination1;
						if (i == 2) destination = move.destination2;
						boolean isHidden = setup.moves.get(log.size());
						if (isHidden) newLog.add(LogEntry.hidden(ticket));
						else {
							newLog.add(LogEntry.reveal(ticket, destination));
							newLocation = destination;
						}
					}

					for(HashMap.Entry<Ticket, Integer> ticketEntry : newTicketSet.entrySet()) {
						if (ticketsUsed.contains(ticketEntry.getKey())) newTicketSet.put(ticketEntry.getKey(), ticketEntry.getValue() - 1);
					}

					Player newMrX = new Player(MrX.MRX, ImmutableMap.copyOf(newTicketSet), newLocation);

					//load new gamestate and return it
					gs = new MyGameState(setup, nextRemaining(remaining), ImmutableList.copyOf(newLog), newMrX, detectives);
					return gs;
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
				//may need to make a flag for when doublemove is called on singlemove (we can go through a detective on a double move)
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
			//if (player.isDetective()) throw new IllegalArgumentException("Detectives can't make double moves");
			Set<DoubleMove> possibleDoubleMoves = new HashSet<>();
			Set<SingleMove> possibleSingleMoves = makeSingleMoves(setup, detectives, player, source1);
			//if (player.isDetective()) System.out.println("Uh oh!");
			if (player.isMrX()) {
				for (SingleMove single : possibleSingleMoves) {
					for (int destination : setup.graph.adjacentNodes(source1)) {
						boolean occupied = detectives.stream().anyMatch(x -> x.location() == destination);
						if (!occupied) {
							for (Transport t : setup.graph.edgeValueOrDefault(source1, destination, ImmutableSet.of())) {
								boolean canTravel = player.tickets().entrySet()
										.stream()
										.filter(x -> x.getKey().equals(t.requiredTicket()))
										.limit(1)
										.anyMatch(x -> x.getValue() > 0);
								if (canTravel) {
									possibleDoubleMoves.add(new DoubleMove(player.piece(), single.source(), single.ticket, single.destination, t.requiredTicket(), destination));
								}
							}

							if (player.tickets().containsKey(SECRET) && player.tickets().get(SECRET) > 0)
								possibleDoubleMoves.add(new DoubleMove(player.piece(), single.source(), single.ticket, single.destination, SECRET, destination));
						}
					}
				}
			}


				return possibleDoubleMoves;
			}

		@Nonnull
		@Override
		public ImmutableSet<Move> getAvailableMoves() {
			Set<Move> allMoves = new HashSet<Move>();
			for (Player player : remaining.stream().map(this::getPlayerOnPiece).toList()) {
				allMoves.addAll(makeSingleMoves(setup, detectives, player, player.location()));
				if(player.isMrX()) allMoves.addAll(makeDoubleMoves(setup, detectives, player, player.location()));
			}

			//System.out.println(allMoves);

			return ImmutableSet.copyOf(allMoves);
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
