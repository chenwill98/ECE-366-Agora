-- MySQL Workbench Forward Engineering

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

-- -----------------------------------------------------
-- Schema AgoraDB
-- -----------------------------------------------------

-- -----------------------------------------------------
-- Schema AgoraDB
-- -----------------------------------------------------
CREATE SCHEMA IF NOT EXISTS `AgoraDB` DEFAULT CHARACTER SET utf8 ;
USE `AgoraDB` ;

-- -----------------------------------------------------
-- Table `AgoraDB`.`Users`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `AgoraDB`.`Users` (
  `User_id` INT NOT NULL AUTO_INCREMENT,
  `Email` VARCHAR(45) NULL,
  `First_name` VARCHAR(45) NOT NULL,
  `Last_name` VARCHAR(45) NOT NULL,
  `Passhash` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`User_id`),
  UNIQUE INDEX `User_id_UNIQUE` (`User_id` ASC) VISIBLE,
  INDEX `Email` (`Email` ASC) VISIBLE)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `AgoraDB`.`Groops`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `AgoraDB`.`Groops` (
  `Groop_id` INT NOT NULL AUTO_INCREMENT,
  `Description` BLOB NOT NULL,
  `Name` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`Groop_id`),
  UNIQUE INDEX `Group_id_UNIQUE` (`Groop_id` ASC) VISIBLE)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `AgoraDB`.`Events`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `AgoraDB`.`Events` (
  `Event_id` INT NOT NULL AUTO_INCREMENT,
  `Event_name` VARCHAR(45) NULL,
  `Description` BLOB NULL,
  `Groop_id` INT NULL,
  `Location` VARCHAR(45) NULL,
  `Date_time` DATETIME NULL,
  PRIMARY KEY (`Event_id`),
  UNIQUE INDEX `Event ID_UNIQUE` (`Event_id` ASC) VISIBLE,
  INDEX `Fk_events_groups_idx` (`Groop_id` ASC) VISIBLE,
  CONSTRAINT `fk_events_groups`
    FOREIGN KEY (`Groop_id`)
    REFERENCES `AgoraDB`.`Groops` (`Groop_id`)
    ON DELETE SET NULL
    ON UPDATE RESTRICT)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `AgoraDB`.`Event_attendance`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `AgoraDB`.`Event_attendance` (
  `User_id` INT NOT NULL,
  `Event_id` INT NOT NULL,
  `Is_attending` ENUM("YES", "MAYBE", "NO") NULL,
  PRIMARY KEY (`User_id`, `Event_id`),
  INDEX `fk_users_has_events_events1_idx` (`Event_id` ASC) VISIBLE,
  INDEX `fk_users_has_events_users1_idx` (`User_id` ASC) VISIBLE,
  CONSTRAINT `fk_users_has_events_users1`
    FOREIGN KEY (`User_id`)
    REFERENCES `AgoraDB`.`Users` (`User_id`)
    ON DELETE CASCADE
    ON UPDATE RESTRICT,
  CONSTRAINT `fk_users_has_events_events1`
    FOREIGN KEY (`Event_id`)
    REFERENCES `AgoraDB`.`Events` (`Event_id`)
    ON DELETE CASCADE
    ON UPDATE RESTRICT)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `AgoraDB`.`Groop_memberships`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `AgoraDB`.`Groop_memberships` (
  `User_id` INT NOT NULL,
  `Groop_id` INT NOT NULL,
  `Is_admin` TINYINT(1) NULL,
  PRIMARY KEY (`User_id`, `Groop_id`),
  INDEX `fk_Users_has_Groops_Groops1_idx` (`Groop_id` ASC) VISIBLE,
  INDEX `fk_Users_has_Groops_Users1_idx` (`User_id` ASC) VISIBLE,
  CONSTRAINT `fk_Users_has_Groops_Users1`
    FOREIGN KEY (`User_id`)
    REFERENCES `AgoraDB`.`Users` (`User_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_Users_has_Groops_Groops1`
    FOREIGN KEY (`Groop_id`)
    REFERENCES `AgoraDB`.`Groops` (`Groop_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
