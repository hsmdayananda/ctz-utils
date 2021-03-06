package ru.concerteza.util.io.noclose;


import java.io.IOException;
import java.io.OutputStream;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Output stream transparent wrapper, {@link java.io.OutputStream#close()}
 * and {@link java.io.OutputStream#flush()} overriden as NOOP.
 * May be used to prevent rough libs from closing or flushing your streams
 *
 * @author alexey
 * Date: 11/19/11
 */
public class NoCloseOutputStream extends OutputStream {
    protected final OutputStream target;

    /**
     * @param target target stream
     */
    public NoCloseOutputStream(OutputStream target) {
        checkNotNull(target, "Provided output stream is null");
        this.target = target;
    }

    /**
     * Factory method
     *
     * @param target target stream
     * @return NoCloseOutputStream instance
     */
    public static NoCloseOutputStream of(OutputStream target) {
        return new NoCloseOutputStream(target);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(int b) throws IOException {
        target.write(b);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        target.write(b, off, len);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void flush() throws IOException {
        // this line is intentionally left blank
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException {
        // this line is intentionally left blank
    }
}
