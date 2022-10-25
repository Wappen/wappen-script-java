public class Struct {
    private final Tree<Object> dataTree;

    private Struct(Tree<Object> dataTree) {
        this.dataTree = dataTree;
    }

    private static Tree.Node<Object> createTree(Tree.Node<Token> expression, Runner.Scope scope) {
        Tree.Node<Object> dataRoot = new Tree.Node<>(null);

        for (Tree.Node<Token> branch : expression.branches()) {
            if (branch.value().type() == Token.Type.STRUCT_START) {
                dataRoot.addBranch(Struct.createTree(branch, scope));
            }
            else {
                dataRoot.addBranch(new Tree.Node<>(Runner.Scope.run(branch, scope)));
            }
        }

        return dataRoot;
    }

    public static Struct fromExpression(Tree.Node<Token> expression, Runner.Scope scope) {
        Tree<Object> dataTree = new Tree<>();
        Tree.Node<Object> dataRoot = createTree(expression, scope);
        dataTree.setRoot(dataRoot);

        return new Struct(dataTree);
    }

    @Override
    public String toString() {
        return dataTree.getRoot().toString();
    }
}
