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
-- Table `mydb`.`Users`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `mydb`.`Users` (
  `User_id` INT NOT NULL AUTO_INCREMENT,
  `Email` VARCHAR(45) NULL,
  `First_name` VARCHAR(45) NOT NULL,
  `Last_name` VARCHAR(45) NOT NULL,
  `Passhash` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`User_id`),
  UNIQUE INDEX `User_id_UNIQUE` (`User_id` ASC) VISIBLE,
  INDEX `email` (`Email` ASC) VISIBLE)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `mydb`.`Groops`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `mydb`.`Groops` (
  `Groop_id` INT NOT NULL AUTO_INCREMENT,
  `Description` VARCHAR(45) NOT NULL,
  `Name` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`Groop_id`),
  UNIQUE INDEX `Group_id_UNIQUE` (`Groop_id` ASC) VISIBLE)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `mydb`.`Events`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `mydb`.`Events` (
  `Event_id` INT NOT NULL AUTO_INCREMENT,
  `Event_name` VARCHAR(45) NULL,
  `Descrip` VARCHAR(45) NULL,
  `Group_gid` INT NULL,
  `Location` VARCHAR(45) NULL,
  `Date_time` DATETIME NULL,
  PRIMARY KEY (`Event_id`),
  UNIQUE INDEX `Event ID_UNIQUE` (`Event_id` ASC) VISIBLE,
  FULLTEXT INDEX `Descripsearch` (`Descrip`) INVISIBLE,
  INDEX `Fk_events_groups_idx` (`Group_gid` ASC) VISIBLE,
  CONSTRAINT `fk_events_groups`
    FOREIGN KEY (`Group_gid`)
    REFERENCES `mydb`.`Groops` (`Groop_id`)
    ON DELETE SET NULL
    ON UPDATE RESTRICT)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `mydb`.`Event_attendance`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `mydb`.`Event_attendance` (
  `User_id` INT NOT NULL,
  `Event_id` INT NOT NULL,
  `Is_attending` ENUM("YES", "MAYBE", "NO") NULL,
  PRIMARY KEY (`User_id`, `Event_id`),
  INDEX `fk_users_has_events_events1_idx` (`Event_id` ASC) VISIBLE,
  INDEX `fk_users_has_events_users1_idx` (`User_id` ASC) VISIBLE,
  CONSTRAINT `fk_users_has_events_users1`
    FOREIGN KEY (`User_id`)
    REFERENCES `mydb`.`Users` (`User_id`)
    ON DELETE CASCADE
    ON UPDATE RESTRICT,
  CONSTRAINT `fk_users_has_events_events1`
    FOREIGN KEY (`Event_id`)
    REFERENCES `mydb`.`Events` (`Event_id`)
    ON DELETE CASCADE
    ON UPDATE RESTRICT)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `mydb`.`Groop_memberships`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `mydb`.`Groop_memberships` (
  `User_id` INT NOT NULL,
  `Groops_id` INT NOT NULL,
  `Is_admin` TINYINT(1) NULL,
  PRIMARY KEY (`User_id`, `Groops_id`),
  INDEX `fk_Users_has_Groops_Groops1_idx` (`Groops_id` ASC) VISIBLE,
  INDEX `fk_Users_has_Groops_Users1_idx` (`User_id` ASC) VISIBLE,
  CONSTRAINT `fk_Users_has_Groops_Users1`
    FOREIGN KEY (`User_id`)
    REFERENCES `mydb`.`Users` (`User_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_Users_has_Groops_Groops1`
    FOREIGN KEY (`Groops_id`)
    REFERENCES `mydb`.`Groops` (`Groop_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
