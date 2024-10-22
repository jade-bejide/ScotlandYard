package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.*;

import javax.annotation.Nonnull;

import java.util.*;
import uk.ac.bris.cs.scotlandyard.model.Board.GameState;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Factory;
import uk.ac.bris.cs.scotlandyard.model.Move.*;
import uk.ac.bris.cs.scotlandyard.model.Piece.*;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.*;

import java.util.function.Predicate;
import java.util.stream.Collectors;

import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Ticket.*;

/**
 * cw-model
 * Stage 1: Complete this class
 */

public final class MyGameStateFactory implements Factory<GameState> {
	private final static class MyGameState implements GameState {
		private final GameSetup setup;
		private final ImmutableSet<Piece> remaining;
		private final ImmutableList<LogEntry> log;
		private final Player mrX;
		private final List<Player> detectives;
		private final ImmutableSet<Move> moves;
		private final ImmutableSet<Piece> winner;



		private MyGameState(final GameSetup gs, final ImmutableSet<Piece> remaining, final ImmutableList<LogEntry> log, final Player mrX, final List<Player> detectives) {
			this.setup = gs;
			this.remaining = remaining;
			this.log = log;
			this.mrX = mrX;
			this.detectives = detectives;
			this.moves = getAvailableMoves();
			this.winner = ImmutableSet.copyOf(determineWinner().stream().map(Player::piece).collect(Collectors.toSet()));
			constructorTests();
		}

		private void constructorTests() {
			//Mr X Tests
			if (!mrX.isMrX()) throw new IllegalArgumentException("Mr X is empty");
			if (mrX.isDetective()) throw new IllegalArgumentException("Mr X has been swapped!");

			//Board tests
			if (setup.graph.nodes().size() == 0) throw new IllegalArgumentException("Graph is empty!");
			if (setup.moves.isEmpty()) throw new IllegalArgumentException("Moves is empty!");
			if (this.remaining == null) throw new NullPointerException("Remaining players is empty!");
			if (this.detectives.isEmpty()) throw new NullPointerException("No detectives present!");

			//Player tests
			if (this.detectives.contains(null)) throw new NullPointerException("Null detective is not allowed!");
			Player[] oneMrX = detectives.stream().filter(Player::isMrX).toArray(Player[]::new);
			if (oneMrX.length > 0) throw new IllegalArgumentException("Only one Mr X allowed!");

			//Correctness of detectives test
			if (detectiveLoops.overlap(this.detectives)) throw new IllegalArgumentException("Overlap between detectives!");
			if (detectiveLoops.samePiece(this.detectives)) throw new IllegalArgumentException("Duplicate detectives!");
			if (detectiveLoops.secretTicket(this.detectives)) throw new IllegalArgumentException("Detective with secret ticket!");
			if (detectiveLoops.doubleTicket(this.detectives)) throw new IllegalArgumentException("Detective with double ticket!");
		}

		private final static class detectiveLoops{ //(setup validation) (/stream/lined) (strategy pattern)
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

		//helper method which tests if mr X is trapped
		private boolean canMrXMove() {
			List<Integer> possibleLocations = setup.graph.adjacentNodes(mrX.location()).stream().toList();
			List<Integer> finalPossibleLocations = possibleLocations;

			//checks if the detectives are on one of Mr X's adjacent nodes and maps this to those locations
			List<Integer> overlap = detectives.stream().filter(x -> finalPossibleLocations.contains(x.location())).map(Player::location).toList();

			//possible locations now stores locations where the detectives nodes are not on
			possibleLocations = possibleLocations.stream().filter(x -> !overlap.contains(x)).toList();


			//checks if mr X can go to the remaining locations
			for (Integer possibleLocation : possibleLocations) {
				ImmutableSet<Transport> map = setup.graph.edgeValueOrDefault(mrX.location(), possibleLocation, ImmutableSet.of());
				assert map != null; //since edgeValueOrDefault is nullable, we check the return value to quell a warning
				for (Transport t : map) {

					boolean canTravel = mrX.tickets().containsKey(t.requiredTicket()) && mrX.tickets().get(t.requiredTicket()) > 0;
					if (canTravel) {
						return true;
					}
				}

				if (mrX.tickets().containsKey(SECRET) && mrX.tickets().get(SECRET) > 0) {
					return true;
				}
			}

			//if not, mr X can't move
			return false;
		}

