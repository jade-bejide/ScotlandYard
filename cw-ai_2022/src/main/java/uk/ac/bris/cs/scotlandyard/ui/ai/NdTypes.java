package uk.ac.bris.cs.scotlandyard.ui.ai;

//Generic datastructures to allow storage of up to three different pieces of data
public class NdTypes<T, U, V> {
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
