-- comment 1
connect ${DB_USER}/${DB_PASSWORD}@logdb;

update log_event set event_id = 1 where event_id is null;

connect ${DB_USER}/${DB_PASSWORD}@test;
connect ${DB_USER}/${DB_PASSWORD}@jdbc:postgresql://localhost:5432/test;

create table db_version (since timestamp, version varchar(100));

update cv_country set country_id = 1 where country_id is null;

-- @version(2.0.13)

@subscript.sql