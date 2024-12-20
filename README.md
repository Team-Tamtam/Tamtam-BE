## Mooney
**예산 설정과 지출 관리에 어려움을 겪는 사회 초년생을 위한 개인 일정 및 소비 특성 기반 밀착관리 가계부 서비스**
> KEYWORD: 생성형 AI, 프롬프트 엔지니어링, 금융

## 🔗 레포지토리 링크
| 🛠️ 파트 | 🔗 링크 |
|:---:|:---:|
| **BE** | https://github.com/Team-Tamtam/Tamtam-BE |
| **FE** | https://github.com/Team-Tamtam/Tamtam-FE |

## 🎨 플로우차트
<img width="4700" alt="무니_플로우차트" src="https://github.com/user-attachments/assets/7f5ccb8a-d495-406e-9102-62fe5c79d910" />

## 📝 프로젝트 개요
사회초년생의 소비 관리에 길잡이가 되어줄 밀착관리 가계부 서비스를 만들고자 합니다. GPT 모델 기반의 프롬포팅을 통해, 세운 한달 예산을 잘 지킬수 있도록, 매일의 일정의 특수성을 고려한 하루예산을 제공합니다. 월말에는, 이번 달의 소비에 대한 상세 피드백 및 개선방향을 제공합니다. 더불어 이번달 소비내역, 다음달의 특수한 일정, 사용자의 예산에서의 요구사항을 반영하여 다음달 예산안을 추천합니다. 서비스를 사용하는 첫달에는, 예산을 세워보지 않은 사회초년생이 어려움을 겪지 않도록 예산 설정을 단계별로 안내하여 쉽게 첫 달 예산을 세울 수 있도록 돕습니다. 이러한 과정을 통해 사용자가 꾸준히 소비를 관리하여 재정 안정성을 촉진시킬 수 있도록 돕습니다.

## ⚡ Pain Point
### 1. 적절한 예산 설정의 어려움
- 기존 가계부 앱은 다음 달 예산 설정 시 기존 예산의 문제점 분석 및 수정 과정을 사용자에게 맡깁니다.
- 특수한 일정(여행 등)을 반영하는 기능이 없어 비효율적인 예산안으로 이어짐.

### 2. 예산을 지키기 어려움
- 일정 특수성을 고려하지 않고 1/N 방식으로 하루 예산을 배정.
- 예산 계획을 지킬 수 있도록 돕는 기능이 부족.

## 🚀 솔루션
### 💻 기술 스택
- **Front-End(App)**: Flutter  
- **Back-End**: SpringBoot 3.3.4, Java 21, JWT 0.11.5, AWS VPC, AWS EC2(Ubuntu), Nginx 1.24.0, Docker 27.1.2, Docker Compose 2.29.2, GitHub Actions  
- **Database**: AWS RDS(MySQL 8.0.35), Redis 7.4.0  
- **생성형 AI API**: OpenAI GPT API(GPT-4o mini)  
- **결제내역 문자 읽기 라이브러리**: flutter_sms_inbox 1.0.3  
- **캘린더 API**: Google Calendar API(v3)  
- **푸시 알림**: Firebase Cloud Messaging(HTTP v1)

## 💎 오래 축적한 데이터의 잠재적 가치
1. **개인 소비 및 예산 패턴 데이터**
   - **축적 데이터**: 소비 패턴, 예산 초과 내역, 카테고리별 지출.
   - **가치**: 맞춤형 예산 추천 정확도 및 소비 예측 개선.
2. **일정과 지출 상관관계 데이터**
   - **축적 데이터**: 일정 유형별 지출 패턴.
   - **가치**: 일정 맞춤 예산 최적화.
3. **집단 소비 경향 분석 데이터**
   - **축적 데이터**: 연령 및 직업별 소비 트렌드.
   - **가치**: 소비 트렌드 분석 및 맞춤형 서비스 개발.

## 💡 기대 성과
1. **효율적인 예산 수립 및 소비 관리 습관화**  
   - 일정과 소비 특성을 반영한 맞춤형 예산 관리.
2. **재정 안정성 및 재무적 자기효능감 향상**  
   - 맞춤형 예산 제안을 통해 사회초년생의 재무 관리 능력을 향상.

<br>

---

<br>

## 📝 백엔드 협업 규칙

#### 커밋 컨벤션

- "태그: 한글 커밋 메시지" 형식으로 작성
- 컨벤션 예시
  - feat: 새로운 기능 추가, 기존의 기능을 요구 사항에 맞추어 수정
  - fix: 버그 수정
  - docs: 문서 수정
  - style: 코드 포맷팅, 오타 수정, 주석 수정 및 삭제 등
  - refactor: 코드 리팩터링
  - chore: 빌드 및 패키지 수정 및 삭제
  - merge: 브랜치를 머지
  - ci: CI 관련 설정 수정
  - test: 테스트 코드 추가/수정
  - release: 버전 릴리즈

<br>

#### PR 템플릿

```
# 구현 기능
  - 구현한 기능을 요약하여 정리합니다.

# 구현 상태 (선택)
  - img, gif, video...
  - 혹은 내용 정리

# Resolve
  - 이슈 태그(ex: #7)
```

- PR 체크 리스트
  - PR 제목 형식 : `[Type] PR 제목`
    - ex. `[Feat] 회원가입 및 로그인 기능 개발`
    - 타입은 대문자로
  - label 설정
  - 작업자 자신을 Assign하고, Code Review 요청
  - 작성자 외 1명 확인 시 작성자가 merge

<br>

#### issue 규칙

- 각 기능에 맞는 이슈 템플릿 작성 (작업 및 변경사항 확인용)
- to-do에 구현해야할 기능을 작성하고, 구현이 끝나면 체크표시

<br>

#### branch 규칙

- 브랜치 네이밍 규칙: `feat/{도메인_혹은_큰_기능}` ex) `feat/user`
- `feat -> develop -> deploy -> main` 순으로 merge
- `feat` : 각 기능을 개발하는 브랜치
- `develop` : 각 기능의 개발을 완료하고 테스트 완료 후 병합하는 브랜치
- `deploy` : 배포 브랜치
