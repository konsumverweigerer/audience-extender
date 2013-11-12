# --- !Ups

create table campaign (
  id                        bigint not null,
  name                      varchar(255),
  constraint pk_campaign primary key (id))
;

create sequence campaign_seq;

alter table campaign add column publisher_id bigint;

alter table campaign add constraint fk_campaign_publisher_1 foreign key (publisher_id) references publisher (id) on delete restrict on update restrict;
create index ix_campaign_publisher_1 on campaign (publisher_id);

# --- !Downs

alter table campaign drop column publisher_id;

drop table if exists campaign;

drop sequence if exists campaign_seq;
