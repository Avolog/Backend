
# todo-service 구현 계획서 

## 1. 핵심 개념 재정의 (정리)

### 1-1. Todo

* 실제로 체크/완료되는 **행동 단위**
* 날짜(`todo_date`)를 가진다
* 루틴에 의해 생성될 수도 있고, 단일 todo일 수도 있다

### 1-2. Routine (중요)

루틴은 **“기간 + 반복 규칙”** 이다.

* 루틴은 **삭제되지 않는다**
* 루틴은 **시작일 ~ 종료일**을 가진다
* 루틴 안에는

    * “일 / 주 / 월 단위”
    * “각 단위 안에서 몇 번 해야 하는지”
    * “최대 / 최소 루틴” 규칙이 있다
* **각 반복 단위(하루, 한 주, 한 달)** 안에서

    * 정해진 횟수를 다 채우면
    * 그 단위 안에 생성되어 있던 **해당 루틴의 todo 인스턴스들을 삭제**

👉 “총 기간 전체에서 끝나면 삭제” ❌
👉 “각 기간 단위(일/주/월)마다 목표 달성 시 해당 기간의 todo를 제거” ⭕

---

## 2. 카테고리 개념 수정 (중요)

카테고리는 **도메인 분류용 개념**이다.

* 예: `시험공부`, `운동`, `알바`, `프로젝트`
* 개인도 만들 수 있고
* 그룹도 만들 수 있다
* **scope 개념은 유지하되 의미가 변경됨**

즉,

* “개인 카테고리” = 개인이 쓰는 분류
* “그룹 카테고리” = 그룹에서 공유하는 분류

---

## 3. DB 설계 (todo_db) – 수정본

## 3-1. categories (수정)

```sql
categories
- id uuid PK
- name varchar(50) NOT NULL
- owner_type varchar(10) NOT NULL  -- PERSONAL | GROUP
- owner_user_id uuid NULL          -- PERSONAL일 때
- group_id uuid NULL               -- GROUP일 때
- created_at
- updated_at
```

제약

* PERSONAL: `(owner_user_id, name)` unique
* GROUP: `(group_id, name)` unique

---

## 3-2. routines (대폭 수정)

### 핵심 포인트

* period가 있으면 **기준 요일/기준일**이 반드시 필요
* “몇 번 완료하면 끝”은 **기간 단위 기준**

```sql
routines
- id uuid PK
- title varchar(200) NOT NULL

- routine_type varchar(10) NOT NULL   -- MAX | MIN
- period varchar(10) NOT NULL         -- DAILY | WEEKLY | MONTHLY
- target_count int NOT NULL           -- 해당 period 안에서 완료해야 하는 횟수

- start_date date NOT NULL
- end_date date NOT NULL

-- 기준점
- week_start_day int NULL             -- WEEKLY일 때 (1=월 ... 7=일)
- month_base_day int NULL             -- MONTHLY일 때 (1~31)

- creator_user_id uuid NOT NULL
- group_id uuid NULL
- category_id uuid NULL

- created_at
- updated_at
```

### 기준점 설명

* DAILY
  → 기준 불필요
* WEEKLY
  → `week_start_day` (예: 월요일 시작 주)
* MONTHLY
  → `month_base_day`

    * 예: 15 → 매달 15일 기준으로 한 달

---

## 3-3. todos (루틴 인스턴스 개념 명확화)

```sql
todos
- id uuid PK
- creator_user_id uuid NOT NULL
- group_id uuid NULL
- category_id uuid NULL
- routine_id uuid NULL          -- 루틴 기반 todo면 값 있음

- title
- description
- status varchar(20)            -- PENDING | COMPLETED

- todo_date date NOT NULL       -- 이 todo가 속한 날짜
- period_key varchar(20) NOT NULL
  -- DAILY: 2025-12-16
  -- WEEKLY: 2025-W51
  -- MONTHLY: 2025-12

- created_at
- updated_at
```

> `period_key`는 **루틴 완료 집계 + 삭제 범위 계산**의 핵심 키

---

## 3-4. todo_assignees (동일)

* 담당자 1:N / N:M 유지

---

## 3-5. todo_completions (의미 변경)

이제 이 테이블은 **“몇 번 했는지” 집계용**이다.

```sql
todo_completions
- id uuid PK
- todo_id uuid NOT NULL
- routine_id uuid NULL
- completed_by uuid NOT NULL
- period_key varchar(20) NOT NULL
- completed_at timestamp NOT NULL
```

---

## 4. Todo & Routine 동작 로직 (중요 변경)

## 4-1. 루틴 Todo 생성 시점

* 사용자가 특정 날짜의 todo 목록을 요청하면:

    1. 해당 날짜가 루틴 기간 안인지 확인
    2. 그 날짜가 속한 `period_key` 계산
    3. 해당 `routine_id + period_key`에 대해

        * 아직 todo 인스턴스가 없으면 생성
        * 있으면 그대로 사용

👉 **배치 없음 / 요청 기반 생성**

---

## 4-2. Todo 완료 처리 (API 변경 핵심)

### `POST /todos/{id}/complete`

#### 권한

* creator 또는 assignee
* 그룹 todo면 assignee여도 가능

#### 처리 순서

1. todo 상태 COMPLETED 처리
2. `todo_completions`에 완료 기록 insert
3. `routine_id`가 있으면:

    1. 같은 `routine_id + period_key` 완료 횟수 집계
    2. `count >= target_count`이면:

        * **그 period_key에 속한 모든 todo 인스턴스 삭제**
        * (MAX / MIN 동일하게 동작)
        * 단,

            * MAX: 이후에도 계속 생성되지만 “그 period에서는 끝”
            * MIN: 목표 초과 완료도 가능

> ✔️ 이게 네가 말한 “그 기간 안에 생성된 todo들이 사라진다”의 정확한 구현

---

## 5. Todo CRUD 권한 (정리)

### 생성 / 수정 / 삭제

* 개인 todo: creator
* 그룹 todo: group manager 이상

### 완료

* creator
* assignee

### 루틴 todo 삭제

* **사용자 직접 삭제 불가**
* 오직 루틴 규칙 충족 시 자동 삭제

---

## 6. 이벤트 발행(SNS) – 수정 포인트

### 새로운 핵심 이벤트

* `TodoCompleted`
* `RoutinePeriodCompleted` ⭐️ (중요)
* `RoutineTodoGenerated`
* `RoutineTodoCleared` (period 목표 달성으로 삭제)

### `RoutinePeriodCompleted` 예시

```json
{
  "eventType": "RoutinePeriodCompleted",
  "routineId": "uuid",
  "period": "WEEKLY",
  "periodKey": "2025-W51",
  "groupId": "uuid-or-null",
  "categoryId": "uuid",
  "completedCount": 5
}
```

👉 activity-service가 통계 만들기 매우 쉬워짐

---

## 7. 구현 순서 (업데이트)

1. categories / routines / todos / completions DDL
2. todo CRUD + 권한
3. 담당자 로직
4. 루틴 todo 자동 생성
5. 완료 시 period 목표 달성 → todo 일괄 삭제
6. 이벤트 발행
7. activity-service 통계 연동

---

## 요약 (핵심만)

* 루틴은 **기간 + 반복 규칙**이다
* period에는 **기준점(요일/기준일)** 이 반드시 필요
* 목표 달성 시:

    * 루틴 종료 ❌
    * **해당 period의 todo 인스턴스 삭제 ⭕**
* 카테고리는 단순 분류 개념이며

    * 개인/그룹 모두에서 생성 가능
* todo-service는 **도메인의 중심**

    * CRUD
    * 루틴 로직
    * 이벤트 발행


