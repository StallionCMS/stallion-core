db.execute('''
CREATE TABLE IF NOT EXISTS `stallion_audit_trail` (
`id` bigint(20) unsigned NOT NULL,
    `table`  varchar(255)  NULL ,
    `createdat`  datetime  NULL ,
    `orgid`  bigint(20)  NULL ,
    `objectid`  bigint(20)  NULL ,
    `useragent`  varchar(255)  NULL ,
    `valetemail`  varchar(255)  NULL ,
    `userid`  bigint(20)  NULL ,
    `objectdata`  longtext  NULL ,
    `useremail`  varchar(255)  NULL ,
    `remoteip`  varchar(50)  NULL ,
    `valetid`  bigint(20)  NULL ,
    `keeplongterm`  bit(1)  NULL ,
    `deleted`  bit(1)  NULL ,
  `row_updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `row_updated_at_key` (`row_updated_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci; 
''');