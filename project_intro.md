# 📘 프로젝트 설계서

**ECS(Fargate) 기반 마이크로서비스 아키텍처API Gateway + Google OAuth + SNS/SQS Pub/Sub**

---

## 1. 프로젝트 개요

본 프로젝트는 **AWS ECS(Fargate)** 환경에서 컨테이너 기반 마이크로서비스를 운영하고,
**Amazon API Gateway**를 단일 진입점으로 사용하는 투두 관리 백엔드 시스템을 구축하는 것을 목표로 한다.

- Spring Cloud Gateway는 사용하지 않는다.
- 인증/회원가입은 **Google OAuth 2.0** 기반으로 구현한다.
- 서비스 간 부가 기능 처리는 **SNS + SQS Pub/Sub** 구조로 비동기 분리한다.

이를 통해 **확장 가능성, 낮은 결합도, 운영 단순성**을 동시에 확보한다.

---

## 2. 전체 아키텍처 개요

```
Client (React)
   ↓
Amazon API Gateway (HTTP API)
   - JWT Authorizer
   - CORS / Rate Limit
   ↓
Application Load Balancer (ALB)
   ↓
ECS (Fargate, Private Subnet)
   ├─ users-service
   ├─ todo-service
   ├─ groups-service
   └─ activity-service
        ↑
        │ (비동기 이벤트)
        └── SQS ← SNS (todo-events-topic)
   ↓
Data Layer
   - RDS (PostgreSQL)
   - ElastiCache (Redis, optional)

```

---

## 3. 마이크로서비스 구성

### 3.1 서비스 목록 및 역할

| 서비스명 | 역할 |
| --- | --- |
| **users-service** | Google OAuth 로그인/회원가입, 사용자 관리, JWT 발급 |
| **todo-service** | Todo 생성/수정/완료/삭제 (핵심 도메인) |
| **groups-service** | 그룹 및 멤버 관리, 권한 처리 |
| **activity-service** | Todo 이벤트 기반 활동 로그 및 통계 처리 |

---

## 4. 인증 및 보안 설계

### 4.1 인증 전략 요약

- **Google OAuth 처리**: users-service
- **JWT 검증**: API Gateway (JWT Authorizer)
- *도메인 서비스(todo, groups, activity)**는 인증 로직을 직접 구현하지 않는다.

---

### 4.2 Google OAuth 로그인 흐름

```
Client
 → GET /users/auth/google/login
 → Google OAuth Authorization Server
 → redirect (/users/auth/google/callback)
 → users-service
   - Google userinfo 조회
   - 회원가입 또는 기존 회원 조회
   - JWT 발급
 → Client (JWT 저장)

```

---

### 4.3 users-service 인증 API

```
GET  /users/auth/google/login
GET  /users/auth/google/callback
POST /users/auth/refresh        (선택)
GET  /users/me                  (JWT 필요)

```

---

### 4.4 JWT 정책

- Access Token 기반 인증
- Payload 예시:

```json
{
  "sub": "user-uuid",
  "email": "user@gmail.com",
  "provider": "GOOGLE",
  "roles": ["USER"]
}

```

---

## 5. API Gateway 설계

### 5.1 역할

- 단일 진입점
- JWT Authorizer를 통한 인증
- CORS / Rate Limiting
- ALB로 프록시 라우팅

### 5.2 인증 적용 범위

| 경로 | 인증 |
| --- | --- |
| `/users/auth/*` | ❌ |
| `/users/me` | ✅ |
| `/todos/*` | ✅ |
| `/groups/*` | ✅ |
| `/activity/*` | ✅ |

---

## 6. ALB 라우팅 설계

### 6.1 Path 기반 라우팅

| Path Prefix | Target Service |
| --- | --- |
| `/users/*` | users-service |
| `/todos/*` | todo-service |
| `/groups/*` | groups-service |
| `/activity/*` | activity-service |

API Gateway는 모든 요청을 ALB로 전달한다.

---

## 7. 네트워크 구성 (VPC)

