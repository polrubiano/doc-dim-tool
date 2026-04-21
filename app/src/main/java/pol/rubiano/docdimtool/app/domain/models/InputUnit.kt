package pol.rubiano.docdimtool.app.domain.models

enum class InputUnit {
    PX, MM, CM, INCH;

    companion object {
        private const val MM_PER_INCH = 25.4
        private const val CM_PER_INCH = 2.54
        private const val MM_PER_CM = 10
    }

    fun toInches(value: Double): Double = when (this) {
        PX   -> error("DPI required to convert from pixels")
        MM   -> value / MM_PER_INCH
        CM   -> value / CM_PER_INCH
        INCH -> value
    }

    fun toMM(value: Double, dpi: Int? = null): Double = when (this) {
        PX   -> {
            require(dpi != null) { "DPI required to convert from pixels" }
            (value / dpi) * MM_PER_INCH
        }
        MM   -> value
        CM   -> value * MM_PER_CM
        INCH -> value * MM_PER_INCH
    }
}