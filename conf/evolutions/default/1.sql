# --- !Ups
create table users (
                     userId bigint not null ,
                     userName varchar(15) not null ,
                     profileImageUrl varchar(225) not null ,
                     deleted boolean not null ,
                     primary key (userId)
);
create index userName_users on users (userName);
create index deleted_users on users (deleted);


create table channels (
                        channelId varchar(36) not null ,
                        channelName varchar(32) not null ,
                        purpose varchar(64) not null ,
                        isPublic boolean not null ,
                        members text not null ,
                        createdBy bigint not null ,
                        updatedAt timestamp not null ,
                        primary key (channelId)
);
create index createdBy_channels on channels (createdBy);
create index updatedAt_channels on channels (updatedAt);


create table messages (
                        messageId varchar(36) not null  ,
                        message varchar(128) not null ,
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
values ('general', 'general', 'デフォルトのチャンネル', true, '945597672450166784', 945597672450166784, '2019-01-25T00:00:00.000+09:00');

insert into users (userId, userName, profileImageUrl, deleted)
values (945597672450166784, '208_209_bot', 'https://pbs.twimg.com/profile_images/1086588467423850496/oP_97e5P_normal.jpg', false);

# --- !Downs
drop table users;
drop table channels;
drop table messages;
drop table bookmarks;