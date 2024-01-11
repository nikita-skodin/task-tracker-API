create table "user"
(
    id              bigint generated by default as identity
        constraint user_pk
            primary key,
    username        varchar not null,
    password        varchar not null,
    email           varchar not null,
    role            varchar not null,
    activation_code varchar
);

create unique index user_email_uindex
    on "user" (email);

create unique index user_username_uindex
    on "user" (username);

create table "project"
(
    id         bigint generated by default as identity
        constraint project_pk
            primary key,
    name       varchar   not null
        constraint check_name
            check ((length((name)::text) >= 3) AND (length((name)::text) <= 20)),
    created_at timestamp not null,
    user_id    bigint    not null
        constraint project_user_id_fk
            references "user"
            on delete cascade
);

create table "task-state"
(
    id                     bigint generated by default as identity
        constraint "task-state_pk"
            primary key,
    name                   varchar   not null
        constraint check_name
            check ((length((name)::text) >= 3) AND (length((name)::text) <= 20)),
    created_at             timestamp not null,
    project_id             bigint
        constraint "task-state_project_id_fk"
            references project
            on delete cascade,
    next_task_state_id     bigint
        constraint next_task_state_id_constraint
            references "task-state"
            on delete cascade,
    previous_task_state_id bigint
        constraint previous_task_state_id_constraint
            references "task-state"
            on delete cascade
);

create table "task"
(
    id            bigint generated by default as identity
        constraint task_pk
            primary key,
    name          varchar   not null
        constraint check_name
            check ((length((name)::text) >= 3) AND (length((name)::text) <= 20)),
    created_at    timestamp not null,
    description   varchar(100),
    task_state_id bigint    not null
        constraint task_state_id_constraint
            references "task-state"
            on delete cascade
);