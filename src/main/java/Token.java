import java.util.Optional;
import java.util.regex.Pattern;

public record Token(String string, Type type) {
    public enum Type {
        OPERATOR("\\+", "\\-", "\\*", "\\/",
                "=", "!", "\\?", "\\|", "&",
                "==", "!=", "\\<=", "\\>=", "\\<", "\\>",
                "\\^", "@", "#", "~"),
        LITERAL_STR("\"(?s:.*)(?<!\\\\)\""),
        LITERAL_NUM("\\d+"),
        SCOPE_IN("\\("),
        SCOPE_OUT("\\)"),
        IDENTIFIER(".+");

        private final Pattern pattern;

        Type(String... pattern) {
            final String escapedPattern = "(?<!\\\\)";
            this.pattern = Pattern.compile(escapedPattern + String.join("|", pattern));
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
