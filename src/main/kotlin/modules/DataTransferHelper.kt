package modules

import models.Schema
import java.sql.*
import java.util.concurrent.TimeUnit

class DataTransferHelper {
    object Main {
        @JvmStatic
        fun main(args: Array<String>) {
            val schemaConstructor : SchemaConstructor = SchemaConstructor()
            val metaDataParser : MetaDataParser = MetaDataParser()
            val dataTransferHelper : DataTransferHelper = DataTransferHelper()

            val schema : Schema = metaDataParser.getSchema()
            schemaConstructor.createSchema(schema)
            dataTransferHelper.transferData(schema)
        }
    }

    private val CONNECTION_URL_MSSQL = "jdbc:sqlserver://localhost:1433;database=Northwind;username=SA;password=Фф2429989"
    private val CONNECTION_URL_POSTGRES = "jdbc:postgresql://localhost:5432/postgres?user=akbar&password=Aa2429989"
    private val BATCH_SIZE_LIMIT = 1000

    fun transferData(schema: Schema) {
        val connection: Connection = Utils.connect(CONNECTION_URL_MSSQL)!!
        val connection2: Connection = Utils.connect(CONNECTION_URL_POSTGRES)!!

        try {
            disableConstraints()
            connection.use {
                connection2.use {
                    schema.tables.forEach { table ->
                        val fields: List<String> = table.fields.map { field -> field.name }
                        var rowsInserted : Int = 0

                        val select = prepareSelect(table.name, fields)
                        val selectStatement: PreparedStatement = connection.prepareStatement(select)
                        val insertStatement: Statement = connection2.createStatement()
                        println(select)

                        val resultSet: ResultSet = selectStatement.executeQuery()
                        resultSet.use {
                            while (resultSet.next()) {
                                val values: ArrayList<Any> = arrayListOf()
                                fields.forEach { field ->
                                    values.add(resultSet.getObject(field))
                                }

                                if (values.isNotEmpty()) {
                                    if (rowsInserted >= BATCH_SIZE_LIMIT) {
                                        insertStatement.executeBatch()
                                        connection2.commit()
                                        rowsInserted = 0
                                    }
                                    val insert = prepareInsert(table.name, fields, values)
                                    println(insert)
                                    insertStatement.addBatch(insert)
                                    rowsInserted++
                                }
                            }
                            if (rowsInserted > 0) {
                                insertStatement.executeBatch()
                                connection2.commit()
                            }
                        }
                    }
                }
            }
        } catch (e : SQLException) {
            e.printStackTrace()
        } finally {
            enableConstraints()
        }
    }

    private fun prepareSelect(tableName: String, fields: List<String>): String {
        return "SELECT ${fields.joinToString(",")} FROM $tableName"
    }

    private fun prepareInsert(tableName: String, fields: List<String>, values: ArrayList<Any>): String {
        val valuesStr: ArrayList<String> = arrayListOf()

        values.forEach { value ->
            if (value is ByteArray) {
                return ""
            } else if (value == null) {
                valuesStr.add("null")
            } else if (value is String) {
                valuesStr.add("'${prepareValue(value)}'")
            } else if (value is Char
                    || value is Date
                    || value is Timestamp) {
                valuesStr.add("'$value'")
            } else {
                valuesStr.add(prepareValue(value.toString()))
            }
        }

        return "INSERT INTO $tableName(${fields.joinToString(",")}) VALUES(${valuesStr.joinToString(",")})"
    }

    private fun prepareValue(value : String) : String {
        return value.replace("'","")
    }

    private fun disableConstraints() {
        val connection: Connection = Utils.connect(CONNECTION_URL_POSTGRES)!!
        val statement:Statement = connection.createStatement()
        statement.execute("SET session_replication_role = 'replica';")
        statement.close()
    }

    private fun enableConstraints() {
        val connection: Connection = Utils.connect(CONNECTION_URL_POSTGRES)!!
        val statement:Statement = connection.createStatement()
        statement.execute("SET session_replication_role = 'origin';")
        statement.close()
    }
}