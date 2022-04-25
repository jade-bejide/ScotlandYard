package uk.ac.bris.cs.scotlandyard.ui.ai;

import java.util.ArrayList;
import java.util.List;

public class Node {
    private double value;
    private final List<Node> branches;

    public Node(double value, List<Node> branches) {
        this.value = value;
        this.branches = branches;
    }

    public Node(double value) {
        this.value = value;
        this.branches = new ArrayList<Node>();
    }

    public List<Node> getBranches() {
        return branches;
    }

    public Double getValue() {
        return value;
    }
    public void setValue(double value) { this.value = value; }
    //public void setBranches(List<Node> branches) { this.branches = branches; }

    public void addNode(Node node) {
        branches.add(node);
    }
    public void pruneAllChildren(){ branches.clear(); }

    public boolean equals(Node otherTree){ //tries to return as soon as possible to save computation
        if(otherTree.getValue() != value) return false;
        List<Node> otherBranches = otherTree.getBranches();
        if(branches.size() != otherBranches.size()) return false;
        for(int i = 0; i < branches.size(); i++){
            Node thisBranch = branches.get(i);
            Node otherBranch = otherBranches.get(i);
            if(!thisBranch.equals(otherBranch)) { return false; }
        }
        return true;
    }

    public String toString(){
        return "[" + value + "]";
    }

    // pretty printer
    public void show() {
        this.show("", true);
    }

    private void show(String depth, boolean terminal) {
        System.out.print(depth);
        if (depth.length() > 0) System.out.print(terminal ? "└" : "├");
        System.out.print("[" + this.value + "]\n");
        depth += "  ";
        if (depth.length() > 2) {
            char[] chars = (depth + " ").toCharArray();
            chars[depth.length() - 2] = terminal ? ' ' : '│';
            depth = String.valueOf(chars);
        }
        for (int i = 0; i < branches.size(); i++) {
            terminal = (i == branches.size() - 1);
            branches.get(i).show(depth, terminal);
        }
    }
}

