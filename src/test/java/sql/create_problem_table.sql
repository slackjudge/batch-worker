create table if not exists public.problem
(
    problem_id    bigint  not null
        primary key,
    problem_title varchar(255),
    problem_level integer not null,
    problem_url   varchar(500)
);