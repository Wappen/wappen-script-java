import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

public class Runner {

    public Object run(Tree<Token> ast) {
        return Scope.run(ast.getRoot(), null);
    }

    private static class Scope {
        private final Scope base;
        private final Map<Object, Object> variables;
        private final Map<Object, Tree.Node<Token>> functions;

        private Scope(Scope base) {
            this.base = base;
            variables = new HashMap<>();
            functions = new HashMap<>();
        }

        private Object getValue(Object varKey) {
            if (variables.containsKey(varKey)) {
                return variables.get(varKey);
            }

            if (base != null)
                return base.getValue(varKey);

            return null;
        }

        private Tree.Node<Token> getFunction(Object funKey) {
            if (functions.containsKey(funKey)) {
                return functions.get(funKey);
            }

            if (base != null)
                return base.getFunction(funKey);

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
                            return value;
                        }
                        case "!" -> {
                            Object varKey = evalArg(expression, 0);
                            return getValue(varKey);
                        }
                        case "?" -> {
                            Object condition = evalArg(expression, 0);
                            if (bool(condition)) {
                                return evalArg(expression, 1);
                            }
                            else if (expression.branches().size() == 3) {
                                return evalArg(expression, 2);
                            }
                        }
                        case "==" -> {
                            Set<Object> vals = new HashSet<>();
                            for (int i = 0; i < expression.branches().size(); i++) {
                                vals.add(evalArg(expression, i));
                            }
                            return vals.size() == 1;
                        }
                        case "!=" -> {
                            Set<Object> vals = new HashSet<>();
                            for (int i = 0; i < expression.branches().size(); i++) {
                                vals.add(evalArg(expression, i));
                            }
                            return vals.size() == expression.branches().size();
                        }
                        case ">=" -> {
                            return cascadeCompare(expression, vals -> vals.a() >= vals.b());
                        }
                        case "<=" -> {
                            return cascadeCompare(expression, vals -> vals.a() <= vals.b());
                        }
                        case ">" -> {
                            return cascadeCompare(expression, vals -> vals.a() > vals.b());
                        }
                        case "<" -> {
                            return cascadeCompare(expression, vals -> vals.a() < vals.b());
                        }
                        case "+" -> {
                            return cascadeEval(expression, vals -> vals.a() + vals.b());
                        }
                        case "-" -> {
                            return cascadeEval(expression, vals -> vals.a() - vals.b());
                        }
                        case "*" -> {
                            return cascadeEval(expression, vals -> vals.a() * vals.b());
                        }
                        case "/" -> {
                            return cascadeEval(expression, vals -> vals.a() / vals.b());
                        }
                        case "^" -> {
                            Object funKey = evalArg(expression, 0);
                            functions.put(funKey, expression.branches().get(1));
                        }
                        case "@" -> {
                            Object funKey = evalArg(expression, 0);
                            Tree.Node<Token> function = getFunction(funKey);
                            if (function != null) {
                                return Scope.run(function, this);
                            }
                            return null;
                        }
                    }
                }
                case LITERAL_STR -> {
                    String str = expression.value().string();
                    return str.substring(1, str.length() - 1); // Without ""
                }
                case LITERAL_NUM -> {
                    return Double.parseDouble(expression.value().string());
                }
                case NAME -> {
                    return expression.value().string();
                }
            }

            return null;
        }

        private static double num(Object value) {
            return switch (value) {
                case Double f -> f;
                case String str -> Double.parseDouble(str);
                default -> 0;
            };
        }

        private static boolean bool(Object value) {
            if (value == null)
                return false;

            return switch (value) {
                case Double f -> f != 0;
                case String str -> str.length() != 0;
                case Boolean b -> b;
                default -> true;
            };
        }

        private Object evalArg(Tree.Node<Token> expression, int index) {
            return Scope.run(expression.branches().get(index), this);
        }

        private boolean cascadeCompare(Tree.Node<Token> expression, Predicate<Tuple<Double, Double>> pred) {
            double a = num(evalArg(expression, 0));
            for (int i = 1; i < expression.branches().size(); i++) {
                double b = num(evalArg(expression, i));
                if (!pred.test(new Tuple<>(a, b)))
                    return false;
                a = b;
            }
            return true;
        }

        private double cascadeEval(Tree.Node<Token> expression, Function<Tuple<Double, Double>, Double> func) {
            double result = num(evalArg(expression, 0));
            for (int i = 1; i < expression.branches().size(); i++) {
                double arg = num(evalArg(expression, i));
                result = func.apply(new Tuple<>(result, arg));
            }
            return result;
        }

        private record Tuple<T, U>(T a, U b) {}

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
}
