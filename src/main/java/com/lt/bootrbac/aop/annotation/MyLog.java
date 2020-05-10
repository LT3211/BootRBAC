package com.lt.bootrbac.aop.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MyLog {

    /**
     * 用户操作的哪个模块
     */
    String title() default "";

    /**
     * 记录用户操作的动作
     * @return
     */
    String action() default "";

}
