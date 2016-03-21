package com.routesearch.route;

import java.util.*;

/**
 * Created by imgos on 2016/3/20.
 */
public class GAAA {
    //    ga
    private int totalWeight;
    //    权重
    private int Popsize = 500; //种群规模
    private double crossRate = 0.9;//交叉概率
    private double mutationRate = 0.5;//变异概率
    private ArrayList bestRoute;//当前最短路径 route[0]存放begin route[end]存放end point  route[1-end]存中间节点
    private int[] demand;//要求哦 route[0]存放begin route[end]存放end point  route[1-end]存中间节点
    private Set<Integer> demandSet;//中间点集合 不包括起点和终点
    private ArrayList<int[]>[] graphList;
    //用来存储图，每一行表示一个点可以到达的点和权值，里面存储的是二维数组 [x][0]表示点，[x][1]表示权值.
    private ArrayList<Integer> population[];//种群
    private int pointNum;
//    图中点的个数
    private int edgeNum;
//    图中边的个数
    public GAAA(ArrayList<int[]>[] graphList, int[] demand) {
//        构造函数传入graph
        this.demand = demand;
        this.graphList = graphList;
        this.demandSet = new HashSet<Integer>();
        for (int i = 0; i < this.demand.length - 2; i++) {
            demandSet.add(this.demand[i + 1]);
        }
    }

    public void setMsg(int pointNum,int edgeNum){
//        设置图中边和点的个数
        this.pointNum=pointNum;
        this.edgeNum=edgeNum;
    }

    private double CalculFit(ArrayList<Integer> route) {
//        计算一条路径的适应度  fit=1/totalLength+N N 表示经过的中间点个数
        double totalLength = 0;//路径长度
        int midNum = 0;
        for (int i = 0; i < route.size() - 1; i++) {
//            求i 到i+1之间的距离
            int point = route.get(i);
            int nextPoint = route.get(i + 1);
            for (int j = 0; j < this.graphList[point].size(); j++) {
                if (this.graphList[point].get(j)[0] == nextPoint) {
                    totalLength += this.graphList[point].get(j)[1];
                    break;
                }
            }
            if (demandSet.contains(point)) midNum++;
        }
        double fit = 0;
        if (totalLength != 0) {

            fit = 1 / totalLength + midNum;
        }
        return fit;
    }

    private double CalculCost(ArrayList<Integer> route) {
//        计算权重
        double fit = CalculFit(route);
        int midNum = (int) fit;
        double cost = 1 / (fit - midNum);
        return cost;
    }

    public void InitPop() {
//        初始化种群，随机产生一群从起点到终点的路径
        population = new ArrayList[this.Popsize];
        for (int i = 0; i < this.Popsize; i++) {
            population[i] = this.CreateOneRoute();
        }
    }

    public ArrayList CreateOneRoute() {
        //生成一条从起点到终点的路径dd
        ArrayList<Integer> route = new ArrayList<Integer>();

        int startPoint = demand[0];
        int endPoint = demand[demand.length - 1];
        while (true) {
            ArrayList<int[]>[] graphListClon = Tool.CloneGraph(this.graphList);

            route.add(startPoint);
            graphListClon = Tool.RemovePoint(graphListClon, startPoint);
//          删除所有能够到达起点的路径
            int pointer = startPoint;//当前所在节点
            while (pointer != endPoint) {
                int connectNum = graphListClon[pointer].size();
                if (connectNum == 0) break;//路走不通了就break
//            当前节点所连通边数
                Random random = new Random();
                int nextPointIndex = random.nextInt(connectNum);//注意这里的nextPointIndex只是索引不是具体的点
//            生成[0-connectNum) 之间的整数
                int nextPoint = graphListClon[pointer].get(nextPointIndex)[0];
                if (!route.contains(nextPoint)) {
                    route.add(nextPoint);
                    graphListClon = Tool.RemovePoint(graphListClon, nextPoint);
                    //每次添加完成后都remove掉所有能够到该点的路径
                    pointer = nextPoint;
                }

            }
            graphListClon = null;
            if (pointer == endPoint) {
                break;
            } else route.clear();
        }
        return route;
    }

    public boolean JugeConnect() {
//        判断起点和终点是否连通
        int startPoint = this.demand[0];
        int endPoint = this.demand[demand.length - 1];
        if (Tool.dijstra(this.graphList,startPoint, endPoint).size()==0) return false;
        else return true;
    }

