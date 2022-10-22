public class Runtime {

    public Object run(Tree<Token> ast) {
        return Scope.run(ast.getRoot(), null);
    }
}
