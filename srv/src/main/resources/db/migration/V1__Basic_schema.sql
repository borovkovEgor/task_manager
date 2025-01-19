create schema if not exists task_manager;

create table if not exists task_manager.users (
    id serial primary key,
    username varchar not null unique,
    password varchar not null,
    role varchar not null check ( role in ('ROLE_USER', 'ROLE_ADMIN') ),
    created_by bigint,
    created_at timestamp default current_timestamp,
    updated_at timestamp default current_timestamp
);

create table if not exists task_manager.tasks (
   id serial primary key,
   title varchar not null,
   description text,
   status varchar not null check ( status in ('NEW', 'IN_PROGRESS', 'COMPLETED', 'OVERDUE') ),
   assigned_to bigint references task_manager.users(id) not null,
   created_by bigint references task_manager.users(id) not null,
   created_at timestamp default current_timestamp,
   updated_at timestamp default current_timestamp,
   deadline timestamp,
   group_id bigint references task_manager.users(id) not null
);