		//helper function returns the winner is there is one or empty set if there isn't one
		private ImmutableSet<Player> determineWinner() {
			//mr X loses if a detective lands on his location
			boolean caught = detectives.stream().anyMatch(x -> x.location() == mrX.location());

			//mr X loses if he all his adjacent nodes are cornered
			boolean cornered = detectives.stream().allMatch(x -> setup.graph.adjacentNodes(mrX.location()).contains(x.location()));
			//if there are all detectives have no tickets left, mr x will win
			boolean noTickets = detectives.stream().allMatch(x -> x.tickets().entrySet().stream().allMatch(y -> y.getValue() == 0));
			//mr X wins if the log size is full (typically 24)
			boolean noMovesLeft = setup.moves.size() == log.size();

			if (noMovesLeft) return ImmutableSet.copyOf(Set.of(mrX));

			//mr X is cornered but can still escape
			if (cornered && canMrXMove()) {
				return ImmutableSet.copyOf(Collections.emptySet());
			}

			//if mr X is fully cornered, the detectives win
			if (cornered) return ImmutableSet.copyOf(detectives);

			//if there are no moves left, the detectives win, Mr X cannot run anymore
			if (getAvailableMoves().isEmpty())  {
				return ImmutableSet.copyOf(detectives);
			}

			//if the detectives have no tickets left, mrX wins
			if (noTickets) return ImmutableSet.copyOf(Set.of(mrX));


			if (caught) return ImmutableSet.copyOf(detectives);

			//else there is no winner
			return ImmutableSet.copyOf(Collections.emptySet());
		}

