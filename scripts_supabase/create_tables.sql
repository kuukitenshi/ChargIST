drop table if exists chargers;
drop table if exists favorite_stations;
drop table if exists reviews;
drop table if exists stations;
drop table if exists users;

create table if not exists stations (
  id bigint primary key generated always as identity,
  "name" text not null,
  "latitude" float8 not null,
  "longitude" float8 not null,
  "paymentMethods" text not null,
  "nearbyServices" text,
  "avgRating" float4 default 0.0,
  "imageUrl" text
);

create table if not exists users (
  id bigint primary key generated always as identity,
  "username" text unique not null,
  "name" text not null,
  "password" text not null,
  "pictureUrl" text
);

create table if not exists chargers (
  id bigint primary key generated always as identity,
  "stationId" bigint not null,
  "type" text not null,
  "power" text not null,
  "price" float not null,
  "issue" text not null,
  "status" text not null,
  foreign key ("stationId") references stations("id") on delete cascade
);

create table if not exists favorite_stations (
  "stationId" bigint not null,
  "userId" bigint not null,
  primary key ("stationId", "userId"),
  foreign key ("stationId") references stations("id") on delete cascade,
  foreign key ("userId") references users("id") on delete cascade
);

create table if not exists reviews (
  "stationId" bigint not null,
  "userId" bigint not null,
  "rating" int not null,
  "date" bigint not null,
  "comment" text,
  primary key ("stationId", "userId"),
  foreign key ("stationId") references stations("id") on delete cascade,
  foreign key ("userId") references users("id") on delete cascade
);

alter publication supabase_realtime add table chargers;
alter publication supabase_realtime add table favorite_stations;
alter publication supabase_realtime add table reviews;
alter publication supabase_realtime add table stations;
alter publication supabase_realtime add table users;