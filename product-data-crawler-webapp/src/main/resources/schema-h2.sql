create table if not exists SEARCH_QUERIES
(
  ID BIGINT primary key auto_increment,
  SEARCH_TERM VARCHAR(500),
  CREATED_AT TIMESTAMP default CURRENT_TIMESTAMP() not null
);