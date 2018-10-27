package models

data class Field(var name: String = "",
                 var rname: String = "",
                 var domain: String = "",
                 var properties: List<String> = emptyList())