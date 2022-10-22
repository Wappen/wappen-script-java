import java.util.Optional;
import java.util.regex.Pattern;

public record Token(String string, Type type) {
    public enum Type {
        OPERATOR("\\+|\\-|\\*|\\/|=|!|\\?|==|\\<=|\\>=|\\<|\\>|\\^|@"),
        LITERAL("\\d+|\".*\""),
        SCOPE_IN("\\("),
        SCOPE_OUT("\\)"),
        NAME(".+");

        private final Pattern pattern;

        Type(String pattern) {
            this.pattern = Pattern.compile(pattern);
        }

        public static Optional<Type> from(String str) {
            for (Type type : values()) {
                if (type.pattern.matcher(str).matches()) {
                    return Optional.of(type);
                }
            }

            return Optional.empty();
        }
    }
}
