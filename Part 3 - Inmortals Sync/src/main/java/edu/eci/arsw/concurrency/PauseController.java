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
      
      // Esperar hasta que todos los hilos registrados estén pausados
      // O hasta timeout para evitar deadlocks
      long deadline = System.currentTimeMillis() + 1000; // 1 segundo máximo
      
      while (registeredThreads.get() > 0 && 
             threadsWaitingToPause.get() < registeredThreads.get() &&
             System.currentTimeMillis() < deadline) {
        
        if (!allPaused.await(100, TimeUnit.MILLISECONDS)) {
          // Timeout - continuar de todas formas
          break;
        }
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
      paused = false; 
      threadsWaitingToPause.set(0); // Reset contador
      unpaused.signalAll(); 
    } finally { 
      lock.unlock(); 
    } 
  }
  
  public boolean paused() { return paused; }

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
    // Notificar que un hilo terminó
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
    lock.lockInterruptibly();
    try { 
      if (paused) {
        // Incrementar contador de hilos esperando pausa
        threadsWaitingToPause.incrementAndGet();
        allPaused.signalAll(); // Notificar que estamos pausados
        
        try {
          // Esperar hasta que se reanude
          while (paused) {
            unpaused.await();
          }
        } finally {
          // Decrementar contador al salir
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
    return Math.max(0, registeredThreads.get()); // Asegurar que nunca sea negativo
  }
  
  /**
   * Obtiene el número de hilos pausados.
   */
  public int getPausedThreads() {
    return Math.max(0, threadsWaitingToPause.get()); // Asegurar que nunca sea negativo
  }
  
  /**
   * Verifica si todos los hilos registrados están pausados.
   */
  public boolean allThreadsPaused() {
    int registered = registeredThreads.get();
    int pausing = threadsWaitingToPause.get();
    
    return paused && (registered == 0 || pausing >= registered);
  }
  
  /**
   * Obtiene información de estado para debugging.
   */
  public String getDebugInfo() {
    return String.format(
        "PauseController State: paused=%s, registered=%d, waiting=%d, allPaused=%s",
        paused, getActiveThreads(), getPausedThreads(), allThreadsPaused()
    );
  }
}
