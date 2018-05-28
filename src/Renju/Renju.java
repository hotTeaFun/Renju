package Renju;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static java.lang.Integer.min;

public class Renju extends Frame {
    private final static Color BLACK=new Color(1);
    private final static Color WHITE=new Color(0xDFE1D7);
    private final static int m=5;//储存五子棋的5
    private final static int n=15;//棋盘行列数（标准15×15）
    private static int[][] HasX;
    private static int[][] HasY;
    enum Model{PvP,PvE,EvE}//模式(分别对应人人，人机，机机)
    Model model;
    volatile int[] DropPoint;//储存落子点坐标（X=DropPoint[0],Y=DropPoint[1]）
    private LinkedList<int[]> PointsRecord;//储存对弈落子情况
    volatile boolean Statement;//储存当前棋手（0人1机）
    boolean Forbidden;//储存禁手方（0黑1白）
    private boolean FlagLong;//储存是否有长连禁手
    private boolean FlagTwoAlive3;//储存是否有三三禁手
    volatile boolean Actor;//储存当前落子方（0黑1白）
    int per;//储存棋盘点之间的距离
    int Oval;//储存棋子半径
    enum State{Empty,Black,White}//对应三种不同状态
    State[][] Pieces;//储存棋盘状态
    public static void main(String[] args){
        BeginUI Begin=new BeginUI();
        Begin.setTitle("The Begin Page");
        Begin.setSize(new Dimension(600,600));
        Begin.setVisible(true);
    }
    Renju(){
        setBackground(new Color(127, 190, 255));
        Pieces=new State[n][n];
        HasX=new int[4][2];
        HasY=new int[4][2];
        for (int i=0;i<4;i++) {
            HasX[i][0]=0;
            HasX[i][1]=i!=1 ? n-m:n;
            HasY[i][0]=i!=3 ? 0:m;
            HasY[i][1]=i<3&&i>0 ? n-m:n;
        }
        DropPoint=new int[2];
        PointsRecord=new LinkedList<>();
        for(int i=0;i<n;i++)
            for (int j=0;j<n;j++){
                Pieces[i][j]=State.Empty;
           }
        model=Model.PvP;//默认模式为人机
        Actor=false;//默认先手为黑方
        Statement=true;
        addMouseListener(new MyMouseAdapted(this));
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowStateChanged(WindowEvent e) {
                repaint();
            }
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        addKeyListener(new MyKeyAdapted(this));
    }
    void LaterAct(Graphics g,PiecesDetails piecesDetails) {
        g.setColor(Actor?WHITE:BLACK);
        g.fillOval((2+DropPoint[0])*per-Oval,(2+DropPoint[1])*per-Oval,Oval*2,Oval*2);
        Pieces[DropPoint[0]][DropPoint[1]]=Actor?Renju.State.White: Renju.State.Black;
        PreScan(piecesDetails);
        int[] Current={Actor ? 1 : 0, DropPoint[0], DropPoint[1]};
        if(PointsRecord.size()+1==n*n){
            EndUI endUI=new EndUI(this,"Nobody");
            endUI.setTitle("filled Pieces");
            endUI.setSize(new Dimension(400,400));
            endUI.setVisible(true);
        }
        PointsRecord.add(Current);
        Actor=!Actor;
    }
    void SetFlagLong(boolean FlagLong) {
        this.FlagLong=FlagLong;
    }
    void SetFlagTwoAlive3(boolean FlagTwoAlive3) {
        this.FlagTwoAlive3 = FlagTwoAlive3;
    }
    void SetModel(Model model){
        this.model=model;
    }
    //储存扫描棋盘后的详细信息
    class PiecesDetails{
        Boolean AFlush6,DFlush6,AAlive4,AAlive3,AFlush3,DAlive4,AFlush4,DFlush4,Dflush3,DAlive3;
        ArrayList<int[]> Forbidden,Two3P,AFlush6P,DFlush6P,AALive4P,DALive4P,AFlush4P,DFlush4P,DAlive3P,AALive3P,DFlush3P,AFlush3P,AAlive2P;
    PiecesDetails(){
        AAlive3=DAlive3=AFlush3=Dflush3=AAlive4=DAlive4=AFlush4=DFlush4=AFlush6=DFlush6=false;
        Forbidden=new ArrayList<>();
        AFlush6P=new ArrayList<>();
        DFlush6P=new ArrayList<>();
        AALive4P=new ArrayList<>();
        DALive4P=new ArrayList<>();
        AFlush4P=new ArrayList<>();
        DFlush4P=new ArrayList<>();
        AALive3P=new ArrayList<>();
        DAlive3P=new ArrayList<>();
        DFlush3P=new ArrayList<>();
        AFlush3P=new ArrayList<>();
        AAlive2P=new ArrayList<>();
        Two3P=new ArrayList<>();
    }
    boolean AFlush64(int[] check) {
        for (int[] text:Forbidden
             ) {
            if (text[0]==check[0]&&text[1]==check[1])
                return false;
        }
        return true;
    }
}

