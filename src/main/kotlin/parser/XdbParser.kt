package parser

import models.*
import org.w3c.dom.*
import org.xml.sax.InputSource
import java.io.File
import java.io.StringReader
import javax.xml.parsers.DocumentBuilderFactory

class XdbParser {

    // For parser tests
    object Main {
        @JvmStatic
        fun main(args: Array<String>) {
            val parser: XdbParser = XdbParser()
            val xmlDoc: Document = parser.readXml("src/main/resources/tasks.xdb")
            xmlDoc.documentElement.normalize()
            parser.parseSchema(xmlDoc)
        }
    }

    fun readXml(filepath: String): Document {
        val xmlFile = File(filepath)

        val dbFactory = DocumentBuilderFactory.newInstance()
        val dBuilder = dbFactory.newDocumentBuilder()
        val xmlInput = InputSource(StringReader(xmlFile.readText()))
        val doc = dBuilder.parse(xmlInput)

        return doc
    }

    fun parseSchema(xmlDoc: Document): Schema? {
        try {
            val schema: Schema = Schema()
            val attributes: NamedNodeMap = xmlDoc.documentElement.attributes

            attributes.getNamedItem("name")?.let { schema.name = it.nodeValue }
            attributes.getNamedItem("fulltest_engine")?.let { schema.fulltest_engine = it.nodeValue }
            attributes.getNamedItem("version")?.let { schema.version = it.nodeValue }
            attributes.getNamedItem("description")?.let { schema.description = it.nodeValue }

            // add domains to schema
            val domainsList: ArrayList<Domain> = arrayListOf()
            val domains: NodeList = xmlDoc.getElementsByTagName("domain")
            for (i in 0 until domains.length) {
                val domainNode: Node? = domains.item(i)
                if (domainNode != null) {
                    domainsList.add(parseDomain(domainNode))
                }
            }
            schema.domains = domainsList

            // add tables to schema
            val tablesList: ArrayList<Table> = arrayListOf()
            val tables: NodeList = xmlDoc.getElementsByTagName("table")
            for (i in 0 until tables.length) {
                val tableNode : Node = tables.item(i)
                parseTable(tableNode).let { tablesList.add(it!!) }
            }
            schema.tables = tablesList

            return schema
        } catch (ex: DOMException) {
            println("Schema parsing exception: $ex")
        }

        return null
    }

    private fun parseTable(tableNode: Node): Table? {
        try {
            val table: Table = Table()
            val attributes: NamedNodeMap = tableNode.attributes

            attributes.getNamedItem("name")?.let { table.name = it.nodeValue }
            attributes.getNamedItem("description")?.let { table.description = it.nodeValue }
            attributes.getNamedItem("ht_table_flags")?.let { table.ht_table_flags = false }
            attributes.getNamedItem("accessLevel")?.let { table.accessLevel = Integer.parseInt(it.nodeValue) }
            attributes.getNamedItem("properties")?.let { table.properties = it.nodeValue.split(",") }

            // add child tags
            val fields: ArrayList<Field> = ArrayList()
            val constraints: ArrayList<Constraint> = ArrayList()
            val indexes: ArrayList<Index> = ArrayList()

            for (i in 0 until tableNode.childNodes.length) {
                val node: Node = tableNode.childNodes.item(i)
                when {
                    node.nodeName.equals("field") -> {
                        fields.add(parseField(node))
                    }
                    node.nodeName.equals("constraint") -> {
                        constraints.add(parseConstraint(node))
                    }
                    node.nodeName.equals("index") -> {
                        indexes.add(parseIndex(node))
                    }
                }
            }

            table.fields = fields
            table.constraints = constraints
            table.indexes = indexes

            return table
        } catch (ex: DOMException) {
            println("Table parsing exception: $ex")
        }

        return null
    }

    private fun parseDomain(node: Node): Domain {
        val domain: Domain = Domain()
        val attributes: NamedNodeMap = node.attributes

        attributes.getNamedItem("name")?.let  { domain.name = it.nodeValue }
        attributes.getNamedItem("description")?.let  { domain.description = it.nodeValue }
        attributes.getNamedItem("type")?.let { domain.type = it.nodeValue }
        attributes.getNamedItem("align")?.let { domain.align = it.nodeValue }
        attributes.getNamedItem("width")?.let { domain.width = Integer.parseInt(it.nodeValue) }
        attributes.getNamedItem("charLength")?.let { domain.charLength = Integer.parseInt(it.nodeValue)}
        attributes.getNamedItem("properties")?.let { domain.properties = it.nodeValue.split(",") }

        return domain
    }

    private fun parseConstraint(node: Node): Constraint {
        val constraint: Constraint = Constraint()

        return constraint
    }

    private fun parseField(node: Node): Field {
        val field: Field = Field()

        return field
    }

    private fun parseIndex(node: Node): Index {
        val index: Index = Index()

        return index
    }
}