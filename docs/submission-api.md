# Submission API (링크 제출)

로그인은 기존 `POST /api/auth/login`을 사용한다. Submission은 명세의 `/api/v1`과 기존 코드의 `/api`를 모두 지원한다.
모든 요청은 로그인 세션이 필요하고, POST/PATCH/DELETE에는 `X-XSRF-TOKEN` 헤더도 필요하다.

| 기능 | Method | 명세 경로 | 권한 |
| --- | --- | --- | --- |
| 과제 제출 | POST | `/api/v1/weeks/{weekId}/submissions` | 활동 중인 멤버 |
| 주차별 제출물 목록 | GET | `/api/v1/weeks/{weekId}/submissions` | 운영진 |
| 내 제출물 | GET | `/api/v1/weeks/{weekId}/submissions/me` | 본인 |
| 제출물 상세 | GET | `/api/v1/submissions/{submissionId}` | 본인 또는 운영진 |
| 제출물 수정 | PATCH | `/api/v1/submissions/{submissionId}` | 본인 |
| 제출물 삭제 | DELETE | `/api/v1/submissions/{submissionId}` | 본인 |
| 제출 확인/취소 | PATCH | `/api/v1/submissions/{submissionId}/check` | 운영진 |

## 요청과 응답

제출/수정 본문: `{"url":"https://example.com/my-note"}`

확인/취소 본문: `{"checked":true}` / `{"checked":false}`. `checked`는 필수다.

제출 성공은 201, 나머지 성공은 200이다. 공통 응답 `success`, `data`, `error` 형식을 사용한다.
제출물 응답 필드는 `id`, `weekId`, `memberId`, `userId`, `name`, `url`, `checked`, `submittedAt`, `updatedAt`이다.
삭제 성공은 `data: null`, 빈 목록은 `data: []`, 미제출 상태에서 내 제출물 조회는 404이다.

## 현재 적용한 정책

- `AssignmentType.LINK` 주차에서 HTTP/HTTPS 링크를 제출한다. 앞뒤 공백은 제거하고 최대 2048자다.
- 한 멤버가 한 주차에 한 건 제출한다. 중복 요청은 409이며 삭제 후 재제출할 수 있다.
- 제출자는 세션에서 가져온다. 요청 본문에 제출자 ID를 받지 않는다.
- 링크가 변경되면 `checked`는 false로 초기화된다. 같은 링크를 다시 보내면 확인 상태는 유지된다.
- 제출물이 있는 주차는 삭제할 수 없다(409). 주차 삭제의 기존 TODO를 연결했다.
- 마감 후 제출/수정 제한은 아직 적용하지 않았다. 팀 정책 확정 후 추가한다.
- 파일 업로드(`FILE`, `LINK_AND_FILE`)는 별도 구현 범위다. 링크만 보내는 요청은 400으로 거부한다.
- 주차 전체 목록 및 상세 조회의 공개 범위는 위 권한표 기준이다. 팀 정책과 함께 검토한다.

## 주요 오류

| 상태 | 코드 | 의미 |
| --- | --- | --- |
| 400 | `COMMON_400` | 누락/잘못된 URL/checked 누락 |
| 400 | `SUBMISSION_400_TYPE` | 파일 제출이 필요한 주차 |
| 401 | `COMMON_401` | 미로그인 |
| 403 | `STUDY_403_MEMBER` / `STUDY_403_MANAGER` | 스터디 권한 없음 |
| 403 | `SUBMISSION_403_OWNER` / `SUBMISSION_403_READ` | 본인/조회 권한 없음 |
| 404 | `WEEK_404` / `SUBMISSION_404` | 주차/제출물 없음 |
| 409 | `SUBMISSION_409` | 중복 제출 |
| 409 | `WEEK_409_SUBMISSIONS` | 제출물이 있는 주차 삭제 |

아직 우수 멤버 선정 시 제출 이력을 검증하는 BestMemberService의 TODO는 연결하지 않았다.
