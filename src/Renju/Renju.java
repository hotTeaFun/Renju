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
    private static int[][] WhileBodyX;
    private static int[][] WhileBodyY;
    enum Model{PvP,PvE,EvE}//模式(分别对应人人，人机，机机)
    Model model;
    volatile int[] DropPoint;//储存落子点坐标（X=DropPoint[0],Y=DropPoint[1]）
    private volatile LinkedList<int[]> PointsRecord;//储存对弈落子情况
    volatile boolean Statement;//储存当前棋手（0人1机）
    boolean Forbidden;//储存禁手方（0黑1白）
    private boolean FlagLong;//储存是否有长连禁手
    private boolean FlagTwoAlive3;//储存是否有三三禁手
    volatile boolean Actor;//储存当前落子方（0黑1白）
    private int[] WinPiece;//储存最后五子连珠的第一个子坐标
    int per;//储存棋盘点之间的距离
    int Oval;//储存棋子半径
    enum State{Empty,Black,White}//对应三种不同状态
    State[][] Pieces;//储存棋盘状态
    private boolean[][][] Flag;//标识活三的位置
    int[] ComputerJudge() {
        return new int[2];
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
    private void SetWinPoint(int[] WinPiece){
        this.WinPiece[0]=WinPiece[0];
        this.WinPiece[1]=WinPiece[1];
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
    void LaterAct(Graphics g) {
        g.setColor(Actor?WHITE:BLACK);
        g.fillOval((2+DropPoint[0])*per-Oval,(2+DropPoint[1])*per-Oval,Oval*2,Oval*2);
        Pieces[DropPoint[0]][DropPoint[1]]=Actor?Renju.State.White: Renju.State.Black;
        if(FlagTwoAlive3&&HasTwoAlive3(Forbidden?State.White:State.Black)){
            ForbiddenHand();
        }
        int[] Current={Actor ? 1 : 0, DropPoint[0], DropPoint[1]};
        if(PointsRecord.size()==n*n){
            EndUI endUI=new EndUI(this,"Nobody");
            endUI.setTitle("filled Pieces");
            endUI.setSize(new Dimension(400,400));
            endUI.setVisible(true);
        }
        PointsRecord.add(Current);
        State winner=Win();
        State F=Forbidden?State.White:State.Black;
        if(winner!=State.Empty) {
            if (!FlagLong||WinPiece[0] == n || WinPiece[1] == n || WinPiece[1] == -1 || Actor != Forbidden || Pieces[WinPiece[0]][WinPiece[1]] != F)
                GameOver(winner);
            else
              ForbiddenHand();
        }
        Actor=!Actor;
    }

    private boolean HasTwoAlive3(State target) {
        for (int i=0;i<n-m;i++)
            for (int j=0;j<n;j++)
                if(TraviseX(i,j,target))
                    return true;
        for (int i=0;i<n;i++)
            for (int j=0;j<n-m;j++)
                if(TraviseY(i,j,target))
                    return true;
        for (int i=0;i<n-m;i++)
            for (int j=m;j<n;j++)
                if(TraviseW(i,j,target))
                    return true;
        for (int i=0;i<n-m;i++)
            for (int j=0;j<n-m;j++)
                if(TraviseZ(i,j,target))
                    return true;
        for(int i=0;i<n;i++)
            for (int j=0;j<n;j++){
                for (int k=0;k<4;k++)
                    Flag[k][i][j]=false;
            }
        return false;
    }

    private boolean TraviseX(int i, int j, State target) {
        State Opp =target==State.White?State.Black:State.White;
        for (int k=1;k<m+1;k++)
            if (Pieces[i + k][j] == Opp)
                return false;
        if (Pieces[i][j]!=State.Empty)
            return false;
        else {
            if (Pieces[i+2][j]==target&&Pieces[i+3][j]==target&&Pieces[i+4][j]==target) {
                Flag[0][i+2][j]=true;Flag[0][i+3][j]=true;Flag[0][i+4][j]=true;
                if(Flag[1][i+2][j] || Flag[2][i+2][j] || Flag[3][i+2][j]||Flag[1][i+3][j] || Flag[2][i+3][j] || Flag[3][i+3][j]||Flag[1][i+4][j] || Flag[2][i+4][j] || Flag[3][i+4][j])
                return true;
            }
            if (Pieces[i+1][j]==target&&Pieces[i+2][j]==target&&Pieces[i+3][j]==target)
            {
                Flag[0][i+1][j]=true;Flag[0][i+2][j]=true;Flag[0][i+3][j]=true;
                if(Flag[1][i+1][j] || Flag[2][i+1][j] || Flag[3][i+1][j]||Flag[1][i+2][j] || Flag[2][i+2][j] || Flag[3][i+2][j]||Flag[1][i+3][j] || Flag[2][i+3][j] || Flag[3][i+3][j])
                    return true;
            }
            if (Pieces[i+1][j]==target&&Pieces[i+2][j]==target&&Pieces[i+4][j]==target)
            {
                Flag[0][i+2][j]=true;Flag[0][i+1][j]=true;Flag[0][i+4][j]=true;
                if(Flag[1][i+2][j] || Flag[2][i+2][j] || Flag[3][i+2][j]||Flag[1][i+1][j] || Flag[2][i+1][j] || Flag[3][i+1][j]||Flag[1][i+4][j] || Flag[2][i+4][j] || Flag[3][i+4][j])
                    return true;
            }
            if (Pieces[i + 1][j] == target&&Pieces[i + 3][j] == target&&Pieces[i + 4][j] == target) {
                Flag[0][i + 1][j] = true;Flag[0][i + 3][j] = true;Flag[0][i + 4][j] = true;
                return Flag[1][i + 1][j] || Flag[2][i + 1][j] || Flag[3][i + 1][j] || Flag[1][i + 3][j] || Flag[2][i + 3][j] || Flag[3][i + 3][j] || Flag[1][i + 4][j] || Flag[2][i + 4][j] || Flag[3][i + 4][j];
            }
        }
        return false;
    }

    private boolean TraviseW(int i, int j, State target) {
        State Opp = target == State.White ? State.Black : State.White;
        for (int k = 1; k < m + 1; k++)
            if (Pieces[i + k][j-k] == Opp)
                return false;
        if (Pieces[i][j] != State.Empty||Pieces[i+m][j-m]!=State.Empty)
            return false;
        else {
            if (Pieces[i + 2][j-2] == target && Pieces[i + 3][j-3] == target && Pieces[i + 4][j-4] == target)
            {
                Flag[3][i+2][j-2]=true;Flag[3][i+3][j-3]=true;Flag[3][i+4][j-4]=true;
                if(Flag[1][i+2][j-2] || Flag[2][i+2][j-2] || Flag[0][i+2][j-2]||Flag[1][i+3][j-3] || Flag[2][i+3][j-3] || Flag[0][i+3][j-3]||Flag[1][i+4][j-4] || Flag[2][i+4][j-4] || Flag[0][i+4][j-4])
                    return true;
            }
            if (Pieces[i + 1][j-1] == target && Pieces[i + 2][j-2] == target && Pieces[i + 3][j-3] == target)
            {
                Flag[3][i+2][j-2]=true;Flag[3][i+3][j-3]=true;Flag[3][i+1][j-1]=true;
                if(Flag[1][i+2][j-2] || Flag[2][i+2][j-2] || Flag[0][i+2][j-2]||Flag[1][i+3][j-3] || Flag[2][i+3][j-3] || Flag[0][i+3][j-3]||Flag[1][i+1][j-1] || Flag[2][i+1][j-1] || Flag[0][i+1][j-1])
                    return true;
            }
            if (Pieces[i + 1][j-1] == target && Pieces[i + 2][j-2] == target && Pieces[i + 4][j-4] == target)
            {
                Flag[3][i+2][j-2]=true;Flag[3][i+1][j-1]=true;Flag[3][i+4][j-4]=true;
                if(Flag[1][i+2][j-2] || Flag[2][i+2][j-2] || Flag[0][i+2][j-2]||Flag[1][i+1][j-1] || Flag[2][i+1][j-1] || Flag[0][i+1][j-1]||Flag[1][i+4][j-4] || Flag[2][i+4][j-4] || Flag[0][i+4][j-4])
                    return true;
            }
            if (Pieces[i + 1][j-1] == target&&Pieces[i + 3][j-3] == target&&Pieces[i + 4][j-4] == target) {
                Flag[3][i + 1][j-1] = true;Flag[3][i + 3][j-3] = true;Flag[3][i + 4][j-4] = true;
                return Flag[1][i + 1][j - 1] || Flag[2][i + 1][j - 1] || Flag[0][i + 1][j - 1] || Flag[1][i + 3][j - 3] || Flag[2][i + 3][j - 3] || Flag[0][i + 3][j - 3] || Flag[1][i + 4][j - 4] || Flag[2][i + 4][j - 4] || Flag[0][i + 4][j - 4];
            }
        }
        return false;
    }
    private boolean TraviseY(int i, int j, State target) {
        State Opp = target == State.White ? State.Black : State.White;
        for (int k = 1; k < m + 1; k++)
            if (Pieces[i][j+k] == Opp)
                return false;
        if (Pieces[i][j] != State.Empty||Pieces[i][j+m]!=State.Empty)
            return false;
        else {
            if (Pieces[i][j+2] == target && Pieces[i][j+3] == target && Pieces[i][j+4] == target)
            {
                Flag[1][i][j+2]=true;Flag[1][i][j+3]=true;Flag[1][i][j+4]=true;
                if(Flag[2][i][j+2] || Flag[3][i][j+2] || Flag[0][i][j+2]||Flag[2][i][j+3] || Flag[3][i][j+3] || Flag[0][i][j+3]||Flag[2][i][j+4] || Flag[3][i][j+4] || Flag[0][i][j+4])
                    return true;
            }
            if (Pieces[i][j+1] == target && Pieces[i][j+2] == target && Pieces[i][j+3] == target)
            {
                Flag[1][i][j+2]=true;Flag[1][i][j+3]=true;Flag[1][i][j+1]=true;
                if(Flag[2][i][j+2] || Flag[3][i][j+2] || Flag[0][i][j+2]||Flag[2][i][j+3] || Flag[3][i][j+3] || Flag[0][i][j+3]||Flag[2][i][j+1] || Flag[3][i][j+1] || Flag[0][i][j+1])
                    return true;
            }
            if (Pieces[i][j+1] == target && Pieces[i][j+2] == target && Pieces[i][j+4] == target)
            {
                Flag[1][i][j+2]=true;Flag[1][i][j+1]=true;Flag[1][i][j+4]=true;
                if(Flag[3][i][j+2] || Flag[2][i][j+2] || Flag[0][i][j+2]||Flag[3][i][j+1] || Flag[2][i][j+1] || Flag[0][i][j+1]||Flag[3][i][j+4] || Flag[2][i][j+4] || Flag[0][i][j+4])
                    return true;
            }
            if (Pieces[i][j+1] == target&&Pieces[i][j+3] == target&&Pieces[i][j+4] == target) {
                Flag[1][i][j+1] = true;Flag[1][i][j+3] = true;Flag[1][i][j+4] = true;
                return Flag[2][i][j + 1] || Flag[3][i][j + 1] || Flag[0][i][j + 1] || Flag[2][i][j + 3] || Flag[3][i][j + 3] || Flag[0][i][j + 3] || Flag[2][i][j + 4] || Flag[3][i][j + 4] || Flag[0][i][j + 4];
            }
        }
        return false;
    }

    private boolean TraviseZ(int i, int j, State target) {
        State Opp = target == State.White ? State.Black : State.White;
        for (int k = 1; k < m + 1; k++)
            if (Pieces[i + k][j+k] == Opp)
                return false;
        if (Pieces[i][j] != State.Empty||Pieces[i+m][j+m]!=State.Empty)
            return false;
        else {
            if (Pieces[i + 2][j+2] == target && Pieces[i + 3][j+3] == target && Pieces[i + 4][j+4] == target)
            {
                Flag[2][i+2][j+2]=true;Flag[2][i+3][j+3]=true;Flag[2][i+4][j+4]=true;
                if(Flag[1][i+2][j+2] || Flag[3][i+2][j+2] || Flag[0][i+2][j+2]||Flag[1][i+3][j+3] || Flag[3][i+3][j+3] || Flag[0][i+3][j+3]||Flag[1][i+4][j+4] || Flag[3][i+4][j+4] || Flag[0][i+4][j+4])
                    return true;
            }
            if (Pieces[i + 1][j+1] == target && Pieces[i + 2][j+2] == target && Pieces[i + 3][j+3] == target)
            {
                Flag[2][i+2][j+2]=true;Flag[2][i+3][j+3]=true;Flag[2][i+1][j+1]=true;
                if(Flag[1][i+2][j+2] || Flag[3][i+2][j+2] || Flag[0][i+2][j+2]||Flag[1][i+3][j+3] || Flag[3][i+3][j+3] || Flag[0][i+3][j+3]||Flag[1][i+1][j+1] || Flag[3][i+1][j+1] || Flag[0][i+1][j+1])
                    return true;
            }
            if (Pieces[i + 1][j+1] == target && Pieces[i + 2][j+2] == target && Pieces[i + 4][j+4] == target)
            {
                Flag[2][i+2][j+2]=true;Flag[2][i+1][j+1]=true;Flag[2][i+4][j+4]=true;
                if(Flag[1][i+2][j+2] || Flag[3][i+2][j+2] || Flag[0][i+2][j+2]||Flag[1][i+1][j+1] || Flag[3][i+1][j+1] || Flag[0][i+1][j+1]||Flag[1][i+4][j+4] || Flag[3][i+4][j+4] || Flag[0][i+4][j+4])
                    return true;
            }
            if (Pieces[i + 1][j+1] == target&&Pieces[i + 3][j+3] == target&&Pieces[i + 4][j+4] == target) {
                Flag[2][i + 1][j+1] = true;Flag[2][i + 3][j+3] = true;Flag[2][i + 4][j+4] = true;
                return Flag[1][i + 1][j + 1] || Flag[3][i + 1][j + 1] || Flag[0][i + 1][j + 1] || Flag[1][i + 3][j + 3] || Flag[3][i + 3][j + 3] || Flag[0][i + 3][j + 3] || Flag[1][i + 4][j + 4] || Flag[3][i + 4][j + 4] || Flag[0][i + 4][j + 4];
            }
        }
        return false;
    }

    private void GameOver(Renju.State winner) {
        EndUI endUI=new EndUI(this,winner==State.Black?"Black":"White");
        endUI.setTitle("GameOver");
        endUI.setSize(new Dimension(400,400));
        endUI.setVisible(true);
    }

    private State Win() {
        for (int flag = 0; flag < 4; flag++)
            for (int i = WhileBodyX[flag][0]; i < WhileBodyX[flag][1]; i++)
                for (int j = WhileBodyY[flag][0]; j < WhileBodyY[flag][1]; j++) {
                    if (Pieces[i][j] == State.Empty) continue;
                    for (int k = 1, flag0 = 1; k < m; k++) {
                        if (Pieces[i][j] == Pieces[i + get(flag, k)[0]][j + get(flag, k)[1]]) flag0++;
                        if (flag0 == m) {
                            SetWinPoint(new int[]{i + get(flag, m)[0], j + get(flag, m)[1]});
                            return Pieces[i][j];
                        }
                    }
                }
                   return State.Empty;
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
     Flag=new boolean[4][n][n];
    WhileBodyX=new int[4][2];
    WhileBodyY=new int[4][2];
     for (int i=0;i<4;i++)
     {
         WhileBodyX[i][0]=0;
         WhileBodyX[i][1]= i!=1 ? n-m-1:n;
         WhileBodyY[i][0]= i!=3 ? 0:m-1;
         WhileBodyY[i][1]= i<3&&i>0 ? n-m-1:n;
     }
     DropPoint=new int[2];
     WinPiece=new int[2];

     PointsRecord=new LinkedList<>();
     for(int i=0;i<n;i++)
         for (int j=0;j<n;j++){
         Pieces[i][j]=State.Empty;
         for (int k=0;k<4;k++)
         Flag[k][i][j]=false;
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
