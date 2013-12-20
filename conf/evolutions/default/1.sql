# --- !Ups

create sequence admin_seq;

create table admin (
  id                        bigint not null default(nextval('admin_seq')),
  email                     varchar(255),
  name                      varchar(120),
  password                  varchar(255),
  password_change_token     varchar(100),
  password_change_token_date timestamp,
  email_confirm_token       varchar(100),
  locked                    boolean,
  need_password_change      boolean,
  created                   timestamp,
  logged_in                 timestamp,
  changed                   timestamp,
  url                       varchar(1024),
  streetaddress1            varchar(100),
  streetaddress2            varchar(100),
  streetaddress3            varchar(100),
  state                     varchar(50),
  country                   varchar(50),
  telephone                 varchar(20),
  admin_roles               varchar(255),
  publisher_id              bigint,
  constraint pk_admin primary key (id))
;

create sequence campaign_seq;

create table campaign (
  id                        bigint not null default(nextval('campaign_seq')),
  name                      varchar(120),
  created                   timestamp,
  value                     decimal(6,4),
  publisher_id              bigint not null,
  campaign_package_id       bigint,
  state                     varchar(4),
  start_date                timestamp,
  end_date                  timestamp,
  constraint pk_campaign primary key (id))
;

create sequence campaign_package_seq;

create table campaign_package (
  id                        bigint not null default(nextval('campaign_package_seq')),
  name                      varchar(120),
  created                   timestamp,
  variant                   varchar(20),
  state                     varchar(4),
  publisher_id              bigint,
  campaign_id               bigint,
  campaign_package_id       bigint,
  start_date                timestamp,
  end_date                  timestamp,
  impressions               bigint,
  reach                     bigint,
  goal                      bigint,
  buy_cpm                   decimal(6,6),
  sales_cpm                 decimal(6,6),
  constraint pk_package primary key (id))
;

create sequence campaign_package_preview_seq;

create table campaign_package_preview (
  id                        bigint not null default(nextval('campaign_package_preview_seq')),
  created                   timestamp,
  variant                   varchar(20),
  data                      bytea,
  campaign_package_id       bigint,
  constraint pk_campaign_package_preview primary key (id))
;

create sequence audience_seq;

create table audience (
  id                        bigint not null default(nextval('audience_seq')),
  name                      varchar(120),
  publisher_id              bigint not null,
  created                   timestamp,
  state                     varchar(4),
  tracking                  varchar(1024),
  constraint pk_audience primary key (id))
;

create table campaign_audience (
  campaign_id                   bigint not null,
  audience_id                   bigint not null,
  constraint pk_campaign_audience primary key (campaign_id, audience_id))
;

create sequence cookie_seq;

create table cookie (
  id                        bigint not null default(nextval('cookie_seq')),
  name                      varchar(120),
  created                   timestamp,
  variant                   varchar(20),
  uuid                      varchar(50),
  content                   varchar(1024),
  website_id                bigint not null,
  audience_id               bigint not null,
  pathhash                  bigint,
  state                     varchar(4),
  constraint pk_cookie primary key (id))
;

create sequence cookie_stat_data_seq;

create table cookie_stat_data (
  id                        bigint not null default(nextval('cookie_stat_data_seq')),
  timestep                  varchar(255),
  views                     bigint,
  cookie_id                 bigint not null,
  sub                       varchar(64),
  constraint pk_cookie_stat_data primary key (id))
;

create sequence website_seq;

create table website (
  id                        bigint not null default(nextval('website_seq')),
  name                      varchar(120),
  created                   timestamp,
  uuid                      varchar(50),
  url                       varchar(1024),
  email                     varchar(255),
  publisher_id                bigint,
  constraint pk_website primary key (id))
;

create sequence website_preview_seq;

create table website_preview (
  id                        bigint not null default(nextval('website_preview_seq')),
  created                   timestamp,
  variant                   varchar(20),
  data                      bytea,
  website_id                bigint,
  constraint pk_website_preview primary key (id))
;

create table audience_website (
  audience_id                   bigint not null,
  website_id                    bigint not null,
  constraint pk_audience_website primary key (audience_id, website_id))
;

create sequence path_target_seq;

create table path_target (
  id                        bigint not null default(nextval('path_target_seq')),
  url_path                  varchar(255),
  variant                   varchar(20),
  audience_id               bigint,
  website_id                bigint,
  constraint pk_path_target primary key (id))
;

create sequence creative_seq;

create table creative (
  id                        bigint not null default(nextval('creative_seq')),
  name                      varchar(120),
  created                   timestamp,
  variant                   varchar(20),
  uuid                      varchar(50),
  url                       varchar(1024),
  data                      bytea,
  campaign_id               bigint,
  state                     varchar(4),
  constraint pk_creative primary key (id))
;

create sequence creative_stat_data_seq;

create table creative_stat_data (
  id                        bigint not null default(nextval('creative_stat_data_seq')),
  timestep                  varchar(20),
  views                     bigint,
  cpm                       decimal(6,4),
  cost                      decimal(6,4),
  revenue                   decimal(6,4),
  creative_id               bigint not null,
  constraint pk_creative_stat_data primary key (id))
;

create sequence publisher_seq;

create table publisher (
  id                        bigint not null default(nextval('publisher_seq')),
  name                      varchar(120),
  created                   timestamp,
  changed                   timestamp,
  url                       varchar(1024),
  streetaddress1            varchar(100),
  streetaddress2            varchar(100),
  streetaddress3            varchar(100),
  state                     varchar(50),
  country                   varchar(50),
  telephone                 varchar(20),
  constraint pk_publisher primary key (id))
;

create table publisher_admin (
  publisher_id                   bigint not null,
  admin_id                       bigint not null,
  constraint pk_publisher_admin primary key (publisher_id, admin_id))
