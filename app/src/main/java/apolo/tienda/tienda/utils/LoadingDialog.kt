package apolo.tienda.tienda.utils

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import apolo.tienda.tienda.R

class LoadingDialog(private val context: Context) {

    private var dialog: Dialog? = null

    fun show() {
        if (dialog?.isShowing == true) return

        dialog = Dialog(context).apply {
            setContentView(LayoutInflater.from(context).inflate(R.layout.dialog_loading, null))
            setCancelable(false)
            show()
        }
    }

    fun hide() {
        dialog?.dismiss()
    }
}
