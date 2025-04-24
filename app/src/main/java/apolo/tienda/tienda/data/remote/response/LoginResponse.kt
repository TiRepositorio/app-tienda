package apolo.tienda.tienda.data.remote.response

data class LoginResponse(
    val token: String, // Depende de lo que te devuelve tu API
    val error: String, // Depende de lo que te devuelve tu API
    //val user: String
)
