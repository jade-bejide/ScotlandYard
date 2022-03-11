package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;
import javax.annotation.Nonnull;
import javax.swing.text.html.Option;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.*;
import javax.annotation.Nonnull;
import uk.ac.bris.cs.scotlandyard.model.Board.GameState;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Factory;
import uk.ac.bris.cs.scotlandyard.model.Move.*;
import uk.ac.bris.cs.scotlandyard.model.Piece.*;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.*;


import java.util.stream.Collectors;

/**
 * cw-model
 * Stage 1: Complete this class
 */
public final class MyGameStateFactory implements Factory<GameState> {
	private final class MyGameState implements GameState {
		private GameSetup setup;
		private ImmutableSet<Piece> remaining;
		private ImmutableList<LogEntry> log;
		private Player mrX;
		private List<Player> detectives;
		private ImmutableSet<Move> moves;
		private ImmutableSet<Piece> winner;

		private MyGameState(final GameSetup gs, final ImmutableSet<Piece> remaining, final ImmutableList<LogEntry> log, final Player mrX, final List<Player> detectives) {
			this.setup = gs;
			this.mrX = mrX;
			this.remaining = remaining;
			this.log = log;
			this.detectives = detectives;

			if (setup.moves.isEmpty()) throw new IllegalArgumentException("Moves is empty!");
			if (this.mrX == null) throw new IllegalArgumentException("No detective present!");
			if (this.remaining == null) throw new IllegalArgumentException("Remaining players is empty!");
			if (this.log.isEmpty()) throw new IllegalArgumentException("Log is empty!");
			if (this.detectives.isEmpty()) throw new IllegalArgumentException("No detectives present!");

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

			return (ImmutableSet<Piece>) players;
		}

		@Nonnull
		@Override
		public GameState advance(Move move) {
			List<Player> players = new ArrayList<Player>(detectives); players.add(mrX);
			List<Player> filter = players
					.stream()
					.filter(x -> x.piece() == move.commencedBy())
					.toList(); //gets player (singleton list)
			Player player = filter.get(0);
			Map<ScotlandYard.Ticket, Integer> mutableTickets = player.tickets();
			mutableTickets.remove(move.tickets()); //warning can be ignored because this method only deals with valid moves
			player = new Player(player.piece(), mutableTickets, /*destination*/);
			if(!player.equals(mrX)){
				Map<ScotlandYard.Ticket, Integer> mrXTickets = mrX.tickets();
				for(ScotlandYard.Ticket t : move.tickets()){
					mrXTickets.put(t, mrXTickets.get(move.tickets()) + 1); //adds one to each ticket type
				}
				mrX = new Player(mrX.piece(), (ImmutableMap<ScotlandYard.Ticket, Integer>) mrXTickets, mrX.location()); //mrx receives ticket but stays still
				//sets the correct player in detectives
				for(int i = 0; i < detectives.size(); i++){
					if(detectives.get(i).piece() == player.piece()) {
						detectives.set(i, player); i = detectives.size(); //exit loop
					}
				}
			}else{ mrX = player; } //sets mrX to discard one ticket

			return null;
		}

		@Nonnull
		@Override
		public Optional<Integer> getDetectiveLocation(Piece.Detective detective) {
			return Optional.empty();
		}

		@Nonnull
		@Override
		public Optional<TicketBoard> getPlayerTickets(Piece piece) {
			return Optional.empty();
		}

		@Nonnull
		@Override
		public ImmutableList<LogEntry> getMrXTravelLog() {
			return null;
		}

		@Nonnull
		@Override
		public ImmutableSet<Piece> getWinner() {
			return null;
		}

		private static Set<Move.SingleMove> makeSingleMoves(GameSetup setup, List<Player> detectives, Player player, int source){

			// TODO create an empty collection of some sort, say, HashSet, to store all the SingleMove we generate

			for(int destination : setup.graph.adjacentNodes(source)) {
				// TODO find out if destination is occupied by a detective
				//  if the location is occupied, don't add to the collection of moves to return

				for(Transport t : setup.graph.edgeValueOrDefault(source, destination, ImmutableSet.of()) ) {
					// TODO find out if the player has the required tickets
					//  if it does, construct a SingleMove and add it the collection of moves to return
				}

				// TODO consider the rules of secret moves here
				//  add moves to the destination via a secret ticket if there are any left with the player
			}

			// TODO return the collection of moves

			return null;
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
