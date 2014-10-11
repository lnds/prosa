# --- DB Schema

# --- !Ups

ALTER TABLE blog ADD status int not null default 0;

# --- !Downs

ALTER TABLE blog DROP status;
