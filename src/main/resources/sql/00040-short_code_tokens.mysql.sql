CREATE TABLE IF NOT EXISTS `stallion_short_code_tokens` (
`id` bigint(20) unsigned NOT NULL,
    `key`  varchar(255)  NULL ,
    `code`  varchar(255)  NULL ,
    `createdat`  datetime  NULL ,
    `usedat`  datetime  NULL ,
    `actionkey`  varchar(255)  NULL ,
    `expiresat`  datetime  NULL ,
    `deleted`  bit(1)  NULL ,
  `row_updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `row_updated_at_key` (`row_updated_at`),
  UNIQUE KEY `key_key` (`key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
