package pol.rubiano.docdimtool.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel
import pol.rubiano.docdimtool.app.domain.models.*
import pol.rubiano.docdimtool.app.presentation.*
import pol.rubiano.docdimtool.ui.cards.ReverseResultCard

@Composable
fun ReverseCalculatorScreen(
    modifier: Modifier = Modifier,
    viewModel: ReverseCalculatorViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Text("Reverse Calculator", style = MaterialTheme.typography.headlineMedium)

        Text(
            text = "Physical dimensions → Pixels",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        RatioSection(
            selected = state.ratioPreset,
            customWidth = state.customRatioWidth,
            customHeight = state.customRatioHeight,
            onPreset = viewModel::onRatioPresetSelected,
            onCustomWidth = viewModel::onCustomRatioWidthChanged,
            onCustomHeight = viewModel::onCustomRatioHeightChanged,
        )

        KnownValueSectionReverse(
            knownSide = state.knownSide,
            value = state.knownValue,
            unit = state.inputUnit,
            error = state.error,
            onSide = viewModel::onKnownSideChanged,
            onValue = viewModel::onKnownValueChanged,
            onUnit = viewModel::onInputUnitChanged,
        )

        DpiSection(
            selected = state.selectedDpis,
            customValue = state.customDpi,
            onToggle = viewModel::onDpiToggled,
            onCustomChange = viewModel::onCustomDpiChanged,
            onCustomConfirm = viewModel::onCustomDpiConfirmed,
        )

        OrientationSection(
            orientation = state.orientation,
            onChange = viewModel::onOrientationChanged,
        )

        BleedSection(
            value = state.bleedValue,
            unit = state.bleedUnit,
            onValueChange = viewModel::onBleedValueChanged,
            onUnitChange = viewModel::onBleedUnitChanged,
        )

        Button(
            onClick = viewModel::onCalculate,
            modifier = Modifier.fillMaxWidth(),
            enabled = state.selectedDpis.isNotEmpty(),
        ) {
            Text("Calculate")
        }

        state.error?.let { ErrorBanner(it) }

        if (state.results.isNotEmpty()) {
            ReverseResultsSection(state.results)
        }
    }
}

@Composable
private fun ReverseResultsSection(results: List<ReverseDocumentResult>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Results",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
        )
        HorizontalDivider()
        results.forEach { result ->
            ReverseResultCard(result)
        }
    }
}

@Composable
private fun KnownValueSectionReverse(
    knownSide: Side,
    value: String,
    unit: InputUnit,
    error: CalculatorError?,
    onSide: (Side) -> Unit,
    onValue: (String) -> Unit,
    onUnit: (InputUnit) -> Unit,
) {
    FormSection(title = "Known Physical Dimension") {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Side.entries.forEach { side ->
                FilterChip(
                    selected = knownSide == side,
                    onClick = { onSide(side) },
                    label = { Text(side.name.lowercase().replaceFirstChar { it.uppercase() }) },
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = onValue,
                label = { Text("Value") },
                isError = error is CalculatorError.EmptyValue || error is CalculatorError.InvalidValue,
                modifier = Modifier.weight(1f),
                keyboardOptions = numericOptions(),
            )
            PhysicalUnitDropdown(selected = unit, onChange = onUnit)
        }

        if (error is CalculatorError.InvalidValue) {
            Text(
                "Enter a valid positive number", color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun PhysicalUnitDropdown(selected: InputUnit, onChange: (InputUnit) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val physicalUnits = listOf(InputUnit.MM, InputUnit.CM, InputUnit.INCH)

    Box {
        FilledTonalButton(onClick = { expanded = true }) {
            Text(selected.name)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            physicalUnits.forEach { unit ->
                DropdownMenuItem(
                    text = { Text(unit.name) },
                    onClick = { onChange(unit); expanded = false },
                )
            }
        }
    }
}