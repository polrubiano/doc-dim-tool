package pol.rubiano.docdimtool.app.presentation

import pol.rubiano.docdimtool.app.domain.models.*

data class CalculatorUiState(
    val knownSide: Side = Side.WIDTH,
    val knownValue: String = "",
    val inputUnit: InputUnit = InputUnit.PX,
    val ratioPreset: RatioPreset = RatioPreset.RATIO_16_9,
    val customRatioWidth: String = "",
    val customRatioHeight: String = "",
    val selectedDpis: Set<Int> = emptySet(),
    val customDpi: String = "",
    val orientation: Orientation = Orientation.LANDSCAPE,
    val bleedValue: String = "",
    val bleedUnit: BleedUnit = BleedUnit.MM,
    val results: List<DocumentResult> = emptyList(),
    val error: CalculatorError? = null,
)

enum class RatioPreset(val label: String, val ratio: DocumentAspectRatio?) {
    RATIO_16_9("16:9",   DocumentAspectRatio.RATIO_16_9),
    RATIO_4_3("4:3",     DocumentAspectRatio.RATIO_4_3),
    RATIO_1_1("1:1",     DocumentAspectRatio.RATIO_1_1),
    RATIO_9_16("9:16",   DocumentAspectRatio.RATIO_9_16),
    RATIO_A4("A4",       DocumentAspectRatio.RATIO_A4),
    RATIO_LETTER("Letter", DocumentAspectRatio.RATIO_LETTER),
    CUSTOM("Custom",     null),
}

enum class BleedUnit {
    MM, CM, INCH;

    fun toMm(value: Double): Double = when (this) {
        MM -> value
        CM -> value * 10.0
        INCH -> value * 25.4
    }
}

sealed interface CalculatorError {
    data object EmptyValue : CalculatorError
    data object InvalidValue : CalculatorError
    data object InvalidCustomRatio : CalculatorError
    data object NoDpisSelected : CalculatorError
    data object CalculationError : CalculatorError
}