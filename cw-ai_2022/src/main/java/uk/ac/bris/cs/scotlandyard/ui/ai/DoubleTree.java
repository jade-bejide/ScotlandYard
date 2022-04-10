package uk.ac.bris.cs.scotlandyard.ui.ai;

import java.util.ArrayList;
import java.util.List;

public class DoubleTree {
    private final Node root;
    private List<Integer> location; // selects the branch to go down from root {branch #, branch #, ...}
    private Node atLocation; // the node at location

    public DoubleTree(Node root) {
        this.root = root;
        this.location = new ArrayList<Integer>();
    }

    public DoubleTree(){
        this.root = new Node(-1);
        this.location = new ArrayList<Integer>();
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

    public boolean setLocation(List<Integer> location){ //returns its success
        try{
            atLocation = navigateTo(location);
        }catch(IndexOutOfBoundsException e){
            System.err.println("Warning: no such node as " + location + " at depth " + location.size());
            return false;
        }
        this.location = location;
        return true;
    }

    public void add(Node node){ //add to node at location's branches
        if(location.isEmpty()) { root.addNode(node); } //add to root if location is default
        else { atLocation.addNode(node); } //adds node as a child of specified node
    }

    public void setValue(double value){ atLocation.setValue(value); }

    public void show(){ root.show(); }

    public boolean equals(DoubleTree tree){
        return true;
    }
}
