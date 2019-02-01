# Chat app

Twitterのログイン認証を利用したチャットアプリケーション
https://play-chat-app.herokuapp.com/
## 要件定義
- Twitterアカウントでログインして認証することができる
- チャンネルが作れる
- 招待制のプライベートチャンネルが作れる
- チャンネルを編集・削除できる
- チャンネルにメッセージを送れる
- メッセージを削除できる
- ログインユーザーが表示される
- チャンネルをブックマークできる
- ブックマークを編集できる
## 用語定義
| 用語 | 英語表記 | 意味 |
| :-- | :-- | :-- |
| ユーザー | user | Chat appの利用者 |
| チャンネル | channel | 複数のユーザーがメッセージを送ることができる場所 |
| メッセージ | message | チャンネルに対してユーザーが送る文字列 |
| ブックマーク | bookmark | チャンネルに対してユーザーが行うお気に入り登録 |
## データモデリング
### User (users)
| 属性名 | 形式 | 内容 |
| :-- | :-- | :-- |
| userId | Long | TwitterのユーザーID (PK) |
| userName | String | Twitter のユーザー名(INDEX) |
| profileImageUrl | String | Twitterのプロフィール画像のURL |
| deleted | Boolean | 論理削除フラグ |
- user 1 ___ 0...* channel
- user 1 ___ 0...* message
- user 1 ___ 0...* bookmark
### Channel (channels)
| 属性名 | 形式 | 内容 |
| :-- | :-- | :-- |
| channelId | String(UUID) | チャンネルID (PK) |
| channelName | String | チャンネル名 |
| purpose | String | チャンネルの目的 |
| isPublic | Boolean | パブリックチャンネル: true, プライベートチャンネル: false |
| members | String | プライベートチャンネルにアクセスできるユーザーのIDを , で結合した文字列 |
| createdBy | Long | 作成者のユーザーID(INDEX)(FK) |
| updatedAt | OffsetDateTime | 更新日時(INDEX) |
- channel 1 ___ 0...* message
- channel 1 ___ 0...* bookmark
### Message (messages)
| 属性名 | 形式 | 内容 |
| :-- | :-- | :-- |
| messageId | String(UUID) | メッセージID (PK) |
| message | String | メッセージ |
| channelId | String(UUID) | チャンネルID(INDEX) |
| createdBy | Long | 投稿者のユーザーID(INDEX)(FK) |
| updatedAt | OffsetDateTime | 更新日時(INDEX) |
### Bookmark（bookmarks）
| 属性名 | 形式 | 内容 |
| :-- | :-- | :-- |
| channelId | String(UUID) | チャンネルID(複合PK)(FK) |
| userId | Long | TwitterのユーザーID(複合PK) |
| isBookmark | Boolean | ブックマーク(INDEX) |
## URL設計
### ページの URL 一覧
| パス | メソッド | ページ内容 |
| :-- | :-- | :-- |
| / | GET | トップページ |
| /channels/:channelId | GET | チャンネルの詳細、チャンネルの新規作成、チャンネルの編集 |
| /login | GET | ログイン |
| /logout | GET | ログアウト |
### Web API の URL 一覧
| パス | メソッド | 処理内容 | 利用方法 |
| :-- | :-- | :-- | :-- |
| /channels/:channelId | POST | チャンネルの新規作成 | フォーム |
| /channels/:channelId/update | POST | チャンネルの編集 | フォーム |
| /channels/:channelId/delete | POST | チャンネルの削除 | フォーム |
| /channels/:channelId/users/:userId/message | GET | メッセージの送信と削除 | WebSocket |
| /channels/:channelId/users/:userId/bookmark | POST | ブックマークの登録と編集 | AJAX |
## モジュール設計
### controllers モジュール一覧
| ファイル名 | 責務 |
| :-- | :-- |
| HomeController | トップページに関する処理 |
| ChannelController | チャンネルに関する処理 |
| MessageController | メッセージに関する処理 |
| BookmarkController | ブックマークに関する処理 |
| OAuthController | ログインに関する処理 |
### データモデル一覧
| ファイル名 | 責務 |
| :-- | :-- |
| User | ユーザーの定義と永続化 |
| Channel | チャンネルの定義と永続化|
| Message | メッセージの定義と永続化 |
| Bookmark | ブックマークの定義と永続化 |
