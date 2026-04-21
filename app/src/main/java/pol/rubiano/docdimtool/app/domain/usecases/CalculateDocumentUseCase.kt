package pol.rubiano.docdimtool.app.domain.usecases

import pol.rubiano.docdimtool.app.domain.models.*

class CalculateDocumentUseCase {

    operator fun invoke(spec: DocumentSpecifications): List<DocumentResult> {
        val (widthPx, heightPx) = resolveDimensions(spec)

        return spec.dpis.map { dpi ->
            val wMm = pxToMm(widthPx, dpi)
            val hMm = pxToMm(heightPx, dpi)

            DocumentResult(
                dpi = dpi,
                widthPx = widthPx,
                heightPx = heightPx,
                widthMm = wMm,
                heightMm = hMm,
                widthCm = wMm / 10.0,
                heightCm = hMm / 10.0,
                widthInch = mmToInch(wMm),
                heightInch = mmToInch(hMm),
                usageHint = DpiUsageHint.from(dpi),
                bleed = if (spec.bleedMm > 0) resolveBleed(
                    widthPx,
                    heightPx,
                    spec.bleedMm,
                    dpi
                ) else null,
            )
        }
    }

    private fun resolveDimensions(spec: DocumentSpecifications): Pair<Double, Double> {
        val knownPx = resolveKnownPx(spec)
        val ratio = resolveOrientedRatio(spec)

        val (widthPx, heightPx) = when (spec.knownSide) {
            Side.WIDTH -> knownPx to knownPx * (ratio.height.toDouble() / ratio.width)
            Side.HEIGHT -> knownPx * (ratio.width.toDouble() / ratio.height) to knownPx
        }

        return widthPx to heightPx
    }

    private fun resolveKnownPx(spec: DocumentSpecifications): Double = when (spec.inputUnit) {
        InputUnit.PX -> spec.knownValue
        // For physical units, px is resolved once from the first DPI.
        // px count is DPI-independent — only the physical output size changes per DPI.
        InputUnit.MM -> spec.dpis.first().let { mmToPx(spec.knownValue, it) }
        InputUnit.CM -> spec.dpis.first().let { mmToPx(spec.knownValue * 10.0, it) }
        InputUnit.INCH -> spec.dpis.first().let { spec.knownValue * it }
    }

    /**
     * Applies orientation to the ratio so that:
     * - LANDSCAPE → wider side is always W
     * - PORTRAIT  → taller side is always H
     */
    private fun resolveOrientedRatio(spec: DocumentSpecifications): DocumentAspectRatio {
        val ratio = spec.ratio
        return when (spec.orientation) {
            Orientation.LANDSCAPE ->
                if (ratio.width >= ratio.height) ratio
                else DocumentAspectRatio(ratio.height, ratio.width)

            Orientation.PORTRAIT ->
                if (ratio.height >= ratio.width) ratio
                else DocumentAspectRatio(ratio.height, ratio.width)
        }
    }

    private fun resolveBleed(
        widthPx: Double,
        heightPx: Double,
        bleedMm: Double,
        dpi: Int,
    ): BleedResult {
        val bleedPx = mmToPx(bleedMm, dpi) * 2
        val totalWidthMm = pxToMm(widthPx, dpi) + bleedMm * 2
        val totalHeightMm = pxToMm(heightPx, dpi) + bleedMm * 2

        return BleedResult(
            bleedMm           = bleedMm,
            totalWidthPx      = widthPx + bleedPx,
            totalHeightPx     = heightPx + bleedPx,
            totalWidthMm      = totalWidthMm,
            totalHeightMm     = totalHeightMm,
            totalWidthCm      = totalWidthMm / 10.0,
            totalHeightCm     = totalHeightMm / 10.0,
            totalWidthInch    = mmToInch(totalWidthMm),
            totalHeightInch   = mmToInch(totalHeightMm),
        )
    }

    private fun pxToMm(px: Double, dpi: Int): Double = (px / dpi) * 25.4
    private fun mmToPx(mm: Double, dpi: Int): Double = (mm / 25.4) * dpi
    private fun mmToInch(mm: Double): Double = mm / 25.4
}