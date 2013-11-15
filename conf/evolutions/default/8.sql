# --- !Ups

create table website (
  id                        bigint not null,
  name                      varchar(255),
  constraint pk_website primary key (id))
;

create table audience_website (
  audience_id                   bigint not null,
  website_id                    bigint not null,
  constraint pk_audience_website primary key (audience_id, website_id))
;

create sequence website_seq;

alter table cookie add column website_id bigint;

alter table campaign add column value decimal(6,4);

alter table cookie add constraint fk_cookie_website_1 foreign key (website_id) references website (id) on delete restrict on update restrict;
create index ix_cookie_website_1 on cookie (website_id);

# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists audience_website;

drop table if exists website;

SET REFERENTIAL_INTEGRITY TRUE;

drop sequence if exists website_seq;

alter table cookie drop column website_id;

alter table campaign drop column value;
