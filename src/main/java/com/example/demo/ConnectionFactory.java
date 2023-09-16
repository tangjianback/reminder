package com.example.demo;

import com.example.demo.controller.HelloController;
import com.mchange.v2.c3p0.ComboPooledDataSource;

import java.beans.PropertyVetoException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class ConnectionFactory {
    static ComboPooledDataSource cpds = null;
    static {
        cpds = new ComboPooledDataSource();
        try {
            cpds.setDriverClass("com.mysql.jdbc.Driver");
        } catch (PropertyVetoException e) {
            throw new RuntimeException(e);
        }

        //the code below is to avoid manual connection configure switch
        InetAddress address = null;
        try {
            address = InetAddress.getLocalHost();
            System.out.println("address is "+address);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
//        if(address.getHostName().contains("localhost") || address.getHostName().contains("127.0.0.1"))
//        {
            System.out.println("local debug");
            cpds.setJdbcUrl("jdbc:mysql://localhost:3306/search?useUnicode=true&characterEncoding=utf-8&useSSL=false&autoReconnect = true");
            cpds.setUser("root");
//        }
//        else {
//            System.out.println("remote deploy");
//            cpds.setJdbcUrl("jdbc:mysql://172.17.0.2:3306/search?useUnicode=true&characterEncoding=utf-8&useSSL=false&autoReconnect = true");
//            cpds.setUser("lqq");
//        }

        cpds.setPassword("123");
        // Optional Settings
        cpds.setInitialPoolSize(5);
        cpds.setMinPoolSize(5);
        cpds.setAcquireIncrement(5);
        cpds.setMaxPoolSize(20);
        cpds.setMaxStatements(100);
        cpds.setUnreturnedConnectionTimeout(60*60*2);
        cpds.setDebugUnreturnedConnectionStackTraces(true);
        cpds.setMaxIdleTime(60*60*4);
    }
    public static Connection get_connection() throws SQLException {
        return cpds.getConnection();
    }
    public  static void CleanConection()
    {
        cpds.close();
    }
}
