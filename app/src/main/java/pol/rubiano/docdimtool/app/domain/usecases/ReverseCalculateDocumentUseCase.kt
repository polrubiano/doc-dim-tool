package pol.rubiano.docdimtool.app.domain.usecases

import pol.rubiano.docdimtool.app.domain.models.*

class ReverseCalculateDocumentUseCase {

    operator fun invoke(spec: ReverseDocumentSpecifications): List<ReverseDocumentResult> {
        val (widthPx, heightPx) = resolveDimensions(spec)

        return spec.dpis.map { dpi ->
            val wMm = pxToMm(widthPx, dpi)
            val hMm = pxToMm(heightPx, dpi)

            ReverseDocumentResult(
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

    private fun resolveDimensions(spec: ReverseDocumentSpecifications): Pair<Double, Double> {
        val knownMm = spec.knownValue.let { value ->
            when (spec.inputUnit) {
                InputUnit.MM -> value
                InputUnit.CM -> value * 10.0
                InputUnit.INCH -> value * 25.4
                InputUnit.PX -> error("Use forward calculation for pixels")
            }
        }

        val orientedRatio = resolveOrientedRatio(spec)

        val (widthMm, heightMm) = when (spec.knownSide) {
            Side.WIDTH -> knownMm to knownMm * (orientedRatio.height.toDouble() / orientedRatio.width)
            Side.HEIGHT -> knownMm * (orientedRatio.width.toDouble() / orientedRatio.height) to knownMm
        }

        val dpi = spec.dpis.first()
        val widthPx = mmToPx(widthMm, dpi)
        val heightPx = mmToPx(heightMm, dpi)

        return widthPx to heightPx
    }

    private fun resolveOrientedRatio(spec: ReverseDocumentSpecifications): DocumentAspectRatio {
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
            bleedMm = bleedMm,
            totalWidthPx = widthPx + bleedPx,
            totalHeightPx = heightPx + bleedPx,
            totalWidthMm = totalWidthMm,
            totalHeightMm = totalHeightMm,
            totalWidthCm = totalWidthMm / 10.0,
            totalHeightCm = totalHeightMm / 10.0,
            totalWidthInch = mmToInch(totalWidthMm),
            totalHeightInch = mmToInch(totalHeightMm),
        )
    }

    private fun pxToMm(px: Double, dpi: Int): Double = (px / dpi) * 25.4
    private fun mmToPx(mm: Double, dpi: Int): Double = (mm / 25.4) * dpi
    private fun mmToInch(mm: Double): Double = mm / 25.4
}