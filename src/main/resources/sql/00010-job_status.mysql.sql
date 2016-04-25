
CREATE TABLE IF NOT EXISTS `stallion_job_status` (
  `id` bigint unsigned NOT NULL,
  `deleted` bit default 0,
  `name` varchar(200) NOT NULL DEFAULT '',
  `startedAt` bigint default 0,
  `completedAt` bigint default 0,
  `failedAt` bigint default 0,
  `error` longtext,
  `shouldSucceedBy` bigint default 0,
  `lastDurationSeconds` bigint default 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY name_key (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
