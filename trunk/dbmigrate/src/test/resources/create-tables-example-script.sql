/* ---------------------------------------------------------------------- */
/* Script generated with: DeZign for Databases v4.1.3                     */
/* Target DBMS:           PostgreSQL 8                                    */
/* ---------------------------------------------------------------------- */


/* ---------------------------------------------------------------------- */
/* Sequences                                                              */
/* ---------------------------------------------------------------------- */

-- with and without constraint name for foreign keys
create table alternative_foreignkey_syntax (
      username varchar(255) not null,
      authority varchar(255) not null,
      user_id  BIGINT,
      constraint fk_authorities_users foreign key(username) references users(username),
      foreign key(user_id) references User_Core(user_id),
      primary key (username, authority));

CREATE SEQUENCE hibernate_sequence INCREMENT 1 START 500000;

/* ---------------------------------------------------------------------- */
/* Tables                                                                 */
/* ---------------------------------------------------------------------- */

/* ---------------------------------------------------------------------- */
/* Add table "Address"                                                    */
/* ---------------------------------------------------------------------- */

CREATE TABLE Address_0 (
    field_1 CHARACTER VARYING(255),
);

CREATE TABLE Address_1 (
    field_1 CHARACTER VARYING(255),
    field_2 CHARACTER VARYING(255),
    field_3 CHARACTER VARYING(255),
    zip CHARACTER VARYING(40),
    city CHARACTER VARYING(40),
);

CREATE TABLE Address_2 (
    address_id BIGINT CONSTRAINT NN_address_id NOT NULL
);

CREATE TABLE Address_3 (
    address_id BIGINT CONSTRAINT NN_address_id NOT NULL,
    field_1 CHARACTER VARYING(255),
    field_2 CHARACTER VARYING(255),
    field_3 CHARACTER VARYING(255),
    zip CHARACTER VARYING(40),
    country BIGINT CONSTRAINT NN_country NOT NULL,
    type BIGINT CONSTRAINT NN_type NOT NULL,
    city CHARACTER VARYING(40)
);

CREATE TABLE Address (
    address_id BIGINT CONSTRAINT NN_address_id NOT NULL,
    field_1 CHARACTER VARYING(255),
    field_2 CHARACTER VARYING(255),
    field_3 CHARACTER VARYING(255),
    zip CHARACTER VARYING(40),
    country BIGINT CONSTRAINT NN_country NOT NULL,
    type BIGINT CONSTRAINT NN_type NOT NULL,
    city CHARACTER VARYING(40),
    CONSTRAINT Address_pkey PRIMARY KEY (address_id)
);

CREATE UNIQUE INDEX IDX_Address1 ON Address (address_id);

CREATE INDEX IDX_Address2 ON Address (country);


/* ---------------------------------------------------------------------- */
/* Add table "User_Core"                                                  */
/* ---------------------------------------------------------------------- */

CREATE TABLE User_Core_0 (
    user_id BIGINT CONSTRAINT NN_user_id NOT NULL,
    email CHARACTER VARYING(50),
    mobile_prefix CHARACTER VARYING(40),
    mobile_number CHARACTER VARYING(40),
    state BIGINT CONSTRAINT NN_state NOT NULL,
    role_id BIGINT CONSTRAINT NN_role_id NOT NULL,
    address_id BIGINT CONSTRAINT NN_address_id NOT NULL,
    first_name CHARACTER VARYING(40) CONSTRAINT NN_first_name NOT NULL,
    last_name CHARACTER VARYING(40) CONSTRAINT NN_last_name NOT NULL,
    user_identification CHARACTER VARYING(40) CONSTRAINT NN_user_identification NOT NULL,
    registration_time TIMESTAMP CONSTRAINT NN_registration_time NOT NULL,
    type SMALLINT,
    gender CHARACTER VARYING(10),
    locale_code CHARACTER VARYING(10),
    CONSTRAINT User_Core_pkey PRIMARY KEY (user_id)
);

CREATE TABLE User_Core (
    user_id BIGINT CONSTRAINT NN_user_id NOT NULL,
    email CHARACTER VARYING(50),
    mobile_prefix CHARACTER VARYING(40),
    mobile_number CHARACTER VARYING(40),
    state BIGINT CONSTRAINT NN_state NOT NULL,
    role_id BIGINT CONSTRAINT NN_role_id NOT NULL,
    address_id BIGINT CONSTRAINT NN_address_id NOT NULL,
    first_name CHARACTER VARYING(40) CONSTRAINT NN_first_name NOT NULL,
    last_name CHARACTER VARYING(40) CONSTRAINT NN_last_name NOT NULL,
    user_identification CHARACTER VARYING(40) CONSTRAINT NN_user_identification NOT NULL,
    registration_time TIMESTAMP CONSTRAINT NN_registration_time NOT NULL,
    type SMALLINT,
    gender CHARACTER VARYING(10),
    locale_code CHARACTER VARYING(10),
    CONSTRAINT User_Core_pkey PRIMARY KEY (user_id),
    CONSTRAINT TUC_User_Core_1 UNIQUE (user_identification)
);

