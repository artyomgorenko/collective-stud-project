# collective-stud-project
Учебный проект по дисциплине - Коллективная разраотка приложений
# Структура проекта
- **common**
  - `DbDescription.kt` - описатель базы данных(DBD)
  - `Tables.kt` - таблицы для DBD

- **models** 
  - `Constraint.kt` - data-класс Constraint
  - `Domain.kt` - data-класс Domain
  - `Field.kt` - data-класс Field
  - `Index.kt` - data-класс Index
  - `Schema.kt` - data-класс Schema
  - `Table.kt` - data-класс Table

- **modules**
  - `XdbParser.kt` - модуль, для создания RAM-представления, на основе XDB (XDB -> RAM)
  - `XdbConstructor.kt` - модуль, для создания XDB на основе RAM (RAM -> XDB)
  - `DbdConstructor.kt` - модуль, для создания DBD (RAM -> DBD)
  - `MetaDataParser.kt` - модуль, для создания RAM-представления на основе метаданных из MSSQL (MSSQL -> RAM)
  - `DataTransferHelper.kt` - модуль, для переноса данных из MSSQL в PostgreSQL (MSSQL -> PostgreSQL)
  - `SchemaConstructor.kt` - модуль, для генерации DDL целевой БД PostgreSQL на основе RAM-представления (RAM -> PostgreSQL)
  - `Utils.kt` - утилиты приложения
 
