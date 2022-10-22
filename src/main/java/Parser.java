import java.util.Stack;

public class Parser {
    Stack<Tree.Node<Token>> workingStack;

    public Tree<Token> parse(Token[] tokens) {
        workingStack = new Stack<>();
        workingStack.push(new Tree.Node<>(null));

        for (Token currentToken : tokens) {
            switch (currentToken.type()) {
                case OPERATOR -> {
                    workingStack.peek().setValue(currentToken);
                }
                case LITERAL_STR, LITERAL_NUM, NAME -> {
                    Tree.Node<Token> node = new Tree.Node<>(currentToken);
                    workingStack.peek().addBranch(node);
                }
                case SCOPE_IN -> {
                    Tree.Node<Token> node = new Tree.Node<>(null);
                    workingStack.peek().addBranch(node);
                    workingStack.push(node);
                }
                case SCOPE_OUT -> {
                    workingStack.pop();
                }
            }
        }

        Tree<Token> tree = new Tree<>();
        tree.setRoot(workingStack.pop());
        return tree;
    }
}
