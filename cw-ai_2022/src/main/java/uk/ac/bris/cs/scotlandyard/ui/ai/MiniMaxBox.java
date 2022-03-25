package uk.ac.bris.cs.scotlandyard.ui.ai;

import io.atlassian.fugue.Pair;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.model.Piece;

import java.util.*;

public class MiniMaxBox {
    /*minimax handler is a stateful singleton class
    it defines a recursion depth for the algorithm,
    it should remember when we've asked it to predict detective's moves,
    the minimax algorithm can predict lists of optimal moves at once which optimises the process of moving multiple
    detectives in the same turn. it will store these moves until it needs to calculate more (recursion depth correlates
    to number of moves calculated.
     */
    static private MiniMaxBox instance = null;

    private final int depth = 3; //recursion depth
    private List<Move> preComputedMoves;
    private final Evaluator evaluator;

    private MiniMaxBox(Evaluator evaluator){
        this.evaluator = evaluator;
    }

    static MiniMaxBox getInstance(Evaluator... evaluators){ //singleton
        Evaluator evaluator = evaluators[0]; //if someone mistakenly passes lots of evaluators we only want the first
        if(evaluators.length > 1) System.out.println("Warning: MiniMaxBox will take the first of " + evaluators.length + " evaluators.");
        if(instance == null) { instance = new MiniMaxBox(evaluator); }
        return instance;
    }

    //  returns a list of moves which are best for for player(s) in the starting round
    private Pair<Integer, List<Move>> minimax(List<Turn> order, int depth, Board.GameState board){
        //we've reached ample recursion depth
        if(depth == 0) { return new Pair<Integer, List<Move>>(evaluator.score(board), new ArrayList<Move>()); }

        Piece inPlay = order.get(order.size() - depth).playedBy(); //0th, 1st, 2nd... turn in the tree-level order
        //stream decides which moves were done by the player moving this round
        List<Move> moves = board.getAvailableMoves().stream().filter(x -> x.commencedBy().equals(inPlay)).toList();
        //someone has won
        if(moves.size() == 0) { return new Pair<Integer, List<Move>>(evaluator.score(board), new ArrayList<Move>());  }


        List<Move> newPath = new ArrayList<Move>(); //keeps compiler smiling (choice is always initialised)
        int evaluation;

        //maximising player
        if(inPlay.isMrX()) {
            evaluation = Integer.MIN_VALUE;
            //System.out.println(inPlay);
            for(Move move : moves){ //for all mrx's moves
                //copy variables we pass to the next recursion level (pass by-ref messed whosLeft up)
                Pair<Integer, List<Move>> child = minimax(order,depth - 1, board.advance(move)); //board.advance is causing issues that may be solved by deep copying gamestate
                if(evaluation <= child.left()){ //child's children's min
                    evaluation = child.left();
                    newPath = child.right(); //sets the movement path in the gametree for a respective good route
                    newPath.add(0, move); //prepend this move to the path
                }
            }
            return new Pair<Integer, List<Move>>(evaluation, newPath);
        }
        //minimising player
        else /*if(toMove.isDetective())*/ {
            evaluation = Integer.MAX_VALUE;
            //System.out.println(inPlay);
            for (Move move : moves) { //for all mrx's moves
                Pair<Integer, List<Move>> child = minimax(order,depth - 1, board.advance(move));
                if (evaluation >= child.left()) { //child's children's max
                    evaluation = child.left();
                    newPath = child.right(); //sets the movement path in the gametree for a respective good route
                    newPath.add(0, move); //prepend this move to the path
                }
            }
            return new Pair<Integer, List<Move>>(evaluation, newPath);
        }
    }

    //i'll find you all the pieces currently yet to play in a round! (for the minimax method)
    private ArrayList<Piece> getBoardRemaining(Board.GameState board){
        List<Move> moves = board.getAvailableMoves().stream().toList();
        Set<Piece> pieces = new HashSet<Piece>(); //element-distinct set of people in remaining
        for(Move move : moves){
            pieces.add(move.commencedBy());
        }
        return new ArrayList<Piece>(pieces);
    }

    private Piece getNextGo(List<Piece> remaining){
        int selector = new Random().nextInt(remaining.size());
        return remaining.get(selector); //who's turn is it in the tree level below this?
    }

    private Turn getTurn(List<Piece> remaining, Board.GameState board){
        Piece inPlay = getNextGo(remaining);
        if(remaining.equals(List.of(Piece.MrX.MRX))) {
            remaining = new ArrayList<Piece>(board.getPlayers()); //now its all players
            remaining.remove(Piece.MrX.MRX); //remove whos currently played from whos left to play
        }else /*if detective*/{
            remaining.remove(inPlay);
            if(remaining.isEmpty()) { remaining.add(Piece.MrX.MRX); } //mrx's round
        }

        return new Turn(inPlay, remaining);

    }

    private List<Turn> makeTurnSequence(int depth, Board.GameState board){
        List<Turn> sequence = new ArrayList<>();
        List<Piece> remaining = getBoardRemaining(board); //starts from where the game currently is
        for(int i = 0; i < depth; i++){
            Turn nextTurn = getTurn(remaining, board);
            sequence.add(nextTurn);
            remaining = nextTurn.remaining(); //getter method
        }

        return sequence;
    }
     //@Overloading
    public Move minimax(int depth, Board.GameState board){
        List<Turn> order = makeTurnSequence(depth, board);
        List<Move> path = minimax(order, depth, board).right(); //start on the first piece in remaining
    }
}
