--
-- PostgreSQL database dump
--

-- Dumped from database version 15.1 (Debian 15.1-1.pgdg110+1)
-- Dumped by pg_dump version 15.0

-- Started on 2022-11-28 14:42:06 CST

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- TOC entry 214 (class 1259 OID 24600)
-- Name: user; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public."user" (
    id bigint NOT NULL,
    name text,
    extension jsonb
);


ALTER TABLE public."user" OWNER TO postgres;

--
-- TOC entry 3319 (class 0 OID 24600)
-- Dependencies: 214
-- Data for Name: user; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public."user" (id, name, extension) FROM stdin;
1597112785711599618	张三	{"age": 25, "sex": "男", "addr": "上海浦东", "city": "上海", "phone": "131", "family": ["dad", "mum"]}
1597112785724182530	李四	{"age": 25, "sex": "男", "addr": "上海浦东", "city": "上海", "phone": "131", "family": ["dad", "mum"]}
1597112785724182531	王五	{"age": 27, "sex": "男", "addr": "北京海淀", "city": "北京", "phone": "131", "family": ["dad", "mum"]}
1597112785724182532	小红	{"age": 18, "sex": "女", "addr": "北京海淀", "city": "北京", "phone": "131", "family": ["dad", "mum"]}
1597112785728376833	小红1	{"age": 18, "sex": "女", "addr": "北京海淀", "city": "北京", "phone": "131", "family": ["dad", "mum"]}
1597112785728376834	小红1	{"age": 18, "sex": "女", "addr": "上海浦西", "city": "上海", "phone": "131", "family": ["dad", "mum"]}
1597112785728376835	小红11	{"age": 28, "sex": "女", "addr": "上海浦西", "city": "上海", "phone": "131", "family": ["dad", "mum"]}
\.


--
-- TOC entry 3176 (class 2606 OID 24606)
-- Name: user user_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."user"
    ADD CONSTRAINT user_pkey PRIMARY KEY (id);


-- Completed on 2022-11-28 14:42:07 CST

--
-- PostgreSQL database dump complete
--

