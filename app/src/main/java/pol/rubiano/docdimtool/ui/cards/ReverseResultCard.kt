package pol.rubiano.docdimtool.ui.cards

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pol.rubiano.docdimtool.app.domain.models.BleedResult
import pol.rubiano.docdimtool.app.domain.models.DpiUsageHint
import pol.rubiano.docdimtool.app.domain.models.ReverseDocumentResult

@Composable
fun ReverseResultCard(result: ReverseDocumentResult) {
    AnimatedVisibility(
        visible = true,
        enter = fadeIn() + expandVertically(),
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                CardHeader(result)
                ProportionPreview(result)
                DimensionTable(result)
                result.bleed?.let { BleedRow(it) }
            }
        }
    }
}

@Composable
private fun CardHeader(result: ReverseDocumentResult) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "${result.dpi} DPI",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        )
        UsageHintBadge(result.usageHint)
    }
}

@Composable
private fun UsageHintBadge(hint: DpiUsageHint) {
    val (label, color) = when (hint) {
        DpiUsageHint.SCREEN -> "Screen" to MaterialTheme.colorScheme.tertiary
        DpiUsageHint.DRAFT_PRINT -> "Draft" to MaterialTheme.colorScheme.secondary
        DpiUsageHint.STANDARD_PRINT -> "Print" to MaterialTheme.colorScheme.primary
        DpiUsageHint.HIGH_RES_PRINT -> "High-res Print" to MaterialTheme.colorScheme.primary
        DpiUsageHint.ULTRA_PRINT -> "Ultra Print" to MaterialTheme.colorScheme.error
    }
    Surface(
        shape = RoundedCornerShape(50),
        color = color.copy(alpha = 0.15f),
    ) {
        Text(
            text = label,
            color = color,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
        )
    }
}

@Composable
private fun ProportionPreview(result: ReverseDocumentResult) {
    val surfaceColor = MaterialTheme.colorScheme.surface
    val primaryColor = MaterialTheme.colorScheme.primary
    val bleedColor = MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)

    val ratio = (result.widthPx / result.heightPx).toFloat()
    val bleedRatio = result.bleed?.let {
        (it.totalWidthPx / it.totalHeightPx).toFloat()
    }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
        ) {
            val maxW = size.width * 0.85f
            val maxH = size.height * 0.85f

            val (rectW, rectH) = if (ratio >= 1f) {
                maxW to (maxW / ratio)
            } else {
                (maxH * ratio) to maxH
            }.let { (w, h) ->
                if (h > maxH) maxH * ratio to maxH else w to h
            }

            val left = (size.width - rectW) / 2f
            val top = (size.height - rectH) / 2f

            if (result.bleed != null && bleedRatio != null) {
                val bleedScale = rectW / result.widthPx.toFloat()
                val bleedW = result.bleed.totalWidthPx.toFloat() * bleedScale
                val bleedH = result.bleed.totalHeightPx.toFloat() * bleedScale
                val bleedLeft = (size.width - bleedW) / 2f
                val bleedTop = (size.height - bleedH) / 2f
                drawRect(
                    color = bleedColor,
                    topLeft = Offset(bleedLeft, bleedTop),
                    size = Size(bleedW, bleedH),
                    style = Stroke(
                        width = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f)),
                    ),
                )
            }

            drawRect(
                color = surfaceColor,
                topLeft = Offset(left, top),
                size = Size(rectW, rectH),
            )

            drawRect(
                color = primaryColor,
                topLeft = Offset(left, top),
                size = Size(rectW, rectH),
                style = Stroke(width = 1.5.dp.toPx()),
            )

            drawLine(
                onSurfaceColor,
                Offset(left, top),
                Offset(left + rectW, top + rectH),
                strokeWidth = 0.5.dp.toPx()
            )
            drawLine(
                onSurfaceColor,
                Offset(left + rectW, top),
                Offset(left, top + rectH),
                strokeWidth = 0.5.dp.toPx()
            )
        }

        Text(
            text = "${result.widthPx.toInt()} × ${result.heightPx.toInt()} px",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
        )
    }
}

@Composable
private fun DimensionTable(result: ReverseDocumentResult) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        DimensionRow(
            label = "Pixels",
            value = "${result.widthPx.formatted()} × ${result.heightPx.formatted()} px",
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
        DimensionRow(
            label = "Millimeters",
            value = "${result.widthMm.formatted()} × ${result.heightMm.formatted()} mm",
        )
        DimensionRow(
            label = "Centimeters",
            value = "${result.widthCm.formatted()} × ${result.heightCm.formatted()} cm",
        )
        DimensionRow(
            label = "Inches",
            value = "${result.widthInch.formatted()} × ${result.heightInch.formatted()} in",
        )
    }
}

@Composable
private fun DimensionRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
        )
    }
}

@Composable
private fun BleedRow(bleed: BleedResult) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
            ) {
                Text(
                    text = "+ ${bleed.bleedMm.formatted()} mm bleed",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            DimensionRow(
                label = "Pixels",
                value = "${bleed.totalWidthPx.formatted()} × ${bleed.totalHeightPx.formatted()} px",
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
            DimensionRow(
                label = "Millimeters",
                value = "${bleed.totalWidthMm.formatted()} × ${bleed.totalHeightMm.formatted()} mm",
            )
            DimensionRow(
                label = "Centimeters",
                value = "${bleed.totalWidthCm.formatted()} × ${bleed.totalHeightCm.formatted()} cm",
            )
            DimensionRow(
                label = "Inches",
                value = "${bleed.totalWidthInch.formatted()} × ${bleed.totalHeightInch.formatted()} in",
            )
        }
    }
}

private fun Double.formatted(): String =
    if (this % 1.0 == 0.0) toInt().toString()
    else "%.2f".format(this)