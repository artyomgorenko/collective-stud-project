package common

class DbDescription {

    @JvmField val SQL_DBD_PRE_INIT = """
        pragma foreign_keys = on;
        begin transaction;

        --
        -- Каталог схем для таблиц
        --
        create table ${Tables.SCHEMAS.tableName} (
            id integer primary key autoincrement not null,
            name varchar not null,              -- имя схемы
            description varchar default(null)   -- описание
            version varchar default(null)
            fulltext_engine varchar default(null)
        );
    """.trimIndent()

    @JvmField val SQL_DBD_DOMAINS_TABLE_INIT = """
        --
        -- Домены
        --
        create table ${Tables.DOMAINS.tableName} (
            id  integer primary key autoincrement default(null),
            name varchar unique default(null),                  -- имя домена
            description varchar default(null),                  -- описание
            data_type_id integer not null,                      -- идентификатор типа (${Tables.DATA_TYPES.tableName})
            length integer default(null),                       -- длина
            char_length integer default(null),                  -- длина в символах
            precision integer default(null),                    -- точность
            scale integer default(null),                        -- количество знаков после запятой
            width integer default(null),                        -- ширина визуализации в символах
            align char default(null),                           -- признак выравнивания
            show_null boolean default(null),                    -- нужно показывать нулевое значение?
            show_lead_nulls boolean default(null),              -- следует ли показывать лидирующие нули?
            thousands_separator boolean default(null),          -- нужен ли разделитель тысяч?
            summable boolean default(null),                     -- признак того, что поле является суммируемым
            case_sensitive boolean default(null),               -- признак необходимости регистронезависимого поиска для поля
            unnamed boolean default(null),                      -- именованный/неименованный домен
            uuid varchar unique not null COLLATE NOCASE         -- уникальный идентификатор домена
        );

        create index "idx.FZX832TFV" on ${Tables.DOMAINS.tableName}(data_type_id);
        create index "idx.4AF9IY0XR" on ${Tables.DOMAINS.tableName}(uuid);
    """.trimIndent()

    @JvmField val SQL_DBD_TABLES_TABLE_INIT = """
        --
        -- Каталог таблиц
        --
        create table ${Tables.TABLES.tableName} (
            id integer primary key autoincrement default(null),
            schema_id integer default(null),                    -- идетификатор схемы (${Tables.SCHEMAS.tableName})
            name varchar unique,                                -- имя таблицы
            description varchar default(null),                  -- описание
            can_add boolean default(null),                      -- разрешено ли добавление в таблицу
            can_edit boolean default(null),                     -- разрешено ли редактирование  таблице?
            can_delete boolean default(null),                   -- разрешено ли удаление в таблице
            temporal_mode varchar default(null),                -- временная таблица или нет? Если временная, то какого типа?
            ht_table_flags boolean default(null),
            access_level integer default(null),
            means varchar default(null),                        -- шаблон описания записи таблицы
            uuid varchar unique not null COLLATE NOCASE         -- уникальный идентификатор таблицы
        );

        create index "idx.GCOFIBEBJ" on ${Tables.TABLES.tableName}(name);
        create index "idx.2J02T9LQ7" on ${Tables.TABLES.tableName}(uuid);
    """.trimIndent()

