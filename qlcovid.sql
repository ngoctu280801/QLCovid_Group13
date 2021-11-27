-- phpMyAdmin SQL Dump
-- version 4.2.11
-- http://www.phpmyadmin.net
--
-- Host: 127.0.0.1
-- Generation Time: Nov 21, 2021 at 08:28 AM
-- Server version: 5.6.21
-- PHP Version: 5.6.3

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

CREATE DATABASE `qlcovid` CHARACTER SET utf8 COLLATE utf8_general_ci;
USE `qlcovid`;
--
-- Database: `qlcovid`
--

DELIMITER $$
--
-- Procedures
--
CREATE DEFINER=`root`@`localhost` PROCEDURE `addPatient`(IN `usrNameIn` VARCHAR(20), IN `fName` VARCHAR(50), IN `DOB` DATE, IN `idCard` VARCHAR(12), IN `qrtPos` VARCHAR(50), IN `stateF` VARCHAR(2), IN `prov` VARCHAR(50), IN `townN` VARCHAR(50), IN `vlg` VARCHAR(50), IN `usrManager` VARCHAR(20), OUT `code` INT)
BEGIN
	declare id_acc int;
    declare id_prov int; 
    declare id_town int;
    declare id_vlg int;
    declare id_pos int;
    declare id_manager int;
    declare date_now date;
    select -1 into code;
    set date_now = now();
    set id_manager = (select id from accounts where usrname = usrManager and id_permission = 0 limit 1);
    
    set id_acc = (select id from accounts where usrname = usrNameIn limit 1);
    set id_pos = (select id from quarantinepos where name = qrtPos limit 1);
    set id_prov = (select id from provinces where name = prov limit 1);
    set id_town = (select t.id from towns t join provinces_towns pt on pt.id_town = t.id where t.name = townN and pt.id_prov = id_prov limit 1);
    set id_vlg = (select v.id from villages v join towns_villages tv on tv.id_vlg = v.id where v.name = vlg and tv.id_town = id_town limit 1);
    
    
	insert into patients(id, full_name, id_card, date_of_birth, id_prov, id_town, id_vlg, state, id_pos) 
 	values (id_acc, fName, idCard, DOB, id_prov, id_town, id_vlg, stateF, id_pos);
    
    insert into activity_history(usr_manager, date, id_card_patient, description)
    values (usrManager, date_now, idCard, concat(N'Thêm ', idCard, N' làm ', stateF, N' tại ', qrtPos));
    select 1 into code;
     
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `addPkg`(IN `pkgName` VARCHAR(50), IN `limitPerPerson` VARCHAR(20), IN `dateLimitIn` DATE, IN `priceIn` VARCHAR(20), IN `usrManager` VARCHAR(20), OUT `code` INT)
    NO SQL
begin
    declare date_now date;
    declare priceDecimal decimal;
    declare limit_person int;
    select -1 into code;
    set limit_person = cast(replace(limitPerPerson, ",", "") as unsigned);
    set priceDecimal = cast(replace(priceIn, ",", "") as decimal);
    set date_now = now();
            
    insert into necessary_packages(pkg_name, limit_quantity_per_person, date_limit, price) 
    values (pkgName, limit_person, dateLimitIn, priceDecimal);
    
    insert into activity_history(usr_manager, date, id_card_patient, description) 
    values (usrManager, date_now, null, concat(N'Thêm gói ', pkgName, ', hạn mức ', limitPerPerson, N' gói/ người, bán đến hết ngày ', dateLimitIn, N' với giá ', priceIn, N' VNĐ'));
    
    select 1 into code;
end$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `addRelatedPerson`(IN `idCard` VARCHAR(12), IN `listRPer` MEDIUMTEXT, OUT `code` INT)
BEGIN

DECLARE _next varchar(20) DEFAULT NULL;
DECLARE _nextlen INT DEFAULT NULL;
DECLARE _value varchar(20) DEFAULT NULL;
DECLARE id_acc int;
select 0 into code;
set id_acc = (select p.id from patients p where p.id_card = idCard);

iterator:
LOOP

  IF CHAR_LENGTH(TRIM(listRPer)) = 0 OR listRPer IS NULL THEN
    LEAVE iterator;
  END IF;
 
  SET _next = SUBSTRING_INDEX(listRPer, ';', 1);

  SET _nextlen = CHAR_LENGTH(_next);

  SET _value = TRIM(_next);

  INSERT INTO related_persons (id_patient, id_related) VALUES (id_acc, _value);

  select code + 1 into code;

  SET listRPer = INSERT(listRPer, 1 , _nextlen + 1,'');
END LOOP;



END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `changeQrtPos`(IN `userManager` VARCHAR(20), IN `id_card_patient` VARCHAR(12), IN `currQrtPos` VARCHAR(50), IN `newQrtPosId` INT(10), IN `newQrtPos` VARCHAR(50), OUT `code` INT)
    NO SQL
begin
        declare date_now date;
        declare priceDecimal decimal;
        declare limit_person int;
        select -1 into code;
        set date_now = now();
                
        update patients p 
        set p.id_pos = newQrtPosId 
        where p.id_card = id_card_patient;
        
        insert into activity_history(usr_manager, date, id_card_patient, description) 
        values (userManager, date_now, id_card_patient, concat(N'Chuyển nơi điều trị của ', id_card_patient, N' từ ', currQrtPos, N', đến ', newQrtPos));
		select 1 into code;

end$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `countDebt`(OUT `idCardList` TEXT, OUT `debtList` TEXT)
    NO SQL
begin
declare idCard varchar(12);
declare debt_temp decimal;
declare id_temp int;
DECLARE done INT DEFAULT FALSE;
declare cur_id cursor for 
	select id_patient from debt where debt != 0;
DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

select '' into idCardList;
select '' into debtList;

open cur_id;
iters: LOOP
	fetch cur_id into id_temp;
	IF done THEN
    	LEAVE iters;
	end if;
	set debt_temp = (select debt from debt where id_patient = id_temp limit 1);
    set idCard = (select p.id_card from accounts a join patients p on p.id = a.id where a.id = id_temp limit 1);
    
    select concat(idCardList, idCard, ';') into idCardList;
    select concat(debtList, debt_temp, ';') into debtList;

END LOOP;
close cur_id;

select LEFT(idCardList, LENGTH(idCardList) - 1) into idCardList;
select LEFT(debtList, LENGTH(debtList) - 1) into debtList;

end$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `countPatientLastNDay`(IN `nDays` INT, OUT `f0State` TEXT, OUT `f1State` TEXT, OUT `f2State` TEXT, OUT `f3State` TEXT, OUT `days` TEXT)
    NO SQL
