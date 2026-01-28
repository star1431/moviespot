show databases;
use moviespotdb;
show tables;

select * from users;
select * from refresh_tokens;

select * from genres;
select * from keywords;
select * from movies;
select * from movie_types;
-- 트레일러 URL 컬럼 확인
select movie_id, tmdb_id, title, trailer_url from movies order by movie_id desc limit 20;
select * from user_rating;
select * from watched_movie;
select * from reviews;
select * from user_genre;
select * from user_keyword;

-- 리뷰 작성 시 필요한 movieId(DB movie_id) 찾기
select movie_id, tmdb_id, title from movies order by created_at desc limit 20;
select movie_id, tmdb_id, title from movies where tmdb_id = 550;
