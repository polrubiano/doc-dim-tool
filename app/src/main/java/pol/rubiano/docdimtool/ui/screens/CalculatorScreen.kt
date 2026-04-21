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
import pol.rubiano.docdimtool.app.presentation.BleedUnit
import pol.rubiano.docdimtool.app.presentation.CalculatorError
import pol.rubiano.docdimtool.app.presentation.CalculatorViewModel
import pol.rubiano.docdimtool.app.presentation.RatioPreset
import pol.rubiano.docdimtool.ui.cards.ResultCard

@Composable
fun CalculatorScreen(
    modifier: Modifier = Modifier,
    viewModel: CalculatorViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Text("Calculator", style = MaterialTheme.typography.headlineMedium)

        RatioSection(
            selected = state.ratioPreset,
            customWidth  = state.customRatioWidth,
            customHeight  = state.customRatioHeight,
            onPreset = viewModel::onRatioPresetSelected,
            onCustomWidth = viewModel::onCustomRatioWidthChanged,
            onCustomHeight = viewModel::onCustomRatioHeightChanged,
        )

        KnownValueSection(
            knownSide   = state.knownSide,
            value       = state.knownValue,
            unit        = state.inputUnit,
            error       = state.error,
            onSide      = viewModel::onKnownSideChanged,
            onValue     = viewModel::onKnownValueChanged,
            onUnit      = viewModel::onInputUnitChanged,
        )

        DpiSection(
            selected    = state.selectedDpis,
            customValue = state.customDpi,
            onToggle    = viewModel::onDpiToggled,
            onCustomChange   = viewModel::onCustomDpiChanged,
            onCustomConfirm  = viewModel::onCustomDpiConfirmed,
        )

        OrientationSection(
            orientation = state.orientation,
            onChange    = viewModel::onOrientationChanged,
        )

        BleedSection(
            value    = state.bleedValue,
            unit     = state.bleedUnit,
            onValueChange = viewModel::onBleedValueChanged,
            onUnitChange  = viewModel::onBleedUnitChanged,
        )

        Button(
            onClick  = viewModel::onCalculate,
            modifier = Modifier.fillMaxWidth(),
            enabled  = state.selectedDpis.isNotEmpty(),
        ) {
            Text("Calculate")
        }

        state.error?.let { ErrorBanner(it) }

        if (state.results.isNotEmpty()) {
            ResultsSection(state.results)
        }
    }
}

@Composable
private fun ResultsSection(results: List<DocumentResult>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text  = "Results",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
        )
        HorizontalDivider()
        results.forEach { result ->
            ResultCard(result)
        }
    }
}

// --- Sections -----------------------------------------------------------------

@Composable
private fun RatioSection(
    selected: RatioPreset,
    customWidth: String,
    customHeight: String,
    onPreset: (RatioPreset) -> Unit,
    onCustomWidth: (String) -> Unit,
    onCustomHeight: (String) -> Unit,
) {
    FormSection(title = "Aspect Ratio") {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            RatioPreset.entries.forEach { preset ->
                FilterChip(
                    selected = selected == preset,
                    onClick  = { onPreset(preset) },
                    label    = { Text(preset.label) },
                )
            }
        }

        if (selected == RatioPreset.CUSTOM) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value         = customWidth,
                    onValueChange = onCustomWidth,
                    label         = { Text("W") },
                    modifier      = Modifier.weight(1f),
                    keyboardOptions = numericOptions(),
                )
                Text(":", style = MaterialTheme.typography.titleLarge)
                OutlinedTextField(
                    value         = customHeight,
                    onValueChange = onCustomHeight,
                    label         = { Text("H") },
                    modifier      = Modifier.weight(1f),
                    keyboardOptions = numericOptions(),
                )
            }
        }
    }
}

