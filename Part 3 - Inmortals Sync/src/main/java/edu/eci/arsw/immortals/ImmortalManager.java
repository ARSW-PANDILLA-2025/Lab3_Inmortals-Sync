package edu.eci.arsw.immortals;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import edu.eci.arsw.concurrency.PauseController;

public final class ImmortalManager implements AutoCloseable {
    private final List<Immortal> population = new CopyOnWriteArrayList<>();
    private final List<Future<?>> futures = new CopyOnWriteArrayList<>();
    private final PauseController controller = new PauseController();
    private final ScoreBoard scoreBoard = new ScoreBoard();
    private ExecutorService exec;

    private final String fightMode;
    private final int initialHealth;
    private final int damage;
    private final int initialCount;

    public ImmortalManager(int n, String fightMode) {
        this(n, fightMode, 100, 10);
    }

    public ImmortalManager(int n, String fightMode, int initialHealth, int damage) {
        this.fightMode = fightMode == null ? "ordered" : fightMode.toLowerCase();
        this.initialHealth = initialHealth;
        this.damage = damage;
        this.initialCount = Math.max(0, n);
        for (int i = 0; i < initialCount; i++) {
            population.add(new Immortal("Immortal-" + i, initialHealth, damage,
                    population, scoreBoard, controller, this.fightMode));
        }
    }

    public synchronized void start() {
        if (exec != null) stop();
        exec = Executors.newVirtualThreadPerTaskExecutor();
        futures.clear();
        for (Immortal im : population) futures.add(exec.submit(im));
    }

    public void pause() throws InterruptedException { controller.pause(); }
    public void pauseNonBlocking() { controller.pauseNonBlocking(); }
    public void resume() { controller.resume(); }

    public void stop() {
        for (Immortal im : population) im.stop();
        if (exec != null) {
            exec.shutdown();
            try {
                if (!exec.awaitTermination(5, TimeUnit.SECONDS)) exec.shutdownNow();
            } catch (InterruptedException e) {
                exec.shutdownNow();
                Thread.currentThread().interrupt();
            } finally { exec = null; }
        }
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
        long totalFights = scoreBoard.totalFights();
        long initialTotal = (long) initialCount * initialHealth;
        long lostHealth = totalFights * (damage / 2);
        return initialTotal - lostHealth;
    }

    public boolean validateInvariant() { return totalHealth() == expectedTotalHealth(); }

    public String getInvariantInfo() {
        long actual = totalHealth();
        long expected = expectedTotalHealth();
        long fights = scoreBoard.totalFights();
        long initialTotal = (long) initialCount * initialHealth;

        return String.format(
                "Invariant Analysis:%n" +
                "  Immortals (initial): %d%n" +
                "  Initial health each: %d%n" +
                "  Damage per fight: %d%n" +
                "  Total fights: %d%n" +
                "  Initial total health: %d%n" +
                "  Expected health loss: %d (fights * damage/2 = %d * %d/2)%n" +
                "  Expected total health: %d%n" +
                "  Actual total health: %d%n" +
                "  Invariant valid: %s%n" +
                "  Difference: %d%n",
                initialCount, initialHealth, damage, fights,
                initialTotal, fights*(damage/2), fights, damage,
                expected, actual, validateInvariant() ? "YES" : "NO",
                actual - expected);
    }

    public List<Immortal> populationSnapshot() {
        return Collections.unmodifiableList(new ArrayList<>(population));
    }

    public ScoreBoard scoreBoard() { return scoreBoard; }
    public PauseController controller() { return controller; }

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
                controller.allThreadsPaused() ? "YES" : "NO");
    }

    @Override
    public void close() { stop(); }

    public List<Future<?>> getFutures() { return futures; }
}
