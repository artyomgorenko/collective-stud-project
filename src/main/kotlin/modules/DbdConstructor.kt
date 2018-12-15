package modules

import common.Tables
import models.*
import org.w3c.dom.Document
import java.sql.*
import java.util.*


class DbdConstructor {
    object Main {
        @JvmStatic
        fun main(args: Array<String>) {
            val parser: XdbParser = XdbParser()
            val xmlDoc: Document = parser.readXml("src/main/resources/tasks.xdb")
            xmlDoc.documentElement.normalize()

            val constructor: DbdConstructor = DbdConstructor()
            constructor.insertSchema(parser.parseSchema(xmlDoc)!!)
        }
    }

    @JvmField val INSERT_SCHEMA = """
        insert into ${Tables.SCHEMAS.tableName} (
          name
        , description
        , version
        , fulltext_engine)
        values(?, ?, ?, ?)
    """.trimIndent()

    @JvmField val INSERT_TABLE = """
        insert into ${Tables.TABLES.tableName} (
          schema_id
        , name
        , description
        , can_add
        , can_edit
        , can_delete
        , temporal_mode
        , ht_table_flags
        , access_level
        , means
        , uuid)
        values (
        (SELECT id FROM ${Tables.SCHEMAS.tableName} WHERE name = ?), ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    """.trimIndent()

    @JvmField val INSERT_DOMAIN = """
        insert into ${Tables.DOMAINS.tableName} (
          name
        , description
        , length
        , char_length
        , precision
        , scale
        , width
        , align
        , show_null
        , show_lead_nulls
        , thousands_separator
        , summable
        , case_sensitive
        , uuid
        , data_type_id)
        values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, (SELECT id FROM ${Tables.DATA_TYPES.tableName} WHERE type_id = ?))
    """.trimIndent()

    @JvmField val INSERT_FIELD = """
        insert into ${Tables.FIELDS.tableName} (
          table_id
        , position
        , name
        , russian_short_name
        , description
        , domain_id
        , can_input
        , can_edit
        , show_in_grid
        , show_in_details
        , is_mean
        , autocalculated
        , required
        , uuid)
        values (
          (SELECT id FROM ${Tables.TABLES.tableName} WHERE name = ?)
        , ?
        , ?
        , ?
        , ?
        , (SELECT id FROM ${Tables.DOMAINS.tableName} WHERE name = ?)
        , ?
        , ?
        , ?
        , ?
        , ?
        , ?
        , ?
        , ?)
    """.trimIndent()

    @JvmField val INSERT_CONSTRAINT = """
        insert into ${Tables.CONSTRAINTS.tableName} (
          table_id
        , name
        , constraint_type
        , reference
        , unique_key_id
        , has_value_edit
        , cascading_delete
        , expression
        , uuid)
        values (
        (SELECT id FROM ${Tables.TABLES.tableName} WHERE name = ?), ?, ?,
        (SELECT id FROM ${Tables.TABLES.tableName} WHERE name = ?), ?, ?, ?, ?, ?)
    """. trimIndent()

    @JvmField val INSERT_CONSTRAINT_DETAIL = """
        insert into ${Tables.CONSTRAINT_DETAILS.tableName} (
          constraint_id
        , position
        , field_id)
        values (
        (SELECT id FROM ${Tables.CONSTRAINTS.tableName} WHERE name = ?), ?, ?)
    """.trimIndent()

    @JvmField val INSERT_INDEX = """
        insert into ${Tables.INDEXES.tableName} (
          table_id
        , name
        , local
        , kind
        , uuid)
        values (
        (SELECT id FROM ${Tables.TABLES.tableName} WHERE name = ?), ?, ?, ?, ?)
    """.trimIndent()

    @JvmField val INSERT_INDEX_DETAIL = """
        insert into ${Tables.INDEX_DETAILS.tableName} (
          index_id
        , position
        , field_id
        , expression
        , descend)
        values (
        (SELECT id FROM ${Tables.INDEXES.tableName} WHERE name = ?), ?, ?, ?, ?)
    """.trimIndent()

    fun insertSchema(schema: Schema) {
        val connection: Connection = connect() ?: return
        connection.use {
            try {
                connection.autoCommit = false
                insertSchema(schema, connection)
                connection.commit()
            }  catch (e: SQLException) {
                connection.rollback()
                println(e.printStackTrace())
            }
        }
    }

    fun insertSchema(schema: Schema, connection: Connection) {
        val statement: PreparedStatement = connection.prepareStatement(INSERT_SCHEMA)
        statement.use {
            statement.setString(1, schema.name)
            statement.setString(2, schema.description)
            statement.setString(3, schema.version)
            statement.setString(4, schema.fulltest_engine)
            statement.execute()
        }

        insertDomains(schema.domains, connection)
        insertTables(schema.tables, schema.name, connection)
    }

    private fun insertDomains(domains: List<Domain>, connection: Connection) {
        domains.forEach { insertDomain(it, connection) }
    }