CREATE INDEX IDX_User_Core1 ON User_Core (role_id);

CREATE UNIQUE INDEX IDX_User_Core2 ON User_Core (user_id);

CREATE INDEX IDX_User_Core3 ON User_Core (address_id);

CREATE INDEX IDX_User_Core4 ON User_Core (state);

CREATE UNIQUE INDEX IDX_User_Core_5 ON User_Core (user_identification);

COMMENT ON TABLE User_Core IS 'Speichert die User-Daten von im System registrierten Kunden - ebenso Zusteller und Servicepersonal.  Die Rolle des Users wird in role_id festgelegt.  Abhängigkeiten: Jeder User_Core muss eine role_id und eine address_id haben.';

COMMENT ON COLUMN User_Core.gender IS 'MALE or FEMALE or null as GenderEnum';



/* ---------------------------------------------------------------------- */
/* Add table "Role"                                                       */
/* ---------------------------------------------------------------------- */

CREATE TABLE Role (
    role_id BIGINT CONSTRAINT NN_role_id NOT NULL,
    role_name CHARACTER VARYING(40) CONSTRAINT NN_role_name NOT NULL,
    role_description CHARACTER VARYING(255),
    CONSTRAINT Role_pkey PRIMARY KEY (role_id),
    CONSTRAINT TUC_Role_1 UNIQUE (role_name)
);

CREATE UNIQUE INDEX IDX_Role1 ON Role (role_id);

CREATE UNIQUE INDEX IDX_Role_2 ON Role (role_name);


/* ---------------------------------------------------------------------- */
/* Add table "Privilege"                                                  */
/* ---------------------------------------------------------------------- */

CREATE TABLE Privilege (
    privilege_id BIGINT CONSTRAINT NN_privilege_id NOT NULL,
    privilege_name CHARACTER VARYING(40) CONSTRAINT NN_privilege_name NOT NULL,
    privilege_description CHARACTER VARYING(255),
    role_id BIGINT CONSTRAINT NN_role_id NOT NULL,
    privilege_value CHARACTER VARYING(40) CONSTRAINT NN_privilege_value NOT NULL,
    internal_name CHARACTER VARYING(40),
    CONSTRAINT Privilege_pkey PRIMARY KEY (privilege_id)
);

CREATE INDEX IDX_Privilege1 ON Privilege (role_id);

/* ---------------------------------------------------------------------- */
/* Add table "User_Authentication"                                        */
/* ---------------------------------------------------------------------- */

CREATE TABLE User_Authentication (
    user_data_id BIGINT CONSTRAINT NN_user_data_id NOT NULL,
    internet_password CHARACTER VARYING(40),
    pin CHARACTER VARYING(40),
    user_id BIGINT CONSTRAINT NN_user_id NOT NULL,
    question CHARACTER VARYING(255),
    answer CHARACTER VARYING(255),
    CONSTRAINT User_Authentication_pkey PRIMARY KEY (user_data_id)
);

CREATE INDEX IDX_User_Authentication1 ON User_Authentication (user_id);


/* ---------------------------------------------------------------------- */
/* Add table "CV_Country"                                                 */
/* ---------------------------------------------------------------------- */

CREATE TABLE CV_Country (
    country_id BIGINT CONSTRAINT NN_country_id NOT NULL,
    country CHARACTER VARYING(40) CONSTRAINT NN_country NOT NULL,
    language_code CHARACTER VARYING(40),
    CONSTRAINT CV_Country_pkey PRIMARY KEY (country_id),
    CONSTRAINT TUC_CV_Country_1 UNIQUE (country)
);

CREATE UNIQUE INDEX IDX_CV_Country1 ON CV_Country (country_id);

/* ---------------------------------------------------------------------- */
/* Add table "Card"                                                       */
/* ---------------------------------------------------------------------- */

CREATE TABLE Card (
    card_id BIGINT CONSTRAINT NN_card_id NOT NULL,
    track_1 CHARACTER VARYING(40) CONSTRAINT NN_track_1 NOT NULL,
    track_2 CHARACTER VARYING(40) CONSTRAINT NN_track_2 NOT NULL,
    track_3 CHARACTER VARYING(40) CONSTRAINT NN_track_3 NOT NULL,
    user_id BIGINT CONSTRAINT NN_user_id NOT NULL,
    CONSTRAINT Card_pkey PRIMARY KEY (card_id)
);

