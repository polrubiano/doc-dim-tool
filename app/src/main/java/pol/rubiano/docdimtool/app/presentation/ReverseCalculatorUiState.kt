package pol.rubiano.docdimtool.app.presentation

import pol.rubiano.docdimtool.app.domain.models.*

data class ReverseCalculatorUiState(
    val knownSide: Side = Side.WIDTH,
    val knownValue: String = "",
    val inputUnit: InputUnit = InputUnit.MM,  // Default MM para reverse
    val ratioPreset: RatioPreset = RatioPreset.RATIO_16_9,
    val customRatioWidth: String = "",
    val customRatioHeight: String = "",
    val selectedDpis: Set<Int> = emptySet(),
    val customDpi: String = "",
    val orientation: Orientation = Orientation.LANDSCAPE,
    val bleedValue: String = "",
    val bleedUnit: BleedUnit = BleedUnit.MM,
    val results: List<ReverseDocumentResult> = emptyList(),
    val error: CalculatorError? = null,
)