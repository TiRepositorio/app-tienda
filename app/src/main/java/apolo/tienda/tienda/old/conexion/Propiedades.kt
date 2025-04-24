package apolo.tienda.tienda.old.conexion

class Propiedades {

    companion object{
        private var dbDriver = ""
        private var url = ""
        private var dbJdbc = ""
        private var puerto = ""
        private var dbName = ""
        private var dbHost = ""
        fun getUrl(): String {
            return url
        }
        fun getDbDriver(): String {
            return dbDriver
        }
        fun loadProperties(usuario: String, clave: String) {
            try {
                dbJdbc = "jdbc:oracle:thin:@"
                dbHost = "10.1.1.251"
                puerto = "1521"
                dbName = "orcl"
                if (dbJdbc.equals("jdbc:mysql:"))
                    url = dbJdbc + dbHost + ":" + puerto + "/" + dbName + "?user=" + usuario + "&password=" + clave;
                else if (dbJdbc.equals("jdbc:sqlserver:"))
                    url = dbJdbc + dbHost + ":" + puerto + ";dataBaseName=" + dbName + ";user=" + usuario + ";password=" + clave + ";";
                else if (dbJdbc.equals("jdbc:oracle:thin:@"))
                    url = "jdbc:oracle:thin:@" + dbHost + ":1521:" + dbName;
                else if (dbJdbc.equals("jdbc:sybase:Tds:")) {
                    url = dbJdbc + dbHost + ":" + puerto + "/" + dbName;
                }
                dbDriver = "oracle.jdbc.driver.OracleDriver"
            }
            catch (e: Exception) {
                println("Exception:" + e.message)
                e.printStackTrace()
            }
        }
    }

}