    private void PreScan(PiecesDetails piecesDetails) {
        State current=Actor?State.White:State.Black;
        State opp=!Actor?State.White:State.Black;
        for (int flag = 0; flag < 4; flag++)
            for (int i = HasX[flag][0]; i < HasX[flag][1]; i++)
                for (int j = HasY[flag][0]; j < HasY[flag][1]; j++) {
                    int Cc = 0, Ec = 0;
                    for (int f = 0; f <= m; f++) {
                        if (Pieces[i + get(flag, f)[0]][j + get(flag, f)[1]] == current)
                            Cc++;
                        else if (Pieces[i + get(flag, f)[0]][j + get(flag, f)[1]] == State.Empty)
                            Ec++;
                        switch (Cc) {
                            case 6:
                                if (Actor == Forbidden && FlagLong)
                                    ForbiddenHand();
                                else GameOver(current);
                                break;
                            case 5:
                                if (Pieces[i][j] != current || Pieces[i + get(flag, m)[0]][j + get(flag, m)[1]] != current)
                                    GameOver(current);
                                else if (Ec==1) {
                                    int Symbol = 0;
                                    while (++Symbol < m)
                                        if (Pieces[i + get(flag, Symbol)[0]][j + get(flag, Symbol)[1]] == State.Empty)
                                            break;
                                    if (Actor == Forbidden && FlagLong)
                                        piecesDetails.Forbidden.add(new int[]{i + get(flag, Symbol)[0], j + get(flag, Symbol)[1]});

                                    else {
                                        piecesDetails.AFlush6 = true;
                                        piecesDetails.AFlush6P.add(new int[]{i + get(flag, Symbol)[0], j + get(flag, Symbol)[1]});
                                    }
                                }break;
                            case 3:
                             if (Ec == 3 && Pieces[i][j] == State.Empty && Pieces[i + get(flag, m)[0]][j + get(flag, m)[1]] == State.Empty) {
                                 piecesDetails.AAlive3=true;
                                 for (int s = 1; s < m; s++) {
                                     if (Actor == Forbidden && FlagTwoAlive3 && Pieces[i + get(flag, s)[0]][j + get(flag, s)[1]] == current)
                                         piecesDetails.Two3P.add(new int[]{flag, i + get(flag, s)[0], j + get(flag, s)[1]});
                                     if (Pieces[i + get(flag, s)[0]][j + get(flag, s)[1]] == State.Empty)
                                         piecesDetails.AALive3P.add(new int[]{i + get(flag, s)[0], j + get(flag, s)[1]});
                                 }
                             }
                             else if (((Pieces[i][j] == opp&&Pieces[i+get(flag,1)[0]][j+get(flag,1)[1]]==current) || (Pieces[i + get(flag, m)[0]][j + get(flag, m)[1]] ==opp&&Pieces[i+get(flag,m-1)[0]][j+get(flag,m-11)[1]]==current))&&Ec==2) {
                                        piecesDetails.AFlush3=true;
                                        for (int q=0;q<=m;q++)
                                         if (Pieces[i+get(flag,q)[0]][j+get(flag,q)[1]]==State.Empty)
                                           piecesDetails.AFlush3P.add(new int[]{i+get(flag,q)[0],j+get(flag,q)[1]});
                                    }break;
                        }
                    }
                }
                if (piecesDetails.Two3P.size()>1)
                  for (int t=0;t<piecesDetails.Two3P.size();t++)
                    for (int k=t+1;k<piecesDetails.Two3P.size();k++){
            if (piecesDetails.Two3P.get(t)[0]!=piecesDetails.Two3P.get(k)[0]&&piecesDetails.Two3P.get(t)[1]==piecesDetails.Two3P.get(k)[1]&&piecesDetails.Two3P.get(k)[2]==piecesDetails.Two3P.get(t)[2])
                ForbiddenHand();  }
    }
       private void ComputerScan(PiecesDetails piecesDetails) {
        State current=Actor?State.White:State.Black;
        State opp=!Actor?State.White:State.Black;
        for (int flag = 0; flag < 4; flag++)
            for (int i = HasX[flag][0]; i < HasX[flag][1]; i++)
                for (int j = HasY[flag][0]; j < HasY[flag][1]; j++) {
            int Cc=0,Ec=0;
            for (int f=0;f<=m;f++){
                    if(Pieces[i+get(flag,f)[0]][j+get(flag,f)[1]]==current)
                        Cc++;
                    else if (Pieces[i+get(flag,f)[0]][j+get(flag,f)[1]]==State.Empty)
                        Ec++;

            }
            switch (Cc) {

                case 4:
                    if(Pieces[i][j] == State.Empty && Pieces[i + get(flag, m)[0]][j + get(flag, m)[1]] == State.Empty) {
                        if (Actor == Forbidden && FlagLong)
                        {
                            if (piecesDetails.AFlush64(new int[]{i,j})&&piecesDetails.AFlush64(new int[]{i + get(flag, m)[0],j + get(flag, m)[1]}))
                      {piecesDetails.AAlive4 = true;
                        piecesDetails.AALive4P.add(new int[]{i, j});
                        piecesDetails.AALive4P.add(new int[]{i + get(flag, m)[0], i + get(flag, m)[1]});}
                        else if (!piecesDetails.AFlush64(new int[]{i,j})&&!piecesDetails.AFlush64(new int[]{i + get(flag, m)[0],j + get(flag, m)[1]}))
                            break;
                      else if (!piecesDetails.AFlush64(new int[]{i,j}))
                            {
                                piecesDetails.AFlush4=true;
                                piecesDetails.AFlush4P.add(new int[]{i + get(flag, m)[0],j + get(flag, m)[1]});
                            }
                            else   {
                                piecesDetails.AFlush4=true;
                                piecesDetails.AFlush4P.add(new int[]{i,j});
                            }
                    }
                    else {piecesDetails.AAlive4 = true;
                            piecesDetails.AALive4P.add(new int[]{i, j});
                            piecesDetails.AALive4P.add(new int[]{i + get(flag, m)[0], i + get(flag, m)[1]});}
                    }
                    else if (Ec==1){
                        if (Pieces[i + get(flag, m)[0]][i + get(flag, m)[1]]==opp){
                            int Symbol = -1;
                            while (++Symbol < m)
                                if (Pieces[i + get(flag, Symbol)[0]][j + get(flag, Symbol)[1]] == State.Empty)
                                    break;
                            if (Actor != Forbidden || !FlagLong||piecesDetails.AFlush64(new int[]{i + get(flag, Symbol)[0],j + get(flag, Symbol)[1]})) {
                                piecesDetails.AFlush4 = true;
                                piecesDetails.AFlush4P.add(new int[]{i + get(flag, Symbol)[0], j + get(flag, Symbol)[1]});
                            }
                        }
                        else if (Pieces[i][j] == opp){
                            piecesDetails.AFlush4=true;
                            piecesDetails.AFlush4P.add(new int[]{i + get(flag, m)[0], j + get(flag, m)[1]});
                        }
                    }
                    break;
                case 2:
                case 1:
                case 0:

            }
                    for (int flag0=1,k= 1; k < m; k++) {
                        if (Pieces[i][j] == Pieces[i + get(flag, k)[0]][j + get(flag, k)[1]]) flag0++;
                    }
                }
    }
    int[] ComputerJudge(PiecesDetails piecesDetails) {
        ComputerScan(piecesDetails);
          return new int[2];
    }
    private void GameOver(Renju.State winner) {
        EndUI endUI=new EndUI(this,winner==State.Black?"Black":"White");
        endUI.setTitle("GameOver");
        endUI.setSize(new Dimension(400,400));
        endUI.setVisible(true);
    }
    @Override
    public void paint(Graphics g) {
        //储存窗口尺寸
        Dimension dimension = getSize();
        //储存窗口高宽的较小值
        int minimum = min(dimension.height, dimension.width);
        per= minimum /(n+3);
        Oval=per/3;
        for(int i=2;i<n+2;i++) {
            g.drawLine(per*2,i*per,(n+1)*per,i*per);
            g.drawLine(i*per,per*2,i*per,(n+1)*per);
        }
        PaintPieces(g);
    }
    private void PaintPieces(Graphics g) {
        for (int x=0;x<n;x++)
            for (int y=0;y<n;y++)
                if(Pieces[x][y]!=State.Empty) {
                    g.setColor(Pieces[x][y] == State.White ? WHITE : BLACK);
                    g.fillOval((2 + x) * per - Oval, (2 + y) * per - Oval, Oval * 2, Oval * 2);
                }
    }
    private int[] get(int flag,int arg){
        int[] re=new int[2];
        switch (flag){
            case 0:
                re= new int[]{arg,0};
                break;
            case 1:
                re= new int[]{0,arg};
                break;
            case 2:
                re= new int[]{arg, arg};
                break;
            case 3:
                re= new int[]{arg, -arg};
                break;
        }
        return re;
    }
    void Swap(){
            for(int i=0;i<n;i++)
                for (int j=0;j<n;j++)
                    if(Pieces[i][j]!=State.Empty)
                        Pieces[i][j]=Pieces[i][j]==State.White?State.Black:State.White;
            Actor=!Actor;
            Forbidden=!Forbidden;
            PaintPieces(this.getGraphics());
            for (int[] change:PointsRecord)
                change[0]=change[0]==0?1:0;
    }
    void Undo(){
        int[] Current;
        if(!PointsRecord.isEmpty())
        { Current= PointsRecord.removeLast();
       Pieces[Current[1]][Current[2]]=State.Empty;
       Actor=Current[0]==1;
       repaint();}
    }
    void Capitulation(){
        EndUI endUI=new EndUI(this,Actor?"Black":"White");
        endUI.setTitle(!Actor?"Black capitulated":"White capitulated");
        endUI.setSize(new Dimension(400,400));
        endUI.setVisible(true);
    }
    private void ForbiddenHand(){
        EndUI endUI=new EndUI(this,Forbidden?"Black":"White");
        endUI.setTitle(!Forbidden?"Black is in a forbidden hand":"White is in a forbidden hand");
        endUI.setSize(new Dimension(400,400));
        endUI.setVisible(true);
    }
}
