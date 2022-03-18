package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableSet;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Factory;

import java.util.ArrayList;
import java.util.List;

/**
 * cw-model
 * Stage 2: Complete this class
 */

public final class MyModelFactory implements Factory<Model> {

	@Nonnull @Override public Model build(GameSetup setup,
	                                      Player mrX,
	                                      ImmutableList<Player> detectives) {
		// TODO
		return new Model() {

			private List<Observer> characters = new ArrayList<>();
			private Board.GameState gs;

			@Nonnull
			@Override
			public Board getCurrentBoard() {
				return null;
			}

			@Override
			public void registerObserver(@Nonnull Observer observer) {
				if (observer == null) throw new NullPointerException("No null observers!");
				if (characters.contains(observer)) throw new IllegalArgumentException("Can't have two of the same observer!");
				characters.add(observer);
			}

			@Override
			public void unregisterObserver(@Nonnull Observer observer) {
				if (observer == null) throw new NullPointerException("No null observers!");
				if (!characters.contains(observer)) throw new IllegalArgumentException("Observer not present!");
				characters.remove(observer);
			}

			@Nonnull
			@Override
			public ImmutableSet<Observer> getObservers() {
				return ImmutableSet.copyOf(characters);
			}

			@Override
			public void chooseMove(@Nonnull Move move) {
				System.out.println("");
			}
		};
	}
}
