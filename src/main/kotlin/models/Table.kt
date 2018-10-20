package models

class Table {
    // atributes
    var name: String = ""
    var description: String = ""
    var ht_table_flags : Boolean = false
    var accessLevel : Int = 0
    var properties: List<String>? = null

    // child tags
    var fields: ArrayList<Field>? = null
    var constraints: ArrayList<Constraint>? = null
    var indexes: ArrayList<Index>? = null
}