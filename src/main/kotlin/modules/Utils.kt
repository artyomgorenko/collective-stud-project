package modules

import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

class Utils {
    companion object {
        fun connect(connectionUrl: String): Connection? {
            var connection: Connection? = null
            try {
                connection = DriverManager.getConnection(connectionUrl)
            } catch (e: SQLException) {
                e.printStackTrace()
            }
            return connection
        }
    }
}