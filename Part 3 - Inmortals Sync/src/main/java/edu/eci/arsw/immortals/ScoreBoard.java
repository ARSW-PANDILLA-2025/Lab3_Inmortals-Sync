package edu.eci.arsw.immortals;

import java.util.concurrent.atomic.AtomicLong;

/**
 * ScoreBoard: registra peleas y pérdida neta sobre la salud total.
 * - recordFight(actualDamage): incrementa totalFights y totalNetLoss
 *   donde netLoss = actualDamage - (actualDamage / 2)
 */
public final class ScoreBoard {
    private final AtomicLong totalFights = new AtomicLong(0);
    private final AtomicLong totalNetLoss = new AtomicLong(0);

    public void recordFight(int actualDamage) {
        if (actualDamage <= 0) return;
        totalFights.incrementAndGet();
        long netLoss = actualDamage - (actualDamage / 2);
        totalNetLoss.addAndGet(netLoss);
    }

    public long totalFights() { return totalFights.get(); }
    public long totalNetLoss() { return totalNetLoss.get(); }
}
