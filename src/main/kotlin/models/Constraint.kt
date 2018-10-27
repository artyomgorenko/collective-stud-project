package models

data class Constraint(var kind: String = "",
                      var items: String = "",
                      var reference: String = "",
                      var properties: List<String> = emptyList())