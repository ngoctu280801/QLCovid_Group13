-- phpMyAdmin SQL Dump
-- version 5.1.1
-- https://www.phpmyadmin.net/
--
-- Máy chủ: 127.0.0.1
-- Thời gian đã tạo: Th1 06, 2022 lúc 05:29 AM
-- Phiên bản máy phục vụ: 10.4.21-MariaDB
-- Phiên bản PHP: 8.0.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

CREATE DATABASE `qlcovid` CHARACTER SET utf8 COLLATE utf8_general_ci;
USE `qlcovid`;
--
-- Cơ sở dữ liệu: `qlcovid`
--

DELIMITER $$
--
-- Thủ tục
--
CREATE DEFINER=`root`@`localhost` PROCEDURE `addPatient` (IN `usrNameIn` VARCHAR(20), IN `fName` VARCHAR(50), IN `DOB` DATE, IN `idCard` VARCHAR(12), IN `qrtPos` VARCHAR(50), IN `stateF` VARCHAR(2), IN `prov` VARCHAR(50), IN `townN` VARCHAR(50), IN `vlg` VARCHAR(50), IN `usrManager` VARCHAR(20), OUT `code` INT)  BEGIN
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

CREATE DEFINER=`root`@`localhost` PROCEDURE `addPkg` (IN `pkgName` VARCHAR(50), IN `limitPerPerson` VARCHAR(20), IN `dateLimitIn` DATE, IN `priceIn` VARCHAR(20), IN `usrManager` VARCHAR(20), OUT `code` INT)  NO SQL
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

CREATE DEFINER=`root`@`localhost` PROCEDURE `addRelatedPerson` (IN `idCard` VARCHAR(12), IN `listRPer` MEDIUMTEXT, OUT `code` INT)  BEGIN

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

CREATE DEFINER=`root`@`localhost` PROCEDURE `changeQrtPos` (IN `userManager` VARCHAR(20), IN `id_card_patient` VARCHAR(12), IN `currQrtPos` VARCHAR(50), IN `newQrtPosId` INT(10), IN `newQrtPos` VARCHAR(50), OUT `code` INT)  NO SQL
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

CREATE DEFINER=`root`@`localhost` PROCEDURE `countChangedStatePatientsLastNDay` (IN `nDays` INT, OUT `changedStatePatients` TEXT, OUT `days` TEXT)  begin
	declare date_now date;
    declare temp_date date;
    declare i int;
    declare changed int;
    
    set date_now = now();
    set i = nDays - 1;
    
    select '' into changedStatePatients;
    select '' into days;
    
    iters: LOOP
    	if i < 0 then
        	leave iters;
        end if;
        
        set temp_date = date_now - interval i day;
        
        set changed = (select count(*) from activity_history where description like N'Chuyển% thành %' and date = temp_date);
      	        
        select concat(changedStatePatients, changed, ';') into changedStatePatients;
        select concat(days, (select date_format(temp_date, '%d/%m')), ';') into days;
        
        set i = i - 1;
    
    
    END LOOP;
    
	select LEFT(changedStatePatients, LENGTH(changedStatePatients) - 1) into changedStatePatients;

end$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `countCuredPatientsLastNDay` (IN `nDays` INT, OUT `curedPatients` TEXT, OUT `days` TEXT)  NO SQL
begin
	declare date_now date;
    declare temp_date date;
    declare i int;
    declare cured int;
    
    set date_now = now();
    set i = nDays - 1;
    
    select '' into curedPatients;
    select '' into days;
    
    iters: LOOP
    	if i < 0 then
        	leave iters;
        end if;
        
        set temp_date = date_now - interval i day;
        
        set cured = (select count(*) from activity_history where description like N'Chuyển % thành Khỏi bệnh' and date = temp_date);
      	        
        select concat(curedPatients, cured, ';') into curedPatients;
        select concat(days, (select date_format(temp_date, '%d/%m')), ';') into days;
        
        set i = i - 1;
    
    
    END LOOP;
    
	select LEFT(curedPatients, LENGTH(curedPatients) - 1) into curedPatients;

end$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `countDebt` (OUT `idCardList` TEXT, OUT `debtList` TEXT)  NO SQL
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

CREATE DEFINER=`root`@`localhost` PROCEDURE `countPatientLastNDay` (IN `nDays` INT, OUT `f0State` TEXT, OUT `f1State` TEXT, OUT `f2State` TEXT, OUT `f3State` TEXT, OUT `days` TEXT)  NO SQL
begin
	declare date_now date;
    declare temp_date date;
    declare i int;
    declare F0 int;
    declare F1 int;
    declare F2 int;
    declare F3 int;
    
    set date_now = now();
    set i = nDays - 1;
    
    select '' into f0State;
    select '' into f1State;
    select '' into f2State;
    select '' into f3State;
    select '' into days;
    
    iters: LOOP
    	if i < 0 then
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

CREATE DEFINER=`root`@`localhost` PROCEDURE `countPkgConsumed` (OUT `pkgNList` TEXT, OUT `quanList` TEXT)  NO SQL
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

CREATE DEFINER=`root`@`localhost` PROCEDURE `delPkg` (IN `pkgNList` TEXT, IN `usrManager` VARCHAR(20), OUT `code` INT)  NO SQL
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

CREATE DEFINER=`root`@`localhost` PROCEDURE `doTransaction` (IN `usrNameIn` VARCHAR(20), IN `creditIn` DECIMAL, OUT `code` INT)  NO SQL
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

CREATE DEFINER=`root`@`localhost` PROCEDURE `getAllPatients` ()  BEGIN
	SELECT p.id, p.full_name, p.id_card, p.date_of_birth, prov.name, t.name, v.name, qrt.name, p.state 
 	FROM patients p join provinces prov on prov.id = p.id_prov 
    	join towns t on p.id_town = t.id 
        join villages v on v.id = p.id_vlg 
        join quarantinepos qrt on qrt.id = p.id_pos;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `getNextRelatedPersonByIdCard` (IN `idCard` VARCHAR(12))  BEGIN
	SELECT p.full_name, p.id_card, p.date_of_birth, prov.name, t.name, v.name, p.state, qrt.name 
 	FROM related_persons rp join patients p on p.id = rp.id_related 
    	join provinces prov on p.id_prov = prov.id 
        join towns t on t.id = p.id_town 
        join villages v on v.id = p.id_vlg 
        join quarantinepos qrt on qrt.id = p.id_pos 
	WHERE rp.id_patient = (select id from patients where id_card = idCard limit 1);
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `getRelatedPersonBeforeByIdCard` (IN `idCard` VARCHAR(12))  NO SQL
BEGIN
	SELECT p.full_name, p.id_card, p.date_of_birth, prov.name, t.name, v.name, p.state, qrt.name 
 	FROM related_persons rp join patients p on p.id = rp.id_patient 
    	join provinces prov on p.id_prov = prov.id 
        join towns t on t.id = p.id_town 
        join villages v on v.id = p.id_vlg 
        join quarantinepos qrt on qrt.id = p.id_pos 
	WHERE rp.id_related = (select id from patients where id_card = idCard limit 1);
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `getTowns` (IN `provName` NVARCHAR(50))  BEGIN
	select t.name from provinces_towns pt join provinces p on pt.id_prov = p.id 
    		join towns t on t.id = pt.id_town 
    where p.name = provName
    order by t.name;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `getUsrInfoByIdCard` (IN `idCard` VARCHAR(12))  BEGIN
	select p.full_name, p.id_card, p.date_of_birth, v.name, t.name, prov.name, p.state, q.name, p.id
    from patients p join quarantinepos q on p.id_pos = q.id 
    	join provinces prov on prov.id = p.id_prov
        join towns t on t.id = p.id_town
        join villages v on v.id = p.id_vlg
    where p.id_card = idCard;
    
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `getVillages` (IN `provName` NVARCHAR(50), IN `townName` NVARCHAR(50))  BEGIN
	select v.name from towns_villages tv join villages v on tv.id_vlg = v.id 
    		join towns t on t.id = tv.id_town 
            join provinces_towns pt on pt.id_town = t.id
            join provinces p on p.id = pt.id_prov
    where t.name = townName and p.name = provName
    order by v.name;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `insertIntoBoughtPkgHis` (IN `idCard` VARCHAR(12), IN `listPkgN` MEDIUMTEXT, IN `listQuan` MEDIUMTEXT, OUT `code` INT)  NO SQL
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

