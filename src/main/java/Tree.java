import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Tree<T> {
    private Node<T> root;

    public Node<T> getRoot() {
        return root;
    }

    public void setRoot(Node<T> root) {
        this.root = root;
    }

    public static final class Node<T> {
        private final List<Node<T>> branches;
        private T value;

        public Node(T value) {
            this.branches = new ArrayList<>();
            this.value = value;
        }

        public void addBranch(Node<T> branch) {
            branches.add(branch);
        }

        public List<Node<T>> branches() {
            return branches;
        }

        public T value() {
            return value;
        }

        public void setValue(T value) {
            this.value = value;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (Node) obj;
            return Objects.equals(this.branches, that.branches) &&
                    Objects.equals(this.value, that.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(branches, value);
        }

        @Override
        public String toString() {
            return "Node[" +
                    "branches=" + branches + ", " +
                    "value=" + value + ']';
        }
    }
}
