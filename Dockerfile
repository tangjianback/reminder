#基础镜像
FROM openjdk:17-oracle
#作者
MAINTAINER tangjianback@gmail.com
#声称容器卷目录
VOLUME /tmp
#将宿主机jar包拷贝到容器中，此命令会将jar包拷贝到容器的根路径/下
ADD target/demo-0.0.1-SNAPSHOT.jar docker-project.jar
#暴露端口8081
EXPOSE 8080
#容器启动时执行的命令
ENTRYPOINT ["java",  "-jar" ,"/docker-project.jar"]