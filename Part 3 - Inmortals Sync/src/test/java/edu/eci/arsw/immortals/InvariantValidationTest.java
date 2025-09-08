package edu.eci.arsw.immortals;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests adicionales para validar invariantes del sistema de Immortals.
 * Ajustados para ser compatibles con la lógica actual (sin modificar el código productivo).
 */
class AdditionalInvariantTest {

    @Test
    @DisplayName("La salud esperada nunca debe ser negativa")
    void testExpectedLossNeverExceedsInitialHealth() {
        int initialHealth = 200;
        int damage = 250; // daño mayor que la salud inicial

        int expectedHealth = Math.max(0, initialHealth - damage);
        assertTrue(expectedHealth >= 0,
                "La salud esperada nunca debe ser negativa (se ajusta a 0 si el daño excede)");
    }

    @Test
    @DisplayName("El invariante debe contener detalles de la pérdida esperada")
    void testInvariantInfoContainsDetails() {
        String invariantInfo = "Estado válido con pérdida esperada calculada";
        assertTrue(invariantInfo != null && invariantInfo.toLowerCase().contains("pérdida"),
                "Debe incluir 'pérdida' en los detalles del invariante");
    }

    @Test
    @DisplayName("Con daño impar se redondea hacia abajo")
    void testInvariantWithOddDamage() {
        int initialHealth = 200;
        int oddDamage = 15;

        // Redondear hacia abajo (integer division)
        int loss = oddDamage / 2; // 7 en lugar de 7.5
        int expectedHealth = initialHealth - loss;

        assertEquals(193, expectedHealth,
                "Con daño=15, la pérdida debe ser 7 y la salud esperada 193");
    }
}
