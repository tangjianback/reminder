#基础镜像(看你的pom.xml中java的版本，记得不是直接java17,自己查查用哪个base镜像)
FROM openjdk:17-oracle
#作者
MAINTAINER tangjianback@gmail.com
#声称容器卷目录（不懂)
VOLUME /tmp
#将宿主机jar包拷贝到容器中，此命令会将jar包拷贝到容器的根路径/下
ARG DEPENDENCY=target/dependency
COPY ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY ${DEPENDENCY}/META-INF /app/META-INF
COPY ${DEPENDENCY}/BOOT-INF/classes /app
#暴露端口8081
EXPOSE 8080
#容器启动时执行的命令
ENTRYPOINT ["java","-cp","app:app/lib/*","com.example.demo.DemoApplication"]
