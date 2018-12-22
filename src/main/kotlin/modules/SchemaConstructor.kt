package modules

import models.*
import org.w3c.dom.Document
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.sql.Statement

class SchemaConstructor {

    object Main {
        @JvmStatic
        fun main(args: Array<String>) {
            val parser: XdbParser = XdbParser()
            val xmlDoc: Document = parser.readXml("src/main/resources/tasks.xdb")
            xmlDoc.documentElement.normalize()

            val constructor : SchemaConstructor = SchemaConstructor()
            val schema : Schema = parser.parseSchema(xmlDoc)!!
            constructor.createSchema(schema)
        }
    }

    fun createSchema(schema : Schema) {
        val connection: Connection = connect() ?: return
        connection.use {
            try {
                connection.autoCommit = false
                createSchema(schema, connection)
                connection.commit()
            }  catch (e: SQLException) {
                connection.rollback()
                println(e.printStackTrace())
            }
        }
    }

    fun createSchema(schema : Schema, connection : Connection) {
        createDomains(schema.domains, connection)
        createTables(schema.tables, connection)
    }

    private fun createDomains(domains: List<Domain>, connection: Connection) {
        domains.forEach { domain ->
            createDomain(domain, connection)
        }
    }

    private fun createDomain(domain: Domain, connection: Connection) {
        val sb : StringBuilder = StringBuilder()

        var postgresType: String = ""
        when {
            domain.type.equals("BLOB") -> {
                postgresType = "BYTEA"
            }
            domain.type.equals("STRING") -> {
                postgresType = "VARCHAR"
            }
            domain.type.equals("LARGEINT") -> {
                postgresType = "NUMERIC"
            }
            domain.type.equals("WORD") -> {
                postgresType = "NUMERIC"
            }
            domain.type.equals("MEMO") -> {
                postgresType = "VARCHAR"
            }
            domain.type.equals("DATE") -> {
                postgresType = "TIMESTAMP"
            }
            domain.type.equals("BYTE") -> {
                postgresType = "SMALLINT"
            }
            else -> {
                postgresType = domain.type
            }
        }

        if (!postgresType.equals("BOOLEAN") && !postgresType.equals("BYTE") && !postgresType.equals("SMALLINT")) {
            if (domain.width != 0) postgresType += "(${domain.width})"
        }
        sb.append("CREATE DOMAIN ${domain.name} AS $postgresType;\n")

        val statement : Statement = connection.createStatement()
        statement.use {
            println("DOMAIN:" + sb.toString())
            statement.executeUpdate(sb.toString())
        }
    }

    private fun createTables(tables: List<Table>, connection: Connection) {
        tables.forEach { table ->
          try {
              createTable(table, connection)
          } catch (e : SQLException) {
              println(e.message)
          }
        }
        tables.forEach { table ->
            try {
                createTableDetails(table, connection)
            } catch (e : SQLException) {
                println(e.message)
            }
        }
    }

    private fun createTable(table: Table, connection: Connection) {
        val sb:StringBuilder = StringBuilder()

        sb.append("CREATE TABLE IF NOT EXISTS ${table.name} (\n")
        for (i in 0 until table.fields.size - 1) {
            sb.append("${table.fields[i].name} ${table.fields[i].domain}, \n")
        }
        sb.append("${table.fields[table.fields.size - 1].name} ${table.fields[table.fields.size - 1].domain}\n")
        sb.append(");\n")

        sb.append(createCommentOnTableSql(table))
        table.fields.forEach { field ->
            sb.append(createCommentOnTableSql(table, field))
        }

        val statement : Statement = connection.createStatement()
        statement.use {
            println(sb.toString())
            statement.executeUpdate(sb.toString())
        }
    }

    private fun createTableDetails(table: Table, connection: Connection) {
        val sb:StringBuilder = StringBuilder()
        table.constraints.forEach { constraint ->
            sb.append(createConstraintSql(constraint, table.name))
        }
        table.indexes.forEach { index ->
            sb.append(createIndexSql(index, table.name))
        }
        val statement : Statement = connection.createStatement()
        statement.use {
            println(sb.toString())
            statement.executeUpdate(sb.toString())
        }
    }

    private fun createConstraintSql(constraint: Constraint, tableName : String) : String  {
        val constraintName : String = "${constraint.kind}_${constraint.items}"
        var constraintKind : String = ""
        if ("PRIMARY".equals(constraint.kind)) {
            constraintKind = constraint.kind + " KEY"
            return "ALTER TABLE $tableName ADD CONSTRAINT $constraintName $constraintKind(${constraint.items});\n"
        } else if ("FOREIGN".equals(constraint.kind)) {
            constraintKind = constraint.kind + " KEY"
            return "ALTER TABLE $tableName ADD CONSTRAINT $constraintName $constraintKind(${constraint.items}) REFERENCES ${constraint.reference}(${constraint.items});\n"
        }
        return constraintKind;
    }

    private fun createIndexSql(index : Index, tableName: String) : String {
        return "CREATE INDEX ON $tableName (${index.field});\n"
    }

    private fun createCommentOnTableSql(table: Table, field: Field) : String {
        return "COMMENT ON COLUMN ${table.name}.${field.name} IS '${field.description}';\n"
    }

    private fun createCommentOnTableSql(table: Table) : String {
        return "COMMENT ON TABLE ${table.name} IS '${table.description}';\n"
    }

    private fun connect() : Connection? {
        var connection: Connection? = null
        try {
            val url = "jdbc:postgresql://localhost:5432/postgres?user=admin&password=admin"
            connection = DriverManager.getConnection(url)
        } catch (e: SQLException) {
            println(e.message)
        }

        return connection
    }
}