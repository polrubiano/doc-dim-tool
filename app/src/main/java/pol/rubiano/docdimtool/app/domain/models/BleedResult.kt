package pol.rubiano.docdimtool.app.domain.models

data class BleedResult(
    val bleedMm: Double,
    val totalWidthPx: Double,
    val totalHeightPx: Double,
    val totalWidthMm: Double,
    val totalHeightMm: Double,
    val totalWidthCm: Double,
    val totalHeightCm: Double,
    val totalWidthInch: Double,
    val totalHeightInch: Double,
)