# Java 동시성 제어 방식 및 각 적용의 장/단점

Java에서의 대표적인 동시성 제어 방식들과 그 적용 사례, 장단점에 대해 정리한 자료입니다. 멀티스레드 환경에서의 데이터 정합성과 안정성 확보를 위한 기초 내용 입니다.

---

## 1. Synchronized (동기화 키워드)

### 설명
- Java의 가장 기본적인 동시성 제어 방식
- 메서드나 블록에 `synchronized` 키워드를 붙여 한 번에 하나의 스레드만 접근 가능하도록 제한

### 장점
- 구현이 간단하고 직관적임
- JVM 차원에서 지원되므로 별도의 라이브러리 없이 사용 가능

### 단점
- 성능 저하 가능성 (블로킹으로 인한 스레드 대기)
- 교착 상태(deadlock) 위험 존재
- 세밀한 락 제어 불가능 (전체 메서드나 객체 단위)

---

## 2. ReentrantLock

### 설명
- `java.util.concurrent.locks` 패키지 제공
- 명시적으로 lock() / unlock() 제어
- 공정성 설정, tryLock(), 조건 변수(Condition) 등 기능 제공

### 장점
- 세밀한 락 제어 가능
- tryLock으로 데드락 회피 가능
- 공정성(fairness) 옵션 지원

### 단점
- 사용자가 명시적으로 락 해제를 해야 하므로 실수 시 문제가 발생할 수 있음
- 코드가 복잡해짐

---

## 3. Atomic 클래스 (CAS 기반)

### 설명
- `java.util.concurrent.atomic` 패키지 제공
- `AtomicInteger`, `AtomicLong` 등 원자성 보장
- 내부적으로 Compare-And-Swap(CAS) 사용

### 장점
- 락 없이 빠른 동시성 제어 가능
- 성능이 뛰어남 (락 기반 방식보다 빠름)

### 단점
- 복잡한 연산이나 복수 필드 처리에 적합하지 않음
- 스핀락으로 인한 CPU 낭비 가능성 존재

---

## 4. Concurrent Collections

### 설명
- `ConcurrentHashMap`, `CopyOnWriteArrayList` 등
- 내부적으로 락 또는 CAS 기반으로 동기화 처리됨

### 장점
- Thread-safe한 컬렉션 제공
- 고성능 및 병렬 처리에 유리함

### 단점
- 구조에 따라 성능이 달라지므로 목적에 맞는 선택이 필요
- 쓰기 작업이 많은 경우 `CopyOnWrite` 계열은 비효율적

---

## 5. Volatile 키워드

### 설명
- 변수의 값을 각 스레드의 캐시가 아닌 메인 메모리에서 직접 읽고 쓰도록 보장

### 장점
- 캐시 일관성 문제 해결
- 간단한 상태 플래그 등에 유용

### 단점
- 원자성을 보장하지 않음 (복합 연산에는 부적합)
- 제한적인 사용 용도

---

## 6. ThreadLocal

### 설명
- 각 스레드마다 독립적인 변수를 제공

### 장점
- 스레드 간 데이터 격리를 통해 동기화 없이 안전하게 사용 가능
- 성능 우수 (락이 필요 없음)

### 단점
- 잘못 사용할 경우 메모리 누수 발생 가능
- 관리가 어려움 (특히 스레드 풀 사용 시 주의 필요)

---

## 비교 요약

| 방식               | 락 사용 | 성능 | 복잡도 | 사용 예시 |
|--------------------|---------|------|--------|-----------|
| synchronized       | 있음    | 낮음 | 낮음   | 단순한 메서드 동기화 |
| ReentrantLock      | 있음    | 중간 | 중간   | 세밀한 락 제어 필요 시 |
| Atomic 클래스      | 없음    | 높음 | 낮음   | 카운터, 플래그 등 단일 변수 조작 |
| Concurrent 컬렉션  | 있음/없음| 높음 | 중간   | 공유 자료구조 병렬 처리 |
| volatile           | 없음    | 높음 | 낮음   | 상태 플래그 |
| ThreadLocal        | 없음    | 높음 | 중간   | 사용자 정보, 트랜잭션 등 스레드별 변수 필요 시 |

---

## 결론

Java에서는 다양한 동시성 제어 도구를 제공하며, 각각의 도구는 상황과 목적에 따라 선택되어야 합니다. 단순한 경우는 `synchronized`, 고성능을 요구하는 경우는 `Atomic` 또는 `Concurrent` 컬렉션, 세밀한 제어가 필요한 경우는 `ReentrantLock`, 데이터 격리가 필요한 경우는 `ThreadLocal` 등으로 사용 목적을 명확히 하고 설계하는 것이 중요합니다.

