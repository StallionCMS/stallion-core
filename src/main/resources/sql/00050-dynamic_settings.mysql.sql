CREATE TABLE IF NOT EXISTS `stallion_dynamic_settings` (
    `group`  varchar(40)  NOT NULL ,
    `name`  varchar(40)  NOT NULL ,
    `value` longtext,
    `row_updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
     KEY `row_updated_at_key` (`row_updated_at`),
     PRIMARY KEY `group_name_pk` (`group`, `name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
