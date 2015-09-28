-- CREATE DATABASE slotmachine;

DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `username` varchar(128) NOT NULL,
  `password` varchar(128) DEFAULT NULL,
  `displayName` varchar(128) DEFAULT '',
  `email` varchar(256) DEFAULT '',
  `avatar` varchar(256) DEFAULT '',
  `cash` BIGINT DEFAULT 0,
  `gem` int(11) DEFAULT 0,
  `createdAt` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `lastLogin` timestamp DEFAULT CURRENT_TIMESTAMP,
  `facebookId` varchar(64) DEFAULT '',
  `bossKill` int(11) DEFAULT 0,
  `totalWin` BIGINT DEFAULT 0,
  `biggestWin` BIGINT DEFAULT 0,
  `lastClaimedDaily` BIGINT DEFAULT 0,
  `inboxMes` LONGTEXT NOT NULL,								-- array of user inbox messages
  `lastInboxTime` BIGINT DEFAULT 0, 					-- used to track unread message
  `lastReadInboxTime` BIGINT DEFAULT 0, 			-- used to track unread message
  `lastAdminMesTime` BIGINT DEFAULT 0,				-- used to track which admin message should be add to user inbox
  PRIMARY KEY (`username`),
  KEY `facebook_id_index` (`facebookId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf16;

-- used to add new row to table 'user'
ALTER TABLE user
ADD COLUMN `lastInboxTime` BIGINT DEFAULT 0,
-- ADD COLUMN `lastReadInboxTime` BIGINT DEFAULT 0,
-- ADD COLUMN `lastAdminMesTime` BIGINT DEFAULT 0,
-- ADD COLUMN `adminMesIds` TEXT;

-- This table will store admin message send to all offline users, when user go online, they will copy these messages to their inbox table
DROP TABLE IF EXISTS `adminMessage`;
CREATE TABLE `adminMessage` (
  `id` MEDIUMINT NOT NULL AUTO_INCREMENT,
  `message` TEXT NOT NULL,
  `createdAt` BIGINT DEFAULT 0,
  `expiredAt` BIGINT DEFAULT 0,												-- used to remove when message expired
  `targetType` TINYINT DEFAULT 0,											-- used to choose which users can get this message
  `usernames` TEXT,																		-- used to choose targets are some specific users
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf16;

DROP TABLE IF EXISTS `userIAP`;
CREATE TABLE `userIAP` (
  `username` varchar(128) NOT NULL,
  `purchase` varchar(512) DEFAULT NULL,
  `platform` varchar(32) DEFAULT NULL,
  `createdAt` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `purchaseMd5Checksum` varchar(64) DEFAULT NULL,
  `purchaseValue` int(11) DEFAULT '0',
  KEY `username_index` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf16;

DROP TABLE IF EXISTS `jackpots`;
CREATE TABLE `jackpots` (
  `id` MEDIUMINT NOT NULL AUTO_INCREMENT,
  `gType` varchar(128) NOT NULL,
  `val` BIGINT DEFAULT 0,
  `createdAt` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `claimedAt` timestamp,
  `username` varchar(128) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf16;