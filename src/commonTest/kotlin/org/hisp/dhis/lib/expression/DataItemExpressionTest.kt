package org.hisp.dhis.lib.expression

import org.hisp.dhis.lib.expression.spi.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests all forms of [DataItem]s are correctly transformed from an expression to an AST and into a
 * [DataItem] value.
 *
 * @author Jan Bernitt
 */
internal class DataItemExpressionTest {
    @Test
    fun testDataElement() {
        assertEquals(
            setOf(
                DataItem(
                    DataItemType.DATA_ELEMENT,
                    ID(IDType.DataElementUID, "u1234567890"))),
            evaluate("#{u1234567890}"))
    }

    @Test
    fun testDataElement2() {
        assertEquals(
            setOf(
                DataItem(
                    DataItemType.DATA_ELEMENT,
                    ID(IDType.DataElementUID, "u1234567890")),
                DataItem(
                    DataItemType.DATA_ELEMENT,
                    ID(IDType.DataElementUID, "v1234567890"))),
            evaluate("#{u1234567890} + #{v1234567890}"))
    }

    @Test
    fun testDataElementGroup() {
        assertEquals(
            setOf(
                DataItem(
                    DataItemType.DATA_ELEMENT,
                    ID(IDType.DateElementGroupUID, "u1234567890"))),
            evaluate("#{deGroup:u1234567890}"))
    }

    @Test
    fun testProgramStageDataElement() {
        assertEquals(
            setOf(
                DataItem(
                    DataItemType.DATA_ELEMENT,
                    ID(IDType.ProgramStageUID, "u1234567890"),
                    ID(IDType.DataElementUID, "v1234567890"))),
            evaluate("#{u1234567890.v1234567890}"))
    }

    @Test
    fun testProgramStageCategoryOption() {
        assertEquals(
            setOf(
                DataItem(
                    DataItemType.DATA_ELEMENT,
                    ID(IDType.ProgramStageUID, "u1234567890"),
                    ID(IDType.CategoryOptionUID, "v1234567890"))),
            evaluate("#{u1234567890.co:v1234567890}"))
    }

    @Test
    fun testProgramStageCategoryOptionGroup() {
        assertEquals(
            setOf(
                DataItem(
                    DataItemType.DATA_ELEMENT,
                    ID(IDType.ProgramStageUID, "u1234567890"),
                    listOf(
                        ID(IDType.CategoryOptionGroupUID, "v1234567890"),
                        ID(IDType.CategoryOptionGroupUID, "w1234567890")), listOf(),
                    QueryModifiers())),
            evaluate("#{u1234567890.coGroup:v1234567890&w1234567890}"))
    }

    @Test
    fun testDataElementCategoryOptionAttributeOptionCombo() {
        assertEquals(
            setOf(
                DataItem(
                    DataItemType.DATA_ELEMENT,
                    ID(IDType.DataElementUID, "u1234567890"),
                    ID(IDType.CategoryOptionUID, "v1234567890"),
                    ID(IDType.AttributeOptionComboUID, "w1234567890"))),
            evaluate("#{u1234567890.v1234567890.w1234567890}"))
    }

    @Test
    fun testAttribute() {
        assertEquals(
            setOf(
                DataItem(
                    DataItemType.ATTRIBUTE,
                    ID(IDType.AttributeUID, "u1234567890"))),
            evaluate("A{u1234567890}"))
    }

    @Test
    fun testProgramAttribute() {
        assertEquals(
            setOf(
                DataItem(
                    DataItemType.ATTRIBUTE,
                    ID(IDType.ProgramUID, "u1234567890"),
                    ID(IDType.AttributeUID, "v1234567890"))),
            evaluate("A{u1234567890.v1234567890}"))
    }

    @Test
    fun testConstant() {
        assertEquals(
            setOf(
                DataItem(
                    DataItemType.CONSTANT,
                    ID(IDType.ConstantUID, "u1234567890"))),
            evaluate("C{u1234567890}"))
    }

    @Test
    fun testProgramDataElement() {
        assertEquals(
            setOf(
                DataItem(
                    DataItemType.PROGRAM_DATA_ELEMENT,
                    ID(IDType.ProgramUID, "u1234567890"),
                    ID(IDType.DataElementUID, "v1234567890"))),
            evaluate("D{u1234567890.v1234567890}"))
    }

    @Test
    fun testProgramIndicator() {
        assertEquals(
            setOf(
                DataItem(
                    DataItemType.PROGRAM_INDICATOR,
                    ID(IDType.ProgramIndicatorUID, "u1234567890"))),
            evaluate("I{u1234567890}"))
    }

    @Test
    fun testIndicator() {
        assertEquals(
            setOf(
                DataItem(
                    DataItemType.INDICATOR,
                    ID(IDType.IndicatorUID, "u1234567890"))),
            evaluate("N{u1234567890}"))
    }

    @Test
    fun testOrganisationUnitGroup() {
        assertEquals(
            setOf(
                DataItem(
                    DataItemType.ORG_UNIT_GROUP,
                    ID(IDType.OrganisationUnitGroupUID, "u1234567890"))),
            evaluate("OUG{u1234567890}"))
    }

    @Test
    fun testValidationRule() {
        assertEquals(
            setOf(
                DataItem(
                    DataItemType.VALIDATION_RULE,
                    ID(IDType.ValidationRuleUID, "u1234567890"))),
            evaluate("VR{u1234567890}"))
    }

    @Test
    fun testValidationRule_Predictor() {
        assertEquals(
            setOf(
                DataItem(
                    DataItemType.VALIDATION_RULE,
                    ID(IDType.ValidationRuleUID, "u1234567890"))),
            Expression("VR{u1234567890}", ExpressionMode.PREDICTOR_GENERATOR_EXPRESSION).collectDataItems())
    }

    @Test
    fun testValidationRule_OnlyInIndicatorsAndPredictors() {
        for (mode in listOf(
            ExpressionMode.VALIDATION_RULE_EXPRESSION,
            ExpressionMode.PROGRAM_INDICATOR_EXPRESSION,
            ExpressionMode.RULE_ENGINE_CONDITION)) {
            val e = runCatching { Expression("VR{u1234567890}", mode) }.exceptionOrNull()
            assertTrue(e?.message?.contains("Unknown function or constant: 'VR'") == true, "$mode: ${e?.message}")
        }
    }

    @Test
    fun testValidationRule_ProgramVariableUnaffected() {
        // VR shares its first letter with V (program variables), as VAR (Android custom intents) does
        assertEquals(
            setOf("event_date"),
            Expression("V{event_date}", ExpressionMode.PROGRAM_INDICATOR_EXPRESSION).collectProgramVariablesNames())
    }

    @Test
    fun testDataSet() {
        assertEquals(
            setOf(
                DataItem(
                    DataItemType.REPORTING_RATE,
                    ID(IDType.DataSetUID, "u1234567890"), ID(IDType.ReportingRateType, "ACTUAL_REPORTS"))),
            evaluate("R{u1234567890.ACTUAL_REPORTS}"))
    }

    companion object {
        private fun evaluate(expression: String): Set<DataItem> {
            return Expression(expression, ExpressionMode.INDICATOR_EXPRESSION).collectDataItems()
        }
    }
}
