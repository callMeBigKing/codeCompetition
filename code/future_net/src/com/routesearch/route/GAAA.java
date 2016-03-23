package com.routesearch.route;

import java.util.*;
import com.filetool.util.LogUtil;
import com.sun.javafx.image.BytePixelSetter;
import com.sun.org.apache.xpath.internal.FoundIndex;

/**
 * Created by imgos on 2016/3/20.
 */
public class GAAA {
    //    ga
    private int totalWeight;
    //    权重
    private int Popsize = 50; //种群规模
    private double crossRate =0.9;//交叉概率
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
    ArrayList<Integer>[][]dijstraRoute;
    int [][]dijstraRouteBo;

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
        if(pointNum<=10){
            this.Popsize=10;
        }
        else if(pointNum<=20){
            this.Popsize=40;
        }
        else if(pointNum<40){
            this.Popsize=130;
        }
        else if(pointNum<50){
            this.Popsize=180;
        }
        else if(pointNum<100){
            this.Popsize=200;
        }
        else if(pointNum*2<700){
           this.Popsize=pointNum*2;
        }else{
            this.Popsize=700;
        }
        this.pointNum=pointNum;
        this.edgeNum=edgeNum;
        this.dijstraRouteBo=new int[pointNum][pointNum];
        this.dijstraRoute=new ArrayList[pointNum][pointNum];
        for(int i=0;i<pointNum;i++){
            for(int j=0;j<pointNum;j++){
                dijstraRoute[i][j]=new ArrayList<Integer>();
            }
        }
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

