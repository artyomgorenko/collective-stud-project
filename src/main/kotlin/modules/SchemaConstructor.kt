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

    val CONNECTION_URL = ""

    fun createSchema(schema : Schema) {
        val connection: Connection = Utils.connect(CONNECTION_URL) ?: return
        connection.autoCommit = false
        val ddl : String = createSchemaDDL(schema)

        connection.use {
            try {
                val statement : Statement = connection.createStatement()
                statement.use {
                    println(ddl)
                    statement.executeUpdate(ddl)
                }
                connection.commit()
            }  catch (e: SQLException) {
                connection.rollback()
                e.printStackTrace()
            }
        }
    }

    fun createSchemaDDL(schema: Schema): String {
        val sb : StringBuilder = StringBuilder()
        createDomains(schema.domains, sb)
        createTables(schema.tables, sb)
        return sb.toString()
    }

    private fun createDomains(domains: List<Domain>, sb : StringBuilder) {
        domains.forEach { domain ->
            createDomain(domain, sb)
        }
    }

    private fun createDomain(domain: Domain, sb : StringBuilder) {
        var postgresType: String = convertToPostgresType(domain.type)

        if (!postgresType.equals("BOOLEAN") && !postgresType.equals("BYTE") && !postgresType.equals("SMALLINT")) {
            if (domain.width != 0) postgresType += "(${domain.width})"
        }
        sb.append("CREATE DOMAIN ${domain.name} AS $postgresType;\n")
    }

    private fun createTables(tables: List<Table>, sb: StringBuilder) {
        tables.forEach { table ->
          try {
              createTable(table, sb)
          } catch (e : SQLException) {
              println(e.message)
          }
        }
        tables.forEach { table ->
            try {
                createTableDetails(table, sb)
            } catch (e : SQLException) {
                println(e.message)
            }
        }
    }

    private fun createTable(table: Table, sb: StringBuilder) {
        sb.append("CREATE TABLE IF NOT EXISTS ${table.name} (\n")
        for (i in 0 until table.fields.size - 1) {
            sb.append("${table.fields[i].name} ${convertToPostgresType(table.fields[i].domain)}, \n")
        }
        sb.append("${table.fields[table.fields.size - 1].name} ${convertToPostgresType(table.fields[table.fields.size - 1].domain)}\n")
        sb.append(");\n")

        sb.append(createCommentOnTableSql(table))
        table.fields.forEach { field ->
            sb.append(createCommentOnTableSql(table, field))
        }
    }

    private fun createTableDetails(table: Table, sb: StringBuilder) {
        table.constraints.forEach { constraint ->
            sb.append(createConstraintSql(constraint, table.name))
        }
        table.indexes.forEach { index ->
            sb.append(createIndexSql(index, table.name))
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

    private fun convertToPostgresType(type: String): String {
        val postgresType: String
        when {
            type.equals("BLOB")
                    || type.equals("image") -> {
                postgresType = "BYTEA"
            }
            type.equals("STRING")
                    || type.toUpperCase().equals("NCHAR")
                    || type.toUpperCase().equals("NVARCHAR")
                    || type.toUpperCase().equals("NTEXT") -> {
                postgresType = "VARCHAR"
            }
            type.equals("LARGEINT") -> {
                postgresType = "NUMERIC"
            }
            type.equals("WORD") -> {
                postgresType = "NUMERIC"
            }
            type.equals("MEMO") -> {
                postgresType = "VARCHAR"
            }
            type.equals("DATE")
                    || type.toUpperCase().equals("DATETIME") -> {
                postgresType = "TIMESTAMP"
            }
            type.equals("BYTE") -> {
                postgresType = "SMALLINT"
            }
            type.equals("bit") -> {
                postgresType = "boolean"
            }
            else -> {
                postgresType = type
            }
        }
        return postgresType
    }
}