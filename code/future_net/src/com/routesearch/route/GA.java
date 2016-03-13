package com.routesearch.route;

import java.util.ArrayList;

/**
 * Created by Administrator on 2016/3/13.
 */
public class GA {
    private int Popsize; //种群规模
    private int crossRate;//交叉概率
    private int mutationRate;//变异概率
    private ArrayList bestRoute;//当前最短路径 route[0]存放begin route[end]存放end point  route[1-end]存中间节点
    private int []demand;//要求 route[0]存放begin route[end]存放end point  route[1-end]存中间节点
    private int graph[][][];
    //用来存储图，每一行表示一个点可以到达的点和权值，里面存储的是二维数组 [x][0]表示点，[x][1]表示权值.
    ArrayList<Integer>population[];

    public GA(int [][][] graph){
//        构造函数传入graph
        this.graph=graph.clone();

    }

    public void  InitPop(){
//        初始化种群，随机产生一群从起点到终点的路径

    }

    private int[] CreateOneRoute(){
        //生成一条从起点到终点的路径

    }

    public void Crossover(){
//      交叉函数

    }

    public void Filter(){
//        自然选择，选出father and mather
    }
    public int[] dijstra(int i,int j)//迪杰斯特拉斯算法，
    {
        int i1,i2,i3;
        int routeji[]=new int[601];//第一个数为路径点个数， 后续为全路径点
        int spoint[]=new int[600];//已找到最短路径的点
        int frontpoint[][]=new int[3][600];  //   0 行为后点，1行为前点,2行为前点所在列的下标
        int routelength[][]=new int[2][600]// 0行为点，1行为最短路径长度
        int spointnum=1;  //找到最短路径的点的个数
        routelength[0][0]=i;//初始点i设置
        routelength[1][0]=0;
        frontpoint[0][0]=i;
        frontpoint[1][0]=i;
        spoint[0]=i;
        int i6=0;
        int a[][]=new int[3][600];    //拓展时用到的数组，0行为前点，1行为后点，2行为路径长度
        for(i1=0,i1<graph.length,i1++) //拓展次数
        {
            int k=0;
            int s=0;
            int i4=0;

            for (i2=0,i2<spointnum,i2++)//已达点
            {
                a[0][i4]=spoint[i2];
                for(i3=0,i3<graph[spoint[i2]][0].length,i3++)//已经达到的点的度
                {
                    a[1][i4]=graph[spoint[i2]][0][i3];

                    a[2][i4]=graph[spoint[i2]][1][i3]+routelength[1][i2];//计算路径长度
                    i4++;
                    int i5=0;
                    for (i5=0,i5<spointnum,i5++)// 如果所找的点是已经找到最短路径的点，
                                                // 则删去该点对应的值
                    {
                        if (spoint[i5]==a[1][i4])
                            i4--;
                    }
                }
            }
            if (i4==0)    //i4为0即为无法扩充得到新的点，即没有到达目标点的通路
            {
                break;//无通路
                return null;
            }
            s=a[2][0];
            for (i2=0,i2<i4,i2++) //在扩充的路径中找最短的
            {
                if (a[2][i2]<s)
                {
                    s=a[2][i2];
                    k=i2;
                }
            }
            spoint[spointnum]=a[1][k];//更新最短路径上的点之间的前后关系
            frontpoint[0][spointnum]=a[1][k];
            frontpoint[1][spointnum]=a[0][k];
            for (i6=0,i6<spointnum,i6++)  //找前点在spoint中的列标
            {
                if (spoint[i6]==a[0][k])
                {
                    frontpoint[2][spointnum]=i6;
                    break;
                }
            }
            routelength[0][spointnum]=a[1][k];//更新找到最短路径的点对应的路径长度
            routelength[1][spointnum]=a[2][k];
            spointnum++;

            if (a[1][k]==j)//如果新更的点为目标点，则路径寻找过程结束
                break;
        }

        i1=spointnum-1;
        i2=1;
        while(i1>0)//找最短路径
        {
            routeji[i2]=frontpoint[0][i1];
            i1=frontpoint[2][i1];
            i2++;
        }
        routeji[i2]=i;
        routeji[0]=i2;
        for (i1=1,i1<=i2/2,i1++)//换个方向
        {
            i3=routeji[i2+1-i1];
            routeji[i2+1-i1]=routeji[i1];
            routeji[i1]=i3;
        }
    return routeji;

    }

    public void Mutation()
    {
//        变异函数  必须要有中间节点
        //int Popsize=population.length;
        int Dpointnum=this.demand.length-2;//必经中间节点个数
        int i;
        int s;//变异点的前节点
        int t;//变异点的后节点
        for(i=0,i<Popsize,i+=1) //对每一个个体进行变异操作
        {
            int xi=(int)(Math.random()*(Dpointnum-1)); //随机找一个必经点
            int j,k;
            j=-1;
            int s=30;
            int sign=-1;

            for(j=0,j<graph[demand[xi+1]][0].length,j=j+1)// 对必经点的所有后续节点
            {

                for(k=0,k<population[i].size(),k++)  //对该基因表示的所有基因节点
                {
                    if(graph[demand[xi+1]][0][j]==population[i].get(k)
                            &graph[demand[xi+1]][1][j]<s)
                    {
                        s=graph[demand[xi+1]][1][j];
                        sign=k;
                    }
                }
            }
            if (sign>-0.5)
                j=sign;
            else
                j=(int)Math.random()*(population[i].size()-2)+1;

            s=population[i].get(j-1);                //j的前一个节点
            t=population[i].get(j+1);            //j的后一个节点


            //迪杰斯特拉斯算法找新路径
            int[]rout_s_j=this.dijstra(s,demand[xi+1]);
            int[]rout_j_t=this.dijstra(demand[xi+1],t);
            //路径拼接
            int m=rout_s_j[0];
            int n=rout_j_t[0];
            for (k=m+n-4,k>=1,k--)
            {
                int ss=population[i].size();
                population[i].set(ss-1+k,ss-1+k-(m+n-4));
            }
            for (k=3,k<=m+1,k++)
                population[i].set(j+k-3,rout_s_j[k]);
            for(k=m+2,k<=m+n-1)
                population[i].set(j+k-3,rout_j_t[k-m+1]);
        }

    }



}
