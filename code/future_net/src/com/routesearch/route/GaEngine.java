package com.routesearch.route;

import com.filetool.util.LogUtil;
import sun.misc.Sort;

import java.lang.reflect.Array;
import java.util.*;

/**
 * Created by imgos on 2016/3/11.
 */
public class GaEngine {
    private int totalWeight;
    //    权重
    private int Popsize = 500; //种群规模
    private double crossRate = 0.9;//交叉概率
    private double mutationRate = 0.3;//变异概率
    private ArrayList bestRoute;//当前最短路径 route[0]存放begin route[end]存放end point  route[1-end]存中间节点
    private int[] demand;//要求哦 route[0]存放begin route[end]存放end point  route[1-end]存中间节点
    private Set<Integer> demandSet;//中间点集合
    private int graph[][][];
    private ArrayList<int[]>[] graphList;
    //用来存储图，每一行表示一个点可以到达的点和权值，里面存储的是二维数组 [x][0]表示点，[x][1]表示权值.
    private ArrayList<Integer> population[];//种群

    //    经过必经点的个数
    public GaEngine(int[][][] graph, int[] demand, ArrayList<int[]>[] graphList) {
//        构造函数传入graph
        this.graph = graph.clone();
        this.demand = demand;
        this.graphList = graphList;
        this.demandSet = new HashSet<Integer>();
        for (int i = 0; i < this.demand.length - 2; i++) {
            demandSet.add(this.demand[i + 1]);
        }

    }

