create table public."switch"(
	id serial primary key,
	name varchar(128) not null,
	manufacturer varchar(128) not null,
	type varchar(16) not null,
	actuation_force integer,
	key_travel integer,
	optical bool default false,
	pins integer default 3
);
