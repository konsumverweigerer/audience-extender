# --- !Ups

alter table cookie add column modified timestamp;

# --- !Downs

alter table cookie drop column modified;
