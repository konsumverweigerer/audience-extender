# --- !Ups

alter table creative add column uuid varchar(50);

create table package (
  id                        bigint not null,
  name                      varchar(255),
  variant                   varchar(255),
  constraint pk_package primary key (id))
;

create sequence package_seq;

alter table campaign add column package_id bigint;

alter table campaign add constraint fk_campaign_package_1 foreign key (package_id) references package (id) on delete restrict on update restrict;
create index ix_campaign_package_1 on campaign (package_id);

create unique index uuid_id on creative (uuid);

# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists package;

SET REFERENTIAL_INTEGRITY TRUE;

drop sequence if exists package_seq;

alter table campaign drop column package_id;

alter table creative drop column uuid;
