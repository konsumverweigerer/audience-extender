# --- !Ups

alter table creative add column url varchar(255);
alter table creative add column data bytea;

# --- !Downs

alter table creative drop column url;
alter table creative drop column data;
