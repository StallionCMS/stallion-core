CREATE TABLE IF NOT EXISTS `stallion_users` (
  `id` bigint unsigned NOT NULL,
  `displayName` varchar(200) NOT NULL DEFAULT '',
  `givenName` varchar(200) NOT NULL DEFAULT '',
  `familyName` varchar(200) NOT NULL DEFAULT '',
  `email` varchar(100) NOT NULL DEFAULT '',
  `username` varchar(100) NOT NULL DEFAULT '',
  `role` varchar(20) NOT NULL DEFAULT '',
  `orgId` bigint(20) NOT NULL,
  `createdAt` bigint(20) NOT NULL DEFAULT '0',
  `secret` varchar(50) NOT NULL DEFAULT '',
  `encryptionSecret` varchar(50) NOT NULL DEFAULT '',
  `timeZoneId` varchar(14) NOT NULL DEFAULT '',
  `bcryptedPassword` varchar(125) NULL,
  `filePath` varchar(200) NOT NULL DEFAULT '',
  `predefined` tinyint(1) NOT NULL DEFAULT '0',
  `row_updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `emailVerified` tinyint(1) NOT NULL DEFAULT '0',
  `approved` bit(1) NOT NULL DEFAULT b'1',
  `deleted` tinyint(1) NOT NULL DEFAULT '0',
  `resetToken` varchar(40) NOT NULL DEFAULT '',
  `aliasForId` bigint(20) NOT NULL DEFAULT '0',
  `honorific` varchar(30) NOT NULL DEFAULT '',
  `totallyOptedOut` bit(1)  NOT NULL DEFAULT 0,
  `optedOut` bit(1) NOT NULL DEFAULT 0,
  `disabled` bit(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `email` (`email`),
  UNIQUE KEY `username_key` (`username`),
  KEY `row_updated_at_key` (`row_updated_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
