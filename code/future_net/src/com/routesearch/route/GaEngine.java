package com.routesearch.route;

import java.util.ArrayList;
import java.util.Queue;
import java.util.Random;

/**
 * Created by imgos on 2016/3/11.
 */
public class GaEngine {

    private int Popsize; //种群规模
    private int crossRate;//交叉概率
    private int mutationRate;//变异概率
    private ArrayList bestRoute;//当前最短路径 route[0]存放begin route[end]存放end point  route[1-end]存中间节点
    private int []demand;//要求哦 route[0]存放begin route[end]存放end point  route[1-end]存中间节点
    private int graph[][][];
    //用来存储图，每一行表示一个点可以到达的点和权值，里面存储的是二维数组 [x][0]表示点，[x][1]表示权值.
    ArrayList population[];
    public GaEngine(int [][][] graph){
//        构造函数传入graph
        this.graph=graph.clone();

    }

    private double CalculFit(ArrayList route){
//        计算一条路径的适应度  fit=1/totalLength+N N 表示经过的中间点个数
        double fit=0;
        double totalLength=0;//路径长度
        for(int i=0;i<route.size()-1;i++){
            //totalLength+=graph[route.get(i)][][]

        }
        return fit;
    }

    public void  InitPop(){
//        初始化种群，随机产生一群从起点到终点的路径
        population=new ArrayList[this.Popsize];
        for(int i=0;i<this.Popsize;i++){
            population[i]=this.CreateOneRoute();
        }

    }

    private ArrayList CreateOneRoute(){
        //生成一条从起点到终点的路径
        ArrayList route=new ArrayList();
        int startPoint=demand[0];
        int endPoint=demand[demand.length-1];
        route.add(startPoint);

        int pointer=startPoint;//当前所在节点
        while(pointer!=endPoint){
            int connectNum=this.graph[pointer].length;
//            当前节点所连通边数
            Random random=new Random();
            int nextPoint= random.nextInt(connectNum);
//            生成[0-connectNum) 之间的整数
            route.add(this.graph[pointer][nextPoint][0]);
            pointer=this.graph[pointer][nextPoint][0];
        }
        return route;
    }

    public void Crossover(){
//      交叉函数

    }

    public void Filter(){
//        自然选择，选出father and mather

    }

    public void Mutation(){
//        变异函数
    }

}
