package edu.eci.arsw.immortals;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import edu.eci.arsw.concurrency.PauseController;

/**
 * Inmortal: cada hilo representa un inmortal.
 * - sleepMillis permite desacelerar las peleas (duración mayor).
 * - Usa AtomicInteger para health.
 * - Registra/desregistra en PauseController dentro de run().
 */
public final class Immortal implements Runnable {
    private final String name;
    private final AtomicInteger health;
    private final int damage;
    private final List<Immortal> population;
    private final ScoreBoard scoreBoard;
    private final PauseController controller;
    private volatile boolean running = true;
    private final String fightMode;
    private final int sleepMillis;

    public Immortal(String name, int health, int damage,
                    List<Immortal> population,
                    ScoreBoard scoreBoard,
                    PauseController controller,
                    String fightMode,
                    int sleepMillis) {
        this.name = Objects.requireNonNull(name);
        this.health = new AtomicInteger(health);
        this.damage = Math.max(1, damage); // asegurar daño mínimo 1
        this.population = Objects.requireNonNull(population);
        this.scoreBoard = Objects.requireNonNull(scoreBoard);
        this.controller = Objects.requireNonNull(controller);
        this.fightMode = fightMode == null ? "ordered" : fightMode.toLowerCase();
        this.sleepMillis = Math.max(1, sleepMillis);
    }

    public String name() { return name; }

    public int getHealth() { return health.get(); }

    public boolean isAlive() { return getHealth() > 0 && running; }

    /** Señal para detener ordenadamente este inmortal */
    public void stop() { running = false; }

    @Override
    public void run() {
        // Registrar en el controlador de pausa
        controller.registerThread();
        try {
            while (running && isAlive()) {
                // Espera si la simulación está pausada
                controller.awaitIfPaused();

                Immortal opponent = pickOpponent();
                if (opponent != null) {
                    if ("ordered".equals(fightMode)) fightOrdered(opponent);
                    else fightNaive(opponent);
                }

                // Hacemos un sleep controlado para que la simulación no se extinga instantáneamente
                try {
                    Thread.sleep(sleepMillis);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        } finally {
            // Desregistrar en cualquier caso (importante para PauseController)
            controller.unregisterThread();
        }
    }

    private Immortal pickOpponent() {
        int size = population.size();
        if (size <= 1) return null;

        // intentos aleatorios rápidos
        for (int attempts = 0; attempts < 5; attempts++) {
            int idx = ThreadLocalRandom.current().nextInt(size);
            Immortal candidate = population.get(idx);
            if (candidate != this && candidate.isAlive()) return candidate;
        }
        // fallback: primer vivo
        for (Immortal cand : population) {
            if (cand != this && cand.isAlive()) return cand;
        }
        return null;
    }

    /** Lógica ordenada para evitar deadlocks: sincronizar por orden de nombre */
    private void fightOrdered(Immortal other) {
        Immortal first = this.name.compareTo(other.name) < 0 ? this : other;
        Immortal second = this.name.compareTo(other.name) < 0 ? other : this;

        synchronized (first) {
            synchronized (second) {
                if (!this.isAlive() || !other.isAlive()) return;

                int opponentHealth = other.health.get();
                int actualDamage = Math.min(opponentHealth, damage);
                if (actualDamage <= 0) return;

                // Aplicar daño y curación de forma segura dentro de la región crítica
                other.health.addAndGet(-actualDamage);
                this.health.addAndGet(actualDamage / 2);

                // Registrar pelea con el daño real (net loss se calcula en ScoreBoard)
                scoreBoard.recordFight(actualDamage);
            }
        }
    }

    /** Variante 'naive' - sin orden (no usada por defecto) */
    private void fightNaive(Immortal other) {
        synchronized (this) {
            synchronized (other) {
                if (!this.isAlive() || !other.isAlive()) return;
                int opponentHealth = other.health.get();
                int actualDamage = Math.min(opponentHealth, damage);
                if (actualDamage <= 0) return;
                other.health.addAndGet(-actualDamage);
                this.health.addAndGet(actualDamage / 2);
                scoreBoard.recordFight(actualDamage);
            }
        }
    }
}
