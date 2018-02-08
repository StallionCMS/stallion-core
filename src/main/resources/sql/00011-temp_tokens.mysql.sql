
CREATE TABLE IF NOT EXISTS `stallion_temp_tokens` (
  `id` bigint unsigned NOT NULL,
    customKey varchar(100) NOT NULL,
    expiresAt datetime,
    token varchar(75) NOT NULL,
    userType varchar(75) NOT NULL,
    targetKey varchar(75) NOT NULL,
    createdAt datetime,
    usedAt datetime,
    `deleted` bit(1) NOT NULL,
    `row_updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    data longtext,
  PRIMARY KEY (`id`),
  UNIQUE KEY custom_key_key (`customKey`),
  UNIQUE KEY token_key (`token`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