    @JvmField val SQL_DBD_TABLES_INIT = """
        --
        -- Поля таблиц
        --
        create table ${Tables.FIELDS.tableName} (
            id integer primary key autoincrement default(null),
            table_id integer not null,                          -- идентификатор таблицы (${Tables.TABLES.tableName})
            position integer not null,                          -- номер поля в таблице (для упорядочивания полей)
            name varchar not null,                              -- латинское имя поля (будет использовано в схеме Oracle)
            russian_short_name varchar not null,                -- русское имя поля для отображения пользователю в интерактивных режимах
            description varchar default(null),                  -- описание
            domain_id integer not null,                         -- идентификатор типа поля (${Tables.DOMAINS.tableName})
            can_input boolean default(null),                    -- разрешено ли пользователю вводить значение в поле?
            can_edit boolean default(null),                     -- разрешено ли пользователю изменять значение в поле?
            show_in_grid boolean default(null),                 -- следует ли отображать значение поля в браузере таблицы?
            show_in_details boolean default(null),              -- следует ли отображать значение поля в полной информации о записи таблицы?
            is_mean boolean default(null),                      -- является ли поле элементом описания записи таблицы?
            autocalculated boolean default(null),               -- признак того, что значение в поле вычисляется программным кодом
            required boolean default(null),                     -- признак того, что поле дорлжно быть заполнено
            uuid varchar unique not null COLLATE NOCASE         -- уникальный идентификатор поля
        );
        create index "idx.7UAKR6FT7" on ${Tables.FIELDS.tableName}(table_id);
        create index "idx.7HJ6KZXJF" on ${Tables.FIELDS.tableName}(position);
        create index "idx.74RSETF9N" on ${Tables.FIELDS.tableName}(name);
        create index "idx.6S0E8MWZV" on ${Tables.FIELDS.tableName}(domain_id);
        create index "idx.88KWRBHA7" on ${Tables.FIELDS.tableName}(uuid);
        --
        -- Спец. настройки описателя
        --
        create table ${Tables.SETTINGS.tableName} (
            key varchar primary key not null,
            value varchar,
            valueb BLOB
        );
        --
        -- Ограничения
        --
        create table ${Tables.CONSTRAINTS.tableName} (
            id integer primary key autoincrement default (null),
            table_id integer not null,                           -- идентификатор таблицы (${Tables.TABLES.tableName})
            name varchar default(null),                          -- имя ограничения
            constraint_type char default(null),                  -- вид ограничения
            reference integer default(null),                     -- идентификатор таблицы (${Tables.TABLES.tableName}), на которую ссылается внешний ключ
            unique_key_id integer default(null),                 -- (опционально) идентификатор ограничения (${Tables.CONSTRAINTS.tableName}) таблицы, на которую ссылается внешний ключ (*1*)
            has_value_edit boolean default(null),                -- признак наличия поля ввода ключа
            cascading_delete boolean default(null),              -- признак каскадного удаления для внешнего ключа
            expression varchar default(null),                    -- выражение для контрольного ограничения
            uuid varchar unique not null COLLATE NOCASE          -- уникальный идентификатор ограничения
        );
        create index "idx.6F902GEQ3" on ${Tables.CONSTRAINTS.tableName}(table_id);
        create index "idx.6SRYJ35AJ" on ${Tables.CONSTRAINTS.tableName}(name);
        create index "idx.62HLW9WGB" on ${Tables.CONSTRAINTS.tableName}(constraint_type);
        create index "idx.5PQ7Q3E6J" on ${Tables.CONSTRAINTS.tableName}(reference);
        create index "idx.92GH38TZ4" on ${Tables.CONSTRAINTS.tableName}(unique_key_id);
        create index "idx.6IOUMJINZ" on ${Tables.CONSTRAINTS.tableName}(uuid);
        --
        -- Детали ограничений
        --
        create table ${Tables.CONSTRAINT_DETAILS.tableName} (
            id integer primary key autoincrement default(null),
            constraint_id integer not null,                      -- идентификатор ограничения (${Tables.CONSTRAINTS.tableName})
            position integer not null,                           -- номер элемента ограничения
            field_id integer not null default(null)              -- идентификатор поля (${Tables.FIELDS.tableName}) в таблице, для которой определено ограничение
        );
        create index "idx.5CYTJWVWR" on ${Tables.CONSTRAINT_DETAILS.tableName}(constraint_id);
        create index "idx.507FDQDMZ" on ${Tables.CONSTRAINT_DETAILS.tableName}(position);
        create index "idx.4NG17JVD7" on ${Tables.CONSTRAINT_DETAILS.tableName}(field_id);
        --
        -- Индексы
        --
        create table ${Tables.INDEXES.tableName} (
            id integer primary key autoincrement default(null),
            table_id integer not null,                           -- идентификатор таблицы (${Tables.TABLES.tableName})
            name varchar default(null),                          -- имя индекса
            local boolean default(0),                            -- показывает тип индекса: локальный или глобальный
            kind char default(null),                             -- вид индекса (простой/уникальный/полнотекстовый)
            uuid varchar unique not null COLLATE NOCASE          -- уникальный идентификатор индекса
        );
        create index "idx.12XXTJUYZ" on ${Tables.INDEXES.tableName}(table_id);
        create index "idx.6G0KCWN0R" on ${Tables.INDEXES.tableName}(name);
        create index "idx.FQH338PQ7" on ${Tables.INDEXES.tableName}(uuid);
        --
        -- Детали индексов
        --
        create table ${Tables.INDEX_DETAILS.tableName} (
            id integer primary key autoincrement default(null),
            index_id integer not null,                           -- идентификатор индекса (${Tables.INDEXES.tableName})
            position integer not null,                           -- порядковый номер элемента индекса
            field_id integer default(null),                      -- идентификатор поля (${Tables.FIELDS.tableName}), участвующего в индексе
            expression varchar default(null),                    -- выражение для индекса
            descend boolean default(null)                        -- направление сортировки
        );
        create index "idx.H1KFOWTCB" on ${Tables.INDEX_DETAILS.tableName}(index_id);
        create index "idx.BQA4HXWNF" on ${Tables.INDEX_DETAILS.tableName}(field_id);
        --
        -- Типы данных
        --
        create table ${Tables.DATA_TYPES.tableName} (
            id integer primary key autoincrement, -- идентификатор типа
            type_id varchar unique not null       -- имя типа
        );
        insert into ${Tables.DATA_TYPES.tableName}(type_id) values ('STRING');
        insert into ${Tables.DATA_TYPES.tableName}(type_id) values ('SMALLINT');
        insert into ${Tables.DATA_TYPES.tableName}(type_id) values ('INTEGER');
        insert into ${Tables.DATA_TYPES.tableName}(type_id) values ('WORD');
        insert into ${Tables.DATA_TYPES.tableName}(type_id) values ('BOOLEAN');
        insert into ${Tables.DATA_TYPES.tableName}(type_id) values ('FLOAT');
        insert into ${Tables.DATA_TYPES.tableName}(type_id) values ('CURRENCY');
        insert into ${Tables.DATA_TYPES.tableName}(type_id) values ('BCD');
        insert into ${Tables.DATA_TYPES.tableName}(type_id) values ('FMTBCD');
        insert into ${Tables.DATA_TYPES.tableName}(type_id) values ('DATE');
        insert into ${Tables.DATA_TYPES.tableName}(type_id) values ('TIME');
        insert into ${Tables.DATA_TYPES.tableName}(type_id) values ('DATETIME');
        insert into ${Tables.DATA_TYPES.tableName}(type_id) values ('TIMESTAMP');
        insert into ${Tables.DATA_TYPES.tableName}(type_id) values ('BYTES');
        insert into ${Tables.DATA_TYPES.tableName}(type_id) values ('VARBYTES');
        insert into ${Tables.DATA_TYPES.tableName}(type_id) values ('BLOB');
        insert into ${Tables.DATA_TYPES.tableName}(type_id) values ('MEMO');
        insert into ${Tables.DATA_TYPES.tableName}(type_id) values ('GRAPHIC');
        insert into ${Tables.DATA_TYPES.tableName}(type_id) values ('FMTMEMO');
        insert into ${Tables.DATA_TYPES.tableName}(type_id) values ('FIXEDCHAR');
        insert into ${Tables.DATA_TYPES.tableName}(type_id) values ('WIDESTRING');
        insert into ${Tables.DATA_TYPES.tableName}(type_id) values ('LARGEINT');
        insert into ${Tables.DATA_TYPES.tableName}(type_id) values ('COMP');
        insert into ${Tables.DATA_TYPES.tableName}(type_id) values ('ARRAY');
        insert into ${Tables.DATA_TYPES.tableName}(type_id) values ('FIXEDWIDECHAR');
        insert into ${Tables.DATA_TYPES.tableName}(type_id) values ('WIDEMEMO');
        insert into ${Tables.DATA_TYPES.tableName}(type_id) values ('CODE');
        insert into ${Tables.DATA_TYPES.tableName}(type_id) values ('RECORDID');
        insert into ${Tables.DATA_TYPES.tableName}(type_id) values ('SET');
        insert into ${Tables.DATA_TYPES.tableName}(type_id) values ('PERIOD');
        insert into ${Tables.DATA_TYPES.tableName}(type_id) values ('BYTE');
        insert into ${Tables.SETTINGS.tableName}(key, value) values ('dbd.version', '3.1');
    """.trimIndent()

