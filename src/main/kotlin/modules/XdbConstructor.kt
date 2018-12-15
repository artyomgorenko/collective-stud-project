package modules

import com.sun.xml.internal.txw2.output.IndentingXMLStreamWriter
import models.*
import org.w3c.dom.Document
import java.io.ByteArrayOutputStream
import javax.xml.stream.XMLOutputFactory

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
        writeAttribute("fulltext_engine", schema.fulltest_engine, writer)
        writeAttribute("version", schema.version, writer)
        writeAttribute("name", schema.name, writer)
        writeAttribute("description", schema.description, writer)

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
        writeAttribute("name", domain.name, writer)
        writeAttribute("type", domain.type, writer)
        writeAttribute("align", domain.align, writer)
        writeAttribute("width", domain.width.toString(), writer)
        writeAttribute("description", domain.description, writer)
        writer.writeEndElement()
    }

    private fun writeTables(tables: List<Table>, writer: IndentingXMLStreamWriter) {
        writer.writeStartElement("tables")
        tables.forEach { writeTable(it, writer) }
        writer.writeEndElement()
    }

    private fun writeTable(table: Table, writer: IndentingXMLStreamWriter) {
        writer.writeStartElement("table")
        writeAttribute("name", table.name, writer)
        writeAttribute("description", table.description, writer)
        writeAttribute("props", "table.properties", writer)
        writeAttribute("ht_table_flags", "rws", writer)// TODO: Replace with real value
        writeAttribute("access_level", table.accessLevel.toString(), writer)


        writeFields(table.fields, writer)
        writeConstaraints(table.constraints, writer)
        writeIndexes(table.indexes, writer)

        writer.writeEndElement()
    }

    private fun writeFields(fields: List<Field>, writer: IndentingXMLStreamWriter) {
        fields.forEach { writeField(it, writer) }
    }

    private fun writeField(field: Field, writer: IndentingXMLStreamWriter) {
        writer.writeStartElement("field")
        writeAttribute("name", field.name, writer)
        writeAttribute("rname", field.rname, writer)
        writeAttribute("domain", field.domain, writer)
        writeAttribute("description", field.description, writer)

        var properties: String = constractProperties(field.properties)
        writeAttribute("properties", properties, writer)

        writer.writeEndElement()
    }

    private fun writeConstaraints(constraints: List<Constraint>, writer: IndentingXMLStreamWriter) {
        constraints.forEach { writeConstaraint(it, writer) }

    }

    private fun writeConstaraint(constraint: Constraint, writer: IndentingXMLStreamWriter) {
        writer.writeStartElement("constraint")
        writeAttribute("kind", constraint.kind, writer)
        writeAttribute("items", constraint.items, writer)
        writeAttribute("reference", constraint.reference, writer)

        var properties: String = constractProperties(constraint.properties)
        writeAttribute("properties", properties, writer)

        writer.writeEndElement()
    }

    private fun writeIndexes(indexes: List<Index>, writer: IndentingXMLStreamWriter) {
        indexes.forEach { writeIndex(it, writer) }
    }

    private fun writeIndex(index: Index, writer: IndentingXMLStreamWriter) {
        writer.writeStartElement("index")
        writeAttribute("field", index.field, writer)

        var properties: String = constractProperties(index.properties)
        writeAttribute("properties", properties, writer)

        writer.writeEndElement()
    }

    private fun writeAttribute(name: String, attributeValue: String, writer: IndentingXMLStreamWriter) {
        if (!attributeValue.isEmpty()) {
            writer.writeAttribute(name, attributeValue)
        }
    }

    private fun constractProperties(properties: List<String>): String {
        var propertiesStr: String = ""
        for (i in 0 until properties.size) {
            propertiesStr += properties[i]
            if (i < properties.size - 1) {
                propertiesStr += ","
            }
        }
        return propertiesStr
    }
}