CREATE DEFINER=`root`@`localhost` PROCEDURE `isFirstInit` ()  BEGIN
	SELECT count(*)
 	FROM accounts
	WHERE id_permission = 1;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `updatePatientState` (IN `idCard` VARCHAR(12), IN `stateInt` INT, IN `idRelated` INT, IN `usrManager` VARCHAR(20), OUT `code` INT)  NO SQL
begin
declare currentId int;
declare currentState varchar(9);
declare currentStateInt int;
declare date_now date;
declare desState varchar(9);
declare currentIdRelated int;
declare currentRelatedState varchar(2);
declare currentRelatedIdCard varchar(12);
declare counter int;
declare done1 int default false;
DECLARE done INT DEFAULT FALSE;
declare cur_id_related cursor for 
	select id_patient from related_persons where id_related = 
    (select id from patients where id_card = idCard limit 1);
    
declare cur_id_related_before cursor for
	select id_related from related_persons where id_patient = 
    (select id from patients where id_card = idCard limit 1);
    
DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

select -1 into code;

set currentId = (select id from patients where id_card = idCard limit 1);
set desState = concat('F', stateInt);
if stateInt = -1 then
	set desState = N'Khỏi bệnh';
end if;

set currentState = (select state from patients where id_card = idCard);
if currentState = N'Khỏi bệnh' then
	set currentStateInt = -1;
else
	set currentStateInt = RIGHT(currentState, 1);
end if;




if stateInt < currentStateInt and desState != currentState then
set date_now = now();
update patients set state = desState 
where id_card = idCard;

insert into activity_history 
values (null, usrManager, date_now, idCard, concat('Chuyển ', idCard, ' từ ', currentState, ' thành ', desState));


-- if stateInt = -1 then
	-- select concat('Chuyen CMND', idCard, ' tu ', currentState,' thanh khoi benh');
    
    
-- else








if stateInt != -1 then
open cur_id_related_before;
iters1: loop

	fetch cur_id_related_before into currentIdRelated;
    
    IF done THEN
    	LEAVE iters1;
	end if;
    
    set currentRelatedIdCard = (select id_card from patients 
                                       where id = currentIdRelated);
    
	call updatePatientState(currentRelatedIdCard, stateInt + 1, currentId, usrManager, code);
    
end loop;
close cur_id_related_before;
set done = false;
end if;

delete from related_persons where id_patient = currentId;

if stateInt != -1 then
	-- select concat('Chuyen CMND ', idCard, ' tu ', currentState, ' thanh ', desState);
    if idRelated != -1 then
    	-- select concat('va ghi log: la ', desState, ' cua ', idRelated);
        
        insert into related_persons(id_patient, id_related)
        values (currentId, idRelated);
    end if;
end if;





open cur_id_related;
iters: loop
	fetch cur_id_related into currentIdRelated;
	IF done THEN
    	LEAVE iters;
	end if;
    
    -- Cac F1, F2, F3 -> Khoi benh
    set counter = (select count(*) from related_persons where id_patient = currentIdRelated);
    
    set currentRelatedIdCard = (select id_card from patients 
                                       where id = currentIdRelated);
    
    if stateInt = -1 then
    	if counter > 1 then
    		-- select concat('Giu nguyen trang thai cua ', currentIdRelated);
        	-- select concat('Xoa dong trong bang related_persons where ', 'id_patient = ', currentIdRelated, ' and id_related = ', currentId );
        	delete from related_persons 
            where id_patient = currentIdRelated and id_related = currentId;
        
        else
        	
        	-- select concat('De quy ham xuong idCard = ',currentRelatedIdCard, ' ve khoi benh');
            call updatePatientState(currentRelatedIdCard, -1, -1, usrManager, code);
        end if;
    else 
    	-- select concat('De quy ham xuong idCard = ', currentRelatedIdCard, '  ve F', stateInt + 1);
        call updatePatientState(currentRelatedIdCard, stateInt + 1, currentId, usrManager, code);
    end if;

end loop;

close cur_id_related;

end if; -- end if concat(stateInt) < currentState and desState != currentState then

select 1 into code;

end$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `updatePkg` (IN `pkgName` VARCHAR(50), IN `limitPerPerson` VARCHAR(20), IN `dateLimitIn` DATE, IN `priceIn` VARCHAR(20), IN `usrManager` VARCHAR(20), IN `newPkgN` VARCHAR(50), OUT `code` INT)  NO SQL
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
-- Cấu trúc bảng cho bảng `accounts`
--

