package pol.rubiano.docdimtool.app.domain.usecases

import org.junit.Test
import pol.rubiano.docdimtool.app.domain.models.*
import kotlin.test.*

class ReverseCalculateDocumentUseCaseTest {

    private val useCase = ReverseCalculateDocumentUseCase()

    @Test
    fun `given width in mm and 16-9 ratio landscape, resolves correct height in mm`() {
        val spec = buildReverseSpec(knownSide = Side.WIDTH, value = 297.0, unit = InputUnit.MM)
        val result = useCase(spec).first()

        assertEquals(297.0, result.widthMm, absoluteTolerance = 0.01)
        assertEquals(167.44, result.heightMm, absoluteTolerance = 0.5)
    }

    @Test
    fun `given height in mm and 16-9 ratio landscape, resolves correct width in mm`() {
        val spec = buildReverseSpec(knownSide = Side.HEIGHT, value = 167.44, unit = InputUnit.MM)
        val result = useCase(spec).first()

        assertEquals(167.44, result.heightMm, absoluteTolerance = 0.01)
        assertEquals(297.0, result.widthMm, absoluteTolerance = 1.0)
    }

    @Test
    fun `given width in cm, converts correctly to mm and then to px`() {
        val spec = buildReverseSpec(knownSide = Side.WIDTH, value = 29.7, unit = InputUnit.CM, dpis = listOf(300))
        val result = useCase(spec).first()

        assertEquals(297.0, result.widthMm, absoluteTolerance = 0.01)
        assertEquals(3507.87, result.widthPx, absoluteTolerance = 0.5)
    }

    @Test
    fun `given width in inches at 300 dpi, resolves correct px`() {
        val spec = buildReverseSpec(knownSide = Side.WIDTH, value = 10.0, unit = InputUnit.INCH, dpis = listOf(300))
        val result = useCase(spec).first()

        assertEquals(254.0, result.widthMm, absoluteTolerance = 0.01)
        assertEquals(3000.0, result.widthPx, absoluteTolerance = 0.5)
    }

    @Test
    fun `given multiple dpis, returns one result per dpi`() {
        val dpis = listOf(72, 150, 300)
        val spec = buildReverseSpec(dpis = dpis)
        val results = useCase(spec)

        assertEquals(dpis.size, results.size)
        assertEquals(dpis, results.map { it.dpi })
    }

    @Test
    fun `cm is mm divided by 10`() {
        val result = useCase(buildReverseSpec()).first()
        assertEquals(result.widthMm / 10.0, result.widthCm, absoluteTolerance = 0.001)
        assertEquals(result.heightMm / 10.0, result.heightCm, absoluteTolerance = 0.001)
    }

    @Test
    fun `inch is mm divided by 25_4`() {
        val result = useCase(buildReverseSpec()).first()
        assertEquals(result.widthMm / 25.4, result.widthInch, absoluteTolerance = 0.001)
        assertEquals(result.heightMm / 25.4, result.heightInch, absoluteTolerance = 0.001)
    }

    @Test
    fun `landscape orientation ensures width is greater than height`() {
        val spec = buildReverseSpec(orientation = Orientation.LANDSCAPE)
        val result = useCase(spec).first()
        assertTrue(result.widthMm > result.heightMm)
    }

    @Test
    fun `portrait orientation inverts the ratio`() {
        val spec = buildReverseSpec(orientation = Orientation.PORTRAIT)
        val result = useCase(spec).first()
        assertTrue(result.heightMm > result.widthMm)
    }

    @Test
    fun `square ratio is unaffected by orientation`() {
        val landscape = buildReverseSpec(ratio = DocumentAspectRatio.RATIO_1_1, orientation = Orientation.LANDSCAPE)
        val portrait = buildReverseSpec(ratio = DocumentAspectRatio.RATIO_1_1, orientation = Orientation.PORTRAIT)

        val r1 = useCase(landscape).first()
        val r2 = useCase(portrait).first()

        assertEquals(r1.widthMm, r2.widthMm, absoluteTolerance = 0.01)
        assertEquals(r1.heightMm, r2.heightMm, absoluteTolerance = 0.01)
    }

    @Test
    fun `bleed result is null when bleed is zero`() {
        val result = useCase(buildReverseSpec(bleedMm = 0.0)).first()
        assertNull(result.bleed)
    }

    @Test
    fun `bleed total dimensions are larger than base dimensions`() {
        val result = useCase(buildReverseSpec(bleedMm = 3.0)).first()
        assertNotNull(result.bleed)
        assertTrue(result.bleed.totalWidthMm > result.widthMm)
        assertTrue(result.bleed.totalHeightMm > result.heightMm)
    }

    @Test
    fun `bleed adds exactly 2x bleed margin to each side`() {
        val bleed = 3.0
        val result = useCase(buildReverseSpec(bleedMm = bleed)).first()
        assertNotNull(result.bleed)
        assertEquals(result.widthMm + bleed * 2, result.bleed.totalWidthMm, absoluteTolerance = 0.01)
        assertEquals(result.heightMm + bleed * 2, result.bleed.totalHeightMm, absoluteTolerance = 0.01)
    }

    @Test
    fun `bleed pixel dimensions differ per dpi even with same mm input`() {
        val result = useCase(buildReverseSpec(bleedMm = 3.0, dpis = listOf(300))).first()
        assertNotNull(result.bleed)
        assertTrue(result.bleed.totalWidthPx > result.widthPx)
        assertTrue(result.bleed.totalHeightPx > result.heightPx)
    }

    @Test
    fun `72 dpi maps to SCREEN hint`() {
        val result = useCase(buildReverseSpec(dpis = listOf(72))).first()
        assertEquals(DpiUsageHint.SCREEN, result.usageHint)
    }

    @Test
    fun `300 dpi maps to HIGH_RES_PRINT hint`() {
        val result = useCase(buildReverseSpec(dpis = listOf(300))).first()
        assertEquals(DpiUsageHint.HIGH_RES_PRINT, result.usageHint)
    }

    @Test
    fun `negative known value throws`() {
        assertFailsWith<IllegalArgumentException> {
            buildReverseSpec(value = -1.0)
        }
    }

    @Test
    fun `empty dpi list throws`() {
        assertFailsWith<IllegalArgumentException> {
            buildReverseSpec(dpis = emptyList())
        }
    }

    @Test
    fun `negative bleed throws`() {
        assertFailsWith<IllegalArgumentException> {
            buildReverseSpec(bleedMm = -1.0)
        }
    }

    @Test
    fun `px input throws error`() {
        assertFailsWith<IllegalArgumentException> {
            buildReverseSpec(unit = InputUnit.PX)
        }
    }

    private fun buildReverseSpec(
        knownSide: Side = Side.WIDTH,
        value: Double = 297.0,
        unit: InputUnit = InputUnit.MM,
        ratio: DocumentAspectRatio = DocumentAspectRatio.RATIO_16_9,
        dpis: List<Int> = listOf(300),
        orientation: Orientation = Orientation.LANDSCAPE,
        bleedMm: Double = 0.0,
    ) = ReverseDocumentSpecifications(
        knownSide = knownSide,
        knownValue = value,
        inputUnit = unit,
        ratio = ratio,
        dpis = dpis,
        orientation = orientation,
        bleedMm = bleedMm,
    )
}