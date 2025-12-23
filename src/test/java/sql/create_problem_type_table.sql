create table if not exists public.problem_type
(
    problem_type_id   bigint not null
        primary key,
    problem_type_name varchar(100) not null
);