;

alter table admin add constraint fk_admin_publisher_1 foreign key (publisher_id) references publisher (id) on delete restrict on update restrict;
create index ix_admin_publisher_1 on admin (publisher_id);

alter table campaign add constraint fk_campaign_publisher_1 foreign key (publisher_id) references publisher (id) on delete restrict on update restrict;
create index ix_campaign_publisher_1 on campaign (publisher_id);

alter table creative add constraint fk_creative_campaign_1 foreign key (campaign_id) references campaign (id) on delete restrict on update restrict;
create index ix_creative_campaign_1 on creative (campaign_id);

alter table path_target add constraint fk_path_target_audience_1 foreign key (audience_id) references audience (id) on delete restrict on update restrict;
create index ix_path_target_audience_1 on path_target (audience_id);

alter table path_target add constraint fk_path_target_website_1 foreign key (website_id) references website (id) on delete restrict on update restrict;
create index ix_path_target_website_1 on path_target (website_id);

alter table campaign_audience add constraint fk_campaign_audience_campaign_01 foreign key (campaign_id) references campaign (id) on delete restrict on update restrict;
alter table campaign_audience add constraint fk_campaign_audience_audience_02 foreign key (audience_id) references audience (id) on delete restrict on update restrict;

alter table audience_website add constraint fk_audience_website_audience_01 foreign key (audience_id) references audience (id) on delete restrict on update restrict;
alter table audience_website add constraint fk_audience_website_website_02 foreign key (website_id) references website (id) on delete restrict on update restrict;

alter table audience add constraint fk_audience_publisher_1 foreign key (publisher_id) references publisher (id) on delete restrict on update restrict;
create index ix_audience_publisher_1 on audience (publisher_id);

alter table website add constraint fk_website_publisher_1 foreign key (publisher_id) references publisher (id) on delete restrict on update restrict;
create index ix_website_publisher_1 on website (publisher_id);

alter table website_preview add constraint fk_website_preview_website_1 foreign key (website_id) references website (id) on delete restrict on update restrict;
create index ix_website_preview_website_1 on website_preview (website_id);

alter table cookie add constraint fk_cookie_website_1 foreign key (website_id) references website (id) on delete restrict on update restrict;
create index ix_cookie_website_1 on cookie (website_id);

alter table cookie add constraint fk_cookie_audience_1 foreign key (audience_id) references audience (id) on delete restrict on update restrict;
create index ix_cookie_audience_1 on cookie (audience_id);

alter table campaign add constraint fk_campaign_campaign_package_1 foreign key (campaign_package_id) references campaign_package (id) on delete restrict on update restrict;
create index ix_campaign_campaign_package_1 on campaign (campaign_package_id);

alter table campaign_package add constraint fk_campaign_package_campaign_1 foreign key (campaign_id) references campaign (id) on delete restrict on update restrict;
create index ix_campaign_package_campaign_1 on campaign_package (campaign_id);

alter table campaign_package add constraint fk_campaign_package_publisher_1 foreign key (publisher_id) references publisher (id) on delete restrict on update restrict;
create index ix_campaign_package_publisher_1 on campaign_package (publisher_id);

alter table campaign_package add constraint fk_campaign_package_campaign_package_1 foreign key (campaign_package_id) references campaign_package (id) on delete restrict on update restrict;
create index ix_campaign_package_campaign_package_1 on campaign_package (campaign_package_id);

alter table campaign_package_preview add constraint fk_campaign_package_preview_campaign_package_1 foreign key (campaign_package_id) references campaign_package (id) on delete restrict on update restrict;
create index ix_campaign_package_preview_campaign_package_1 on campaign_package_preview (campaign_package_id);

alter table cookie_stat_data add constraint fk_cookie_stat_data_cookie_1 foreign key (cookie_id) references cookie (id) on delete restrict on update restrict;
create index ix_cookie_stat_data_cookie_1 on cookie_stat_data (cookie_id);

alter table creative_stat_data add constraint fk_creative_stat_data_creative_1 foreign key (creative_id) references creative (id) on delete restrict on update restrict;
create index ix_creative_stat_data_creative_1 on creative_stat_data (creative_id);

alter table publisher_admin add constraint fk_publisher_admin_publisher_01 foreign key (publisher_id) references publisher (id) on delete restrict on update restrict;

alter table publisher_admin add constraint fk_publisher_admin_admin_02 foreign key (admin_id) references admin (id) on delete restrict on update restrict;

create unique index uuid_id on cookie (uuid);

create unique index creative_uuid_id on creative (uuid);

# --- !Downs

drop table if exists publisher_admin;

drop table if exists campaign_audience;

drop table if exists admin;

drop table if exists creative_stat_data;

drop table if exists creative;

alter table campaign_package drop column campaign_id;

drop table if exists campaign;

drop table if exists campaign_package_preview;

drop table if exists campaign_package;

drop table if exists cookie_stat_data;

drop table if exists cookie;

drop table if exists audience_website;

drop table if exists path_target;

drop table if exists audience;

drop table if exists website_preview;

drop table if exists website;

drop table if exists publisher;

drop sequence if exists admin_seq;

drop sequence if exists website_preview_seq;

drop sequence if exists website_seq;

drop sequence if exists campaign_seq;

drop sequence if exists audience_seq;

drop sequence if exists campaign_package_preview_seq;

drop sequence if exists campaign_package_seq;

drop sequence if exists cookie_seq;

drop sequence if exists cookie_stat_data_seq;

drop sequence if exists creative_seq;

drop sequence if exists creative_stat_data_seq;

drop sequence if exists path_target_seq;

drop sequence if exists publisher_seq;

