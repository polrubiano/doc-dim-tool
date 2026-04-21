package pol.rubiano.docdimtool.app.domain.models

data class ReverseDocumentSpecifications(
    val knownSide: Side,
    val knownValue: Double,
    val inputUnit: InputUnit,
    val ratio: DocumentAspectRatio,
    val dpis: List<Int>,
    val orientation: Orientation = Orientation.LANDSCAPE,
    val bleedMm: Double = 0.0,
) {
    init {
        require(knownValue > 0) { "Known value must be positive" }
        require(inputUnit != InputUnit.PX) { "Use forward calculation for pixels" }
        require(dpis.isNotEmpty()) { "At least one DPI value is required" }
        require(dpis.all { it > 0 }) { "All DPI values must be positive" }
        require(bleedMm >= 0) { "Bleed must be zero or positive" }
    }
}