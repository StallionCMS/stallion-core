CREATE TABLE IF NOT EXISTS `stallion_sql_migrations` (
  `versionNumber` int(11) unsigned NOT NULL,
  `appName` varchar(50)  NOT NULL DEFAULT '',
  `fileName` varchar(70) NOT NULL DEFAULT '0',
  `executedAt` datetime NOT NULL,
  PRIMARY KEY (`appName`, `versionNumber`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;