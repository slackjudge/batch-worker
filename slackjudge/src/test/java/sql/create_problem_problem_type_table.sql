create table if not exists public.problem_problem_type
(
    problem_type_id bigint not null
        constraint fk_ptm_type
            references public.problem_type,
    problem_id      bigint not null
        constraint fk_ptm_problem
            references public.problem
);