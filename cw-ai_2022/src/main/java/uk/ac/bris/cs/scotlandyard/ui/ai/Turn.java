package uk.ac.bris.cs.scotlandyard.ui.ai;

import uk.ac.bris.cs.scotlandyard.model.Piece;

import java.util.List;

public class Turn {
    private final Piece playedBy;
    private final List<Piece> remaining; //remaining after the playedBy turn
    private final Evaluator evaluator; //what should we use to score this move if its the last move?

    Turn(Piece playedBy, List<Piece> remaining, Evaluator evaluator){
        this.playedBy = playedBy;
        this.remaining = remaining;
        this.evaluator = evaluator;
    }

    public Piece playedBy(){ return playedBy; }
    public List<Piece> remaining(){ return remaining; }
    public Evaluator evaluator(){ return evaluator; }
}
