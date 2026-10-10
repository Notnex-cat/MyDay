package com.notnex.myday.di

import javax.inject.Qualifier

/** CoroutineScope, живущий столько же, сколько процесс: для записей, которые не должны отменяться вместе с экраном. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope
