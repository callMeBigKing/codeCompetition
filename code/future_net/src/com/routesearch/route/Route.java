/**
 * 实现代码文件
 * 
 * @author XXX
 * @since 2016-3-4
 * @version V1.0
 */
package com.routesearch.route;



import com.filetool.util.LogUtil;
import com.sun.org.apache.bcel.internal.generic.GOTO;
import com.sun.org.apache.bcel.internal.generic.NEW;


import javax.swing.text.AbstractDocument;
import java.util.ArrayList;
import java.util.Date;
import java.util.Stack;

public final class Route
{
    /**
     * 你需要完成功能的入口
     * 
     * @author XXX
     * @since 2016-3-4
     * @version V1
     */
    private static GAAA GA;
    public static ArrayList<int[]>[] graphList;
    private static int pointNum;
    private static int edgeNum;
    private static int [][]contenInt;
    public static String searchRoute(String graphContent, String condition,long startTime) {

        ArrayList<int []>[]graph=ContentTrans(graphContent);
        int []demand=DemandTrans(condition);
        if(demand.length==2){
            ArrayList<Integer>bestRoute=noDmand(graph,demand);
            if(bestRoute.size()==0) {
                return "NA";
            }
            ArrayList<Integer> edge = PointTransEdge(bestRoute);
            String str = "";
            for (int i = 0; i < edge.size(); i++) {
                str += edge.get(i).toString();
                if (i != edge.size() - 1) str += "|";
            }
            return (str);
        }
        GA=new GAAA(graphList,demand);
        GA.setMsg(pointNum,edgeNum);
        double coe=0;
        if(pointNum<=20){
            coe=0.5;
        }else if(pointNum<40){
            coe=0.8;
        }
        else if(pointNum<50){
            coe=1;
        }else if(pointNum<100){
            coe=2.7;
        }
        else if(pointNum<=300){
            coe=4;
        }else {
            coe=5;
        }

        int maxIter=(int)(100*coe);
//        LogUtil.printLog("End");
        GA.InitPop();
//        LogUtil.printLog("End");
        for(int i=0;i<maxIter;i++){
            GA.Breed();
//           System.out.println( "  iter:"+i);
           GA.Mutation();
            if(pointNum>200){
                if(System.currentTimeMillis()-startTime>9400) {
//                    System.out.println( "  iter:"+i);
                    break;
                }
            }
        }

        GA.CalculFit();
        ArrayList<Integer>bestRoute=GA.getBestRoute();
//        System.out.printl
// n(Tool.Connected(graphList,bestRoute)+" 符合规范");

        boolean flage= GA.JugeDemand();
        if(flage==false){
            return "NA";
        } else {
            ArrayList<Integer> edge = PointTransEdge(bestRoute);
            String str = "";
            for (int i = 0; i < edge.size(); i++) {
                str += edge.get(i).toString();
                if (i != edge.size() - 1) str += "|";
            }
//            System.out.print("weight:"+ GA.CalculWeight(bestRoute)+" ");

            return (str);
        }
//        boolean flage= GA.JugeDemand();
//        System.out.println(flage+"  demand");
//        String str = "";
//        for (int i = 0; i < bestRoute.size(); i++) {
//            str += bestRoute.get(i).toString();
//            if (i != bestRoute.size() - 1) str += ",";
//        }
//        System.out.print("weight:"+ GA.CalculWeight(bestRoute)+" ");
//        return (str);
    }

    private static ArrayList<Integer> noDmand(ArrayList<int []>[]graph,int []demand){
//        demand 中只有起点和终点没有中间点
        ArrayList<Integer> route=Tool.dijstra(graph,demand[0],demand[1]);
        return route;

    }

    public static ArrayList<int []>[] ContentTrans(String conditionContent){
//        数据转换功能
        String []edge=conditionContent.split("\n");
        edgeNum=edge.length;
        String [][]contentStr=new String[edge.length][4];
        contenInt=new int[edge.length][3];
        int maxPoint=0;
        for(int i=0;i<edge.length;i++) {
            contentStr[i]=edge[i].split(",");
            for(int j=1;j<4;j++){
                contenInt[i][j-1]=Integer.parseInt(contentStr[i][j]);
                if(maxPoint<contenInt[i][j-1]&&j<3)maxPoint=contenInt[i][j-1];
//                注意这里有加1的内容
            }
        }
        maxPoint+=1;
        pointNum=maxPoint;
        ArrayList<int []>[] contentArr=new ArrayList[maxPoint];
        for(int i=0;i<maxPoint;i++){
            contentArr[i]=new ArrayList<int[]>();
        }
//      这个 arrlist 中存的是int[2]类型的数组 i
        for(int i=0;i<contenInt.length;i++) {
            //            遍历contenInt
//            contenInt[i][0]为起点 contenInt[i][1]为终点
            int []first={contenInt[i][1],contenInt[i][2]};
            contentArr[contenInt[i][0]].add(first);

        }
        graphList=contentArr;


        return contentArr;
    }

    public static int [] DemandTrans(String condition){
//数据转换功能
        condition=condition.split("\n")[0];//去除最后的换行符号
        String []strings=condition.split(",");

        if(strings.length>2) {
            String[]  midPoint= strings[2].split("\\|");
            int length=midPoint.length+2;
            int [] demand=new int[length];
            demand[0]=Integer.parseInt(strings[0]);
            for(int i=0;i<midPoint.length;i++){
                demand[i+1]=Integer.parseInt(midPoint[i]);
            }
            demand[length-1]=Integer.parseInt(strings[1]);

            return demand;
        }else {
            int [] demand ={Integer.parseInt(strings[0]),Integer.parseInt(strings[1])};
            return demand;
        }


    }

    private static ArrayList<Integer> PointTransEdge(ArrayList<Integer>route){
//        结果中的点转换成边
        ArrayList<Integer>edge=new ArrayList<Integer>(route.size()-1);
        for(int i=0;i<route.size()-1;i++){
            int point=route.get(i);
            int nextPoint=route.get(i+1);
            for(int j=0;j<edgeNum;j++){
                if(contenInt[j][0]==point&&contenInt[j][1]==nextPoint){
                    edge.add(j);
                    break;
                }
            }
        }
        return edge;
    }

    private static void BianLi(int start,int end){
        ArrayList<Integer>[] routes=new ArrayList[500];
        Stack<Integer>stack=new Stack<Integer>();
        stack.add(start);
        ArrayList<Integer>[] already=new ArrayList[pointNum];
        for (int i=0;i<already.length;i++){
            already[i]=new ArrayList<Integer>();
        }
        int thisPoint=stack.peek();
        int prePoint=stack.peek();
        while (graphList[start].size()>already[start].size()){
            while (graphList[thisPoint].size()>already[thisPoint].size()) {

                int tempPoint=graphList[thisPoint].get(already[thisPoint].size())[0];
//                尝试性走一步如果形成了环则不干了
                if(tempPoint==end){
                    already[thisPoint].add(1);
                    break;
                }
                if(stack.contains(tempPoint)){
                    already[thisPoint].add(1);
                }else {
                    prePoint = thisPoint;
                    thisPoint=tempPoint;
                    already[prePoint].add(1);
                    stack.push(thisPoint);

                }

            }

        }

    }

}