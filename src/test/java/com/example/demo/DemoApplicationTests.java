package com.example.demo;
import com.example.demo.Dao.Dao;
import com.example.demo.service.Service;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Date;
import java.util.Properties;
import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;


@SpringBootTest
class DemoApplicationTests {

	@Test
	public void test()  {
		//SendEmail("tangjians@icloud.com", "www.baidu.com", "jiange");
		StringBuffer strBuf = new StringBuffer("original");
		test1(strBuf);
		System.out.println(strBuf);
	}
	public void test1(StringBuffer s)
	{
	}




}
