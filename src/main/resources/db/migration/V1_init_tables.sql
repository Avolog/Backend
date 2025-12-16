-- Groups service DDL (PostgreSQL)
-- 요구: uuid-ossp 또는 pgcrypto(gen_random_uuid) 확장 사용

-- 그룹 테이블
CREATE TABLE IF NOT EXISTS groups (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT NULL,
    owner_user_id UUID NOT NULL,
    join_password_hash VARCHAR(255) NOT NULL,
    join_password_updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 그룹 멤버십
CREATE TABLE IF NOT EXISTS group_members (
    group_id UUID NOT NULL REFERENCES groups (id) ON DELETE CASCADE,
    user_id UUID NOT NULL,
    role VARCHAR(20) NOT NULL, -- OWNER, MANAGER, MEMBER
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, INVITED, LEFT, REMOVED
    joined_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT group_members_pk PRIMARY KEY (group_id, user_id)
);
CREATE INDEX IF NOT EXISTS idx_group_members_user ON group_members (user_id);

-- 초대 테이블
CREATE TABLE IF NOT EXISTS group_invites (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    group_id UUID NOT NULL REFERENCES groups (id) ON DELETE CASCADE,
    inviter_user_id UUID NOT NULL,
    target_user_id UUID NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'MEMBER',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- PENDING, ACCEPTED, REVOKED, EXPIRED
    expires_at TIMESTAMPTZ NULL,
    accepted_at TIMESTAMPTZ NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT group_invites_unique_target UNIQUE (group_id, target_user_id, status) DEFERRABLE INITIALLY IMMEDIATE
);
CREATE INDEX IF NOT EXISTS idx_group_invites_target ON group_invites (target_user_id, status);

-- 트리거 템플릿(옵션): updated_at 자동 업데이트
-- CREATE TRIGGER trg_groups_updated
-- BEFORE UPDATE ON groups
-- FOR EACH ROW EXECUTE FUNCTION set_updated_at();

