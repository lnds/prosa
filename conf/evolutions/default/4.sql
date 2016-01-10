# --- DB Schema

# --- !Ups

ALTER TABLE author ADD twitter_handle text;

# --- !Downs

ALTER TABLE author DROP twitter_handle;
