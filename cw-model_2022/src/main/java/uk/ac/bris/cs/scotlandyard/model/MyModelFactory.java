package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableSet;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Factory;

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

			private List<Observer> characters;
			private Board.GameState gs;

			@Nonnull
			@Override
			public Board getCurrentBoard() {
				return null;
			}

			@Override
			public void registerObserver(@Nonnull Observer observer) {
				characters.add(observer);
			}

			@Override
			public void unregisterObserver(@Nonnull Observer observer) {
				characters.remove(observer);
			}

			@Nonnull
			@Override
			public ImmutableSet<Observer> getObservers() {
				return null;
			}

			@Override
			public void chooseMove(@Nonnull Move move) {
				gs = gs.advance(move);
				ImmutableSet<Piece> winners = gs.getWinner();
				if(winners.size() > 0){
					for(Observer observer : characters) observer.onModelChanged(gs, Observer.Event.GAME_OVER);
				}else{
					for(Observer observer : characters) observer.onModelChanged(gs, Observer.Event.MOVE_MADE);
				}
			}
		};
	}
}
