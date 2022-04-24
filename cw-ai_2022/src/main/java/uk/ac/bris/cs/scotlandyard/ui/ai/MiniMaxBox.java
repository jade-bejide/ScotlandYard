package uk.ac.bris.cs.scotlandyard.ui.ai;

import io.atlassian.fugue.Pair;
import uk.ac.bris.cs.scotlandyard.model.*;

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

    private final Evaluator mrXEvaluator;
    private final Evaluator detectiveEvaluator;
    private Turn thisTurn; //// the turn that pickMove takes ////
    private Evaluator thisTurnStrategy; //// which evaluator to use for this turn ////
    private List<Move> mrXMoves;
    private List<Move> currentDetectiveMoves;
    private int myID;
    // unit test minimax tree
    private final DoubleTree tree;

    private MiniMaxBox(Evaluator eMrX, Evaluator eDetectives, DoubleTree... possibleTestTree){
        this.mrXEvaluator = eMrX;
        this.detectiveEvaluator = eDetectives;
        this.tree = possibleTestTree.length > 0 ? possibleTestTree[0] : null;
}

    public static MiniMaxBox getInstance(Evaluator eMrX, Evaluator eDetectives, DoubleTree... possibleTestTree){ //singleton
        //if(possibleTestTree.length > 1) throw new AssertionError("You're passing in too many test trees to MiniMaxBox");
        //Evaluator evaluator = evaluators[0]; //if someone mistakenly passes lots of evaluators we only want the first
        //if(evaluators.length > 1) System.out.println("Warning: MiniMaxBox will take the first of " + evaluators.length + " evaluators.");
        if(instance == null) { instance = new MiniMaxBox(eMrX, eDetectives, possibleTestTree); }
        return instance;
    }

    private Pair<Double, List<Move>> evaluate(List<Move> moves, int id, Board.GameState board){
        if (thisTurn.playedBy().isDetective()) {
            moves = currentDetectiveMoves.stream().filter(x -> x.commencedBy().equals(thisTurn.playedBy())).toList();
        }
        double evaluation = thisTurnStrategy.score(thisTurn.playedBy(), moves, id, board);
        return new Pair<Double, List<Move>>(evaluation, new ArrayList<Move>());
    }

