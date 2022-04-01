package uk.ac.bris.cs.scotlandyard.ui.ai;

import java.util.List;

public class DoubleTree {
    private Node root;

    public DoubleTree(double value, List<Node> branches){
        this.root = new Node(value, branches);
    }

    public static class Node{
        private final double value;
        private List<Node> branches;

        public Node(double value, List<Node> branches){
            this.value = value;
            this.branches = branches;
        }

        public List<Node> getBranches() { return branches; }
        public Double getValue(){ return value; }
        //public void setBranches(List<Node> branches) { this.branches = branches; }

        public void addNode(Node node){ branches.add(node); }
        public String toString(){
//            String result = "|" + value;
//            for(Node n : branches){
//                result += n.getValue() + ", ";
//            }
//            //result += "|\n";
//            for(Node n : branches) {
//                result += n.toString();
//            }
//            result += "|";
//            return result;
            return "string";
        }
    }

    public String toString(){
//        String result = "|" + root.getValue().toString();
//        result += "|\n";
//        for(Node n : node){
//            result += n.getValue() + ", ";
//        }
//        for(Node n : root.getBranches()){
//            result += n.toString();
//        }
//        return result;
        return "string";
    }
}
