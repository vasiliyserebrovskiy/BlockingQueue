package ait.mediation;

import java.util.LinkedList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BlkQueueImpl<T> implements BlkQueue<T> {

    private final int capacity;
    private LinkedList<T> messages;

    //Lock and conditions variables
    private final Lock mutex = new ReentrantLock();
    private final Condition pushWaitCondition = mutex.newCondition();
    private final Condition popWaitCondition = mutex.newCondition();

    public BlkQueueImpl(int maxSize) {
        this.capacity = maxSize;
        this.messages = new LinkedList<>();
    }

    @Override
    public void push(T message) {
       mutex.lock();
       try {
           // if we did not have free space in messages list we need to wait
           while(messages.size() == capacity) {
               try {
                   pushWaitCondition.await();
               } catch (InterruptedException e) {
                   throw new RuntimeException(e);
               }
           }
           this.messages.addLast(message);
           popWaitCondition.signal();
       } finally {
           mutex.unlock();
       }
    }

    @Override
    public T pop() {
        mutex.lock();
        try {
            // if we did not have any message in messages list we need to wait
            while(messages.isEmpty()) {
                try {
                    popWaitCondition.await();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            T res = messages.removeFirst();
            pushWaitCondition.signal();
            return res;

        } finally {
            mutex.unlock();
        }
    }
}
