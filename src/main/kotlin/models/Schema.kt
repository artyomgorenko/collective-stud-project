package models

class Schema {
    // atributes
    var name: String = ""
    var fulltest_engine: String = ""
    var description: String = ""

    // child tags
    var domains: List<Domain>? = null
    var tables: List<Table>? = null
}