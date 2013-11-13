# --- !Ups

alter table admin add column publisher_id bigint;

alter table admin add constraint fk_admin_publisher_1 foreign key (publisher_id) references publisher (id) on delete restrict on update restrict;
create index ix_admin_publisher_1 on admin (publisher_id);

# --- !Downs

alter table admin drop column publisher_id;