CREATE INDEX IDX_Card1 ON Card (user_id);


/* ---------------------------------------------------------------------- */
/* Add table "CV_User_State"                                              */
/* ---------------------------------------------------------------------- */

CREATE TABLE CV_User_State (
    id BIGINT CONSTRAINT NN_id NOT NULL,
    name CHARACTER VARYING(40) CONSTRAINT NN_name NOT NULL,
    description CHARACTER VARYING(255),
    CONSTRAINT CV_User_State_pkey PRIMARY KEY (id),
    CONSTRAINT TUC_CV_User_State_1 UNIQUE (name)
);

CREATE UNIQUE INDEX IDX_CV_User_State1 ON CV_User_State (id);



/* ---------------------------------------------------------------------- */
/* Add table "Geo_data"                                                   */
/* ---------------------------------------------------------------------- */

CREATE TABLE Geo_data (
    geo_data_id BIGINT CONSTRAINT NN_geo_data_id NOT NULL,
    coordinates_x CHARACTER VARYING(40),
    coordinates_y CHARACTER VARYING(40),
    address_id BIGINT CONSTRAINT NN_address_id NOT NULL,
    CONSTRAINT Geo_data_pkey PRIMARY KEY (geo_data_id)
);


/* ---------------------------------------------------------------------- */
/* Add table "CV_Address_Type"                                            */
/* ---------------------------------------------------------------------- */

CREATE TABLE CV_Address_Type (
    type SMALLINT CONSTRAINT NN_type NOT NULL,
    description CHARACTER VARYING(40),
    CONSTRAINT CV_Address_Type_pkey PRIMARY KEY (type)
);

/* ---------------------------------------------------------------------- */
/* Add table "Tag"                                                        */
/* ---------------------------------------------------------------------- */

CREATE TABLE Tag (
    tag_id BIGINT CONSTRAINT NN_tag_id NOT NULL,
    tag CHARACTER VARYING(40) CONSTRAINT NN_tag NOT NULL,
    CONSTRAINT Tag_pkey PRIMARY KEY (tag_id),
    CONSTRAINT TUC_Tag1 UNIQUE (tag)
);

CREATE UNIQUE INDEX IDX_Tag1 ON Tag (tag);

/* ---------------------------------------------------------------------- */
/* Add table "db_version"                                                 */
/* ---------------------------------------------------------------------- */

CREATE TABLE db_version (
    since TIMESTAMP CONSTRAINT NN_since NOT NULL,
    version CHARACTER VARYING(100) CONSTRAINT NN_version NOT NULL
);

/* ---------------------------------------------------------------------- */
/* Foreign key constraints                                                */
/* ---------------------------------------------------------------------- */

ALTER TABLE Address ADD CONSTRAINT CV_Country_Address_FK_1 
    FOREIGN KEY (country) REFERENCES CV_Country (country_id);

ALTER TABLE Address ADD CONSTRAINT CV_Address_Type_Address_FK_2 
    FOREIGN KEY (type) REFERENCES CV_Address_Type (type);

ALTER TABLE User_Core ADD CONSTRAINT Role_User_Core_FK_1 
    FOREIGN KEY (role_id) REFERENCES Role (role_id);

ALTER TABLE User_Core ADD CONSTRAINT Address_User_Core_FK_2 
    FOREIGN KEY (address_id) REFERENCES Address (address_id);

ALTER TABLE User_Core ADD CONSTRAINT CV_User_State_User_Core_FK_3 
    FOREIGN KEY (state) REFERENCES CV_User_State (id);

ALTER TABLE Privilege ADD CONSTRAINT Role_Privilege_FK_1 
    FOREIGN KEY (role_id) REFERENCES Role (role_id);

ALTER TABLE Rule ADD CONSTRAINT CV_Rule_Type_Rule_FK_1 
    FOREIGN KEY (type) REFERENCES CV_Rule_Type (id);

ALTER TABLE User_Authentication ADD CONSTRAINT User_Core_User_Authentication_FK_1 
    FOREIGN KEY (user_id) REFERENCES User_Core (user_id);

ALTER TABLE Card ADD CONSTRAINT User_Core_Card_FK_1 
    FOREIGN KEY (user_id) REFERENCES User_Core (user_id);

ALTER TABLE Geo_data ADD CONSTRAINT Address_Geo_data_FK_1 
    FOREIGN KEY (address_id) REFERENCES Address (address_id);

