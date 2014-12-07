package ru.concerteza.util.io.copying;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * {@link CopyingInputStream} implementation, copies unread source bytes until EOF on
 * {@code close()} call. Use this class if client closes input stream before EOF,
 * but you need full input copy
 *
 * @author alexey
 * Date: 12/6/11
 * @see FullCopyingInputStreamTest
 */
public class FullCopyingInputStream extends CopyingInputStream {

    private final AtomicBoolean closed = new AtomicBoolean(false);

    /**
     * @param source source input stream
     * @param copy stream to copy read bytes into, will NOT be closed on EOF or {@code close()}
     */
    public FullCopyingInputStream(InputStream source, OutputStream copy) {
        super(source, copy);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException {
        if (closed.get()) return;
        try {
            IOUtils.copyLarge(source, copy);
        } finally {
            IOUtils.closeQuietly(source);
            closed.set(true);
        }
    }
}
