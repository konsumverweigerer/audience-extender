# --- !Ups

alter table creative add column external_url varchar(1024);

# --- !Downs

alter table creative drop column external_url;
