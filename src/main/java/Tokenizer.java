import exceptions.IllegalSyntaxException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Tokenizer {
    public Token[] tokenize(String code) throws IllegalSyntaxException {
        String[] words = split(code);
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

    private String[] split(String code) {
        List<String> words = new ArrayList<>();

        boolean inQuotes = false;
        boolean escaped = false;

        StringBuilder currentWord = new StringBuilder();

        for (int i = 0; i < code.length(); i++) {
            char c = code.charAt(i);

            switch (c) {
                case '"' -> {
                    if (!escaped) {
                        inQuotes ^= true;
                    }
                }
                case '\\' -> {
                    if (!escaped) {
                        escaped = true;
                        continue;
                    }
                }
                case ' ', '\r', '\n', '\t' -> {
                    if (!escaped && !inQuotes) {
                        String newWord = currentWord.toString().trim();
                        if (!newWord.isEmpty())
                            words.add(newWord);
                        currentWord = new StringBuilder();
                        continue;
                    }
                }
            }

            currentWord.append(c);
            escaped = false;
        }

        return words.toArray(new String[0]);
    }
}
