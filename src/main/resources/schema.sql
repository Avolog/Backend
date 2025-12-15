CREATE TABLE IF NOT EXISTS categories (
    id UUID PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    owner_type VARCHAR(10) NOT NULL,
    owner_user_id UUID NULL,
    group_id UUID NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    UNIQUE (owner_user_id, name),
    UNIQUE (group_id, name)
);

CREATE TABLE IF NOT EXISTS routines (
    id UUID PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    routine_type VARCHAR(10) NOT NULL,
    period VARCHAR(10) NOT NULL,
    target_count INT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    week_start_day INT NULL,
    month_base_day INT NULL,
    creator_user_id UUID NOT NULL,
    group_id UUID NULL,
    category_id UUID NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE IF NOT EXISTS todos (
    id UUID PRIMARY KEY,
    creator_user_id UUID NOT NULL,
    group_id UUID NULL,
    category_id UUID NULL,
    routine_id UUID NULL,
    title VARCHAR(200) NOT NULL,
    description VARCHAR(2000),
    status VARCHAR(20) NOT NULL,
    todo_date DATE NOT NULL,
    period_key VARCHAR(20) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_todos_routine_period ON todos (routine_id, period_key);
CREATE INDEX IF NOT EXISTS idx_todos_creator ON todos (creator_user_id);
CREATE INDEX IF NOT EXISTS idx_todos_group ON todos (group_id);

CREATE TABLE IF NOT EXISTS todo_assignees (
    todo_id UUID NOT NULL,
    user_id UUID NOT NULL,
    PRIMARY KEY (todo_id, user_id)
);

CREATE TABLE IF NOT EXISTS todo_completions (
    id UUID PRIMARY KEY,
    todo_id UUID NOT NULL,
    routine_id UUID NULL,
    completed_by UUID NOT NULL,
    period_key VARCHAR(20) NOT NULL,
    completed_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_todo_completions_routine ON todo_completions (routine_id, period_key);
