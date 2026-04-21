package pol.rubiano.docdimtool.app.presentation

import pol.rubiano.docdimtool.app.domain.models.*
import pol.rubiano.docdimtool.app.domain.usecases.CalculateDocumentUseCase
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
class CalculatorViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private lateinit var vm: CalculatorViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
        vm = CalculatorViewModel(CalculateDocumentUseCase())
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has no results and no error`() {
        val s = vm.state.value
        assertTrue(s.results.isEmpty())
        assertNull(s.error)
    }

    @Test
    fun `changing known value clears results`() {
        calculateSuccessfully()
        vm.onKnownValueChanged("1280")
        assertTrue(vm.state.value.results.isEmpty())
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
        vm.onDpiToggled(72)
        assertTrue(vm.state.value.results.isEmpty())
    }

    @Test
    fun `changing orientation clears results and error`() {
        calculateSuccessfully()
        vm.onOrientationChanged(Orientation.PORTRAIT)
        assertTrue(vm.state.value.results.isEmpty())
        assertNull(vm.state.value.error)
    }

    @Test
    fun `changing bleed clears results and error`() {
        calculateSuccessfully()
        vm.onBleedValueChanged("5")
        assertTrue(vm.state.value.results.isEmpty())
        assertNull(vm.state.value.error)
    }

    @Test
    fun `changing orientation clears pre-existing error`() {
        vm.onKnownValueChanged("")
        vm.onCalculate()
        assertNotNull(vm.state.value.error)
        vm.onOrientationChanged(Orientation.PORTRAIT)
        assertNull(vm.state.value.error)
    }

    @Test
    fun `changing bleed clears pre-existing error`() {
        vm.onKnownValueChanged("")
        vm.onCalculate()
        assertNotNull(vm.state.value.error)
        vm.onBleedValueChanged("3")
        assertNull(vm.state.value.error)
    }

    @Test
    fun `empty value produces EmptyValue error`() {
        vm.onKnownValueChanged("")
        vm.onCalculate()
        assertEquals(CalculatorError.EmptyValue, vm.state.value.error)
    }

    @Test
    fun `non-numeric value produces InvalidValue error`() {
        vm.onKnownValueChanged("abc")
        vm.onCalculate()
        assertEquals(CalculatorError.InvalidValue, vm.state.value.error)
    }

    @Test
    fun `zero value produces InvalidValue error`() {
        vm.onKnownValueChanged("0")
        vm.onCalculate()
        assertEquals(CalculatorError.InvalidValue, vm.state.value.error)
    }

    @Test
    fun `no dpis selected produces NoDpisSelected error`() {
        vm.onKnownValueChanged("1920")
        // Deselect all default dpis
        vm.state.value.selectedDpis.toList().forEach { vm.onDpiToggled(it) }
        vm.onCalculate()
        assertEquals(CalculatorError.NoDpisSelected, vm.state.value.error)
    }

    @Test
    fun `custom ratio with empty fields produces InvalidCustomRatio error`() {
        vm.onKnownValueChanged("1920")
        vm.onRatioPresetSelected(RatioPreset.CUSTOM)
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
        vm.onKnownValueChanged("1920")
        vm.onDpiToggled(72)
        vm.onDpiToggled(150)
        vm.onDpiToggled(300)
        vm.onCalculate()
        assertEquals(vm.state.value.selectedDpis.size, vm.state.value.results.size)
    }

    @Test
    fun `custom dpi confirmed is included in results`() {
        vm.onKnownValueChanged("1920")
        vm.onCustomDpiChanged("150")
        vm.onCustomDpiConfirmed()
        vm.onCalculate()
        assertTrue(vm.state.value.results.any { it.dpi == 150 })
    }

    @Test
    fun `custom ratio produces results when both fields are valid`() {
        vm.onKnownValueChanged("1920")
        vm.onDpiToggled(300)
        vm.onRatioPresetSelected(RatioPreset.CUSTOM)
        vm.onCustomRatioWidthChanged("3")
        vm.onCustomRatioHeightChanged("2")
        vm.onCalculate()
        assertTrue(vm.state.value.results.isNotEmpty())
    }

    @Test
    fun `portrait orientation produces taller than wide results`() {
        vm.onKnownValueChanged("1920")
        vm.onDpiToggled(300)
        vm.onOrientationChanged(Orientation.PORTRAIT)
        vm.onCalculate()
        val r = vm.state.value.results.first()
        assertTrue(r.heightPx > r.widthPx)
    }

    @Test
    fun `bleed value zero produces null bleed in results`() {
        vm.onKnownValueChanged("1920")
        vm.onDpiToggled(300)
        vm.onBleedValueChanged("0")
        vm.onCalculate()
        assertNull(vm.state.value.results.first().bleed)
    }

    @Test
    fun `valid bleed value produces bleed result with all conversions`() {
        vm.onKnownValueChanged("1920")
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
    fun `bleed correctly converts to multiple units`() {
        vm.onKnownValueChanged("1920")
        vm.onDpiToggled(300)
        vm.onBleedValueChanged("10")
        vm.onCalculate()
        val bleed = vm.state.value.results.first().bleed
        assertNotNull(bleed)
        // Verify that cm is correctly converted from mm (10mm of bleed = 1cm)
        assertEquals(bleed.totalWidthMm / 10.0, bleed.totalWidthCm, absoluteTolerance = 0.01)
        assertEquals(bleed.totalHeightMm / 10.0, bleed.totalHeightCm, absoluteTolerance = 0.01)
    }

    @Test
    fun `bleed total dimensions are larger than base dimensions`() {
        vm.onKnownValueChanged("1920")
        vm.onDpiToggled(300)
        vm.onBleedValueChanged("5")
        vm.onCalculate()
        val result = vm.state.value.results.first()
        assertNotNull(result.bleed)
        assertTrue(result.bleed.totalWidthPx > result.widthPx)
        assertTrue(result.bleed.totalHeightPx > result.heightPx)
        assertTrue(result.bleed.totalWidthMm > result.widthMm)
        assertTrue(result.bleed.totalHeightMm > result.heightMm)
    }

    private fun calculateSuccessfully() {
        vm.onKnownValueChanged("1920")
        vm.onDpiToggled(300)
        vm.onCalculate()
    }
}