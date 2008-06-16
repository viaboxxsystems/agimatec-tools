-- Tables of Catalog with oracle

/* ---------------------------------------------------------------------- */
/* Sequences                                                              */
/* ---------------------------------------------------------------------- */
CREATE SEQUENCE hibernate_sequence
    START WITH  1000
    INCREMENT BY  1
    NOMINVALUE
    NOMAXVALUE
    CACHE  100;


/* ---------------------------------------------------------------------- */
/* Tables                                                                 */
/* ---------------------------------------------------------------------- */
/* ---------------------------------------------------------------------- */
/* Table "Address" */
/* ---------------------------------------------------------------------- */

CREATE TABLE Address (
    address_id INTEGER NOT NULL,
    version INTEGER DEFAULT 0 NOT NULL,
    field_1 VARCHAR2(255),
    field_2 VARCHAR2(255),
    field_3 VARCHAR2(255),
    zip VARCHAR2(40),
    city VARCHAR2(40),
    country CHARACTER(2) NOT NULL,
    CONSTRAINT Address_pkey PRIMARY KEY (address_id));

CREATE INDEX IDX_Address_2 ON Address (country) ;



/* ---------------------------------------------------------------------- */
/* Table "CV_Country" */
/* ---------------------------------------------------------------------- */

CREATE TABLE CV_Country (
    code CHARACTER(2) NOT NULL,
    name VARCHAR2(40),
    CONSTRAINT CV_Country_pkey PRIMARY KEY (code) );


COMMENT ON TABLE CV_Country IS 'Country-table contains all countries available for selection';
COMMENT ON COLUMN CV_Country.code IS 'ISO 3166-1-alpha-2 country code';
COMMENT ON COLUMN CV_Country.name IS 'country name (as description)';

/* ---------------------------------------------------------------------- */
/* Table "CV_Currency" */
/* ---------------------------------------------------------------------- */

CREATE TABLE CV_Currency (
    code CHARACTER(3) NOT NULL,
    name VARCHAR2(40),
    CONSTRAINT CV_Currency_pkey PRIMARY KEY (code) );


COMMENT ON TABLE CV_Currency IS 'Contains currencies available for selection';
COMMENT ON COLUMN CV_Currency.code IS 'ISO 4217 Currency Code';
COMMENT ON COLUMN CV_Currency.name IS 'currency name (as description)';

/* ---------------------------------------------------------------------- */
/* Table "CV_Postcode" */
/* ---------------------------------------------------------------------- */

CREATE TABLE CV_Postcode (
    postcode_id INTEGER NOT NULL,
    version INTEGER DEFAULT 0 NOT NULL,
    zip VARCHAR2(40) NOT NULL,
    description VARCHAR2(100),
    valid_from TIMESTAMP,
    country CHARACTER(2) NOT NULL,
    CONSTRAINT CV_Postcode_pkey PRIMARY KEY (postcode_id) );

COMMENT ON TABLE CV_Postcode IS 'postal codes in a country';
COMMENT ON COLUMN CV_Postcode.zip IS 'ZIP-code ';
COMMENT ON COLUMN CV_Postcode.description IS 'ZIP-code description';
COMMENT ON COLUMN CV_Postcode.valid_from IS 'The ZIP-code is valid from this date';

/* ---------------------------------------------------------------------- */
/* Table "db_version" */
/* ---------------------------------------------------------------------- */

CREATE TABLE db_version (
    since TIMESTAMP NOT NULL,
    version VARCHAR2(100) NOT NULL,
    CONSTRAINT db_version_pkey PRIMARY KEY (version) );


COMMENT ON TABLE db_version IS 'version of the database schema. ';

/* ---------------------------------------------------------------------- */
/* Table "Import_Control" */
/* ---------------------------------------------------------------------- */

CREATE TABLE Import_Control (
    import_id INTEGER NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP,
    status VARCHAR2(40),
    import_name VARCHAR2(250) NOT NULL,
    row_count INTEGER,
    error_count INTEGER,
    description VARCHAR2(500),
    file_name VARCHAR2(500),
    CONSTRAINT Import_Control_pkey PRIMARY KEY (import_id) );


