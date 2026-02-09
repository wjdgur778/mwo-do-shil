# 🍺 뭐드실? (Mwo-do-shil) - AI 기반 페어링 맛집 추천 서비스

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.9-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![Docker](https://img.shields.io/badge/Docker-Ready-blue.svg)](https://www.docker.com/)

### 📖 개요 (Overview)
**뭐드실?**(Mwo-do-shil)은 현재 날씨, 지리적 위치, 가게의 정보 분석하여 사용자가 원하는 주종과의 페어링이 좋은 
 식당 및 주점을 제안하는 AI 추천 서비스입니다.
llm api의 적절한 웹 그라운딩을 통해 실제 가게들의 정보를 수집하고, 수집한 정보들을 바탕으로 페어링 맛집을 추천합니다.

Springboot와 React기반으로 개발되었으며, 모바일 웹에 특화되어있습니다. Vercel(frontend 배포), Render(backend 배포)를 사용하여 빠른 배포를 수행했습니다.

#### 👉🏻URL : https://mwo-do-shil.vercel.app/

---

### 📅 개발 기간 및 인원
기간 : 2026.01.08 ~ 2025.02.10

팀원 : 개인 프로젝트 (backend,frontend 공동 개발)

---
### 🎯 비즈니스 문제 및 해결책

**기존의 문제점**
- 일반 검색 엔진은 주류와 안주의 페어링(조합)을 고려하지 않음
- 날씨 변화가 실제 메뉴 선택에 미치는 영향이 반영되지 않음
- 수동적인 맛집 검색은 시간이 많이 소요 되거나 한번에 정리되지 않음

**뭐드실**의 솔루션

3단계 AI 엔진을 통해 다음과 같은 솔루션을 제공합니다:
1. **발견(Discovery)**: 지리적 세그멘테이션과 Kakao Local API를 통한 정밀 장소 탐색 후 1차 필터링
2. **분석(Analysis)**: Gemini-2.5-flash-lite 모델의 웹 그라운딩(Web Grounding) 기술로 실시간 데이터를 확보
3. **추천(Recommendation)**: Gemini-2.5-flash-lite-preview 모델로 자체 스코어링 시스템과 구체적인 추천 사유를 포함한 적절한 맛집 제안
---


### 기술 스택 (Technology Stack)

| 구분 | 사용 기술                                                         | 용도                       |
|----------|---------------------------------------------------------------|--------------------------|
| **Backend** | Spring Boot 3.5.9, Java 17                                    | 코어 애플리케이션 프레임워크          |
| **Database** | Firestore                                                     | 운영 DB, 개발용 DB, 지능형 캐시 계층 |
| **Security** | Firebase Auth, Spring Security                                | JWT 기반의 보안 및 사용자 인증      |
| **Reactive** | WebClient                                            | 논블로킹 방식의 고성능 HTTP 통신     |
| **AI/ML** | Google Gemini AI                                              | 맛집 필터링, 맛집 정보 수집, 추천 판단  |
| **External APIs** | Kakao Local, Kakao Map, OpenWeatherMap                        | 맛집 데이터 검색 및 실시간 기상 정보 확보 |
| **DevOps** | 프론트엔드 : Vercel<br/> 백엔드 :  Render(Docker (Multi-stage build)) | 최소 비용 배포, 배포 효율화 및 환경 격리 |
---


### 🚀 핵심 기능
### 1. 지능형 추천
- **3단계 AI 프로세싱**: 1차 맛집 필터링 → 필터링된 가게 정보 수집 → 최종 추천 리스트 생성
- **날씨 최적화**: "비 오는 날엔 파전에 막걸리"처럼 기상 상황에 맞는 메뉴 추천
- **주종별 특화**: 소주, 맥주, 와인, 위스키 등 각 주종에 최적화된 안주 페어링
- **정교한 스코어링**: 800~950점 사이의 점수 산출 및 추천 근거 리포트 제공

### 2. 성능 및 확장성
- **병렬 처리 아키텍처**: 
  - kakao local api 호출시 16개의 세그먼트 동시 호출로 검색 속도 및 범위 극대화
  - 웹그라운딩 llm api 호출시 최대 10번의 동시 호출 (데이터의 질을 위해 1곳의 가게당 1회 검색 api 호출)
- **지능형 캐싱**: Firestore를 활용해 AI API 호출 비용 절감
- **호출 제한(Rate Limiting)**: 비용 제어를 위해 / 사용자별(일 3회), 시스템 전체(일 150회) 트래픽 제어
- **논블로킹 api호출** : WebClient를 통해 비동기 api 호출
### 3. 보안 및 운영 신뢰성
- **Firebase 익명 인증 연동**: 사용자 경험을 위해 로그인 없이 익명 토큰을 통해 사용자 구분
- **AOP 기반 관리**: `@RateLimitedApi` 어노테이션으로 비즈니스 로직과 분리된 호출 제어
- **장애 복구**: 외부 API 호출 실패 시 Fallback 응답 제공
### 4. 비용 고려
- **사용자 호출 제한:** 과도한 llm api 호출을 막기 위해 익명 토큰을 통해 사용자 api 호출수 및 시스템 api 호출수 관리
- **무료 배포 :** Render(back.)와 Vercel(Front.)를 통해 무료 배포.
- **데이터 다이어트 :** 입력 토큰 및 출력 토큰 절약을 위해 필요한 데이터만 요청 및 응답

### 📊 API 설계

#### 주요 엔드포인트
```http request
GET /api/recommend/{alcohol}
Authorization: Bearer {Firebase JWT}
Params: minX, minY, maxX, maxY (지리적 경계 좌표)

{
  "message": "맛집 추천 조회 api 성공!!",
  "data": {
    "recommendations": [
      {
        "place": {
          "id": "123456789",
          "name": "현대백화점 무역센터점",
          "category": "음식점 > 고기집",
          "address": "서울 강남구 ..."
        },
        "score": 920,
        "reason": "소주와 잘 어울리는 신선한 해산물 요리를 제공하며...",
      }
    ]
  }
}
```

### AI 프롬프트 엔지니어링
시스템은 각 AI 단계별로 모듈화된 프롬프트 템플릿을 사용합니다:
1. **필터링 단계 :** kakao local api를 통해 얻은 가게 정보를 기반으로 주종에 따른 식당의 적합성 분석 및 필터링
2. **웹 그라운딩 :** 필터링된 가게의 최신 정보를 검색하여 데이터 보강
3. **최종 추천 :** 점수와 추천 사유가 포함된 결과 생성

