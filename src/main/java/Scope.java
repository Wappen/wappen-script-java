import java.util.HashMap;
import java.util.Map;

public class Scope {
    private final Scope base;
    private final Map<Object, Object> variables;

    private Scope(Scope base) {
        this.base = base;
        variables = new HashMap<>();
    }

    private Object getValue(Object varKey) {
        if (variables.containsKey(varKey)) {
            return variables.get(varKey);
        }

        if (base != null)
            return base.getValue(varKey);

        return null;
    }

    private Object evaluate(Tree.Node<Token> expression) {
        switch (expression.value().type()) {
            case OPERATOR -> {
                switch (expression.value().string()) {
                    case "=" -> {
                        Object varKey = evalArg(expression, 0);
                        Object value = evalArg(expression, 1);
                        variables.put(varKey, value);
                        System.out.printf("Set variable '%s' to '%s'%n", varKey, value);
                        return value;
                    }
                    case "!" -> {
                        Object varKey = evalArg(expression, 0);
                        Object value = getValue(varKey);
                        System.out.printf("Return value for '%s' as '%s'%n", varKey, value);
                        return value;
                    }
                    case "+" -> {
                        Object value1 = evalArg(expression, 0);
                        Object value2 = evalArg(expression, 1);
                        return number(value1) + number(value2);
                    }
                    case "-" -> {
                        Object value1 = evalArg(expression, 0);
                        Object value2 = evalArg(expression, 1);
                        return number(value1) - number(value2);
                    }
                    case "*" -> {
                        Object value1 = evalArg(expression, 0);
                        Object value2 = evalArg(expression, 1);
                        return number(value1) * number(value2);
                    }
                    case "/" -> {
                        Object value1 = evalArg(expression, 0);
                        Object value2 = evalArg(expression, 1);
                        return number(value1) / number(value2);
                    }
                }
            }
            case NUMBER, NAME -> {
                return expression.value().string();
            }
        }

        return null;
    }

    private static float number(Object value) {
        return switch (value) {
            case Float f -> f;
            case String str -> Float.parseFloat(str);
            default -> 0;
        };
    }

    private Object evalArg(Tree.Node<Token> expression, int index) {
        return Scope.run(expression.branches().get(index), this);
    }

    public static Object run(Tree.Node<Token> statements, Scope base) {
        Scope scope = new Scope(base);

        if (statements.value() == null) {
            Object lastValidValue = null;

            for (Tree.Node<Token> statement : statements.branches()) {
                Object tmp = scope.evaluate(statement);
                lastValidValue = tmp == null ? lastValidValue : tmp;
            }

            return lastValidValue;
        }
        else {
            return scope.evaluate(statements);
        }
    }
}