    private double CalculFit(ArrayList route) {
//        计算一条路径的适应度  fit=1/totalLength+N N 表示经过的中间点个数
        double totalLength = 0;//路径长度
        int midNum = 0;
        for (int i = 0; i < route.size() - 1; i++) {
            int point = (Integer) route.get(i);
            int nextPoint = (Integer) route.get(i + 1);
            for (int j = 0; j < this.graph[point].length; j++) {
                if (this.graph[point][j][0] == nextPoint) {
                    totalLength += this.graph[point][j][1];
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

    private int CalculWeight(ArrayList route) {
//        计算权重  fit=1/totalLength+N N 表示经过的中间点个数
        int totalLength = 0;//路径长度
        for (int i = 0; i < route.size() - 1; i++) {
            int point = (Integer) route.get(i);
            int nextPoint = (Integer) route.get(i + 1);
            for (int j = 0; j < this.graph[point].length; j++) {
                if (this.graph[point][j][0] == nextPoint) {
                    totalLength += this.graph[point][j][1];
                    break;
                }
            }

        }

        return totalLength;
    }

    public void InitPop() {
//        初始化种群，随机产生一群从起点到终点的路径


        population = new ArrayList[this.Popsize];
        for (int i = 0; i < this.Popsize; i++) {
            population[i] = this.CreateOneRoute();

        }

    }

    public boolean JugeConnect() {
//        判断起点和终点是否连通
        int startPoint = this.demand[0];
        int endPoint = this.demand[demand.length - 1];
        if (this.dijstra(startPoint, endPoint)[0] > 499) return false;
        else return true;
    }

    public ArrayList CreateOneRoute() {
        //生成一条从起点到终点的路径

        ArrayList<Integer> route = new ArrayList<Integer>();

        int startPoint = demand[0];
        int endPoint = demand[demand.length - 1];
        while (true) {
            ArrayList<int[]>[] graphListClon = this.CloneGraph(this.graphList);

            route.add(startPoint);
            graphListClon = this.RemovePoint(graphListClon, startPoint);
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
                    graphListClon = this.RemovePoint(graphListClon, nextPoint);
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

    private ArrayList<int[]>[] CloneGraph(ArrayList<int[]>[] graphList) {
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

    private ArrayList<Integer> ListClone(ArrayList<Integer> list, int start, int end) {
//        sublist 不具有clone 效果需要手动进行clone
        ArrayList listClone = new ArrayList(end - start);
        for (int i = start; i < end; i++) {
            listClone.add(list.get(i));
        }
        return listClone;
    }

    private ArrayList<int[]>[] RemovePoint(ArrayList<int[]>[] graphListClon, int point) {
//        remove掉已经选择的点
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

    private ArrayList[] Crossover2(int[] parents) {

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
        boolean fFlag=false;  //判断父亲sub1和母亲sub2是否连通
        boolean mFlag=false;
        for (int i = 0; i < graphList[fPoint].size(); i++) {
            if (this.graphList[fPoint].get(i)[0] == mPointNext) {
                fFlag=true;
                break;
            }
        }

        for (int i = 0; i < graphList[mPoint].size(); i++) {
            if (this.graphList[mPoint].get(i)[0] == fPointNext) {
                mFlag=true;
                break;
            }
        }


        ArrayList<Integer> subFather1 = this.ListClone(father, 0, fPointIndex+1);
//            前面的默认设置是subFather1保留fPointIndex，subMather1保留mPointIndex
        ArrayList<Integer> subMather2 = this.ListClone(mather, mPointIndex+1, mather.size());
        subFather1.addAll(subMather2);
        if(!fFlag){
//           不能够直接连接起来 寻找一条最短路径
            ArrayList<Integer> route= this.dijstraList(fPoint,mPointNext);

            for(int i=0;i<route.size()-2;i++){
                subFather1.add(i+1+fPointIndex,route.get(i+1));
            }
        }


        ArrayList<Integer> subMather1 = this.ListClone(mather, 0, mPointIndex+1);
//            前面的默认设置是subFather1保留fPointIndex，subMather1保留mPointIndex
        ArrayList<Integer> subFather2 = this.ListClone(father, fPointIndex+1, father.size());
        subMather1.addAll(subFather2);

        if(!mFlag){
//            不能够连接起来
            ArrayList<Integer> route= this.dijstraList(mPoint,fPointNext);

            for(int i=0;i<route.size()-2;i++){
                subMather1.add(i+1+mPointIndex,route.get(i+1));
            }

        }

        babby1 = this.removeLoop(subFather1);
        babby2 = this.removeLoop(subMather1);

        return new ArrayList[]{babby1,babby2} ;


    }

    private ArrayList[] Crossover(int[] parents) {
//      交叉函数
        ArrayList<Integer> father = (ArrayList<Integer>) this.population[parents[0]].clone();
        ArrayList<Integer> mather = (ArrayList<Integer>) this.population[parents[1]].clone();
        ArrayList<Integer> babby1 = new ArrayList<Integer>();
        ArrayList<Integer> babby2 = new ArrayList<Integer>();
        Set<Integer> fatherSet = new HashSet<Integer>();
        Set<Integer> matherSet = new HashSet<Integer>();
        Set<Integer> commSet = new HashSet<Integer>();

        for (int i = 1; i < father.size() - 1; i++) {
            fatherSet.add(father.get(i));
        }
        for (int i = 1; i < mather.size() - 1; i++) {
            matherSet.add(mather.get(i));
        }
//        共同的中间节点不能够包括起点和终点。

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
//              crossPoint是进行交叉的点
                int crossPointIndexF = father.indexOf(crossPoint);
                int crossPointIndexM = mather.indexOf(crossPoint);
//                mather and father 的交叉点的索引

                ArrayList<Integer> subFather1 = this.ListClone(father, 0, crossPointIndexF);
                ArrayList<Integer> subFather2 = this.ListClone(father, crossPointIndexF, father.size());
                ArrayList<Integer> subMather1 = this.ListClone(mather, 0, crossPointIndexM);
                ArrayList<Integer> subMather2 = this.ListClone(mather, crossPointIndexM, mather.size());
//              这里需要注意sublist返回的是一个视图，如果对原来的list做结构性改变那么 分出来的子list sublist 也将发生变化
                subFather1.addAll(subMather2);
                subMather1.addAll(subFather2);

                //拼接
                babby1 = this.removeLoop(subFather1);
                babby2 = this.removeLoop(subMather1);
//                father = this.removeLoop(subFather1);
//                mather = this.removeLoop(subMather1);

//                subFather1.addAll()
            }

        }
//        父代参与到子代的竞争中去

        ArrayList<Integer>[] competitions = new ArrayList[4];
        competitions[0] = babby1;
        competitions[1] = babby2;
        competitions[2] = father;
        competitions[3] = mather;
//
//
        ArrayList[] parentsRoute = new ArrayList[2];
        competitions = this.RouteCompetition(competitions);
        parentsRoute[0] = competitions[0];
        parentsRoute[1] = competitions[1];
//        parentsRoute[0]=father;
//        parentsRoute[1]=mather;
        return parentsRoute;
    }

    private ArrayList<Integer>[] RouteCompetition(ArrayList<Integer>[] competitions) {
//        多条路径排序
        int length = competitions.length;
        double[] fit = new double[length];
        for (int i = 0; i < length; i++) {
            fit[i] = this.CalculFit(competitions[i]);
        }
        for (int i = 0; i < length - 1; i++) {
            for (int j = i + 1; j < length; j++) {
                if (fit[i] < fit[j]) {
//                    分别交换route 和fit的位置
                    double fitTemp = fit[i];
                    fit[i] = fit[j];
                    fit[j] = fitTemp;
                    ArrayList<Integer> routeFit = competitions[i];
                    competitions[i] = competitions[j];
                    competitions[j] = routeFit;
                }
            }
        }
        return competitions;
    }


    public void Breed() {
//        繁殖函数包括整个选择和交叉过程

        ArrayList[] nextPopulation = new ArrayList[this.Popsize];
        this.CalculFit();
        System.out.print(this.CalculFit(this.getBestRoute()));
        nextPopulation[0] = this.getBestRoute();
//      循环从1开始把上一代最好的个体留下来
        for (int i = 1; i < this.Popsize; i += 2) {
            int[] parentsPoint = this.Filter();
            //选择出父母
            ArrayList<Integer>[] babbyRoute = this.Crossover2(parentsPoint);
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
                    ArrayList<Integer> list1 = this.ListClone(route, 0, i);  //
                    ArrayList<Integer> list2 = this.ListClone(route, j, route.size());
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

    public int getTotalWeight() {
        totalWeight = this.CalculWeight(this.bestRoute);
        return this.totalWeight;
    }

    public int[] dijstra(int i, int j)//迪杰斯特拉斯算法，
    {
        int i1, i2, i3;
        int routeji[] = new int[601];//第一个数为路径点个数， 后续为全路径点
        int spoint[] = new int[600];//已找到最短路径的点
        int frontpoint[][] = new int[3][600];  //   0 行为后点，1行为前点,2行为前点所在列的下标
        int routelength[][] = new int[2][600];// 0行为点，1行为最短路径长度
        int spointnum = 1;  //找到最短路径的点的个数
        routelength[0][0] = i;//初始点i设置
        routelength[1][0] = 0;
        frontpoint[0][0] = i;
        frontpoint[1][0] = i;
        frontpoint[2][0] = 0;
        spoint[0] = i;
        int i6 = 0;
        int a[][] = new int[3][600];    //拓展时用到的数组，0行为前点，1行为后点，2行为路径长度

        int k = 0;
        int s = 0;
        int i4 = 0;
        for (i1 = 0; i1 < graph.length - 1; i1++) //拓展次数
        {
            k = 0;
            s = 0;
            i4 = 0;

            for (i2 = 0; i2 < spointnum; i2++)//已达点
            {

                for (i3 = 0; i3 < graph[spoint[i2]].length; i3++)//已经达到的点的度
                {
                    a[0][i4] = spoint[i2];
                    a[1][i4] = graph[spoint[i2]][i3][0];


                    a[2][i4] = graph[spoint[i2]][i3][1] + routelength[1][i2];//计算路径长度
                    //System.out.println(i4);
                    i4++;
                    int i5 = 0;
                    for (i5 = 0; i5 < spointnum; i5++)// 如果所找的点是已经找到最短路径的点，
                    // 则删去该点对应的值
                    {

                        if (spoint[i5] == a[1][i4 - 1]) {
                            i4--;
                            break;
                        }

                    }
                }
            }

            if (i4 == 0)    //i4为0即为无法扩充得到新的点，即没有到达目标点的通路
            {
                routeji[0] = 1000;
                return routeji;
                //break;//无通路
            }
            s = a[2][0];
            k = 0;
            for (i2 = 0; i2 < i4; i2++) //在扩充的路径中找最短的
            {
                if (a[2][i2] < s) {
                    s = a[2][i2];
                    k = i2;
                }
            }

            spoint[spointnum] = a[1][k];//更新最短路径上的点之间的前后关系
            frontpoint[0][spointnum] = a[1][k];
            frontpoint[1][spointnum] = a[0][k];
            for (i6 = 0; i6 < spointnum; i6++)  //找前点在spoint中的列标
            {
                if (frontpoint[0][i6] == frontpoint[1][spointnum]) {
                    frontpoint[2][spointnum] = i6;
                    break;
                }
            }
            routelength[0][spointnum] = a[1][k];//更新找到最短路径的点对应的路径长度
            routelength[1][spointnum] = a[2][k];
            spointnum++;

            if (frontpoint[0][spointnum - 1] == j)//如果新更的点为目标点，则路径寻找过程结束
                break;
        }

        if (i1 == graph.length - 1) {//无通路
            routeji[0] = 1001;
            return routeji;
        }

        i1 = spointnum - 1;
        i2 = 1;
        while (i1 > 0)//找最短路径
        {
            routeji[i2] = frontpoint[0][i1];
            i1 = frontpoint[2][i1];
            i2++;
        }
        routeji[i2] = i;
        routeji[0] = i2;
        for (i1 = 1; i1 <= i2 / 2; i1++)//换个方向
        {
            i3 = routeji[i2 + 1 - i1];
            routeji[i2 + 1 - i1] = routeji[i1];
            routeji[i1] = i3;
        }
        return routeji;

    }

    public ArrayList<Integer> dijstraList(int i, int j)//迪杰斯特拉斯算法，
    {
        int i1, i2, i3;
        int routeji[] = new int[601];//第一个数为路径点个数， 后续为全路径点
        int spoint[] = new int[600];//已找到最短路径的点
        int frontpoint[][] = new int[3][600];  //   0 行为后点，1行为前点,2行为前点所在列的下标
        int routelength[][] = new int[2][600];// 0行为点，1行为最短路径长度
        int spointnum = 1;  //找到最短路径的点的个数
        routelength[0][0] = i;//初始点i设置
        routelength[1][0] = 0;
        frontpoint[0][0] = i;
        frontpoint[1][0] = i;
        frontpoint[2][0] = 0;
        spoint[0] = i;
        int i6 = 0;
        int a[][] = new int[3][600];    //拓展时用到的数组，0行为前点，1行为后点，2行为路径长度

        int k = 0;
        int s = 0;
        int i4 = 0;
        for (i1 = 0; i1 < graph.length - 1; i1++) //拓展次数
        {
            k = 0;
            s = 0;
            i4 = 0;

            for (i2 = 0; i2 < spointnum; i2++)//已达点
            {

                for (i3 = 0; i3 < graph[spoint[i2]].length; i3++)//已经达到的点的度
                {
                    a[0][i4] = spoint[i2];
                    a[1][i4] = graph[spoint[i2]][i3][0];


                    a[2][i4] = graph[spoint[i2]][i3][1] + routelength[1][i2];//计算路径长度
                    //System.out.println(i4);
                    i4++;
                    int i5 = 0;
                    for (i5 = 0; i5 < spointnum; i5++)// 如果所找的点是已经找到最短路径的点，
                    // 则删去该点对应的值
                    {

                        if (spoint[i5] == a[1][i4 - 1]) {
                            i4--;
                            break;
                        }

                    }
                }
            }

            if (i4 == 0)    //i4为0即为无法扩充得到新的点，即没有到达目标点的通路
            {
                routeji[0] = 1000;
                return new ArrayList<Integer>();
                //break;//无通路
            }
            s = a[2][0];
            k = 0;
            for (i2 = 0; i2 < i4; i2++) //在扩充的路径中找最短的
            {
                if (a[2][i2] < s) {
                    s = a[2][i2];
                    k = i2;
                }
            }

            spoint[spointnum] = a[1][k];//更新最短路径上的点之间的前后关系
            frontpoint[0][spointnum] = a[1][k];
            frontpoint[1][spointnum] = a[0][k];
            for (i6 = 0; i6 < spointnum; i6++)  //找前点在spoint中的列标
            {
                if (frontpoint[0][i6] == frontpoint[1][spointnum]) {
                    frontpoint[2][spointnum] = i6;
                    break;
                }
            }
            routelength[0][spointnum] = a[1][k];//更新找到最短路径的点对应的路径长度
            routelength[1][spointnum] = a[2][k];
            spointnum++;

            if (frontpoint[0][spointnum - 1] == j)//如果新更的点为目标点，则路径寻找过程结束
                break;
        }

        if (i1 == graph.length - 1) {
            //无通路 返回一个空的

            routeji[0] = 1001;
            return new ArrayList<Integer>();
        }

        i1 = spointnum - 1;
        i2 = 1;
        while (i1 > 0)//找最短路径
        {
            routeji[i2] = frontpoint[0][i1];
            i1 = frontpoint[2][i1];
            i2++;
        }
        routeji[i2] = i;
        routeji[0] = i2;
        for (i1 = 1; i1 <= i2 / 2; i1++)//换个方向
        {
            i3 = routeji[i2 + 1 - i1];
            routeji[i2 + 1 - i1] = routeji[i1];
            routeji[i1] = i3;
        }
        ArrayList<Integer>arrayList=new ArrayList<Integer>();
        for(int iter=1;iter<routeji[0]+1;iter++){
            arrayList.add(routeji[i]);
        }
        return arrayList;

    }

    public void Mutation() {
//        this.CalculFit();
//        System.out.println(this.CalculFit(this.getBestRoute()));
//        变异函数  必须要有中间节点
        if (this.demand.length - 2 == 0)
            return;
        //int Popsize=population.length;
        int Dpointnum = this.demand.length - 2;//必经中间节点个数
        int i;
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
            int j, k;
            j = -1;
            s = 30;//路径长度小于30
            int sign = -1;

            for (j = 0; j < graph[demand[xi + 1]].length; j = j + 1)// 对必经点的所有后续节点
            {

                for (k = 0; k < population[i].size(); k++)  //对该基因表示的所有基因节点
                {
                    if (graph[demand[xi + 1]][j][0] == population[i].get(k)
                            & graph[demand[xi + 1]][j][1] < s) {
                        s = graph[demand[xi + 1]][j][1];
                        sign = k;
                        break;
                    }
                    if (graph[demand[xi + 1]][j][0] == population[i].get(k)) {
                        break;
                    }
                }
            }
            if (sign > -0.5 & sign > 0 & sign < population[i].size() - 1)
                j = sign;
            else
                j = (int) (Math.random() * (population[i].size() - 2.001)) + 1;

            s = population[i].get(j - 1);                //j的前一个节点
            t = population[i].get(j + 1);            //j的后一个节点


            //迪杰斯特拉斯算法找新路径
            int[][][] newGraph = this.ReShapeGraph(population[i], s, t);

            int[] rout_s_j = this.dijstra(newGraph, s, demand[xi + 1]);
            int[] rout_j_t = this.dijstra(newGraph, demand[xi + 1], t);
            //路径拼接
            int m = rout_s_j[0];
            int n = rout_j_t[0];
            if (m > 900 | n > 900)//  找不到拼接路径的话，则跳过该条的变异
                continue;
            int ss;
            for (k = 2; k <= m - 1; k++) {
                population[i].add(j + k - 2, rout_s_j[k]);
            }
            population[i].set(j + m - 2, demand[xi + 1]);
            for (k = 2; k <= n - 1; k++) {
                population[i].add(j + m - 2 + 1 + k - 2, rout_j_t[k]);
            }

            population[i] = this.removeLoop(population[i]);//去环
        }

    }

    private int[][][] ReShapeGraph(ArrayList<Integer> route, int fPoint, int bPoint) {
//        在图中去除经过route 中点的变
        int pointNum = this.graph.length;
        int[][][] newGraph = new int[pointNum][][];
        for (int i = 0; i < pointNum; i++) {
            if (route.contains(i) && i != fPoint && i != bPoint) {
                newGraph[i] = new int[0][0];
            } else {
                int iLength = this.graph[i].length;
                newGraph[i] = new int[iLength][2];

                for (int j = 0; j < iLength; j++) {
                    newGraph[i][j] = this.graph[i][j].clone();
                }
            }
        }
        return newGraph;
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

    public int[] dijstra(int[][][] graph, int i, int j) {
        //迪杰斯特拉斯算法
        int i1, i2, i3;
        int routeji[] = new int[601];//第一个数为路径点个数， 后续为全路径点
        int spoint[] = new int[600];//已找到最短路径的点
        int frontpoint[][] = new int[3][600];  //   0 行为后点，1行为前点,2行为前点所在列的下标
        int routelength[][] = new int[2][600];// 0行为点，1行为最短路径长度
        int spointnum = 1;  //找到最短路径的点的个数
        routelength[0][0] = i;//初始点i设置
        routelength[1][0] = 0;
        frontpoint[0][0] = i;
        frontpoint[1][0] = i;
        frontpoint[2][0] = 0;
        spoint[0] = i;
        int i6 = 0;
        int a[][] = new int[3][600];    //拓展时用到的数组，0行为前点，1行为后点，2行为路径长度

        int k = 0;
        int s = 0;
        int i4 = 0;
        for (i1 = 0; i1 < graph.length - 1; i1++) //拓展次数
        {
            k = 0;
            s = 0;
            i4 = 0;

            for (i2 = 0; i2 < spointnum; i2++)//已达点
            {

                for (i3 = 0; i3 < graph[spoint[i2]].length; i3++)//已经达到的点的度
                {
                    a[0][i4] = spoint[i2];
                    a[1][i4] = graph[spoint[i2]][i3][0];


                    a[2][i4] = graph[spoint[i2]][i3][1] + routelength[1][i2];//计算路径长度
                    //System.out.println(i4);
                    i4++;
                    int i5 = 0;
                    for (i5 = 0; i5 < spointnum; i5++)// 如果所找的点是已经找到最短路径的点，
                    // 则删去该点对应的值
                    {

                        if (spoint[i5] == a[1][i4 - 1]) {
                            i4--;
                            break;
                        }

                    }
                }
            }

            if (i4 == 0)    //i4为0即为无法扩充得到新的点，即没有到达目标点的通路
            {
                routeji[0] = 1000;
                return routeji;
                //break;//无通路
            }
            s = a[2][0];
            k = 0;
            for (i2 = 0; i2 < i4; i2++) //在扩充的路径中找最短的
            {
                if (a[2][i2] < s) {
                    s = a[2][i2];
                    k = i2;
                }
            }

            spoint[spointnum] = a[1][k];//更新最短路径上的点之间的前后关系
            frontpoint[0][spointnum] = a[1][k];
            frontpoint[1][spointnum] = a[0][k];
            for (i6 = 0; i6 < spointnum; i6++)  //找前点在spoint中的列标
            {
                if (frontpoint[0][i6] == frontpoint[1][spointnum]) {
                    frontpoint[2][spointnum] = i6;
                    break;
                }
            }
            routelength[0][spointnum] = a[1][k];//更新找到最短路径的点对应的路径长度
            routelength[1][spointnum] = a[2][k];
            spointnum++;

            if (frontpoint[0][spointnum - 1] == j)//如果新更的点为目标点，则路径寻找过程结束
                break;
        }

        if (i1 == graph.length - 1) {//无通路
            routeji[0] = 1001;
            return routeji;
        }

        i1 = spointnum - 1;
        i2 = 1;
        while (i1 > 0)//找最短路径
        {
            routeji[i2] = frontpoint[0][i1];
            i1 = frontpoint[2][i1];
            i2++;
        }
        routeji[i2] = i;
        routeji[0] = i2;
        for (i1 = 1; i1 <= i2 / 2; i1++)//换个方向
        {
            i3 = routeji[i2 + 1 - i1];
            routeji[i2 + 1 - i1] = routeji[i1];
            routeji[i1] = i3;
        }
        return routeji;

    }

}
