package edu.eci.arsw.concurrency;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Controlador de pausa mejorado que garantiza que todos los hilos
 * estén efectivamente pausados antes de continuar.
 */
public final class PauseController {
  private final ReentrantLock lock = new ReentrantLock();
  private final Condition unpaused = lock.newCondition();
  private final Condition allPaused = lock.newCondition();

  private volatile boolean paused = false;
  private final AtomicInteger registeredThreads = new AtomicInteger(0);
  private final AtomicInteger threadsWaitingToPause = new AtomicInteger(0);

  /**
   * Pausa la ejecución y espera hasta que todos los hilos estén pausados.
   * Usa un timeout para evitar bloqueos indefinidos.
   */
  public void pause() throws InterruptedException {
    lock.lock();
    try {
      paused = true;

      if (registeredThreads.get() == 0) {
        return;
      }

      long deadline = System.currentTimeMillis() + 3000;
      int expectedThreads = registeredThreads.get();

      while (threadsWaitingToPause.get() < expectedThreads &&
          System.currentTimeMillis() < deadline) {

        allPaused.await(100, TimeUnit.MILLISECONDS);

        expectedThreads = registeredThreads.get();
        if (expectedThreads == 0)
          break;
      }

      int finalWaiting = threadsWaitingToPause.get();
      int finalRegistered = registeredThreads.get();

      if (finalWaiting < finalRegistered && finalRegistered > 0) {
        System.err.println("Warning: No todos los hilos se pausaron en el tiempo esperado. " +
            "Registrados: " + finalRegistered +
            ", Pausados: " + finalWaiting);
      }

    } finally {
      lock.unlock();
    }
  }

  /**
   * Versión no-bloqueante de pause() para compatibilidad.
   */
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

  public boolean paused() {
    return paused;
  }

  /**
   * Registra un hilo al inicio de su ejecución.
   */
  public void registerThread() {
    registeredThreads.incrementAndGet();
  }

  /**
   * Desregistra un hilo al final de su ejecución.
   */
  public void unregisterThread() {
    registeredThreads.decrementAndGet();
    lock.lock();
    try {
      allPaused.signalAll();
    } finally {
      lock.unlock();
    }
  }

  /**
   * Espera si está pausado. Debe ser llamado en puntos de pausa.
   */
  public void awaitIfPaused() throws InterruptedException {

    if (!paused)
      return;

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

  /**
   * Obtiene el número de hilos registrados.
   */
  public int getActiveThreads() {
    return Math.max(0, registeredThreads.get()); 
  }

  /**
   * Obtiene el número de hilos pausados.
   */
  public int getPausedThreads() {
    return Math.max(0, threadsWaitingToPause.get());
  }

  /**
   * Verifica si todos los hilos registrados están pausados.
   */
  public boolean allThreadsPaused() {
    int registered = registeredThreads.get();
    int pausing = threadsWaitingToPause.get();

    if (registered == 0) {
      return paused;
    }

    return paused && (pausing >= registered);
  }

  /**
   * Obtiene información de estado para debugging.
   */
  public String getDebugInfo() {
    return String.format(
        "PauseController State: paused=%s, registered=%d, waiting=%d, allPaused=%s",
        paused, getActiveThreads(), getPausedThreads(), allThreadsPaused());
  }
}
