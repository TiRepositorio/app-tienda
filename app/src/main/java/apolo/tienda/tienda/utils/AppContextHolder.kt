package apolo.tienda.tienda.utils

import android.annotation.SuppressLint
import android.content.Context


@SuppressLint("StaticFieldLeak")
object AppContextHolder {
    private lateinit var context: Context

    fun init(context: Context) {
        this.context = context.applicationContext
    }

    fun getContext(): Context = context
}
