


CREATE TABLE `stallion_test_house` (
  `address` varchar(200) DEFAULT NULL,
  `postalCode` varchar(30) DEFAULT NULL,
  `squareFeet` int(11) DEFAULT NULL,
  `vacant` tinyint(4) DEFAULT NULL,
  `condemned` tinyint(4) DEFAULT NULL,
  `taxesPaid` tinyint(4) DEFAULT NULL,
  `buildYear` int(11) DEFAULT NULL,
  `deleted` bit DEFAULT 0,
  `row_updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `row_udpated_at_index` (`row_updated_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `stallion_test_payment` (
  `id` bigint NOT NULL,
  `date` bigint(20) NOT NULL DEFAULT '0',
  `accountId` varchar(50) DEFAULT NULL,
  `amount` int(11) DEFAULT NULL,
  `onTime` tinyint(4) DEFAULT NULL,
  `payee` varchar(250) DEFAULT NULL,
  `deleted` bit DEFAULT 0,
  `memo` varchar(250) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `stallion_test_picnic` (
  `id` bigint NOT NULL,
  `date` datetime DEFAULT NULL,
  `replyBy` date DEFAULT NULL,
  `deleted` bit DEFAULT 0,
  `canceled` bit DEFAULT 0,
  `location` VARCHAR(100) NOT NULL DEFAULT '',
  `description` VARCHAR(100) NOT NULL DEFAULT '',
  `attendees` longtext,
  `adminIds` longtext,
  `dishes` longtext,
  `extra` longtext,
  `type` varchar(30) DEFAULT NULl,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;