    @JvmField val SQL_DBD_VIEWS_INIT = """
        create view ${Tables.VIEW_FIELDS.tableName} as
        select
          ${Tables.SCHEMAS.tableName}.name "schema",
          ${Tables.TABLES.tableName}.name "table",
          ${Tables.FIELDS.tableName}.position "position",
          ${Tables.FIELDS.tableName}.name "name",
          ${Tables.FIELDS.tableName}.russian_short_name "russian_short_name",
          ${Tables.FIELDS.tableName}.description "description",
          ${Tables.DATA_TYPES.tableName}.type_id "type_id",
          ${Tables.DOMAINS.tableName}.length "length",
          ${Tables.DOMAINS.tableName}.char_length,
          ${Tables.DOMAINS.tableName}.width "width",
          ${Tables.DOMAINS.tableName}.align "align",
          ${Tables.DOMAINS.tableName}.precision "precision",
          ${Tables.DOMAINS.tableName}.scale "scale",
          ${Tables.DOMAINS.tableName}.show_null "show_null",
          ${Tables.DOMAINS.tableName}.show_lead_nulls "show_lead_nulls",
          ${Tables.DOMAINS.tableName}.thousands_separator "thousands_separator",
          ${Tables.DOMAINS.tableName}.summable,
          ${Tables.DOMAINS.tableName}.case_sensitive "case_sensitive",
          ${Tables.FIELDS.tableName}.can_input "can_input",
          ${Tables.FIELDS.tableName}.can_edit "can_edit",
          ${Tables.FIELDS.tableName}.show_in_grid "show_in_grid",
          ${Tables.FIELDS.tableName}.show_in_details "show_in_details",
          ${Tables.FIELDS.tableName}.is_mean "is_mean",
          ${Tables.FIELDS.tableName}.autocalculated "autocalculated",
          ${Tables.FIELDS.tableName}.required "required"
        from ${Tables.FIELDS.tableName}
          inner join ${Tables.TABLES.tableName} on ${Tables.FIELDS.tableName}.table_id = ${Tables.TABLES.tableName}.id
          inner join ${Tables.DOMAINS.tableName} on ${Tables.FIELDS.tableName}.domain_id = ${Tables.DOMAINS.tableName}.id
          inner join ${Tables.DATA_TYPES.tableName} on ${Tables.DOMAINS.tableName}.data_type_id = ${Tables.DATA_TYPES.tableName}.id
          Left Join ${Tables.SCHEMAS.tableName} On ${Tables.TABLES.tableName}.schema_id = ${Tables.SCHEMAS.tableName}.id
        order by
          ${Tables.TABLES.tableName}.name,
          ${Tables.FIELDS.tableName}.position;
        create view ${Tables.VIEW_DOMAINS.tableName} as
        select
          ${Tables.DOMAINS.tableName}.id,
          ${Tables.DOMAINS.tableName}.name,
          ${Tables.DOMAINS.tableName}.description,
          ${Tables.DATA_TYPES.tableName}.type_id,
          ${Tables.DOMAINS.tableName}.length,
          ${Tables.DOMAINS.tableName}.char_length,
          ${Tables.DOMAINS.tableName}.width,
          ${Tables.DOMAINS.tableName}.align,
          ${Tables.DOMAINS.tableName}.summable,
          ${Tables.DOMAINS.tableName}.precision,
          ${Tables.DOMAINS.tableName}.scale,
          ${Tables.DOMAINS.tableName}.show_null,
          ${Tables.DOMAINS.tableName}.show_lead_nulls,
          ${Tables.DOMAINS.tableName}.thousands_separator,
          ${Tables.DOMAINS.tableName}.case_sensitive "case_sensitive"
        from ${Tables.DOMAINS.tableName}
          inner join ${Tables.DATA_TYPES.tableName} on ${Tables.DOMAINS.tableName}.data_type_id = ${Tables.DATA_TYPES.tableName}.id
        order by ${Tables.DOMAINS.tableName}.id;
        create view ${Tables.VIEW_CONSTRAINTS.tableName} as
        select
          ${Tables.CONSTRAINTS.tableName}.id "constraint_id",
          ${Tables.CONSTRAINTS.tableName}.constraint_type "constraint_type",
          ${Tables.CONSTRAINT_DETAILS.tableName}.position "position",
          ${Tables.SCHEMAS.tableName}.name "schema",
          ${Tables.TABLES.tableName}.name "table_name",
          ${Tables.FIELDS.tableName}.name "field_name",
          "references".name "reference"
        from
          ${Tables.CONSTRAINT_DETAILS.tableName}
          inner join ${Tables.CONSTRAINTS.tableName} on ${Tables.CONSTRAINT_DETAILS.tableName}.constraint_id = ${Tables.CONSTRAINTS.tableName}.id
          inner join ${Tables.TABLES.tableName} on ${Tables.CONSTRAINTS.tableName}.table_id = ${Tables.TABLES.tableName}.id
          left join ${Tables.TABLES.tableName} "references" on ${Tables.CONSTRAINTS.tableName}.reference = "references".id
          left join ${Tables.FIELDS.tableName} on ${Tables.CONSTRAINT_DETAILS.tableName}.field_id = ${Tables.FIELDS.tableName}.id
          Left Join ${Tables.SCHEMAS.tableName} On ${Tables.TABLES.tableName}.schema_id = ${Tables.SCHEMAS.tableName}.id
        order by
          constraint_id, position;
        create view ${Tables.VIEW_INDEXES.tableName} as
        select
          ${Tables.INDEXES.tableName}.id "index_id",
          ${Tables.INDEXES.tableName}.name as index_name,
          ${Tables.SCHEMAS.tableName}.name "schema",
          ${Tables.TABLES.tableName}.name as table_name,
          ${Tables.INDEXES.tableName}.local,
          ${Tables.INDEXES.tableName}.kind,
          ${Tables.INDEX_DETAILS.tableName}.position,
          ${Tables.FIELDS.tableName}.name as field_name,
          ${Tables.INDEX_DETAILS.tableName}.expression,
          ${Tables.INDEX_DETAILS.tableName}.descend
        from
          ${Tables.INDEX_DETAILS.tableName}
          inner join ${Tables.INDEXES.tableName} on ${Tables.INDEX_DETAILS.tableName}.index_id = ${Tables.INDEXES.tableName}.id
          inner join ${Tables.TABLES.tableName} on ${Tables.INDEXES.tableName}.table_id = ${Tables.TABLES.tableName}.id
          left join ${Tables.FIELDS.tableName} on ${Tables.INDEX_DETAILS.tableName}.field_id = ${Tables.FIELDS.tableName}.id
          Left Join ${Tables.SCHEMAS.tableName} On ${Tables.TABLES.tableName}.schema_id = ${Tables.SCHEMAS.tableName}.id
        order by
          ${Tables.TABLES.tableName}.name, ${Tables.INDEXES.tableName}.name, ${Tables.INDEX_DETAILS.tableName}.position;
    """.trimIndent()

    @JvmField val COMMIT = "commit;"

    @JvmField val SQL_DBD_INIT = SQL_DBD_PRE_INIT + SQL_DBD_DOMAINS_TABLE_INIT +
    SQL_DBD_TABLES_TABLE_INIT + SQL_DBD_TABLES_INIT +
    SQL_DBD_VIEWS_INIT + COMMIT
}