package Renju;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import static java.lang.Integer.min;

public class Renju extends Frame {
    final static Color BLACK=new Color(1);
    final static Color WHITE=new Color(0xDFE1D7);
    enum Model{PvP,PvE,EvE};//模式(分别对应人人，人机，机机)
    Model model;
    volatile int[] DropPoint;//储存落子点坐标（X=DropPoint[0],Y=DropPoint[1]）
    volatile boolean Statement;//储存当前棋手（0人1机）
   volatile boolean Actor;//储存当前落子方（0黑1白）
    Dimension dimension;//储存窗口尺寸
    int Minimum;//储存窗口高宽的较小值
    int per;//储存棋盘点之间的距离
    int Oval;//储存棋子半径
    enum State{Empty,Black,White};//对应三种不同状态
    public State[][] Pieces;//储存棋盘状态
    public int[] ComputerJudge() {
        return new int[2];
    }
    public void LaterAct(Graphics g) {
        g.setColor(Actor?WHITE:BLACK);
        g.fillOval((2+DropPoint[0])*per-Oval,(2+DropPoint[1])*per-Oval,Oval*2,Oval*2);
        Pieces[DropPoint[0]][DropPoint[1]]=Actor?Renju.State.White: Renju.State.Black;
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
        for (int i=0;i<11;i++)
            for (int j=0;j<15;j++)
               if(XWin(i,j)!=State.Empty) return XWin(i,j);
        for (int i=0;i<11;i++)
            for (int j=4;j<15;j++)
                if(YWin(j,i)!=State.Empty) return YWin(j,i);
        for (int i=0;i<11;i++)
            for (int j=4;j<15;j++)
                if(WWin(i,j)!=State.Empty) return WWin(i,j);
        for (int i=0;i<11;i++)
            for (int j=0;j<11;j++)
                if(ZWin(i,j)!=State.Empty) return ZWin(i,j);
        return State.Empty;
    }

    private State XWin(int i, int j) {
        int flag=1;
        for(int k=1;k<5;k++)
        if(Pieces[i][j]==Pieces[i+k][j])flag++;
        return flag==5?Pieces[i][j]:State.Empty;
    }
    private State YWin(int i, int j){
        int flag=1;
        for(int k=1;k<5;k++)
            if(Pieces[i][j]==Pieces[i][j+k])flag++;
        return flag==5?Pieces[i][j]:State.Empty;
    }
    private State ZWin(int i, int j) {
        int flag=1;
        for(int k=1;k<5;k++)
            if(Pieces[i][j]==Pieces[i+k][j+k])flag++;
        return flag==5?Pieces[i][j]:State.Empty;
    }
    private State WWin(int i, int j) {
        int flag=1;
        for(int k=1;k<5;k++)
            if(Pieces[i][j]==Pieces[i+k][j-k])flag++;
        return flag==5?Pieces[i][j]:State.Empty;
    }
    public static void main(String[] args){
        BeginUI Begin=new BeginUI();
        Begin.setTitle("The Begin Page");
        Begin.setSize(new Dimension(600,600));
        Begin.setVisible(true);
    }
public Renju(){
    setBackground(new Color(127, 190, 255));
     Pieces=new State[15][15];
     DropPoint=new int[2];
     for(int i=0;i<15;i++)
         for (int j=0;j<15;j++)
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
}

    @Override
    public void paint(Graphics g) {
        dimension=getSize();
        Minimum=min(dimension.height,dimension.width);
        per=Minimum/18;
        Oval=per/3;
        for(int i=2;i<17;i++)
        {  g.drawLine(per*2,i*per,16*per,i*per);
           g.drawLine(i*per,per*2,i*per,16*per);
    }
        PaintPieces(g);
    }

    private void PaintPieces(Graphics g) {
        for (int x=0;x<15;x++)
            for (int y=0;y<15;y++)
                if(Pieces[x][y]==State.Empty){}
                else {
                    g.setColor(Pieces[x][y]==State.White?WHITE:BLACK);
                    g.fillOval((2+x)*per-Oval,(2+y)*per-Oval,Oval*2,Oval*2);
                }
    }
}