    private ArrayList[] Crossover(int[] parents) {

//        另外一种交叉规范
        ArrayList<Integer> father = (ArrayList<Integer>) this.population[parents[0]].clone();
        ArrayList<Integer> mather = (ArrayList<Integer>) this.population[parents[1]].clone();
        ArrayList<Integer> babby1 = new ArrayList<Integer>();
        ArrayList<Integer> babby2 = new ArrayList<Integer>();
        Random random = new Random();
        int fPointIndex = random.nextInt(father.size() - 2) + 1;
//        生成一个【1~~size-1） 的点  father的交叉点
        int mPointIndex = random.nextInt(mather.size() - 2) + 1;
//        同上
        int fPoint = father.get(fPointIndex);
        int fPointNext = father.get(fPointIndex + 1);
        int mPoint = mather.get(mPointIndex);
        int mPointNext = mather.get(mPointIndex + 1);
//        判断这几个点是不是连通的，不是的话就用dj 得到一条最短路径
        boolean fFlag = false;  //判断父亲sub1和母亲sub2是否连通
        boolean mFlag = false;
        for (int i = 0; i < graphList[fPoint].size(); i++) {
            if (this.graphList[fPoint].get(i)[0] == mPointNext) {
                fFlag = true;
                break;
            }
        }

        for (int i = 0; i < graphList[mPoint].size(); i++) {
            if (this.graphList[mPoint].get(i)[0] == fPointNext) {
                mFlag = true;
                break;
            }
        }


        ArrayList<Integer> subFather1 = Tool.ListClone(father, 0, fPointIndex + 1);
//            前面的默认设置是subFather1保留fPointIndex，subMather1保留mPointIndex
        ArrayList<Integer> subMather2 = Tool.ListClone(mather, mPointIndex + 1, mather.size());
        subFather1.addAll(subMather2);
        if (!fFlag) {
//           不能够直接连接起来 寻找一条最短路径
            ArrayList<Integer> route = Tool.dijstra(this.graphList,fPoint, mPointNext);

            for (int i = 0; i < route.size() - 2; i++) {
                subFather1.add(i + 1 + fPointIndex, route.get(i + 1));
            }
        }


        ArrayList<Integer> subMather1 = Tool.ListClone(mather, 0, mPointIndex + 1);
//            前面的默认设置是subFather1保留fPointIndex，subMather1保留mPointIndex
        ArrayList<Integer> subFather2 = Tool.ListClone(father, fPointIndex + 1, father.size());
        subMather1.addAll(subFather2);

        if (!mFlag) {
//            不能够连接起来
            ArrayList<Integer> route = Tool.dijstra(this.graphList,mPoint, fPointNext);

            for (int i = 0; i < route.size() - 2; i++) {
                subMather1.add(i + 1 + mPointIndex, route.get(i + 1));
            }

        }

        babby1 = this.removeLoop(subFather1);
        babby2 = this.removeLoop(subMather1);

        return new ArrayList[]{babby1, babby2};

    }

    public void Breed() {
//        繁殖函数包括整个选择和交叉过程

        ArrayList[] nextPopulation = new ArrayList[this.Popsize];
        this.CalculFit();
        System.out.println(Tool.Connected(graphList,bestRoute)+" 符合规范");
        System.out.print(this.CalculFit(this.getBestRoute()));
        nextPopulation[0] = this.getBestRoute();
//      循环从1开始把上一代最好的个体留下来
        for (int i = 1; i < this.Popsize; i += 2) {
            int[] parentsPoint = this.Filter();
            //选择出父母
            ArrayList<Integer>[] babbyRoute = this.Crossover(parentsPoint);
            //
            nextPopulation[i] = babbyRoute[0];
            if (i + 1 < this.Popsize) nextPopulation[i + 1] = babbyRoute[1];
//            判断一下防止种群数是奇数
        }
        this.population = nextPopulation;
    }

    public void CalculFit() {
//        计算最短最大适应度和最短路径
        double bestFit = 0;

        for (int i = 0; i < this.Popsize; i++) {
            ArrayList arrayList = this.population[i];
            double currentFit = this.CalculFit(arrayList);
            if (bestFit < currentFit) {
                bestFit = currentFit;
                this.bestRoute = this.population[i];
            }
        }

    }

    private ArrayList<Integer> removeLoop(ArrayList<Integer> route) {
//        去除路径中的环
        for (int i = 0; i < route.size() - 1; i++) {
            for (int j = route.size() - 1; j > i; j--) {
                if (route.get(i).equals(route.get(j))) {
                    ArrayList<Integer> list1 = Tool.ListClone(route, 0, i);  //
                    ArrayList<Integer> list2 = Tool.ListClone(route, j, route.size());
                    list1.addAll(list2);
                    route = list1;
                    break;
                }
            }
        }
        return route;
    }

    private int[] Filter() {
//        自然选择，选出father and mather 随机选取出6条路径选择出其中适应度大的两个返回他们在population 中的位置
        Random random = new Random();
        ArrayList<Integer> pointList = new ArrayList<Integer>();
        double[] fit = new double[6];
        int[] parents = new int[2];

        while (pointList.size() < 6) {
            int point = random.nextInt(Popsize);
            if (!pointList.contains(point)) {
                pointList.add(point);
                fit[pointList.size() - 1] = this.CalculFit(this.population[point]);
            }
        }

        for (int i = 0; i < 2; i++) {
            //选择排序，选择出前两个适应度最高的
            for (int j = 2; j < fit.length; j++) {
                if (fit[j] > fit[i]) {
                    double tempFit = fit[i];
                    fit[i] = fit[j];
                    fit[j] = tempFit;

                    Integer tempPoint = pointList.get(i);
                    pointList.set(i, pointList.get(j));
                    pointList.set(j, tempPoint);

                }
            }
        }
        parents[0] = pointList.get(0);
        parents[1] = pointList.get(1);
        return parents;


    }

