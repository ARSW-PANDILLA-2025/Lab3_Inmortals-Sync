# ARSW Lab 3 — **Concurrency & Synchronization** — Java 21 Threading Laboratory

**Escuela Colombiana de Ingeniería – Arquitecturas de Software**  
Complete concurrent programming laboratory covering producer-consumer patterns, parallel processing, and immortal synchronization with advanced threading concepts in Java 21.

![Java](https://img.shields.io/badge/Java-21-orange.svg)
![Maven](https://img.shields.io/badge/Maven-3.9+-blue.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-green.svg)
![License](https://img.shields.io/badge/License-MIT-yellow.svg)
![Build](https://img.shields.io/badge/Build-Passing-brightgreen.svg)

---

## 📋 Laboratory Overview

This comprehensive laboratory explores advanced concurrency concepts through three progressively complex parts, each demonstrating fundamental threading patterns used in modern software architecture. From basic producer-consumer synchronization to complex multi-threaded game simulations, students gain hands-on experience with real-world concurrent programming challenges.

### 🎯 Learning Objectives

- **Part I**: Master efficient CPU usage through proper synchronization patterns (`wait/notify`)
- **Part II**: Implement early termination strategies in parallel search algorithms  
- **Part III**: Build complex synchronization systems with deadlock prevention

---

## 🏗️ Project Structure

```
Lab3_Inmortals-Sync/
├── Part 1 - Busy Wait vs Wait Notify/    # Producer-Consumer Synchronization
│   ├── src/main/java/edu/eci/arsw/pc/
│   │   ├── BoundedBuffer.java             # Thread-safe bounded buffer implementation
│   │   ├── BusySpinQueue.java             # Busy-wait implementation (high CPU)
│   │   ├── Producer.java                  # Data producer with configurable timing
│   │   ├── Consumer.java                  # Data consumer with configurable timing
│   │   └── PCApp.java                     # Main application with performance modes
│   └── README.md                          # Part I specific instructions
│
├── Part 2 - Lab Threads BlackList API/   # Parallel Processing & Early Termination
│   ├── src/main/java/co/eci/blacklist/
│   │   ├── api/                          # REST API Layer
│   │   │   ├── BlacklistController.java  # Main API endpoint
│   │   │   └── dto/CheckResponseDTO.java  # Response data transfer object
│   │   ├── application/
│   │   │   └── BlacklistService.java     # Business logic coordination
│   │   ├── domain/                       # Core domain logic
│   │   │   ├── BlacklistChecker.java     # Virtual threads implementation
│   │   │   ├── MatchResult.java          # Result encapsulation
│   │   │   └── Policies.java             # Business rules
│   │   ├── infrastructure/               # External dependencies
│   │   └── labs/                         # Laboratory implementations
│   │       ├── part1/CountThread.java    # Basic threading concepts
│   │       └── part2/BlacklistChecker2.java # Traditional thread implementation
│   ├── src/test/java/                    # Comprehensive test suite (31 tests)
│   └── README.md                         # Part II detailed documentation
│
└── Part 3 - Inmortals Sync/             # Complex Synchronization & UI
    ├── src/main/java/edu/eci/arsw/
    │   ├── app/Main.java                 # Bootstrap with multiple execution modes
    │   ├── highlandersim/               # Swing UI (Highlander Simulator)
    │   │   └── ControlFrame.java        # Main UI with Start/Pause/Resume/Stop
    │   ├── immortals/                   # Core game domain
    │   │   ├── Immortal.java            # Fighting entities with thread safety
    │   │   ├── ImmortalManager.java     # Game coordination and lifecycle
    │   │   └── ScoreBoard.java          # Thread-safe score tracking
    │   ├── concurrency/                 # Advanced synchronization primitives
    │   │   └── PauseController.java     # Cooperative pause/resume system
    │   ├── demos/                       # Deadlock demonstration and solutions
    │   └── core/                        # Banking examples for deadlock scenarios
    ├── src/test/java/                   # Comprehensive testing suite
    └── README.md                        # Part III instructions and UI guide
```
## Component Diagram
![alt text](image.png)

## 🚀 Quick Start

### Prerequisites

Ensure you have the following installed before starting:

- **Java 21** (Temurin, Oracle, or OpenJDK)
- **Maven 3.9+** for dependency management
- **Git** for version control
- **jVisualVM** (optional) for performance monitoring

```bash
# Verify installations
java -version    # Should show Java 21.x.x
mvn -version     # Should show Maven 3.9.x or higher
```

### Complete Laboratory Setup

1. **Clone the repository**
```bash
git clone https://github.com/ARSW-PANDILLA-2025/Lab3_Inmortals-Sync.git
cd Lab3_Inmortals-Sync
```

2. **Build all parts**
```bash
# Build Part 1 (Producer-Consumer)
cd "Part 1 - Busy Wait vs Wait Notify"
mvn clean compile
cd ..

# Build Part 2 (Blacklist API)  
cd "Part 2 - Lab Threads BlackList API"
mvn clean compile test
cd ..

# Build Part 3 (Immortals Sync)
cd "Part 3 - Inmortals Sync"
mvn clean compile test
cd ..
```

3. **Run quick verification tests**
```bash
# Test Part 1 - Producer Consumer
cd "Part 1 - Busy Wait vs Wait Notify"
mvn -q exec:java -Dexec.mainClass=edu.eci.arsw.pc.PCApp -Dmode=monitor -DdurationSec=5
cd ..

# Test Part 2 - Blacklist API
cd "Part 2 - Lab Threads BlackList API" 
mvn test -Dtest=BlacklistCheckerTest
cd ..

# Test Part 3 - Immortals UI
cd "Part 3 - Inmortals Sync"
mvn test -Dtest=PauseControlTest
cd ..
```

---

## 📚 Laboratory Parts

### Part I — Producer-Consumer Synchronization

**Learning Focus**: Efficient CPU usage through proper synchronization patterns

**Key Concepts Demonstrated**:
- Busy-wait vs. efficient waiting strategies
- Java monitors (`synchronized`, `wait()`, `notify()`)
- Performance impact analysis with jVisualVM
- Bounded buffer implementations

**Quick Execution**:
```bash
cd "Part 1 - Busy Wait vs Wait Notify"

# High CPU usage (busy-wait) - observe with jVisualVM
mvn -q exec:java -Dmode=spin -Dproducers=1 -Dconsumers=1 -DprodDelayMs=50 -DconsDelayMs=1

# Efficient CPU usage (monitors)
mvn -q exec:java -Dmode=monitor -Dproducers=1 -Dconsumers=1 -DprodDelayMs=50 -DconsDelayMs=1
```

**Performance Analysis Scenarios**:
- **Slow Producer / Fast Consumer**: Consumer waits efficiently when buffer is empty
- **Fast Producer / Slow Consumer**: Producer waits efficiently when buffer is full
- **Bounded Buffer**: Respects capacity limits without active waiting

### Part II — Parallel Processing & Early Termination

**Learning Focus**: Distributed search algorithms with intelligent stopping conditions

**Key Concepts Demonstrated**:
- Thread segmentation and load balancing
- Early termination strategies to avoid unnecessary work
- Thread-safe result aggregation
- Performance scaling analysis
- Virtual threads vs traditional threads

**API Execution**:
```bash
cd "Part 2 - Lab Threads BlackList API"

# Start the REST API
mvn spring-boot:run

# In another terminal, test different configurations
curl "http://localhost:8080/api/v1/blacklist/check?ip=200.24.34.55&threads=1"   # Single thread baseline
curl "http://localhost:8080/api/v1/blacklist/check?ip=202.24.34.55&threads=6"   # Optimal configuration
curl "http://localhost:8080/api/v1/blacklist/check?ip=212.24.24.55&threads=12"  # High concurrency test
```

**Comprehensive Testing**:
```bash
# Run complete test suite (31 tests across 6 test classes)
mvn test

# Run specific performance analysis
mvn test -Dtest=Test4PerformanceEvaluation#test4_completePerformanceEvaluation
```

**Test IP Cases**:
- **200.24.34.55** (Concentrated): Early matches (servers 0-9) → effective early stopping
- **202.24.34.55** (Dispersed): Spread matches → moderate early stopping  
- **212.24.24.55** (Clean): No matches → worst case, full scan required

### Part III — Complex Synchronization & Deadlock Prevention

**Learning Focus**: Advanced synchronization patterns in interactive applications

**Key Concepts Demonstrated**:
- Cooperative pause/resume mechanisms
- Deadlock prevention through ordered locking
- Invariant preservation during concurrent operations
- Real-time UI synchronization
- Thread lifecycle management

**UI Execution**:
```bash
cd "Part 3 - Inmortals Sync"

# Launch Highlander Simulator UI
mvn -q exec:java -Dmode=ui -Dcount=8 -Dfight=ordered -Dhealth=100 -Ddamage=10

# Test different fight modes
mvn -q exec:java -Dmode=ui -Dcount=8 -Dfight=naive    # Can reproduce deadlocks
mvn -q exec:java -Dmode=ui -Dcount=8 -Dfight=ordered  # Deadlock-free
```

**Deadlock Demonstrations**:
```bash
# Demonstrate different deadlock scenarios (console mode)
mvn -q exec:java -Dmode=demos -Ddemo=1  # Naive deadlock demonstration
mvn -q exec:java -Dmode=demos -Ddemo=2  # Total ordering solution
mvn -q exec:java -Dmode=demos -Ddemo=3  # TryLock with timeout approach
```

**UI Controls**:
- **Start**: Initialize simulation with configured parameters
- **Pause & Check**: Safely pause all threads and display health/invariant status
- **Resume**: Continue simulation from paused state
- **Stop**: Graceful shutdown of all immortal threads

**Mathematical Invariant**:
```
Initial Total Health = N × H
Expected Health Loss = Total Fights × (Damage ÷ 2)
Current Expected Health = Initial Total Health - Expected Health Loss
```

---

## 🧪 Testing & Validation

### Comprehensive Test Coverage

**Total Tests**: 31+ tests across all parts
- **Part I**: Producer-consumer pattern validation
- **Part II**: 31 threading and performance tests  
- **Part III**: Synchronization and invariant validation tests

### Performance Testing Framework

**Part II includes extensive performance analysis**:

```bash
# Complete performance evaluation
mvn test -Dtest=Test4PerformanceEvaluation

# Specific threading scenarios
mvn test -Dtest=Test2BlacklistChecker2Test#shouldWorkWithMultipleThreadsOnDifferentSegments
mvn test -Dtest=Test3SpecificIPsTest#test3_8_comprehensivePerformanceAnalysisWithLogging
```

**Performance Insights** (Intel i5-11400F, 6 cores, 12 threads):
- **Optimal Configuration**: 6-8 threads (1-1.3x physical cores)
- **Diminishing Returns**: Beyond 50 threads
- **Early Termination Effectiveness**: 60-95% time reduction
- **Maximum Observed Speedup**: ~5.5x (accounting for overhead)

### Monitoring & Profiling

Use **jVisualVM** to monitor:
- CPU usage patterns (busy-wait vs efficient waiting)
- Memory consumption per thread configuration
- Thread activity and contention
- GC impact under different loads

---

## 🏛️ Architecture & Design Patterns

### Part I - Monitor Pattern Implementation
```java
// Efficient waiting with Java monitors
synchronized (buffer) {
    while (condition) {
        buffer.wait();  // Releases lock and waits efficiently
    }
    // Critical section
    buffer.notifyAll(); // Wake up waiting threads
}
```

### Part II - Parallel Processing Architecture
```java
// Thread segmentation with load balancing
int serversPerThread = totalServers / numThreads;
int remainder = totalServers % numThreads;

// Early termination with atomic operations
AtomicInteger sharedCounter = new AtomicInteger(0);
if (sharedCounter.get() >= ALARM_THRESHOLD) {
    return; // Stop processing immediately
}
```

### Part III - Advanced Synchronization
```java
// Deadlock-free ordered locking
Immortal first = this.name.compareTo(other.name) < 0 ? this : other;
Immortal second = this.name.compareTo(other.name) < 0 ? other : this;
synchronized (first) {
    synchronized (second) {
        // Safe critical section
    }
}
```

---

## 📊 Performance Analysis & Results

### Threading Performance Characteristics

**Amdahl's Law Validation**:
- **Parallel Fraction**: ~92% of work can be parallelized
- **Sequential Bottleneck**: Result aggregation and response formatting
- **Theoretical Maximum Speedup**: ~12.5x
- **Practical Speedup**: ~5.5x (real-world overhead)

### Thread Count Recommendations

| Use Case | Threads | Performance | Resource Usage |
|----------|---------|-------------|----------------|
| Development | 4 | Good | Low |
| Production | 6-8 | Optimal | Moderate |
| High Load | 12 | Maximum | High |
| Batch Processing | 16-20 | Acceptable | Very High |

### Memory Impact Analysis
- **Base Memory**: ~50MB for application
- **Per Thread Cost**: ~7MB additional memory
- **GC Impact**: Minimal with proper thread management
- **Virtual Threads**: Significantly lower memory footprint

---

## 🔧 Configuration & Customization

### Environment Variables

**Part II (Blacklist API)**:
```bash
SPRING_PROFILES_ACTIVE=production
SERVER_PORT=8080
BLACKLIST_ALARM_COUNT=5
JAVA_OPTS="-Xmx2g -XX:+UseG1GC"
```

**Part III (Immortals Sync)**:
```bash
# JVM system properties
-Dcount=100           # Number of immortals
-Dfight=ordered       # Fight strategy (ordered|naive)
-Dhealth=100          # Initial health per immortal
-Ddamage=10           # Damage per attack
-Dmode=ui             # Execution mode (ui|immortals|demos)
```

### Build Profiles

```bash
# Development build
mvn clean compile

# Production build with optimizations
mvn clean package -Pproduction

# Test build with coverage
mvn clean verify -Pcoverage
```

---

## 🐛 Debugging & Troubleshooting

### Common Issues & Solutions

**Part I - High CPU Usage**:
```bash
# Problem: Consumer using 100% CPU when buffer is empty
# Solution: Replace busy-wait with wait()/notify()

# Monitor with jVisualVM
jvisualvm &
# Connect to running process and observe CPU patterns
```

**Part II - Thread Safety Issues**:
```bash
# Problem: Race conditions in shared counter
# Solution: Use AtomicInteger or synchronized blocks

# Debug with comprehensive logging
mvn test -Dtest=Test3SpecificIPsTest#test3_8_comprehensivePerformanceAnalysisWithLogging
```

**Part III - Deadlock Detection**:
```bash
# Problem: Application freezes (potential deadlock)
# Solution: Use jstack for thread dump analysis

# Get process ID
jps

# Generate thread dump
jstack <PID> > thread_dump.txt

# Analyze for circular dependencies
grep -A 10 -B 5 "waiting to lock" thread_dump.txt
```

### Diagnostic Commands

```bash
# Performance monitoring
jcmd <PID> GC.run_finalization
jcmd <PID> VM.classloader_stats
jcmd <PID> Thread.print

# Memory analysis
jmap -histogram <PID>
jmap -dump:live,format=b,file=heap.dump <PID>
```

---

## 🎯 Learning Outcomes & Assessment

### Key Competencies Developed

1. **Threading Fundamentals**
   - Thread lifecycle management
   - Synchronization primitive usage
   - Race condition identification and resolution

2. **Performance Optimization**
   - CPU usage optimization
   - Memory efficiency in concurrent applications
   - Scalability analysis and bottleneck identification

3. **Deadlock Prevention**
   - Ordered locking strategies
   - Timeout-based approaches
   - Resource allocation algorithms

4. **Real-world Application**
   - REST API threading patterns
   - Interactive UI synchronization
   - Production-ready concurrent systems

### Evaluation Criteria (10 points total)

- **(3 pts) Concurrency Correctness**: No data races, proper synchronization, no active waiting
- **(2 pts) Pause/Resume Functionality**: State consistency and invariant preservation
- **(2 pts) Robustness**: High thread count handling, no exceptions, deadlock management
- **(1.5 pts) Code Quality**: Clear architecture, documentation, separation of concerns
- **(1.5 pts) Technical Documentation**: Clear analysis with evidence and technical justification

---



## 📄 License & Academic Use

This project is licensed under the **Creative Commons Attribution-NonCommercial 4.0 International License**.

**Academic Institution**: Escuela Colombiana de Ingeniería Julio Garavito  
**Course**: Arquitectura de Software (ARSW)  
**Academic Period**: 2025-II

### Authors & Contributors

**Laboratory Development Team - ARSW-PANDILLA-2025**

* **Javier Iván Toquica Barrera** - *Course Instructor*
* **Juan Felipe Alfonso Martínez** - *Student Developer*
* **Santiago Andrés Arteaga Gutiérrez** - *Student Developer* 
* **Cristian David Polo Garrido** - *Student Developer*
* **Angie Julieth Ramos Cortés** - *Student Developer*

---

## 🎓 Educational Resources

### Recommended Reading

- **Java Concurrency in Practice** - Brian Goetz
- **Java: The Complete Reference** - Herbert Schildt
- **Effective Java** - Joshua Bloch
- **Java Performance: The Definitive Guide** - Scott Oaks

### Additional Learning Materials

- [Oracle Java Concurrency Tutorial](https://docs.oracle.com/javase/tutorial/essential/concurrency/)
- [Java 21 Virtual Threads Documentation](https://openjdk.org/jeps/444)
- [Spring Boot Threading Best Practices](https://spring.io/guides/gs/async-method/)
- [JVisualVM Performance Monitoring](https://visualvm.github.io/)

---

## 🚀 Quick Commands Reference

```bash
# Complete laboratory setup
git clone https://github.com/ARSW-PANDILLA-2025/Lab3_Inmortals-Sync.git
cd Lab3_Inmortals-Sync

# Part I - Producer Consumer (High vs Low CPU)
cd "Part 1 - Busy Wait vs Wait Notify"
mvn -q exec:java -Dmode=spin -DdurationSec=10    # High CPU (busy-wait)
mvn -q exec:java -Dmode=monitor -DdurationSec=10 # Low CPU (efficient)

# Part II - Blacklist API (Performance Testing)
cd "../Part 2 - Lab Threads BlackList API"
mvn spring-boot:run &
curl "http://localhost:8080/api/v1/blacklist/check?ip=200.24.34.55&threads=6"

# Part III - Immortals Sync (UI Simulation)
cd "../Part 3 - Inmortals Sync"
mvn -q exec:java -Dmode=ui -Dcount=8 -Dfight=ordered

# Run all tests
find . -name "pom.xml" -execdir mvn test \;
```