COMMENT ON TABLE Import_Control IS 'table to control concurrency imports or long-running imports';
COMMENT ON COLUMN Import_Control.import_id IS 'sequence number, primary key';
COMMENT ON COLUMN Import_Control.start_time IS 'time import has been started';
COMMENT ON COLUMN Import_Control.end_time IS 'null when unfinished';
COMMENT ON COLUMN Import_Control.status IS 'status (OK, FAILED, RUNNING)';
COMMENT ON COLUMN Import_Control.import_name IS 'short description or symbolic name for the kind of import';

/* ---------------------------------------------------------------------- */
/* Table "Privilege" */
/* ---------------------------------------------------------------------- */

CREATE TABLE Privilege (
    privilege_id INTEGER NOT NULL,
    version INTEGER DEFAULT 0 NOT NULL,
    privilege_name VARCHAR2(40) NOT NULL,
    privilege_domain VARCHAR2(20) DEFAULT 'ADM' NOT NULL,
    CONSTRAINT Privilege_pkey PRIMARY KEY (privilege_id) );


/* ---------------------------------------------------------------------- */
/* Table "Role" */
/* ---------------------------------------------------------------------- */

CREATE TABLE Role (
    role_id INTEGER NOT NULL,
    version INTEGER DEFAULT 0 NOT NULL,
    role_name VARCHAR2(40) NOT NULL,
    role_description VARCHAR2(255),
    CONSTRAINT Role_pkey PRIMARY KEY (role_id) ,
CONSTRAINT TUC_Role_1 UNIQUE (role_name));

/* ---------------------------------------------------------------------- */
/* Table "Role_Privilege" */
/* ---------------------------------------------------------------------- */

CREATE TABLE Role_Privilege (
    role_id INTEGER NOT NULL,
    privilege_id INTEGER NOT NULL,
    CONSTRAINT Role_Privilege_pkey PRIMARY KEY (role_id, privilege_id) );

/* ---------------------------------------------------------------------- */
/* Table "User_Core" */
/* ---------------------------------------------------------------------- */

CREATE TABLE User_Core (
    user_id INTEGER NOT NULL,
    version INTEGER DEFAULT 0 NOT NULL,
    email VARCHAR2(250),
    mobile_number VARCHAR2(40),
    role_id INTEGER NOT NULL,
    address_id INTEGER,
    first_name VARCHAR2(40),
    last_name VARCHAR2(40),
    user_identification VARCHAR2(40) NOT NULL,
    registration_time TIMESTAMP NOT NULL,
    type VARCHAR2(20) DEFAULT 'REGISTERED' NOT NULL,
    gender VARCHAR2(10),
    state VARCHAR2(40) DEFAULT 'OK' NOT NULL,
    CONSTRAINT User_Core_pkey PRIMARY KEY (user_id) ,
CONSTRAINT TUC_User_Core_1 UNIQUE (user_identification));

CREATE INDEX IDX_User_Core1 ON User_Core (role_id) ;
CREATE UNIQUE INDEX IDX_User_Core3 ON User_Core (address_id) ;

/* ---------------------------------------------------------------------- */
/* Foreign key constraints                                                */
/* ---------------------------------------------------------------------- */
ALTER TABLE Address ADD CONSTRAINT CV_Country_Address_FK_1
    FOREIGN KEY (country) REFERENCES CV_Country (code);

ALTER TABLE CV_Postcode ADD CONSTRAINT CV_Country_CV_Postcode_FK_1
    FOREIGN KEY (country) REFERENCES CV_Country (code);

ALTER TABLE Role_Privilege ADD CONSTRAINT Privilege_Role_Privilege_FK_2
    FOREIGN KEY (privilege_id) REFERENCES Privilege (privilege_id) ON DELETE CASCADE;

ALTER TABLE Role_Privilege ADD CONSTRAINT Role_Role_Privilege_FK_1
    FOREIGN KEY (role_id) REFERENCES Role (role_id) ON DELETE CASCADE;

ALTER TABLE User_Core ADD CONSTRAINT Address_User_Core_FK_2
    FOREIGN KEY (address_id) REFERENCES Address (address_id);

ALTER TABLE User_Core ADD CONSTRAINT Role_User_Core_FK_1
    FOREIGN KEY (role_id) REFERENCES Role (role_id);

