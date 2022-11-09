package com.merative.healthpass.network.qualifier

import javax.inject.Qualifier

@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FIELD,
    AnnotationTarget.CONSTRUCTOR,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.VALUE_PARAMETER
)
@Qualifier
@MustBeDocumented
@Retention
annotation class RegularApi

@MustBeDocumented
@Qualifier
annotation class SPayApi