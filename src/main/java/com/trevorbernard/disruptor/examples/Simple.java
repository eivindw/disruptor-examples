package com.trevorbernard.disruptor.examples;

import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;

import java.util.UUID;
import java.util.concurrent.Executors;

public class Simple {
    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        // Preallocate RingBuffer with 1024 ValueEvents
        Disruptor<ValueEvent> disruptor =
                new Disruptor<>(ValueEvent.EVENT_FACTORY, 1024, Executors.defaultThreadFactory());
        // Build dependency graph
        disruptor.handleEventsWith((event, sequence, endOfBatch) -> {
            System.out.printf(
                    "Sequence: %d Thread: %s%n",
                    sequence, Thread.currentThread().getName()
            );
            System.out.println("ValueEvent: " + event.getValue());
        });
        RingBuffer<ValueEvent> ringBuffer = disruptor.start();

        for (long i = 10; i < 2000; i++) {
            String uuid = UUID.randomUUID().toString();
            // Two phase commit. Grab one of the 1024 slots
            long seq = ringBuffer.next();
            ValueEvent valueEvent = ringBuffer.get(seq);
            valueEvent.setValue(uuid);
            ringBuffer.publish(seq);
        }
        disruptor.shutdown();
    }
}
