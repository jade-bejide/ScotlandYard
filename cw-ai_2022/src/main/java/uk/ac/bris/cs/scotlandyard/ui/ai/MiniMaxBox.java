package uk.ac.bris.cs.scotlandyard.ui.ai;

import io.atlassian.fugue.Pair;
import uk.ac.bris.cs.scotlandyard.model.*;

import java.util.*;
import java.util.stream.Collectors;

public class MiniMaxBox {
    /*minimax handler is a stateful singleton class
    it defines a recursion depth for the algorithm,
    it should remember when we've asked it to predict detective's moves,
    the minimax algorithm can predict lists of optimal moves at once which optimises the process of moving multiple
    detectives in the same turn. it will store these moves until it needs to calculate more (recursion depth correlates
    to number of moves calculated.
     */
    static private MiniMaxBox instance = null;

    private final Evaluator eMrX;
    private final Evaluator eDetectives;

    private MiniMaxBox(Evaluator eMrX, Evaluator eDetectives){
        this.eMrX = eMrX;
        this.eDetectives = eDetectives;
    }

    static MiniMaxBox getInstance(Evaluator eMrX, Evaluator eDetectives){ //singleton
        //Evaluator evaluator = evaluators[0]; //if someone mistakenly passes lots of evaluators we only want the first
        //if(evaluators.length > 1) System.out.println("Warning: MiniMaxBox will take the first of " + evaluators.length + " evaluators.");
        if(instance == null) { instance = new MiniMaxBox(eMrX, eDetectives); }
        return instance;
    }

    private Pair<Integer, List<Move>> evaluate(Turn turn, Board.GameState board){
        return new Pair<Integer, List<Move>>(turn.evaluator().score(board), new ArrayList<Move>());
    }

//    private int getNodeValue(int alphaOrBeta,
//                             List<Move> moves,
//                             int evaluation,
//                             Comparator<Integer> compare){ //generates our branches
//        //System.out.println(inPlay);
//        for(Move move : moves){ //for all mrx's moves
//            //copy variables we pass to the next recursion level (pass by-ref messed whosLeft up)
//            Pair<Integer, List<Move>> child = minimax(order, depth - 1, board.advance(move)); //board.advance is causing issues that may be solved by deep copying gamestate
//            if(evaluation <= child.left()){ //child's children's min
//                evaluation = child.left();
//                newPath = child.right(); //sets the movement path in the gametree for a respective good route
//                newPath.add(0, move); //prepend this move to the path
//            }
//        }
//    }

    //  returns a list of moves which are best for player(s) in the starting round
    private Pair<Integer, List<Move>> minimax(List<Turn> order, int depth, int alpha, int beta, Board.GameState board){
        Turn thisTurn = order.get(Math.min(order.size() - depth, order.size() - 1)); //this turn is last turn on depth = 0
        //we've reached ample recursion depth
        if(depth == 0) { return evaluate(thisTurn, board); }

        Piece inPlay = thisTurn.playedBy(); //0th, 1st, 2nd... turn in the tree-level order

        //stream decides which moves were done by the player moving this round
        List<Move> moves = board.getAvailableMoves().stream().filter(x -> x.commencedBy().equals(inPlay)).toList();
        //System.out.println(moves);
        if(moves.size() == 0) { //this player cant move?
            if (inPlay.isDetective()) { //are we in a detective round?
                if (board.getAvailableMoves().stream().noneMatch(x -> x.commencedBy().isDetective())){ //are all detective stuck?
                    return evaluate(thisTurn, board);
                }
                //if theyre not and one can move,
                return minimax(order, depth - 1, alpha, beta, board); //if we can move some detectives then the game isnt over
            }
            if (inPlay.isMrX()) return evaluate(thisTurn, board);
        }

        List<Move> newPath = new ArrayList<Move>(); //keeps compiler smiling (choice is always initialised)
        int evaluation;

        //maximising player which sets alpha
        if(inPlay.isMrX()) {
            evaluation = Integer.MIN_VALUE;
            for(int i = 0; i < moves.size(); i++){ //for all mrx's moves
                Move move = moves.get(i);
                //alpha and beta just get passed down the tree at first
                Pair<Integer, List<Move>> child = minimax(order, depth - 1, alpha, beta, board.advance(move)); //board.advance is causing issues that may be solved by deep copying gamestate
                int moveValue = child.left();
                // passing back up the tree occurs on the line below
                alpha = Math.max(alpha, moveValue); //sets alpha progressively so that pruning can occur
                if(evaluation < moveValue){ //max
                    evaluation = moveValue;
                    newPath = child.right(); //sets the movement path in the gametree for a respective good route
                    newPath.add(0, move); //prepend this move to the path
                }
                if(beta <= alpha) { //the right of the subtree will be lower than what we've got
                    System.out.println("pruned on maximising player, the best we could expect for mrX is <= " +
                            beta + " on the right subtree, whereas we already have " + alpha);
                    i = moves.size(); //break out of the loop
                }
            }
            return new Pair<Integer, List<Move>>(evaluation, newPath);
        }
        //minimising player
        else /*if(toMove.isDetective())*/ {
            evaluation = Integer.MAX_VALUE;
            for(int i = 0; i < moves.size(); i++){ //for all mrx's moves
                Move move = moves.get(i);
                //alpha and beta just get passed down the tree at first
                Pair<Integer, List<Move>> child = minimax(order, depth - 1, alpha, beta, board.advance(move)); //board.advance is causing issues that may be solved by deep copying gamestate
                int moveValue = child.left();
                beta = Math.min(beta, moveValue); //sets beta progressively so that pruning can occur
                if(evaluation > moveValue){ //min
                    evaluation = moveValue;
                    newPath = child.right(); //sets the movement path in the gametree for a respective good route
                    newPath.add(0, move); //prepend this move to the path
                }
                if(beta <= alpha) { //the right of the subtree will be higher than what we've got
                    System.out.println("pruned on minimising player, the best we could expect for the detectives is <= " +
                            alpha + " on the right subtree, whereas we already have " + beta);
                    i = moves.size();
                }//break out of the loop
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
        Evaluator evaluator;
        if(remaining.equals(List.of(Piece.MrX.MRX))) {
            remaining = new ArrayList<Piece>(board.getPlayers()); //now its all players
            remaining.remove(Piece.MrX.MRX); //remove whos currently played from whos left to play
            evaluator = eMrX;
        }else /*if detective*/{
            remaining.remove(inPlay);
            if(remaining.isEmpty()) { remaining.add(Piece.MrX.MRX); } //mrx's round
            evaluator = eDetectives;
        }

        return new Turn(inPlay, remaining, evaluator);

    }

    private List<Turn> makeTurnSequence(int depth, Board.GameState board){
        List<Turn> sequence = new ArrayList<>();
        List<Piece> remaining = getBoardRemaining(board); //starts from where the game currently is
        for(int i = 0; i < depth; i++){ //as many as we require (may exceed game length)
            Turn nextTurn = getTurn(remaining, board);
            sequence.add(nextTurn);
            remaining = nextTurn.remaining(); //getter method
        }

        return sequence;
    }
     //@Overloading
    public Move minimax(int depth, Board.GameState board){
        List<Turn> order = makeTurnSequence(depth, board);
        Evaluator evaluator = order.get(0).playedBy().isMrX() ? eMrX : eDetectives;
        List<Move> optimalMoves = minimax(order, depth, Integer.MIN_VALUE, Integer.MAX_VALUE, board).right(); //start on the first piece in remaining
        return optimalMoves.get(0);
    }
}
