package apolo.tienda.tienda.utils


import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class AndroidPermissionHelper(
    private val activity: Activity,
    private val permissions: Array<String>,
    private val requestCode: Int = 100
) {
    var onAllPermissionsGranted: (() -> Unit)? = null
    var onPermissionsDenied: ((List<String>) -> Unit)? = null

    fun checkAndRequestPermissions() {
        val deniedPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(activity, it) != PackageManager.PERMISSION_GRANTED
        }

        if (deniedPermissions.isEmpty()) {
            onAllPermissionsGranted?.invoke()
        } else {
            ActivityCompat.requestPermissions(
                activity,
                deniedPermissions.toTypedArray(),
                requestCode
            )
        }
    }

    fun handlePermissionsResult(requestCodeResult: Int, grantResults: IntArray) {
        if (requestCodeResult != requestCode) return

        val denied = mutableListOf<String>()

        // Protegido contra diferencia de tamaño
        for (i in grantResults.indices) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                denied.add(permissions.getOrNull(i) ?: "PERMISO_DESCONOCIDO")
            }
        }

        if (denied.isEmpty()) {
            onAllPermissionsGranted?.invoke()
        } else {
            onPermissionsDenied?.invoke(denied)
        }
    }
}
