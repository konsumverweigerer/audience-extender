# --- !Ups

CREATE FUNCTION update_cookie_stats( cookieid INT, ts VARCHAR) RETURNS void as $$
BEGIN
  UPDATE cookie_stat_data set views = views + 1 WHERE cookie_id=cookieid AND timestep=ts AND sub IS NULL;;
  IF FOUND THEN RETURN;; END IF;;
  BEGIN
    INSERT INTO cookie_stat_data (id,timestep,cookie_id,views) VALUES ((SELECT nextval('cookie_stat_data_seq')),ts,cookieid,1);;
  EXCEPTION WHEN OTHERS THEN 
    UPDATE cookie_stat_data set views = views + 1 WHERE cookie_id=cookieid AND timestep=ts AND sub IS NULL;;
  END;;
  RETURN;;
END;; $$ language plpgsql;

CREATE FUNCTION update_cookie_stats_sub( cookieid INT, ts VARCHAR, s VARCHAR) RETURNS void as $$
BEGIN 
  UPDATE cookie_stat_data set views = views + 1 WHERE cookie_id=cookieid AND timestep=ts AND sub=s;;
  IF FOUND THEN RETURN;; END IF;;
  BEGIN 
    INSERT INTO cookie_stat_data (id,timestep,cookie_id,sub,views) VALUES ((SELECT nextval('cookie_stat_data_seq')),ts,cookieid,s,1);;
  EXCEPTION WHEN OTHERS THEN 
    UPDATE cookie_stat_data set views = views + 1 WHERE cookie_id=cookieid AND timestep=ts AND sub=s;;
  END;;
  RETURN;;
END;; $$ language plpgsql;;

CREATE FUNCTION update_creative_stats( creativeid INT, ts VARCHAR) RETURNS void as $$
BEGIN
  UPDATE creative_stat_data set views = views + 1 WHERE creative_id=creativeid AND timestep=ts;;
  IF FOUND THEN RETURN;; END IF;;
  BEGIN 
    INSERT INTO creative_stat_data (id,timestep,creative_id,views) VALUES ((SELECT nextval('creative_stat_data_seq')),ts,creativeid,1);;
  EXCEPTION WHEN OTHERS THEN 
    UPDATE creative_stat_data set views = views + 1 WHERE creative_id=creativeid AND timestep=ts;;
  END;;
  RETURN;;
END;; $$ language plpgsql;;

# --- !Downs

DROP FUNCTION update_cookie_stats( cookieid INT, ts VARCHAR);
DROP FUNCTION update_cookie_stats_sub( cookieid INT, ts VARCHAR, s VARCHAR);
DROP FUNCTION update_creative_stats( creativeid INT, ts VARCHAR);
