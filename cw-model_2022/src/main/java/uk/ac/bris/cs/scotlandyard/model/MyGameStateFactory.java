package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableSet;
import uk.ac.bris.cs.scotlandyard.model.Board.GameState;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Factory;

import java.util.*;
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
			List<Player> playerOnEnum = players
					.stream()
					.filter(x -> x.piece() == move.commencedBy())
					.toList();
			if(playerOnEnum.get(0).equals(mrX)){
				mrX = new Player(piece, ), );
			}
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

		@Nonnull
		@Override
		public ImmutableSet<Move> getAvailableMoves() {
			return null;
		}
	}

	@Nonnull @Override public GameState build(
			GameSetup setup,
			Player mrX,
			ImmutableList<Player> detectives,
			Piece MrX) {
		return new GameState(setup, ImmutableSet.of(MrX.MRX), ImmutableList.of(), mrX, detectives);

	}

}
