CREATE TABLE `TbUsers` (
    `id` BIGINT AUTO_INCREMENT NOT NULL,
    `name` VARCHAR(255) NOT NULL,
    `hash` VARCHAR(255) NULL,
    `openid` VARCHAR(255) NULL,
    `loginType` VARCHAR(255) NULL,
    `email` VARCHAR(255) NULL,
    `firstNname` VARCHAR(255) NULL,
    `lastNamme` VARCHAR(255) NULL,
    `dateCreated` timestamp NOT NULL,
    `lastUpdated` timestamp NOT NULL,
    CONSTRAINT `pkUsers` PRIMARY KEY (`id`)
) AUTO_INCREMENT=10000;

ALTER TABLE `TbUsers` ADD CONSTRAINT `uniqueUsersName` UNIQUE (`name`);
