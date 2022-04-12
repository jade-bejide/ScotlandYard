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
            System.err.println("Warning: no such child at location " + location + " (at depth " + location.size() + ")");
            throw new IndexOutOfBoundsException();
        }
        this.location = location;
    }

    //faster but please avoid using (unchecked)
    public void unsafeSetLocation(List<Integer> location) { this.location = location; }

    public void setLocationOnDepthAndID(int depth, int ID){
        if(depth > 0) {
            List<Integer> newLocation = location.subList(0, depth - 1);
            newLocation.add(ID);
            setLocation(newLocation);
            //System.out.println("Depth " + depth + " ID " + ID + " => new location: " + location);
        } else { setLocation(new ArrayList<Integer>()); }
    }

    public void addNodeOnLocation(Node node){ //add to node at location's branches
        if(location.isEmpty()) { root.addNode(node); } //add to root if location is default
        else { atLocation.addNode(node); } //adds node as a child of specified node
    }

    public void setValueOnLocation(double value){
        System.out.println("Set value of node at location " + this.location + " to " + value);
        atLocation.setValue(value);
    }

    public void show(){ root.show(); }

    public boolean equals(DoubleTree otherTree){
        show();
        otherTree.show();
        return root.equals(otherTree.getNodeOnLocation());
    }
}
