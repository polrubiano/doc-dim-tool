package pol.rubiano.docdimtool.app.domain.models

data class DocumentAspectRatio(
    val width: Int,
    val height: Int
) {

    init {
        require(width > 0 && height > 0) { "Aspect ratio sides must be positive" }
    }

    companion object {
        val RATIO_16_9  = DocumentAspectRatio(16, 9)
        val RATIO_4_3   = DocumentAspectRatio(4, 3)
        val RATIO_1_1   = DocumentAspectRatio(1, 1)
        val RATIO_9_16  = DocumentAspectRatio(9, 16)
        val RATIO_A4    = DocumentAspectRatio(210, 297) // portrait — mm-based proportion
        val RATIO_LETTER = DocumentAspectRatio(216, 279)
    }

    /**
     * Returns the ratio simplified to its lowest terms (e.g. 32:18 → 16:9)
     */
    fun toSimplified(): DocumentAspectRatio {
        val gcdValue = greatestCommonDivisor(width, height)
        return DocumentAspectRatio(width / gcdValue, height / gcdValue)
    }

    private fun greatestCommonDivisor(dividend: Int, divisor: Int): Int =
        if (divisor == 0) dividend
        else greatestCommonDivisor(divisor, dividend % divisor)

    fun toDouble(): Double = width.toDouble() / height.toDouble()

    override fun toString(): String = "${width}:${height}"
}
