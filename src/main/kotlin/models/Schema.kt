package models

data class Schema(var name: String = "",
                  var fulltest_engine: String = "",
                  var description: String = "",
                  var version: String = "",
                  var domains: List<Domain> = emptyList(),
                  var tables: List<Table> = emptyList())