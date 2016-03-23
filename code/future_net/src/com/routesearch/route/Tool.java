package com.routesearch.route;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

/**
 * Created by imgos on 2016/3/20.
 */
public final class Tool {


    public static ArrayList<int[]>[] CloneGraph(ArrayList<int[]>[] graphList) {
//        遍历一下深拷贝
        int length = graphList.length;
        ArrayList<int[]>[] graphListClone = new ArrayList[length];
        for (int i = 0; i < length; i++) {
            int size = graphList[i].size();
            graphListClone[i] = new ArrayList<int[]>(size);
            for (int j = 0; j < size; j++) {
                graphListClone[i].add(graphList[i].get(j).clone());
//               数组的深拷贝和浅拷贝是一样的
            }
        }
        return graphListClone;
    }



    public static ArrayList<Integer> ListClone(ArrayList<Integer> list, int start, int end) {
//        sublist 不具有clone 效果需要手动进行clone
//        [start,end);
        ArrayList listClone = new ArrayList(end - start);
        for (int i = start; i < end; i++) {
            listClone.add(list.get(i));
        }
        return listClone;
    }

    public static ArrayList<int[]>[] RemovePoint(ArrayList<int[]>[] graphListClon, int point) {
// 删除图 graphListClon 中所有连接到point的边
// remove掉已经选择的点
        for (int i = 0; i < graphListClon.length; i++) {
            for (int j = 0; j < graphListClon[i].size(); j++) {
                if (graphListClon[i].get(j)[0] == point) {
                    graphListClon[i].remove(j);
                    break; //与一个点相邻的point只会出现一次所以直接break
                }
            }
        }
        return graphListClon;
//        数组传过来的是引用，修改完成后直接再return回去就可以了。
    }

    public static ArrayList<Integer> dijstra(ArrayList<int[]>[]graphList,int i, int j)//迪杰斯特拉斯算法，
    {
        //从图graphList中找到点i，到点j的最短路径，并返回该路径
        //要求:i和j不能相同

        ArrayList<Integer> [] ArrivePointInfo;
        ArrivePointInfo=new ArrayList[3];           //   0:当前点，1:当前点的父代点的存储下标，2:当前点的最短路径长度
        ArrivePointInfo[0]=new ArrayList<Integer>();//   用来存储搜寻路径信息
        ArrivePointInfo[1]=new ArrayList<Integer>();
        ArrivePointInfo[2]=new ArrayList<Integer>();
        ArrayList<Integer> [] GrownPointInfo=new ArrayList[3];// 暂存拓展点的信息
        GrownPointInfo[0]=new ArrayList<Integer>();           //0:当前点，1:当前点的父代点的存储下标，2:当前点的延拓路径长度
        GrownPointInfo[1]=new ArrayList<Integer>();
        GrownPointInfo[2]=new ArrayList<Integer>();


        //初始化，即i点的加入
        ArrivePointInfo[0].add(i);
        ArrivePointInfo[1].add(0);
        ArrivePointInfo[2].add(0);

        int sign=1;//标示，1:有解，0:无解
        int arraivepoint=i;//当前已达点
        int kuochongi;
        int kuochongi2;
        while (arraivepoint!=j)
        {
            kuochongi=0;
            while (kuochongi<ArrivePointInfo[0].size())
            {
                kuochongi2=0;
                int growfpoint=ArrivePointInfo[0].get(kuochongi);
                while (kuochongi2<graphList[growfpoint].size())
                {
                    int growspoint=graphList[growfpoint].get(kuochongi2)[0];
                    int growweight=graphList[growfpoint].get(kuochongi2)[1]+ArrivePointInfo[2].get(kuochongi);
                    if (!ArrivePointInfo[0].contains(growspoint))
                    {
                        GrownPointInfo[0].add(growspoint);
                        GrownPointInfo[1].add(kuochongi);
                        GrownPointInfo[2].add(growweight);
                    }
                    kuochongi2++;
                }

                kuochongi++;
            }
            if (GrownPointInfo[0].size()==0)//判断有无解
            {
                sign=0;
                break;
            }
            int k=0;
            int ss=GrownPointInfo[2].get(k);
            for (int ii=1;ii<GrownPointInfo[0].size();ii++)
            {
                int mm=GrownPointInfo[2].get(ii);
                if (mm<ss)
                {
                    k=ii;
                    ss=mm;
                }
            }

            //  int k=GrownPointInfo[2].indexOf(Collections.min(GrownPointInfo[2]));

            //最小值标号
            int newpoint=GrownPointInfo[0].get(k);
            ArrivePointInfo[0].add(newpoint);
            ArrivePointInfo[1].add(GrownPointInfo[1].get(k));
            ArrivePointInfo[2].add(GrownPointInfo[2].get(k));
            arraivepoint=newpoint;


            GrownPointInfo[0].clear();
            GrownPointInfo[1].clear();
            GrownPointInfo[2].clear();

        }


        if (sign==0)
        {
            ArrayList<Integer>route=new ArrayList<Integer>();
            return route;
        }
        else
        {
            ArrayList<Integer>route=new ArrayList<Integer>();
            int m=ArrivePointInfo[0].size()-1;
            while(m!=0)
            {
                route.add(0,ArrivePointInfo[0].get(m));
                m=ArrivePointInfo[1].get(m);
            }
            route.add(0,i);
            return route;
        }
    }



    public  static boolean Connected(ArrayList<int[]>[]graphList,ArrayList<Integer>Route){
//        判断路径是否符合规范
        boolean flag=true;
        for(int i=0;i<Route.size()-1;i++){
            int point=Route.get(i);
            int nextPoint=Route.get(i+1);
            boolean pointflag=false;
            for(int j=0;j<graphList[point].size();j++){
                if(graphList[point].get(j)[0]==nextPoint) {
                    pointflag = true;
                    break;
                }

            }
            if(pointflag==false){
                flag=false;
                break;
            }
        }
        return flag;
    }

    public static int[] Sort(int []arr){
//        数组排序 从小到大
        int length=arr.length;
        for(int i=0;i<length-1;i++){
            for(int j=i+1;j<length;j++){
                if(arr[j]<arr[i]){
                    int temp=arr[i];
                    arr[i]=arr[j];
                    arr[j] =temp;
                }
            }
        }
        return arr;
    }
}

