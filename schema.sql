create database web default charset='utf8';
create user social identified by 'soci@l';
grant all on web.* to social;
use web;
create table member(
    id serial, 
    name varchar(100), 
    email varchar(100) unique, 
    password varchar(200) 
);
