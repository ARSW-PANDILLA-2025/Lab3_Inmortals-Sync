package edu.eci.arsw.immortals;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import edu.eci.arsw.concurrency.PauseController;

public final class Immortal implements Runnable {
    private final String name;
    private int health;
    private final int damage;
    private final List<Immortal> population;
    private final ScoreBoard scoreBoard;
    private final PauseController controller;
    private volatile boolean running = true;
    private final String fightMode;

    public Immortal(String name, int health, int damage,
                    List<Immortal> population,
                    ScoreBoard scoreBoard,
                    PauseController controller,
                    String fightMode) {
        this.name = Objects.requireNonNull(name);
        this.health = health;
        this.damage = damage;
        this.population = Objects.requireNonNull(population);
        this.scoreBoard = Objects.requireNonNull(scoreBoard);
        this.controller = Objects.requireNonNull(controller);
        this.fightMode = fightMode == null ? "ordered" : fightMode.toLowerCase();
    }

    public String name() { return name; }

    public synchronized int getHealth() { return health; }

    public boolean isAlive() { return getHealth() > 0 && running; }

    public void stop() { running = false; }

    @Override
    public void run() {
        controller.registerThread();
        try {
            while (running && isAlive()) {
                controller.awaitIfPaused();

                Immortal opponent = pickOpponent();
                if (opponent != null) fightOrdered(opponent);

                try { Thread.sleep(2); }
                catch (InterruptedException e) { Thread.currentThread().interrupt(); break; }
            }
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        } finally {
            controller.unregisterThread();
        }
    }

    private Immortal pickOpponent() {
        int size = population.size();
        if (size <= 1) return null;

        for (int attempts = 0; attempts < 5; attempts++) {
            int idx = ThreadLocalRandom.current().nextInt(size);
            Immortal candidate = population.get(idx);
            if (candidate != this && candidate.isAlive()) return candidate;
        }

        for (Immortal cand : population) {
            if (cand != this && cand.isAlive()) return cand;
        }
        return null;
    }

    private void fightOrdered(Immortal other) {
        Immortal first = this.name.compareTo(other.name) < 0 ? this : other;
        Immortal second = this.name.compareTo(other.name) < 0 ? other : this;

        synchronized (first) {
            synchronized (second) {
                if (!this.isAlive() || !other.isAlive()) return;

                int damageToApply = this.damage;
                other.decreaseHealth(damageToApply);
                this.increaseHealth(damageToApply / 2);

                scoreBoard.recordFight();
            }
        }
    }

    private synchronized void decreaseHealth(int amount) {
        health -= amount;
        if (health < 0) health = 0;
    }

    private synchronized void increaseHealth(int amount) {
        health += amount;
    }
}
