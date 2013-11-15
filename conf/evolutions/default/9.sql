# --- !Ups

alter table creative add column uuid varchar(50);

create table campaign_package (
  id                        bigint not null,
  name                      varchar(255),
  variant                   varchar(255),
  constraint pk_package primary key (id))
;

create sequence campaign_package_seq;

alter table campaign add column campaign_package_id bigint;

alter table campaign add constraint fk_campaign_campaign_package_1 foreign key (campaign_package_id) references campaign_package (id) on delete restrict on update restrict;
create index ix_campaign_campaign_package_1 on campaign (campaign_package_id);

create unique index creative_uuid_id on creative (uuid);

# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists campaign_package;

SET REFERENTIAL_INTEGRITY TRUE;

drop sequence if exists campaign_package_seq;

alter table campaign drop column campaign_package_id;

alter table creative drop column uuid;
