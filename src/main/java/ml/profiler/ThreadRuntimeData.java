package ml.profiler;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * TODO: Document!
 *
 * @author artem
 *         Date: 10/4/15
 */
public class ThreadRuntimeData {

    private static class MethodCall {
        public final Method method;
        public final long startNano;

        public MethodCall(Method method, long startNano) {
            this.method = method;
            this.startNano = startNano;
        }
    }

    private ThreadLocal<Stack<MethodCall>> callChain = new ThreadLocal<>();

    public void methodStart(Method method) {
        Stack<MethodCall> stack = callChain.get();
        if (stack == null) {
            stack = new Stack<>();
            callChain.set(stack);
        }

        stack.push(new MethodCall(method, System.nanoTime()));
    }

    public void methodEnd(Method method) {
        assert callChain.get().peek().method.equals(method);

    }
}
