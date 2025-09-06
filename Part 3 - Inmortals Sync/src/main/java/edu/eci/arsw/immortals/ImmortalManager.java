package edu.eci.arsw.immortals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import edu.eci.arsw.concurrency.PauseController;

public final class ImmortalManager implements AutoCloseable {
  private final List<Immortal> population = new ArrayList<>();
  private final List<Future<?>> futures = new ArrayList<>();
  private final PauseController controller = new PauseController();
  private final ScoreBoard scoreBoard = new ScoreBoard();
  private ExecutorService exec;

  private final String fightMode;
  private final int initialHealth;
  private final int damage;

  public ImmortalManager(int n, String fightMode) {
    this(n, fightMode, Integer.getInteger("health", 100), Integer.getInteger("damage", 10));
  }

  public ImmortalManager(int n, String fightMode, int initialHealth, int damage) {
    this.fightMode = fightMode;
    this.initialHealth = initialHealth;
    this.damage = damage;
    for (int i=0;i<n;i++) {
      population.add(new Immortal("Immortal-"+i, initialHealth, damage, population, scoreBoard, controller));
    }
  }

  public synchronized void start() {
    if (exec != null) stop();
    exec = Executors.newVirtualThreadPerTaskExecutor();
    for (Immortal im : population) {
      futures.add(exec.submit(im));
    }
  }

  public void pause() throws InterruptedException { 
    controller.pause(); 
  }
  
  /**
   * Pausa la simulación sin bloquear (para compatibilidad).
   */
  public void pauseNonBlocking() { 
    controller.pauseNonBlocking(); 
  }
  
  public void resume() { controller.resume(); }
  public void stop() {
    for (Immortal im : population) im.stop();
    if (exec != null) exec.shutdownNow();
  }

  public int aliveCount() {
    int c = 0;
    for (Immortal im : population) if (im.isAlive()) c++;
    return c;
  }

  public long totalHealth() {
    long sum = 0;
    for (Immortal im : population) sum += im.getHealth();
    return sum;
  }

  /**
   * Calcula la salud total esperada según el invariante.
   * Invariante: Salud_esperada = N * H_inicial - F * (damage/2)
   * donde F es el número de peleas registradas.
   */
  public long expectedTotalHealth() {
    long totalFights = scoreBoard.totalFights();
    long initialTotal = (long) population.size() * initialHealth;
    long lostHealth = totalFights * (damage / 2);
    return initialTotal - lostHealth;
  }

  /**
   * Valida si se cumple el invariante.
   * @return true si la salud actual coincide con la esperada, false en caso contrario
   */
  public boolean validateInvariant() {
    return totalHealth() == expectedTotalHealth();
  }

  /**
   * Obtiene información detallada del invariante para debugging.
   */
  public String getInvariantInfo() {
    long actual = totalHealth();
    long expected = expectedTotalHealth();
    long fights = scoreBoard.totalFights();
    long initialTotal = (long) population.size() * initialHealth;
    
    return String.format(
        "Invariant Analysis:%n" +
        "  Immortals: %d%n" +
        "  Initial health each: %d%n" +
        "  Damage per fight: %d%n" +
        "  Total fights: %d%n" +
        "  Initial total health: %d%n" +
        "  Expected health loss: %d (fights * damage/2 = %d * %d/2)%n" +
        "  Expected total health: %d%n" +
        "  Actual total health: %d%n" +
        "  Invariant valid: %s%n" +
        "  Difference: %d%n",
        population.size(), initialHealth, damage, fights, 
        initialTotal, fights * (damage / 2), fights, damage,
        expected, actual, validateInvariant() ? "YES" : "NO",
        actual - expected
    );
  }

  public List<Immortal> populationSnapshot() {
    return Collections.unmodifiableList(new ArrayList<>(population));
  }

  public ScoreBoard scoreBoard() { return scoreBoard; }
  public PauseController controller() { return controller; }
  
  /**
   * Obtiene información sobre el estado de pausa.
   */
  public String getPauseInfo() {
    return String.format(
        "Pause Status:%n" +
        "  Paused: %s%n" +
        "  Active threads: %d%n" +
        "  Paused threads: %d%n" +
        "  All threads paused: %s%n",
        controller.paused() ? "YES" : "NO",
        controller.getActiveThreads(),
        controller.getPausedThreads(),
        controller.allThreadsPaused() ? "YES" : "NO"
    );
  }
  
  /**
   * Obtiene información de debug detallada del controlador de pausa.
   */
  public String getDebugPauseInfo() {
    return controller.getDebugInfo();
  }

  @Override public void close() { stop(); }
}
