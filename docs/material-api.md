# Material API (세션 자료 링크)

세션 자료는 파일 업로드 대신 제목과 HTTP/HTTPS 링크로 등록한다.
명세의 `/api/v1`과 기존 코드의 `/api` 접두사를 모두 지원한다.
로그인은 기존 `POST /api/auth/login`을 사용한다.

| 기능 | Method | 명세 경로 | 인증/권한 |
| --- | --- | --- | --- |
| 세션 자료 등록 | POST | `/api/v1/weeks/{weekId}/materials` | MANAGER |
| 세션 자료 목록 조회 | GET | `/api/v1/weeks/{weekId}/materials` | MEMBER |
| 세션 자료 조회 | GET | `/api/v1/materials/{materialId}` | MEMBER |
| 세션 자료 삭제 | DELETE | `/api/v1/materials/{materialId}` | MANAGER |

MEMBER/MANAGER는 해당 자료가 속한 스터디의 활동 중인 멤버/운영진을 뜻한다.
운영진도 목록과 상세를 조회할 수 있다. 다른 스터디의 운영진에게는 권한이 없다.
모든 요청에 로그인 세션 쿠키가 필요하고, POST/DELETE에는 CSRF 쿠키와 `X-XSRF-TOKEN` 헤더도 필요하다.

## 1. API 설명

- 등록: 주차에 자료 링크를 추가한다. 등록자는 로그인한 운영진으로 저장한다.
- 목록: 해당 주차 자료만 `createdAt DESC, id DESC` 순으로 반환한다. 자료가 없으면 빈 배열이다. 페이지네이션은 없다.
- 상세: 제목과 링크를 JSON으로 반환한다. 프론트엔드가 반환된 링크를 열면 된다.
- 삭제: 등록자에 관계없이 같은 스터디의 운영진이 자료 기록을 영구 삭제할 수 있다.

## 2. Request Header

GET: `Cookie: JSESSIONID=<로그인 세션>`

POST/DELETE:

```text
Cookie: JSESSIONID=<로그인 세션>; XSRF-TOKEN=<CSRF 토큰>
X-XSRF-TOKEN: <CSRF 토큰>
```

POST에는 `Content-Type: application/json`도 사용한다.

## 3. Request Body

등록 요청만 본문을 사용한다. 나머지는 본문이 없다.

```json
{
  "title": "1주차 세션 발표 자료",
  "url": "https://example.com/slides"
}
```

| 필드 | 필수 | 조건 |
| --- | --- | --- |
| title | O | 앞뒤 공백 제거 후 1~100자, 공백만 있는 제목 불가 |
| url | O | 앞뒤 공백 제거 후 최대 2048자, HTTP/HTTPS URL, 공백 불가 |

경로의 `weekId`는 주차 ID, `materialId`는 자료 ID이다.
등록자 ID는 요청으로 받지 않는다.

## 4. Response

### 4.1 Success

등록은 HTTP 201, 목록/상세/삭제는 HTTP 200이다.
등록/상세 응답 예시(값은 예시):

```json
{
  "success": true,
  "data": {
    "id": 1,
    "weekId": 1,
    "memberId": 2,
    "userId": 3,
    "name": "이승윤",
    "title": "1주차 세션 발표 자료",
    "url": "https://example.com/slides",
    "createdAt": "2026-11-07T23:00:00",
    "updatedAt": "2026-11-07T23:00:00"
  },
  "error": null
}
```

`memberId`는 등록자의 스터디 멤버 ID, `userId`는 등록자의 회원 ID이다.
목록은 `data`에 위 자료 객체들의 배열을 반환한다. 삭제 성공은 `data: null`이다.

### 4.2 Failure

```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "MATERIAL_404",
    "message": "세션 자료를 찾을 수 없습니다."
  }
}
```

| 상태 | 코드 | 조건 | 해당 요청 |
| --- | --- | --- | --- |
| 400 | COMMON_400 | 제목/링크 누락·형식·길이 오류, 잘못된 경로 ID 타입 | 등록 / 경로 ID 사용 요청 |
| 401 | COMMON_401 | 로그인 필요 | 전체 |
| 403 | COMMON_403 | CSRF 토큰 누락/오류 | 등록·삭제 |
| 403 | STUDY_403_MEMBER | 해당 스터디의 활동 멤버가 아님 | 전체 |
| 403 | STUDY_403_MANAGER | 활동 멤버지만 운영진이 아님 | 등록·삭제 |
| 404 | WEEK_404 | 주차 없음 | 등록·목록 |
| 404 | MATERIAL_404 | 자료 없음 또는 삭제됨 | 상세·삭제 |
| 409 | WEEK_409_MATERIALS | 자료가 남은 주차 삭제 시도 | 기존 주차 삭제 API |

## 5. 비고

- 파일 저장/다운로드 대신 링크 등록·조회 방식으로 합의한 구현이다. 외부 링크 콘텐츠의 접근 권한은 링크 제공 서비스에서 관리한다.
- 서버는 등록한 외부 링크에 접속하지 않는다. 링크가 실제로 열리는지, 파일인지 여부는 검사하지 않는다.
- 한 주차에 자료를 여러 개 등록할 수 있으며 같은 URL도 허용한다.
- 자료는 과제 제출 방식(`LINK`, `FILE`, `LINK_AND_FILE`)과 관계없이 등록할 수 있다.
- 자료 수정 API는 기존 4개 항목에 없으므로 제공하지 않는다. 변경 시 삭제 후 다시 등록한다.
- 자료가 있으면 주차 삭제를 409로 막는다. 먼저 자료를 모두 삭제해야 한다.
- 로컬 검증은 H2 기반 자동 통합 테스트이다. MySQL 및 프론트 연동 검증은 별도다.
