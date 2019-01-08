package modules

import com.sun.org.apache.bcel.internal.generic.Select
import com.sun.xml.internal.fastinfoset.alphabet.BuiltInRestrictedAlphabets.table
import common.Tables
import models.*
import org.w3c.dom.Document
import java.sql.*
import java.util.*
import kotlin.collections.ArrayList

class DbdParser {

    object Main {
        @JvmStatic
        fun main(args: Array<String>) {
            val parser: XdbParser = XdbParser()
            val xmlDoc: Document = parser.readXml("src/main/resources/tasks.xdb")
            xmlDoc.documentElement.normalize()

            val constructor: DbdConstructor = DbdConstructor()
            constructor.insertSchema(parser.parseSchema(xmlDoc)!!)

            val parser2 : DbdParser = DbdParser()
            println(parser2.getSchema().name)
        }
    }


    @JvmField val SELECT_SCHEMA = """
        SELECT
          name
        , description
        , version
        , fulltext_engine
        FROM ${Tables.SCHEMAS.tableName}
    """.trimIndent()

    @JvmField val SELECT_TABLE = """
       SELECT
        name
        , description
        , can_add
        , can_edit
        , can_delete
        , temporal_mode
        , ht_table_flags
        , access_level
        , means
        , id
        FROM ${Tables.TABLES.tableName}
    """.trimIndent()

    @JvmField val SELECT_DOMAIN = """
        SELECT
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
        , data_type_id
        FROM ${Tables.DOMAINS.tableName}
    """.trimIndent()

    @JvmField val SELECT_FIELD = """
        SELECT
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
        FROM ${Tables.FIELDS.tableName}
        where table_id = ?
    """.trimIndent()

    @JvmField val SELECT_CONSTRAINT = """
        SELECT
          table_id
        , name
        , constraint_type
        , reference
        , unique_key_id
        , has_value_edit
        , cascading_delete
        , expression
        FROM ${Tables.CONSTRAINTS.tableName}
    """. trimIndent()

    @JvmField val SELECT_CONSTRAINT_DETAIL = """
        SELECT
          constraint_id
        , position
        , field_id
        FROM ${Tables.CONSTRAINT_DETAILS.tableName}
    """.trimIndent()

    @JvmField val SELECT_INDEX = """
        SELECT
          table_id
        , name
        , local
        , kind
        , uuid
        FROM ${Tables.INDEXES.tableName}
    """.trimIndent()

    @JvmField val SELECT_INDEX_DETAIL = """
        SELECT
          index_id
        , position
        , field_id
        , expression
        , descend
        FROM ${Tables.INDEX_DETAILS.tableName}
    """.trimIndent()


    private fun getSchema() : Schema {
        val connection: Connection = this.connect()!!
        connection.autoCommit = false
        return getSchema(connection)
    }

    private fun getSchema(connection : Connection) : Schema {
        val schema : Schema = Schema()

        val statement: PreparedStatement = connection.prepareStatement(SELECT_SCHEMA)
        val rs : ResultSet = statement.executeQuery()
        while (rs.next()) {
            schema.name = rs.getString("name")
            schema.fulltest_engine = rs.getString("fulltext_engine")
            schema.description = rs.getString("description")
            schema.version = rs.getString("version")
            schema.domains = getDomains(connection)
            schema.tables = getTables(connection)
        }
        return schema
    }

    private  fun getTables(connection: Connection) : List<Table>
    {
        var tables: ArrayList<Table> = arrayListOf()
        for (i in 0 until 10)
        {
            tables.add(getTable(connection))
        }
        return tables
    }

    private  fun getDomains(connection: Connection) : List<Domain>
    {
        var domains: ArrayList<Domain> = arrayListOf()
        for (i in 0 until 10)
        {
            domains.add(getDomain(connection))
        }
        return domains
    }


    private fun getIndex(connection : Connection, tableId: Int) : Index {
        val index : Index = Index()

        val statement: PreparedStatement = connection.prepareStatement(SELECT_INDEX)
        statement.setInt(1, tableId)
        val rs : ResultSet = statement.executeQuery()
        while (rs.next()) {
            index.field = rs.getString("??")
            var properties: ArrayList<String> = arrayListOf()
            properties.add(rs.getString("unique_key_id"))
            properties.add(rs.getString("has_value_edit"))
            properties.add(rs.getString("cascading_delete"))
            properties.add(rs.getString("expression"))
            index.properties = properties
        }
        return index
    }

    private fun getIndexDetail(connection : Connection, tableId: Int) : Index {
        val index : Index = Index()

        val statement: PreparedStatement = connection.prepareStatement(SELECT_INDEX_DETAIL)
        statement.setInt(1, tableId)
        val rs : ResultSet = statement.executeQuery()
        while (rs.next()) {
            index.field = rs.getString("??")
            var properties: ArrayList<String> = arrayListOf()
            properties.add(rs.getString("unique_key_id"))
            properties.add(rs.getString("has_value_edit"))
            properties.add(rs.getString("cascading_delete"))
            properties.add(rs.getString("expression"))
            index.properties = properties
        }
        return index
    }

