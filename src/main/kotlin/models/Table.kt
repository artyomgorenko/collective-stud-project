package models

class Table {
    // atributes
    var name: String = ""
    var description: String = ""
    var ht_table_flags : Boolean = false
    var accessLevel : Int = 0
    var properties: List<String>? = null

    // child tags
    var fields: List<Field>? = null
    var constraints: List<Constraint>? = null
    var indexes: List<Index>? = null
}