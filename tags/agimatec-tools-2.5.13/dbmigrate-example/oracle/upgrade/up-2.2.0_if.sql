-- RSt, 22.04.2008

-- #if testdata_enabled=true
update address set country = 'DE' where country is null;
-- #endif

-- @version(2.2.0)
