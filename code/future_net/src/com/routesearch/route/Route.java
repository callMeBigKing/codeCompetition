/**
 * 实现代码文件
 * 
 * @author XXX
 * @since 2016-3-4
 * @version V1.0
 */
package com.routesearch.route;



import com.filetool.util.LogUtil;
import com.sun.org.apache.bcel.internal.generic.NEW;


import javax.swing.text.AbstractDocument;
import java.util.ArrayList;

public final class Route
{
    /**
     * 你需要完成功能的入口
     * 
     * @author XXX
     * @since 2016-3-4
     * @version V1
     */
    private static GaEngine GA;
    private static ArrayList<int[]>[] graphList;
    public static String searchRoute(String graphContent, String condition)
    {
        int [][][]graph=ContentTrans(graphContent);
        int []demand=DemandTrans(condition);
        GA=new GaEngine(graph,demand,graphList);
        int maxIter=500;

//        boolean flag=GA.JugeConnect();
        GA.InitPop();
        for(int i=0;i<maxIter;i++){
            GA.Breed();
            System.out.println( "  iter:"+i);
            GA.Mutation();

        }
        GA.CalculFit();
        ArrayList<Integer>bestRoute=GA.getBestRoute();

//        boolean flage= GA.JugeDemand();
//        if(flage==false){
//            return "NA";
//        }
//        else {
//            String str = "";
//            for (int i = 0; i < bestRoute.size(); i++) {
//                str += bestRoute.get(i).toString();
//                if (i != bestRoute.size() - 1) str += ",";
//            }
//            System.out.print("weight:"+ GA.getTotalWeight()+" ");
//            return (str);
//        }
        String str = "";
        for (int i = 0; i < bestRoute.size(); i++) {
            str += bestRoute.get(i).toString();
            if (i != bestRoute.size() - 1) str += ",";
        }
        System.out.print("weight:"+ GA.getTotalWeight()+" ");
        return (str);
    }

    public static int[] Test(String graphContent, String condition){
        int [][][]graph=ContentTrans(graphContent);
        int []demand=DemandTrans(condition);
        GA=new GaEngine(graph,demand,graphList);
        int maxIter=100;
        boolean flag=GA.JugeConnect();
        LogUtil.printLog("juge end");
        GA.InitPop();
        return  GA.dijstra(1,8);
    }

    public static int [][][] ContentTrans(String conditionContent){
//        数据转换功能
        String []edge=conditionContent.split("\n");
        String [][]contentStr=new String[edge.length][4];
        int [][]contenInt=new int[edge.length][3];
        int maxPoint=0;
        for(int i=0;i<edge.length;i++) {
            contentStr[i]=edge[i].split(",");
            for(int j=1;j<4;j++){
                contenInt[i][j-1]=Integer.parseInt(contentStr[i][j]);
                if(maxPoint<contenInt[i][j-1])maxPoint=contenInt[i][j-1];
//                注意这里有加1的内容
            }
        }
        maxPoint+=1;
        ArrayList<int []>[] contentArr=new ArrayList[maxPoint];
//      这个 arrlist 中存的是int[2]类型的数组 i
        for(int i=0;i<contenInt.length;i++) {
            //            遍历contenInt
//            contenInt[i][0]为起点 contenInt[i][1]为终点
            int []first={contenInt[i][1],contenInt[i][2]};
            if(contentArr[contenInt[i][0]]!=null){
                contentArr[contenInt[i][0]].add(first);
            }
            else{
                contentArr[contenInt[i][0]]=new ArrayList<int[]>();
                contentArr[contenInt[i][0]].add(first);
            }
        }
        graphList=contentArr;
        int [][][] graph=new int[maxPoint][][];
        for(int i=0;i<maxPoint;i++){
            graph[i]=new int[contentArr[i].size()][];
            for(int j=0;j<contentArr[i].size();j++){
                graph[i][j]=new int[2];
                graph[i][j]=contentArr[i].get(j);
            }
        }

        return graph;
    }

    public static int [] DemandTrans(String condition){
//数据转换功能
        condition=condition.split("\n")[0];//去除最后的换行符号
        String []strings=condition.split(",");
        String []midPoint=strings[2].split("\\|");
        int length=midPoint.length+2;
        int [] demand=new int[length];
        demand[0]=Integer.parseInt(strings[0]);
        for(int i=0;i<midPoint.length;i++){
            demand[i+1]=Integer.parseInt(midPoint[i]);
        }
        demand[length-1]=Integer.parseInt(strings[1]);

        return demand;

    }



}