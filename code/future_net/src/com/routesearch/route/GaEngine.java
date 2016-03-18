package com.routesearch.route;

import com.sun.org.apache.bcel.internal.generic.GOTO;
import sun.misc.Sort;

import java.lang.reflect.Array;
import java.util.*;

/**
 * Created by imgos on 2016/3/11.
 */
public class GaEngine {

    private int Popsize=100; //种群规模
    private double crossRate=0.9;//交叉概率
    private double mutationRate=0.2;//变异概率
    private ArrayList bestRoute;//当前最短路径 route[0]存放begin route[end]存放end point  route[1-end]存中间节点
    private int []demand;//要求哦 route[0]存放begin route[end]存放end point  route[1-end]存中间节点
    private Set<Integer> demandSet;//中间点集合
    private int graph[][][];
    private ArrayList<int[]>[]graphList;
    //用来存储图，每一行表示一个点可以到达的点和权值，里面存储的是二维数组 [x][0]表示点，[x][1]表示权值.
    private ArrayList<Integer> population[];//种群
    public GaEngine(int [][][] graph,int[] demand,ArrayList<int[]>[]graphList){
//        构造函数传入graph
        this.graph=graph.clone();
        this.demand=demand;
        this.graphList=graphList;
        this.demandSet=new HashSet<Integer>();
        for(int i=0;i<this.demand.length-2;i++){
            demandSet.add(this.demand[i+1]);
        }

    }

    private double CalculFit(ArrayList route){
//        计算一条路径的适应度  fit=1/totalLength+N N 表示经过的中间点个数
        double totalLength=0;//路径长度
        int midNum=0;
//        for(int i=1;i<this.demand.length-1;i++){
//            demandSet.add(this.demand[i]);
//        }

        for(int i=0;i<route.size()-1;i++){
            int point=(Integer)route.get(i);
            int nextPoint=(Integer)route.get(i+1);
            for(int j=0;j<this.graph[point].length;j++){
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

        ArrayList<Integer>route=new ArrayList<Integer>();

        int startPoint=demand[0];
        int endPoint=demand[demand.length-1];
        while(true) {
            ArrayList<int[]>[]graphListClon=this.graphList.clone();
            route.add(startPoint);
            int pointer = startPoint;//当前所在节点
            while (pointer != endPoint) {
                int connectNum = graphListClon[pointer].size();
                if (connectNum == 0) break;//路走不通了就break
//            当前节点所连通边数
                Random random = new Random();
                int nextPoint = random.nextInt(connectNum);
//            生成[0-connectNum) 之间的整数
                if (!route.contains(graphListClon[pointer].get(nextPoint)[0])) {
                    route.add(graphListClon[pointer].get(nextPoint)[0]);
                    graphListClon[pointer].remove(nextPoint);//每次添加完成后都remove掉
                    pointer = this.graph[pointer][nextPoint][0];
                }
            }
            graphListClon=null;
            if(pointer==endPoint)
            {
                break;
            }
            else route.clear();

        }
        return route;
    }

    private ArrayList[] Crossover(int []parents) {
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

    public void Breed(){
//        繁殖函数包括整个选择和交叉过程
        ArrayList []nextPopulation=new ArrayList[this.Popsize];
        for(int i=0;i<this.Popsize;i+=2){
            int []parentsPoint=this.Filter();
            //选择出父母
            ArrayList<Integer>[] babbyRoute=this.Crossover(parentsPoint);
            //
            nextPopulation[i]=babbyRoute[0];
            if(i+1<this.Popsize) nextPopulation[i+1]=babbyRoute[1];
        }
        this.population=nextPopulation;
    }

    public void CalculFit(){
//        计算最短最大适应度和最短路径
        double bestFit=0;
        for(int i=0;i<this.Popsize;i++){
            ArrayList arrayList=this.population[i];
            double currentFit=this.CalculFit(arrayList);
            if(bestFit<currentFit){
                bestFit=currentFit;
                this.bestRoute=(ArrayList) population[i];
            }
        }
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

    private int[] Filter(){
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

    public ArrayList<Integer> getBestRoute(){
//        返回当前最佳路径
        return this.bestRoute;
    }

    private int[] dijstra(int i,int j)//迪杰斯特拉斯算法，
    {
        int i1,i2,i3;
        int routeji[]=new int[601];//第一个数为路径点个数， 后续为全路径点
        int spoint[]=new int[600];//已找到最短路径的点
        int frontpoint[][]=new int[3][600];  //   0 行为后点，1行为前点,2行为前点所在列的下标
        int routelength[][]=new int[2][600];// 0行为点，1行为最短路径长度
        int spointnum=1;  //找到最短路径的点的个数
        routelength[0][0]=i;//初始点i设置
        routelength[1][0]=0;
        frontpoint[0][0]=i;
        frontpoint[1][0]=i;
        spoint[0]=i;
        int i6=0;
        int a[][]=new int[3][600];    //拓展时用到的数组，0行为前点，1行为后点，2行为路径长度
        for(i1=0;i1<graph.length;i1++) //拓展次数
        {
            int k=0;
            int s=0;
            int i4=0;

            for (i2=0;i2<spointnum;i2++)//已达点
            {
                a[0][i4]=spoint[i2];
                for(i3=0;i3<graph[spoint[i2]][0].length;i3++)//已经达到的点的度
                {
                    a[1][i4]=graph[spoint[i2]][0][i3];

                    a[2][i4]=graph[spoint[i2]][1][i3]+routelength[1][i2];//计算路径长度
                    i4++;
                    int i5=0;
                    for (i5=0;i5<spointnum;i5++)// 如果所找的点是已经找到最短路径的点，
                    // 则删去该点对应的值
                    {
                        if (spoint[i5]==a[1][i4])
                            i4--;
                    }
                }
            }
            if (i4==0)    //i4为0即为无法扩充得到新的点，即没有到达目标点的通路
            {
                routeji[0]=1000;
                return routeji;
                //break;//无通路
            }
            s=a[2][0];
            for (i2=0;i2<i4;i2++) //在扩充的路径中找最短的
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
            for (i6=0;i6<spointnum;i6++)  //找前点在spoint中的列标
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
        for (i1=1;i1<=i2/2;i1++)//换个方向
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
        if (this.demand.length-2==0)
            return;
        //int Popsize=population.length;
        int Dpointnum=this.demand.length-2;//必经中间节点个数
        int i;
        int s;//变异点的前节点
        int t;//变异点的后节点
        for(i=0;i<Popsize;i+=1) //对每一个个体进行变异操作
        {
            if (Math.random()>this.mutationRate) {
                continue;
            }
            int xi=(int)(Math.random()*(Dpointnum)); //随机找一个必经点
            int j,k;
            j=-1;
            s=30;//路径长度小于30
            int sign=-1;

            for(j=0;j<graph[demand[xi+1]][0].length;j=j+1)// 对必经点的所有后续节点
            {

                for(k=0;k<population[i].size();k++)  //对该基因表示的所有基因节点
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
            if (m>900|n>900)//  找不到拼接路径的话，则跳过该条的变异
                continue;
            int ss=population[i].size();
            for (k=m+n-4;k>=1;k--)
            {

                population[i].set(ss-1+k,population[i].get(ss-1+k-(m+n-4)));
            }
            for (k=2;k<=m;k++)
                population[i].set(j+k-2,rout_s_j[k]);
            for(k=m+1;k<=m+n-2;k++)
                population[i].set(j+k-2,rout_j_t[k-m+1]);
            population[i]=this.removeLoop(population[i]);//去环
        }

    }

}
