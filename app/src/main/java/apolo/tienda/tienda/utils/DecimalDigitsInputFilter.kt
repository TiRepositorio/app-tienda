package apolo.tienda.tienda.utils

import android.text.InputFilter
import android.text.Spanned

class DecimalDigitsInputFilter(
    private val maxDigitsBeforeDecimal: Int,
    private val maxDigitsAfterDecimal: Int
) : InputFilter {

    private val regex = Regex("^-?\\d{0,$maxDigitsBeforeDecimal}(\\.\\d{0,$maxDigitsAfterDecimal})?\$")


    override fun filter(
        source: CharSequence?,
        start: Int,
        end: Int,
        dest: Spanned?,
        dstart: Int,
        dend: Int
    ): CharSequence? {
        val newText = dest.toString().substring(0, dstart) +
                source.toString().substring(start, end) +
                dest.toString().substring(dend)

        return if (regex.matches(newText)) null else ""
    }
}