begin
	declare date_now date;
    declare temp_date date;
    declare i int;
    declare F0 int;
    declare F1 int;
    declare F2 int;
    declare F3 int;
    
    set date_now = now();
    set i = nDays;
    
    select '' into f0State;
    select '' into f1State;
    select '' into f2State;
    select '' into f3State;
    select '' into days;
    
    iters: LOOP
    	if i <= 0 then
        	leave iters;
        end if;
        
        set temp_date = date_now - interval i day;
        
        set F0 = (select count(*) from activity_history where description like N'Thêm%F0%' and date = temp_date);
        set F1 = (select count(*) from activity_history where description like N'Thêm%F1%' and date = temp_date);
        set F2 = (select count(*) from activity_history where description like N'Thêm%F2%' and date = temp_date);
        set F3 = (select count(*) from activity_history where description like N'Thêm%F3%' and date = temp_date);
      	        
        select concat(f0State, F0, ';') into f0State;
        select concat(f1State, F1, ';') into f1State;
        select concat(f2State, F2, ';') into f2State;
        select concat(f3State, F3, ';') into f3State;
        select concat(days, (select date_format(temp_date, '%d/%m')), ';') into days;
        
        set i = i - 1;
    
    
    END LOOP;
    
	select LEFT(f0State, LENGTH(f0State) - 1) into f0State;
    select LEFT(f1State, LENGTH(f1State) - 1) into f1State;
    select LEFT(f2State, LENGTH(f2State) - 1) into f2State;
    select LEFT(f3State, LENGTH(f3State) - 1) into f3State;
    select LEFT(days, LENGTH(days) - 1) into days;

end$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `countPkgConsumed`(OUT `pkgNList` TEXT, OUT `quanList` TEXT)
    NO SQL
begin

DECLARE done INT DEFAULT FALSE;
declare pkgN varchar(100);
declare quan int;
declare cur_pkgN cursor for 
	select pkg_name from necessary_packages where is_deleted = 0;
DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
select '' into pkgNList;
select '' into quanList;

open cur_pkgN;    
iters: loop
	fetch cur_pkgN into pkgN;
	
    IF done THEN
    	LEAVE iters;
    end if;
    
    set quan = (select sum(bph.quantity) from bought_pkg_history bph 
                join necessary_packages p on p.id = bph.id_pkg 
                where p.pkg_name = pkgN and p.is_deleted = 0);
    if quan is null then
    	set quan = 0;
    end if;
    
    if quan != 0 then
    	select concat(pkgNList, pkgN, ';') into pkgNList;
    	select concat(quanList, quan, ';') into quanList;
    end if;
    
end loop;
close cur_pkgN;

select LEFT(pkgNList, LENGTH(pkgNList) - 1) into pkgNList;
select LEFT(quanList, LENGTH(quanList) - 1) into quanList;

end$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `delPkg`(IN `pkgNList` TEXT, IN `usrManager` VARCHAR(20), OUT `code` INT)
    NO SQL
begin
    declare date_now date;
    DECLARE _next varchar(50) DEFAULT NULL;
	DECLARE _nextlen INT DEFAULT NULL;
	DECLARE _value varchar(50) DEFAULT NULL;
    set date_now = now();
	select 0 into code;
iterator:
LOOP

  IF CHAR_LENGTH(TRIM(pkgNList)) = 0 OR pkgNList IS NULL THEN
    LEAVE iterator;
  END IF;
 
  SET _next = SUBSTRING_INDEX(pkgNList, ';', 1);

  SET _nextlen = CHAR_LENGTH(_next);

  SET _value = TRIM(_next);
  
  -- delete from necessary_packages where pkg_name = _value;
  update necessary_packages set is_deleted = 1 where pkg_name = _value;
  insert into activity_history(usr_manager, date, id_card_patient, description) 
    values (usrManager, date_now, null, concat(N'Xoá gói ', _value));

  select code + 1 into code;
  SET pkgNList = INSERT(pkgNList, 1 , _nextlen + 1,'');
END LOOP;
    
    
end$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `doTransaction`(IN `usrNameIn` VARCHAR(20), IN `creditIn` DECIMAL, OUT `code` INT)
    NO SQL
begin
	declare from_id int;
    declare to_id int;
    declare debt_remain decimal;
    declare balance_remain decimal;
	select -1 into code;
	set from_id = (select id from accounts where usrname = usrNameIn);
    set to_id = (select id from accounts where id_permission = 1 limit 1);
    set debt_remain = (select debt from debt d where d.id_patient = from_id limit 1);
    set debt_remain = debt_remain - creditIn;
    set balance_remain = (select balance from payment_acc where id_acc = from_id limit 1);
    set balance_remain = balance_remain - creditIn;
    insert into transaction_history(from_id_acc, to_id_acc, credit, remaining_debt, remaining_balance) 
    values (from_id, to_id, creditIn, debt_remain, balance_remain);

	select 1 into code;
end$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `getAllPatients`()
BEGIN
	SELECT p.id, p.full_name, p.id_card, p.date_of_birth, prov.name, t.name, v.name, qrt.name, p.state 
 	FROM patients p join provinces prov on prov.id = p.id_prov 
    	join towns t on p.id_town = t.id 
        join villages v on v.id = p.id_vlg 
        join quarantinepos qrt on qrt.id = p.id_pos;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `getNextRelatedPersonByIdCard`(IN `idCard` VARCHAR(12))
BEGIN
	SELECT p.full_name, p.id_card, p.date_of_birth, prov.name, t.name, v.name, p.state, qrt.name 
 	FROM related_persons rp join patients p on p.id = rp.id_related 
    	join provinces prov on p.id_prov = prov.id 
        join towns t on t.id = p.id_town 
        join villages v on v.id = p.id_vlg 
        join quarantinepos qrt on qrt.id = p.id_pos 
	WHERE rp.id_patient = (select id from patients where id_card = idCard limit 1);
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `getRelatedPersonBeforeByIdCard`(IN `idCard` VARCHAR(12))
    NO SQL
BEGIN
	SELECT p.full_name, p.id_card, p.date_of_birth, prov.name, t.name, v.name, p.state, qrt.name 
 	FROM related_persons rp join patients p on p.id = rp.id_related 
    	join provinces prov on p.id_prov = prov.id 
        join towns t on t.id = p.id_town 
        join villages v on v.id = p.id_vlg 
        join quarantinepos qrt on qrt.id = p.id_pos 
	WHERE rp.id_related = (select id from patients where id_card = idCard limit 1);
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `getTowns`(
	IN provName NVARCHAR(50)
)
BEGIN
	select t.name from provinces_towns pt join provinces p on pt.id_prov = p.id 
    		join towns t on t.id = pt.id_town 
    where p.name = provName
    order by t.name;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `getUsrInfoByIdCard`(IN `idCard` VARCHAR(12))
BEGIN
	select p.full_name, p.id_card, p.date_of_birth, v.name, t.name, prov.name, p.state, q.name, p.id
    from patients p join quarantinepos q on p.id_pos = q.id 
    	join provinces prov on prov.id = p.id_prov
        join towns t on t.id = p.id_town
        join villages v on v.id = p.id_vlg
    where p.id_card = idCard;
    
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `getVillages`(
	IN provName NVARCHAR(50),
	IN townName Nvarchar(50)
)
BEGIN
	select v.name from towns_villages tv join villages v on tv.id_vlg = v.id 
    		join towns t on t.id = tv.id_town 
            join provinces_towns pt on pt.id_town = t.id
            join provinces p on p.id = pt.id_prov
    where t.name = townName and p.name = provName
    order by v.name;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `insertIntoBoughtPkgHis`(IN `idCard` VARCHAR(12), IN `listPkgN` MEDIUMTEXT, IN `listQuan` MEDIUMTEXT, OUT `code` INT)
    NO SQL
