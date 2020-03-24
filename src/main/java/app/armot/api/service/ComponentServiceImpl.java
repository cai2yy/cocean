/**
 * Copyright © 2016-2019 The Thingsboard Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package app.armot.api.service;

import annotations.Inject;
import annotations.Named;
import annotations.Singleton;
import app.armot.api.bean.Component;
import app.armot.core.ArmOT;
import app.armot.utils.Configuration;
import core.Injector;
import utils.Scanner;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Singleton
@Named("ComponentService")
public class ComponentServiceImpl implements ComponentService {

    @Inject
    ArmOT armOT;

    private Map<String, Component> components;
    private Map<Integer, String> componentNameDict;
    private final String componentsPackage = Configuration.componentsPackage;

    public Optional<Component> getComponent(String clazz) {
        return Optional.ofNullable(components.get(clazz));
    }

    public String getComponentName(int ComponentId) {
        return componentNameDict.get(ComponentId);
    }

    public Class<?> getComponentType(String componentName) {
        Component component = components.get(componentName);
        return component.getClazz();
    }

    public Method getMethod(String componentName, int methodNum) {
        Component component = components.get(componentName);
        return component.getActions()[methodNum];
    }

    public Method getMethod(int componentId, int methodNum) {
        Component component = components.get(getComponentName(componentId));
        return component.getActions()[methodNum];
    }

    // 在服务器加载Servlet的时候运行，并且只会被服务器调用一次
    public int init() {
        this.components = armOT.getComponents();
        this.componentNameDict = armOT.getComponentNameDict();
        return discoverComponents();
    }

    private int discoverComponents() {
        List<Class<?>> componentClasses = Scanner.getClasses(componentsPackage);
        var var1 = 0;
        for (Class<?> clazz : componentClasses) {
            // 初始化component实例并放入injector缓存中
            Object obj = Injector.getInjector().getInstance(clazz);
            if (obj == null) {
                continue;
            }
            Component newComponent = new Component(clazz);
            String componentName = clazz.getSimpleName();
            componentNameDict.put(var1++, componentName);
            components.put(componentName, newComponent);
        }
        return var1;
    }

}
