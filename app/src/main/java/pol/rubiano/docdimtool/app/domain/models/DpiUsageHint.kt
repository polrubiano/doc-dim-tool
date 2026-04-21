package pol.rubiano.docdimtool.app.domain.models

enum class DpiUsageHint {
    SCREEN,
    DRAFT_PRINT,
    STANDARD_PRINT,
    HIGH_RES_PRINT,
    ULTRA_PRINT;

    companion object {
        fun from(dpi: Int): DpiUsageHint = when {
            dpi <= 96  -> SCREEN
            dpi <= 149 -> DRAFT_PRINT
            dpi <= 299 -> STANDARD_PRINT
            dpi <= 599 -> HIGH_RES_PRINT
            else       -> ULTRA_PRINT
        }
    }
}
