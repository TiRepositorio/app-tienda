package apolo.tienda.tienda.old.utilidades

import android.content.ContentValues
import apolo.tienda.tienda.old.MainActivity
import java.util.*
import kotlin.collections.ArrayList

class TablasSincronizacion {


    fun listaSQLCreateTables(): Vector<String> {
        val lista : Vector<String> = Vector()
        lista.add(0, SentenciasSQL.createTableStArticulos())

        return lista
    }

    fun listaDatos(): ArrayList<ArrayList<ContentValues>> {
        val lista = ArrayList<ArrayList<ContentValues>>()
        lista.add(MainActivity.listaProductoV)
        return lista
    }

//    fun camposTablaS(): Vector<String> {
//        var lista : Vector<String> = Vector<String>()
//        lista.add(0,"")
//        lista.add(1,"")
//        lista.add(2,"")
//        lista.add(3,"")
//        lista.add(4,"")
//        lista.add(5,"")
//        lista.add(6,"")
//        lista.add(7,"")
//        lista.add(8,"")
//        lista.add(9,"")
//        lista.add(10,"")
//        lista.add(11,"")
//        lista.add(12,"")
//        lista.add(13,"")
//        lista.add(14,"")
//        lista.add(15,"")
//        lista.add(16,"")
//        lista.add(17,"")
//        lista.add(18,"")
//        lista.add(19,"")
//        lista.add(20,"")
//        lista.add(21,"")
//        lista.add(22,"")
//        lista.add(23,"")
//        lista.add(24,"")
//        lista.add(25,"")
//        lista.add(26,"")
//        lista.add(27,"")
//        lista.add(28,"")
//        lista.add(29,"")
//        return lista
//    }


}