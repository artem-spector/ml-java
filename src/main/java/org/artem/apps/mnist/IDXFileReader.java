package org.artem.apps.mnist;

import java.io.FileInputStream;
import java.io.IOException;
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
    private int lastPos;
    private int currPos;

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

    public int[] getDimensions() {
        return dimensions;
    }

    public void close() throws IOException {
        if (gzIn != null)
            gzIn.close();
        else
            in.close();
    }


    public byte readByte() throws IOException {
        if (currPos == lastPos) {
            int res;
            if (gzIn != null)
                res = gzIn.read(byteArray, 0, byteArray.length);
            else
                res = in.read(byteArray, 0, byteArray.length);

            if (res <= 0)
                throw new IOException("Failed to read the data");

            lastPos = res;
            currPos = 0;
        }

        return byteArray[currPos++];
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

}
