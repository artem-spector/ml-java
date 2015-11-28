package org.artem.tools.file;

import com.jmatio.types.MLArray;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

/**
 * TODO: Document!
 *
 * @author artem
 *         Date: 11/28/15
 */
public interface MLExternalizable {

    void toMLData(String prefix, Collection<MLArray> out);

    void fromMLData(String prefix, Map<String, MLArray> in) throws IOException;
}
