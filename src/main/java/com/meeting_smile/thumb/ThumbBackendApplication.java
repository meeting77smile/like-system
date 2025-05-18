package com.meeting_smile.thumb;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author pine
 */
@SpringBootApplication
@EnableScheduling
@MapperScan("com.meeting_smile.thumb.mapper")//告诉 MyBatis 哪些包下的接口需要被解析为 Mapper，无需手动为每个接口添加 @Mapper 注解。
public class ThumbBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(ThumbBackendApplication.class, args);
    }

}
