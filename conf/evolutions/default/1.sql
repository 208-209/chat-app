# --- !Ups
create table users (
                     userId bigint not null primary key ,
                     userName varchar(15) not null
);

create table channels (
                        channelId varchar(36) not null primary key ,
                        channelName varchar(225) not null ,
                        description varchar(225) ,
                        isPublic boolean not null,
                        members text ,
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

create table bookmarks (
                        channelId varchar(36) not null ,
                        userId bigint not null ,
                        isBookmark boolean not null ,
                        UNIQUE (channelId, userId)
);

create index createdBy_channels on channels (createdBy);
create index channelId_messages on messages (channelId);
create index createdBy_messages on messages (createdBy);
create index isBookmark_bookmarks on bookmarks (isBookmark);

insert into channels (channelId, channelName, description, isPublic, members, createdBy, updatedAt)
values ('general', 'general', 'チャンネルの説明を書く場所', 'true', '937000074978107392', 937000074978107392, '2019-01-03T15:30:30.412+09:00');

# --- !Downs
drop table users;
drop table channels;
drop table messages;
drop table bookmarks;