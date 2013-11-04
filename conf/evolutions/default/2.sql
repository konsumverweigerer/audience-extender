# --- !Ups

alter table admin add column email_confirm_token varchar(255);

# --- !Downs

alter table admin drop column emai_confirm_token;
