package uk.ac.cam.cl.ks828.exercises.tick12;

/**
 * Created by KSarm on 08/03/2018.
 */
public class Edge {
    Integer mNode;
    Integer mNeighbour;

    public Edge(Integer node, Integer neighbour) {
        this.mNode = node;
        this.mNeighbour = neighbour;
    }

    public Integer getNode() {
        return mNode;
    }

    public Integer getNeighbour() {
        return mNeighbour;
    }
}