		@Nonnull
		@Override
		public ImmutableSet<Piece> getWinner() {
			return this.winner;
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

		private Player getPlayerOnPiece(Piece p){
			List<Player> players = new ArrayList<Player>(detectives); players.add(mrX);
			List<Player> filter = players
					.stream()
					.filter(x -> x.piece() == p)
					.toList(); //gets player (singleton list)
			return filter.get(0);
		}

		private ImmutableSet<Piece> nextRemaining(ImmutableSet<Piece> remaining, Piece piece){
			//thinking about logic which will skip a turn if the "next" player
			Set<Piece> copyOfRemaining = new HashSet<Piece>(remaining);
			boolean cornered = detectives.stream().allMatch(x -> setup.graph.adjacentNodes(mrX.location()).contains(x.location()));

			//swap to Mr X's go
			if (cornered && canMrXMove()) {
				copyOfRemaining.add(mrX.piece());
				return ImmutableSet.copyOf(copyOfRemaining);
			}

			if(copyOfRemaining.equals(Set.of(MrX.MRX))) {
				copyOfRemaining.remove(piece);
				copyOfRemaining = detectives.stream().map(Player::piece).collect(Collectors.toSet());
			} else {
				//switch to detectives
				copyOfRemaining.remove(piece);
				if(copyOfRemaining.isEmpty()) copyOfRemaining.add(mrX.piece());
			}

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


		//getAvailableMoves helper functions compiles all single moves possible for a particular player based on their
		//current location
		private static Set<Move.SingleMove> makeSingleMoves(GameSetup setup, List<Player> detectives, Player player, int source) {
			Set<SingleMove> possibleMoves = new HashSet<SingleMove>();
			for (int destination : setup.graph.adjacentNodes(source)) {
				boolean occupied = detectives.stream().anyMatch(x -> x.location() == destination);
				if (!occupied) {
					for (Transport t : Objects.requireNonNull(setup.graph.edgeValueOrDefault(source, destination, ImmutableSet.of()))) {

						boolean canTravel = player.tickets().containsKey(t.requiredTicket()) && player.tickets().get(t.requiredTicket()) > 0 && t.requiredTicket() != DOUBLE;
						if (canTravel) {
							possibleMoves.add(new SingleMove(player.piece(), source, t.requiredTicket(), destination));
						}
					}

					if (player.tickets().containsKey(SECRET) && player.tickets().get(SECRET) > 0) {
						possibleMoves.add(new SingleMove(player.piece(), source, SECRET, destination));
					}

				}
			}


			return possibleMoves;
		}

		//get available moves helper functions compiles all double moves possible for Mr X based on their
		//current location
		private static Set<Move.DoubleMove> makeDoubleMoves(GameSetup setup, List<Player> detectives, Player player, int source1) {

			//if (player.isDetective()) throw new IllegalArgumentException("Detectives can't make double moves");
			Set<DoubleMove> possibleDoubleMoves = new HashSet<>();

			//no need to compute anything is mr x doesn't have any double tickets
			if (player.tickets().get(DOUBLE) == 0) return possibleDoubleMoves;
			Set<SingleMove> possibleSingleMoves = makeSingleMoves(setup, detectives, player, source1);

			if (player.isMrX()) {
				//for each single move, check that it can be extended to a double move
				for (SingleMove single : possibleSingleMoves) {

					for (int destination : setup.graph.adjacentNodes(single.destination)) {
						//used to check if the double move is possible based on the tickets left
						Map<Ticket, Integer> ticketTracker = new HashMap<Ticket, Integer>();
						//done by making a temporary mutable copy of the player's tickets
						ticketTracker.putAll(player.tickets());

						ticketTracker.put(single.ticket, ticketTracker.get(single.ticket) - 1);

						boolean occupied = detectives.stream().anyMatch(x -> x.location() == destination);
						if (!occupied) {
							for (Transport t : Objects.requireNonNull(setup.graph.edgeValueOrDefault(single.destination, destination, ImmutableSet.of()))) {

								boolean canTravel = ticketTracker.containsKey(t.requiredTicket()) && ticketTracker.get(t.requiredTicket()) > 0 && t.requiredTicket() != DOUBLE;

								if (canTravel) {
									possibleDoubleMoves.add(new DoubleMove(player.piece(), source1, single.ticket, single.destination, t.requiredTicket(), destination));
									ticketTracker.put(t.requiredTicket(), ticketTracker.get(t.requiredTicket()) - 1);
								}

							}

							if (player.tickets().containsKey(SECRET) && ticketTracker.get(SECRET) > 0) {
								possibleDoubleMoves.add(new DoubleMove(player.piece(), source1, single.ticket, single.destination, SECRET, destination));
								ticketTracker.put(SECRET, ticketTracker.get(SECRET) - 1);
							}

						}
					}
				}

			}

			return possibleDoubleMoves;
		}



		@Nonnull
		@Override
		public ImmutableSet<Move> getAvailableMoves() {
			//gets available moves from the remaining players, granted that the game is not over (a winner has been
			//found)
			Set<Move> allMoves = new HashSet<Move>();
			List<Player> remainingPlayers = new ArrayList<>();
			remainingPlayers.addAll(detectives);
			remainingPlayers.add(mrX);
			remainingPlayers = remainingPlayers.stream().filter(x -> remaining.contains(x.piece())).toList();

			if (winner == null || winner.isEmpty()) {
				for (Player player : remainingPlayers) {
					allMoves.addAll(makeSingleMoves(setup, detectives, player, player.location()));
					if(player.isMrX() && (setup.moves.size() - log.size() >= 2)) allMoves.addAll(makeDoubleMoves(setup, detectives, player, player.location()));
					//if mrx has 2 or more moves left in his log, then he can double move
				}

				boolean detectiveRound = remainingPlayers.stream().anyMatch(x -> x.isDetective()) ;
				if (detectiveRound && allMoves.isEmpty() && remaining.size() == 1) {
					allMoves.addAll(makeSingleMoves(setup, detectives, mrX, mrX.location()));
					if (setup.moves.size() - log.size() >= 2) allMoves.addAll(makeDoubleMoves(setup, detectives, mrX, mrX.location()));
				}

			}

			return ImmutableSet.copyOf(allMoves);
		}

		@Nonnull
		@Override
		public GameState advance(Move move) {

			Player player = getPlayerOnPiece(move.commencedBy());
			Piece piece = player.piece();

			//use of anonymous class as GameState visitor has one point of call
			return move.accept(new Visitor<GameState>(){ //our gamestate-making visitor
				public GameState visit(SingleMove move){
					if (!setup.graph.adjacentNodes(move.source()).contains(move.destination)) throw new IllegalArgumentException("Illegal move! " + move);
					Ticket ticketUsed = ImmutableList.copyOf(move.tickets()).get(0);
					if(player.piece() == MrX.MRX){ //if the player taking the move is a detective (black piece)
						boolean hidden = !setup.moves.get(log.size()); //is this move hidden

						List<LogEntry> logMutable = new ArrayList<LogEntry>(log);
						if (hidden) logMutable.add(LogEntry.hidden(ticketUsed)); //finishes the state of the log
						else logMutable.add(LogEntry.reveal(ticketUsed, move.destination));

						Player mrXMutable = new Player(
								MrX.MRX,
								setTickets(mrX, ticketUsed, -1),
								move.destination
						); //moves mr x and changes his tickets
						//cycle to the next player and set the game state
						return new MyGameState(setup,  nextRemaining(remaining, piece), ImmutableList.copyOf(logMutable), mrXMutable, detectives);
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
						return new MyGameState(setup, nextRemaining(remaining, piece), log, mrXMutable, ImmutableList.copyOf(detectivesMutable));
					}
				}

				public GameState visit(DoubleMove move){
					/* doublemove code */
					GameState gs;

					if (move.commencedBy() != MrX.MRX) throw new IllegalArgumentException("Detectives can't make double moves!");
					//should use three tickets, double move and the associated moves used
					List<Ticket> ticketsUsed = new ArrayList<>();
					move.tickets().forEach(ticketsUsed::add);

					//makes a mutable copy of Mr X's tickets to make sure a double move is possible
					Map<Ticket, Integer>  newTicketSet = new HashMap<Ticket,Integer>();
					newTicketSet.putAll(mrX.tickets());

					List<LogEntry> newLog = new ArrayList<>(log);

					int newLocation = 0;

					int destination = 0;
					//start at index 1 to skip deduction of double ticket
					for (int i = 0; i < ticketsUsed.size()-1; i++) {
						Ticket ticket = ticketsUsed.get(i);
						if (i == 0) destination = move.destination1;
						if (i == 1) destination = move.destination2;

						//check if we should hide the location or reveal it
						boolean isHidden = !setup.moves.get(log.size() + i);
						if (isHidden) newLog.add(LogEntry.hidden(ticket));
						else newLog.add(LogEntry.reveal(ticket, destination));

						newLocation = destination;
					}

					//if the ticket has been used, remove it from the mutable copy
					for(HashMap.Entry<Ticket, Integer> ticketEntry : newTicketSet.entrySet()) {
						if (ticketsUsed.contains(ticketEntry.getKey())) newTicketSet.put(ticketEntry.getKey(), ticketEntry.getValue() - 1);
					}

					Player newMrX = new Player(MrX.MRX, ImmutableMap.copyOf(newTicketSet), newLocation);

					//load new gamestate and return it
					gs = new MyGameState(setup, nextRemaining(remaining, piece), ImmutableList.copyOf(newLog), newMrX, detectives);
					return gs;
				}
			});
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
