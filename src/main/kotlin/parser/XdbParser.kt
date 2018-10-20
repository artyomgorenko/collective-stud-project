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

    private fun readXml(filepath: String): Document {
        val xmlFile = File(filepath)

        val dbFactory = DocumentBuilderFactory.newInstance()
        val dBuilder = dbFactory.newDocumentBuilder()
        val xmlInput = InputSource(StringReader(xmlFile.readText()))
        val doc = dBuilder.parse(xmlInput)

        return doc
    }

    private fun parseSchema(xmlDoc: Document): Schema? {
        try {
            val schemaName: String = xmlDoc.documentElement.getAttribute("name")
            val fullTestEngine: String = xmlDoc.documentElement.getAttribute("fulltext_engine")
            val schemaVersion: String = xmlDoc.documentElement.getAttribute("version")
            val schemaDescription: String = xmlDoc.documentElement.getAttribute("description")
            val schema: Schema = Schema(schemaName, fullTestEngine, schemaVersion, schemaDescription)

            // add domains to schema
            val domainsList: ArrayList<Domain> ?= arrayListOf()
            val domains: NodeList = xmlDoc.getElementsByTagName("domain")
            for (i in 0 until domains.length) {
                val domainNode: Node? = domains.item(i)
                if (domainNode != null) {
                    domainsList?.plus(parseDomain(domainNode))
                }
            }
            schema.domains = domainsList

            // add tables to schema
            val tablesList: ArrayList<Table> ?= arrayListOf()
            val tables: NodeList = xmlDoc.getElementsByTagName("table")
            for (i in 0 until tables.length) {
                val tableNode : Node? = tables.item(i)
                if (tableNode != null) {
                    parseTable(tableNode)?.let { tablesList?.add(it) }
                }
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
            val namedNodeMap: NamedNodeMap = tableNode.attributes

            table.name = namedNodeMap.getNamedItem("name").nodeValue
            table.description = namedNodeMap.getNamedItem("description").nodeValue
            table.ht_table_flags = false // TODO: need to get actual value
            table.accessLevel = Integer.parseInt(namedNodeMap.getNamedItem("accessLevel").nodeValue)
            table.properties = namedNodeMap.getNamedItem("properties").nodeValue.split(",")

            for (i in 0 until tableNode.childNodes.length) {
                val node: Node = tableNode.childNodes.item(i)
                when {
                    node.nodeName.equals("field") -> {
                        table.fields?.add(parseField(node))
                    }
                    node.nodeName.equals("constraint") -> {
                        table.constraints?.add(parseConstarint(node))
                    }
                    node.nodeName.equals("index") -> {
                        table.indexes?.add(parseIndex(node))
                    }
                }
            }

            return table
        } catch (ex: DOMException) {
            println("Table parsing exception: $ex")
        }

        return null
    }

    private fun parseDomain(node: Node): Domain {
        val domain: Domain = Domain()

        return domain
    }

    private fun parseConstarint(node: Node): Constraint {
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