    private fun insertDomain(domain: Domain, connection: Connection) {
        val statement: PreparedStatement = connection.prepareStatement(INSERT_DOMAIN)
        statement.use {
            statement.setString(1, domain.name)
            statement.setString(2, domain.description)
            statement.setInt(3, domain.name.length)
            statement.setInt(4, domain.charLength)
            statement.setInt(7, domain.width)
            statement.setString(8, domain.align)
            statement.setBoolean(9, domain.properties.find { property -> property == "show_null" } != null)
            statement.setBoolean(10, domain.properties.find { property -> property == "show_lead_nulls" } != null)
            statement.setBoolean(11, domain.properties.find { property -> property == "thousands_separator" } != null)
            statement.setBoolean(12, domain.properties.find { property -> property == "summable boolean" } != null)
            statement.setBoolean(13, domain.properties.find { property -> property == "case_sensitive" } != null)
            statement.setString(14, UUID.randomUUID().toString())
            statement.setString(15, domain.type)
            statement.execute()
        }
    }

    private fun insertTables(tables: List<Table>, schemaName:String, connection: Connection) {
        tables.forEach { insertTable(it, schemaName, connection) }
    }

    private fun insertTable(table: Table, schemaName:String, connection: Connection) {
        val statement: PreparedStatement = connection.prepareStatement(INSERT_TABLE)
        statement.use {
            statement.setString(1, schemaName)
            statement.setString(2, table.name)
            statement.setString(3, table.description)
            statement.setBoolean(4, table.properties.find { property -> property == "can_add" } != null)
            statement.setBoolean(5, table.properties.find { property -> property == "can_delete" } != null)
            statement.setBoolean(6, table.properties.find { property -> property == "can_edit" } != null)
            statement.setString(11, UUID.randomUUID().toString())
            statement.execute()
        }
        insertFields(table.fields, table.name, connection)
        insertConstraints(table.constraints, table.name, connection)
        insertIndexes(table.indexes, table.name, connection)
    }

    private fun insertFields(fields: List<Field>, tableName:String, connection: Connection) {
        fields.forEach { insertField(it, tableName, connection) }
    }

    private fun insertField(field: Field, tableName:String, connection: Connection) {
        val statement: PreparedStatement = connection.prepareStatement(INSERT_FIELD)
        statement.use {
            statement.setString(1, tableName)
            statement.setInt(2, 0)
            statement.setString(3, field.name)
            statement.setString(4, field.rname)
            statement.setString(5, field.description)
            statement.setString(6, field.domain)
            statement.setBoolean(7, field.properties.find { property -> property == "can_input" } != null)
            statement.setBoolean(8, field.properties.find { property -> property == "can_edit" } != null)
            statement.setBoolean(9, field.properties.find { property -> property == "show_in_grid" } != null)
            statement.setBoolean(10, field.properties.find { property -> property == "show_in_details" } != null)
            statement.setBoolean(11, field.properties.find { property -> property == "is_mean" } != null)
            statement.setBoolean(12, field.properties.find { property -> property == "autocalculated" } != null)
            statement.setBoolean(13, field.properties.find { property -> property == "required" } != null)
            statement.setString(14, UUID.randomUUID().toString())
            statement.execute()
        }
    }


    private fun insertConstraints(constraints: List<Constraint>, tableName:String, connection: Connection) {
        constraints.forEach { insertConstraint(it, tableName, connection) }
    }

    private fun insertConstraint(constraint: Constraint, tableName:String, connection: Connection) {
        val statement: PreparedStatement = connection.prepareStatement(INSERT_CONSTRAINT)
        val statementDetails: PreparedStatement = connection.prepareStatement(INSERT_CONSTRAINT_DETAIL)
        statement.use {
            statement.setString(1, tableName)
            statement.setString(3, tableName)
            statement.setBoolean(6, constraint.properties.find { property -> property == "has_value_edit" } != null)
            statement.setBoolean(7, constraint.properties.find { property -> property == "cascading_delete" } != null)
            statement.setString(9, UUID.randomUUID().toString())
            statement.execute()
        }
        statementDetails.use {
//            statementDetails.execute()
        }
    }

    private fun insertIndexes(indexes: List<Index>, tableName:String, connection: Connection) {
        indexes.forEach { insertIndex(it, tableName, connection) }
    }

    private fun insertIndex(index: Index, tableName:String, connection: Connection) {
        val statement: PreparedStatement = connection.prepareStatement(INSERT_INDEX)
        val statementDetails: PreparedStatement = connection.prepareStatement(INSERT_INDEX_DETAIL)
        statement.use {
            statement.setString(1, tableName)
            statement.setString(5, UUID.randomUUID().toString())
            statement.execute()
        }
        statementDetails.use {
//            statementDetails.execute()
        }
    }

    private fun connect(): Connection? {
        var connection: Connection? = null
        var dbPath: String = ""
        try {
            val url = "jdbc:sqlite:$dbPath"
            connection = DriverManager.getConnection(url)
        } catch (e: SQLException) {
            println(e.message)
        }

        return connection
    }

}