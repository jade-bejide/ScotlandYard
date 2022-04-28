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
		//use of the observer pattern to monitor the state of the game
		return new Model() {
			private final List<Observer> observers = new ArrayList<>();
			private Board.GameState gs = new MyGameStateFactory().build(setup, mrX, detectives);

			@Nonnull
			@Override
			public Board getCurrentBoard() {
				return gs;
			}

			//adds an observer to the list of observers if it is not already in the list
			@Override
			public void registerObserver(@Nonnull Observer observer) {
				if (observer == null) throw new NullPointerException("No null observers!");
				if (observers.contains(observer)) throw new IllegalArgumentException("Can't have two of the same observer!");
				observers.add(observer);
			}

			//removes an observer from the list of observers, given that the observer is in
			//the list of observers
			@Override
			public void unregisterObserver(@Nonnull Observer observer) {
				if (observer == null) throw new NullPointerException("No null observers!");
				if (!observers.contains(observer)) throw new IllegalArgumentException("Observer not present!");
				observers.remove(observer);
			}

			@Nonnull
			@Override
			public ImmutableSet<Observer> getObservers() {
				return ImmutableSet.copyOf(observers);
			}

			//progresses game if the game is not over or ends the game
			@Override
			public void chooseMove(@Nonnull Move move) {
				gs = gs.advance(move);
				ImmutableSet<Piece> winners = gs.getWinner();
				if(winners.size() > 0){
					for(Observer observer : observers) observer.onModelChanged(gs, Observer.Event.GAME_OVER);
				}else{
					for(Observer observer : observers) observer.onModelChanged(gs, Observer.Event.MOVE_MADE);
				}
			}
		};
	}
}
