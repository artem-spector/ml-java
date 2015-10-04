package ml.profiler;

import java.lang.reflect.Method;
import java.util.*;

/**
 * TODO: Document!
 *
 * @author artem
 *         Date: 10/4/15
 */
public class Profiler {

    private boolean collecting;
    private long startTime;
    private long endTime;

    private static class MethodCall {
        public final Class targetClass;
        public final Method method;
        public final String id;
        public final long startMillis;


        public MethodCall(Class targetClass, Method method, long startMillis) {
            this.targetClass = targetClass;
            this.method = method;
            this.startMillis = startMillis;
            this.id = targetClass.getName() + "." + method.getName() + method.getParameterTypes().length;
        }
    }

    private ThreadLocal<Stack<MethodCall>> callChain;
    private ThreadLocal<Map<String, MethodStatistics>> callStat;
    private List<Map<String, MethodStatistics>> allStatistics;

    private static final Profiler instance = new Profiler();

    public static Profiler getInstance() {
        return instance;
    }

    public void startCollecting() {
        callChain = new ThreadLocal<>();
        callStat = new ThreadLocal<>();
        allStatistics = new ArrayList<>();
        startTime = System.currentTimeMillis();
        endTime = 0;
        collecting = true;
    }

    public void stopCollecting() {
        collecting = false;
        endTime = System.currentTimeMillis();
        for (Map<String, MethodStatistics> statisticsMap : allStatistics) {
            for (MethodStatistics methodStatistics : statisticsMap.values()) {
                methodStatistics.stopCounting();
            }
        }
        callChain = null;
        callStat = null;
    }

    public boolean isCollecting() {
        return collecting;
    }

    public void reportMethodStart(Class targetClass, Method method) {
        if (!collecting) return;

        Stack<MethodCall> stack = callChain.get();
        if (stack == null) {
            stack = new Stack<>();
            callChain.set(stack);
        }
        stack.push(new MethodCall(targetClass, method, System.currentTimeMillis()));
    }

    public void reportMethodEnd(Method method, boolean success) {
        if (!collecting) return;
        assert callChain.get().peek().method.equals(method);

        MethodCall call = callChain.get().pop();
        long duration = System.currentTimeMillis() - call.startMillis;

        Map<String, MethodStatistics> statisticsMap = callStat.get();
        if (statisticsMap == null) {
            statisticsMap = new HashMap<>();
            callStat.set(statisticsMap);
            allStatistics.add(statisticsMap);
        }

        MethodStatistics methodStatistics = statisticsMap.get(call.id);
        if (methodStatistics == null) {
            methodStatistics = new MethodStatistics(call.targetClass, call.method);
            statisticsMap.put(call.id, methodStatistics);
        }
        methodStatistics.reportCall(duration);
    }

    public Map<String, MethodStatistics> getStatistics() {
        assert !collecting;
        if (allStatistics.size() == 0) return null;
        if (allStatistics.size() > 1)
            System.out.println("WARNING: more than one thread statistics, implement aggregation!");
        return allStatistics.iterator().next();
    }

    public String getReportStr() {
        assert !collecting;
        StringBuilder str = new StringBuilder("Total test duration: ").append(endTime - startTime).append("\n");
        str.append(MethodStatistics.getHeadStr()).append("\n");

        List<MethodStatistics> sorted = new ArrayList<>(getStatistics().values());
        sorted.sort((o1, o2) -> (int) (o2.getCumulativeTime() - o1.getCumulativeTime()));
        for (MethodStatistics statistics : sorted) {
            str.append(statistics.toFormattedString()).append("\n");
        }

        return str.toString();
    }
}
