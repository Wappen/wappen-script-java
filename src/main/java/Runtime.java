public class Runtime {

    public void run(Tree<Token> ast) {
        Scope.run(ast.getRoot(), null);
    }
}
