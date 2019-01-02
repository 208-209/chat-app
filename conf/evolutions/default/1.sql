# --- !Ups
create table users (
  userId bigserial not null primary key ,
  userName varchar(15) not null
);

create table channels (
  channelId char(36) not null primary key ,
  channelName varchar(225) not null ,
  description varchar(225) ,
  createdBy bigint not null ,
  updatedAt timestamp not null
);

create table messages (
  messageId bigserial not null primary key ,
  message varchar(225) not null ,
  channelId char(36) not null ,
  createdBy bigint not null ,
  updatedAt timestamp not null
);

create index createdBy_channels on channels (createdBy);
create index channelId_messages on messages (channelId);
create index createdBy_messages on messages (createdBy);

# --- !Downs
drop table users;
drop table channels;
drop table messages;