BEGIN
declare _nextPkgN varchar(50) default null;
DECLARE _nextQuan varchar(20) DEFAULT NULL;

DECLARE _nextlenPkgN INT DEFAULT NULL;
declare _nextlenQuan int default null;

DECLARE _valuePkgN varchar(50) DEFAULT NULL;
declare _valueQuan varchar(20) default null;

DECLARE idPatient int;
declare idPkg int;
declare date_now date;
declare costPkg int default null;
declare quanNum int;

select 0 into code;
set idPatient = (select p.id from patients p where p.id_card = idCard limit 1);
set date_now = now();

iterator:
LOOP

  IF CHAR_LENGTH(TRIM(listPkgN)) = 0 OR listPkgN IS NULL or CHAR_LENGTH(TRIM(listQuan)) = 0 or listQuan is null THEN
    LEAVE iterator;
  END IF;
 
  SET _nextPkgN = SUBSTRING_INDEX(listPkgN, ';', 1);
  SET _nextQuan = SUBSTRING_INDEX(listQuan, ';', 1);

  SET _nextlenPkgN = CHAR_LENGTH(_nextPkgN);
  SET _nextlenQuan = CHAR_LENGTH(_nextQuan);

  SET _valuePkgN = TRIM(_nextPkgN);
  SET _valueQuan = TRIM(_nextQuan);

  set idPkg = (select p.id from necessary_packages p where p.pkg_name = _valuePkgN limit 1);
  set costPkg = (select p.price from necessary_packages p where p.pkg_name = _valuePkgN limit 1);
  set quanNum = cast(_valueQuan as signed);
  set costPkg = costPkg * quanNum;

  INSERT INTO bought_pkg_history (id_patient, id_pkg, quantity, date, price) VALUES (idPatient, idPkg, quanNum, date_now, costPkg);

  select code + 1 into code;

  SET listPkgN = INSERT(listPkgN, 1 , _nextlenPkgN + 1,'');
  SET listQuan = INSERT(listQuan, 1 , _nextlenQuan + 1,'');

END LOOP;

END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `isFirstInit`()
BEGIN
	SELECT count(*)
 	FROM accounts
	WHERE id_permission = 1;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `updatePkg`(IN `pkgName` VARCHAR(50), IN `limitPerPerson` VARCHAR(20), IN `dateLimitIn` DATE, IN `priceIn` VARCHAR(20), IN `usrManager` VARCHAR(20), IN `newPkgN` VARCHAR(50), OUT `code` INT)
    NO SQL
begin
        declare date_now date;
        declare priceDecimal decimal;
        declare limit_person int;
        select -1 into code;
        set limit_person = cast(replace(limitPerPerson, ",", "") as unsigned);
        set priceDecimal = cast(replace(priceIn, ",", "") as decimal);
        set date_now = now();
                
        update necessary_packages 
        set pkg_name = newPkgN, limit_quantity_per_person = limit_person, price = priceDecimal, date_limit = dateLimitIn 
        where pkg_name = pkgName;
        
        insert into activity_history(usr_manager, date, id_card_patient, description) 
        values (usrManager, date_now, null, concat(N'Cập nhật gói ', pkgName, N' thành gói ', newPkgN, N', hạn mức ', limitPerPerson, N' gói/ người, bán đến hết ngày ', dateLimitIn, N' với giá ', priceIn, N' VNĐ'));
		select 1 into code;

end$$

DELIMITER ;

-- --------------------------------------------------------

--
-- Table structure for table `accounts`
--

CREATE TABLE IF NOT EXISTS `accounts` (
`id` int(10) unsigned zerofill NOT NULL,
  `usrname` varchar(20) CHARACTER SET ascii NOT NULL,
  `pwd` varchar(50) CHARACTER SET ascii NOT NULL,
  `id_permission` int(11) NOT NULL,
  `is_locked` tinyint(1) NOT NULL
) ENGINE=InnoDB AUTO_INCREMENT=41 DEFAULT CHARSET=utf8;

--
-- Dumping data for table `accounts`
--

INSERT INTO `accounts` (`id`, `usrname`, `pwd`, `id_permission`, `is_locked`) VALUES
(0000000007, 'test', '3440400d53523c54a5f5c142f828afe7e2326d20dfe8bd3c0d', 2, 0),
(0000000013, 'test2', '3440400d53523c54a5f5c142f828afe7e2326d20dfe8bd3c0d', 2, 0),
(0000000014, 'test3', '3440400d53523c54a5f5c142f828afe7e2326d20dfe8bd3c0d', 2, 0),
(0000000019, 'test4', '3440400d53523c54a5f5c142f828afe7e2326d20dfe8bd3c0d', 2, 0),
(0000000020, 'admin', 'e5486a6670a3f3b6821574c1812be01914b9908a94be89e1c8', 1, 0),
(0000000021, 'test5', '3440400d53523c54a5f5c142f828afe7e2326d20dfe8bd3c0d', 2, 0),
(0000000022, 'manager', 'e5486a6670a3f3b6821574c1812be01914b9908a94be89e1c8', 0, 0),
(0000000023, 'test6', '3440400d53523c54a5f5c142f828afe7e2326d20dfe8bd3c0d', 2, 0),
(0000000024, 'test7', '3440400d53523c54a5f5c142f828afe7e2326d20dfe8bd3c0d', 2, 0),
(0000000025, 'test8', '3440400d53523c54a5f5c142f828afe7e2326d20dfe8bd3c0d', 2, 0),
(0000000026, 'manager1', 'e5486a6670a3f3b6821574c1812be01914b9908a94be89e1c8', 0, 0),
(0000000027, 'manager2', 'e5486a6670a3f3b6821574c1812be01914b9908a94be89e1c8', 0, 0),
(0000000028, 'test9', '3440400d53523c54a5f5c142f828afe7e2326d20dfe8bd3c0d', 2, 0),
(0000000029, 'test10', '3440400d53523c54a5f5c142f828afe7e2326d20dfe8bd3c0d', 2, 0),
(0000000030, 'test11', '3440400d53523c54a5f5c142f828afe7e2326d20dfe8bd3c0d', 2, 0),
(0000000031, 'test12', '3440400d53523c54a5f5c142f828afe7e2326d20dfe8bd3c0d', 2, 0),
(0000000032, 'test13', '3440400d53523c54a5f5c142f828afe7e2326d20dfe8bd3c0d', 2, 0),
(0000000033, 'test14', '3440400d53523c54a5f5c142f828afe7e2326d20dfe8bd3c0d', 2, 0),
(0000000034, 'test15', '3440400d53523c54a5f5c142f828afe7e2326d20dfe8bd3c0d', 2, 0),
(0000000035, 'test16', '3440400d53523c54a5f5c142f828afe7e2326d20dfe8bd3c0d', 2, 0),
(0000000036, 'test17', '3440400d53523c54a5f5c142f828afe7e2326d20dfe8bd3c0d', 2, 0),
(0000000037, 'manager3', 'e5486a6670a3f3b6821574c1812be01914b9908a94be89e1c8', 0, 0),
(0000000038, 'test18', '3440400d53523c54a5f5c142f828afe7e2326d20dfe8bd3c0d', 2, 0),
(0000000039, 'test19', '3440400d53523c54a5f5c142f828afe7e2326d20dfe8bd3c0d', 2, 0),
(0000000040, 'test20', '3440400d53523c54a5f5c142f828afe7e2326d20dfe8bd3c0d', 2, 0);

