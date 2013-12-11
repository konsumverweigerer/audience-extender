# --- !Ups

alter table cookie add column state varchar(4);
alter table creative add column state varchar(4);

# --- !Downs

alter table cookie drop column state;
alter table creative drop column state;
