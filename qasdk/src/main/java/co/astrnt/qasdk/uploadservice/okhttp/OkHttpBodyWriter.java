package co.astrnt.qasdk.uploadservice.okhttp;

import java.io.IOException;

import co.astrnt.qasdk.uploadservice.http.BodyWriter;
import okio.BufferedSink;

/**
 * @author Aleksandar Gotev
 */

public class OkHttpBodyWriter extends BodyWriter {

    private BufferedSink mSink;

    protected OkHttpBodyWriter(BufferedSink sink) {
        mSink = sink;
    }

    @Override
    public void write(byte[] bytes) throws IOException {
        mSink.write(bytes);
    }

    @Override
    public void write(byte[] bytes, int lengthToWriteFromStart) throws IOException {
        mSink.write(bytes, 0, lengthToWriteFromStart);
    }

    @Override
    public void flush() throws IOException {
        mSink.flush();
    }
}
