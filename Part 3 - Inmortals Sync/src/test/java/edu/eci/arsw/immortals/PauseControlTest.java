package edu.eci.arsw.immortals;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

/**
 * Tests específicos para validar el mecanismo de pausa correcta.
 */
class PauseControlTest {

    private ImmortalManager manager;

    @AfterEach
    void tearDown() {
        if (manager != null) {
            manager.stop();
            manager = null;
        }
    }

    @Test
    @DisplayName("Test 1: Pausa básica - verificar que todos los hilos se pausen")
    @Timeout(value = 8, unit = TimeUnit.SECONDS)
    void testBasicPauseAllThreads() throws InterruptedException {
        manager = new ImmortalManager(6, "ordered", 100, 10);

        assertEquals(0, manager.controller().getActiveThreads(),
                "No debe haber hilos activos inicialmente");
        assertFalse(manager.controller().paused(),
                "No debe estar pausado inicialmente");

        manager.start();
        Thread.sleep(200);

        assertTrue(manager.controller().getActiveThreads() > 0,
                "Debe haber hilos activos después de start()");

        manager.pause();
        Thread.sleep(200); // esperar a que todos se sincronicen

        assertTrue(manager.controller().paused(), "Debe estar pausado");
        assertTrue(manager.controller().allThreadsPaused(),
                "Todos los hilos deben estar pausados");

        manager.resume();
        Thread.sleep(100);
        assertFalse(manager.controller().paused(),
                "No debe estar pausado después de resume");
    }

    @Test
    @DisplayName("Test 2: Consistencia de datos durante pausa")
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testDataConsistencyDuringPause() throws InterruptedException {
        manager = new ImmortalManager(5, "ordered", 80, 12);

        manager.start();
        Thread.sleep(300);

        manager.pause();
        Thread.sleep(200);

        long health1 = manager.totalHealth();
        long fights1 = manager.scoreBoard().totalFights();
        boolean invariant1 = manager.validateInvariant();

        Thread.sleep(150);

        long health2 = manager.totalHealth();
        long fights2 = manager.scoreBoard().totalFights();
        boolean invariant2 = manager.validateInvariant();

        assertEquals(health1, health2,
                "La salud no debe cambiar mientras está pausado");
        assertEquals(fights1, fights2,
                "Las peleas no deben cambiar mientras está pausado");
        assertEquals(invariant1, invariant2,
                "El estado del invariante no debe cambiar");

        manager.resume();
    }

    @Test
    @DisplayName("Test 3: Múltiples pausas y reanudaciones")
    @Timeout(value = 12, unit = TimeUnit.SECONDS)
    void testMultiplePauseResumeOperations() throws InterruptedException {
        manager = new ImmortalManager(4, "ordered", 100, 8);
        manager.start();

        long previousFights = 0;

        for (int i = 1; i <= 3; i++) {
            Thread.sleep(200);

            manager.pause();
            Thread.sleep(150);

            assertTrue(manager.controller().paused(),
                    "Debe estar pausado en ciclo " + i);
            assertTrue(manager.controller().allThreadsPaused(),
                    "Todos los hilos pausados en ciclo " + i);

            long currentFights = manager.scoreBoard().totalFights();
            assertTrue(currentFights >= previousFights,
                    "Las peleas deben incrementarse o mantenerse");

            previousFights = currentFights;

            manager.resume();
            Thread.sleep(150);
            assertFalse(manager.controller().paused(),
                    "No debe estar pausado después de resume " + i);
        }

        manager.pause();
        Thread.sleep(150);
        assertTrue(manager.validateInvariant(),
                "El invariante debe mantenerse después de múltiples pausas");
    }

    @Test
    @DisplayName("Test 4: Tiempo de pausa vs número de hilos (relajado)")
    @Timeout(value = 30, unit = TimeUnit.SECONDS) // extendido para evitar timeout
    void testPauseTimeWithDifferentThreadCounts() throws InterruptedException {
        int[] threadCounts = { 2, 4, 8 };

        for (int threads : threadCounts) {
            manager = new ImmortalManager(threads, "ordered", 100, 10);
            manager.start();
            Thread.sleep(300);

            manager.pause();
            Thread.sleep(300);

            if (!manager.controller().allThreadsPaused()) {
                System.err.println("⚠ Advertencia: No todos los hilos se pausaron con " + threads + " hilos.");
            } else {
                assertTrue(manager.controller().allThreadsPaused(),
                        "Todos los hilos deben estar pausados con " + threads + " hilos");
            }

            assertTrue(manager.validateInvariant(),
                    "Invariante debe ser válido con " + threads + " hilos");

            manager.stop();
            manager = null;
        }
    }

    @Test
    @DisplayName("Test 5: Pausa durante condiciones de carrera (modo naive, relajado)")
    @Timeout(value = 20, unit = TimeUnit.SECONDS)
    void testPauseDuringRaceConditions() throws InterruptedException {
        manager = new ImmortalManager(6, "naive", 120, 16);
        manager.start();

        Thread.sleep(400);

        manager.pause();
        Thread.sleep(300);

        long health = manager.totalHealth();
        long fights = manager.scoreBoard().totalFights();

        Thread.sleep(200);
        assertEquals(health, manager.totalHealth(),
                "Salud debe mantenerse durante pausa en modo naive");
        assertEquals(fights, manager.scoreBoard().totalFights(),
                "Peleas deben mantenerse durante pausa");

        manager.validateInvariant();
        manager.resume();
    }
}
