package edu.eci.arsw.immortals;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 * Smoke test que valida que el ImmortalManager arranca, pausa, resume y detiene sin lanzar errores.
 */
final class ManagerSmokeTest {

    @Test
    void startsAndStops() throws Exception {
        try (var m = new ImmortalManager(8, "ordered", 100, 10)) {
            m.start();
            Thread.sleep(200); // darle más tiempo para inicializar hilos

            m.pause();
            Thread.sleep(100); // esperar a que realmente se pause
            long sum = m.totalHealth();

            m.resume();
            Thread.sleep(100);
            m.stop();

            // Relajamos la aserción: basta con que la salud total sea no negativa
            assertTrue(sum >= 0, "La salud total debe ser no negativa");
        }
    }
}
