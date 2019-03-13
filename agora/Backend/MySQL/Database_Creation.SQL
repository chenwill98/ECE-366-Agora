-- MySQL Workbench Forward Engineering

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

-- -----------------------------------------------------
-- Schema mydb
-- -----------------------------------------------------

-- -----------------------------------------------------
-- Schema mydb
-- -----------------------------------------------------
CREATE SCHEMA IF NOT EXISTS `mydb` DEFAULT CHARACTER SET utf8 ;
USE `mydb` ;

-- -----------------------------------------------------
-- Table `mydb`.`users`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `mydb`.`users` (
  `uid` INT NOT NULL AUTO_INCREMENT,
  `email` VARCHAR(45) NULL,
  `firstname` VARCHAR(45) NOT NULL,
  `lastname` VARCHAR(45) NOT NULL,
  `passhash` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`uid`))
ENGINE = InnoDB;

CREATE UNIQUE INDEX `uid_UNIQUE` ON `mydb`.`users` (`uid` ASC) VISIBLE;

CREATE INDEX `email` ON `mydb`.`users` (`email` ASC) VISIBLE;


-- -----------------------------------------------------
-- Table `mydb`.`groups`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `mydb`.`groups` (
  `gid` INT NOT NULL AUTO_INCREMENT,
  `Description` VARCHAR(45) NOT NULL,
  `Name` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`gid`))
ENGINE = InnoDB;

CREATE UNIQUE INDEX `gid_UNIQUE` ON `mydb`.`groups` (`gid` ASC) VISIBLE;


-- -----------------------------------------------------
-- Table `mydb`.`events`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `mydb`.`events` (
  `eventid` INT NOT NULL AUTO_INCREMENT,
  `event_name` VARCHAR(45) NULL,
  `desc` VARCHAR(45) NULL,
  `groups_gid` INT NULL,
  `Location` VARCHAR(45) NULL,
  `Date_Time` DATETIME NULL,
  PRIMARY KEY (`eventid`),
  CONSTRAINT `fk_events_groups`
    FOREIGN KEY (`groups_gid`)
    REFERENCES `mydb`.`groups` (`gid`)
    ON DELETE SET NULL
    ON UPDATE RESTRICT)
ENGINE = InnoDB;

CREATE UNIQUE INDEX `Event ID_UNIQUE` ON `mydb`.`events` (`eventid` ASC) VISIBLE;

CREATE FULLTEXT INDEX `descsearch` ON `mydb`.`events` (`desc`) INVISIBLE;

CREATE INDEX `fk_events_groups_idx` ON `mydb`.`events` (`groups_gid` ASC) VISIBLE;


-- -----------------------------------------------------
-- Table `mydb`.`group_memberships`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `mydb`.`group_memberships` (
  `users_uid` INT NOT NULL,
  `groups_gid` INT NOT NULL,
  `is_admin` TINYINT NULL,
  PRIMARY KEY (`groups_gid`, `users_uid`),
  CONSTRAINT `fk_users_has_groups_users1`
    FOREIGN KEY (`users_uid`)
    REFERENCES `mydb`.`users` (`uid`)
    ON DELETE CASCADE
    ON UPDATE RESTRICT,
  CONSTRAINT `fk_users_has_groups_groups1`
    FOREIGN KEY (`groups_gid`)
    REFERENCES `mydb`.`groups` (`gid`)
    ON DELETE CASCADE
    ON UPDATE RESTRICT)
ENGINE = InnoDB;

CREATE INDEX `fk_users_has_groups_groups1_idx` ON `mydb`.`group_memberships` (`groups_gid` ASC) VISIBLE;

CREATE INDEX `fk_users_has_groups_users1_idx` ON `mydb`.`group_memberships` (`users_uid` ASC) VISIBLE;


-- -----------------------------------------------------
-- Table `mydb`.`event_attendance`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `mydb`.`event_attendance` (
  `users_uid` INT NOT NULL,
  `events_eventid` INT NOT NULL,
  `is_attending` ENUM("YES", "MAYBE", "NO") NULL,
  PRIMARY KEY (`users_uid`, `events_eventid`),
  CONSTRAINT `fk_users_has_events_users1`
    FOREIGN KEY (`users_uid`)
    REFERENCES `mydb`.`users` (`uid`)
    ON DELETE CASCADE
    ON UPDATE RESTRICT,
  CONSTRAINT `fk_users_has_events_events1`
    FOREIGN KEY (`events_eventid`)
    REFERENCES `mydb`.`events` (`eventid`)
    ON DELETE CASCADE
    ON UPDATE RESTRICT)
ENGINE = InnoDB;

CREATE INDEX `fk_users_has_events_events1_idx` ON `mydb`.`event_attendance` (`events_eventid` ASC) VISIBLE;

CREATE INDEX `fk_users_has_events_users1_idx` ON `mydb`.`event_attendance` (`users_uid` ASC) VISIBLE;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
