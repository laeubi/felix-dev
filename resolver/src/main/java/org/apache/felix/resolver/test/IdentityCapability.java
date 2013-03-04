/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.felix.resolver.test;

import java.util.HashMap;
import java.util.Map;

import org.osgi.framework.namespace.IdentityNamespace;
import org.osgi.resource.Capability;
import org.osgi.resource.Resource;

class IdentityCapability implements Capability
{
    private final Resource m_resource;
    private final Map<String, String> m_dirs;
    private final Map<String, Object> m_attrs;

    public IdentityCapability(Resource resource, String name, String type)
    {
        m_resource = resource;
        m_dirs = new HashMap<String, String>();
        m_attrs = new HashMap<String, Object>();
        m_attrs.put(IdentityNamespace.IDENTITY_NAMESPACE, name);
        m_attrs.put(IdentityNamespace.CAPABILITY_TYPE_ATTRIBUTE, type);
    }

    public String getNamespace()
    {
        return IdentityNamespace.IDENTITY_NAMESPACE;
    }

    public Map<String, String> getDirectives()
    {
        return m_dirs;
    }

    public Map<String, Object> getAttributes()
    {
        return m_attrs;
    }

    public Resource getResource()
    {
        return m_resource;
    }

    @Override
    public String toString()
    {
        return getNamespace() + "; "
            + getAttributes().get(IdentityNamespace.IDENTITY_NAMESPACE).toString();
    }
}