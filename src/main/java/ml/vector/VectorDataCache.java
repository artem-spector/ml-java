package ml.vector;

import java.util.HashMap;
import java.util.Map;

/**
 * TODO: Document!
 *
 * @author artem
 *         Date: 10/4/15
 */
public class VectorDataCache {

    public interface DataFetcher {
        double[] produceData(int key);
    }

    private Map<Integer, double[]> cache = new HashMap<>();
    private DataFetcher fetcher;

    public VectorDataCache(DataFetcher fetcher) {
        this.fetcher = fetcher;
    }

    public double[] get(int key) {
        double[] res = cache.get(key);
        if (res == null) {
            res = fetcher.produceData(key);
            cache.put(key, res);
        }

        return res;
    }

}