--
-- Triggers `accounts`
--
DELIMITER //
CREATE TRIGGER `addNewDebtAndPaymentAcc` AFTER INSERT ON `accounts`
 FOR EACH ROW BEGIN
    declare isExisted int;
    set isExisted = (select count(*) from accounts where id_permission = 1);
    
    if new.id_permission = 2 then
        
        insert into debt(id_patient, debt)
        values (new.id, 0);
        insert into payment_acc(id_acc, balance) values (new.id, 10000000);
    end if;
    if new.id_permission = 1 and isExisted = 1 then
    	insert into payment_acc(id_acc, balance) values(new.id, 0);
    end if;
    
END
//
DELIMITER ;
DELIMITER //
CREATE TRIGGER `isFirstLogin` AFTER UPDATE ON `accounts`
 FOR EACH ROW begin
	declare isExisted int;
    set isExisted = (select count(*) from logon where id_patient = old.id );
    if old.id_permission = 2 and isExisted = 0 then

        insert into logon set id_patient = old.id;

    end if;




end
//
DELIMITER ;

-- --------------------------------------------------------

--
-- Table structure for table `activity_history`
--

CREATE TABLE IF NOT EXISTS `activity_history` (
`id` int(10) unsigned zerofill NOT NULL,
  `usr_manager` varchar(20) CHARACTER SET ascii NOT NULL,
  `date` date NOT NULL,
  `id_card_patient` varchar(12) DEFAULT NULL,
  `description` varchar(200) NOT NULL
) ENGINE=InnoDB AUTO_INCREMENT=48 DEFAULT CHARSET=utf8;

--
-- Dumping data for table `activity_history`
--

INSERT INTO `activity_history` (`id`, `usr_manager`, `date`, `id_card_patient`, `description`) VALUES
(0000000003, 'manager', '2021-11-09', '444455555', 'Thêm 444455555 làm F0 tại Bệnh viện Bạch Mai'),
(0000000005, 'manager', '2021-11-11', '111111119', 'Thêm 111111119 làm F2 tại Bệnh viện Bạch Mai'),
(0000000009, 'manager', '2021-11-11', NULL, 'Thêm gói package test, hạn mức 2 gói/ người, bán đến hết ngày 1911-11-11 với giá 19,000 VNĐ'),
(0000000010, 'manager', '2021-11-12', NULL, 'Thêm gói PACKAGE TEST2, hạn mức 3 gói/ người, bán đến hết ngày 2001-11-11 với giá 90,000 VNĐ'),
(0000000012, 'manager', '2021-11-12', NULL, 'Thêm gói pkgdel1, hạn mức 3 gói/ người, bán đến hết ngày 2021-11-12 với giá 19,000 VNĐ'),
(0000000013, 'manager', '2021-11-12', NULL, 'Thêm gói pkgdel2, hạn mức 3 gói/ người, bán đến hết ngày 2021-11-12 với giá 19,000 VNĐ'),
(0000000014, 'manager', '2021-11-12', NULL, 'Xoá gói pkgdel1'),
(0000000015, 'manager', '2021-11-12', NULL, 'Xoá gói pkgdel2'),
(0000000016, 'manager', '2021-11-12', NULL, 'Thêm gói pkgdel1, hạn mức 3 gói/ người, bán đến hết ngày 2021-11-12 với giá 19,000 VNĐ'),
(0000000017, 'manager', '2021-11-12', NULL, 'Thêm gói pkgdel2, hạn mức 2 gói/ người, bán đến hết ngày 2021-11-12 với giá 19,000 VNĐ'),
(0000000018, 'manager', '2021-11-12', NULL, 'Xoá gói pkgdel1'),
(0000000019, 'manager', '2021-11-12', NULL, 'Xoá gói pkgdel2'),
(0000000022, 'manager', '2021-11-12', NULL, 'Cập nhật gói package test thành gói PACKAGE TEST, hạn mức 3 gói/ người, bán đến hết ngày 2021-11-12 với giá 19,000 VNĐ'),
(0000000023, 'manager', '2021-11-12', NULL, 'Cập nhật gói PACKAGE TEST3 thành gói PACKAGE TEST2, hạn mức 3 gói/ người, bán đến hết ngày 2021-11-12 với giá 19,000 VNĐ'),
(0000000024, 'manager', '2021-11-13', NULL, 'Cập nhật gói PACKAGE TEST2 thành gói PACKAGE TEST2, hạn mức 5 gói/ người, bán đến hết ngày 2021-11-13 với giá 69,000 VNĐ'),
(0000000025, 'manager', '2021-11-13', '987123546', 'Thêm 987123546 làm F0 tại Bệnh viện Bạch Mai'),
(0000000026, 'manager', '2021-11-15', '999999999', 'Thêm 999999999 làm F0 tại Bệnh viện Bạch Mai'),
(0000000027, 'manager', '2021-11-15', '101010101', 'Thêm 101010101 làm F0 tại Bệnh viện Bạch Mai'),
(0000000028, 'manager', '2021-11-15', '111111199', 'Thêm 111111199 làm F0 tại Bệnh viện Gia An 115'),
(0000000029, 'manager', '2021-11-15', '121212121', 'Thêm 121212121 làm F0 tại Bệnh viện Gia An 115'),
(0000000030, 'manager', '2021-11-15', '123123123', 'Thêm 123123123 làm F0 tại Bệnh viện Bạch Mai'),
(0000000031, 'manager', '2021-11-15', '140214021', 'Thêm 140214021 làm F0 tại Bệnh viện Bạch Mai'),
(0000000032, 'manager', '2021-11-15', NULL, 'Thêm gói PACKAGE TEST3, hạn mức 3 gói/ người, bán đến hết ngày 2021-11-16 với giá 19,000 VNĐ'),
(0000000033, 'manager', '2021-11-15', NULL, 'Thêm gói PACKAGE TEST4, hạn mức 6 gói/ người, bán đến hết ngày 2021-11-16 với giá 19,000 VNĐ'),
(0000000034, 'manager', '2021-11-15', NULL, 'Thêm gói PACKAGE TEST5, hạn mức 6 gói/ người, bán đến hết ngày 2021-11-16 với giá 19,000 VNĐ'),
(0000000035, 'manager', '2021-11-15', '150119911', 'Thêm 150119911 làm F0 tại Bệnh viện Bạch Mai'),
(0000000036, 'manager', '2021-11-15', NULL, 'Thêm gói PACKAGE TEST6, hạn mức 6 gói/ người, bán đến hết ngày 2021-11-16 với giá 16,000 VNĐ'),
(0000000037, 'manager', '2021-11-15', '160619999', 'Thêm 160619999 làm F1 tại Bệnh viện Bạch Mai'),
(0000000038, 'manager', '2021-11-15', '170919999', 'Thêm 170919999 làm F1 tại Bệnh viện Gia An 115'),
(0000000039, 'manager', '2021-11-15', '180819988', 'Thêm 180819988 làm F0 tại Bệnh viện Bạch Mai'),
(0000000040, 'manager', '2021-11-15', NULL, 'Xoá gói PACKAGE TEST5'),
(0000000041, 'manager', '2021-11-15', NULL, 'Xoá gói PACKAGE TEST6'),
(0000000042, 'manager', '2021-11-15', NULL, 'Xoá gói PACKAGE TEST3'),
(0000000043, 'manager', '2021-11-15', NULL, 'Xoá gói PACKAGE TEST4'),
(0000000044, 'manager', '2021-11-15', NULL, 'Thêm gói PACKAGE TEST3, hạn mức 3 gói/ người, bán đến hết ngày 2021-11-15 với giá 19,000 VNĐ'),
(0000000045, 'manager', '2021-11-15', NULL, 'Cập nhật gói PACKAGE TEST2 thành gói PACKAGE TEST2, hạn mức 5 gói/ người, bán đến hết ngày 2021-11-16 với giá 69,000 VNĐ'),
(0000000046, 'manager', '2021-11-17', '190119919', 'Thêm 190119919 làm F0 tại Bệnh viện Bạch Mai'),
(0000000047, 'manager', '2021-11-17', '200120011', 'Thêm 200120011 làm F0 tại Bệnh viện Bạch Mai');

