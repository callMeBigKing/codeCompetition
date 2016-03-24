package com.filetool.main;

import com.filetool.util.FileUtil;
import com.filetool.util.LogUtil;
import com.routesearch.route.Route;
import com.routesearch.route.Tool;

import java.util.ArrayList;
import java.util.Date;

/**
 * 工具入口
 *
 * @author
 * @version v1.0
 * @since 2016-3-1
 */
public class Main {
    public static void main(String[] args) {


        if (args.length != 3) {
            System.err.println("please input args: graphFilePath, conditionFilePath, resultFilePath");
            return;
        }

        String graphFilePath = args[0];
        String conditionFilePath = args[1];
        String resultFilePath = args[2];

//        long startTime = System.currentTimeMillis();
        LogUtil.printLog("Begin");

        // 读取输入文件
        String graphContent = FileUtil.read(graphFilePath, null);
        String conditionContent = FileUtil.read(conditionFilePath, null);
//         功能实现入口
//        for(int i=0;i<20;i++) {
            long startTime= System.currentTimeMillis();
            String resultStr = Route.searchRoute(graphContent, conditionContent, startTime);
//            System.out.println(resultStr);
//        }
        // 写入输出文件

        FileUtil.write(resultFilePath, resultStr, false);


        LogUtil.printLog("End");



    }


}
