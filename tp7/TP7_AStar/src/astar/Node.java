package astar;

/**
 * Représente un nœud dans le graphe pondéré.
 * h(n) n'est pas stockée en dur : elle est injectée depuis une source externe (HeuristicTable)
 * pour respecter le principe de séparation des responsabilités (SRP).
 */
public class Node implements Comparable<Node> {
    private final String name;
    private double g;   // coût réel depuis la source
    private double h;   // heuristique (injectée depuis l'extérieur)
    private double f;   // f = g + h
    private Node parent;

    public Node(String name) {
        this.name = name;
        this.g = Double.MAX_VALUE;
        this.h = 0;
        this.f = Double.MAX_VALUE;
        this.parent = null;
    }

    // Injection de h depuis la source externe
    public void setH(double h) {
        this.h = h;
    }

    public void setG(double g) {
        this.g = g;
        this.f = this.g + this.h;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    public String getName()  { return name; }
    public double getG()     { return g; }
    public double getH()     { return h; }
    public double getF()     { return f; }
    public Node   getParent(){ return parent; }

    @Override
    public int compareTo(Node other) {
        return Double.compare(this.f, other.f);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Node)) return false;
        Node node = (Node) o;
        return name.equals(node.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return String.format("%s(f=%.0f)", name, f);
    }
}
