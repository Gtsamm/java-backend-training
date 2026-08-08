package com.lab.jvm.oom;

public class StringPoolExperiment {
    public static void main(String[] args) {
        // 实验 1：字面量 vs new
        String s1 = "hello";
        String s2 = "hello";
        String s3 = new String("hello");
        String s4 = s3.intern();

        System.out.println(s1 == s2); // true  —— 指向常量池同一个位置
        System.out.println(s1 == s3); // false —— s3 在堆上
        System.out.println(s1 == s4); // true  —— intern() 返回常量池引用

        // 实验 2：字符串拼接
        String a = "a";
        String b = "b";
        String ab = "ab";
        System.out.println(ab == a + b);         // false —— 行期拼出来的是新String，不在常量池里
        System.out.println(ab == "a" + "b");     // true  —— 编译期优化
        System.out.println(ab == (a + b).intern()); // true
    }
}