package com.lab.jvm.oom;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;

public class MetaspaceOOM {
    public static void main(String[] args) {
        int i = 0;
        try {
            while (true) {
                // 用 CGLIB 动态生成类，填满元空间
                Enhancer enhancer = new Enhancer();
                enhancer.setSuperclass(OOMObject.class);
                enhancer.setUseCache(false);
                enhancer.setCallback((MethodInterceptor) (obj, method, args1, proxy) ->
                    proxy.invokeSuper(obj, args1));
                enhancer.create();
                System.out.println("创建 " + (++i) + " 个动态类");
            }
        } catch (Throwable e) {
            //System.out.println("元空间 OOM! 共创建 " + i + " 个类");
            //e.getClass().getName();
            System.out.println(i);
        }
    }
    static class OOMObject {}
}