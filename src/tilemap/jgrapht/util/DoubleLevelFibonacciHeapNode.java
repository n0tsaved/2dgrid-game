package tilemap.jgrapht.util;

/**
 * Created by notsaved on 4/22/17.
 */
public class DoubleLevelFibonacciHeapNode<T> extends FibonacciHeapNode<T>  {

    protected double secondKey;

    /**
     * Constructs a new node.
     *
     * @param data data for this node
     */
    public DoubleLevelFibonacciHeapNode(T data) {
        super(data);
    }
}
