# FELIX-6724 Test Case

This directory contains test cases for FELIX-6724 and FELIX-6069, which report issues with incorrectly detected circular dependencies in Felix SCR.

## Files in this Test Case

### Test Components
- `ServiceA.java` - Component A with dependency on ServiceB
- `ServiceB.java` - Component B with dependency on ServiceC
- `ServiceC.java` - Component C with dependency on ServiceA (creates circular reference)

### Test Configuration
- `../../test/resources/integration_test_FELIX_6724.xml` - Component descriptors defining 4 test scenarios

### Test Class
- `../../Felix6724Test.java` - Integration test with 4 scenarios

### Documentation
See the scr directory root for:
- `FELIX-6724-SUMMARY.md` - Overview and test results
- `FELIX-6724-ANALYSIS.md` - Detailed analysis of detection algorithm
- `FELIX-6724-MITIGATIONS.md` - Potential mitigation strategies

## Test Scenarios

### Scenario 1: Real Circular Dependency
Tests that true circular dependencies (all mandatory) are correctly detected and prevented.

### Scenario 2: Optional Dependency Chain (Immediate)
Tests that circular dependencies with optional links work correctly with immediate activation.

### Scenario 3: Optional Dependency Chain (Delayed)
Tests that circular dependencies with optional links work correctly with delayed activation.

### Scenario 4: Static Binding Policy
Tests that static binding policy doesn't negatively affect circular dependency handling.

## Running the Tests

```bash
cd /path/to/felix-dev/scr
mvn clean verify -Dit.test=Felix6724Test
```

## Results

All tests pass, indicating that the current Felix SCR implementation correctly:
- Detects true circular dependencies
- Resolves circular dependencies with optional links
- Works with both immediate and delayed activation
- Works with both static and dynamic binding policies
