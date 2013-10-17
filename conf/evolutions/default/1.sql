# --- !Ups

create table admin (
  id                        bigint not null,
  email                     varchar(255),
  name                      varchar(255),
  password                  varchar(255),
  admin_roles               varchar(255),
  constraint pk_admin primary key (id))
;

create table audience (
  id                        bigint not null,
  name                      varchar(255),
  constraint pk_audience primary key (id))
;

create table cookie (
  id                        bigint not null,
  name                      varchar(255),
  constraint pk_cookie primary key (id))
;

create table publisher (
  id                        bigint not null,
  name                      varchar(255),
  constraint pk_publisher primary key (id))
;


create table publisher_admin (
  publisher_id                   bigint not null,
  admin_id                       bigint not null,
  constraint pk_publisher_admin primary key (publisher_id, admin_id))
;
create sequence admin_seq;

create sequence audience_seq;

create sequence cookie_seq;

create sequence publisher_seq;




alter table publisher_admin add constraint fk_publisher_admin_publisher_01 foreign key (publisher_id) references publisher (id) on delete restrict on update restrict;

alter table publisher_admin add constraint fk_publisher_admin_admin_02 foreign key (admin_id) references admin (id) on delete restrict on update restrict;

# --- !Downs

drop table if exists admin;

drop table if exists audience;

drop table if exists cookie;

drop table if exists publisher;

drop table if exists publisher_admin;

SET REFERENTIAL_INTEGRITY TRUE;

drop sequence if exists admin_seq;

drop sequence if exists audience_seq;

drop sequence if exists cookie_seq;

drop sequence if exists publisher_seq;

