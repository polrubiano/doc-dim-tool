package pol.rubiano.docdimtool.app.domain.models

data class DocumentResult(
    val dpi: Int,
    val widthPx: Double,
    val heightPx: Double,
    val widthMm: Double,
    val heightMm: Double,
    val widthCm: Double,
    val heightCm: Double,
    val widthInch: Double,
    val heightInch: Double,
    val usageHint: DpiUsageHint,
    val bleed: BleedResult? = null,
)
