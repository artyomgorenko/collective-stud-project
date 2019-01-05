package modules

import models.*
import java.sql.*

class MetaDataParser {

    val GET_SCHEMA_INFO = """
        SELECT SCHEMA_NAME
        FROM INFORMATION_SCHEMA.SCHEMATA
    """.trimIndent()

    val GET_TABLES_LIST = """
        SELECT
            tbl.table_name AS table_name
        FROM information_schema.tables tbl
        WHERE tbl.table_type = 'base table'
        AND tbl.table_schema = 'dbo'
    """.trimIndent()

    val GET_TABLE_INFO = """
        SELECT
            tbl.table_name AS table_name
        FROM information_schema.tables tbl
        INNER JOIN information_schema.columns col
            ON col.table_name = tbl.table_name
            AND col.table_schema = tbl.table_schema
        WHERE tbl.table_type = 'base table'
        AND tbl.table_schema = 'dbo'
        AND tbl.table_name = ?
    """.trimIndent()

    val GET_FIELD_INFO = """
        SELECT
            col.column_name AS column_name,
            col.data_type AS column_data_type,
            col.domain_name AS domain_name
        FROM information_schema.tables tbl
        INNER JOIN information_schema.columns col
            ON col.table_name = tbl.table_name
            AND col.table_schema = tbl.table_schema
        WHERE tbl.table_type = 'base table'
        AND tbl.table_schema = 'dbo'
        AND tbl.table_name = ?

    """.trimIndent()

    val GET_DOMAIN_INFO = """
        SELECT
            name,
            system_type_id,
            user_type_id,
            max_length,
            precision,
            scale,
            is_user_defined
        FROM sys.types
    """.trimIndent()

    val CONNECTION_URL = ""

    object Main
    {
        @JvmStatic
        fun main(args: Array<String>) {
            val metaDataParser : MetaDataParser = MetaDataParser()
            metaDataParser.getSchema()
        }
    }

    fun getSchema() : Schema {
        var schema : Schema = Schema()
        val connection : Connection = Utils.connect(CONNECTION_URL)!!

        connection.use {
            try {
                connection.autoCommit = false
                schema = getSchema(connection)
                connection.commit()
            }  catch (e: SQLException) {
                connection.rollback()
                e.printStackTrace()
            }
        }

        return schema
    }

    private fun getSchema(connection: Connection) : Schema {
        val schema : Schema = Schema()
        val statement: PreparedStatement = connection.prepareStatement(GET_SCHEMA_INFO)
        statement.use {
            val resultSet : ResultSet = statement.executeQuery()
            resultSet.use {
                if (resultSet.next()) {
                    schema.name = resultSet.getString("SCHEMA_NAME")
                    schema.domains = getDomains(connection)
                    schema.tables = getTables(connection)
                }
            }
        }
        return schema
    }

    private fun getDomains(connection: Connection): ArrayList<Domain> {
        val domains : ArrayList<Domain> = arrayListOf()

        val statement : PreparedStatement= connection.prepareStatement(GET_DOMAIN_INFO)
        statement.use {
            val resultSet : ResultSet = statement.executeQuery()
            while (resultSet.next()) {
                val domain : Domain = Domain()
                domain.name = resultSet.getString("name")

                domains.add(domain)
            }
        }

        return domains
    }

    private fun getTables(connection: Connection) : ArrayList<Table> {
        val tables : ArrayList<Table> = arrayListOf()

        val tableNames : ArrayList<String> = getTablesNames(connection)
        tableNames.forEach {
            tableName -> tables.add(getTable(tableName, connection))
        }

        return tables
    }

    private fun getTablesNames(connection: Connection) : ArrayList<String> {
        val tablesNames : ArrayList<String> = arrayListOf()

        val statement: PreparedStatement = connection.prepareStatement(GET_TABLES_LIST)
        statement.use {
            val resultSet : ResultSet = statement.executeQuery()
            resultSet.use {
                while (resultSet.next()) {
                    tablesNames.add(resultSet.getString("table_name"))
                }
            }
        }

        return tablesNames
    }

    private fun getTable(tableName: String, connection: Connection) : Table {
        val table: Table = Table()

        val statement: PreparedStatement = connection.prepareStatement(GET_TABLE_INFO)
        statement.use {
            statement.setString(1, tableName)
            val resultSet : ResultSet = statement.executeQuery()
            resultSet.use {
                if (resultSet.next()) {
                    table.name = resultSet.getString("table_name")

                    table.fields = getFields(tableName, connection)
                    table.indexes = getIndexes(tableName, connection)
                    table.constraints = getConstraints(tableName, connection)
                }
            }
        }

        return table
    }

    private fun getFields(tableName: String, connection: Connection) : ArrayList<Field> {
        val fields : ArrayList<Field> = arrayListOf()
        val statement: PreparedStatement = connection.prepareStatement(GET_FIELD_INFO)
        statement.use {
            statement.setString(1, tableName)
            val resultSet: ResultSet = statement.executeQuery()
            resultSet.use {
                while (resultSet.next()) {
                    val field : Field = Field()
                    field.name = resultSet.getString("column_name")
                    field.domain = resultSet.getString("column_data_type")

                    fields.add(field)
                }
            }
        }

        return fields
    }

    private fun getIndexes(tableName: String, connection: Connection): ArrayList<Index> {
        val indexes : ArrayList<Index> = arrayListOf()

        val databaseMetaData = connection.metaData
        val resultSet: ResultSet = databaseMetaData.getIndexInfo(connection.catalog, null, tableName, true, true)
        resultSet.use {
            while (resultSet.next()) {
                val index : Index = Index()
                index.field= resultSet.getString(3)//column name
                indexes.add(index)
            }
        }

        return indexes
    }

    private fun getConstraints(tableName: String, connection: Connection): ArrayList<Constraint> {
        val constraints : ArrayList<Constraint> = arrayListOf()

        // get primary keys
        val databaseMetaData: DatabaseMetaData = connection.metaData
        val resultSetPk: ResultSet = databaseMetaData.getPrimaryKeys(connection.catalog, null, tableName)
        resultSetPk.use {
            while (resultSetPk.next()) {
                val constraint : Constraint = Constraint()
                constraint.kind = "PRIMARY"
                constraint.items = resultSetPk.getString("COLUMN_NAME")
                constraints.add(constraint)
            }
        }

        // get foreign keys
        val resultSetFk: ResultSet = databaseMetaData.getExportedKeys(connection.catalog, null, tableName)
        resultSetFk.use {
            while (resultSetFk.next()) {
                val constraint : Constraint = Constraint()
                val reference : String = resultSetFk.getString("FKTABLE_NAME")
                if (reference != null) {
                    constraint.kind = "FOREIGN"
                    constraint.reference = resultSetFk.getString("FKTABLE_NAME")
                    constraint.items = resultSetFk.getString("FKCOLUMN_NAME")
                    constraints.add(constraint)
                }
            }
        }

        return constraints
    }
}