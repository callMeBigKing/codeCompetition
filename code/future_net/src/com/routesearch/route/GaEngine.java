package com.routesearch.route;

import sun.misc.Sort;

import java.lang.reflect.Array;
import java.util.*;

/**
 * Created by imgos on 2016/3/11.
 */
public class GaEngine {

    private int Popsize; //种群规模
    private int crossRate;//交叉概率
    private int mutationRate;//变异概率
    private ArrayList bestRoute;//当前最短路径 route[0]存放begin route[end]存放end point  route[1-end]存中间节点
    private int []demand;//要求哦 route[0]存放begin route[end]存放end point  route[1-end]存中间节点
    private Set<Integer> demandSet;//中间点集合
    private int graph[][][];
    //用来存储图，每一行表示一个点可以到达的点和权值，里面存储的是二维数组 [x][0]表示点，[x][1]表示权值.
    ArrayList population[];//种群
    public GaEngine(int [][][] graph){
//        构造函数传入graph
        this.graph=graph.clone();
        for(int i=1;i<this.demand.length-1;i++){
            demandSet.add(this.demand[i]);
        }

    }

    private double CalculFit(ArrayList route){
//        计算一条路径的适应度  fit=1/totalLength+N N 表示经过的中间点个数
        double totalLength=0;//路径长度
        int midNum=0;
        for(int i=1;i<this.demand.length-1;i++){
            demandSet.add(this.demand[i]);
        }

        for(int i=0;i<route.size()-1;i++){
            int point=(Integer)route.get(i);
            int nextPoint=(Integer)route.get(i+1);
            for(int j=0;j<this.graph[point].length;i++){
                if(this.graph[point][j][0]==nextPoint) {
                    totalLength += this.graph[point][j][1];
                    break;
                }
            }
            if(demandSet.contains(point))midNum++;
        }
        double fit=1/totalLength+midNum;
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


    public ArrayList[] Crossover(int []parents) {
//      交叉函数
        ArrayList<Integer> father = new ArrayList<Integer>();
        ArrayList<Integer> mather = new ArrayList<Integer>();

        Set<Integer> fatherSet = new HashSet<Integer>();
        Set<Integer> matherSet = new HashSet<Integer>();
        Set<Integer> commSet = new HashSet<Integer>();

        for (int i = 0; i < father.size(); i++) {
            fatherSet.add(father.get(i));
        }
        for (int i = 0; i < mather.size(); i++) {
            fatherSet.add(mather.get(i));
        }

        commSet.clear();
        commSet.addAll(fatherSet);
        commSet.retainAll(matherSet);
//        取交集


        Random random = new Random();
        double rand = random.nextDouble();

        if (rand < this.crossRate) {
//            满足交叉条件
            if (commSet.size() != 0) {
                //有共同的点才能进行交叉
                //这个细节后期再进行调整
                int crossPoint = random.nextInt(commSet.size());//选择出来的共同点
                Iterator iterator = commSet.iterator();
                int location = 0;
                while (iterator.hasNext()) {
                    if (location == crossPoint) {
                        crossPoint = (Integer) iterator.next();
                        break;
                    } else iterator.next();
                    location++;
                }
                int point = father.indexOf(crossPoint);
                List<Integer> subFather1 = father.subList(0, crossPoint);
                List<Integer> subFather2 = father.subList(crossPoint, father.size());
                List<Integer> subMather1 = mather.subList(0, crossPoint);
                List<Integer> subMather2 = mather.subList(crossPoint, father.size());
                subFather1.addAll(subMather2);
                subMather1.addAll(subFather2);
                //拼接
                father=this.removeLoop((ArrayList<Integer>) subFather1);
                mather=this.removeLoop((ArrayList<Integer>) subMather1);

//                subFather1.addAll()
            }
        }
        ArrayList[] parentsRoute = new ArrayList[2];
        parentsRoute[0] = father;
        parentsRoute[1] = mather;
        return parentsRoute;
    }


    private ArrayList<Integer> removeLoop(ArrayList<Integer>route){
//        去除路径中的环
        for(int i=0;i<route.size()-1;i++){
            for(int j=route.size()-1;j>i;j--){
                if(route.get(i).equals(route.get(j))){
                    ArrayList<Integer>list1=(ArrayList<Integer>) route.subList(0,i);
                    ArrayList<Integer>list2=(ArrayList<Integer>) route.subList(j+1,route.size());
                    list1.addAll(list2);
                    route=list1;
                    break;
                }
            }
        }
        return route;
    }


    public int[] Filter(){
//        自然选择，选出father and mather 随机选取出6条路径选择出其中适应度大的两个返回他们在population 中的位置
        Random random=new Random();
        ArrayList <Integer>pointList=new ArrayList<Integer>();
        double []fit=new double[6];
        int []parents =new int[2];

        while (pointList.size()<6) {
            int point = random.nextInt(Popsize);
            if (!pointList.contains(point)) {
                pointList.add(point);
                fit[pointList.size() - 1] = this.CalculFit(this.population[point]);
            }
        }

        for(int i=0;i<2;i++){
            //选择排序，选择出前两个适应度最高的
            for(int j=2;j<fit.length;j++){
                if(fit[j]>fit[i]){
                    double tempFit=fit[i];
                    fit[i]=fit[j];
                    fit[j]=tempFit;

                    Integer tempPoint=pointList.get(i);
                    pointList.set(i,pointList.get(j));
                    pointList.set(j,tempPoint);

                }
            }
        }
        parents[0]=pointList.get(0);
        parents[1]=pointList.get(1);
        return parents;


    }

    public void Mutation(){
//        变异函数
    }

}
