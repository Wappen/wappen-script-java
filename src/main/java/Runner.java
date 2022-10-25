import exceptions.IllegalSyntaxException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

public class Runner {
    public Object run(Tree<Token> ast, Scope scope) {
        return Scope.run(ast.getRoot(), scope);
    }

    public static class Scope {
        private final Scope base;
        private final Map<Object, Object> variables;
        private final Map<Object, Tree.Node<Token>> functions;

        Scope(Scope base) {
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
                        case "|" -> {
                            return cascadeBool(expression, vals -> vals.a() | vals.b());
                        }
                        case "&" -> {
                            return cascadeBool(expression, vals -> vals.a() & vals.b());
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
                        }
                        case "#" -> {
                            try {
                                return cascadeInclude(expression);
                            } catch (IOException | IllegalSyntaxException e) {
                                throw new RuntimeException(e);
                            }
                        }
                        case "~" -> {
                            int num = (int) num(evalArg(expression, 0));
                            Object[] args = new Object[expression.branches().size() - 1];
                            for (int i = 0; i < args.length; i++) {
                                args[i] = evalArg(expression, i + 1);
                            }
                            return CStdLib.INSTANCE.syscall(num, args);
                        }
                    }
                }
                case LITERAL_STR -> {
                    String str = expression.value().string();
                    return str.substring(1, str.length() - 1); // Exclude ""
                }
                case LITERAL_NUM -> {
                    return Double.parseDouble(expression.value().string());
                }
                case IDENTIFIER -> {
                    return expression.value().string();
                }
                case STRUCT_START -> {
                    return Struct.fromExpression(expression, this);
                }
            }

            return null;
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

        private boolean cascadeBool(Tree.Node<Token> expression, Function<Tuple<Boolean, Boolean>, Boolean> func) {
            boolean result = bool(evalArg(expression, 0));
            for (int i = 1; i < expression.branches().size(); i++) {
                boolean arg = bool(evalArg(expression, i));
                result = func.apply(new Tuple<>(result, arg));
            }
            return result;
        }

        private Object cascadeInclude(Tree.Node<Token> expression) throws IOException, IllegalSyntaxException {
            Object lastValidValue = null;

            for (int i = 0; i < expression.branches().size(); i++) {
                String param = str(evalArg(expression, i));

                Object tmp;
                if (param.contains("\n")) { // Decide whether it should include a file or execute the param directly
                    tmp = Interpreter.runCode(param, this);
                }
                else {
                    tmp = Interpreter.runScript(Path.of(param), this);
                }
                lastValidValue = tmp == null ? lastValidValue : tmp;
            }

            return lastValidValue;
        }

        private record Tuple<T, U>(T a, U b) {}

        private static double num(Object value) {
            return switch (value) {
                case Double f -> f;
                case String str -> Double.parseDouble(str);
                case Boolean b -> b ? 1 : 0;
                default -> 0;
            };
        }

        private static String str(Object value) {
            return switch (value) {
                case Double f -> f.toString();
                case String str -> str.replaceAll("(?<!\\\\)\\\\", ""); // Remove all escape sequences
                case Boolean b -> b ? "true" : "false";
                default -> "";
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

        public static Object run(Tree.Node<Token> statements, Scope scope) {
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
