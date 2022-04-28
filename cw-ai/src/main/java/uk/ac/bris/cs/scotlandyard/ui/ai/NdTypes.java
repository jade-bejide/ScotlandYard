package uk.ac.bris.cs.scotlandyard.ui.ai;

//Generic datastructures to allow storage of up to three different pieces of data
//the main use of this class was to test the different components of the Dijkstra class's behaviour
public class NdTypes {
    public static class Id<T> {
        private final T a;

        Id(T a) {
            this.a = a;
        }

        public T getFirst() {
            return a;
        }
    }


    public static class Tuple<T, U> extends Id<T>{
        private final U b;

        Tuple(T a, U b){
            //define a via the superclass Id
            super(a);
            this.b = b;
        }

        public U getMiddle() {
            return b;
        }

    }

    public static class Triple<T, U, V> extends Tuple<T, U>{
        private final V c;

        Triple(T a, U b, V c){
            super(a, b);
            this.c = c;
        }

        public V getLast() {
            return c;
        }
    }


}
