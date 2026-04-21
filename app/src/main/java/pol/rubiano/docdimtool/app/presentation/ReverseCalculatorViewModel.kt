package pol.rubiano.docdimtool.app.presentation

import androidx.lifecycle.ViewModel
import pol.rubiano.docdimtool.app.domain.models.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import pol.rubiano.docdimtool.app.domain.usecases.ReverseCalculateDocumentUseCase

class ReverseCalculatorViewModel(
    private val reverseCalculateDocument: ReverseCalculateDocumentUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(ReverseCalculatorUiState())
    val state = _state.asStateFlow()

    fun onKnownSideChanged(side: Side) =
        _state.update { it.copy(knownSide = side, results = emptyList()) }

    fun onKnownValueChanged(value: String) =
        _state.update { it.copy(knownValue = value, error = null, results = emptyList()) }

    fun onInputUnitChanged(unit: InputUnit) {
        if (unit == InputUnit.PX) return
        _state.update { it.copy(inputUnit = unit, results = emptyList()) }
    }

    fun onRatioPresetSelected(preset: RatioPreset) =
        _state.update { it.copy(ratioPreset = preset, results = emptyList()) }

    fun onCustomRatioWidthChanged(value: String) =
        _state.update { it.copy(customRatioWidth = value, results = emptyList()) }

    fun onCustomRatioHeightChanged(value: String) =
        _state.update { it.copy(customRatioHeight = value, results = emptyList()) }

    fun onDpiToggled(dpi: Int) = _state.update { s ->
        val updated = if (dpi in s.selectedDpis) s.selectedDpis - dpi else s.selectedDpis + dpi
        s.copy(selectedDpis = updated, results = emptyList())
    }

    fun onCustomDpiChanged(value: String) =
        _state.update { it.copy(customDpi = value) }

    fun onCustomDpiConfirmed() {
        val dpi = _state.value.customDpi.toIntOrNull() ?: return
        if (dpi <= 0) return
        _state.update {
            it.copy(
                selectedDpis = it.selectedDpis + dpi,
                customDpi = "",
                results = emptyList()
            )
        }
    }

    fun onOrientationChanged(orientation: Orientation) =
        _state.update { it.copy(orientation = orientation, results = emptyList(), error = null) }

    fun onBleedValueChanged(value: String) =
        _state.update { it.copy(bleedValue = value, results = emptyList(), error = null) }

    fun onBleedUnitChanged(unit: BleedUnit) =
        _state.update { it.copy(bleedUnit = unit, results = emptyList(), error = null) }

    fun onCalculate() {
        val s = _state.value
        val spec = buildSpec(s) ?: run {
            return
        }
        try {
            val results = reverseCalculateDocument(spec)
            _state.update { it.copy(results = results, error = null) }
        } catch (_: Exception) {
            _state.update {
                it.copy(error = CalculatorError.CalculationError)
            }
        }
    }

    private fun buildSpec(s: ReverseCalculatorUiState): ReverseDocumentSpecifications? {
        if (s.knownValue.isBlank()) {
            _state.update { it.copy(error = CalculatorError.EmptyValue) }
            return null
        }

        val value = s.knownValue.toDoubleOrNull()
        if (value == null || value <= 0) {
            _state.update { it.copy(error = CalculatorError.InvalidValue) }
            return null
        }

        val ratio = resolveRatio(s) ?: return null

        val dpis = buildDpiList(s)
        if (dpis.isEmpty()) {
            _state.update { it.copy(error = CalculatorError.NoDpisSelected) }
            return null
        }

        val bleedMm = if (s.bleedValue.isBlank()) {
            0.0
        } else {
            val bleedValue = s.bleedValue.toDoubleOrNull() ?: 0.0
            s.bleedUnit.toMm(bleedValue)
        }

        return ReverseDocumentSpecifications(
            knownSide = s.knownSide,
            knownValue = value,
            inputUnit = s.inputUnit,
            ratio = ratio,
            dpis = dpis,
            orientation = s.orientation,
            bleedMm = bleedMm,
        )
    }

    private fun resolveRatio(s: ReverseCalculatorUiState): DocumentAspectRatio? {
        if (s.ratioPreset != RatioPreset.CUSTOM) return s.ratioPreset.ratio

        val w = s.customRatioWidth.toIntOrNull()
        val h = s.customRatioHeight.toIntOrNull()
        if (w == null || h == null || w <= 0 || h <= 0) {
            _state.update { it.copy(error = CalculatorError.InvalidCustomRatio) }
            return null
        }
        return DocumentAspectRatio(w, h)
    }

    private fun buildDpiList(s: ReverseCalculatorUiState): List<Int> {
        val extra = s.customDpi.toIntOrNull()?.takeIf { it > 0 }
        return (s.selectedDpis + listOfNotNull(extra)).sorted()
    }
}