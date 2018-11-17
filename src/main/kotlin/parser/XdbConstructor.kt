package parser

import com.sun.xml.internal.txw2.output.IndentingXMLStreamWriter
import models.*
import org.w3c.dom.Document
import javax.xml.stream.XMLOutputFactory
import java.io.ByteArrayOutputStream

class XdbConstructor {
    object Main {
        @JvmStatic
        fun main(args: Array<String>) {
            val parser: XdbParser = XdbParser()
            val xmlDoc: Document = parser.readXml("src/main/resources/tasks.xdb")
            xmlDoc.documentElement.normalize()

            val xdbConstructor: XdbConstructor = XdbConstructor()
            println(xdbConstructor.createXdbSchema(parser.parseSchema(xmlDoc)!!))
        }
    }

    fun createXdbSchema(schema: Schema): String {
        val out = ByteArrayOutputStream()
        val writer = IndentingXMLStreamWriter(XMLOutputFactory.newFactory().createXMLStreamWriter(out, "UTF-8"))

        writer.writeStartDocument()
        writer.writeStartElement("dbd_schema")
        writer.writeAttribute("fulltext_engine", schema.fulltest_engine)
        writer.writeAttribute("version", schema.version)
        writer.writeAttribute("name", schema.name)
        writer.writeAttribute("description", schema.description)

        writeDomains(schema.domains, writer)
        writeTables(schema.tables, writer)
        writer.writeEndElement()

        writer.writeEndDocument()
        writer.flush()
        out.flush()
        return out.toString()
    }

    private fun writeDomains(domains: List<Domain>, writer: IndentingXMLStreamWriter) {
        writer.writeStartElement("domains")
        domains.forEach { writeDomain(it, writer) }
        writer.writeEndElement()
    }

    private fun writeDomain(domain: Domain, writer: IndentingXMLStreamWriter) {
        writer.writeStartElement("domain")
        writer.writeAttribute("name", domain.name)
        writer.writeAttribute("type", domain.type)
        writer.writeAttribute("align", domain.align)
        writer.writeAttribute("width", domain.width.toString())
        writer.writeAttribute("description", domain.description)
        writer.writeEndElement()
    }

    private fun writeTables(tables: List<Table>, writer: IndentingXMLStreamWriter) {
        writer.writeStartElement("tables")
        tables.forEach { writeTable(it, writer) }
        writer.writeEndElement()
    }

    private fun writeTable(table: Table, writer: IndentingXMLStreamWriter) {
        writer.writeStartElement("table")
        writer.writeAttribute("name", table.name)
        writer.writeAttribute("description", table.description)
        writer.writeAttribute("props", "table.properties")
        writer.writeAttribute("ht_table_flags", "rws")// TODO: Replace with real value
        writer.writeAttribute("access_level", table.accessLevel.toString())


        writeFields(table.fields, writer)
        writeConstaraints(table.constraints, writer)
        writeIndexes(table.indexes, writer)

        writer.writeEndElement()
    }

    private fun writeFields(fields: List<Field>, writer: IndentingXMLStreamWriter) {

    }

    private fun writeField(field: Field, writer: IndentingXMLStreamWriter) {

    }

    private fun writeConstaraints(constraints: List<Constraint>, writer: IndentingXMLStreamWriter) {

    }

    private fun writeConstaraint(constraint: Constraint, writer: IndentingXMLStreamWriter) {

    }

    private fun writeIndexes(indexes: List<Index>, writer: IndentingXMLStreamWriter) {

    }

    private fun writeIndex(index: Index, writer: IndentingXMLStreamWriter) {

    }
}