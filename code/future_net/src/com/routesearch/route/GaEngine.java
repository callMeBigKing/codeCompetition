package com.routesearch.route;

import java.util.ArrayList;
import java.util.Queue;

/**
 * Created by imgos on 2016/3/11.
 */
public class GaEngine {

    private int Popsize; //种群规模
    private int crossRate;//交叉概率
    private int mutationRate;//变异概率
    private int []bestRoute;//当前最短路径 route[0]存放begin route[end]存放end point  route[1-end]存中间节点
    private int graph[][][];
    //用来存储图，每一行表示一个点可以到达的点和权值，里面存储的是二维数组 [x][0]表示点，[x][1]表示权值.

    public GaEngine(int [][][] graph){
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

    public void Mutation(){
//        变异函数
    }

}
