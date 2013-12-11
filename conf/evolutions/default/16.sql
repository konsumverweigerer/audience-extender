# --- !Ups

alter table audience add column tracking text;

# --- !Downs

alter table audience drop column tracking;
