# --- DB Schema

# --- !Ups

ALTER TABLE blog ADD twitter_handle text;

# --- !Downs

ALTER TABLE blog DROP twitter_handle;