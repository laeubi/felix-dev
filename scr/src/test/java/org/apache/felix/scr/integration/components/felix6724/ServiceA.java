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

package org.apache.felix.scr.integration.components.felix6724;

import java.util.ArrayList;
import java.util.List;

import org.osgi.service.component.ComponentContext;

/**
 * Service A for testing FELIX-6724 circular dependency false positive
 */
public class ServiceA
{
    private List<ServiceB> bServices = new ArrayList<ServiceB>();
    
    @SuppressWarnings("unused")
    private boolean activated;

    @SuppressWarnings("unused")
    private void activate(ComponentContext cc)
    {
        activated = true;
    }

    @SuppressWarnings("unused")
    private void setServiceB(ServiceB b)
    {
        bServices.add(b);
    }

    @SuppressWarnings("unused")
    private void unsetServiceB(ServiceB b)
    {
        bServices.remove(b);
    }

    public List<ServiceB> getBServices()
    {
        return bServices;
    }
}