-- --------------------------------------------------------

--
-- Table structure for table `bought_pkg_history`
--

CREATE TABLE IF NOT EXISTS `bought_pkg_history` (
`id` int(10) unsigned zerofill NOT NULL,
  `id_patient` int(10) unsigned zerofill NOT NULL,
  `id_pkg` int(10) unsigned zerofill NOT NULL,
  `quantity` int(10) unsigned NOT NULL,
  `date` date NOT NULL,
  `price` decimal(10,0) unsigned NOT NULL
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8;

--
-- Dumping data for table `bought_pkg_history`
--

INSERT INTO `bought_pkg_history` (`id`, `id_patient`, `id_pkg`, `quantity`, `date`, `price`) VALUES
(0000000002, 0000000025, 0000000003, 2, '2021-11-14', '138000'),
(0000000003, 0000000025, 0000000003, 1, '2021-11-14', '69000'),
(0000000010, 0000000025, 0000000002, 2, '2021-11-14', '38000'),
(0000000011, 0000000025, 0000000003, 1, '2021-11-14', '69000'),
(0000000012, 0000000025, 0000000003, 1, '2021-11-14', '69000'),
(0000000013, 0000000036, 0000000004, 2, '2021-11-15', '38000');

--
-- Triggers `bought_pkg_history`
--
DELIMITER //
CREATE TRIGGER `checkDebtWhenBuyPkg` AFTER INSERT ON `bought_pkg_history`
 FOR EACH ROW BEGIN
	declare credit decimal;
    set credit = (select debt from debt where id_patient = new.id_patient limit 1);
    set credit = credit + new.price;
	
    update debt set debt = credit where id_patient = new.id_patient;

END
//
DELIMITER ;

-- --------------------------------------------------------

--
-- Table structure for table `debt`
--

CREATE TABLE IF NOT EXISTS `debt` (
`id_patient` int(10) unsigned zerofill NOT NULL,
  `debt` int(10) unsigned NOT NULL
) ENGINE=InnoDB AUTO_INCREMENT=41 DEFAULT CHARSET=utf8;

--
-- Dumping data for table `debt`
--

INSERT INTO `debt` (`id_patient`, `debt`) VALUES
(0000000007, 0),
(0000000013, 0),
(0000000014, 0),
(0000000019, 0),
(0000000021, 0),
(0000000023, 0),
(0000000024, 0),
(0000000025, 383000),
(0000000028, 0),
(0000000029, 0),
(0000000030, 0),
(0000000031, 0),
(0000000032, 0),
(0000000033, 0),
(0000000034, 0),
(0000000035, 0),
(0000000036, 38000),
(0000000038, 0),
(0000000039, 0),
(0000000040, 440000);

-- --------------------------------------------------------

--
-- Table structure for table `logon`
--

CREATE TABLE IF NOT EXISTS `logon` (
  `id_patient` int(10) unsigned zerofill NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `logon`
--

INSERT INTO `logon` (`id_patient`) VALUES
(0000000007),
(0000000024),
(0000000025),
(0000000040);

-- --------------------------------------------------------

--
-- Table structure for table `management_history`
--

CREATE TABLE IF NOT EXISTS `management_history` (
`id` int(10) unsigned zerofill NOT NULL,
  `id_patient` int(10) unsigned zerofill NOT NULL,
  `date` date NOT NULL,
  `state` varchar(2) NOT NULL,
  `id_qrt_pos` int(10) unsigned zerofill NOT NULL
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8;

--
-- Dumping data for table `management_history`
--

INSERT INTO `management_history` (`id`, `id_patient`, `date`, `state`, `id_qrt_pos`) VALUES
(0000000007, 0000000023, '2021-11-09', 'F0', 0000000001),
(0000000008, 0000000024, '2021-11-11', 'F2', 0000000001),
(0000000009, 0000000025, '2021-11-13', 'F0', 0000000001),
(0000000010, 0000000028, '2021-11-15', 'F0', 0000000001),
(0000000011, 0000000029, '2021-11-15', 'F0', 0000000001),
(0000000012, 0000000030, '2021-11-15', 'F0', 0000000002),
(0000000013, 0000000031, '2021-11-15', 'F0', 0000000002),
(0000000014, 0000000032, '2021-11-15', 'F0', 0000000001),
(0000000015, 0000000033, '2021-11-15', 'F0', 0000000001),
(0000000016, 0000000034, '2021-11-15', 'F0', 0000000001),
(0000000017, 0000000035, '2021-11-15', 'F1', 0000000001),
(0000000018, 0000000036, '2021-11-15', 'F1', 0000000002),
(0000000019, 0000000038, '2021-11-15', 'F0', 0000000001),
(0000000020, 0000000039, '2021-11-17', 'F0', 0000000001),
(0000000021, 0000000040, '2021-11-17', 'F0', 0000000001);

-- --------------------------------------------------------

--
-- Table structure for table `necessary_packages`
--

CREATE TABLE IF NOT EXISTS `necessary_packages` (
`id` int(10) unsigned zerofill NOT NULL,
  `pkg_name` varchar(50) NOT NULL,
  `limit_quantity_per_person` int(10) unsigned NOT NULL,
  `date_limit` date NOT NULL,
  `price` decimal(10,0) unsigned NOT NULL,
  `is_deleted` int(11) NOT NULL DEFAULT '0'
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8;

--
-- Dumping data for table `necessary_packages`
--

INSERT INTO `necessary_packages` (`id`, `pkg_name`, `limit_quantity_per_person`, `date_limit`, `price`, `is_deleted`) VALUES
(0000000002, 'PACKAGE TEST', 3, '2021-11-14', '19000', 0),
(0000000003, 'PACKAGE TEST2', 5, '2021-11-16', '69000', 0),
(0000000004, 'PACKAGE TEST3', 3, '2021-11-16', '19000', 1),
(0000000005, 'PACKAGE TEST4', 6, '2021-11-16', '19000', 1),
(0000000008, 'PACKAGE TEST3', 3, '2021-11-15', '19000', 0);

-- --------------------------------------------------------

--
-- Table structure for table `patients`
--

CREATE TABLE IF NOT EXISTS `patients` (
  `id` int(10) unsigned zerofill NOT NULL,
  `full_name` varchar(50) NOT NULL,
  `id_card` varchar(12) NOT NULL,
  `date_of_birth` date NOT NULL,
  `id_prov` int(10) unsigned zerofill NOT NULL,
  `id_town` int(10) unsigned zerofill NOT NULL,
  `id_vlg` int(10) unsigned zerofill NOT NULL,
  `state` varchar(2) NOT NULL,
  `id_pos` int(10) unsigned zerofill NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `patients`
--

INSERT INTO `patients` (`id`, `full_name`, `id_card`, `date_of_birth`, `id_prov`, `id_town`, `id_vlg`, `state`, `id_pos`) VALUES
(0000000007, 'OKLA', '123456789', '1991-03-12', 0000000001, 0000000001, 0000000001, 'F0', 0000000001),
(0000000013, 'Test cái procedure', '987651234', '2012-12-31', 0000000001, 0000000001, 0000000001, 'F0', 0000000001),
(0000000014, 'Lại là Testt', '222222222', '2012-12-31', 0000000001, 0000000001, 0000000001, 'F1', 0000000001),
(0000000019, 'TEST4', '444444444', '1992-12-13', 0000000001, 0000000001, 0000000001, 'F1', 0000000001),
(0000000021, 'Đây Là Test5', '555111555', '1991-07-09', 0000000001, 0000000001, 0000000001, 'F1', 0000000001),
(0000000023, 'Test test6', '444455555', '1992-11-13', 0000000001, 0000000001, 0000000001, 'F0', 0000000001),
(0000000024, 'ĐÂY LÀ TEST 7', '111111119', '1991-11-11', 0000000001, 0000000001, 0000000001, 'F2', 0000000001),
(0000000025, 'ĐÂY LÀ NGTEST', '987123546', '1991-11-11', 0000000001, 0000000001, 0000000001, 'F0', 0000000001),
(0000000028, 'TESTTTTTTTTTT', '999999999', '2000-09-09', 0000000001, 0000000001, 0000000001, 'F0', 0000000001),
(0000000029, 'ĐÂY LÀ TEST', '101010101', '2000-10-10', 0000000001, 0000000001, 0000000001, 'F0', 0000000001),
(0000000030, 'TEST THỨ MƯỜI MỘT', '111111199', '1911-11-11', 0000000001, 0000000001, 0000000001, 'F0', 0000000002),
(0000000031, 'ĐÂY LÀ TEST MƯỜI HAI', '121212121', '1992-12-12', 0000000001, 0000000001, 0000000001, 'F0', 0000000002),
(0000000032, 'ĐÂY LÀ TEST MƯỜI BA', '123123123', '1991-01-13', 0000000001, 0000000001, 0000000001, 'F0', 0000000001),
(0000000033, 'ĐÂY LÀ TEST MƯỜI BỐN', '140214021', '1992-02-14', 0000000001, 0000000001, 0000000001, 'F0', 0000000001),
(0000000034, 'ĐÂY LÀ TEST MƯỜI LĂM', '150119911', '1991-01-15', 0000000001, 0000000001, 0000000001, 'F0', 0000000001),
(0000000035, 'ĐÂY LÀ TEST MƯỜI SÁU', '160619999', '1999-06-16', 0000000001, 0000000001, 0000000001, 'F1', 0000000001),
(0000000036, 'ĐÂY LÀ TEST MƯỜI BẢY', '170919999', '1999-09-17', 0000000001, 0000000001, 0000000001, 'F1', 0000000002),
(0000000038, 'ĐÂY LÀ TEST MƯỜI TÁM', '180819988', '1998-08-18', 0000000001, 0000000001, 0000000001, 'F0', 0000000001),
(0000000039, 'ĐÂY LÀ TEST MƯỜI CHÍN', '190119919', '1991-01-19', 0000000001, 0000000001, 0000000001, 'F0', 0000000001),
(0000000040, 'ĐÂY LÀ TEST HAI MƯƠI', '200120011', '2001-01-20', 0000000001, 0000000001, 0000000001, 'F0', 0000000001);

--
-- Triggers `patients`
--
DELIMITER //
CREATE TRIGGER `addMngmHis_insert` AFTER INSERT ON `patients`
 FOR EACH ROW BEGIN
	
 declare date_now date;
set date_now = now();
 INSERT INTO management_history(id_patient, date, state, id_qrt_pos)
 values (new.id, date_now, new.state, new.id_pos);

END
//
DELIMITER ;
DELIMITER //
CREATE TRIGGER `addMngmHis_update` AFTER UPDATE ON `patients`
 FOR EACH ROW BEGIN
    	declare date_now date;
        set date_now = now();
        INSERT INTO management_history(id_patient,date, state, id_qrt_pos)
        VALUES (new.id, date_now, new.state, new.id_pos);
    
END
//
DELIMITER ;
DELIMITER //
CREATE TRIGGER `checkWhenAddPatient` BEFORE INSERT ON `patients`
 FOR EACH ROW BEGIN
declare cur_cap int;
declare max_cap int;
set cur_cap = (select current_capacity from quarantinepos where id = new.id_pos limit 1);
set max_cap = (select capacity from quarantinepos where id = new.id_pos limit 1);
set cur_cap = cur_cap + 1;
if cur_cap <= max_cap then
	update quarantinepos
	set current_capacity = cur_cap
    where id = new.id_pos;
end if;

END
//
DELIMITER ;

-- --------------------------------------------------------

--
-- Table structure for table `payment_acc`
--

CREATE TABLE IF NOT EXISTS `payment_acc` (
  `id_acc` int(10) unsigned zerofill NOT NULL,
  `balance` decimal(10,0) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `payment_acc`
--

INSERT INTO `payment_acc` (`id_acc`, `balance`) VALUES
(0000000020, '60000'),
(0000000039, '10000000'),
(0000000040, '9940000');

-- --------------------------------------------------------

--
-- Table structure for table `provinces`
--

CREATE TABLE IF NOT EXISTS `provinces` (
`id` int(10) unsigned zerofill NOT NULL,
  `name` varchar(50) NOT NULL
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;

--
-- Dumping data for table `provinces`
--

INSERT INTO `provinces` (`id`, `name`) VALUES
(0000000001, 'Hồ Chí Minh'),
(0000000002, 'Hà Nội'),
(0000000003, 'An Giang');

-- --------------------------------------------------------

--
-- Table structure for table `provinces_towns`
--

CREATE TABLE IF NOT EXISTS `provinces_towns` (
  `id_prov` int(10) unsigned zerofill NOT NULL,
  `id_town` int(10) unsigned zerofill NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `provinces_towns`
--

INSERT INTO `provinces_towns` (`id_prov`, `id_town`) VALUES
(0000000001, 0000000001);

-- --------------------------------------------------------

--
-- Table structure for table `quarantinepos`
--

CREATE TABLE IF NOT EXISTS `quarantinepos` (
`id` int(10) unsigned zerofill NOT NULL,
  `name` varchar(50) NOT NULL,
  `capacity` int(10) unsigned zerofill NOT NULL,
  `current_capacity` int(10) unsigned zerofill NOT NULL
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;

--
-- Dumping data for table `quarantinepos`
--

INSERT INTO `quarantinepos` (`id`, `name`, `capacity`, `current_capacity`) VALUES
(0000000001, 'Bệnh viện Bạch Mai', 0000001000, 0000000910),
(0000000002, 'Bệnh viện Gia An 115', 0000000900, 0000000104);

-- --------------------------------------------------------

--
-- Table structure for table `related_persons`
--

CREATE TABLE IF NOT EXISTS `related_persons` (
  `id_patient` int(10) unsigned zerofill NOT NULL,
  `id_related` int(10) unsigned zerofill NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `related_persons`
--

INSERT INTO `related_persons` (`id_patient`, `id_related`) VALUES
(0000000014, 0000000007),
(0000000019, 0000000007),
(0000000014, 0000000013),
(0000000019, 0000000013),
(0000000024, 0000000019),
(0000000035, 0000000034),
(0000000036, 0000000034);

-- --------------------------------------------------------

--
-- Table structure for table `towns`
--

CREATE TABLE IF NOT EXISTS `towns` (
`id` int(10) unsigned zerofill NOT NULL,
  `name` varchar(50) NOT NULL
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;

--
-- Dumping data for table `towns`
--

INSERT INTO `towns` (`id`, `name`) VALUES
(0000000001, 'Quận 1');

-- --------------------------------------------------------

--
-- Table structure for table `towns_villages`
--

CREATE TABLE IF NOT EXISTS `towns_villages` (
  `id_town` int(10) unsigned zerofill NOT NULL,
  `id_vlg` int(10) unsigned zerofill NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `towns_villages`
--

INSERT INTO `towns_villages` (`id_town`, `id_vlg`) VALUES
(0000000001, 0000000001);

-- --------------------------------------------------------

--
-- Table structure for table `transaction_history`
--

CREATE TABLE IF NOT EXISTS `transaction_history` (
`id` int(10) unsigned zerofill NOT NULL,
  `from_id_acc` int(10) unsigned zerofill NOT NULL,
  `to_id_acc` int(10) unsigned zerofill NOT NULL,
  `credit` decimal(10,0) unsigned NOT NULL,
  `date_trans` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `remaining_debt` decimal(10,0) unsigned NOT NULL,
  `remaining_balance` decimal(10,0) unsigned NOT NULL
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8;

--
-- Dumping data for table `transaction_history`
--

INSERT INTO `transaction_history` (`id`, `from_id_acc`, `to_id_acc`, `credit`, `date_trans`, `remaining_debt`, `remaining_balance`) VALUES
(0000000002, 0000000040, 0000000020, '20000', '2021-11-17 09:44:11', '480000', '9980000'),
(0000000003, 0000000040, 0000000020, '20000', '2021-11-17 10:11:56', '460000', '9960000'),
(0000000004, 0000000040, 0000000020, '20000', '2021-11-17 11:00:00', '440000', '9940000');

--
-- Triggers `transaction_history`
--
DELIMITER //
CREATE TRIGGER `updateBalanceAndDebt` BEFORE INSERT ON `transaction_history`
 FOR EACH ROW begin
	update payment_acc 
    set balance = balance - new.credit 
    where id_acc = new.from_id_acc;

	update payment_acc 
    set balance = balance + new.credit 
    where id_acc = new.to_id_acc;

	update debt 
    set debt = debt - new.credit 
    where id_patient = new.from_id_acc;





end
//
DELIMITER ;

-- --------------------------------------------------------

--
-- Table structure for table `villages`
--

CREATE TABLE IF NOT EXISTS `villages` (
`id` int(10) unsigned zerofill NOT NULL,
  `name` varchar(50) NOT NULL
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;

--
-- Dumping data for table `villages`
--

INSERT INTO `villages` (`id`, `name`) VALUES
(0000000001, 'Phường Cầu Ông Lãnh');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `accounts`
--
ALTER TABLE `accounts`
 ADD PRIMARY KEY (`id`), ADD UNIQUE KEY `usrname` (`usrname`);

--
-- Indexes for table `activity_history`
--
ALTER TABLE `activity_history`
 ADD PRIMARY KEY (`id`), ADD KEY `FK_actHis_patients` (`id_card_patient`), ADD KEY `fk_acthis_account` (`usr_manager`);

--
-- Indexes for table `bought_pkg_history`
--
ALTER TABLE `bought_pkg_history`
 ADD PRIMARY KEY (`id`), ADD KEY `fk_bpkghis_patient` (`id_patient`), ADD KEY `fk_bpkghis_pkg` (`id_pkg`);

--
-- Indexes for table `debt`
--
ALTER TABLE `debt`
 ADD PRIMARY KEY (`id_patient`);

--
-- Indexes for table `logon`
--
ALTER TABLE `logon`
 ADD PRIMARY KEY (`id_patient`);

--
-- Indexes for table `management_history`
--
ALTER TABLE `management_history`
 ADD PRIMARY KEY (`id`), ADD KEY `fk_mgmthis_patient` (`id_patient`), ADD KEY `fk_mgmthis_qrtpos` (`id_qrt_pos`);

--
-- Indexes for table `necessary_packages`
--
ALTER TABLE `necessary_packages`
 ADD PRIMARY KEY (`id`);

--
-- Indexes for table `patients`
--
ALTER TABLE `patients`
 ADD PRIMARY KEY (`id`), ADD UNIQUE KEY `id_card` (`id_card`), ADD KEY `fk_patients_qrtpos` (`id_pos`), ADD KEY `fk_patients_prov` (`id_prov`), ADD KEY `fk_patients_town` (`id_town`), ADD KEY `fk_patients_vlg` (`id_vlg`);

--
-- Indexes for table `payment_acc`
--
ALTER TABLE `payment_acc`
 ADD PRIMARY KEY (`id_acc`);

--
-- Indexes for table `provinces`
--
ALTER TABLE `provinces`
 ADD PRIMARY KEY (`id`);

--
-- Indexes for table `provinces_towns`
--
ALTER TABLE `provinces_towns`
 ADD PRIMARY KEY (`id_prov`,`id_town`), ADD KEY `fk_provtown_town` (`id_town`);

--
-- Indexes for table `quarantinepos`
--
ALTER TABLE `quarantinepos`
 ADD PRIMARY KEY (`id`), ADD UNIQUE KEY `name` (`name`);

--
-- Indexes for table `related_persons`
--
ALTER TABLE `related_persons`
 ADD PRIMARY KEY (`id_patient`,`id_related`), ADD KEY `fk_rltper_patient_to` (`id_related`);

--
-- Indexes for table `towns`
--
ALTER TABLE `towns`
 ADD PRIMARY KEY (`id`);

--
-- Indexes for table `towns_villages`
--
ALTER TABLE `towns_villages`
 ADD PRIMARY KEY (`id_town`,`id_vlg`), ADD KEY `fk_townvlg_vlg` (`id_vlg`);

--
-- Indexes for table `transaction_history`
--
ALTER TABLE `transaction_history`
 ADD PRIMARY KEY (`id`), ADD KEY `FK_transHis_fromIDAcc` (`from_id_acc`), ADD KEY `FK_transHis_toIDAcc` (`to_id_acc`);

--
-- Indexes for table `villages`
--
ALTER TABLE `villages`
 ADD PRIMARY KEY (`id`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `accounts`
--
ALTER TABLE `accounts`
MODIFY `id` int(10) unsigned zerofill NOT NULL AUTO_INCREMENT,AUTO_INCREMENT=41;
--
-- AUTO_INCREMENT for table `activity_history`
--
ALTER TABLE `activity_history`
MODIFY `id` int(10) unsigned zerofill NOT NULL AUTO_INCREMENT,AUTO_INCREMENT=48;
--
-- AUTO_INCREMENT for table `bought_pkg_history`
--
ALTER TABLE `bought_pkg_history`
MODIFY `id` int(10) unsigned zerofill NOT NULL AUTO_INCREMENT,AUTO_INCREMENT=14;
--
-- AUTO_INCREMENT for table `debt`
--
ALTER TABLE `debt`
MODIFY `id_patient` int(10) unsigned zerofill NOT NULL AUTO_INCREMENT,AUTO_INCREMENT=41;
--
-- AUTO_INCREMENT for table `management_history`
--
ALTER TABLE `management_history`
MODIFY `id` int(10) unsigned zerofill NOT NULL AUTO_INCREMENT,AUTO_INCREMENT=22;
--
-- AUTO_INCREMENT for table `necessary_packages`
--
ALTER TABLE `necessary_packages`
MODIFY `id` int(10) unsigned zerofill NOT NULL AUTO_INCREMENT,AUTO_INCREMENT=9;
--
-- AUTO_INCREMENT for table `provinces`
--
ALTER TABLE `provinces`
MODIFY `id` int(10) unsigned zerofill NOT NULL AUTO_INCREMENT,AUTO_INCREMENT=4;
--
-- AUTO_INCREMENT for table `quarantinepos`
--
ALTER TABLE `quarantinepos`
MODIFY `id` int(10) unsigned zerofill NOT NULL AUTO_INCREMENT,AUTO_INCREMENT=3;
--
-- AUTO_INCREMENT for table `towns`
--
ALTER TABLE `towns`
MODIFY `id` int(10) unsigned zerofill NOT NULL AUTO_INCREMENT,AUTO_INCREMENT=2;
--
-- AUTO_INCREMENT for table `transaction_history`
--
ALTER TABLE `transaction_history`
MODIFY `id` int(10) unsigned zerofill NOT NULL AUTO_INCREMENT,AUTO_INCREMENT=5;
--
-- AUTO_INCREMENT for table `villages`
--
ALTER TABLE `villages`
MODIFY `id` int(10) unsigned zerofill NOT NULL AUTO_INCREMENT,AUTO_INCREMENT=2;
--
-- Constraints for dumped tables
--

--
-- Constraints for table `activity_history`
--
ALTER TABLE `activity_history`
ADD CONSTRAINT `FK_actHis_patients` FOREIGN KEY (`id_card_patient`) REFERENCES `patients` (`id_card`),
ADD CONSTRAINT `fk_acthis_account` FOREIGN KEY (`usr_manager`) REFERENCES `accounts` (`usrname`);

--
-- Constraints for table `bought_pkg_history`
--
ALTER TABLE `bought_pkg_history`
ADD CONSTRAINT `fk_bpkghis_patient` FOREIGN KEY (`id_patient`) REFERENCES `patients` (`id`),
ADD CONSTRAINT `fk_bpkghis_pkg` FOREIGN KEY (`id_pkg`) REFERENCES `necessary_packages` (`id`);

--
-- Constraints for table `debt`
--
ALTER TABLE `debt`
ADD CONSTRAINT `debt_ibfk_1` FOREIGN KEY (`id_patient`) REFERENCES `accounts` (`id`);

--
-- Constraints for table `logon`
--
ALTER TABLE `logon`
ADD CONSTRAINT `FK_logon_acc` FOREIGN KEY (`id_patient`) REFERENCES `accounts` (`id`);

--
-- Constraints for table `management_history`
--
ALTER TABLE `management_history`
ADD CONSTRAINT `fk_mgmthis_patient` FOREIGN KEY (`id_patient`) REFERENCES `patients` (`id`),
ADD CONSTRAINT `fk_mgmthis_qrtpos` FOREIGN KEY (`id_qrt_pos`) REFERENCES `quarantinepos` (`id`);

--
-- Constraints for table `patients`
--
ALTER TABLE `patients`
ADD CONSTRAINT `fk_patients_acc` FOREIGN KEY (`id`) REFERENCES `accounts` (`id`),
ADD CONSTRAINT `fk_patients_prov` FOREIGN KEY (`id_prov`) REFERENCES `provinces` (`id`),
ADD CONSTRAINT `fk_patients_qrtpos` FOREIGN KEY (`id_pos`) REFERENCES `quarantinepos` (`id`),
ADD CONSTRAINT `fk_patients_town` FOREIGN KEY (`id_town`) REFERENCES `towns` (`id`),
ADD CONSTRAINT `fk_patients_vlg` FOREIGN KEY (`id_vlg`) REFERENCES `villages` (`id`);

--
-- Constraints for table `payment_acc`
--
ALTER TABLE `payment_acc`
ADD CONSTRAINT `FK_paymentAcc_Accounts` FOREIGN KEY (`id_acc`) REFERENCES `accounts` (`id`);

--
-- Constraints for table `provinces_towns`
--
ALTER TABLE `provinces_towns`
ADD CONSTRAINT `fk_provtown_prov` FOREIGN KEY (`id_prov`) REFERENCES `provinces` (`id`),
ADD CONSTRAINT `fk_provtown_town` FOREIGN KEY (`id_town`) REFERENCES `towns` (`id`);

--
-- Constraints for table `related_persons`
--
ALTER TABLE `related_persons`
ADD CONSTRAINT `fk_rltper_patient_from` FOREIGN KEY (`id_patient`) REFERENCES `patients` (`id`),
ADD CONSTRAINT `fk_rltper_patient_to` FOREIGN KEY (`id_related`) REFERENCES `patients` (`id`);

--
-- Constraints for table `towns_villages`
--
ALTER TABLE `towns_villages`
ADD CONSTRAINT `fk_townvlg_town` FOREIGN KEY (`id_town`) REFERENCES `towns` (`id`),
ADD CONSTRAINT `fk_townvlg_vlg` FOREIGN KEY (`id_vlg`) REFERENCES `villages` (`id`);

--
-- Constraints for table `transaction_history`
--
ALTER TABLE `transaction_history`
ADD CONSTRAINT `FK_transHis_fromIDAcc` FOREIGN KEY (`from_id_acc`) REFERENCES `accounts` (`id`),
ADD CONSTRAINT `FK_transHis_toIDAcc` FOREIGN KEY (`to_id_acc`) REFERENCES `accounts` (`id`);

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
