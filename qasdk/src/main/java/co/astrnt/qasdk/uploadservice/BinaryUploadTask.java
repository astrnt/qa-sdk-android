package co.astrnt.qasdk.uploadservice;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import co.astrnt.qasdk.uploadservice.http.BodyWriter;

/**
 * Implements a binary file upload task.
 *
 * @author cankov
 * @author gotev (Aleksandar Gotev)
 */
public class BinaryUploadTask extends HttpUploadTask {

    @Override
    protected long getBodyLength() throws UnsupportedEncodingException {
        return params.files.get(0).length(service);
    }

    @Override
    public void onBodyReady(BodyWriter bodyWriter) throws IOException {
        bodyWriter.writeStream(params.files.get(0).getStream(service), this);
    }

    @Override
    protected void onSuccessfulUpload() {
        addSuccessfullyUploadedFile(params.files.get(0));
    }
}
