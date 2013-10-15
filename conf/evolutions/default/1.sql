# --- !Ups
create table admin (
  email                     varchar(255) not null,
  name                      varchar(255),
  password                  varchar(255),
  constraint pk_admin primary key (email))
;

create sequence admin_seq;
# --- !Downs
drop table if exists admin;

drop sequence if exists admin_seq;

