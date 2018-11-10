package models

data class Table(var name: String = "",
                 var description: String = "",
                 var ht_table_flags: Boolean = false,
                 var accessLevel: Int = 0,
                 var properties: List<String> = emptyList(),
                 // child tags
                 var fields: List<Field> = emptyList(),
                 var constraints: List<Constraint> = emptyList(),
                 var indexes: List<Index> = emptyList())