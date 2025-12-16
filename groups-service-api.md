# Groups Service API & Events

본 문서는 `groups-service_intro.md` 계획에 따라 그룹 서비스의 HTTP API와 이벤트 포맷을 명세합니다. 모든 API는 JWT 인증을 요구하며, 응답 본문이 없는 경우 204를 사용합니다.

## 1. 그룹 가입 (이름 + 비밀번호)

### `POST /groups/join`
- body: `{ "groupName": "Avolog", "joinPassword": "1234" }`
- 처리:
  1. `groupName`으로 그룹 조회 (없으면 404)
  2. `joinPassword` 검증 실패 시 401 (같은 메시지로 그룹 존재 여부 노출 방지)
  3. 이미 멤버면 409
  4. 멤버로 등록 후 200 + 멤버십 정보
- response 200:
```json
{
  "groupId": "uuid",
  "role": "MEMBER",
  "joinedAt": "2025-12-15T13:05:30Z"
}
```

## 2. 그룹 생성

### `POST /groups`
- body: `{ "name": "Avolog", "description": "team notes", "joinPassword": "1234" }`
- 제약: `name` 중복 시 409, `joinPassword` 누락 시 400
- 처리: 그룹 생성 후 OWNER를 group_members에 등록
- response 201:
```json
{
  "groupId": "uuid",
  "name": "Avolog",
  "description": "team notes",
  "ownerUserId": "uuid",
  "createdAt": "2025-12-15T13:05:30Z"
}
```

## 3. 가입 비밀번호 변경

### `PATCH /groups/{groupId}/join-password`
- body: `{ "newJoinPassword": "abcd" }`
- 권한: OWNER (추후 MANAGER 확장 가능)
- 처리: 비밀번호 해시 갱신 및 `join_password_updated_at` 갱신
- response: 204

## 4. 초대 기반 가입 API

### `POST /groups/{groupId}/invites` (MANAGER+)
- body: `{ "targetUserId": "uuid", "role": "MEMBER", "expiresAt": "2025-12-20T00:00:00Z" }`
- 응답: 201 + 초대 정보

### `GET /invites/received`
- 설명: 내게 온 초대 목록 조회
- 응답: 200 `[ { "inviteId": "uuid", "groupId": "uuid", "groupName": "Avolog", "inviterUserId": "uuid", "role": "MEMBER", "expiresAt": "2025-12-20T00:00:00Z", "status": "PENDING", "createdAt": "..."} ]`

### `POST /invites/{inviteId}/accept`
- 처리: 초대 상태를 ACCEPTED로 전환 후 멤버 등록, 만료/취소/중복 시 409
- 응답: 200 + 멤버십 정보

### `POST /groups/{groupId}/invites/{inviteId}/revoke` (MANAGER+)
- 처리: 초대 상태를 REVOKED로 변경
- 응답: 204

## 5. 추가 유틸 API (선택)

- `GET /groups/{groupId}`: 그룹 상세 + 멤버수, 내 역할
- `GET /groups/{groupId}/members`: 멤버 목록 페이징
- `DELETE /groups/{groupId}/members/{userId}` (OWNER/MANAGER): 멤버 추방, 본인이면 탈퇴

## 6. 이벤트 포맷 (SNS → SQS → activity-service)

모든 이벤트 공통 필드:
```json
{
  "eventId": "uuid",
  "eventType": "GroupCreated",
  "occurredAt": "2025-12-15T13:05:30Z",
  "producer": "groups-service",
  "data": { "...": "..." }
}
```

### 6.1 GroupCreated
```json
{
  "data": {
    "groupId": "uuid",
    "name": "Avolog",
    "ownerUserId": "uuid",
    "createdAt": "2025-12-15T13:05:30Z"
  }
}
```

### 6.2 GroupJoinRequested
> `POST /groups/join` 성공 시 발행. 이름 기반 가입 흐름 추적용.
```json
{
  "data": {
    "groupId": "uuid",
    "userId": "uuid",
    "via": "NAME_PASSWORD",
    "joinedAt": "2025-12-15T13:05:30Z",
    "role": "MEMBER"
  }
}
```

### 6.3 GroupInviteCreated
```json
{
  "data": {
    "inviteId": "uuid",
    "groupId": "uuid",
    "inviterUserId": "uuid",
    "targetUserId": "uuid",
    "role": "MEMBER",
    "expiresAt": "2025-12-20T00:00:00Z"
  }
}
```

### 6.4 GroupInviteAccepted
```json
{
  "data": {
    "inviteId": "uuid",
    "groupId": "uuid",
    "userId": "uuid",
    "role": "MEMBER",
    "acceptedAt": "2025-12-15T13:05:30Z"
  }
}
```

### 6.5 GroupInviteRevoked
```json
{
  "data": {
    "inviteId": "uuid",
    "groupId": "uuid",
    "revokedBy": "uuid",
    "revokedAt": "2025-12-15T13:05:30Z"
  }
}
```

### 6.6 GroupJoinPasswordChanged
```json
{
  "data": {
    "groupId": "uuid",
    "updatedBy": "uuid",
    "updatedAt": "2025-12-15T13:05:30Z"
  }
}
```

