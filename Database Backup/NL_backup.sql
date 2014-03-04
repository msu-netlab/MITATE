-- MySQL dump 10.13  Distrib 5.1.73, for redhat-linux-gnu (x86_64)
--
-- Host: localhost    Database: mitate
-- ------------------------------------------------------
-- Server version	5.1.73

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `criteria`
--

DROP TABLE IF EXISTS `criteria`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `criteria` (
  `criteriaid` int(10) NOT NULL,
  `specification` varchar(500) DEFAULT NULL,
  `deviceid` varchar(200) DEFAULT 'client',
  PRIMARY KEY (`criteriaid`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `criteria`
--

LOCK TABLES `criteria` WRITE;
/*!40000 ALTER TABLE `criteria` DISABLE KEYS */;
/*!40000 ALTER TABLE `criteria` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `experiment`
--

DROP TABLE IF EXISTS `experiment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `experiment` (
  `experiment_id` int(13) NOT NULL,
  `username` varchar(20) NOT NULL,
  `permission` varchar(7) NOT NULL,
  `cellulardata` decimal(10,2) NOT NULL,
  `wifidata` decimal(10,2) NOT NULL,
  PRIMARY KEY (`experiment_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `experiment`
--

LOCK TABLES `experiment` WRITE;
/*!40000 ALTER TABLE `experiment` DISABLE KEYS */;
/*!40000 ALTER TABLE `experiment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `logs`
--

DROP TABLE IF EXISTS `logs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `logs` (
  `logid` int(5) NOT NULL AUTO_INCREMENT,
  `username` varchar(20) NOT NULL,
  `transferid` int(10) NOT NULL,
  `deviceid` varchar(10) NOT NULL,
  `logmessage` mediumtext,
  `transferfinished` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`logid`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `logs`
--

LOCK TABLES `logs` WRITE;
/*!40000 ALTER TABLE `logs` DISABLE KEYS */;
/*!40000 ALTER TABLE `logs` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `metric`
--

DROP TABLE IF EXISTS `metric`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `metric` (
  `name` varchar(100) NOT NULL,
  `metricid` int(6) NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`metricid`)
) ENGINE=MyISAM AUTO_INCREMENT=10043 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `metric`
--

LOCK TABLES `metric` WRITE;
/*!40000 ALTER TABLE `metric` DISABLE KEYS */;
INSERT INTO `metric` VALUES ('udp_uplink_throughput',10000),('tcp_uplink_throughput',10001),('udp_downlink_throughput',10002),('tcp_downlink_throughput',10003),('udp_min_uplink_delay',10004),('tcp_min_uplink_delay',10005),('udp_mean_uplink_delay',10006),('tcp_mean_uplink_delay',10007),('udp_max_uplink_delay',10008),('tcp_max_uplink_delay',10009),('udp_min_downlink_delay',10010),('tcp_min_downlink_delay',10011),('udp_mean_downlink_delay',10012),('tcp_mean_downlink_delay',10013),('udp_max_downlink_delay',10014),('tcp_max_downlink_delay',10015),('udp_maximum_uplink_jitter',10016),('tcp_maximum_uplink_jitter',10017),('udp_maximum_downlink_jitter',10018),('tcp_maximum_downlink_jitter',10019),('tcp_uplink_packet_loss',10020),('tcp_downlink_packet_loss',10021),('accelerometer_reading_y',10037),('tcp_uplink_median',10024),('udp_uplink_median',10023),('udp_downlink_median',10025),('tcp_downlink_median',10026),('one_way_cdn_delay',10027),('rtt_cdn_latency',10028),('one_way_cdn_throughput',10029),('latitude_before',10030),('longitude_before',10031),('latitude_after',10032),('longitude_after',10033),('device_travel_speed',10034),('signal_strength',10035),('accelerometer_reading_x',10036),('accelerometer_reading_z',10038),('is_device_incall',10039),('responsedata',10040),('udp_uplink_packet_loss',10041),('udp_downlink_packet_loss',10042);
/*!40000 ALTER TABLE `metric` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `metricdata`
--

DROP TABLE IF EXISTS `metricdata`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `metricdata` (
  `metricid` int(5) NOT NULL DEFAULT '0',
  `transferid` int(10) NOT NULL DEFAULT '0',
  `transactionid` int(10) NOT NULL DEFAULT '0',
  `value` decimal(18,10) DEFAULT NULL,
  `transferfinished` varchar(50) DEFAULT NULL,
  `deviceid` varchar(200) DEFAULT NULL,
  `responsedata` varchar(512) DEFAULT NULL,
  UNIQUE KEY `mttdt` (`metricid`,`transferid`,`transactionid`,`deviceid`,`transferfinished`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `metricdata`
--

LOCK TABLES `metricdata` WRITE;
/*!40000 ALTER TABLE `metricdata` DISABLE KEYS */;
/*!40000 ALTER TABLE `metricdata` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `trans_criteria_link`
--

DROP TABLE IF EXISTS `trans_criteria_link`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `trans_criteria_link` (
  `criteriaid` int(10) DEFAULT NULL,
  `transactionid` int(20) DEFAULT NULL
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `trans_criteria_link`
--

LOCK TABLES `trans_criteria_link` WRITE;
/*!40000 ALTER TABLE `trans_criteria_link` DISABLE KEYS */;
/*!40000 ALTER TABLE `trans_criteria_link` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `trans_transfer_link`
--

DROP TABLE IF EXISTS `trans_transfer_link`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `trans_transfer_link` (
  `transferid` int(10) DEFAULT NULL,
  `transactionid` int(10) DEFAULT NULL,
  `orderno` int(2) NOT NULL
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `trans_transfer_link`
--

LOCK TABLES `trans_transfer_link` WRITE;
/*!40000 ALTER TABLE `trans_transfer_link` DISABLE KEYS */;
/*!40000 ALTER TABLE `trans_transfer_link` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `transaction_fetched`
--

DROP TABLE IF EXISTS `transaction_fetched`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `transaction_fetched` (
  `transactionid` int(10) NOT NULL DEFAULT '0',
  `deviceid` varchar(15) NOT NULL DEFAULT '',
  PRIMARY KEY (`transactionid`,`deviceid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `transaction_fetched`
--

LOCK TABLES `transaction_fetched` WRITE;
/*!40000 ALTER TABLE `transaction_fetched` DISABLE KEYS */;
/*!40000 ALTER TABLE `transaction_fetched` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `transactions`
--

DROP TABLE IF EXISTS `transactions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `transactions` (
  `transactionid` int(10) NOT NULL,
  `username` varchar(20) DEFAULT NULL,
  `count` int(3) DEFAULT '1',
  `original_count` int(3) DEFAULT '1',
  `experiment_id` int(13) DEFAULT NULL,
  PRIMARY KEY (`transactionid`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `transactions`
--

LOCK TABLES `transactions` WRITE;
/*!40000 ALTER TABLE `transactions` DISABLE KEYS */;
/*!40000 ALTER TABLE `transactions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `transfer`
--

DROP TABLE IF EXISTS `transfer`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `transfer` (
  `transferid` int(10) NOT NULL,
  `sourceip` varchar(100) NOT NULL,
  `destinationip` varchar(100) NOT NULL,
  `bytes` int(10) DEFAULT NULL,
  `type` int(1) NOT NULL,
  `transferadded` varchar(50) DEFAULT NULL,
  `packetdelay` int(10) DEFAULT '300',
  `explicit` int(1) NOT NULL,
  `content` mediumtext,
  `noofpackets` int(4) NOT NULL DEFAULT '10',
  `protocoltype` varchar(10) DEFAULT NULL,
  `portnumber` int(5) DEFAULT NULL,
  `contenttype` varchar(10) NOT NULL DEFAULT 'ASCII',
  `response` int(1) NOT NULL DEFAULT '0',
  `delay` int(10) DEFAULT '0',
  PRIMARY KEY (`transferid`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `transfer`
--

LOCK TABLES `transfer` WRITE;
/*!40000 ALTER TABLE `transfer` DISABLE KEYS */;
/*!40000 ALTER TABLE `transfer` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `transferexecutedby`
--

DROP TABLE IF EXISTS `transferexecutedby`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `transferexecutedby` (
  `transferid` int(10) NOT NULL,
  `devicename` varchar(50) NOT NULL,
  `username` varchar(20) NOT NULL,
  `carriername` varchar(50) NOT NULL,
  `deviceid` varchar(15) NOT NULL DEFAULT '',
  PRIMARY KEY (`transferid`,`deviceid`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `transferexecutedby`
--

LOCK TABLES `transferexecutedby` WRITE;
/*!40000 ALTER TABLE `transferexecutedby` DISABLE KEYS */;
/*!40000 ALTER TABLE `transferexecutedby` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `transfermetrics`
--

DROP TABLE IF EXISTS `transfermetrics`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `transfermetrics` (
  `transferid` int(10) NOT NULL DEFAULT '0',
  `transactionid` int(10) NOT NULL DEFAULT '0',
  `udppacketmetrics` longtext,
  `tcppacketmetrics` longtext,
  `udplatencyconf` decimal(10,2) DEFAULT NULL,
  `udpthroughputconf` decimal(10,2) DEFAULT NULL,
  `tcplatencyconf` decimal(10,2) DEFAULT NULL,
  `tcpthroughputconf` decimal(10,2) DEFAULT NULL,
  `deviceid` varchar(15) NOT NULL DEFAULT '',
  PRIMARY KEY (`transferid`,`transactionid`,`deviceid`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `transfermetrics`
--

LOCK TABLES `transfermetrics` WRITE;
/*!40000 ALTER TABLE `transfermetrics` DISABLE KEYS */;
/*!40000 ALTER TABLE `transfermetrics` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `usercredits`
--

DROP TABLE IF EXISTS `usercredits`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `usercredits` (
  `credit_id` int(10) NOT NULL,
  `username` varchar(20) DEFAULT NULL,
  `available_cellular_credits` decimal(10,2) DEFAULT '0.00',
  `contributed_cellular_credits` decimal(10,2) DEFAULT '0.00',
  `available_wifi_credits` decimal(10,2) DEFAULT '0.00',
  `contributed_wifi_credits` decimal(10,2) DEFAULT '0.00',
  PRIMARY KEY (`credit_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `usercredits`
--

LOCK TABLES `usercredits` WRITE;
/*!40000 ALTER TABLE `usercredits` DISABLE KEYS */;
/*!40000 ALTER TABLE `usercredits` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `userdevice`
--

DROP TABLE IF EXISTS `userdevice`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `userdevice` (
  `username` varchar(20) NOT NULL DEFAULT '',
  `devicename` varchar(50) NOT NULL,
  `pollinterval` int(5) NOT NULL,
  `deviceid` varchar(10) NOT NULL DEFAULT '',
  `minbatterypower` int(3) DEFAULT NULL,
  `random_string` varchar(200) DEFAULT NULL,
  `timespingedwifi` int(5) DEFAULT '0',
  `timespingedcellular` int(5) DEFAULT '0',
  PRIMARY KEY (`deviceid`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `userdevice`
--

LOCK TABLES `userdevice` WRITE;
/*!40000 ALTER TABLE `userdevice` DISABLE KEYS */;
/*!40000 ALTER TABLE `userdevice` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `userinfo`
--

DROP TABLE IF EXISTS `userinfo`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `userinfo` (
  `fname` varchar(20) NOT NULL,
  `lname` varchar(20) NOT NULL,
  `username` varchar(20) NOT NULL,
  `password` varchar(500) DEFAULT NULL,
  `email` varchar(50) NOT NULL,
  `datecreated` date DEFAULT NULL,
  PRIMARY KEY (`username`),
  UNIQUE KEY `email` (`email`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `userinfo`
--

LOCK TABLES `userinfo` WRITE;
/*!40000 ALTER TABLE `userinfo` DISABLE KEYS */;
/*!40000 ALTER TABLE `userinfo` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2014-02-24 23:17:16
