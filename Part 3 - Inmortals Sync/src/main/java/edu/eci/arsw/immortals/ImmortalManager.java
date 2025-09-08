package edu.eci.arsw.immortals;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import edu.eci.arsw.concurrency.PauseController;

/**
 * Gestor de inmortales:
 * - mantiene población en CopyOnWriteArrayList (lecturas rápidas, modificaciones seguras)
 * - scheduledCleaner elimina muertos periódicamente sin bloquear la simulación
 * - controla start/stop y expone info diagnóstica
 */
public final class ImmortalManager implements AutoCloseable {

    private final List<Immortal> population = new CopyOnWriteArrayList<>();
    private final Map<Immortal, Future<?>> futuresMap = new ConcurrentHashMap<>();
    private final PauseController controller = new PauseController();
    private final ScoreBoard scoreBoard = new ScoreBoard();
    private ExecutorService exec;
    private ScheduledExecutorService cleaner;

    private final String fightMode;
    private final int initialHealth;
    private final int damage;
    private final int initialCount;
    private final int sleepMillis; // para ralentizar el loop de cada Immortal

    // Diagnóstico
    private final AtomicInteger activeWrappers = new AtomicInteger(0);
    private final AtomicInteger startedWrappers = new AtomicInteger(0);

    /** Constructor por defecto */
    public ImmortalManager(int n, String fightMode) {
        this(n, fightMode, 100, 10, 20);
    }

    /** Constructor con salud y daño (se usa en ControlFrame) */
    public ImmortalManager(int n, String fightMode, int initialHealth, int damage) {
        this(n, fightMode, initialHealth, damage, 20); // sleepMillis por defecto
    }

    /** Constructor completo */
    public ImmortalManager(int n, String fightMode, int initialHealth, int damage, int sleepMillis) {
        this.fightMode = fightMode == null ? "ordered" : fightMode.toLowerCase();
        this.initialHealth = initialHealth;
        this.damage = damage;
        this.initialCount = Math.max(0, n);
        this.sleepMillis = Math.max(1, sleepMillis);
        for (int i = 0; i < initialCount; i++) {
            population.add(new Immortal(
                    "Immortal-" + i,
                    initialHealth,
                    damage,
                    population,
                    scoreBoard,
                    controller,
                    this.fightMode,
                    this.sleepMillis));
        }
    }

    /** Arranca simulación con pool fijo y cleaner periódico */
    public synchronized void start() {
        if (exec != null) stop();

        exec = Executors.newFixedThreadPool(Math.max(1, initialCount));
        futuresMap.clear();
        activeWrappers.set(0);
        startedWrappers.set(0);

        for (Immortal im : population) {
            Future<?> f = exec.submit(() -> {
                startedWrappers.incrementAndGet();
                activeWrappers.incrementAndGet();
                try {
                    im.run();
                } finally {
                    activeWrappers.decrementAndGet();
                }
            });
            futuresMap.put(im, f);
        }

        cleaner = Executors.newSingleThreadScheduledExecutor();
        cleaner.scheduleAtFixedRate(() -> {
            try {
                removeDeadImmortals();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }, 300, 300, TimeUnit.MILLISECONDS);
    }

    public void pause() throws InterruptedException {
        controller.pause();
    }

    public void pauseNonBlocking() {
        controller.pauseNonBlocking();
    }

    public void resume() {
        controller.resume();
    }

    /** Detiene todo ordenadamente */
    public void stop() {
        for (Immortal im : population) im.stop();

        if (exec != null) {
            exec.shutdown();
            try {
                if (!exec.awaitTermination(5, TimeUnit.SECONDS)) {
                    exec.shutdownNow();
                }
            } catch (InterruptedException e) {
                exec.shutdownNow();
                Thread.currentThread().interrupt();
            } finally {
                exec = null;
            }
        }

        if (cleaner != null) {
            cleaner.shutdownNow();
            cleaner = null;
        }

        futuresMap.clear();
    }

    /** Eliminar muertos sin bloquear */
    public void removeDeadImmortals() {
        population.removeIf(im -> !im.isAlive());
        futuresMap.keySet().removeIf(im -> !population.contains(im));
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

    public long expectedTotalHealth() {
        long initialTotal = (long) initialCount * initialHealth;
        long totalNetLoss = scoreBoard.totalNetLoss();
        return initialTotal - totalNetLoss;
    }

    public boolean validateInvariant() {
        return totalHealth() == expectedTotalHealth();
    }

    /** Info de diagnóstico */
    public String getPauseInfo() {
        int submitted = futuresMap.size();
        long finished = futuresMap.values().stream().filter(Future::isDone).count();
        int runningNow = activeWrappers.get();
        int started = startedWrappers.get();
        int registeredInController = controller.getActiveThreads();
        int pausedThreadsInController = controller.getPausedThreads();
        int alive = aliveCount();

        return String.format(
                "Pause Status:%n" +
                "  Paused flag: %s%n" +
                "  Submitted tasks: %d%n" +
                "  Tasks finished (future.done): %d%n" +
                "  Tasks started (entered run wrapper): %d%n" +
                "  Active threads (wrapper count): %d%n" +
                "  Registered threads in PauseController: %d%n" +
                "  Paused threads in PauseController: %d%n" +
                "  Immortals vivos (health > 0): %d%n" +
                "  All threads paused (controller): %s%n",
                controller.paused() ? "YES" : "NO",
                submitted,
                finished,
                started,
                runningNow,
                registeredInController,
                pausedThreadsInController,
                alive,
                controller.allThreadsPaused() ? "YES" : "NO");
    }

    public String getInvariantInfo() {
        long actual = totalHealth();
        long expected = expectedTotalHealth();
        long fights = scoreBoard.totalFights();
        long initialTotal = (long) initialCount * initialHealth;
        long netLoss = scoreBoard.totalNetLoss();

        return String.format(
                "Invariant Analysis:%n" +
                "  Immortals (initial): %d%n" +
                "  Initial health each: %d%n" +
                "  Damage per fight (max): %d%n" +
                "  Total fights: %d%n" +
                "  Initial total health (N × H): %d%n" +
                "  Total net loss (accumulated): %d%n" +
                "  Expected total health (Total Fights × (Damage ÷ 2)): %d%n" +
                "  Actual total health: %d%n" +
                "  Invariant valid: %s%n" +
                "  Difference: %d%n" +
                "  Immortals vivos (health > 0): %d%n",
                initialCount, initialHealth, damage, fights,
                initialTotal, netLoss, expected, actual,
                (expected == actual) ? "YES" : "NO",
                actual - expected,
                aliveCount());
    }

    public List<Immortal> populationSnapshot() {
        return Collections.unmodifiableList(new ArrayList<>(population));
    }

    public ScoreBoard scoreBoard() {
        return scoreBoard;
    }

    public PauseController controller() {
        return controller;
    }

    @Override
    public void close() {
        stop();
    }
}
