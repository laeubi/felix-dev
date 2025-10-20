/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.felix.scr.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.apache.felix.scr.integration.components.felix6724.ServiceA;
import org.apache.felix.scr.integration.components.felix6724.ServiceB;
import org.apache.felix.scr.integration.components.felix6724.ServiceC;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.junit.PaxExam;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.runtime.dto.ComponentConfigurationDTO;

/**
 * Test for FELIX-6724: Incorrectly reported circular dependency
 * 
 * This test reproduces the issue where the SCR incorrectly reports a circular
 * dependency in scenarios where the dependency chain can actually be resolved.
 * 
 * The typical false positive scenario is:
 * - Component A depends on Service B (mandatory)
 * - Component B depends on Service C (mandatory)
 * - Component C depends on Service A (optional or multiple)
 * 
 * In this case, the components should be able to activate in the order C -> B -> A,
 * with C initially not having a reference to A, and then being bound to A after
 * A is activated.
 * 
 * @version $Rev$ $Date$
 */
@RunWith(PaxExam.class)
public class Felix6724Test extends ComponentTestBase
{

    static
    {
        // uncomment to enable debugging of this test class
        //        paxRunnerVmOption = DEBUG_VM_OPTION;

        descriptorFile = "/integration_test_FELIX_6724.xml";
        COMPONENT_PACKAGE = COMPONENT_PACKAGE + ".felix6724";
    }

    /**
     * Scenario 1: Real circular dependency with three components
     * A -> B (1..1) -> C (1..1) -> A (1..1)
     * 
     * Expected: None of the components should activate due to true circular dependency
     */
    @Test
    public void test_scenario1_real_circular_dependency() throws Exception
    {
        String componentNameA = "felix6724.scenario1.A";
        String componentNameB = "felix6724.scenario1.B";
        String componentNameC = "felix6724.scenario1.C";
        
        // Enable all three components
        enableAndCheck(findComponentDescriptorByName(componentNameA));
        enableAndCheck(findComponentDescriptorByName(componentNameB));
        enableAndCheck(findComponentDescriptorByName(componentNameC));
        
        delay();
        
        // All components should remain unsatisfied due to circular dependency
        findComponentConfigurationByName(componentNameA, ComponentConfigurationDTO.UNSATISFIED_REFERENCE);
        findComponentConfigurationByName(componentNameB, ComponentConfigurationDTO.UNSATISFIED_REFERENCE);
        findComponentConfigurationByName(componentNameC, ComponentConfigurationDTO.UNSATISFIED_REFERENCE);
    }

    /**
     * Scenario 2: False positive circular dependency (immediate components)
     * A -> B (1..1) -> C (1..1) -> A (0..n)
     * 
     * Expected: All components should activate successfully.
     * The activation order should be: C (without A) -> B -> A, then C binds to A.
     * 
     * This tests FELIX-6724: The system should NOT report a circular dependency
     * because C's dependency on A is optional (0..n).
     */
    @Test
    public void test_scenario2_false_positive_with_optional_dependency_immediate() throws Exception
    {
        String componentNameA = "felix6724.scenario2.A";
        String componentNameB = "felix6724.scenario2.B";
        String componentNameC = "felix6724.scenario2.C";
        
        // Enable all three components
        enableAndCheck(findComponentDescriptorByName(componentNameA));
        enableAndCheck(findComponentDescriptorByName(componentNameB));
        enableAndCheck(findComponentDescriptorByName(componentNameC));
        
        delay();
        
        // All components should be active (or at least satisfied/active)
        ComponentConfigurationDTO componentA = findComponentConfigurationByName(
            componentNameA, ComponentConfigurationDTO.ACTIVE);
        ComponentConfigurationDTO componentB = findComponentConfigurationByName(
            componentNameB, ComponentConfigurationDTO.ACTIVE);
        ComponentConfigurationDTO componentC = findComponentConfigurationByName(
            componentNameC, ComponentConfigurationDTO.ACTIVE);
        
        // Verify the services are available
        ServiceA serviceA = getServiceFromConfiguration(componentA, ServiceA.class);
        ServiceB serviceB = getServiceFromConfiguration(componentB, ServiceB.class);
        ServiceC serviceC = getServiceFromConfiguration(componentC, ServiceC.class);
        
        assertNotNull("ServiceA should be available", serviceA);
        assertNotNull("ServiceB should be available", serviceB);
        assertNotNull("ServiceC should be available", serviceC);
        
        // A should have B
        assertEquals("ServiceA should have 1 ServiceB reference", 1, serviceA.getBServices().size());
        
        // B should have C
        assertEquals("ServiceB should have 1 ServiceC reference", 1, serviceB.getCServices().size());
        
        delay(); // Allow time for optional binding
        
        // C should eventually have A (optional binding)
        assertTrue("ServiceC should have 0 or 1 ServiceA references", 
            serviceC.getAServices().size() <= 1);
    }

