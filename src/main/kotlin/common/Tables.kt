package common

enum class Tables(val tableName:String)  {
    SCHEMAS("dbd\$schemas"),
    DOMAINS("dbd\$domains"),
    TABLES("dbd\$tables"),
    FIELDS("dbd\$fields"),
    SETTINGS("dbd\$settings"),
    CONSTRAINTS("dbd\$constraints"),
    CONSTRAINT_DETAILS("dbd\$constraint_details"),
    INDEXES("dbd\$indexes"),
    INDEX_DETAILS("dbd\$index_details"),
    DATA_TYPES("dbd\$data_types"),
    VIEW_FIELDS("dbd\$view_fields"),
    VIEW_DOMAINS("dbd\$view_domains"),
    VIEW_CONSTRAINTS("dbd\$view_constraints"),
    VIEW_INDEXES("dbd\$view_indexes"),
}