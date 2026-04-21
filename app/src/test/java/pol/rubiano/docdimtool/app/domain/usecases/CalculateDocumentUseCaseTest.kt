package pol.rubiano.docdimtool.app.domain.usecases

import org.junit.Test
import pol.rubiano.docdimtool.app.domain.models.*
import kotlin.test.*

class CalculateDocumentUseCaseTest {

    private val useCase = CalculateDocumentUseCase()

    @Test
    fun `given width in px and 16-9 ratio, resolves correct height`() {
        val spec = buildSpec(knownSide = Side.WIDTH, value = 1920.0, unit = InputUnit.PX)
        val result = useCase(spec).first()

        assertEquals(1920.0, result.widthPx, absoluteTolerance = 0.01)
        assertEquals(1080.0, result.heightPx, absoluteTolerance = 0.01)
    }

    @Test
    fun `given height in px and 16-9 ratio, resolves correct width`() {
        val spec = buildSpec(knownSide = Side.HEIGHT, value = 1080.0, unit = InputUnit.PX)
        val result = useCase(spec).first()

        assertEquals(1920.0, result.widthPx, absoluteTolerance = 0.01)
        assertEquals(1080.0, result.heightPx, absoluteTolerance = 0.01)
    }

    @Test
    fun `given width in mm at 300 dpi, resolves correct px dimensions`() {
        // 297mm at 300dpi → 297 / 25.4 * 300 ≈ 3507.87 px
        // px is resolved from dpis.first() — same value for all result cards
        val spec = buildSpec(knownSide = Side.WIDTH, value = 297.0, unit = InputUnit.MM, dpis = listOf(300))
        val result = useCase(spec).first()

        assertEquals(3507.87, result.widthPx, absoluteTolerance = 0.5)
    }

    @Test
    fun `given physical unit input, px dimensions are identical across all dpi results`() {
        // px count is DPI-independent when input is a physical unit —
        // only physical output dimensions (mm, inch) change per DPI
        val spec = buildSpec(knownSide = Side.WIDTH, value = 297.0, unit = InputUnit.MM, dpis = listOf(72, 150, 300))
        val results = useCase(spec)

        val referencePx = results.first().widthPx
        results.forEach { assertEquals(referencePx, it.widthPx, absoluteTolerance = 0.01) }
    }

    @Test
    fun `given physical unit input, physical dimensions differ across dpi results`() {
        // Same physical input → same px → but different mm output per DPI
        val spec = buildSpec(knownSide = Side.WIDTH, value = 297.0, unit = InputUnit.MM, dpis = listOf(72, 300))
        val (low, high) = useCase(spec)

        // At 72dpi the physical output is larger than at 300dpi for the same px count
        assertTrue(low.widthMm > high.widthMm)
    }

    @Test
    fun `given width in inches at 72 dpi, resolves correct px`() {
        // 10 inch at 72 dpi → 720 px
        val spec = buildSpec(knownSide = Side.WIDTH, value = 10.0, unit = InputUnit.INCH, dpis = listOf(72))
        val result = useCase(spec).first()

        assertEquals(720.0, result.widthPx, absoluteTolerance = 0.01)
    }

    @Test
    fun `given width in cm, converts correctly to mm internally`() {
        // 29.7cm = 297mm → same px as mm test above
        val specCm = buildSpec(knownSide = Side.WIDTH, value = 29.7, unit = InputUnit.CM, dpis = listOf(300))
        val specMm = buildSpec(knownSide = Side.WIDTH, value = 297.0, unit = InputUnit.MM, dpis = listOf(300))

        val resultCm = useCase(specCm).first()
        val resultMm = useCase(specMm).first()

        assertEquals(resultMm.widthPx, resultCm.widthPx, absoluteTolerance = 0.01)
    }

    @Test
    fun `given multiple dpis, returns one result per dpi`() {
        val dpis = listOf(72, 150, 300)
        val spec = buildSpec(dpis = dpis)
        val results = useCase(spec)

        assertEquals(dpis.size, results.size)
        assertEquals(dpis, results.map { it.dpi })
    }

    @Test
    fun `physical dimensions differ per dpi for same px value`() {
        val spec = buildSpec(dpis = listOf(72, 300))
        val (low, high) = useCase(spec)

        // Same pixel count, different physical size
        assertEquals(low.widthPx, high.widthPx, absoluteTolerance = 0.01)
        assertTrue(low.widthMm > high.widthMm)
    }

