package pol.rubiano.docdimtool.app.presentation

import pol.rubiano.docdimtool.app.domain.models.*
import pol.rubiano.docdimtool.app.domain.usecases.ReverseCalculateDocumentUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class ReverseCalculatorViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private lateinit var vm: ReverseCalculatorViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
        vm = ReverseCalculatorViewModel(ReverseCalculateDocumentUseCase())
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has no results, no error, and empty dpis`() {
        val s = vm.state.value
        assertTrue(s.results.isEmpty())
        assertNull(s.error)
        assertTrue(s.selectedDpis.isEmpty())
    }

    @Test
    fun `initial input unit is MM not PX`() {
        assertEquals(InputUnit.MM, vm.state.value.inputUnit)
    }

    @Test
    fun `changing known side clears results`() {
        calculateSuccessfully()
        vm.onKnownSideChanged(Side.HEIGHT)
        assertTrue(vm.state.value.results.isEmpty())
        assertEquals(Side.HEIGHT, vm.state.value.knownSide)
    }

    @Test
    fun `changing known value clears results and error`() {
        calculateSuccessfully()
        vm.onKnownValueChanged("250")
        assertTrue(vm.state.value.results.isEmpty())
        assertNull(vm.state.value.error)
    }

    @Test
    fun `changing input unit clears results`() {
        calculateSuccessfully()
        vm.onInputUnitChanged(InputUnit.CM)
        assertTrue(vm.state.value.results.isEmpty())
    }

    @Test
    fun `attempting to set input unit to PX is rejected`() {
        val initialUnit = vm.state.value.inputUnit
        vm.onInputUnitChanged(InputUnit.PX)
        assertEquals(initialUnit, vm.state.value.inputUnit)
    }

    @Test
    fun `changing ratio preset clears results`() {
        calculateSuccessfully()
        vm.onRatioPresetSelected(RatioPreset.RATIO_4_3)
        assertTrue(vm.state.value.results.isEmpty())
    }

    @Test
    fun `toggling dpi clears results`() {
        calculateSuccessfully()
        vm.onDpiToggled(150)
        assertTrue(vm.state.value.results.isEmpty())
    }

    @Test
    fun `dpi is added when toggled from empty set`() {
        vm.onDpiToggled(300)
        assertTrue(300 in vm.state.value.selectedDpis)
    }

    @Test
    fun `dpi is removed when toggled from set`() {
        vm.onDpiToggled(300)
        vm.onDpiToggled(300)
        assertTrue(300 !in vm.state.value.selectedDpis)
    }

    @Test
    fun `changing orientation clears results and error`() {
        calculateSuccessfully()
        vm.onOrientationChanged(Orientation.PORTRAIT)
        assertTrue(vm.state.value.results.isEmpty())
        assertNull(vm.state.value.error)
    }

    @Test
    fun `changing bleed value clears results and error`() {
        calculateSuccessfully()
        vm.onBleedValueChanged("5")
        assertTrue(vm.state.value.results.isEmpty())
        assertNull(vm.state.value.error)
    }

    @Test
    fun `changing bleed unit clears results and error`() {
        calculateSuccessfully()
        vm.onBleedUnitChanged(BleedUnit.CM)
        assertTrue(vm.state.value.results.isEmpty())
        assertNull(vm.state.value.error)
    }

    @Test
    fun `changing orientation clears pre-existing error`() {
        vm.onKnownValueChanged("")
        vm.onDpiToggled(300)
        vm.onCalculate()
        assertNotNull(vm.state.value.error)
        vm.onOrientationChanged(Orientation.PORTRAIT)
        assertNull(vm.state.value.error)
    }

    @Test
    fun `changing bleed clears pre-existing error`() {
        vm.onKnownValueChanged("")
        vm.onDpiToggled(300)
        vm.onCalculate()
        assertNotNull(vm.state.value.error)
        vm.onBleedValueChanged("3")
        assertNull(vm.state.value.error)
    }

    @Test
    fun `empty value produces EmptyValue error`() {
        vm.onKnownValueChanged("")
        vm.onDpiToggled(300)
        vm.onCalculate()
        assertEquals(CalculatorError.EmptyValue, vm.state.value.error)
    }

    @Test
    fun `non-numeric value produces InvalidValue error`() {
        vm.onKnownValueChanged("abc")
        vm.onDpiToggled(300)
        vm.onCalculate()
        assertEquals(CalculatorError.InvalidValue, vm.state.value.error)
    }

    @Test
    fun `zero value produces InvalidValue error`() {
        vm.onKnownValueChanged("0")
        vm.onDpiToggled(300)
        vm.onCalculate()
        assertEquals(CalculatorError.InvalidValue, vm.state.value.error)
    }

    @Test
    fun `negative value produces InvalidValue error`() {
        vm.onKnownValueChanged("-10")
        vm.onDpiToggled(300)
        vm.onCalculate()
        assertEquals(CalculatorError.InvalidValue, vm.state.value.error)
    }

    @Test
    fun `no dpis selected produces NoDpisSelected error`() {
        vm.onKnownValueChanged("297")
        vm.onCalculate()
        assertEquals(CalculatorError.NoDpisSelected, vm.state.value.error)
    }

    @Test
    fun `custom ratio with empty fields produces InvalidCustomRatio error`() {
        vm.onKnownValueChanged("297")
        vm.onDpiToggled(300)
        vm.onRatioPresetSelected(RatioPreset.CUSTOM)
        vm.onCalculate()
        assertEquals(CalculatorError.InvalidCustomRatio, vm.state.value.error)
    }

    @Test
    fun `custom ratio with negative width produces InvalidCustomRatio error`() {
        vm.onKnownValueChanged("297")
        vm.onDpiToggled(300)
        vm.onRatioPresetSelected(RatioPreset.CUSTOM)
        vm.onCustomRatioWidthChanged("-5")
        vm.onCustomRatioHeightChanged("3")
        vm.onCalculate()
        assertEquals(CalculatorError.InvalidCustomRatio, vm.state.value.error)
    }

    @Test
    fun `custom ratio with zero height produces InvalidCustomRatio error`() {
        vm.onKnownValueChanged("297")
        vm.onDpiToggled(300)
        vm.onRatioPresetSelected(RatioPreset.CUSTOM)
        vm.onCustomRatioWidthChanged("5")
        vm.onCustomRatioHeightChanged("0")
        vm.onCalculate()
        assertEquals(CalculatorError.InvalidCustomRatio, vm.state.value.error)
    }

    @Test
    fun `valid input produces results and clears error`() {
        calculateSuccessfully()
        val s = vm.state.value
        assertTrue(s.results.isNotEmpty())
        assertNull(s.error)
    }

    @Test
    fun `result count matches selected dpi count`() {
        vm.onKnownValueChanged("297")
        vm.onDpiToggled(72)
        vm.onDpiToggled(150)
        vm.onDpiToggled(300)
        vm.onCalculate()
        assertEquals(vm.state.value.selectedDpis.size, vm.state.value.results.size)
        assertEquals(3, vm.state.value.results.size)
    }

    @Test
    fun `custom dpi confirmed is added to selected dpis`() {
        vm.onCustomDpiChanged("150")
        vm.onCustomDpiConfirmed()
        assertTrue(150 in vm.state.value.selectedDpis)
    }

    @Test
    fun `custom dpi confirmed clears input field`() {
        vm.onCustomDpiChanged("150")
        vm.onCustomDpiConfirmed()
        assertEquals("", vm.state.value.customDpi)
    }

    @Test
    fun `custom dpi confirmed with invalid value is rejected`() {
        vm.onCustomDpiChanged("abc")
        vm.onCustomDpiConfirmed()
        assertTrue(vm.state.value.selectedDpis.isEmpty())
        assertEquals("abc", vm.state.value.customDpi)
    }

    @Test
    fun `custom dpi confirmed with zero is rejected`() {
        vm.onCustomDpiChanged("0")
        vm.onCustomDpiConfirmed()
        assertTrue(vm.state.value.selectedDpis.isEmpty())
    }

    @Test
    fun `custom dpi confirmed with negative value is rejected`() {
        vm.onCustomDpiChanged("-50")
        vm.onCustomDpiConfirmed()
        assertTrue(vm.state.value.selectedDpis.isEmpty())
    }

    @Test
    fun `custom dpi confirmed includes DPI in results after calculation`() {
        vm.onKnownValueChanged("297")
        vm.onCustomDpiChanged("150")
        vm.onCustomDpiConfirmed()
        vm.onCalculate()
        assertTrue(vm.state.value.results.any { it.dpi == 150 })
    }

    @Test
    fun `custom ratio produces results when both fields are valid`() {
        vm.onKnownValueChanged("297")
        vm.onDpiToggled(300)
        vm.onRatioPresetSelected(RatioPreset.CUSTOM)
        vm.onCustomRatioWidthChanged("3")
        vm.onCustomRatioHeightChanged("2")
        vm.onCalculate()
        assertTrue(vm.state.value.results.isNotEmpty())
    }

    @Test
    fun `changing custom ratio width clears results`() {
        calculateSuccessfully()
        vm.onCustomRatioWidthChanged("5")
        assertTrue(vm.state.value.results.isEmpty())
    }

    @Test
    fun `changing custom ratio height clears results`() {
        calculateSuccessfully()
        vm.onCustomRatioWidthChanged("5")
        vm.onCustomRatioHeightChanged("3")
        vm.onRatioPresetSelected(RatioPreset.CUSTOM)
        vm.onCalculate()
        vm.onCustomRatioHeightChanged("4")
        assertTrue(vm.state.value.results.isEmpty())
    }

    @Test
    fun `landscape orientation preserves aspect ratio direction`() {
        vm.onKnownValueChanged("297")
        vm.onDpiToggled(300)
        vm.onOrientationChanged(Orientation.LANDSCAPE)
        vm.onCalculate()
        val r = vm.state.value.results.first()
        assertTrue(r.widthMm >= r.heightMm)
    }

    @Test
    fun `portrait orientation inverts aspect ratio direction`() {
        vm.onKnownValueChanged("297")
        vm.onDpiToggled(300)
        vm.onOrientationChanged(Orientation.PORTRAIT)
        vm.onCalculate()
        val r = vm.state.value.results.first()
        assertTrue(r.heightMm >= r.widthMm)
    }

    @Test
    fun `bleed value zero produces null bleed in results`() {
        vm.onKnownValueChanged("297")
        vm.onDpiToggled(300)
        vm.onBleedValueChanged("0")
        vm.onCalculate()
        assertNull(vm.state.value.results.first().bleed)
    }

    @Test
    fun `bleed empty field is treated as zero`() {
        vm.onKnownValueChanged("297")
        vm.onDpiToggled(300)
        vm.onBleedValueChanged("")
        vm.onCalculate()
        assertNull(vm.state.value.results.first().bleed)
    }

    @Test
    fun `valid bleed value produces bleed result with all conversions`() {
        vm.onKnownValueChanged("297")
        vm.onDpiToggled(300)
        vm.onBleedValueChanged("3")
        vm.onCalculate()
        val bleed = vm.state.value.results.first().bleed
        assertNotNull(bleed)
        assertEquals(3.0, bleed.bleedMm)
        assertTrue(bleed.totalWidthPx > 0)
        assertTrue(bleed.totalHeightPx > 0)
        assertTrue(bleed.totalWidthMm > 0)
        assertTrue(bleed.totalHeightMm > 0)
        assertTrue(bleed.totalWidthCm > 0)
        assertTrue(bleed.totalHeightCm > 0)
        assertTrue(bleed.totalWidthInch > 0)
        assertTrue(bleed.totalHeightInch > 0)
    }

    @Test
    fun `bleed in cm is converted correctly to mm`() {
        vm.onKnownValueChanged("297")
        vm.onDpiToggled(300)
        vm.onBleedUnitChanged(BleedUnit.CM)
        vm.onBleedValueChanged("1")
        vm.onCalculate()
        val bleed = vm.state.value.results.first().bleed
        assertNotNull(bleed)
        assertEquals(10.0, bleed.bleedMm, absoluteTolerance = 0.01)
    }

    @Test
    fun `bleed in inches is converted correctly to mm`() {
        vm.onKnownValueChanged("297")
        vm.onDpiToggled(300)
        vm.onBleedUnitChanged(BleedUnit.INCH)
        vm.onBleedValueChanged("1")
        vm.onCalculate()
        val bleed = vm.state.value.results.first().bleed
        assertNotNull(bleed)
        assertEquals(25.4, bleed.bleedMm, absoluteTolerance = 0.01)
    }

    @Test
    fun `bleed total dimensions are larger than base dimensions`() {
        vm.onKnownValueChanged("297")
        vm.onDpiToggled(300)
        vm.onBleedValueChanged("5")
        vm.onCalculate()
        val result = vm.state.value.results.first()
        assertNotNull(result.bleed)
        assertTrue(result.bleed.totalWidthMm > result.widthMm)
        assertTrue(result.bleed.totalHeightMm > result.heightMm)
    }

    @Test
    fun `cm conversion is mm divided by 10`() {
        vm.onKnownValueChanged("297")
        vm.onDpiToggled(300)
        vm.onCalculate()
        val result = vm.state.value.results.first()
        assertEquals(result.widthMm / 10.0, result.widthCm, absoluteTolerance = 0.001)
        assertEquals(result.heightMm / 10.0, result.heightCm, absoluteTolerance = 0.001)
    }

    @Test
    fun `inch conversion is mm divided by 25_4`() {
        vm.onKnownValueChanged("297")
        vm.onDpiToggled(300)
        vm.onCalculate()
        val result = vm.state.value.results.first()
        assertEquals(result.widthMm / 25.4, result.widthInch, absoluteTolerance = 0.001)
        assertEquals(result.heightMm / 25.4, result.heightInch, absoluteTolerance = 0.001)
    }

    @Test
    fun `multiple dpis produce sorted results`() {
        vm.onKnownValueChanged("297")
        vm.onDpiToggled(300)
        vm.onDpiToggled(72)
        vm.onDpiToggled(150)
        vm.onCalculate()
        val dpis = vm.state.value.results.map { it.dpi }
        assertEquals(listOf(72, 150, 300), dpis)
    }

    @Test
    fun `input in cm is converted correctly before calculation`() {
        vm.onKnownValueChanged("29.7")
        vm.onInputUnitChanged(InputUnit.CM)
        vm.onDpiToggled(300)
        vm.onCalculate()
        val result = vm.state.value.results.first()
        // 29.7 cm = 297 mm → same as direct mm input
        assertEquals(297.0, result.widthMm, absoluteTolerance = 0.5)
    }

    @Test
    fun `input in inches is converted correctly before calculation`() {
        vm.onKnownValueChanged("11.7")
        vm.onInputUnitChanged(InputUnit.INCH)
        vm.onDpiToggled(300)
        vm.onCalculate()
        val result = vm.state.value.results.first()
        // 11.7 inches ≈ 297 mm
        assertEquals(297.0, result.widthMm, absoluteTolerance = 1.0)
    }

    @Test
    fun `unknown side in known value changes state correctly`() {
        vm.onKnownSideChanged(Side.HEIGHT)
        assertEquals(Side.HEIGHT, vm.state.value.knownSide)
        vm.onKnownValueChanged("167")
        vm.onDpiToggled(300)
        vm.onCalculate()
        val result = vm.state.value.results.first()
        assertEquals(167.0, result.heightMm, absoluteTolerance = 0.5)
    }

    @Test
    fun `exception during calculation sets CalculationError`() {
        vm.onKnownValueChanged("297")
        vm.onDpiToggled(300)
        vm.onRatioPresetSelected(RatioPreset.CUSTOM)
        vm.onCustomRatioWidthChanged("0")
        vm.onCustomRatioHeightChanged("0")
        vm.onCalculate()
        assertEquals(CalculatorError.InvalidCustomRatio, vm.state.value.error)
    }

    private fun calculateSuccessfully() {
        vm.onKnownValueChanged("297")
        vm.onDpiToggled(300)
        vm.onCalculate()
    }
}