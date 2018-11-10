package models

class Domain(var name: String = "",
             var description: String = "",
             var type: String = "",
             var align: String = "",
             var width: Int = 0,
             var charLength: Int = 0,
             var properties: List<String> = emptyList())