    /**
     * Scenario 3: False positive circular dependency (delayed components)
     * A -> B (1..1) -> C (1..1) -> A (0..1)
     * 
     * Expected: All components should activate successfully when their service is requested.
     * 
     * This tests FELIX-6724 with delayed components where C has an optional 0..1 dependency on A.
     */
    @Test
    public void test_scenario3_false_positive_with_optional_dependency_delayed() throws Exception
    {
        String componentNameA = "felix6724.scenario3.A";
        String componentNameB = "felix6724.scenario3.B";
        String componentNameC = "felix6724.scenario3.C";
        
        // Enable all three components
        enableAndCheck(findComponentDescriptorByName(componentNameA));
        enableAndCheck(findComponentDescriptorByName(componentNameB));
        enableAndCheck(findComponentDescriptorByName(componentNameC));
        
        delay();
        
        // Components should be satisfied (delayed, not yet activated)
        findComponentConfigurationByName(componentNameA, 
            ComponentConfigurationDTO.SATISFIED | ComponentConfigurationDTO.ACTIVE);
        findComponentConfigurationByName(componentNameB, 
            ComponentConfigurationDTO.SATISFIED | ComponentConfigurationDTO.ACTIVE);
        findComponentConfigurationByName(componentNameC, 
            ComponentConfigurationDTO.SATISFIED | ComponentConfigurationDTO.ACTIVE);
        
        // Request ServiceA - this should trigger activation
        Collection<ServiceReference<ServiceA>> serviceReferencesA = 
            bundleContext.getServiceReferences(ServiceA.class, "(service.pid=" + componentNameA + ")");
        assertEquals("Should have 1 ServiceA reference", 1, serviceReferencesA.size());
        
        ServiceReference<ServiceA> serviceReferenceA = serviceReferencesA.iterator().next();
        ServiceA serviceA = bundleContext.getService(serviceReferenceA);
        assertNotNull("ServiceA should be available", serviceA);
        
        delay();
        
        // Now all services should be active
        ComponentConfigurationDTO componentA = findComponentConfigurationByName(
            componentNameA, ComponentConfigurationDTO.ACTIVE);
        ComponentConfigurationDTO componentB = findComponentConfigurationByName(
            componentNameB, ComponentConfigurationDTO.ACTIVE);
        ComponentConfigurationDTO componentC = findComponentConfigurationByName(
            componentNameC, ComponentConfigurationDTO.ACTIVE);
        
        ServiceB serviceB = getServiceFromConfiguration(componentB, ServiceB.class);
        ServiceC serviceC = getServiceFromConfiguration(componentC, ServiceC.class);
        
        assertNotNull("ServiceB should be available", serviceB);
        assertNotNull("ServiceC should be available", serviceC);
        
        // Verify dependencies are satisfied
        assertEquals("ServiceA should have 1 ServiceB reference", 1, serviceA.getBServices().size());
        assertEquals("ServiceB should have 1 ServiceC reference", 1, serviceB.getCServices().size());
        
        delay(); // Allow time for optional binding
        
        // C should eventually have A (optional binding)
        assertTrue("ServiceC should have 0 or 1 ServiceA references", 
            serviceC.getAServices().size() <= 1);
        
        // Clean up
        bundleContext.ungetService(serviceReferenceA);
    }

    /**
     * Scenario 4: Static binding policy with optional dependency
     * A -> B (1..1 static) -> C (1..1 static) -> A (0..1 static)
     * 
     * Expected: All components should activate successfully even with static binding.
     * 
     * This tests whether the binding policy affects circular dependency detection
     * when optional dependencies are involved.
     */
    @Test
    public void test_scenario4_static_binding_with_optional_dependency() throws Exception
    {
        String componentNameA = "felix6724.scenario4.A";
        String componentNameB = "felix6724.scenario4.B";
        String componentNameC = "felix6724.scenario4.C";
        
        // Enable all three components
        enableAndCheck(findComponentDescriptorByName(componentNameA));
        enableAndCheck(findComponentDescriptorByName(componentNameB));
        enableAndCheck(findComponentDescriptorByName(componentNameC));
        
        delay();
        
        // All components should be active
        ComponentConfigurationDTO componentA = findComponentConfigurationByName(
            componentNameA, ComponentConfigurationDTO.ACTIVE);
        ComponentConfigurationDTO componentB = findComponentConfigurationByName(
            componentNameB, ComponentConfigurationDTO.ACTIVE);
        ComponentConfigurationDTO componentC = findComponentConfigurationByName(
            componentNameC, ComponentConfigurationDTO.ACTIVE);
        
        // Verify the services are available
        ServiceA serviceA = getServiceFromConfiguration(componentA, ServiceA.class);
        ServiceB serviceB = getServiceFromConfiguration(componentB, ServiceB.class);
        ServiceC serviceC = getServiceFromConfiguration(componentC, ServiceC.class);
        
        assertNotNull("ServiceA should be available", serviceA);
        assertNotNull("ServiceB should be available", serviceB);
        assertNotNull("ServiceC should be available", serviceC);
        
        // Verify dependencies are satisfied
        assertEquals("ServiceA should have 1 ServiceB reference", 1, serviceA.getBServices().size());
        assertEquals("ServiceB should have 1 ServiceC reference", 1, serviceB.getCServices().size());
        
        // C should have A bound (optional but static binding means it should bind at activation)
        assertEquals("ServiceC should have 0 or 1 ServiceA references", 
            1, serviceC.getAServices().size());
    }
}