//    private List<Move> setMovesAvailable(List<Move> movesAvailable, Turn lastTurn){
//
//    }

    //  returns a list of moves which are best for player(s) in the starting round
    private List<Move> chooseMyLatestMoves(List<Move> currentlyAvailableMoves,
                                     List<Move> myMoves,
                                     Turn thisTurn,
                                     Turn lastTurn,
                                     int branchID){
        if(thisTurn.playedBy().equals(this.thisTurn.playedBy()) && currentlyAvailableMoves.size() > 0) {
            if(lastTurn.playedBy().equals(this.thisTurn.playedBy())) {
                myID = branchID;
            }
            return new ArrayList<Move>(currentlyAvailableMoves);
        }
        return myMoves;
    }


    private Pair<Double, List<Move>> minimax(List<Turn> order, int depth, double alpha, double beta,
                                             List<Move> myMoves, Board.GameState board,
                                             int branchID){ //branchID is only for tree building and therefore testing
        int recursions = order.size() - depth;
        Turn thisTurn = order.get(Math.min(recursions, order.size() - 1)); //this turn is last recursion's turn on depth = 0
        Turn lastTurn = order.get(Math.max(recursions - 1, 0));
        Piece inPlay = thisTurn.playedBy(); //0th, 1st, 2nd... turn in the tree-level order
        //stream decides which moves were done by the player moving this round
        List<Move> currentlyAvailableMoves = board.getAvailableMoves().stream().filter(x -> x.commencedBy().equals(inPlay)).toList();
        //System.out.println(thisTurn.playedBy());
        myMoves = chooseMyLatestMoves(currentlyAvailableMoves, myMoves, thisTurn, lastTurn, branchID);
//        if(thisTurn.playedBy().equals(this.thisTurn.playedBy()) && currentlyAvailableMoves.size() > 0) {
//            myMoves = new ArrayList<Move>(currentlyAvailableMoves);
//            if(lastTurn.playedBy().equals(this.thisTurn.playedBy())) {
//                myID = branchID;
//            }
//        }
        //if the level above decided which move the evaluation strategy should use as a destination

        //we've reached ample recursion depth
        if(depth == 0) {
            //System.out.println("Evaluate move id " + myID + ", evaluation by " + thisTurnStrategy);
            if (inPlay.isDetective()) {
                return evaluate(myMoves, myID, board); //no different for situations that dont loop over the strategising player more than once
            }
            else {
                return evaluate(myMoves, myID, board);
            }
        }


        //we pass in previous pieces moves, because its the previous piece's moves that are being evaluated
        if(currentlyAvailableMoves.size() == 0) { //this player cant move?
            //System.out.println("Got here! #1");
            if (inPlay.isDetective()) { //are we in a detective round?
                //System.out.println("Got here! #2");
                if (board.getAvailableMoves().stream().noneMatch(x -> x.commencedBy().isDetective())){ //are all detective stuck?
                    //System.out.println("Got here! #3");
                    return evaluate(myMoves, myID, board);
                }
                //if theyre not and one can move,
                //System.out.println("Got here! #4");
                return minimax(order, depth - 1, alpha, beta, currentlyAvailableMoves, board, branchID); //if we can move some detectives then the game isnt over
            }
            if (inPlay.isMrX()) {
                return evaluate(myMoves, myID, board); //dont check any of mrX's moves because hes stuck and has none
            }
        }

        List<Move> newPath = new ArrayList<Move>();
        double evaluation;

        //maximising player which sets alpha
        if(inPlay.isMrX()) {
            evaluation = -Double.MAX_VALUE;
            for(int i = 0; i < currentlyAvailableMoves.size(); i++){ //for all mrx's moves
                Move move = currentlyAvailableMoves.get(i);
                if (move.commencedBy().isDetective()) throw new IllegalArgumentException("Not on a detective level");
                // Tree testing (not part of minimax functionality)
                if(tree != null) { tree.prepareChild(recursions, branchID, evaluation); }
                //
                //alpha and beta just get passed down the tree at first
                Pair<Double, List<Move>> child = minimax(order, depth - 1, alpha, beta, myMoves, board.advance(move), i);
                double moveValue = child.left();
                // Tree testing (not part of minimax functionality)
                if(tree != null) { tree.specifyAndSetChild(tree.getLocation(recursions, branchID), i, moveValue); }
                //
                // passing back up the tree occurs on the line below
                alpha = Math.max(alpha, moveValue); //sets alpha progressively so that pruning can occur
                if(evaluation <= moveValue){ //max
                    evaluation = moveValue;
                    newPath = child.right(); //sets the movement path in the gametree for a respective good route
                    newPath.add(0, move); //prepend this move to the path
                }
                if(beta <= alpha) { //the right of the subtree will be lower than what we've got
                    i = currentlyAvailableMoves.size(); //break out of the loop
                }
            }
            // tree test
            if(tree != null) { tree.specifyAndSetParent(recursions, branchID, evaluation); }
            // //
            return new Pair<Double, List<Move>>(evaluation, newPath);
        }
        //minimising player
        else /*if(toMove.isDetective())*/ {
            evaluation = Double.MAX_VALUE;
            for(int i = 0; i < currentlyAvailableMoves.size(); i++){ //for all mrx's moves
                Move move = currentlyAvailableMoves.get(i);
                if (move.commencedBy().isMrX()) throw new IllegalArgumentException("Not on a mr X level");
                // Tree testing (not part of minimax functionality)
                if(tree != null) { tree.prepareChild(recursions, branchID, evaluation); }
                //
                Pair<Double, List<Move>> child = minimax(order, depth - 1, alpha, beta, myMoves, board.advance(move), i);
                double moveValue = child.left();
                // Tree testing (not part of minimax functionality)
                if(tree != null) { tree.specifyAndSetChild(tree.getLocation(recursions, branchID), i, moveValue); }
                //
                beta = Math.min(beta, moveValue); //sets beta progressively so that pruning can occur
                if(evaluation >= moveValue){ //min
                    evaluation = moveValue;
                    newPath = child.right(); //sets the movement path in the gametree for a respective good route
                    newPath.add(0, move); //prepend this move to the path
                }
                if(beta <= alpha) { //the right of the subtree will be higher than what we've got
                    i = currentlyAvailableMoves.size();
                }//break out of the loop
            }
            // Tree testing (not part of minimax functionality)
            if(tree != null) { tree.specifyAndSetParent(recursions, branchID, evaluation); }
            //
            return new Pair<Double, List<Move>>(evaluation, newPath);
        }
    }

    //i'll find you all the pieces currently yet to play in a round! (for the minimax method)
    private ArrayList<Piece> getBoardRemaining(Board.GameState board){
        //create a snapshot of the board
        return new ArrayList<Piece>(BoardHelper.getRemaining(board));
    }

    private Piece getNextGo(List<Piece> remaining){
        int selector = new Random().nextInt(remaining.size());
        return remaining.get(selector); //who's turn is it in the tree level below this?
    }

    private Turn getTurn(List<Piece> remaining, Board.GameState board){
        if (remaining.equals(List.of(Piece.MrX.MRX))) mrXMoves = List.copyOf(board.getAvailableMoves());
        else currentDetectiveMoves = List.copyOf(board.getAvailableMoves());
        Piece inPlay = getNextGo(remaining);
        if(remaining.equals(List.of(Piece.MrX.MRX))) {
            remaining = new ArrayList<Piece>(board.getPlayers()); //now its all players
            remaining.remove(Piece.MrX.MRX); //remove whos currently played from whos left to play
        }else /*if detective*/{
            remaining.remove(inPlay);
            if(remaining.isEmpty()) { remaining.add(Piece.MrX.MRX); } //mrx's round
        }

        return new Turn(inPlay, new ArrayList<Piece>(remaining));
    }

    private List<Turn> makeTurnSequence(int depth, Board.GameState board){
        List<Turn> sequence = new ArrayList<>();
        List<Piece> remaining = getBoardRemaining(board); //starts from where the game currently is
        for(int i = 0; i < depth; i++){ //as many as we require (may exceed game length)
            Turn nextTurn = getTurn(remaining, board);
            sequence.add(nextTurn);
            remaining = new ArrayList<Piece>(nextTurn.remaining()); //getter method
            // (needs to copy the property to not edit it)
        }

        return sequence;
    }
     //@Overloading
    public List<Move> minimax(int depth, Board.GameState board){

        // test tree
        if(tree != null) { tree.clear(); } //reset to default tree
        //

        //tree.addNodeOnLocation(new Node(-Double.MAX_VALUE));
        List<Turn> order = makeTurnSequence(depth, board);
        thisTurn = order.get(0); // the turn taken by THIS call to pickMove
        //how we score THIS turn (makes sure detectives only see what they should on their turn)
        thisTurnStrategy = order.get(0).playedBy().isMrX() ? mrXEvaluator : detectiveEvaluator;
        return minimax(order, depth, Integer.MIN_VALUE,
                Integer.MAX_VALUE, thisTurnStrategy.equals(mrXEvaluator) ? mrXMoves : currentDetectiveMoves, board, 0)
                .right();
    }

    //pure and safe test methods
    public DoubleTree getTree(){ return tree; }
    public List<Turn> getTurns(int depth, Board.GameState board){ return makeTurnSequence(depth, board); }
    public Evaluator getMrXEvaluator(){ return mrXEvaluator; }
    public Evaluator getDetectiveEvaluator() { return detectiveEvaluator; }
    public Evaluator getThisTurnStrategy(){ return thisTurnStrategy; }
}
