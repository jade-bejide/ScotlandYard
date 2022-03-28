package uk.ac.bris.cs.scotlandyard.ui.ai;

public class NdTypes<T, U, V> {
    private static class Id<T> {
        private final T a;

        Id(T a) {
            this.a = a;
        }

        public T getFirst() {
            return a;
        }
    }


    private static class Tuple<T, U> extends Id<T>{
        private final U b;

        Tuple(T a, U b){
            super(a);
            this.b = b;
        }

        public U getMiddle() {
            return b;
        }

    }

    static class Triple<T, U, V> extends Tuple<T, U>{
        private final V c;

        Triple(T a, U b, V c){
            super(a, b);
            this.c = c;
        }

        public V getLast() {
            return c;
        }
    }


//    public Id<T> getNewID(T t) {
//        return new Id<T>(t);
//    }
//
//    public Tuple<T, U> getNewTuple(T t, U u) {
//        return new Tuple<T, U>(t, u);
//    }
//
//    public Triple<T, U, V> getNewTriple(T t, U u, V v) {
//        return new Triple<T, U, V>(t, u, v);
//    }
}
