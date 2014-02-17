# --- !Ups

alter table admin add column features varchar(1024);

# --- !Downs

alter table admin drop column features;
