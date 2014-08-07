--
-- PostgreSQL database dump
--

SET statement_timeout = 0;
SET lock_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;

--
-- Name: plpgsql; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS plpgsql WITH SCHEMA pg_catalog;


--
-- Name: EXTENSION plpgsql; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION plpgsql IS 'PL/pgSQL procedural language';


SET search_path = public, pg_catalog;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- Name: Author; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE "Author" (
    id text NOT NULL,
    bio text,
    email text,
    fullname text,
    nickname text,
    password text,
    permission text
);


ALTER TABLE public."Author" OWNER TO postgres;

--
-- Name: Blog; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE "Blog" (
    id text NOT NULL,
    alias text,
    description text,
    image text,
    logo text,
    name text,
    owner character varying(45),
    url text
);


ALTER TABLE public."Blog" OWNER TO postgres;

--
-- Name: Image; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE "Image" (
    id text NOT NULL,
    "contentType" text,
    filename text,
    url text
);


ALTER TABLE public."Image" OWNER TO postgres;

--
-- Name: Post; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE "Post" (
    id text NOT NULL,
    author character varying(45),
    blog character varying(45),
    content text,
    created timestamp without time zone,
    draft boolean,
    image text,
    published timestamp without time zone,
    slug text,
    subtitle text,
    title text
);


ALTER TABLE public."Post" OWNER TO postgres;

--
-- Data for Name: Author; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY "Author" (id, bio, email, fullname, nickname, password, permission) FROM stdin;
3700a344-0d54-11e4-9c52-0d90835860a2-75920dab	\N	admin@prosa.io	\N	admin	$2a$10$x7pLuXxXiWp5Z4krveuT6eOsXwxWkJk2BpGCx0BszpAbfYYXGVOQO	Administrator
\.


--
-- Data for Name: Blog; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY "Blog" (id, alias, description, image, logo, name, owner, url) FROM stdin;
\.


--
-- Data for Name: Image; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY "Image" (id, "contentType", filename, url) FROM stdin;
\.


--
-- Data for Name: Post; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY "Post" (id, author, blog, content, created, draft, image, published, slug, subtitle, title) FROM stdin;
\.


--
-- Name: Author_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY "Author"
    ADD CONSTRAINT "Author_pkey" PRIMARY KEY (id);


--
-- Name: Blog_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY "Blog"
    ADD CONSTRAINT "Blog_pkey" PRIMARY KEY (id);


--
-- Name: Image_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY "Image"
    ADD CONSTRAINT "Image_pkey" PRIMARY KEY (id);


--
-- Name: Post_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY "Post"
    ADD CONSTRAINT "Post_pkey" PRIMARY KEY (id);


--
-- Name: idx_alias; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE UNIQUE INDEX idx_alias ON "Blog" USING btree (alias);


--
-- Name: idx_post_blog; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX idx_post_blog ON "Post" USING btree (blog);


--
-- Name: public; Type: ACL; Schema: -; Owner: ediaz
--

REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM ediaz;
GRANT ALL ON SCHEMA public TO ediaz;
GRANT ALL ON SCHEMA public TO PUBLIC;


--
-- PostgreSQL database dump complete
--

