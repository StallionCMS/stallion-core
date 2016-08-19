db.execute('''
CREATE TABLE IF NOT EXISTS `stallion_transaction_log` (
`id` bigint(20) unsigned NOT NULL,
    `type`  varchar(65)  NULL ,
    `body`  longtext  NULL ,
    `createdat`  datetime  NULL ,
    `orgid`  bigint(20)  NULL ,
    `subject`  varchar(255)  NULL ,
    `userid`  bigint(20)  NULL ,
    `customkey`  varchar(255)  NULL ,
    `toaddress`  varchar(255)  NULL ,
    `extra`  longtext  NULL ,
    `deleted`  bit(1)  NULL ,
  `row_updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `row_updated_at_key` (`row_updated_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci; 
''');
