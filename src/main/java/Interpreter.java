import exceptions.IllegalSyntaxException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Optional;

public class Interpreter {
    public static void main(String... args) throws IllegalSyntaxException, IOException {
        if (args.length != 1) {
            System.err.printf("Expected exactly one argument. Got %d.%n", args.length);
            return;
        }
        runCode(Files.readString(Path.of(args[0])));
    }

    private static void runCode(String code) throws IllegalSyntaxException {
        Token[] tokens = tokenize(code);
        Tree<Token> ast = parse(tokens);
        System.out.printf("Program returned '%s'%n", interpret(ast));
    }

    private static Token[] tokenize(String code) throws IllegalSyntaxException {
        String[] words = code.split("\\s(?=([^\"]*\"[^\"]*\")*[^\"]*$)"); // Every space outside of quotes
        ArrayList<Token> tokens = new ArrayList<>();

        for (String word : words) {
            if (word.isEmpty())
                continue;

            Optional<Token.Type> type = Token.Type.from(word);

            if (type.isEmpty()) {
                throw new IllegalSyntaxException(String.format("'%s' did not match any token type.", word));
            }

            tokens.add(new Token(word, type.get()));
        }

        return tokens.toArray(new Token[0]);
    }

    private static Tree<Token> parse(Token[] tokens) {
        return new Parser().parse(tokens);
    }

    private static Object interpret(Tree<Token> ast) {
        return new Runner().run(ast);
    }
}
