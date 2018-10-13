package models

class Table {
    var name: String = ""
    var description: String = ""
    var ht_table_flags : Boolean = false
    var accessLevel : Int = 0

    var fields: List<Field>? = null
    var constraints: List<Constraint>? = null
    var indexes: List<Index>? = null
    var properties: List<String>? = null
}