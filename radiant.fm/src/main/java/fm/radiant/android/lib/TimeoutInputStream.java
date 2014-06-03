package fm.radiant.android.lib;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class TimeoutInputStream extends BufferedInputStream {
    private ExecutorService mReadLimiter = Executors.newSingleThreadExecutor();
    private int mTimeout;

    public TimeoutInputStream(final InputStream inputStream, final int timeout) {
        super(inputStream);
        mTimeout = timeout;
    }

    @Override
    public int read(final byte[] buffer, final int offset, final int count) throws IOException {
        Callable<Integer> readTask = new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return TimeoutInputStream.super.read(buffer, offset, count);
            }
        };

        try {
            return mReadLimiter.submit(readTask).get(mTimeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            throwOnTimeout();
        } catch (ExecutionException e) {
            throwOnTimeout();
        } catch (TimeoutException e) {
            throwOnTimeout();
        }

        return -1;
    }

    @Override
    public void close() throws IOException {
        mReadLimiter.shutdownNow();
        super.close();
    }

    private InterruptedIOException throwOnTimeout() throws InterruptedIOException {
        throw new InterruptedIOException();
    }
}