    private fun getConstraint(connection : Connection, tableId: Int) : Constraint {
        val constraint : Constraint = Constraint()

        val statement: PreparedStatement = connection.prepareStatement(SELECT_CONSTRAINT)
        statement.setInt(1, tableId)
        val rs : ResultSet = statement.executeQuery()
        while (rs.next()) {
            constraint.kind = rs.getString("name")
            constraint.items = rs.getString("??")
            constraint.reference = rs.getString("reference")
            var properties: ArrayList<String> = arrayListOf()
            properties.add(rs.getString("unique_key_id"))
            properties.add(rs.getString("has_value_edit"))
            properties.add(rs.getString("cascading_delete"))
            properties.add(rs.getString("expression"))
            constraint.properties = properties
        }
        return constraint
    }

    private fun getConstraintDetail(connection : Connection, tableId: Int) : Constraint {
        val constraint : Constraint = Constraint()

        val statement: PreparedStatement = connection.prepareStatement(SELECT_CONSTRAINT_DETAIL)
        statement.setInt(1, tableId)
        val rs : ResultSet = statement.executeQuery()
        while (rs.next()) {
            constraint.kind = rs.getString("name")
            constraint.items = rs.getString("??")
            constraint.reference = rs.getString("reference")
            var properties: ArrayList<String> = arrayListOf()
            properties.add(rs.getString("unique_key_id"))
            properties.add(rs.getString("has_value_edit"))
            properties.add(rs.getString("cascading_delete"))
            properties.add(rs.getString("expression"))
            constraint.properties = properties
        }
        return constraint
    }

//    @JvmField val SELECT_DOMAIN = """
//        SELECT
//          name
//        , description
//        , length
//        , char_length
//        , precision
//        , scale
//        , width
//        , align
//        , show_null
//        , show_lead_nulls
//        , thousands_separator
//        , summable
//        , case_sensitive
//        , data_type_id
//        FROM ${Tables.DOMAINS.tableName}
//    """.trimIndent()

    private fun getDomain(connection : Connection) : Domain {
        val domain : Domain = Domain()

        val statement: PreparedStatement = connection.prepareStatement(SELECT_DOMAIN)
        val rs : ResultSet = statement.executeQuery()
        while (rs.next()) {
            domain.name = rs.getString("name")
            domain.description = rs.getString("description")
            domain.type = rs.getString("data_type_id")
            domain.align = rs.getString("align")
            domain.width = rs.getInt("width")
            domain.charLength = rs.getInt("char_length")
            var properties: ArrayList<String> = arrayListOf()
            properties.add(rs.getString("show_null"))
            properties.add(rs.getString("show_lead_nulls"))
            properties.add(rs.getString("thousands_separator"))
            properties.add(rs.getString("summable"))
            properties.add(rs.getString("case_sensitive"))
            domain.properties = properties
        }
        return domain
    }


    private  fun getFields(connection: Connection, tableId: Int) : List<Field>
    {
        var fields: ArrayList<Field> = arrayListOf()
        for (i in 0 until 10)
        {
            fields.add(getField(connection, tableId))
        }
        return fields
    }

    private fun getField(connection : Connection, tableId: Int) : Field {
        val field : Field = Field()

        val statement: PreparedStatement = connection.prepareStatement(SELECT_FIELD)
        statement.setInt(1, tableId)
        val rs : ResultSet = statement.executeQuery()
        while (rs.next()) {
            field.name = rs.getString("name")
            field.rname = rs.getString("russian_short_name")
            field.domain = rs.getString("domain_id")
            field.description = rs.getString("description")
            var properties: ArrayList<String> = arrayListOf()
            properties.add(rs.getString("can_input"))
            properties.add(rs.getString("can_edit"))
            properties.add(rs.getString("show_in_grid"))
            properties.add(rs.getString("show_in_details"))
            properties.add(rs.getString("is_mean"))
            properties.add(rs.getString("autocalculated"))
            properties.add(rs.getString("required"))
            field.properties = properties
        }
        return field
    }

    private fun getTable(connection : Connection) : Table {
        val table : Table = Table()

        val statement: PreparedStatement = connection.prepareStatement(SELECT_TABLE)
        val rs : ResultSet = statement.executeQuery()
        while (rs.next()) {
            table.name = rs.getString("name")
            table.description = rs.getString("description")
            table.ht_table_flags = rs.getBoolean("ht_table_flags")
            table.accessLevel = rs.getInt("access_level")
            var tableId: Int = rs.getInt("id")
            var properties: ArrayList<String> = arrayListOf()
            properties.add(rs.getString("can_add"))
            properties.add(rs.getString("can_edit"))
            properties.add(rs.getString("can_delete"))
            table.properties = properties
            table.fields = getFields(connection, tableId)
        }

        return table
    }

    private fun connect(): Connection? {
        var connection: Connection? = null
        var dbPath: String = "C:\\Program Files (x86)\\SQLite3\\myDB_1.db"
        try {
            val url = "jdbc:sqlite:$dbPath"
            connection = DriverManager.getConnection(url)
        } catch (e: SQLException) {
            println(e.message)
        }
        return connection
    }
}