### 7.1 서브넷

- Public Subnet
    - Application Load Balancer
- Private Subnet
    - ECS Tasks
    - RDS
    - Redis

### 7.2 Security Group 정책

- ALB SG: 443 Inbound 허용
- ECS SG: ALB SG → 서비스 포트(8080) 허용
- RDS/Redis SG: ECS SG에서만 접근 허용

---

## 8. ECS(Fargate) 운영 설계

### 8.1 공통 설정

- 이미지 저장소: Amazon ECR
- 로그: CloudWatch Logs
- 헬스체크: `/health`
- 배포 전략: Rolling Update

### 8.2 초기 리소스 기준

| 항목 | 값 |
| --- | --- |
| CPU | 0.25 vCPU |
| Memory | 0.5~1GB |
| Desired Count | 1~2 |

---

## 9. 데이터 계층 설계

### 9.1 RDS (PostgreSQL)

- 단일 RDS 인스턴스
- 서비스별 Database 분리
    - users_db
    - todo_db
    - groups_db
    - activity_db

### 9.2 Redis (선택)

- 캐시
- 세션
- 간단한 분산 락

---

## 10. 이벤트 기반 아키텍처 (SNS + SQS)

### 10.1 도입 목적

- 서비스 간 결합도 감소
- 부가 기능 장애가 핵심 로직에 영향 주지 않도록 분리
- 향후 알림/통계/분석 확장 대비

---

### 10.2 구성 요소

- **SNS Topic**
    - `todo-events-topic`
- **SQS Queue**
    - `activity-events-queue`
- **DLQ**
    - `activity-events-dlq`

---

### 10.3 이벤트 흐름 (Todo 완료 예시)

```
Client
 → POST /todos/{id}/complete
 → todo-service
    - DB 업데이트
    - TodoCompleted 이벤트 발행 (SNS)
 → SNS Topic
 → SQS Queue
 → activity-service
    - 활동 로그 저장
    - 통계 갱신

```

---

### 10.4 이벤트 타입

| 이벤트명 | 설명 |
| --- | --- |
| TodoCreated | Todo 생성 |
| TodoUpdated | Todo 수정 |
| TodoCompleted | Todo 완료 |
| TodoDeleted | Todo 삭제 |

### 10.5 이벤트 포맷 예시

```json
{
  "eventId": "uuid",
  "eventType": "TodoCompleted",
  "occurredAt": "2025-12-15T13:05:30Z",
  "producer": "todo-service",
  "data": {
    "todoId": "uuid",
    "groupId": "uuid-or-null",
    "userId": "uuid",
    "status": "COMPLETED"
  }
}

```

---

## 11. 장애 대응 및 신뢰성

- SQS 재시도 메커니즘 활용
- 일정 횟수 실패 시 DLQ 이동
- DLQ 메시지 기반 수동 복구 가능

---

## 12. 로깅 및 모니터링

- CloudWatch Logs: 서비스 로그
- CloudWatch Metrics
    - API Gateway latency / error
    - ALB target health
    - ECS CPU / Memory
    - SQS backlog / DLQ count

---

## 13. CI/CD 파이프라인

### GitHub Actions 기준

1. 테스트 및 빌드
2. Docker 이미지 생성
3. ECR Push
4. ECS 서비스 업데이트

---

## 14. 단계별 구현 계획

1. todo-service 단독 ECS + ALB 배포
2. API Gateway 연동
3. users-service + Google OAuth 구현
4. groups / activity 서비스 추가
5. SNS + SQS 이벤트 연결
6. RDS 연동 및 마이그레이션
7. 모니터링 및 오토스케일 적용

---

## 15. 설계 선택 요약

- ❌ Kubernetes 미사용
- ❌ Spring Cloud Gateway 미사용
- ✅ API Gateway 중심 인증/라우팅
- ✅ users-service 단일 인증 책임
- ✅ ECS(Fargate) 기반 단순 운영
- ✅ SNS + SQS로 이벤트 기반 확장 가능