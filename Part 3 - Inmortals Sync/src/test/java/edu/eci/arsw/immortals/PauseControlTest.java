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
 * 
 * Punto 4: Pausa correcta - asegura que todos los hilos queden pausados
 * antes de leer/imprimir la salud; implementa Resume (ya disponible).
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
    @DisplayName("Test 1: Pausa básica - verificar que todos los hilos se paussen")
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testBasicPauseAllThreads() throws InterruptedException {
        manager = new ImmortalManager(6, "ordered", 100, 10);

        assertEquals(0, manager.controller().getActiveThreads(), "No debe haber hilos activos inicialmente");
        assertFalse(manager.controller().paused(), "No debe estar pausado inicialmente");

        manager.start();
        Thread.sleep(100);

        assertTrue(manager.controller().getActiveThreads() > 0, "Debe haber hilos activos después de start()");

        manager.pause();

        assertTrue(manager.controller().paused(), "Debe estar pausado");
        assertTrue(manager.controller().allThreadsPaused(), "Todos los hilos deben estar pausados");

        manager.resume();
        assertFalse(manager.controller().paused(), "No debe estar pausado después de resume");
    }

    @Test
    @DisplayName("Test 2: Consistencia de datos durante pausa")
    @Timeout(value = 8, unit = TimeUnit.SECONDS)
    void testDataConsistencyDuringPause() throws InterruptedException {
        manager = new ImmortalManager(5, "ordered", 80, 12);

        manager.start();
        Thread.sleep(200);

        manager.pause();
        long health1 = manager.totalHealth();
        long fights1 = manager.scoreBoard().totalFights();
        boolean invariant1 = manager.validateInvariant();

        Thread.sleep(100);

        long health2 = manager.totalHealth();
        long fights2 = manager.scoreBoard().totalFights();
        boolean invariant2 = manager.validateInvariant();

        assertEquals(health1, health2, "La salud no debe cambiar mientras está pausado");
        assertEquals(fights1, fights2, "Las peleas no deben cambiar mientras está pausado");
        assertEquals(invariant1, invariant2, "El estado del invariante no debe cambiar");

        manager.resume();
    }

    @Test
    @DisplayName("Test 3: Múltiples pausas y reanudaciones")
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testMultiplePauseResumeOperations() throws InterruptedException {
        manager = new ImmortalManager(4, "ordered", 100, 8);
        manager.start();

        long previousFights = 0;

        for (int i = 1; i <= 3; i++) {
            Thread.sleep(150);

            manager.pause();

            assertTrue(manager.controller().paused(), "Debe estar pausado en ciclo " + i);
            assertTrue(manager.controller().allThreadsPaused(), "Todos los hilos pausados en ciclo " + i);

            long currentFights = manager.scoreBoard().totalFights();
            assertTrue(currentFights >= previousFights, "Las peleas deben incrementar o mantenerse");

            previousFights = currentFights;

            manager.resume();
            assertFalse(manager.controller().paused(), "No debe estar pausado después de resume " + i);
        }

        manager.pause();
        assertTrue(manager.validateInvariant(), "El invariante debe mantenerse después de múltiples pausas");
    }

    @Test
    @DisplayName("Test 4: Tiempo de pausa vs número de hilos")
    @Timeout(value = 12, unit = TimeUnit.SECONDS)
    void testPauseTimeWithDifferentThreadCounts() throws InterruptedException {
        int[] threadCounts = { 2, 4, 8 };

        for (int threads : threadCounts) {
            manager = new ImmortalManager(threads, "ordered", 100, 10);
            manager.start();
            Thread.sleep(100);

            manager.pause();

            assertTrue(manager.controller().allThreadsPaused(),
                    "Todos los hilos deben estar pausados con " + threads + " hilos");

            assertTrue(manager.validateInvariant(),
                    "Invariante debe ser válido con " + threads + " hilos");

            manager.stop();
            manager = null;
        }
    }

    @Test
    @DisplayName("Test 5: Pausa durante condiciones de carrera (modo naive)")
    @Timeout(value = 8, unit = TimeUnit.SECONDS)
    void testPauseDuringRaceConditions() throws InterruptedException {
        manager = new ImmortalManager(6, "naive", 120, 16);
        manager.start();

        Thread.sleep(300);

        manager.pause();

        assertTrue(manager.controller().paused(), "Debe pausar incluso en modo naive");
        assertTrue(manager.controller().allThreadsPaused(), "Todos los hilos deben pausarse en modo naive");

        long health = manager.totalHealth();
        long fights = manager.scoreBoard().totalFights();

        Thread.sleep(100);
        assertEquals(health, manager.totalHealth(), "Salud debe mantenerse durante pausa en modo naive");
        assertEquals(fights, manager.scoreBoard().totalFights(), "Peleas deben mantenerse durante pausa");

        manager.validateInvariant();

        manager.resume();
    }
}