    public void InitPop2() {
//        初始化种群，随机产生一群从起点到终点的路径
        population = new ArrayList[this.Popsize];
        int num=Popsize;
        while (num>0){
            ArrayList<Integer>route=this.CreateOneRoute();
            if(this.JugeDemand(route)){
                population[num-1] = route;
                num--;
            }
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

    public ArrayList<Integer> CreateOneRoute(int startPoint,int endPoint ) {
        //生成一条从起点到终点的路径dd
        ArrayList<Integer> route = new ArrayList<Integer>();
        int maxFind=20;
        while (maxFind>0) {
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
            maxFind--;
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
        if(random.nextDouble()<this.crossRate) {
            int fPointIndex=0;
            int mPointIndex=0;
            try {
                fPointIndex = random.nextInt(father.size() - 2) + 1;
//        生成一个【1~~size-1） 的点  father的交叉点
                mPointIndex = random.nextInt(mather.size() - 2) + 1;
            }
            catch (Exception e ){
//                当father.size()==2 时候直接return无需交叉
                return new ArrayList[]{father, mather};
            }

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
                ArrayList<Integer> route = this.dijstra(fPoint, mPointNext);


                for (int i = 0; i < route.size() - 2; i++) {
                    subFather1.add(i + 1 + fPointIndex, route.get(i + 1));
                }
                //          如果size为0 则没有两个点是不连通的。

                if (route.size() == 0) subFather1 = father;
            }

            ArrayList<Integer> subMather1 = Tool.ListClone(mather, 0, mPointIndex + 1);
//            前面的默认设置是subFather1保留fPointIndex，subMather1保留mPointIndex
            ArrayList<Integer> subFather2 = Tool.ListClone(father, fPointIndex + 1, father.size());
            subMather1.addAll(subFather2);

            if (!mFlag) {
//            不能够连接起来
                ArrayList<Integer> route = this.dijstra(mPoint, fPointNext);

                for (int i = 0; i < route.size() - 2; i++) {
                    subMather1.add(i + 1 + mPointIndex, route.get(i + 1));
                }

                //      sss     如果size为0 则没有两个点是不连通的。
                if (route.size() == 0) subMather1 = mather;
            }

            babby1 = this.removeLoop(subFather1);
            babby2 = this.removeLoop(subMather1);

            return new ArrayList[]{babby1, babby2};
        }
        else return new ArrayList[]{father, mather};

    }

    private ArrayList[] Crossover2(int[] parents) {

//      crossover 为单点交叉，Crossover2为多点交叉。

        ArrayList<Integer> father = (ArrayList<Integer>) this.population[parents[0]].clone();
        ArrayList<Integer> mather = (ArrayList<Integer>) this.population[parents[1]].clone();
        ArrayList<Integer> babby1 = new ArrayList<Integer>();
        ArrayList<Integer> babby2 = new ArrayList<Integer>();

        Random random = new Random();
        if(random.nextDouble()<this.crossRate) {
            int[] fPointIndex = new int[2];
            fPointIndex[0] = random.nextInt(father.size() - 2) + 1;
            fPointIndex[1] = random.nextInt(father.size() - 2) + 1;
            fPointIndex=Tool.Sort(fPointIndex);

//        生成一个【1~~size-1） 的点  father的交叉点

            int[] mPointIndex = new int[2];
            mPointIndex[0] = random.nextInt(mather.size() - 2) + 1;
            mPointIndex[1] = random.nextInt(mather.size() - 2) + 1;
            mPointIndex=Tool.Sort(mPointIndex);//排个序,前面的小后面的大
            if(fPointIndex[0]==fPointIndex[1]||mPointIndex[0]==mPointIndex[1]){
                return new ArrayList[]{father, mather};
            }

//          同上
            int[] fPoint = new int[2];
            fPoint[0] = father.get(fPointIndex[0]);
            fPoint[1] = father.get(fPointIndex[1]);
//             前面的点
            int[] fPointNext = new int[2];
            fPointNext[0] = father.get(fPointIndex[0] + 1);
            fPointNext[1] = father.get(fPointIndex[1] + 1);
//            后面的点
            int[] mPoint = new int[2];
            mPoint[0] = mather.get(mPointIndex[0]);
            mPoint[1] = mather.get(mPointIndex[1]);
//            mather 前面的点
            int[] mPointNext = new int[2];
            mPointNext[0] = mather.get(mPointIndex[0] + 1);
            mPointNext[1] = mather.get(mPointIndex[1] + 1);
//            mather 后面的点


//        判断这几个点是不是连通的，不是的话就用dj 得到一条最短路径
            boolean[] fFlag = {false,false};  //判断第一个点能不能接上，
            boolean []mFlag = {false,false};

            for (int i = 0; i < graphList[fPoint[0]].size(); i++) {
//                判断父亲的第一个交换结点能不能交换
                if (this.graphList[fPoint[0]].get(i)[0] == mPointNext[0]) {
                    fFlag[0] = true;
                    break;
                }
            }

            for (int i = 0; i < graphList[fPoint[1]].size(); i++) {
//                判断父亲的第Er 个交换结点能不能交换
                if (this.graphList[fPoint[1]].get(i)[0] == mPointNext[1]) {
                    fFlag[1] = true;
                    break;
                }
            }

            for (int i = 0; i < graphList[mPoint[0]].size(); i++) {
//                判断母亲的第一个结点能不能直接交换
                if (this.graphList[mPoint[0]].get(i)[0] == fPointNext[0]) {
                    mFlag[0] = true;
                    break;
                }
            }

            for (int i = 0; i < graphList[mPoint[1]].size(); i++) {
//                判断母亲的第er个结点能不能直接交换
                if (this.graphList[mPoint[1]].get(i)[0] == fPointNext[1]) {
                    mFlag[1] = true;
                    break;
                }
            }

//            前面的默认设置是subFather1保留fPointIndex，subMather1保留mPointIndex
//               分成3段 交换中间一段
            ArrayList<Integer> subFather1 = Tool.ListClone(father, 0, fPointIndex[0] + 1);
            ArrayList<Integer> subFather2 = Tool.ListClone(father, fPointIndex[0] + 1, fPointIndex[1] + 1);
            ArrayList<Integer> subFather3 = Tool.ListClone(father,  fPointIndex[1] + 1,father.size());
            ArrayList<Integer> subMather1 = Tool.ListClone(mather, 0, mPointIndex[0] + 1);
            ArrayList<Integer> subMather2 = Tool.ListClone(mather, mPointIndex[0] + 1,mPointIndex[1] + 1);
            ArrayList<Integer> subMather3 = Tool.ListClone(mather, mPointIndex[1] + 1, mather.size());

            subFather1.addAll(subMather2);
            subFather1.addAll(subFather3);

            boolean fConnect[]={true,true};
            if (!fFlag[0]) {
//           前面一个节点不能够直接连接起来 寻找一条最短路径
                ArrayList<Integer> route = this.dijstra(fPoint[0], mPointNext[0]);
                for (int i = 0; i < route.size() - 2; i++) {
                    subFather1.add( 1 + fPointIndex[0], route.get(i + 1));
                    fPointIndex[0]++;
//                   每次都插在 fPointIndex0后面一个位置，第二次插入的时候相对位置不会发生变化
//                    依然是submather.size()
                }
                //          如果size为0 则没有两个点是不连通的。

                if (route.size() == 0) fConnect[0]=false;
//                不是连通的
            }
            if (!fFlag[1]&&fConnect[0]) {
//           前面一个节点不能够直接连接起来 寻找一条最短路径
                int length=subMather2.size();
                ArrayList<Integer> route = this.dijstra(mPoint[1], fPointNext[1]);
                for (int i = 0; i < route.size() - 2; i++) {
                    subFather1.add(i + 1 + fPointIndex[0]+length, route.get(i + 1));
                }
                //          如果size为0 则没有两个点是不连通的。

                if (route.size() == 0) fConnect[0]=false;
//                不是连通的
            }
            if(!fConnect[0]||!fConnect[1]){
//                只要有一段是不联通的子代就用father来替代
                subFather1=father;
            }


            subMather1.addAll(subFather2);
            subMather1.addAll(subMather3);
//            互换第二段
            boolean mConnect[]={true,true};
            if (!mFlag[0]) {
//            第一段不能够连接起来
                ArrayList<Integer> route = this.dijstra(mPoint[0], fPointNext[0]);
                for (int i = 0; i < route.size() - 2; i++) {
                    subMather1.add(1 + mPointIndex[0], route.get(i + 1));
                    mPointIndex[0]++;
                }

                //      sss     如果size为0 则没有两个点是不连通的。
                if (route.size() == 0) mConnect[0]=false;
            }

            if (!mFlag[1]&&mConnect[0]) {
                int length=subFather2.size();
//           前面一个节点不能够直接连接起来 寻找一条最短路径
                ArrayList<Integer> route = this.dijstra(fPoint[1], mPointNext[1]);
                for (int i = 0; i < route.size() - 2; i++) {
                    subMather1.add(i + 1 + mPointIndex[0]+length, route.get(i + 1));
                }
                //          如果size为0 则没有两个点是不连通的。

                if (route.size() == 0) mConnect[0]=false;
//                不是连通的
            }
            if(!mConnect[0]||!mConnect[1]){
//                只要有一段是不联通的子代就用father来替代
                subMather1=mather;
            }

            babby1 = this.removeLoop(subFather1);
            babby2 = this.removeLoop(subMather1);

            return new ArrayList[]{babby1, babby2};
        }
        else return new ArrayList[]{father, mather};

    }

    public void Breed() {
//        繁殖函数包括整个选择和交叉过程

        ArrayList[] nextPopulation = new ArrayList[this.Popsize];
        this.CalculFit();
//        System.out.print(this.CalculFit(this.getBestRoute()));
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

    private boolean JugeDemand(ArrayList<Integer>route) {
//        判断最后的路径是否满足demand
        Iterator<Integer> demand = this.demandSet.iterator();
        boolean flage = true;
        while (demand.hasNext()) {
            if (!route.contains(demand.next())) {
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

        ArrayList<Integer> best = Tool.ListClone(population[0], 0, population[0].size());


        for (i = 1; i < Popsize; i += 1) //对每一个个体进行变异操作
        {
            if (Math.random() > this.mutationRate || population[i].size() < 3) {
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
            if (j <= 0 || j >= population[i].size() - 1) {
                j = (int) (Math.random() * (population[i].size() - 2)) + 1;
            }
            s = population[i].get(j - 1);
            t = population[i].get(j + 1);
//            ArrayList<int[]>[] newGrah=this.ReShapeGraph(population[i],s,t);
            ArrayList<Integer> rout_s_d = this.dijstra(s, demand[xi + 1]);
            ArrayList<Integer> rout_d_t = this.dijstra(demand[xi + 1], t);
            if (rout_d_t.size() == 0 || rout_s_d.size() == 0) {
                continue;
            }
            population[i].set(j, demand[xi + 1]);


            population[i].remove(j - 1);
            population[i].remove(j - 1);
            population[i].remove(j - 1);

            population[i].addAll(j - 1, rout_d_t);
            population[i].remove(j - 1);
            population[i].addAll(j - 1, rout_s_d);

            population[i] = removeLoop(population[i]);

        }
        //第0个
        for (int zi=0;zi<1;zi++)
        {
            i = 0;
            if (Math.random() > this.mutationRate || population[i].size() < 3) {
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
            if (j <= 0 || j >= population[i].size() - 1) {
                j = (int) (Math.random() * (population[i].size() - 2)) + 1;
            }
            s = population[i].get(j - 1);
            t = population[i].get(j + 1);
            ArrayList<int[]>[] newGrah = this.ReShapeGraph(population[i], s, s);
            ArrayList<Integer> rout_s_d = Tool.dijstra(newGrah, s, demand[xi + 1]);
            ArrayList<Integer> p_rout_s_d = new ArrayList<Integer>();
            p_rout_s_d.addAll(0, population[i]);
            p_rout_s_d.addAll(0, rout_s_d);

            newGrah = this.ReShapeGraph(population[i], s, s);
            ArrayList<Integer> rout_d_t = Tool.dijstra(newGrah, demand[xi + 1], t);
            if(rout_s_d.size()==0||rout_d_t.size()==0)
                continue;

            population[i].set(j, demand[xi + 1]);


            population[i].remove(j - 1);
            population[i].remove(j - 1);
            population[i].remove(j - 1);

            population[i].addAll(j - 1, rout_d_t);
            population[i].remove(j - 1);
            population[i].addAll(j - 1, rout_s_d);

            population[i] = removeLoop(population[i]);
            Random random=new Random();
            population[random.nextInt(this.Popsize-1)+1]=population[i];
            population[0]=Tool.ListClone(best, 0, best.size());

        }



        population[0]=best;


    }

    public void Mutation2() {
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

        ArrayList<Integer> best = Tool.ListClone(population[0], 0, population[0].size());

        int chosi=(int) (Math.random() * (population.length));



        for (i = 1; i < Popsize; i += 1) //对每一个个体进行变异操作
        {
            if (Math.random() > this.mutationRate || population[i].size() < 3) {
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
                    if (graphList[demand[xi + 1]].get(dtopi)[0] == population[i].get(dtopj)
                            && graphList[demand[xi + 1]].get(dtopi)[1] < weightdtoj) {
                        j = dtopj;
                        weightdtoj=graphList[demand[xi + 1]].get(dtopi)[1];
                        break;
                    }
                }
            }
            if (j <= 0 || j >= population[i].size() - 1) {
                j = (int) (Math.random() * (population[i].size() - 2)) + 1;
            }
            s = population[i].get(j - 1);
            t = population[i].get(j + 1);
//            ArrayList<int[]>[] newGrah=this.ReShapeGraph(population[i],s,t);
            ArrayList<Integer> rout_s_d;
            ArrayList<Integer> rout_d_t;
            if (Math.abs(i-chosi)<10)
            {
                rout_s_d=this.CreateOneRoute(s, demand[xi + 1]);
                rout_d_t=this.CreateOneRoute(demand[xi + 1], t);
            }

                else
            {
             rout_s_d = this.dijstra(s, demand[xi + 1]);
             rout_d_t = this.dijstra(demand[xi + 1], t);
            }
            if (rout_d_t.size() == 0 || rout_s_d.size() == 0) {
                continue;
            }

            population[i].remove(j - 1);
            population[i].remove(j - 1);
            population[i].remove(j - 1);

            population[i].addAll(j - 1, rout_d_t);
            population[i].remove(j - 1);
            population[i].addAll(j - 1, rout_s_d);

            population[i] = removeLoop(population[i]);

        }

        //第0个

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

    public  ArrayList<Integer> dijstra(int i, int j)//迪杰斯特拉斯算法，
    {
        //从图graphList中找到点i，到点j的最短路径，并返回该路径
        //要求:i和j不能相同
        if(this.dijstraRoute[i][j].size()!=0)return dijstraRoute[i][j];
        if(this.dijstraRouteBo[i][j]==1)
        {
            ArrayList<Integer>route=new ArrayList<Integer>();
            return route;
        }

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
            this.dijstraRouteBo[i][j]=1;//   更新通路判断矩阵
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
            for (int sti=1; sti<ArrivePointInfo[0].size();sti++)//改
            {
                int j1=ArrivePointInfo[0].get(sti);//列标
                if (dijstraRoute[i][j1].size()==0)
                {
                    m=sti;
                    while (m!=0) {
                        dijstraRoute[i][j1].add(0, ArrivePointInfo[0].get(m));
                        m = ArrivePointInfo[1].get(m);
                    }
                    dijstraRoute[i][j1].add(0, i);

                }

            }                                                        //改


            return route;
        }
    }
}
