package edu.eci.arsw.concurrency;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Controlador de pausa robusto que garantiza que los hilos
 * se detengan antes de tomar snapshots del estado.
 */
public final class PauseController {

    private final ReentrantLock lock = new ReentrantLock();
    private final Condition unpaused = lock.newCondition();
    private final Condition allPaused = lock.newCondition();

    private volatile boolean paused = false;
    private final AtomicInteger registeredThreads = new AtomicInteger(0);
    private final AtomicInteger threadsWaitingToPause = new AtomicInteger(0);

    /**
     * Pausa la ejecución y espera hasta que los hilos reporten su pausa.
     */
    public void pause() throws InterruptedException {
        lock.lock();
        try {
            paused = true;

            if (registeredThreads.get() == 0) return;

            long deadline = System.currentTimeMillis() + 3000;
            int expectedThreads = registeredThreads.get();

            while (threadsWaitingToPause.get() < expectedThreads &&
                   System.currentTimeMillis() < deadline) {
                allPaused.await(100, TimeUnit.MILLISECONDS);
                expectedThreads = registeredThreads.get();
                if (expectedThreads == 0) break;
            }

            if (threadsWaitingToPause.get() < registeredThreads.get() &&
                registeredThreads.get() > 0) {
                System.err.printf(
                        "⚠ Warning: No todos los hilos se pausaron. Registrados=%d, Pausados=%d%n",
                        registeredThreads.get(), threadsWaitingToPause.get());
            }

        } finally {
            lock.unlock();
        }
    }

    public void pauseNonBlocking() {
        lock.lock();
        try {
            paused = true;
        } finally {
            lock.unlock();
        }
    }

    public void resume() {
        lock.lock();
        try {
            if (paused) {
                paused = false;
                unpaused.signalAll();
            }
        } finally {
            lock.unlock();
        }
    }

    public boolean paused() { return paused; }

    public void registerThread() { registeredThreads.incrementAndGet(); }

    public void unregisterThread() {
        registeredThreads.decrementAndGet();
        lock.lock();
        try {
            allPaused.signalAll();
        } finally {
            lock.unlock();
        }
    }

    public void awaitIfPaused() throws InterruptedException {
        if (!paused) return;

        lock.lockInterruptibly();
        try {
            if (paused) {
                threadsWaitingToPause.incrementAndGet();
                allPaused.signalAll();

                try {
                    while (paused) {
                        unpaused.await();
                    }
                } finally {
                    threadsWaitingToPause.decrementAndGet();
                }
            }
        } finally {
            lock.unlock();
        }
    }

    public int getActiveThreads() { return Math.max(0, registeredThreads.get()); }
    public int getPausedThreads() { return Math.max(0, threadsWaitingToPause.get()); }

    public boolean allThreadsPaused() {
        int registered = registeredThreads.get();
        int pausing = threadsWaitingToPause.get();
        if (registered == 0) return paused;
        return paused && (pausing >= registered);
    }

    public String getDebugInfo() {
        return String.format(
                "PauseController State: paused=%s, registered=%d, waiting=%d, allPaused=%s",
                paused, getActiveThreads(), getPausedThreads(), allThreadsPaused());
    }
}
