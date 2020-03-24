package core;

import java.lang.reflect.Method;

/**
 * @author Cai2yy
 * @date 2020/3/24 22:21
 */

public class EventBusHelper {

    Class eventBusClazz;

    Object eventBusObj;

    // 这个方法即：asyncFireEvent(String event, Obj... args)
    Method method;

    /**
     * 初始化EventBusHelper，作为外部容器和子容器的通信媒介，提供事件激活功能
     * @param eventBusClazz 外部容器中EventBus的类
     * @param eventBusObj 外部容器中EventBus的实例
     * @return
     */
    public EventBusHelper(Class eventBusClazz, Object eventBusObj) {
        this.eventBusClazz = eventBusClazz;
        this.eventBusObj = eventBusObj;
        Method[] methods = eventBusClazz.getDeclaredMethods();
        Method method0 = null;
        for (Method method : methods) {
            if (method.getName().equals("asyncFireEvent")) {
                method0 = method;
                break;
            }
        }
        if (method0 != null) {
            System.out.println(method0.getName());
        }
        this.method = method0;
    }

    /**
     * 根据子容器内程序传入的参数，激活外部容器的事件总线
     * @param event 事件key
     * @param args 事件的参数
     */
    public void fireEvent(String event, Object... args) {
        System.out.println("--------通信尝试---------" + eventBusObj.getClass().getClassLoader());
        if (method == null) {
            return;
        }
        System.out.println("通信可行" + method + ":" + eventBusClazz.getClass().getClassLoader());
        try {
            method.invoke(eventBusObj, event, args);
        } catch (Exception e) {
        }
        System.out.println("--------通信尝试---------");
    }


}
