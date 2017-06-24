# --- DB Schema

# --- !Ups

ALTER TABLE blog ADD show_ads boolean;
ALTER TABLE blog ADD ads_code text;

# --- !Downs

ALTER TABLE blog DROP show_ads;
ALTER TABLE blog DROP ads_code;