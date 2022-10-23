import exceptions.IllegalSyntaxException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Stack;

public class Interpreter {
    private static final Stack<Path> runningScript = new Stack<>();

    public static void main(String... args) throws IllegalSyntaxException, IOException {
        if (args.length != 1) {
            System.err.printf("Expected exactly one argument. Got %d.%n", args.length);
            return;
        }


        Path script = Path.of(args[0]);
        System.out.printf("Program returned '%s'%n", runScript(script, new Runner.Scope(null)));
    }

    public static Object runScript(Path script, Runner.Scope scope) throws IllegalSyntaxException, IOException {
        if (script.isAbsolute())
            runningScript.push(script);
        else
            runningScript.push(runningScript.peek().getParent().resolve(script));

        Object result = runCode(Files.readString(runningScript.peek()), scope);

        runningScript.pop();
        return result;
    }

    public static Object runCode(String code, Runner.Scope scope) throws IllegalSyntaxException {
        Token[] tokens = tokenize(code);
        Tree<Token> ast = parse(tokens);
        return interpret(ast, scope);
    }

    private static Token[] tokenize(String code) throws IllegalSyntaxException {
        return new Tokenizer().tokenize(code);
    }

    private static Tree<Token> parse(Token[] tokens) {
        return new Parser().parse(tokens);
    }

    private static Object interpret(Tree<Token> ast, Runner.Scope scope) {
        return new Runner().run(ast, scope);
    }
}
