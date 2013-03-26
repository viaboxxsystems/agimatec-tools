CREATE TABLE `http_resource_directory` (
  `nid` bigint(20) NOT NULL AUTO_INCREMENT,
  `ncrc32` int(10) unsigned DEFAULT '0',
  `surl` text COLLATE ascii_bin NOT NULL,
  `setag` text COLLATE ascii_bin,
  `slastmodified` varchar(32) COLLATE ascii_bin DEFAULT NULL,
  `scontenttype` varchar(32) COLLATE ascii_bin DEFAULT NULL,
  `scharacterset` varchar(16) COLLATE ascii_bin DEFAULT NULL,
  `nstatus` int(11) DEFAULT '0',
  `slock` varchar(80) COLLATE ascii_bin DEFAULT NULL,
  `nrequestms` int(11) DEFAULT '0',
  `dtexpires` datetime DEFAULT NULL,
  `dtupdated` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `dtdeleted` datetime DEFAULT NULL,
  PRIMARY KEY (`nid`),
  KEY `IDX_CRC` (`ncrc32`),
  KEY `IDX_PURGE` (`dtexpires`)
) ENGINE=InnoDB AUTO_INCREMENT=473 DEFAULT CHARSET=ascii COLLATE=ascii_bin;

DROP TABLE IF EXISTS `OTHER_TABLE`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `OTHER_TABLE`  (
	JOB_INSTANCE_ID BIGINT  NOT NULL PRIMARY KEY ,
	`VERSION` BIGINT ,
	`NORDER` int(11)  NOT NULL DEFAULT '1',
	`NORDER2` int(11) DEFAULT '1'  NOT NULL ,
	`JOB_NAME` VARCHAR(100) NOT NULL,
	`JOB_KEY` VARCHAR(32) NOT NULL,
	constraint OTHER_TABLE_FK foreign key (JOB_NAME) references REFERENCED_TABLE(JOB_NAME_ID)
    constraint OTHER_TABLE_UN unique (`JOB_NAME`, `JOB_KEY`)
);
-- ENGINE=InnoDB AUTO_INCREMENT=86 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

DROP TABLE IF EXISTS TEST_TABLE;
CREATE TABLE TEST_TABLE (
  client_id smallint(6) NOT NULL,
  recently datetime,
  updated DATETIME DEFAULT NULL,
  VERSION BIGINT  ,
  author varchar(32),
  DOUBLE_VAL DOUBLE PRECISION ,
  SERIALIZED_CONTEXT TEXT ,
  PRIMARY KEY (client_id, `version`)
)  ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE other_table_seq (
  ID BIGINT NOT NULL)
   ENGINE=MYISAM;
INSERT INTO OTHER_TABLE_SEQ values(0);



