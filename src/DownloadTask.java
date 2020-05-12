package src;

import javafx.concurrent.Task;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.URL;
import java.net.URLConnection;

/**
 * Task class to download files from URL
 *
 * @author Bheem Suttipong
 */
public class DownloadTask extends Task<Long> {
    private String urlName;
    final int BUFFER_SIZE = 16 * 1024;
    private File file;
    private long start;
    private long size;
    private InputStream in;

    /**
     * Constructor for DownloadTask class
     */
    public DownloadTask(String urlName, File file, long start, long size) {
        this.urlName = urlName;
        this.file = file;
        this.start = start;
        this.size = size;
    }

    /**
     * Invoked when the Task is executed
     */
    @Override
    protected Long call() throws Exception {
        URL url = new URL(urlName);
        URLConnection conn = url.openConnection();
        String range = String.format("bytes=%d-%d", start, start + size - 1);
        conn.setRequestProperty("Range" , range);
        byte[] buffer = new byte[BUFFER_SIZE];
        in = conn.getInputStream();
        RandomAccessFile writer = new RandomAccessFile(file, "rwd");
        writer.seek(start);
        long bytesRead = 0L;
        do {
            int n = in.read(buffer);
            if (n < 0) break;
            writer.write(buffer, 0, n);
            bytesRead += n;

            updateProgress(bytesRead, size - 1);
            updateValue(bytesRead);
        } while (bytesRead < size);
        in.close();
        writer.close();

        return bytesRead;
    }

    /**
     * Terminates execution of this Worker.
     */
    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        try {
            in.close();
        } catch (IOException ignored) {
        }
        return super.cancel(mayInterruptIfRunning);
    }
}
