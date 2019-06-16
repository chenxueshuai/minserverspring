package com.yonyou.lambda;


import java.io.BufferedReader;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by shuai on 8/12/2018.
 * lambda  jdk8 new function
 */
public class Test1 {
    public static void main(String[] args) {
        List list = new LinkedList();
        new Thread(() -> System.out.println("new thread..")).start();
        list.add("a");
        list.add("b");
        list.add("c");
        list.forEach(System.out::println);
        list.forEach(x -> System.out.print(x + ", "));

    }
}
