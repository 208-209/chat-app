# --- !Ups
create table users (
                     userId bigint not null ,
                     userName varchar(15) not null ,
                     profileImageUrl varchar(225) not null,
                     primary key (userId)
);
create index userName_users on users (userName);


create table channels (
                        channelId varchar(36) not null ,
                        channelName varchar(225) not null ,
                        purpose varchar(225) not null ,
                        isPublic boolean not null,
                        members text not null ,
                        createdBy bigint not null ,
                        updatedAt timestamp not null ,
                        primary key (channelId)
);
create index createdBy_channels on channels (createdBy);
create index updatedAt_channels on channels (updatedAt);


create table messages (
                        messageId varchar(36) not null  ,
                        message varchar(225) not null ,
                        channelId varchar(36) not null ,
                        createdBy bigint not null ,
                        updatedAt timestamp not null ,
                        primary key (messageId)
);
create index channelId_messages on messages (channelId);
create index createdBy_messages on messages (createdBy);
create index updatedAt_messages on messages (updatedAt);


create table bookmarks (
                         channelId varchar(36) not null ,
                         userId bigint not null ,
                         isBookmark boolean not null ,
                         primary key (channelId, userId)
);
create index isBookmark_bookmarks on bookmarks (isBookmark);

insert into channels (channelId, channelName, purpose, isPublic, members, createdBy, updatedAt)
values ('general', 'general', 'デフォルトのチャンネル', 'true', '937000074978107392', 937000074978107392, '2019-01-01T00:00:00.000+09:00');

# --- !Downs
drop table users;
drop table channels;
drop table messages;
drop table bookmarks;