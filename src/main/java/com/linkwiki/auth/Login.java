package com.linkwiki.auth;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.PARAMETER) // 파라미터에서만
@Retention(RetentionPolicy.RUNTIME) // 런타임에서도 동작되도록
public @interface Login {
}