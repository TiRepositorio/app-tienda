package apolo.tienda.tienda

import android.app.Application
import apolo.tienda.tienda.utils.PreferencesHelper
import apolo.tienda.tienda.utils.AppContextHolder
import androidx.appcompat.app.AppCompatDelegate

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        // Inicializamos PreferencesHelper solo una vez
        PreferencesHelper.init(this)
        AppContextHolder.init(this)


        //modo claro siempre
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }
}