    @Test
    fun `cm is mm divided by 10`() {
        val result = useCase(buildSpec()).first()
        assertEquals(result.widthMm / 10.0, result.widthCm, absoluteTolerance = 0.001)
        assertEquals(result.heightMm / 10.0, result.heightCm, absoluteTolerance = 0.001)
    }

    @Test
    fun `inch is mm divided by 25_4`() {
        val result = useCase(buildSpec()).first()
        assertEquals(result.widthMm / 25.4, result.widthInch, absoluteTolerance = 0.001)
        assertEquals(result.heightMm / 25.4, result.heightInch, absoluteTolerance = 0.001)
    }

    @Test
    fun `landscape orientation ensures width is greater than height`() {
        val spec = buildSpec(orientation = Orientation.LANDSCAPE)
        val result = useCase(spec).first()
        assertTrue(result.widthPx > result.heightPx)
    }

    @Test
    fun `portrait orientation ensures height is greater than width`() {
        val spec = buildSpec(orientation = Orientation.PORTRAIT)
        val result = useCase(spec).first()
        assertTrue(result.heightPx > result.widthPx)
    }

    @Test
    fun `square ratio is unaffected by orientation`() {
        val landscape = buildSpec(ratio = DocumentAspectRatio.RATIO_1_1, orientation = Orientation.LANDSCAPE)
        val portrait  = buildSpec(ratio = DocumentAspectRatio.RATIO_1_1, orientation = Orientation.PORTRAIT)

        val r1 = useCase(landscape).first()
        val r2 = useCase(portrait).first()

        assertEquals(r1.widthPx, r2.widthPx, absoluteTolerance = 0.01)
        assertEquals(r1.heightPx, r2.heightPx, absoluteTolerance = 0.01)
    }

    @Test
    fun `bleed result is null when bleed is zero`() {
        val result = useCase(buildSpec(bleedMm = 0.0)).first()
        assertNull(result.bleed)
    }

    @Test
    fun `bleed total dimensions are larger than base dimensions`() {
        val result = useCase(buildSpec(bleedMm = 3.0)).first()
        assertNotNull(result.bleed)
        assertTrue(result.bleed.totalWidthMm > result.widthMm)
        assertTrue(result.bleed.totalHeightMm > result.heightMm)
    }

    @Test
    fun `bleed adds exactly 2x bleed margin to each side`() {
        val bleed = 3.0
        val result = useCase(buildSpec(bleedMm = bleed)).first()
        assertNotNull(result.bleed)
        assertEquals(result.widthMm  + bleed * 2, result.bleed.totalWidthMm,  absoluteTolerance = 0.01)
        assertEquals(result.heightMm + bleed * 2, result.bleed.totalHeightMm, absoluteTolerance = 0.01)
    }

    @Test
    fun `72 dpi maps to SCREEN hint`() {
        val result = useCase(buildSpec(dpis = listOf(72))).first()
        assertEquals(DpiUsageHint.SCREEN, result.usageHint)
    }

    @Test
    fun `300 dpi maps to HIGH_RES_PRINT hint`() {
        val result = useCase(buildSpec(dpis = listOf(300))).first()
        assertEquals(DpiUsageHint.HIGH_RES_PRINT, result.usageHint)
    }

    @Test
    fun `negative known value throws`() {
        assertFailsWith<IllegalArgumentException> {
            buildSpec(value = -1.0)
        }
    }

    @Test
    fun `empty dpi list throws`() {
        assertFailsWith<IllegalArgumentException> {
            buildSpec(dpis = emptyList())
        }
    }

    @Test
    fun `negative bleed throws`() {
        assertFailsWith<IllegalArgumentException> {
            buildSpec(bleedMm = -1.0)
        }
    }

    private fun buildSpec(
        knownSide: Side = Side.WIDTH,
        value: Double = 1920.0,
        unit: InputUnit = InputUnit.PX,
        ratio: DocumentAspectRatio = DocumentAspectRatio.RATIO_16_9,
        dpis: List<Int> = listOf(300),
        orientation: Orientation = Orientation.LANDSCAPE,
        bleedMm: Double = 0.0,
    ) = DocumentSpecifications(
        knownSide   = knownSide,
        knownValue  = value,
        inputUnit   = unit,
        ratio       = ratio,
        dpis        = dpis,
        orientation = orientation,
        bleedMm     = bleedMm,
    )
}