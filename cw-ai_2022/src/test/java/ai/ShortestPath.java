package uk.ac.bris.scotlandyard.ai;

public class ShortestPath {
    public static main(String[] args) {
        Player mrY = new Player(Piece.MrX.MRX, defaultMrXTickets(), 82);
        Player blue = new Player(Piece.Detective.BLUE, defaultDetectiveTickets(), 69);
        Player green = new Player(Piece.Detective.GREEN, defaultDetectiveTickets(), 115);
        GameState game = gameStateFactory.build(standard24MoveSetup(), mrX, blu, green);
    }
}