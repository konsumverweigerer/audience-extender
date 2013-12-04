# --- !Ups

alter table website add column uuid varchar(50);
alter table cookie add column pathhash bigint;
alter table cookie_stat_data add column sub varchar(64);

# --- !Downs

alter table website drop column uuid;
alter table cookie drop column pathhash;
alter table cookie_stat_data drop column sub;
