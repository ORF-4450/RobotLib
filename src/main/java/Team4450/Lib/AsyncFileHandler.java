package Team4450.Lib;

import java.io.IOException;
import java.lang.System.Logger.Level;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.FileHandler;
import java.util.logging.LogRecord;

//  This class was found on the net.

/**
 * Wraps FileHandler class to Implement multi-threaded logging. Queues log records
 * submitted by calling thread and writes them to disk file on a separate
 * thread. Frees calling thread from the I/O overhead.
 */
public class AsyncFileHandler extends FileHandler implements Runnable 
{
    private static final int	offValue = Level.OFF.ordinal();
    private final				BlockingQueue<LogRecord> queue = new ArrayBlockingQueue<>(1024);
    private volatile Thread		worker;

    /**
     * See Java FileHandler class for parameter documentation.
     */
    public AsyncFileHandler() throws IOException 
    {
        super();
    }

    /**
     * See Java FileHandler class for parameter documentation.
     */
    public AsyncFileHandler(String pattern, int limit, int count) throws IOException 
    {
        super(pattern, limit, count);
    }

    /**
     * See Java FileHandler class for parameter documentation.
     */
    public AsyncFileHandler(String pattern, int limit, int count, boolean append) throws IOException 
    {
        super(pattern, limit, count, append);
    }

    /**
     * Intercepts a log record written to the FileHandler and writes it
     * to a memory queue instead of the parent FileHandler. Separate
     * thread will write queued records to disk.
     * @param record The log record to queue.
     */
    @Override
    public void publish(LogRecord record) 
    {
        int levelValue = getLevel().intValue();
    
        if (record.getLevel().intValue() < levelValue || levelValue == offValue) return;

        final Thread t = checkWorker();

        record.getSourceMethodName(); //Infer caller.
        
        boolean interrupted = Thread.interrupted();
        
        try {
            for (;;) 
            {
                try {
                    boolean offered = queue.offer(record, 10, TimeUnit.MILLISECONDS);
            
                    if (t == null || !t.isAlive()) 
                    {
                        if (!offered || queue.remove(record)) super.publish(record);
                       
                        break;
                    } else {
                        if (offered || handleFullQueue(record)) break;
                    }
                } catch (InterruptedException retry) {interrupted = true;}
            }
        } finally { if (interrupted) Thread.currentThread().interrupt(); }
    }

    // If we can't queue a log record, forwards the record to the underlying
    // FileHandler object.
    private boolean handleFullQueue(LogRecord r) 
    {
        super.publish(r);
        return true; //true if handled.
    }

    /**
     * Shutdown the logging thread and release resources.
     * Will flush the queue to disk.
     */
    @Override
    public void close() 
    {
        try {
            try {
                final Thread t = this.worker;
    
                if (t != null) 
                {
                    t.interrupt();
                    //shutdownQueue();
                    t.join();
                    //shutdownQueue();
                }
            } finally { super.close(); }
        } catch (InterruptedException reAssert) { Thread.currentThread().interrupt(); }
    }

    // Dumps queue to disk.
    private void shutdownQueue() 
    {
        for (LogRecord r; (r = queue.poll()) != null;) super.publish(r);
    }

    /**
     * Runs the thread that writes queued log records to disk.
     */
    @Override
    public void run() 
    {
        try {
            final BlockingQueue<LogRecord> q = this.queue;
    
            for (;;) super.publish(q.take()); 
        } catch (InterruptedException shutdown) {
            shutdownQueue();
            Thread.currentThread().interrupt();
        }
    }
    
    // See if we have started the worker thread and if not, start it.
    private Thread checkWorker() 
    {
        Thread t = worker;
    
        if (t == null) t = startWorker();
        
        return t;
    }

    // Create and start a thread from the Java thread pool system.
    private synchronized Thread startWorker() 
    {
        if (worker == null) 
        {
            worker = Executors.defaultThreadFactory().newThread(this);
            worker.setDaemon(true); //JDK-8060132
            worker.setContextClassLoader(getClass().getClassLoader());
            worker.start();
        }
        
        return worker;
    }
}
