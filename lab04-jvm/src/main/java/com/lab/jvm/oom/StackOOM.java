package com.lab.jvm.oom;

public class StackOOM{
    private int  depth = 0;

    public void recurse(){
        depth++;
        recurse(); // 无限递归
    }

    public static void main(String args[]){
        StackOOM oom = new StackOOM();
        try{
            oom.recurse();
        }catch (StackOverflowError e){
            System.out.println("栈深度: " + oom.depth);
        }
    }

}