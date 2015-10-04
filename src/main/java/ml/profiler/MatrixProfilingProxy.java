package ml.profiler;

import ml.vector.Matrix;
import ml.vector.MatrixFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * TODO: Document!
 *
 * @author artem
 *         Date: 10/4/15
 */
public class MatrixProfilingProxy implements InvocationHandler {

    private Object delegate;
    private Profiler profiler;

    public static MatrixFactory createMatrixFactoryProxy(MatrixFactory implementation) {
        return createProxy(MatrixFactory.class, implementation);
    }

    public MatrixProfilingProxy(Object delegate) {
        this.delegate = delegate;
        profiler = Profiler.getInstance();
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        profiler.reportMethodStart(delegate.getClass(), method);
        try {
            Object res = method.invoke(delegate, args);
            profiler.reportMethodEnd(method, true);

            if (res instanceof Matrix && !(res instanceof Proxy)) {
                res = createProxy(Matrix.class, (Matrix) res);
            }
            return res;
        } catch (Throwable e) {
            profiler.reportMethodEnd(method, false);
            throw e;
        }
    }

    private static <T> T createProxy(Class<T> interfaceClass, T implementation) {
        return interfaceClass.cast(
                Proxy.newProxyInstance(
                        implementation.getClass().getClassLoader(),
                        new Class[]{interfaceClass},
                        new MatrixProfilingProxy(implementation))
        );
    }
}
