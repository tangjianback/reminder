package com.example.demo;
import com.example.demo.Dao.Dao;
import com.example.demo.service.Service;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@SpringBootTest
class DemoApplicationTests {
	@Test
	public void test() throws SQLException {
		String s = "/";
		for(String i: s.split("/"))
		{
			System.out.println(i);
			System.out.println("test");
		}

	}

}
