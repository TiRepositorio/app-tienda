// Ruta sugerida: utils/PermissionManager.kt
package apolo.tienda.tienda.utils

object PermissionManager {
    private var permissions: List<String> = emptyList()

    fun setPermissions(permisos: List<String>) {
        permissions = permisos
    }

    fun hasPermission(permiso: String): Boolean {
        return permissions.contains(permiso)
    }

    fun hasAnyPermission(vararg permisosRequeridos: String): Boolean {
        return permisosRequeridos.any { permissions.contains(it) }
    }

    fun clear() {
        permissions = emptyList()
    }
}
