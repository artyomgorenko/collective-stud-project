package models

class Schema {
    // atributes
    var name: String = ""
    var fulltest_engine: String = ""
    var description: String = ""
    var version: String = ""

    // child tags
    var domains: List<Domain>? = null
    var tables: List<Table>? = null

    constructor(name: String, fulltest_engine: String, description: String, version: String) {
        this.name = name
        this.fulltest_engine = fulltest_engine
        this.description = description
        this.version = version
    }
}