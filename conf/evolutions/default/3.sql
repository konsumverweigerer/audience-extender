# --- !Ups

alter table cookie add column variant varchar(20);
alter table cookie add column uuid varchar(50);
alter table cookie add column content text;

create unique index uuid_id on cookie (uuid);

# --- !Downs

alter table cookie drop column variant;
alter table cookie drop column uuid;
alter table cookie drop column content;
