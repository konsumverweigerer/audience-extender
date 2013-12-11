# --- !Ups

alter table campaign add column state varchar(4);
alter table campaign add column start_date timestamp;
alter table campaign add column end_date timestamp;

create table campaign_audience (
  campaign_id                   bigint not null,
  audience_id                   bigint not null,
  constraint pk_campaign_audience primary key (campaign_id, audience_id))
;

alter table campaign_audience add constraint fk_campaign_audience_campaign_01 foreign key (campaign_id) references campaign (id) on delete restrict on update restrict;
alter table campaign_audience add constraint fk_campaign_audience_audience_02 foreign key (audience_id) references audience (id) on delete restrict on update restrict;

create table if not exists audience_website (
  audience_id                   bigint not null,
  website_id                    bigint not null,
  constraint pk_audience_website primary key (audience_id, website_id))
;

alter table audience_website add constraint fk_audience_website_audience_01 foreign key (audience_id) references audience (id) on delete restrict on update restrict;
alter table audience_website add constraint fk_audience_website_website_02 foreign key (website_id) references website (id) on delete restrict on update restrict;

alter table audience add column state varchar(4);
alter table audience add column tracking varchar(1024);

alter table website add column email varchar(255);

alter table campaign_package add column start_date timestamp;
alter table campaign_package add column end_date timestamp;
alter table campaign_package add column count bigint;
alter table campaign_package add column reach bigint;
alter table campaign_package add column goal bigint;
alter table campaign_package add column buy_cpm decimal(6,6);
alter table campaign_package add column sales_cpm decimal(6,6);

alter table creative add column campaign_id bigint;

alter table creative add constraint fk_creative_campaign_1 foreign key (campaign_id) references campaign (id) on delete restrict on update restrict;
create index ix_creative_campaign_1 on creative (campaign_id);

# --- !Downs

alter table campaign drop column state;
alter table campaign drop column start_date;
alter table campaign drop column end_date;

alter table audience drop column state;
alter table audience drop column tracking;

alter table website drop column email;

alter table campaign_package drop column start_date;
alter table campaign_package drop column end_date;
alter table campaign_package drop column count;
alter table campaign_package drop column reach;
alter table campaign_package drop column goal;
alter table campaign_package drop column buy_cpm;
alter table campaign_package drop column sales_cpm;

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists website_audience;

drop table if exists campaign_audience;
drop table if exists audience_website;

SET REFERENTIAL_INTEGRITY TRUE;

alter table creative drop column campaign_id;

