# groups-service 구현 계획서

## 1) 가입 방식 2가지

### A. 그룹이름 + 가입password로 직접 가입

* 사용자가 `groupName + joinPassword`를 입력해서 가입
* 서버는 groupName으로 그룹을 찾고, password 검증 후 가입 처리

### B. userId 초대 기반 가입

* MANAGER 이상이 특정 userId를 초대
* 초대 받은 사용자가 수락하면 가입

---

## 2) DB 설계 수정 포인트

## 2-1. groups 테이블 (name 유니크 필요)

`groupName`으로 가입할 거면 **그룹 이름이 유니크**여야 혼선이 없어.

**groups**

* `id` uuid PK
* `name` varchar(100) NOT NULL **UNIQUE** ✅
* `description` text NULL
* `owner_user_id` uuid NOT NULL
* `join_password_hash` varchar(255) NOT NULL
* `join_password_updated_at` timestamp NOT NULL
* `created_at`, `updated_at`

> 만약 “동일한 그룹명 허용”을 하고 싶으면
> join 시 `groupName`만으로는 불가능해서 `groupCode` 같은 별도 식별자가 필요해.
> 너가 “그룹이름+password”로 하자고 했으니 여기서는 **name unique로 고정**.

---

## 3) API 설계 수정

## 3-1. 가입(이름+비번)

### ✅ `POST /groups/join`

* body: `{ "groupName": "Avolog", "joinPassword": "1234" }`
* 처리:

    1. `groupName`으로 groups 조회 (없으면 404 또는 401 스타일로 뭉개도 됨)
    2. `join_password_hash` 검증
    3. 이미 멤버면 409 또는 멱등 200
    4. group_members에 `MEMBER`로 등록

> 기존 `/groups/{groupId}/join`은 제거해도 되고, 유지하더라도 “내부용/관리용” 정도로만.

## 3-2. 그룹 생성

### `POST /groups`

* body: `{ name, description?, joinPassword }`
* name 중복이면 409
* joinPassword 해시 저장
* 생성자는 OWNER로 group_members 등록

## 3-3. joinPassword 변경

### `PATCH /groups/{groupId}/join-password`

* body: `{ newJoinPassword }`
* 권한: OWNER(또는 정책상 MANAGER도 가능)
* 해시 갱신

---

## 4) 초대 기반 가입 API(동일, 유지)

* `POST /groups/{groupId}/invites` (MANAGER+)
* `GET /invites/received`
* `POST /invites/{inviteId}/accept`
* `POST /groups/{groupId}/invites/{inviteId}/revoke`

---

## 5) 보안/운영 권장사항(이 구조에서 특히 중요)

* `POST /groups/join`은 **브루트포스** 위험이 있으니

    * API Gateway에서 rate limit
    * 실패 응답은 “group not found” vs “password wrong”를 너무 구체적으로 구분하지 않기(정보 누출 방지)
* `groups.name`은 유니크라서, **이름이 곧 가입 식별자**가 됨
  → “짧고 흔한 이름”은 충돌 가능성 높으니 최소 길이/규칙 권장

---

## 변경 요약

* 가입 endpoint를 `POST /groups/join`로 고정
* 입력은 `groupName + joinPassword`
* groups 테이블에서 `name UNIQUE` 필수

