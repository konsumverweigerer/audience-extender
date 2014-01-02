# --- !Ups

alter table campaign add column variant varchar(20);

# --- !Downs

alter table campaign drop column variant;
