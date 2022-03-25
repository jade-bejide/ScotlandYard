package uk.ac.bris.cs.scotlandyard.ui.ai;

import uk.ac.bris.cs.scotlandyard.model.Piece;

import java.util.List;

public class Turn {
    private final Piece playedBy;
    private final List<Piece> remaining;

    Turn(Piece playedBy, List<Piece> remaining){
        this.playedBy = playedBy;
        this.remaining = remaining;
    }

    public Piece playedBy(){ return playedBy; }
    public List<Piece> remaining(){ return remaining; }
}
