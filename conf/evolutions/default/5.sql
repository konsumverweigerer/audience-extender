# --- !Ups

alter table audience add column publisher_id bigint;

alter table audience add constraint fk_audience_publisher_1 foreign key (publisher_id) references publisher (id) on delete restrict on update restrict;
create index ix_audience_publisher_1 on audience (publisher_id);

# --- !Downs

alter table audience drop column publisher_id;