@Composable
private fun KnownValueSection(
    knownSide: Side,
    value: String,
    unit: InputUnit,
    error: CalculatorError?,
    onSide: (Side) -> Unit,
    onValue: (String) -> Unit,
    onUnit: (InputUnit) -> Unit,
) {
    FormSection(title = "Known Side") {
        // Side toggle
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Side.entries.forEach { side ->
                FilterChip(
                    selected = knownSide == side,
                    onClick  = { onSide(side) },
                    label    = { Text(side.name.lowercase().replaceFirstChar { it.uppercase() }) },
                )
            }
        }

        // Value + unit
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment     = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value         = value,
                onValueChange = onValue,
                label         = { Text("Value") },
                isError       = error is CalculatorError.EmptyValue || error is CalculatorError.InvalidValue,
                modifier      = Modifier.weight(1f),
                keyboardOptions = numericOptions(),
            )
            UnitDropdown(selected = unit, onChange = onUnit)
        }

        if (error is CalculatorError.InvalidValue) {
            Text("Enter a valid positive number", color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun DpiSection(
    selected: Set<Int>,
    customValue: String,
    onToggle: (Int) -> Unit,
    onCustomChange: (String) -> Unit,
    onCustomConfirm: () -> Unit,
) {
    val presets = listOf(72, 96, 150, 300, 600)

    FormSection(title = "DPI") {
        // Info text showing selected count
        if (selected.isNotEmpty()) {
            Text(
                text = "Selected: ${selected.sorted().joinToString(", ")}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp),
            )
        }

        // Preset chips
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            presets.forEach { dpi ->
                FilterChip(
                    selected = dpi in selected,
                    onClick  = { onToggle(dpi) },
                    label    = { Text("$dpi") },
                )
            }
        }

        // Custom DPI input
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment     = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value         = customValue,
                onValueChange = onCustomChange,
                label         = { Text("Custom DPI") },
                modifier      = Modifier.weight(1f),
                keyboardOptions = numericOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onCustomConfirm() }),
            )
            FilledTonalButton(onClick = onCustomConfirm) { Text("Add") }
        }

        // Show added custom dpis (those not in presets)
        val customDpis = selected.filter { it !in presets }.sorted()
        if (customDpis.isNotEmpty()) {
            Text(
                text = "Custom values:",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp),
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                customDpis.forEach { dpi ->
                    FilterChip(
                        selected = true,
                        onClick  = { onToggle(dpi) },
                        label    = { Text("$dpi ✕") },
                    )
                }
            }
        }
    }
}

@Composable
private fun OrientationSection(
    orientation: Orientation,
    onChange: (Orientation) -> Unit,
) {
    FormSection(title = "Orientation") {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Orientation.entries.forEach { o ->
                val icon = if (o == Orientation.LANDSCAPE) "↔" else "↕"
                FilterChip(
                    selected = orientation == o,
                    onClick  = { onChange(o) },
                    label    = { Text("$icon ${o.name.lowercase().replaceFirstChar { it.uppercase() }}") },
                )
            }
        }
    }
}

@Composable
private fun BleedSection(
    value: String,
    unit: BleedUnit,
    onValueChange: (String) -> Unit,
    onUnitChange: (BleedUnit) -> Unit,
) {
    FormSection(title = "Bleed (optional)") {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment     = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value         = value,
                onValueChange = onValueChange,
                label         = { Text("Bleed margin") },
                placeholder   = { Text("e.g. 3") },
                modifier      = Modifier.weight(1f),
                keyboardOptions = numericOptions(),
            )
            BleedUnitDropdown(selected = unit, onChange = onUnitChange)
        }
    }
}

@Composable
private fun BleedUnitDropdown(selected: BleedUnit, onChange: (BleedUnit) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        FilledTonalButton(onClick = { expanded = true }) {
            Text(selected.name)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            BleedUnit.entries.forEach { unit ->
                DropdownMenuItem(
                    text    = { Text(unit.name) },
                    onClick = { onChange(unit); expanded = false },
                )
            }
        }
    }
}

// --- Shared composables -------------------------------------------------------

@Composable
private fun FormSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(title, style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary)
        HorizontalDivider()
        content()
    }
}

@Composable
private fun UnitDropdown(selected: InputUnit, onChange: (InputUnit) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        FilledTonalButton(onClick = { expanded = true }) {
            Text(selected.name)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            InputUnit.entries.forEach { unit ->
                DropdownMenuItem(
                    text    = { Text(unit.name) },
                    onClick = { onChange(unit); expanded = false },
                )
            }
        }
    }
}

@Composable
private fun ErrorBanner(error: CalculatorError) {
    val message = when (error) {
        CalculatorError.EmptyValue         -> "Please enter a value"
        CalculatorError.InvalidValue       -> "Value must be a positive number"
        CalculatorError.InvalidCustomRatio -> "Enter valid W and H values for the custom ratio"
        CalculatorError.NoDpisSelected     -> "Select at least one DPI value"
        CalculatorError.CalculationError   -> "An error occurred during calculation"
    }
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
        Text(
            text     = message,
            color    = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier.padding(12.dp),
            style    = MaterialTheme.typography.bodyMedium,
        )
    }
}

// --- Helpers ------------------------------------------------------------------

private fun numericOptions(imeAction: ImeAction = ImeAction.Next) = KeyboardOptions(
    keyboardType = KeyboardType.Decimal,
    imeAction    = imeAction,
)