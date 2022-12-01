package co.astrnt.qasdk.uploadservice.http.impl;


import java.io.IOException;
import java.io.OutputStream;

import co.astrnt.qasdk.uploadservice.http.BodyWriter;

/**
 * @author Aleksandar Gotev
 */

public class HurlBodyWriter extends BodyWriter {

    private OutputStream mOutputStream;

    public HurlBodyWriter(OutputStream outputStream) {
        mOutputStream = outputStream;
    }

    @Override
    public void write(byte[] bytes) throws IOException {
        mOutputStream.write(bytes);
    }

    @Override
    public void write(byte[] bytes, int lengthToWriteFromStart) throws IOException {
        mOutputStream.write(bytes, 0, lengthToWriteFromStart);
    }

    @Override
    public void flush() throws IOException {
        mOutputStream.flush();
    }
}
