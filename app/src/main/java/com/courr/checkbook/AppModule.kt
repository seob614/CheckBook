package com.courr.checkbook

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)  // SingletonComponent: 애플리케이션 전역에서 사용할 수 있도록 설정
object AppModule {

}