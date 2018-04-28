package Renju;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.LinkedList;

import static java.lang.Integer.min;

public class Renju extends Frame {
    private final static Color BLACK=new Color(1);
    private final static Color WHITE=new Color(0xDFE1D7);
    private final static int m=5;//储存五子棋的5
    private final static int n=15;//棋盘行列数（标准15×15）
    enum Model{PvP,PvE,EvE}//模式(分别对应人人，人机，机机)
    Model model;
    volatile int[] DropPoint;//储存落子点坐标（X=DropPoint[0],Y=DropPoint[1]）
    private volatile LinkedList<int[]> PointsRecord;//储存对弈落子情况
    volatile boolean Statement;//储存当前棋手（0人1机）
    boolean Forbidden;//储存禁手方（0黑1白）
   volatile boolean Actor;//储存当前落子方（0黑1白）
    int per;//储存棋盘点之间的距离
    int Oval;//储存棋子半径
    enum State{Empty,Black,White}//对应三种不同状态
    State[][] Pieces;//储存棋盘状态
    int[] ComputerJudge() {
        return new int[2];
    }
    void LaterAct(Graphics g) {
        g.setColor(Actor?WHITE:BLACK);
        g.fillOval((2+DropPoint[0])*per-Oval,(2+DropPoint[1])*per-Oval,Oval*2,Oval*2);
        Pieces[DropPoint[0]][DropPoint[1]]=Actor?Renju.State.White: Renju.State.Black;
        int[] Current={Actor ? 1 : 0, DropPoint[0], DropPoint[1]};
        PointsRecord.add(Current);
        Actor=!Actor;
        State winner=Win();
        if(winner!=State.Empty) GameOver(winner);
    }

    private void GameOver(Renju.State winner) {
        EndUI endUI=new EndUI(this,winner==State.Black?"Black":"White");
        endUI.setTitle("GameOver");
        endUI.setSize(new Dimension(400,400));
        endUI.setVisible(true);
    }

    private State Win() {
        for (int i=0;i<n-m+1;i++)
            for (int j=0;j<n;j++)
               if(XWin(i,j)!=State.Empty) return XWin(i,j);
        for (int i=0;i<n-m+1;i++)
            for (int j=m-1;j<n;j++)
                if(YWin(j,i)!=State.Empty) return YWin(j,i);
        for (int i=0;i<n-m+1;i++)
            for (int j=m-1;j<n;j++)
                if(WWin(i,j)!=State.Empty) return WWin(i,j);
        for (int i=0;i<n-m+1;i++)
            for (int j=0;j<n-m+1;j++)
                if(ZWin(i,j)!=State.Empty) return ZWin(i,j);
        return State.Empty;
    }

    private State XWin(int i, int j) {
        int flag=1;
        for(int k=1;k<m;k++)
        if(Pieces[i][j]==Pieces[i+k][j])flag++;
        return flag==m?Pieces[i][j]:State.Empty;
    }
    private State YWin(int i, int j){
        int flag=1;
        for(int k=1;k<m;k++)
            if(Pieces[i][j]==Pieces[i][j+k])flag++;
        return flag==m?Pieces[i][j]:State.Empty;
    }
    private State ZWin(int i, int j) {
        int flag=1;
        for(int k=1;k<m;k++)
            if(Pieces[i][j]==Pieces[i+k][j+k])flag++;
        return flag==m?Pieces[i][j]:State.Empty;
    }
    private State WWin(int i, int j) {
        int flag=1;
        for(int k=1;k<m;k++)
            if(Pieces[i][j]==Pieces[i+k][j-k])flag++;
        return flag==m?Pieces[i][j]:State.Empty;
    }
    public static void main(String[] args){
        BeginUI Begin=new BeginUI();
        Begin.setTitle("The Begin Page");
        Begin.setSize(new Dimension(600,600));
        Begin.setVisible(true);
    }
Renju(){
    setBackground(new Color(127, 190, 255));
     Pieces=new State[n][n];
     DropPoint=new int[2];
     PointsRecord=new LinkedList<>();
     for(int i=0;i<n;i++)
         for (int j=0;j<n;j++)
             Pieces[i][j]=State.Empty;
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

    @Override
    public void paint(Graphics g) {
        //储存窗口尺寸
        Dimension dimension = getSize();
        //储存窗口高宽的较小值
        int minimum = min(dimension.height, dimension.width);
        per= minimum /(n+3);
        Oval=per/3;
        for(int i=2;i<n+2;i++)
        {  g.drawLine(per*2,i*per,(n+1)*per,i*per);
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
    void Swap(){
            for(int i=0;i<n;i++)
                for (int j=0;j<n;j++)
                    if(Pieces[i][j]!=State.Empty)
                        Pieces[i][j]=Pieces[i][j]==State.White?State.Black:State.White;
            Actor=!Actor;
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
    boolean ForbiddenHand(){
        return false;
    }
}
