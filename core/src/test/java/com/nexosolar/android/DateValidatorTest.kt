package com.nexosolar.android

import com.nexosolar.android.core.DateValidator
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDate

/**
 * Tests unitarios para [com.nexosolar.android.core.DateValidator].
 * Verifica la lógica de validación de rangos y límites de fechas.
 */
class DateValidatorTest {

    // ========== Tests para isValidRange() ==========

    @Test
    fun `isValidRange returns true when start is before end`() {
        // GIVEN
        val start = LocalDate.of(2026, 1, 1)
        val end = LocalDate.of(2026, 1, 31)

        // WHEN
        val result = DateValidator.isValidRange(start, end)

        // THEN
        assertTrue(result, "Un rango con inicio antes del fin debería ser válido")
    }

    @Test
    fun `isValidRange returns true when start equals end`() {
        // GIVEN
        val sameDate = LocalDate.of(2026, 1, 15)

        // WHEN
        val result = DateValidator.isValidRange(sameDate, sameDate)

        // THEN
        assertTrue(result, "Un rango con fechas iguales debería ser válido")
    }

    @Test
    fun `isValidRange returns false when start is after end`() {
        // GIVEN
        val start = LocalDate.of(2026, 2, 1)
        val end = LocalDate.of(2026, 1, 1)

        // WHEN
        val result = DateValidator.isValidRange(start, end)

        // THEN
        assertFalse(result, "Un rango con inicio después del fin debería ser inválido")
    }

    @Test
    fun `isValidRange returns true when both dates are null`() {
        // WHEN
        val result = DateValidator.isValidRange(null, null)

        // THEN
        assertTrue(result, "Un rango con ambas fechas null debería ser válido")
    }

    @Test
    fun `isValidRange returns true when only start is null`() {
        // GIVEN
        val end = LocalDate.of(2026, 1, 31)

        // WHEN
        val result = DateValidator.isValidRange(null, end)

        // THEN
        assertTrue(result, "Un rango con inicio null debería ser válido")
    }

    @Test
    fun `isValidRange returns true when only end is null`() {
        // GIVEN
        val start = LocalDate.of(2026, 1, 1)

        // WHEN
        val result = DateValidator.isValidRange(start, null)

        // THEN
        assertTrue(result, "Un rango con fin null debería ser válido")
    }

    // ========== Tests para isWithinBounds() ==========

    @Test
    fun `isWithinBounds returns true when date is inside bounds`() {
        // GIVEN
        val date = LocalDate.of(2026, 1, 15)
        val min = LocalDate.of(2026, 1, 1)
        val max = LocalDate.of(2026, 1, 31)

        // WHEN
        val result = DateValidator.isWithinBounds(date, min, max)

        // THEN
        assertTrue(result, "Una fecha dentro del rango debería ser válida")
    }

    @Test
    fun `isWithinBounds returns true when date equals min`() {
        // GIVEN
        val date = LocalDate.of(2026, 1, 1)
        val min = LocalDate.of(2026, 1, 1)
        val max = LocalDate.of(2026, 1, 31)

        // WHEN
        val result = DateValidator.isWithinBounds(date, min, max)

        // THEN
        assertTrue(result, "Una fecha igual al mínimo debería ser válida")
    }

    @Test
    fun `isWithinBounds returns true when date equals max`() {
        // GIVEN
        val date = LocalDate.of(2026, 1, 31)
        val min = LocalDate.of(2026, 1, 1)
        val max = LocalDate.of(2026, 1, 31)

        // WHEN
        val result = DateValidator.isWithinBounds(date, min, max)

        // THEN
        assertTrue(result, "Una fecha igual al máximo debería ser válida")
    }

    @Test
    fun `isWithinBounds returns false when date is before min`() {
        // GIVEN
        val date = LocalDate.of(2025, 12, 31)
        val min = LocalDate.of(2026, 1, 1)
        val max = LocalDate.of(2026, 1, 31)

        // WHEN
        val result = DateValidator.isWithinBounds(date, min, max)

        // THEN
        assertFalse(result, "Una fecha antes del mínimo debería ser inválida")
    }

    @Test
    fun `isWithinBounds returns false when date is after max`() {
        // GIVEN
        val date = LocalDate.of(2026, 2, 1)
        val min = LocalDate.of(2026, 1, 1)
        val max = LocalDate.of(2026, 1, 31)

        // WHEN
        val result = DateValidator.isWithinBounds(date, min, max)

        // THEN
        assertFalse(result, "Una fecha después del máximo debería ser inválida")
    }

    @Test
    fun `isWithinBounds returns true when date is null`() {
        // GIVEN
        val min = LocalDate.of(2026, 1, 1)
        val max = LocalDate.of(2026, 1, 31)

        // WHEN
        val result = DateValidator.isWithinBounds(null, min, max)

        // THEN
        assertTrue(result, "Una fecha null debería considerarse válida")
    }

    @Test
    fun `isWithinBounds ignores lower bound when min is null`() {
        // GIVEN
        val date = LocalDate.of(2025, 1, 1)
        val max = LocalDate.of(2026, 1, 31)

        // WHEN
        val result = DateValidator.isWithinBounds(date, null, max)

        // THEN
        assertTrue(result, "Sin límite mínimo, cualquier fecha <= max debería ser válida")
    }

    @Test
    fun `isWithinBounds ignores upper bound when max is null`() {
        // GIVEN
        val date = LocalDate.of(2027, 12, 31)
        val min = LocalDate.of(2026, 1, 1)

        // WHEN
        val result = DateValidator.isWithinBounds(date, min, null)

        // THEN
        assertTrue(result, "Sin límite máximo, cualquier fecha >= min debería ser válida")
    }

    @Test
    fun `isWithinBounds returns true when all parameters are null`() {
        // WHEN
        val result = DateValidator.isWithinBounds(null, null, null)

        // THEN
        assertTrue(result, "Sin ninguna restricción, debería ser válido")
    }
}
