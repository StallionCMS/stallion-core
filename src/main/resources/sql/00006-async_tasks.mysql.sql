
CREATE TABLE IF NOT EXISTS `stallion_async_tasks` (
  `id` bigint unsigned NOT NULL,
  `deleted` bit default 0,
  `lockUuid` varchar(50) NOT NULL DEFAULT '',
  `secret` varchar(50) NOT NULL DEFAULT '',
  `customKey` varchar(175) DEFAULT NULL,
  `handlerName` varchar(175) NOT NULL DEFAULT '',
  `updatedAt` bigint default 0,
  `createdAt` bigint default 0,
  `completedAt` bigint default 0,
  `failedAt` bigint default 0,
  `lockedAt` bigint default 0,
  `originallyScheduledFor` bigint default 0,
  `executeAt` bigint default 0,
  `neverRetry` bit default 0,
  `tryCount` int default 0,
  `errorMessage` longtext,
  `dataJson` longtext,
  `localMode` varchar(50) NOT NULL DEFAULT '',
  PRIMARY KEY (`id`),
  UNIQUE KEY custom_key_key (`customKey`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
