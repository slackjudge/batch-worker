create table public.users_problem
(
    user_id          bigint                not null
        constraint fk_up_user
            references public.users,
    problem_id       bigint                not null
        constraint fk_up_problem
            references public.problem,
    is_solved        boolean default false not null,
    solved_time      timestamp
);

CREATE UNIQUE INDEX idx_user_problem_unique
    ON public.users_problem (user_id, problem_id);
