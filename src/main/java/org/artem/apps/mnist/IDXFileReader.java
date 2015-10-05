package org.artem.apps.mnist;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.zip.GZIPInputStream;

/**
 * TODO: Document!
 *
 * @author artem
 *         Date: 10/5/15
 */
public class IDXFileReader {

    private GZIPInputStream gzIn;
    private FileInputStream in;

    private byte[] byteArray;
    private ByteBuffer buff;

    private int[] dimensions;

    public IDXFileReader(String fileName, int magicNumber, int numDimensions) throws IOException {
        in = new FileInputStream(fileName);
        if (fileName.endsWith(".gz"))
            gzIn = new GZIPInputStream(in);

        byteArray = new byte[256];
        buff = ByteBuffer.wrap(byteArray);
        buff.order(ByteOrder.BIG_ENDIAN);

        readBytes(4);
        int magic = buff.getInt();
        if (magic != magicNumber)
            throw new IOException("Unexpected magic number: " + magic);

        dimensions = new int[numDimensions];
        readBytes(4 * numDimensions);
        for (int i = 0; i < numDimensions; i++)
            dimensions[i] = buff.getInt();
    }

    public <T> T readAllData(Class<T> arrayType) throws IOException {
        Class<?> componentType = arrayType;
        for (int i = 0; i < dimensions.length; i++) {
            assert componentType.isArray();
            componentType = componentType.getComponentType();
        }
        assert componentType != null && !componentType.isArray();

        T res = arrayType.cast(Array.newInstance(componentType, dimensions));
        fillArray(res, 0, componentType);

        return res;
    }

    public void close() throws IOException {
        if (gzIn != null)
            gzIn.close();
        else
            in.close();
    }

    private void fillArray(Object array, int pos, Class<?> componentType) throws IOException {
        if (pos == dimensions.length - 1) {
            readArrayElements(array, componentType, dimensions[pos]);
            return;
        }

        for (int i = 0; i < dimensions[pos]; i++) {
            fillArray(Array.get(array, i), pos + 1, componentType);
        }
    }

    private void readArrayElements(Object array, Class<?> type, int len) throws IOException {
        int componentLen;
        ElementReader elementReader;
        if (type == Byte.TYPE) {
            componentLen = 1;
            elementReader = idx -> Array.set(array, idx, buff.get());
        } else if (type == Integer.TYPE) {
            componentLen = 4;
            elementReader = idx -> Array.setInt(array, idx, buff.getInt());
        } else {
            throw new RuntimeException("Unsupported data type: " + type);
        }

        int count = 0;
        while (len > 0) {
            int numElements = Math.min(len, byteArray.length / componentLen);
            readBytes(numElements * componentLen);
            for (int i = 0; i < numElements; i++)
                elementReader.readArrayElement(count++);
            len -= numElements;
        }
    }

    private void readBytes(int length) throws IOException {
        int res;
        if (gzIn != null)
            res = gzIn.read(byteArray, 0, length);
        else
            res = in.read(byteArray, 0, length);
        if (res <= 0)
            throw new IOException("Failed to read the data");

        buff.position(0);
    }

    private interface ElementReader {
        void readArrayElement(int idx);
    }
}