    public ArrayList<Integer> getBestRoute() {
//        返回当前最佳路径
        return this.bestRoute;
    }

    public double getCost() {
        double cost = this.CalculCost(this.bestRoute);
        return cost;
    }

    public boolean JugeDemand() {
//        判断最后的路径是否满足demand
        Iterator<Integer> demand = this.demandSet.iterator();
        boolean flage = true;
        while (demand.hasNext()) {
            if (!this.bestRoute.contains(demand.next())) {
                flage = false;
                break;
            }
        }
        return flage;
    }

    public void Mutation() {
//        this.CalculFit();
//        System.out.println(this.CalculFit(this.getBestRoute()));
        //  变异函数
        //  无中间节点，则不变异
        //  若有未达的必经点，则用这些点来变异
        //  若必经点都经过，则随机变异
        //  若所选必经点与该个体不连通，则随机选取个体上的点来变异
//test
//        ArrayList<int[]>[] newGrah=this.ReShapeGraph(population[1],-1,-1);
//        ArrayList<Integer> rout_s_d =Tool.dijstra(newGrah,3, 9);
//        for (int i=0;i<rout_s_d.size();i++)
//        {
//            System.out.print(" "+rout_s_d.get(i));
//       }
//test

        if (this.demand.length - 2 == 0)
            return;
        int Dpointnum = this.demand.length - 2;//必经中间节点个数
        int i;//循环变量
        int j = -1;//变异点
        int s;//变异点的前节点
        int t;//变异点的后节点
        //最大适应度点不变异


        for (i = 1; i < Popsize; i += 1) //对每一个个体进行变异操作
        {
            if (Math.random() > this.mutationRate | population[i].size() < 3) {
                continue;
            }
            int xi = (int) (Math.random() * (Dpointnum)); //随机找一个必经点
            int suiji;
            if (CalculFit(population[i]) < Dpointnum)//添加没有的点
            {
                for (suiji = 0; suiji < Dpointnum; suiji++) {
                    if (population[i].contains(this.demand[xi + 1])) {
                        xi = (xi + 1) % Dpointnum;
                    }
                }
            }


            //添加没有的点
            int dtopi;
            int dtopj;
            int ptodi;
            int ptodj;
            // find t
            int weightdtoj = 100;//初始权值设为100
            for (dtopi = 0; dtopi < graphList[demand[xi + 1]].size(); dtopi++) {
                for (dtopj = 0; dtopj < population[i].size(); dtopj++) {
                    if (graphList[demand[xi + 1]].get(dtopi)[0] == population[i].get(dtopj) & graphList[demand[xi + 1]].get(dtopi)[1] < weightdtoj) {
                        j = dtopj;
                        break;
                    }
                }
            }
            if (j <= 0 | j >= population[i].size() - 1) {
                j = (int) (Math.random() * (population[i].size()-2))+1;
            }
            s = population[i].get(j - 1);
            t = population[i].get(j + 1);
            ArrayList<int[]>[] newGrah=this.ReShapeGraph(population[i],s,t);
            ArrayList<Integer> rout_s_d =Tool.dijstra(newGrah,s, demand[xi + 1]);
            ArrayList<Integer> rout_d_t = Tool.dijstra(newGrah,demand[xi + 1], t);
            if (rout_d_t.size()==0|rout_s_d.size()==0)
                continue;
            population[i].set(j, demand[xi + 1]);

            rout_d_t.remove(0);
            if (rout_d_t.size()>1)
            rout_d_t.remove(rout_d_t.size()-1);
            rout_s_d.remove(0);
            if (rout_s_d.size()>1)
            rout_s_d.remove(rout_s_d.size()-1);
            population[i].addAll(j+1,rout_d_t);
            population[i].addAll(j,rout_s_d);

            population[i]=removeLoop(population[i]);

        }

    }

    private ArrayList<int[]>[] ReShapeGraph(ArrayList<Integer> route, int fPoint, int bPoint) {
//        在图中去除经过route 中点的变
        int pointNum = this.graphList.length;
        ArrayList<int[]>[] newGraph = new ArrayList[pointNum];
        for (int i = 0; i < pointNum; i++) {
            newGraph[i] = new ArrayList<int[]>();
            if (!(route.contains(i) && i != fPoint && i != bPoint)) {
                int iLength = this.graphList[i].size();
                for (int j = 0; j < iLength; j++) {
                    newGraph[i].add(this.graphList[i].get(j).clone());
                }
            }
        }
        return newGraph;
    }


    public int CalculWeight(ArrayList route) {
//        计算权重  fit=1/totalLength+N N 表示经过的中间点个数
        int totalLength = 0;//路径长度
        for (int i = 0; i < route.size() - 1; i++) {
            int point = (Integer) route.get(i);
            int nextPoint = (Integer) route.get(i + 1);
            for (int j = 0; j < this.graphList[point].size(); j++) {
                if (this.graphList[point].get(j)[0] == nextPoint) {
                    totalLength += this.graphList[point].get(j)[1];
                    break;
                }
            }

        }

        return totalLength;
    }
}
