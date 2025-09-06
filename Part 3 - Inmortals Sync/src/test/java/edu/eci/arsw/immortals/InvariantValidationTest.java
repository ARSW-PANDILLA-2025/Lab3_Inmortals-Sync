package edu.eci.arsw.immortals;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests adicionales para validar casos específicos del invariante.
 */
class InvariantValidationTest {

    private ImmortalManager manager;

    @AfterEach
    void tearDown() {
        if (manager != null) {
            manager.stop();
            manager = null;
        }
    }

    @Test
    @DisplayName("Test A: Validación matemática del invariante con valores conocidos")
    void testInvariantMathematicalValidation() {
        // Caso 1: 3 inmortales, 60 salud inicial, 12 daño
        manager = new ImmortalManager(3, "ordered", 60, 12);

        assertEquals(180, manager.totalHealth(), "Salud inicial: 3 × 60 = 180");
        assertEquals(180, manager.expectedTotalHealth(), "Salud esperada inicial = 180");

        // Simular manualmente que se registren peleas sin cambiar salud (para test
        // puro)
        manager.scoreBoard().recordFight(); // 1 pelea
        manager.scoreBoard().recordFight(); // 2 peleas

        // Con 2 peleas, pérdida esperada = 2 × (12/2) = 2 × 6 = 12
        assertEquals(180 - 12, manager.expectedTotalHealth(),
                "Salud esperada después de 2 peleas: 180 - 12 = 168");

        // Como no hubo peleas reales, salud actual sigue siendo 180
        assertEquals(180, manager.totalHealth(), "Salud actual no cambió (peleas simuladas)");

        // Por tanto, el invariante no se cumple en este caso controlado
        assertFalse(manager.validateInvariant(),
                "Invariante no se cumple cuando peleas registradas ≠ peleas reales");
    }

    @Test
    @DisplayName("Test B: Verificación de fórmula con diferentes valores de daño")
    void testInvariantWithDifferentDamageValues() {
        // Test con daño = 1 (mínimo)
        manager = new ImmortalManager(2, "ordered", 50, 1);
        assertEquals(100, manager.totalHealth(), "Caso daño=1: salud inicial 2×50=100");

        manager.scoreBoard().recordFight();
        assertEquals(100 - 0, manager.expectedTotalHealth(), "Daño=1: pérdida por pelea = 1/2 = 0 (truncado)");

        manager.stop();

        // Test con daño = 100 (alto)
        manager = new ImmortalManager(2, "ordered", 200, 100);
        assertEquals(400, manager.totalHealth(), "Caso daño=100: salud inicial 2×200=400");

        manager.scoreBoard().recordFight();
        assertEquals(400 - 50, manager.expectedTotalHealth(), "Daño=100: pérdida por pelea = 100/2 = 50");
    }

    @Test
    @DisplayName("Test C: Invariante con múltiples inmortales y verificación de consistencia")
    void testInvariantConsistencyWithManyImmortals() {
        // Caso con muchos inmortales para mayor probabilidad de peleas
        manager = new ImmortalManager(10, "ordered", 80, 16);

        long initialTotal = manager.totalHealth();
        assertEquals(10 * 80, initialTotal, "Salud inicial: 10 × 80 = 800");

        // Verificar que los cálculos internos son consistentes
        assertEquals(800, manager.expectedTotalHealth(), "Salud esperada inicial consistente");
        assertTrue(manager.validateInvariant(), "Invariante válido inicialmente");

        // Simular varias peleas registradas
        for (int i = 0; i < 5; i++) {
            manager.scoreBoard().recordFight();
        }

        // Con 5 peleas, pérdida = 5 × (16/2) = 5 × 8 = 40
        assertEquals(800 - 40, manager.expectedTotalHealth(), "Salud esperada tras 5 peleas: 800 - 40 = 760");

        // Información de debugging
        String info = manager.getInvariantInfo();
        assertTrue(info.contains("Total fights: 5"), "Info debe mostrar 5 peleas");
        assertTrue(info.contains("Expected health loss: 40"), "Info debe mostrar pérdida de 40");
    }

    @Test
    @DisplayName("Test D: Validación de casos extremos y edge cases")
    void testInvariantEdgeCases() {
        // Caso 1: Un solo inmortal (no puede pelear contra sí mismo)
        manager = new ImmortalManager(1, "ordered", 100, 10);
        assertEquals(100, manager.totalHealth(), "Un solo inmortal: salud = 100");
        assertTrue(manager.validateInvariant(), "Invariante válido con un solo inmortal");

        manager.stop();

        // Caso 2: Daño muy pequeño
        manager = new ImmortalManager(4, "ordered", 1000, 1);
        assertEquals(4000, manager.totalHealth(), "Salud inicial con daño mínimo");

        // Con daño = 1, pérdida por pelea = 1/2 = 0 (truncado)
        manager.scoreBoard().recordFight();
        assertEquals(4000, manager.expectedTotalHealth(), "Con daño=1, no hay pérdida neta");

        manager.stop();

        // Caso 3: Salud inicial pequeña, daño grande
        manager = new ImmortalManager(5, "ordered", 10, 20);
        assertEquals(50, manager.totalHealth(), "Salud inicial pequeña: 5×10=50");

        // Una pelea causaría pérdida de 20/2 = 10
        manager.scoreBoard().recordFight();
        assertEquals(40, manager.expectedTotalHealth(), "Una pelea con daño alto: 50-10=40");

        // Verificar que la información es coherente
        String info = manager.getInvariantInfo();
        assertTrue(info.contains("Damage per fight: 20"), "Info debe mostrar daño correcto");
        assertTrue(info.contains("Expected health loss: 10"), "Info debe mostrar pérdida correcta");
    }
}
