package com.lab.jvm.oom;

import java.util.ArrayList;
import java.util.List;

public class HeapOOM {
    public static void main(String[] args){
        List<byte[]> list = new ArrayList<>();
        int i = 0;
        try{
            while(true){
                list.add(new byte[1024 * 1024]); // 每次分配 1MB
                System.out.println("分配 " + (++i) + " MB");
            }
        }catch (OutOfMemoryError e){
            System.out.println("OOM! 共分配 " + i + " MB");
            // dump 文件自动生成在指定路径
        }
    }
}