# --- !Ups

create table creative_stat_data (
  id                        bigint not null,
  timestep                  varchar(255),
  views                     bigint,
  creative_id                 bigint,
  constraint pk_creative_stat_data primary key (id))
;

create sequence creative_stat_data_seq;

alter table creative_stat_data add constraint fk_creative_stat_data_creative_1 foreign key (creative_id) references creative (id) on delete restrict on update restrict;
create index ix_creative_stat_data_creative_1 on creative_stat_data (creative_id);

alter table cookie add column audience_id bigint;

alter table cookie add constraint fk_cookie_audience_1 foreign key (audience_id) references audience (id) on delete restrict on update restrict;
create index ix_cookie_audience_1 on cookie (audience_id);

# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists creative_stat_data;

SET REFERENTIAL_INTEGRITY TRUE;

drop sequence if exists creative_stat_data_seq;

alter table cookie drop column audience_id;
