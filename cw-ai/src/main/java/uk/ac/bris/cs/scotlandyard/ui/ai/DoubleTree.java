package uk.ac.bris.cs.scotlandyard.ui.ai;

import java.util.ArrayList;
import java.util.List;

public class DoubleTree {
    private final Node root;
    //for mutating:
    private List<Integer> location; // selects the branch to go down from root {branch #, branch #, ...}
    private Node atLocation; // the node at location

    public DoubleTree(Node root) {
        this.root = root;
        this.location = new ArrayList<Integer>();
        this.atLocation = root;
    }

    public DoubleTree(){
        this.root = new Node(-1);
        this.location = new ArrayList<Integer>();
        this.atLocation = root;
    }

    public void clear(){ //returns tree to default state
        unsafeSetLocation(new ArrayList<Integer>());
        root.pruneAllChildren();
        root.setValue(-1);
    }

    private Node navigateTo(List<Integer> location){ //find node on location passed in
        Node current = root;
        for(Integer branchNum : location) {
            current = current.getBranches().get(branchNum);
        }
        return current;
    }

    public List<Integer> getLocation(){ return location; }
    public Node getNodeOnLocation() { return atLocation; }

    public void setLocation(List<Integer> location){
        try{
            atLocation = navigateTo(location);
        }catch(IndexOutOfBoundsException e){
            show();
            System.err.println("Warning: no such child at location " + location + " (at depth " + location.size() + ")");
            throw new IndexOutOfBoundsException();
        }
        this.location = location;
    }
        // // // // // // // // // // // // // // // // // // //
      // // //  specific methods for the minimax tree // // //
    // // // // // // // // // // // // // // // // // // //
    public void setLocation(int depth, int branchNumber){ //switches branch number at depth
        setLocation(getLocation(depth, branchNumber));
    }

    public List<Integer> getLocation(int depth, int branchNumber){
        //depth 0 is adding to root, depth 1 is when location.size is 1, etc.
        if(depth > location.size() + 1 || depth < 0) { //if we try to add two or more
            show();
            throw new IndexOutOfBoundsException("Warning: no such depth " + depth + " in, or just after current location " + location);
        }
        List<Integer> newLocation = new ArrayList<Integer>(location);
        if(depth == location.size() + 1) { newLocation.add(branchNumber); }
        else { //depth here is only equal to location.size or smaller
            if(depth > 0) {
                newLocation = newLocation.subList(0, depth - 1);
                newLocation.add(branchNumber);
            }else{
                newLocation = new ArrayList<Integer>();
            }
        }
        return newLocation;
    }

    public List<Integer> prepareChild(int recursions, int branchID, double evaluation){
        setLocation(recursions, branchID);
        addNodeOnLocation(new Node(evaluation));
        return location;
    }

    public void specifyAndSetChild(List<Integer> location, int i, double moveValue){
        location.add(i); // specifies child of parent
        setLocation(location);
        setValueOnLocation(moveValue);
    }

    public void specifyAndSetParent(int recursions, int branchID, double evaluation){
        setLocation(recursions, branchID);
        setValueOnLocation(evaluation);
    }
        // // // // // // // // // // // // // // // // // // //
      // // // // // // // // // // // // // // // // // // //
    // // // // // // // // // // // // // // // // // // //

    //faster but please avoid using (unchecked)
    private void unsafeSetLocation(List<Integer> location) { this.location = location; }

    public void addNodeOnLocation(Node node){ //add to node at location's branches
        if(location.isEmpty()) { root.addNode(node); } //add to root if location is default
        else { atLocation.addNode(node); } //adds node as a child of specified node
    }

    public void setValueOnLocation(double value){
        atLocation.setValue(value);
    }

    public void show(){ root.show(); }

    public boolean equals(DoubleTree otherTree){
        //printing is not superfluous and is in fact useful for holistic debugging
        show();
        otherTree.show();
        return root.equals(otherTree.getNodeOnLocation());
    }
}
