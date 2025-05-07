package apolo.tienda.tienda

import android.app.Application
import apolo.tienda.tienda.utils.PreferencesHelper
import apolo.tienda.tienda.utils.AppContextHolder
import androidx.appcompat.app.AppCompatDelegate
import java.util.UUID

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        // Inicializamos PreferencesHelper solo una vez
        PreferencesHelper.init(this)
        AppContextHolder.init(this)


        //modo claro siempre
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        //generar uuid unico si es que no existe en las preferencias
        val offline = PreferencesHelper.getInstance().getOffline()
        if (!offline) {
            PreferencesHelper.getInstance().saveOffline(true)
        }

        val uuid = PreferencesHelper.getInstance().getUUID()
        if (uuid == "") {
            val newUUID = UUID.randomUUID().toString()
            PreferencesHelper.getInstance().saveUUID(newUUID)
        }



    }
}
