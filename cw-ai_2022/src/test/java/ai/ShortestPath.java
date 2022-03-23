package ai;

import uk.ac.bris.cs.scotlandyard.model.*;

public class ShortestPath {
    public static void main(String[] args) {
        Player mrY = new Player(Piece.MrX.MRX, defaultMrXTickets(), 82);
        Player blue = new Player(Piece.Detective.BLUE, defaultDetectiveTickets(), 69);
        Player green = new Player(Piece.Detective.GREEN, defaultDetectiveTickets(), 115);
        GameState game = new GameStateFactory.build(new GameSetup(defaultGraph, STANDARD24MOVES), mrY, blue, green);

        Pair<Long, TimeUnit> time = new Pair<>(15, SECONDS);

        MyAi ai = new MyAi(game, time);
        ai.score();
    }
}