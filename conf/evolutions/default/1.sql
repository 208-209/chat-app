# --- !Ups
create table users (
  userId bigint not null primary key ,
  userName varchar(15) not null
);

create table channels (
  channelId varchar(36) not null primary key ,
  channelName varchar(225) not null ,
  description varchar(225) ,
  createdBy bigint not null ,
  updatedAt timestamp not null
);

create table messages (
  messageId varchar(36) not null primary key ,
  message varchar(225) not null ,
  channelId varchar(36) not null ,
  createdBy bigint not null ,
  updatedAt timestamp not null
);

create index createdBy_channels on channels (createdBy);
create index channelId_messages on messages (channelId);
create index createdBy_messages on messages (createdBy);

insert into channels (channelId, channelName, description, createdBy, updatedAt)
values ('general', 'general チャンネル', 'チャンネルの説明を書く場所', 937000074978107392, '2019-01-03T15:30:30.412+09:00');

# --- !Downs
drop table users;
drop table channels;
drop table messages;