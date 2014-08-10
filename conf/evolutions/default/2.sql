# Update Blogs

# --- !Ups

ALTER TABLE blog ADD use_avatar_as_logo boolean;

# --- !Downs

ALTER TABLE blog DROP use_avatar_as_logo;