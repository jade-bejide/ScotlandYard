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

    // unit test minimax tree
    private DoubleTree tree;

    private MiniMaxBox(Evaluator eMrX, Evaluator eDetectives){
        this.mrXEvaluator = eMrX;
        this.detectiveEvaluator = eDetectives;
        this.tree = new DoubleTree();
    }

    public static MiniMaxBox getInstance(Evaluator eMrX, Evaluator eDetectives){ //singleton
        //Evaluator evaluator = evaluators[0]; //if someone mistakenly passes lots of evaluators we only want the first
        //if(evaluators.length > 1) System.out.println("Warning: MiniMaxBox will take the first of " + evaluators.length + " evaluators.");
        if(instance == null) { instance = new MiniMaxBox(eMrX, eDetectives); }
        return instance;
    }

    //unit test minimax tree
    public DoubleTree getTree(){ return tree; }

    private Pair<Double, List<Move>> evaluate(List<Move> moves, Board.GameState board){
        double evaluation = thisTurnStrategy.score(thisTurn.playedBy(), moves, board);
        return new Pair<Double, List<Move>>(evaluation, new ArrayList<Move>());
    }

    //  returns a list of moves which are best for player(s) in the starting round
    private Pair<Double, List<Move>> minimax(List<Turn> order, int depth, double alpha, double beta,
                                             List<Move> previousPiecesMoves, Board.GameState board, int ID){
        int recursions = order.size() - depth;
        Turn thisTurn = order.get(Math.min(recursions, order.size() - 1)); //this turn is last turn on depth = 0
        Piece inPlay = thisTurn.playedBy(); //0th, 1st, 2nd... turn in the tree-level order
        //stream decides which moves were done by the player moving this round
        List<Move> moves = board.getAvailableMoves().stream().filter(x -> x.commencedBy().equals(inPlay)).toList();
        //we've reached ample recursion depth
        if(depth == 0) {
            return evaluate(previousPiecesMoves, board);
        }
        //we pass in previous pieces moves, because its the previous piece's moves that are being evaluated

        if(moves.size() == 0) { //this player cant move?
            if (inPlay.isDetective()) { //are we in a detective round?
                if (board.getAvailableMoves().stream().noneMatch(x -> x.commencedBy().isDetective())){ //are all detective stuck?
                    return evaluate(moves, board);
                }
                //if theyre not and one can move,
                return minimax(order, depth - 1, alpha, beta, moves, board, 0); //if we can move some detectives then the game isnt over
            }
            if (inPlay.isMrX()) return evaluate(moves, board);
        }

        List<Move> newPath = new ArrayList<Move>(); //keeps compiler smiling (choice is always initialised)
        double evaluation;

        //maximising player which sets alpha
        if(inPlay.isMrX()) {
            evaluation = -Double.MAX_VALUE;
            for(int i = 0; i < moves.size(); i++){ //for all mrx's moves
                Move move = moves.get(i);

                // Tree testing
//                tree.setLocationOnDepthAndID(recursions, ID);
//                List<Integer> locationToSetValueOf = new ArrayList<Integer>(tree.getLocation());
//                tree.addNodeOnLocation(new Node(evaluation));
                // //

                //alpha and beta just get passed down the tree at first
                Pair<Double, List<Move>> child = minimax(order, depth - 1, alpha, beta, moves, board.advance(move), i);
                Double moveValue = child.left();

                //ok to use unsafe method here as we know this will be a location (already checked)
//                tree.setLocation(locationToSetValueOf);
//                tree.setValueOnLocation(moveValue);

                // passing back up the tree occurs on the line below
                alpha = Math.max(alpha, moveValue); //sets alpha progressively so that pruning can occur
                if(evaluation < moveValue){ //max
                    evaluation = moveValue;
                    newPath = child.right(); //sets the movement path in the gametree for a respective good route
                    newPath.add(0, move); //prepend this move to the path
                }
                if(beta <= alpha) { //the right of the subtree will be lower than what we've got
                    i = moves.size(); //break out of the loop
                }
            }
            return new Pair<Double, List<Move>>(evaluation, newPath);
        }
        //minimising player
        else /*if(toMove.isDetective())*/ {
            evaluation = Double.MAX_VALUE;
            for(int i = 0; i < moves.size(); i++){ //for all mrx's moves
                Move move = moves.get(i);

                // Tree testing
//                tree.setLocationOnDepthAndID(recursions, ID);
//                List<Integer> locationToSetValueOf = new ArrayList<Integer>(tree.getLocation());
//                tree.addNodeOnLocation(new Node(evaluation));
                // //

                Pair<Double, List<Move>> child = minimax(order, depth - 1, alpha, beta, moves, board.advance(move), i);
                Double moveValue = child.left();

//                tree.setLocation(locationToSetValueOf);
//                tree.setValueOnLocation(moveValue);

                beta = Math.min(beta, moveValue); //sets beta progressively so that pruning can occur
                if(evaluation > moveValue){ //min
                    evaluation = moveValue;
                    newPath = child.right(); //sets the movement path in the gametree for a respective good route
                    newPath.add(0, move); //prepend this move to the path
                }
                if(beta <= alpha) { //the right of the subtree will be higher than what we've got
                    i = moves.size();
                }//break out of the loop
            }
            return new Pair<Double, List<Move>>(evaluation, newPath);
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
        for(int i = 0; i < depth; i++){ //as many as we require (may exceed game length)
            Turn nextTurn = getTurn(remaining, board);

            //System.out.println("Turn: " + nextTurn.playedBy());
            sequence.add(nextTurn);
            remaining = nextTurn.remaining(); //getter method
        }

        System.out.println("");

        return sequence;
    }
     //@Overloading
    public List<Move> minimax(int depth, Board.GameState board){

        tree = new DoubleTree();

        List<Turn> order = makeTurnSequence(depth, board);
        thisTurn = order.get(0); // the turn taken by THIS call to pickMove
        //how we score THIS turn (makes sure detectives only see what they should on their turn)
        thisTurnStrategy = order.get(0).playedBy().isMrX() ? mrXEvaluator : detectiveEvaluator;
        return minimax(order, depth, Integer.MIN_VALUE,
                Integer.MAX_VALUE, new ArrayList<Move>(), board, 0)
                .right();
    }

    //pure and safe test methods
    public List<Turn> getTurns(int depth, Board.GameState board){ return makeTurnSequence(depth, board); }
    public Evaluator getMrXEvaluator(){ return mrXEvaluator; }
    public Evaluator getDetectiveEvaluator() { return detectiveEvaluator; }
}
