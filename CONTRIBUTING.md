# 협업 규칙

## 작업 흐름
```
이슈 생성 → 브랜치 생성 → 커밋 → PR → 리뷰 승인(1명) → develop 병합
```

## 브랜치
| 브랜치 | 역할 |
| --- | --- |
| `main` | 운영 배포 |
| `develop` | 개발 통합 (작업 브랜치는 여기서 분기) |
| `feat/#이슈번호-설명` | 기능 개발 (예: `feat/#3-login-api`) |
| `fix/#이슈번호-설명` | 버그 수정 |

- `main`, `develop`에는 직접 push 하지 않고 PR로만 병합한다.

## 커밋 메시지
```
<type>: <제목> (#이슈번호)
```
| type | 용도 |
| --- | --- |
| `feat` | 기능 추가 |
| `fix` | 버그 수정 |
| `refactor` | 동작 변화 없는 코드 개선 |
| `docs` | 문서 |
| `test` | 테스트 |
| `chore` | 설정, 의존성 등 |

예시: `feat: 회원가입 API 구현 (#3)`

커밋 템플릿 적용 (클론 후 1회):
```bash
git config commit.template .gitmessage.txt
```

## PR
- 제목은 커밋 형식과 동일
- 본문에 `Closes #이슈번호` 를 적어 이슈를 자동으로 닫는다
- 1명 이상 승인 후 **Squash and merge**