CREATE TABLE `accounts` (
  `id` int(10) UNSIGNED ZEROFILL NOT NULL,
  `usrname` varchar(20) CHARACTER SET ascii NOT NULL,
  `pwd` varchar(50) CHARACTER SET ascii NOT NULL,
  `id_permission` int(11) NOT NULL,
  `is_locked` tinyint(1) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Đang đổ dữ liệu cho bảng `accounts`
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
(0000000040, 'test20', '3440400d53523c54a5f5c142f828afe7e2326d20dfe8bd3c0d', 2, 0),
(0000000041, 'test21', '3440400d53523c54a5f5c142f828afe7e2326d20dfe8bd3c0d', 2, 0),
(0000000042, 'tu', '3440400d53523c54a5f5c142f828afe7e2326d20dfe8bd3c0d', 2, 0),
(0000000043, 'van', '3440400d53523c54a5f5c142f828afe7e2326d20dfe8bd3c0d', 2, 0),
(0000000044, 'hai', '3440400d53523c54a5f5c142f828afe7e2326d20dfe8bd3c0d', 2, 0),
(0000000045, 'duy', '3440400d53523c54a5f5c142f828afe7e2326d20dfe8bd3c0d', 2, 0),
(0000000046, 'test22', '3440400d53523c54a5f5c142f828afe7e2326d20dfe8bd3c0d', 2, 0),
(0000000047, 'tuf0', '3440400d53523c54a5f5c142f828afe7e2326d20dfe8bd3c0d', 2, 0),
(0000000048, 'vanf1', '3440400d53523c54a5f5c142f828afe7e2326d20dfe8bd3c0d', 2, 0),
(0000000049, 'haif0', '3440400d53523c54a5f5c142f828afe7e2326d20dfe8bd3c0d', 2, 0),
(0000000050, 'af1', '3440400d53523c54a5f5c142f828afe7e2326d20dfe8bd3c0d', 2, 0),
(0000000051, 'bf1', '3440400d53523c54a5f5c142f828afe7e2326d20dfe8bd3c0d', 2, 0),
(0000000052, 'cf2', '3440400d53523c54a5f5c142f828afe7e2326d20dfe8bd3c0d', 2, 0),
(0000000053, 'df3', '3440400d53523c54a5f5c142f828afe7e2326d20dfe8bd3c0d', 2, 0),
(0000000054, 'manager4', '3440400d53523c54a5f5c142f828afe7e2326d20dfe8bd3c0d', 0, 0),
(0000000055, 'test23', '3440400d53523c54a5f5c142f828afe7e2326d20dfe8bd3c0d', 2, 0);

--
-- Bẫy `accounts`
--
DELIMITER $$
CREATE TRIGGER `addNewDebtAndPaymentAcc` AFTER INSERT ON `accounts` FOR EACH ROW BEGIN
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
$$
DELIMITER ;
DELIMITER $$
CREATE TRIGGER `isFirstLogin` AFTER UPDATE ON `accounts` FOR EACH ROW begin
	declare isExisted int;
    set isExisted = (select count(*) from logon where id_patient = old.id );
    if old.id_permission = 2 and isExisted = 0 then

        insert into logon set id_patient = old.id;

    end if;




end
$$
DELIMITER ;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `activity_history`
--

CREATE TABLE `activity_history` (
  `id` int(10) UNSIGNED ZEROFILL NOT NULL,
  `usr_manager` varchar(20) CHARACTER SET ascii NOT NULL,
  `date` date NOT NULL,
  `id_card_patient` varchar(12) DEFAULT NULL,
  `description` varchar(200) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Đang đổ dữ liệu cho bảng `activity_history`
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
(0000000047, 'manager', '2021-11-17', '200120011', 'Thêm 200120011 làm F0 tại Bệnh viện Bạch Mai'),
(0000000048, 'manager', '2021-11-23', '210119999', 'Thêm 210119999 làm F2 tại Bệnh viện Bạch Mai'),
(0000000049, 'manager', '2021-11-24', '010120011', 'Thêm 010120011 làm F0 tại Bệnh viện Bạch Mai'),
(0000000050, 'manager', '2021-11-25', '020120011', 'Thêm 020120011 làm F1 tại Bệnh viện Bạch Mai'),
(0000000051, 'manager', '2021-11-25', '030120011', 'Thêm 030120011 làm F2 tại Bệnh viện Bạch Mai'),
(0000000052, 'manager', '2021-11-25', '040120011', 'Thêm 040120011 làm F3 tại Bệnh viện Bạch Mai'),
(0000000053, 'manager', '2021-11-25', '221219922', 'Thêm 221219922 làm F2 tại Bệnh viện Bạch Mai'),
(0000000054, 'manager', '2021-11-25', '010120012', 'Thêm 010120012 làm F0 tại Bệnh viện Bạch Mai'),
(0000000055, 'manager', '2021-11-25', '020120012', 'Thêm 020120012 làm F1 tại Bệnh viện Bạch Mai'),
(0000000056, 'manager', '2021-11-25', '030120012', 'Thêm 030120012 làm F0 tại Bệnh viện Bạch Mai'),
(0000000057, 'manager', '2021-11-25', '010111111', 'Thêm 010111111 làm F1 tại Bệnh viện Bạch Mai'),
(0000000058, 'manager', '2021-11-25', '020111111', 'Thêm 020111111 làm F1 tại Bệnh viện Bạch Mai'),
(0000000059, 'manager', '2021-11-25', '030111111', 'Thêm 030111111 làm F2 tại Bệnh viện Bạch Mai'),
(0000000060, 'manager', '2021-11-25', '040111111', 'Thêm 040111111 làm F3 tại Bệnh viện Bạch Mai'),
(0000000069, 'manager', '2021-11-26', '040111111', 'Chuyển 040111111 từ F3 thành F0'),
(0000000070, 'manager', '2021-11-26', '040111111', 'Chuyển 040111111 từ F3 thành F0'),
(0000000071, 'manager', '2021-11-26', '040111111', 'Chuyển 040111111 từ F3 thành F0'),
(0000000072, 'manager', '2021-11-26', '040111111', 'Chuyển 040111111 từ F3 thành F0'),
(0000000073, 'manager', '2021-11-26', '040111111', 'Chuyển 040111111 từ F3 thành F0'),
(0000000074, 'manager', '2021-11-26', '040111111', 'Chuyển 040111111 từ F3 thành F0'),
(0000000075, 'manager', '2021-11-26', '040111111', 'Chuyển 040111111 từ F3 thành F0'),
(0000000076, 'manager', '2021-11-26', '040111111', 'Chuyển 040111111 từ F3 thành F0'),
(0000000077, 'manager', '2021-11-26', '030111111', 'Chuyển 030111111 từ F2 thành F1'),
(0000000078, 'manager', '2021-11-26', '040111111', 'Chuyển 040111111 từ F3 thành F0'),
(0000000079, 'manager', '2021-11-26', '030111111', 'Chuyển 030111111 từ F2 thành F1'),
(0000000080, 'manager', '2021-11-26', '010120012', 'Chuyển 010120012 từ F0 thành Khỏi bệnh'),
(0000000081, 'manager', '2021-11-26', '020120011', 'Chuyển 020120011 từ F1 thành F0'),
(0000000082, 'manager', '2021-11-26', '030120011', 'Chuyển 030120011 từ F2 thành F1'),
(0000000083, 'manager', '2021-11-26', '040120011', 'Chuyển 040120011 từ F3 thành F2'),
(0000000084, 'manager', '2021-11-26', '040111111', 'Chuyển 040111111 từ F3 thành F0'),
(0000000085, 'manager', '2021-11-26', '030111111', 'Chuyển 030111111 từ F2 thành F1'),
(0000000086, 'manager', '2021-11-26', '040111111', 'Chuyển 040111111 từ F3 thành F0'),
(0000000087, 'manager', '2021-11-26', '030111111', 'Chuyển 030111111 từ F2 thành F1'),
(0000000088, 'manager', '2021-12-27', '010111111', 'Chuyển 010111111 từ F1 thành Khỏi bệnh'),
(0000000089, 'manager', '2021-12-27', '999999999', 'Chuyển 999999999 từ F0 thành Khỏi bệnh'),
(0000000090, 'manager', '2021-12-27', '123456789', 'Chuyển 123456789 từ F0 thành Khỏi bệnh'),
(0000000091, 'manager', '2021-12-29', '211219922', 'Thêm 211219922 làm F0 tại Bệnh viện Bạch Mai'),
(0000000092, 'manager', '2021-12-29', '444444444', 'Chuyển 444444444 từ F1 thành Khỏi bệnh'),
(0000000093, 'manager', '2021-12-29', '210119999', 'Chuyển 210119999 từ F2 thành Khỏi bệnh'),
(0000000094, 'manager', '2022-01-06', NULL, 'Xoá gói PACKAGE TEST2'),
(0000000095, 'manager', '2022-01-06', NULL, 'Xoá gói PACKAGE TEST'),
(0000000096, 'manager', '2022-01-06', NULL, 'Xoá gói PACKAGE TEST3'),
(0000000097, 'manager', '2022-01-06', NULL, 'Thêm gói KHAU TRANG, hạn mức 10 gói/ người, bán đến hết ngày 2023-02-22 với giá 10,000 VNĐ'),
(0000000098, 'manager', '2022-01-06', NULL, 'Thêm gói KHU KHUAN, hạn mức 10 gói/ người, bán đến hết ngày 2023-02-20 với giá 100,000 VNĐ'),
(0000000099, 'manager', '2022-01-06', NULL, 'Thêm gói KIT TEST, hạn mức 10 gói/ người, bán đến hết ngày 2023-02-20 với giá 120,000 VNĐ'),
(0000000100, 'manager', '2022-01-06', NULL, 'Xoá gói KHU KHUAN'),
(0000000101, 'manager', '2022-01-06', NULL, 'Xoá gói KIT TEST');

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `bought_pkg_history`
--

CREATE TABLE `bought_pkg_history` (
  `id` int(10) UNSIGNED ZEROFILL NOT NULL,
  `id_patient` int(10) UNSIGNED ZEROFILL NOT NULL,
  `id_pkg` int(10) UNSIGNED ZEROFILL NOT NULL,
  `quantity` int(10) UNSIGNED NOT NULL,
  `date` date NOT NULL,
  `price` decimal(10,0) UNSIGNED NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Đang đổ dữ liệu cho bảng `bought_pkg_history`
--

INSERT INTO `bought_pkg_history` (`id`, `id_patient`, `id_pkg`, `quantity`, `date`, `price`) VALUES
(0000000002, 0000000025, 0000000003, 2, '2021-11-14', '138000'),
(0000000003, 0000000025, 0000000003, 1, '2021-11-14', '69000'),
(0000000010, 0000000025, 0000000002, 2, '2021-11-14', '38000'),
(0000000011, 0000000025, 0000000003, 1, '2021-11-14', '69000'),
(0000000012, 0000000025, 0000000003, 1, '2021-11-14', '69000'),
(0000000013, 0000000036, 0000000004, 2, '2021-11-15', '38000');

--
-- Bẫy `bought_pkg_history`
--
DELIMITER $$
CREATE TRIGGER `checkDebtWhenBuyPkg` AFTER INSERT ON `bought_pkg_history` FOR EACH ROW BEGIN
	declare credit decimal;
    set credit = (select debt from debt where id_patient = new.id_patient limit 1);
    set credit = credit + new.price;
	
    update debt set debt = credit where id_patient = new.id_patient;

END
$$
DELIMITER ;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `debt`
--

CREATE TABLE `debt` (
  `id_patient` int(10) UNSIGNED ZEROFILL NOT NULL,
  `debt` int(10) UNSIGNED NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Đang đổ dữ liệu cho bảng `debt`
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
(0000000040, 420000),
(0000000041, 0),
(0000000042, 0),
(0000000043, 0),
(0000000044, 0),
(0000000045, 0),
(0000000046, 0),
(0000000047, 0),
(0000000048, 0),
(0000000049, 0),
(0000000050, 0),
(0000000051, 0),
(0000000052, 0),
(0000000053, 0),
(0000000055, 0);

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `logon`
--

CREATE TABLE `logon` (
  `id_patient` int(10) UNSIGNED ZEROFILL NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Đang đổ dữ liệu cho bảng `logon`
--

INSERT INTO `logon` (`id_patient`) VALUES
(0000000007),
(0000000024),
(0000000025),
(0000000040);

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `management_history`
--

CREATE TABLE `management_history` (
  `id` int(10) UNSIGNED ZEROFILL NOT NULL,
  `id_patient` int(10) UNSIGNED ZEROFILL NOT NULL,
  `date` date NOT NULL,
  `state` varchar(9) NOT NULL,
  `id_qrt_pos` int(10) UNSIGNED ZEROFILL NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Đang đổ dữ liệu cho bảng `management_history`
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
(0000000021, 0000000040, '2021-11-17', 'F0', 0000000001),
(0000000022, 0000000041, '2021-11-23', 'F2', 0000000001),
(0000000023, 0000000042, '2021-11-24', 'F0', 0000000001),
(0000000024, 0000000043, '2021-11-25', 'F1', 0000000001),
(0000000025, 0000000044, '2021-11-25', 'F2', 0000000001),
(0000000026, 0000000045, '2021-11-25', 'F3', 0000000001),
(0000000027, 0000000046, '2021-11-25', 'F2', 0000000001),
(0000000028, 0000000047, '2021-11-25', 'F0', 0000000001),
(0000000029, 0000000048, '2021-11-25', 'F1', 0000000001),
(0000000032, 0000000049, '2021-11-25', 'F0', 0000000001),
(0000000034, 0000000050, '2021-11-25', 'F1', 0000000001),
(0000000035, 0000000051, '2021-11-25', 'F1', 0000000001),
(0000000038, 0000000052, '2021-11-25', 'F2', 0000000001),
(0000000041, 0000000053, '2021-11-25', 'F3', 0000000001),
(0000000055, 0000000053, '2021-11-26', 'F0', 0000000001),
(0000000056, 0000000053, '2021-11-26', 'F3', 0000000001),
(0000000057, 0000000053, '2021-11-26', 'F0', 0000000001),
(0000000058, 0000000053, '2021-11-26', 'F3', 0000000001),
(0000000059, 0000000053, '2021-11-26', 'F0', 0000000001),
(0000000060, 0000000053, '2021-11-26', 'F3', 0000000001),
(0000000061, 0000000053, '2021-11-26', 'F0', 0000000001),
(0000000062, 0000000053, '2021-11-26', 'F3', 0000000001),
(0000000063, 0000000053, '2021-11-26', 'F0', 0000000001),
(0000000064, 0000000053, '2021-11-26', 'F3', 0000000001),
(0000000065, 0000000053, '2021-11-26', 'F0', 0000000001),
(0000000066, 0000000053, '2021-11-26', 'F3', 0000000001),
(0000000067, 0000000053, '2021-11-26', 'F0', 0000000001),
(0000000068, 0000000053, '2021-11-26', 'F3', 0000000001),
(0000000069, 0000000053, '2021-11-26', 'F0', 0000000001),
(0000000070, 0000000052, '2021-11-26', 'F1', 0000000001),
(0000000071, 0000000053, '2021-11-26', 'F3', 0000000001),
(0000000072, 0000000052, '2021-11-26', 'F2', 0000000001),
(0000000073, 0000000053, '2021-11-26', 'F0', 0000000001),
(0000000074, 0000000052, '2021-11-26', 'F1', 0000000001),
(0000000075, 0000000047, '2021-11-26', 'Khỏi bệnh', 0000000001),
(0000000076, 0000000043, '2021-11-26', 'F0', 0000000001),
(0000000077, 0000000044, '2021-11-26', 'F1', 0000000001),
(0000000078, 0000000045, '2021-11-26', 'F2', 0000000001),
(0000000079, 0000000052, '2021-11-26', 'F2', 0000000001),
(0000000080, 0000000053, '2021-11-26', 'F3', 0000000001),
(0000000081, 0000000053, '2021-11-26', 'F0', 0000000001),
(0000000082, 0000000052, '2021-11-26', 'F1', 0000000001),
(0000000083, 0000000053, '2021-11-26', 'F3', 0000000001),
(0000000084, 0000000052, '2021-11-26', 'F2', 0000000001),
(0000000085, 0000000053, '2021-11-26', 'F0', 0000000001),
(0000000086, 0000000052, '2021-11-26', 'F1', 0000000001),
(0000000087, 0000000050, '2021-12-27', 'Khỏi bệnh', 0000000001),
(0000000088, 0000000028, '2021-12-27', 'Khỏi bệnh', 0000000001),
(0000000089, 0000000007, '2021-12-27', 'Khỏi bệnh', 0000000001),
(0000000090, 0000000055, '2021-12-29', 'F0', 0000000001),
(0000000091, 0000000019, '2021-12-29', 'Khỏi bệnh', 0000000001),
(0000000092, 0000000041, '2021-12-29', 'Khỏi bệnh', 0000000001);

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `necessary_packages`
--

CREATE TABLE `necessary_packages` (
  `id` int(10) UNSIGNED ZEROFILL NOT NULL,
  `pkg_name` varchar(50) NOT NULL,
  `limit_quantity_per_person` int(10) UNSIGNED NOT NULL,
  `date_limit` date NOT NULL,
  `price` decimal(10,0) UNSIGNED NOT NULL,
  `is_deleted` int(11) NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Đang đổ dữ liệu cho bảng `necessary_packages`
--

INSERT INTO `necessary_packages` (`id`, `pkg_name`, `limit_quantity_per_person`, `date_limit`, `price`, `is_deleted`) VALUES
(0000000002, 'PACKAGE TEST', 3, '2021-12-14', '19000', 1),
(0000000003, 'PACKAGE TEST2', 5, '2021-11-16', '69000', 1),
(0000000004, 'PACKAGE TEST3', 3, '2021-11-16', '19000', 1),
(0000000005, 'PACKAGE TEST4', 6, '2021-11-16', '19000', 1),
(0000000008, 'PACKAGE TEST3', 3, '2021-11-15', '19000', 1),
(0000000009, 'KHAU TRANG', 10, '2023-02-22', '10000', 0),
(0000000010, 'KHU KHUAN', 10, '2023-02-20', '100000', 1),
(0000000011, 'KIT TEST', 10, '2023-02-20', '120000', 1);

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `patients`
--

CREATE TABLE `patients` (
  `id` int(10) UNSIGNED ZEROFILL NOT NULL,
  `full_name` varchar(50) NOT NULL,
  `id_card` varchar(12) NOT NULL,
  `date_of_birth` date NOT NULL,
  `id_prov` int(10) UNSIGNED ZEROFILL NOT NULL,
  `id_town` int(10) UNSIGNED ZEROFILL NOT NULL,
  `id_vlg` int(10) UNSIGNED ZEROFILL NOT NULL,
  `state` varchar(9) NOT NULL,
  `id_pos` int(10) UNSIGNED ZEROFILL NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Đang đổ dữ liệu cho bảng `patients`
--

INSERT INTO `patients` (`id`, `full_name`, `id_card`, `date_of_birth`, `id_prov`, `id_town`, `id_vlg`, `state`, `id_pos`) VALUES
(0000000007, 'OKLA', '123456789', '1991-03-12', 0000000001, 0000000001, 0000000001, 'Khỏi bệnh', 0000000001),
(0000000013, 'Test cái procedure', '987651234', '2012-12-31', 0000000001, 0000000001, 0000000001, 'F0', 0000000001),
(0000000014, 'Lại là Testt', '222222222', '2012-12-31', 0000000001, 0000000001, 0000000001, 'F1', 0000000001),
(0000000019, 'TEST4', '444444444', '1992-12-13', 0000000001, 0000000001, 0000000001, 'Khỏi bệnh', 0000000001),
(0000000021, 'Đây Là Test5', '555111555', '1991-07-09', 0000000001, 0000000001, 0000000001, 'F1', 0000000001),
(0000000023, 'Test test6', '444455555', '1992-11-13', 0000000001, 0000000001, 0000000001, 'F0', 0000000001),
(0000000024, 'ĐÂY LÀ TEST 7', '111111119', '1991-11-11', 0000000001, 0000000001, 0000000001, 'F2', 0000000001),
(0000000025, 'ĐÂY LÀ NGTEST', '987123546', '1991-11-11', 0000000001, 0000000001, 0000000001, 'F0', 0000000001),
(0000000028, 'TESTTTTTTTTTT', '999999999', '2000-09-09', 0000000001, 0000000001, 0000000001, 'Khỏi bệnh', 0000000001),
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
(0000000040, 'ĐÂY LÀ TEST HAI MƯƠI', '200120011', '2001-01-20', 0000000001, 0000000001, 0000000001, 'F0', 0000000001),
(0000000041, 'ĐÂY LÀ TEST HAI MỐT', '210119999', '1999-01-21', 0000000001, 0000000001, 0000000001, 'Khỏi bệnh', 0000000001),
(0000000042, 'TÚ', '010120011', '2001-01-01', 0000000001, 0000000001, 0000000001, 'F0', 0000000001),
(0000000043, 'VĂN', '020120011', '2001-01-02', 0000000001, 0000000001, 0000000001, 'F0', 0000000001),
(0000000044, 'HẢI', '030120011', '2001-01-03', 0000000001, 0000000001, 0000000001, 'F1', 0000000001),
(0000000045, 'DUY', '040120011', '2001-01-04', 0000000001, 0000000001, 0000000001, 'F2', 0000000001),
(0000000046, 'TEST HAI HAI', '221219922', '1992-12-22', 0000000001, 0000000001, 0000000001, 'F2', 0000000001),
(0000000047, 'TÚ F0', '010120012', '2001-01-01', 0000000001, 0000000001, 0000000001, 'Khỏi bệnh', 0000000001),
(0000000048, 'VĂN F1', '020120012', '2001-01-02', 0000000001, 0000000001, 0000000001, 'F1', 0000000001),
(0000000049, 'HẢI F0', '030120012', '2001-01-03', 0000000001, 0000000001, 0000000001, 'F0', 0000000001),
(0000000050, 'A F1', '010111111', '1111-01-01', 0000000001, 0000000001, 0000000001, 'Khỏi bệnh', 0000000001),
(0000000051, 'B F1', '020111111', '1111-01-02', 0000000001, 0000000001, 0000000001, 'F1', 0000000001),
(0000000052, 'C F2 LIÊN QUAN A VÀ B', '030111111', '1111-01-03', 0000000001, 0000000001, 0000000001, 'F1', 0000000001),
(0000000053, 'D F3 LIÊN QUAN C', '040111111', '1111-01-04', 0000000001, 0000000001, 0000000001, 'F0', 0000000001),
(0000000055, 'TEST THỨ HAI BA', '211219922', '1992-12-21', 0000000001, 0000000001, 0000000001, 'F0', 0000000001);

--
-- Bẫy `patients`
--
DELIMITER $$
CREATE TRIGGER `addMngmHis_insert` AFTER INSERT ON `patients` FOR EACH ROW BEGIN
	
 declare date_now date;
set date_now = now();
 INSERT INTO management_history(id_patient, date, state, id_qrt_pos)
 values (new.id, date_now, new.state, new.id_pos);

END
$$
DELIMITER ;
DELIMITER $$
CREATE TRIGGER `addMngmHis_update` AFTER UPDATE ON `patients` FOR EACH ROW BEGIN
    	declare date_now date;
        set date_now = now();
        if new.state != old.state or new.id_pos != old.id_pos then
        INSERT INTO management_history(id_patient,date, state, id_qrt_pos)
        VALUES (new.id, date_now, new.state, new.id_pos);
    	end if;
END
$$
DELIMITER ;
DELIMITER $$
CREATE TRIGGER `checkWhenAddPatient` BEFORE INSERT ON `patients` FOR EACH ROW BEGIN
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
$$
DELIMITER ;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `payment_acc`
--

CREATE TABLE `payment_acc` (
  `id_acc` int(10) UNSIGNED ZEROFILL NOT NULL,
  `balance` decimal(10,0) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Đang đổ dữ liệu cho bảng `payment_acc`
--

INSERT INTO `payment_acc` (`id_acc`, `balance`) VALUES
(0000000020, '80000'),
(0000000039, '10000000'),
(0000000040, '9920000'),
(0000000041, '10000000'),
(0000000042, '10000000'),
(0000000043, '10000000'),
(0000000044, '10000000'),
(0000000045, '10000000'),
(0000000046, '10000000'),
(0000000047, '10000000'),
(0000000048, '10000000'),
(0000000049, '10000000'),
(0000000050, '10000000'),
(0000000051, '10000000'),
(0000000052, '10000000'),
(0000000053, '10000000'),
(0000000055, '10000000');

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `provinces`
--

CREATE TABLE `provinces` (
  `id` int(10) UNSIGNED ZEROFILL NOT NULL,
  `name` varchar(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Đang đổ dữ liệu cho bảng `provinces`
--

INSERT INTO `provinces` (`id`, `name`) VALUES
(0000000001, 'Hồ Chí Minh'),
(0000000002, 'Hà Nội'),
(0000000003, 'Long An'),
(0000000004, 'Đồng Nai'),
(0000000005, 'Bình Định');

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `provinces_towns`
--

CREATE TABLE `provinces_towns` (
  `id_prov` int(10) UNSIGNED ZEROFILL NOT NULL,
  `id_town` int(10) UNSIGNED ZEROFILL NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Đang đổ dữ liệu cho bảng `provinces_towns`
--

INSERT INTO `provinces_towns` (`id_prov`, `id_town`) VALUES
(0000000001, 0000000001),
(0000000001, 0000000002),
(0000000001, 0000000003),
(0000000001, 0000000004),
(0000000001, 0000000005),
(0000000002, 0000000006),
(0000000002, 0000000007),
(0000000002, 0000000008),
(0000000002, 0000000009),
(0000000002, 0000000010),
(0000000003, 0000000011),
(0000000003, 0000000012),
(0000000003, 0000000013),
(0000000003, 0000000014),
(0000000003, 0000000015),
(0000000004, 0000000016),
(0000000004, 0000000017),
(0000000004, 0000000018),
(0000000004, 0000000019),
(0000000004, 0000000020),
(0000000005, 0000000021),
(0000000005, 0000000022),
(0000000005, 0000000023),
(0000000005, 0000000024),
(0000000005, 0000000025);

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `quarantinepos`
--

CREATE TABLE `quarantinepos` (
  `id` int(10) UNSIGNED ZEROFILL NOT NULL,
  `name` varchar(50) NOT NULL,
  `capacity` int(10) UNSIGNED ZEROFILL NOT NULL,
  `current_capacity` int(10) UNSIGNED ZEROFILL NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Đang đổ dữ liệu cho bảng `quarantinepos`
--

INSERT INTO `quarantinepos` (`id`, `name`, `capacity`, `current_capacity`) VALUES
(0000000001, 'Bệnh viện Bạch Mai', 0000001000, 0000000925),
(0000000002, 'Bệnh viện Gia An 115', 0000000900, 0000000104);

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `related_persons`
--

CREATE TABLE `related_persons` (
  `id_patient` int(10) UNSIGNED ZEROFILL NOT NULL,
  `id_related` int(10) UNSIGNED ZEROFILL NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Đang đổ dữ liệu cho bảng `related_persons`
--

INSERT INTO `related_persons` (`id_patient`, `id_related`) VALUES
(0000000014, 0000000013),
(0000000021, 0000000023),
(0000000024, 0000000021),
(0000000035, 0000000034),
(0000000036, 0000000034),
(0000000044, 0000000043),
(0000000045, 0000000044),
(0000000046, 0000000021),
(0000000048, 0000000049),
(0000000051, 0000000030),
(0000000052, 0000000053);

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `towns`
--

CREATE TABLE `towns` (
  `id` int(10) UNSIGNED ZEROFILL NOT NULL,
  `name` varchar(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Đang đổ dữ liệu cho bảng `towns`
--

INSERT INTO `towns` (`id`, `name`) VALUES
(0000000001, 'Quận 1'),
(0000000002, 'Quận 3'),
(0000000003, 'Quận 5'),
(0000000004, 'Quận 10'),
(0000000005, 'Quận Bình Thạnh'),
(0000000006, 'Từ Liêm'),
(0000000007, 'Hà Đông'),
(0000000008, 'Ba Đình'),
(0000000009, 'Cầu Giấy'),
(0000000010, 'Hoàn Kiếm'),
(0000000011, 'Cần Đước'),
(0000000012, 'Tân An'),
(0000000013, 'Cần Giuộc'),
(0000000014, 'Bến Lức'),
(0000000015, 'Đức Hòa'),
(0000000016, 'Thống Nhất'),
(0000000017, 'Biên Hòa'),
(0000000018, 'Định Quán'),
(0000000019, 'Long Khánh'),
(0000000020, 'Long Thành'),
(0000000021, 'Quy Nhơn'),
(0000000022, 'Tây Sơn'),
(0000000023, 'An Nhơn'),
(0000000024, 'Phù Cát'),
(0000000025, 'Hoài Nhơn');

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `towns_villages`
--

CREATE TABLE `towns_villages` (
  `id_town` int(10) UNSIGNED ZEROFILL NOT NULL,
  `id_vlg` int(10) UNSIGNED ZEROFILL NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Đang đổ dữ liệu cho bảng `towns_villages`
--

INSERT INTO `towns_villages` (`id_town`, `id_vlg`) VALUES
(0000000001, 0000000001),
(0000000001, 0000000002),
(0000000001, 0000000003),
(0000000001, 0000000004),
(0000000001, 0000000005),
(0000000002, 0000000006),
(0000000002, 0000000007),
(0000000002, 0000000008),
(0000000002, 0000000009),
(0000000002, 0000000010),
(0000000003, 0000000011),
(0000000003, 0000000012),
(0000000003, 0000000013),
(0000000003, 0000000014),
(0000000003, 0000000015),
(0000000004, 0000000016),
(0000000004, 0000000017),
(0000000004, 0000000018),
(0000000004, 0000000019),
(0000000004, 0000000020),
(0000000005, 0000000021),
(0000000005, 0000000022),
(0000000005, 0000000023),
(0000000005, 0000000024),
(0000000005, 0000000025);

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `transaction_history`
--

CREATE TABLE `transaction_history` (
  `id` int(10) UNSIGNED ZEROFILL NOT NULL,
  `from_id_acc` int(10) UNSIGNED ZEROFILL NOT NULL,
  `to_id_acc` int(10) UNSIGNED ZEROFILL NOT NULL,
  `credit` decimal(10,0) UNSIGNED NOT NULL,
  `date_trans` timestamp NOT NULL DEFAULT current_timestamp(),
  `remaining_debt` decimal(10,0) UNSIGNED NOT NULL,
  `remaining_balance` decimal(10,0) UNSIGNED NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Đang đổ dữ liệu cho bảng `transaction_history`
--

INSERT INTO `transaction_history` (`id`, `from_id_acc`, `to_id_acc`, `credit`, `date_trans`, `remaining_debt`, `remaining_balance`) VALUES
(0000000002, 0000000040, 0000000020, '20000', '2021-11-17 09:44:11', '480000', '9980000'),
(0000000003, 0000000040, 0000000020, '20000', '2021-11-17 10:11:56', '460000', '9960000'),
(0000000004, 0000000040, 0000000020, '20000', '2021-11-17 11:00:00', '440000', '9940000'),
(0000000005, 0000000040, 0000000020, '20000', '2021-11-23 14:16:15', '420000', '9920000');

--
-- Bẫy `transaction_history`
--
DELIMITER $$
CREATE TRIGGER `updateBalanceAndDebt` BEFORE INSERT ON `transaction_history` FOR EACH ROW begin
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
$$
DELIMITER ;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `villages`
--

CREATE TABLE `villages` (
  `id` int(10) UNSIGNED ZEROFILL NOT NULL,
  `name` varchar(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Đang đổ dữ liệu cho bảng `villages`
--

INSERT INTO `villages` (`id`, `name`) VALUES
(0000000001, 'Phường Bến Nghé'),
(0000000002, 'Phường Bến Thành'),
(0000000003, 'Phường Đa Kao'),
(0000000004, 'Phường Nguyễn Cư Trinh'),
(0000000005, 'Phường Nguyễn Thái Bình'),
(0000000006, 'Phường 1'),
(0000000007, 'Phường 2'),
(0000000008, 'Phường 3'),
(0000000009, 'Phường 4'),
(0000000010, 'Phường 5'),
(0000000011, 'Phường 1'),
(0000000012, 'Phường 2'),
(0000000013, 'Phường 3'),
(0000000014, 'Phường 4'),
(0000000015, 'Phường 5'),
(0000000016, 'Phường 1'),
(0000000017, 'Phường 2'),
(0000000018, 'Phường 3'),
(0000000019, 'Phường 4'),
(0000000020, 'Phường 5'),
(0000000021, 'Phường 1'),
(0000000022, 'Phường 2'),
(0000000023, 'Phường 3'),
(0000000024, 'Phường 5'),
(0000000025, 'Phường 6');

--
-- Chỉ mục cho các bảng đã đổ
--

--
-- Chỉ mục cho bảng `accounts`
--
ALTER TABLE `accounts`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `usrname` (`usrname`);

--
-- Chỉ mục cho bảng `activity_history`
--
ALTER TABLE `activity_history`
  ADD PRIMARY KEY (`id`),
  ADD KEY `FK_actHis_patients` (`id_card_patient`),
  ADD KEY `fk_acthis_account` (`usr_manager`);

--
-- Chỉ mục cho bảng `bought_pkg_history`
--
ALTER TABLE `bought_pkg_history`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_bpkghis_patient` (`id_patient`),
  ADD KEY `fk_bpkghis_pkg` (`id_pkg`);

--
-- Chỉ mục cho bảng `debt`
--
ALTER TABLE `debt`
  ADD PRIMARY KEY (`id_patient`);

--
-- Chỉ mục cho bảng `logon`
--
ALTER TABLE `logon`
  ADD PRIMARY KEY (`id_patient`);

--
-- Chỉ mục cho bảng `management_history`
--
ALTER TABLE `management_history`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_mgmthis_patient` (`id_patient`),
  ADD KEY `fk_mgmthis_qrtpos` (`id_qrt_pos`);

--
-- Chỉ mục cho bảng `necessary_packages`
--
ALTER TABLE `necessary_packages`
  ADD PRIMARY KEY (`id`);

--
-- Chỉ mục cho bảng `patients`
--
ALTER TABLE `patients`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `id_card` (`id_card`),
  ADD KEY `fk_patients_qrtpos` (`id_pos`),
  ADD KEY `fk_patients_prov` (`id_prov`),
  ADD KEY `fk_patients_town` (`id_town`),
  ADD KEY `fk_patients_vlg` (`id_vlg`);

--
-- Chỉ mục cho bảng `payment_acc`
--
ALTER TABLE `payment_acc`
  ADD PRIMARY KEY (`id_acc`);

--
-- Chỉ mục cho bảng `provinces`
--
ALTER TABLE `provinces`
  ADD PRIMARY KEY (`id`);

--
-- Chỉ mục cho bảng `provinces_towns`
--
ALTER TABLE `provinces_towns`
  ADD PRIMARY KEY (`id_prov`,`id_town`),
  ADD KEY `fk_provtown_town` (`id_town`);

--
-- Chỉ mục cho bảng `quarantinepos`
--
ALTER TABLE `quarantinepos`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `name` (`name`);

--
-- Chỉ mục cho bảng `related_persons`
--
ALTER TABLE `related_persons`
  ADD PRIMARY KEY (`id_patient`,`id_related`),
  ADD KEY `fk_rltper_patient_to` (`id_related`);

--
-- Chỉ mục cho bảng `towns`
--
ALTER TABLE `towns`
  ADD PRIMARY KEY (`id`);

--
-- Chỉ mục cho bảng `towns_villages`
--
ALTER TABLE `towns_villages`
  ADD PRIMARY KEY (`id_town`,`id_vlg`),
  ADD KEY `fk_townvlg_vlg` (`id_vlg`);

--
-- Chỉ mục cho bảng `transaction_history`
--
ALTER TABLE `transaction_history`
  ADD PRIMARY KEY (`id`),
  ADD KEY `FK_transHis_fromIDAcc` (`from_id_acc`),
  ADD KEY `FK_transHis_toIDAcc` (`to_id_acc`);

--
-- Chỉ mục cho bảng `villages`
--
ALTER TABLE `villages`
  ADD PRIMARY KEY (`id`);

--
-- AUTO_INCREMENT cho các bảng đã đổ
--

--
-- AUTO_INCREMENT cho bảng `accounts`
--
ALTER TABLE `accounts`
  MODIFY `id` int(10) UNSIGNED ZEROFILL NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=56;

--
-- AUTO_INCREMENT cho bảng `activity_history`
--
ALTER TABLE `activity_history`
  MODIFY `id` int(10) UNSIGNED ZEROFILL NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=102;

--
-- AUTO_INCREMENT cho bảng `bought_pkg_history`
--
ALTER TABLE `bought_pkg_history`
  MODIFY `id` int(10) UNSIGNED ZEROFILL NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=14;

--
-- AUTO_INCREMENT cho bảng `debt`
--
ALTER TABLE `debt`
  MODIFY `id_patient` int(10) UNSIGNED ZEROFILL NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=56;

--
-- AUTO_INCREMENT cho bảng `management_history`
--
ALTER TABLE `management_history`
  MODIFY `id` int(10) UNSIGNED ZEROFILL NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=93;

--
-- AUTO_INCREMENT cho bảng `necessary_packages`
--
ALTER TABLE `necessary_packages`
  MODIFY `id` int(10) UNSIGNED ZEROFILL NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=12;

--
-- AUTO_INCREMENT cho bảng `provinces`
--
ALTER TABLE `provinces`
  MODIFY `id` int(10) UNSIGNED ZEROFILL NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT cho bảng `quarantinepos`
--
ALTER TABLE `quarantinepos`
  MODIFY `id` int(10) UNSIGNED ZEROFILL NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT cho bảng `towns`
--
ALTER TABLE `towns`
  MODIFY `id` int(10) UNSIGNED ZEROFILL NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=26;

--
-- AUTO_INCREMENT cho bảng `transaction_history`
--
ALTER TABLE `transaction_history`
  MODIFY `id` int(10) UNSIGNED ZEROFILL NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT cho bảng `villages`
--
ALTER TABLE `villages`
  MODIFY `id` int(10) UNSIGNED ZEROFILL NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=26;

--
-- Các ràng buộc cho các bảng đã đổ
--

--
-- Các ràng buộc cho bảng `activity_history`
--
ALTER TABLE `activity_history`
  ADD CONSTRAINT `FK_actHis_patients` FOREIGN KEY (`id_card_patient`) REFERENCES `patients` (`id_card`),
  ADD CONSTRAINT `fk_acthis_account` FOREIGN KEY (`usr_manager`) REFERENCES `accounts` (`usrname`);

--
-- Các ràng buộc cho bảng `bought_pkg_history`
--
ALTER TABLE `bought_pkg_history`
  ADD CONSTRAINT `fk_bpkghis_patient` FOREIGN KEY (`id_patient`) REFERENCES `patients` (`id`),
  ADD CONSTRAINT `fk_bpkghis_pkg` FOREIGN KEY (`id_pkg`) REFERENCES `necessary_packages` (`id`);

--
-- Các ràng buộc cho bảng `debt`
--
ALTER TABLE `debt`
  ADD CONSTRAINT `debt_ibfk_1` FOREIGN KEY (`id_patient`) REFERENCES `accounts` (`id`);

--
-- Các ràng buộc cho bảng `logon`
--
ALTER TABLE `logon`
  ADD CONSTRAINT `FK_logon_acc` FOREIGN KEY (`id_patient`) REFERENCES `accounts` (`id`);

--
-- Các ràng buộc cho bảng `management_history`
--
ALTER TABLE `management_history`
  ADD CONSTRAINT `fk_mgmthis_patient` FOREIGN KEY (`id_patient`) REFERENCES `patients` (`id`),
  ADD CONSTRAINT `fk_mgmthis_qrtpos` FOREIGN KEY (`id_qrt_pos`) REFERENCES `quarantinepos` (`id`);

--
-- Các ràng buộc cho bảng `patients`
--
ALTER TABLE `patients`
  ADD CONSTRAINT `fk_patients_acc` FOREIGN KEY (`id`) REFERENCES `accounts` (`id`),
  ADD CONSTRAINT `fk_patients_prov` FOREIGN KEY (`id_prov`) REFERENCES `provinces` (`id`),
  ADD CONSTRAINT `fk_patients_qrtpos` FOREIGN KEY (`id_pos`) REFERENCES `quarantinepos` (`id`),
  ADD CONSTRAINT `fk_patients_town` FOREIGN KEY (`id_town`) REFERENCES `towns` (`id`),
  ADD CONSTRAINT `fk_patients_vlg` FOREIGN KEY (`id_vlg`) REFERENCES `villages` (`id`);

--
-- Các ràng buộc cho bảng `payment_acc`
--
ALTER TABLE `payment_acc`
  ADD CONSTRAINT `FK_paymentAcc_Accounts` FOREIGN KEY (`id_acc`) REFERENCES `accounts` (`id`);

--
-- Các ràng buộc cho bảng `provinces_towns`
--
ALTER TABLE `provinces_towns`
  ADD CONSTRAINT `fk_provtown_prov` FOREIGN KEY (`id_prov`) REFERENCES `provinces` (`id`),
  ADD CONSTRAINT `fk_provtown_town` FOREIGN KEY (`id_town`) REFERENCES `towns` (`id`);

--
-- Các ràng buộc cho bảng `related_persons`
--
ALTER TABLE `related_persons`
  ADD CONSTRAINT `fk_rltper_patient_from` FOREIGN KEY (`id_patient`) REFERENCES `patients` (`id`),
  ADD CONSTRAINT `fk_rltper_patient_to` FOREIGN KEY (`id_related`) REFERENCES `patients` (`id`);

--
-- Các ràng buộc cho bảng `towns_villages`
--
ALTER TABLE `towns_villages`
  ADD CONSTRAINT `fk_townvlg_town` FOREIGN KEY (`id_town`) REFERENCES `towns` (`id`),
  ADD CONSTRAINT `fk_townvlg_vlg` FOREIGN KEY (`id_vlg`) REFERENCES `villages` (`id`);

--
-- Các ràng buộc cho bảng `transaction_history`
--
ALTER TABLE `transaction_history`
  ADD CONSTRAINT `FK_transHis_fromIDAcc` FOREIGN KEY (`from_id_acc`) REFERENCES `accounts` (`id`),
  ADD CONSTRAINT `FK_transHis_toIDAcc` FOREIGN KEY (`to_id_acc`) REFERENCES `accounts` (`id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
