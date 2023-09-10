package com.example.demo;

import com.example.demo.Morning.Pusher;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.ResourceUtils;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

@SpringBootApplication
@EnableScheduling
public class DemoApplication extends WebMvcConfigurationSupport {

	public static void main(String[] args) {
		Date date = new Date();
		SimpleDateFormat dateFormat= new SimpleDateFormat("yyyy-MM-dd :HH:mm:ss");
		System.out.println(dateFormat.format(date));
		SpringApplication.run(DemoApplication.class, args);
	}
	@Scheduled(cron = "0 0 7 * * ?")
	public void goodMorning(){
		Date date = new Date();
		SimpleDateFormat dateFormat= new SimpleDateFormat("yyyy-MM-dd :HH:mm:ss");
		System.out.println(dateFormat.format(date));
		String userid_1 = "oDP1_6QzjppAUi3RS_GdKgD2_C4w";
		String userid_2 = "oDP1_6QWwJn5do1d-4N6ZdJ_rFDM";
		Pusher.push(userid_1);
		Pusher.push(userid_2);
	}

	@Override
	protected void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/css/**").addResourceLocations(ResourceUtils.CLASSPATH_URL_PREFIX + "/static/css/");
		registry.addResourceHandler("/js/**").addResourceLocations(ResourceUtils.CLASSPATH_URL_PREFIX + "/static/js/");
		registry.addResourceHandler("/libs/**").addResourceLocations(ResourceUtils.CLASSPATH_URL_PREFIX + "/static/libs/");
		registry.addResourceHandler("/img/**").addResourceLocations(ResourceUtils.CLASSPATH_URL_PREFIX + "/static/img/");
		registry.addResourceHandler("/uploadFile/**").addResourceLocations(ResourceUtils.CLASSPATH_URL_PREFIX +"/static/uploadFile/");
		registry.addResourceHandler("/favicon.ico").addResourceLocations(ResourceUtils.CLASSPATH_URL_PREFIX + "/static/favicon.ico");
		super.addResourceHandlers(registry);
	}
}
