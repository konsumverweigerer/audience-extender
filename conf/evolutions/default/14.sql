# --- !Ups

alter table campaign_package add column campaign_id bigint;

alter table campaign_package add constraint fk_campaign_package_campaign_1 foreign key (campaign_id) references campaign (id) on delete restrict on update restrict;
create index ix_campaign_package_campaign_1 on campaign_package (campaign_id);

# --- !Downs

alter table campaign_package drop column campaign_id;
