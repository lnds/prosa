# --- DB Schema

# --- !Ups

CREATE TABLE author (
    id character varying(250) NOT NULL,
    bio text,
    email text,
    fullname text,
    nickname text,
    password text,
    permission text,
    PRIMARY KEY(id)
);

CREATE TABLE blog (
    id character varying(250) NOT NULL,
    alias character varying(250),
    description text,
    image text,
    logo text,
    name text,
    owner character varying(45),
    url text,
    PRIMARY KEY(id)
);

CREATE TABLE image (
    id character varying(250) NOT NULL,
    contentType text,
    filename text,
    url text,
    PRIMARY KEY(id)
);

CREATE TABLE post (
    id character varying(250) NOT NULL,
    author character varying(45),
    blog character varying(45),
    content text,
    created timestamp,
    draft boolean,
    image text,
    published timestamp,
    slug text,
    subtitle text,
    title text,
    PRIMARY KEY(id)
);

INSERT INTO author (id, bio, email, fullname, nickname, password, permission) VALUES ('3700a344-0d54-11e4-9c52-0d90835860a2-75920dab','','admin@prosa.io','', 'admin','$2a$10$x7pLuXxXiWp5Z4krveuT6eOsXwxWkJk2BpGCx0BszpAbfYYXGVOQO','Administrator');

CREATE UNIQUE INDEX idx_alias ON blog (alias);
CREATE INDEX idx_post_blog ON post (blog);

# --- !Downs

DROP TABLE IF EXISTS author;
DROP TABLE IF EXISTS blog;
DROP TABLE IF EXISTS image;
DROP TABLE IF EXISTS post;

