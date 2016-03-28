package com.trevorbernard.disruptor.examples;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class WriterExample {
    private static final String FILE_NAME = "writefile.txt";
    private static AsynchronousFileChannel outputfile;
    private static AtomicInteger fileindex = new AtomicInteger(0);
    private static ThreadPoolExecutor pool = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>());

    public static void main(String[] args) throws InterruptedException, IOException, ExecutionException {
        outputfile = AsynchronousFileChannel.open(
                Paths.get(FILE_NAME),
                new HashSet<StandardOpenOption>(Arrays.asList(StandardOpenOption.WRITE,
                        StandardOpenOption.CREATE)), pool);
        List<Future<Integer>> futures = new ArrayList<>();
        for (int i = 0; i < 10000; i++) {
            futures.add(outputfile.write(ByteBuffer.wrap(("Hello"+"\n").getBytes()), fileindex.getAndIncrement() * 6));
        }
        outputfile.close();
        pool.shutdown();
        pool.awaitTermination(60, TimeUnit.SECONDS);
        for (Future<Integer> future : futures) {
            try {
                future.get();
            } catch (ExecutionException e) {
                System.out.println("Task wasn't executed!");
            }
        }
    }

}