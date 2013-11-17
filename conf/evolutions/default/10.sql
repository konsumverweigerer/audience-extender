# --- !Ups

create table path_target (
  id                        bigint not null,
  url_path                  varchar(255),
  variant                   varchar(255),
  audience_id               bigint,
  website_id                bigint,
  constraint pk_path_target primary key (id))
;

alter table path_target add constraint fk_path_target_audience_1 foreign key (audience_id) references audience (id) on delete restrict on update restrict;
create index ix_path_target_audience_1 on path_target (audience_id);

alter table path_target add constraint fk_path_target_website_1 foreign key (website_id) references website (id) on delete restrict on update restrict;
create index ix_path_target_website_1 on path_target (website_id);

create sequence path_target_seq;

# --- !Downs

drop table if exists path_target;

drop sequence if exists path_target_seq;
