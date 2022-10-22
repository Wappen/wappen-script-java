import exceptions.IllegalSyntaxException;

import java.util.ArrayList;
import java.util.Optional;

public class Program {
    public static void main(String... args) throws IllegalSyntaxException {
        Token[] tokens = tokenize(
                """
                        ( a = ( 3 + ( 7 * 9 ) ) )
                        ( b = ( + 10 ( a ! ) ) )
                        ( b ! )""");
        Tree<Token> ast = parse(tokens);

        System.out.println("Parse complete.");

        new Runtime().run(ast);
    }

    private static Token[] tokenize(String code) throws IllegalSyntaxException {
        String[] words = code.split("\\s+");
        ArrayList<Token> tokens = new ArrayList<>();

        for (String word : words) {
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
}
