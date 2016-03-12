package com.routesearch.route;

/**
 * Created by imgos on 2016/3/11.
 */
public class GaEngine {
    int Popsize; //种群规模
    int crossRate;//交叉概率
    int mutationRate;//变异概率
    int []route;//route[0]存放begin route[end]存放end point  route[1-end]存中间节点


//    图用稀疏矩阵存储

    public void  InitPop(){
//        初始化种群，随机产生一群从起点到终点的路径
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
