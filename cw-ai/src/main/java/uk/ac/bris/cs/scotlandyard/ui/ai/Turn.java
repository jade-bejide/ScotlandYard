package uk.ac.bris.cs.scotlandyard.ui.ai;

import uk.ac.bris.cs.scotlandyard.model.Piece;

import java.util.List;

// like a piece wrapper class but it needs remaining to generate successive turns,
// and evaluator to decide how to evaluate each terminal move in the minimax tree
public class Turn {

    private final Piece playedBy;
    private final List<Piece> remaining; //remaining after the playedBy turn

    Turn(Piece playedBy, List<Piece> remaining/*, Evaluator evaluator*/){
        this.playedBy = playedBy;
        this.remaining = remaining;
    }

    public Piece playedBy(){ return playedBy; }
    public List<Piece> remaining(){ return remaining; }
    public String toString(){ return "(playedBy:" + playedBy() + ", remaining:" + remaining() + ")"; }
}
