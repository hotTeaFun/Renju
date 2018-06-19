package Renju;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

import static java.lang.Integer.min;

public class Renju extends Frame {
    private final static Color BLACK = new Color(1);
    private final static Color WHITE = new Color(0xDFE1D7);
    private final static int m = 5;//储存五子棋的5
    private final static int n = 15;//棋盘行列数（标准15×15）
    private static int[][] HasX;
    private static int[][] HasY;

    enum Model {PvP, PvE, EvE}//模式(分别对应人人，人机，机机)

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

    enum State {Empty, Black, White}//对应三种不同状态

    State[][] Pieces;//储存棋盘状态
    int[][] PiecesInt;//将棋盘状态传入ｊｎｉ的媒介（0==Empty;-1==White;1==Black）
    private boolean IsAhead;//标志电脑是否在向前演算
    private State[][] PiecesBak;
    private boolean ActorBak;
    private boolean StatementBak;

    static {
        System.loadLibrary("AI");
    }

    public static native int[] AI();

    void StartAhead() {
        IsAhead = true;
        ActorBak = Actor;
        StatementBak = Statement;
        PiecesBak = Pieces.clone();
    }

    void StopAhead() {
        IsAhead = false;
        Actor = ActorBak;
        Statement = StatementBak;
        Pieces = PiecesBak.clone();
    }

    public static void main(String[] args) {
        BeginUI Begin = new BeginUI();
        Begin.setTitle("The Begin Page");
        Begin.setSize(new Dimension(600, 600));
        Begin.setVisible(true);
    }

    Renju() {
        setBackground(new Color(136, 97, 35));
        Pieces = new State[n][n];
        PiecesBak = new State[n][n];
        HasX = new int[4][2];
        HasY = new int[4][2];
        for (int i = 0; i < 4; i++) {
            HasX[i][0] = 0;
            HasX[i][1] = i != 1 ? n - m : n;
            HasY[i][0] = i != 3 ? 0 : m;
            HasY[i][1] = i < 3 && i > 0 ? n - m : n;
        }
        DropPoint = new int[2];
        PointsRecord = new LinkedList<>();
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++) {
                Pieces[i][j] = State.Empty;
            }
        model = Model.PvP;//默认模式为人机
        Actor = false;//默认先手为黑方
        IsAhead = false;
        Statement = true;
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

    void LaterAct(Graphics g, PiecesDetails piecesDetailA) {
        g.setColor(Actor ? WHITE : BLACK);
        g.fillOval((2 + DropPoint[0]) * per - Oval, (2 + DropPoint[1]) * per - Oval, Oval * 2, Oval * 2);
        Pieces[DropPoint[0]][DropPoint[1]] = Actor ? Renju.State.White : Renju.State.Black;
        PreScan(piecesDetailA, Actor, Pieces);
//        PreScan(piecesDetailD,!Actor);
        int[] Current = {Actor ? 1 : 0, DropPoint[0], DropPoint[1]};
        if (PointsRecord.size() + 1 == n * n) {
            EndUI endUI = new EndUI(this, "Nobody");
            endUI.setTitle("filled Pieces");
            endUI.setSize(new Dimension(400, 400));
            endUI.setVisible(true);
        }
        PointsRecord.add(Current);
        Actor = !Actor;
    }

    void SetFlagLong(boolean FlagLong) {
        this.FlagLong = FlagLong;
    }

    void SetFlagTwoAlive3(boolean FlagTwoAlive3) {
        this.FlagTwoAlive3 = FlagTwoAlive3;
    }

    void SetModel(Model model) {
        this.model = model;
    }

    //储存扫描棋盘后的详细信息
    class PiecesDetails {
        Boolean WinFlag, LongForFlag, AFlush6, AAlive4, AAlive3, AFlush3, AFlush4;
        ArrayList<int[]> Forbidden, Two3P, AFlush6P, AALive4P, AFlush4P, AALive3P, AFlush3P,AFlush2P, AAlive2P, AAlive1P;

        PiecesDetails() {
            WinFlag = LongForFlag = AAlive3 = AFlush3 = AAlive4 = AFlush4 = AFlush6 = false;
            Forbidden = new ArrayList<>();
            AFlush6P = new ArrayList<>();
            AALive4P = new ArrayList<>();
            AFlush4P = new ArrayList<>();
            AALive3P = new ArrayList<>();
            AFlush3P = new ArrayList<>();
            AAlive2P = new ArrayList<>();
            AFlush2P=new ArrayList<>();
            AAlive1P = new ArrayList<>();
            Two3P = new ArrayList<>();
        }

        boolean Check(int[] check, ArrayList<int[]> Text, final int a, final int b, final int c, final int d) {
            for (int[] text : Text
                    ) {
                if (text[a] == check[c] && text[b] == check[d])
                    return false;
            }
            return true;
        }

        boolean Check32(int[] check, ArrayList<int[]> Text) {
            for (int[] text : Text
                    ) {
                if (text[0] == check[0] && text[1] == check[1])
                    return false;
            }
            return true;
        }

        boolean Check3(int[] check, ArrayList<int[]> Text) {
            for (int[] text : Text
                    ) {
                if (text[0] != check[0] && text[1] == check[1] && text[2] == check[2])
                    return false;
            }
            return true;
        }
    }

    private void PreScan(PiecesDetails piecesDetails, boolean Actor, final State[][] Pieces) {
        State current = Actor ? State.White : State.Black;
        State opp = !Actor ? State.White : State.Black;
        for (int flag = 0; flag < 4; flag++)
            for (int i = HasX[flag][0]; i < HasX[flag][1]; i++)
                for (int j = HasY[flag][0]; j < HasY[flag][1]; j++) {
                    int Cc = 0, Ec = 0;
                    for (int f = 0; f <= m; f++) {
                        if (Pieces[i + get(flag, f)[0]][j + get(flag, f)[1]] == current)
                            Cc++;
                        else if (Pieces[i + get(flag, f)[0]][j + get(flag, f)[1]] == State.Empty)
                            Ec++;
                    }
                    switch (Cc) {
                        case 6:
                            if (Actor == Forbidden && FlagLong) {
                                if (!IsAhead) ForbiddenHand();
                                else piecesDetails.LongForFlag = true;
                            } else {
                                if (!IsAhead) GameOver(current);
                                else piecesDetails.WinFlag = true;
                            }
                            break;
                        case 5:
                            if (Pieces[i][j] != current) {
                                if (Actor != Forbidden || !FlagLong || i + get(flag, m + 1)[0] == n + 1 || j + get(flag, m + 1)[1] == -1 || j + get(flag, m + 1)[1] == n + 1 || Pieces[i + get(flag, m + 1)[0]][j + get(flag, m + 1)[1]] != current) {
                                    if (!IsAhead) GameOver(current);
                                    else piecesDetails.WinFlag = true;
                                } else {
                                    if (!IsAhead) ForbiddenHand();
                                    else piecesDetails.LongForFlag = true;
                                }
                            }
                            if (Pieces[i + get(flag, m)[0]][j + get(flag, m)[1]] != current) {
                                if (!IsAhead) GameOver(current);
                                else piecesDetails.WinFlag = true;
                            }
                            if (Pieces[i][j] == current && Pieces[i + get(flag, m)[0]][j + get(flag, m)[1]] == current && Ec == 1) {
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
                            }
                            break;
                        case 3:
                            switch (Ec) {

                                case 2:
                                    if (Pieces[i][j] != opp && Pieces[i + get(flag, m)[0]][j + get(flag, m)[1]] != opp)
                                        break;
                                    else {
                                        piecesDetails.AFlush3 = true;
                                        for (int q = 0; q <= m; q++)
                                            if (Pieces[i + get(flag, q)[0]][j + get(flag, q)[1]] == State.Empty)
                                                piecesDetails.AFlush3P.add(new int[]{flag, i + get(flag, q)[0], j + get(flag, q)[1]});
                                        break;
                                    }
                                case 3:
                                    if (Pieces[i][j] == State.Empty && Pieces[i + get(flag, m)[0]][j + get(flag, m)[1]] == State.Empty) {
                                        piecesDetails.AAlive3 = true;
                                        for (int s = 1; s < m; s++) {
                                            if (Actor == Forbidden && FlagTwoAlive3 && Pieces[i + get(flag, s)[0]][j + get(flag, s)[1]] == current)
                                                piecesDetails.Two3P.add(new int[]{flag, i + get(flag, s)[0], j + get(flag, s)[1]});
                                            if (Pieces[i + get(flag, s)[0]][j + get(flag, s)[1]] == State.Empty)
//                                                if (piecesDetails.Check3(new int[]{flag, i + get(flag, s)[0], j + get(flag, s)[1]}, piecesDetails.AALive3P))
                                                piecesDetails.AALive3P.add(new int[]{flag, i + get(flag, s)[0], j + get(flag, s)[1]});
                                        }
                                    }
                                    break;
                            }
                    }
                }
        if (piecesDetails.Two3P.size() > 1)
            for (int t = 0; t < piecesDetails.Two3P.size(); t++)
                for (int k = t + 1; k < piecesDetails.Two3P.size(); k++) {
                    if (piecesDetails.Two3P.get(t)[0] != piecesDetails.Two3P.get(k)[0] && piecesDetails.Two3P.get(t)[1] == piecesDetails.Two3P.get(k)[1] && piecesDetails.Two3P.get(k)[2] == piecesDetails.Two3P.get(t)[2])
                        ForbiddenHand();
                }
    }

    private void ComputerScan(PiecesDetails piecesDetails, boolean Actor, final State[][] Pieces) {
        State current = Actor ? State.White : State.Black;
        State opp = !Actor ? State.White : State.Black;
        for (int flag = 0; flag < 4; flag++)
            for (int i = HasX[flag][0]; i < HasX[flag][1]; i++)
                for (int j = HasY[flag][0]; j < HasY[flag][1]; j++) {
                    int Cc = 0, Ec = 0;
                    for (int f = 0; f <= m; f++) {
                        if (Pieces[i + get(flag, f)[0]][j + get(flag, f)[1]] == current)
                            Cc++;
                        else if (Pieces[i + get(flag, f)[0]][j + get(flag, f)[1]] == State.Empty)
                            Ec++;

                    }
                    switch (Cc) {

                        case 4:
                            if (Pieces[i][j] == State.Empty && Pieces[i + get(flag, m)[0]][j + get(flag, m)[1]] == State.Empty) {
                                if (Actor == Forbidden && FlagLong) {
                                    if (piecesDetails.Check(new int[]{i, j}, piecesDetails.Forbidden, 0, 1, 0, 1)) {
                                        if (piecesDetails.Check(new int[]{i + get(flag, m)[0], j + get(flag, m)[1]}, piecesDetails.Forbidden, 0, 1, 0, 1)) {
                                            piecesDetails.AAlive4 = true;
                                            piecesDetails.AALive4P.add(new int[]{i, j});
                                            piecesDetails.AALive4P.add(new int[]{i + get(flag, m)[0], i + get(flag, m)[1]});
                                            break;
                                        } else {
                                            piecesDetails.AFlush4 = true;
                                            piecesDetails.AFlush4P.add(new int[]{i, j});
                                            break;
                                        }
                                    } else if (piecesDetails.Check(new int[]{i + get(flag, m)[0], j + get(flag, m)[1]}, piecesDetails.Forbidden, 0, 1, 0, 1)) {
                                        piecesDetails.AFlush4 = true;
                                        piecesDetails.AFlush4P.add(new int[]{i + get(flag, m)[0], j + get(flag, m)[1]});
                                        break;
                                    }
                                } else {
                                    piecesDetails.AAlive4 = true;
                                    piecesDetails.AALive4P.add(new int[]{i, j});
                                    piecesDetails.AALive4P.add(new int[]{i + get(flag, m)[0], i + get(flag, m)[1]});
                                    break;
                                }
                            } else if (Ec == 1) {
                                if (Pieces[i + get(flag, m)[0]][i + get(flag, m)[1]] == opp || Pieces[i][j] == opp) {
                                    int Symbol = -1;
                                    while (++Symbol <= m)
                                        if (Pieces[i + get(flag, Symbol)[0]][j + get(flag, Symbol)[1]] == State.Empty)
                                            break;
                                    if (Actor != Forbidden || !FlagLong || piecesDetails.Check(new int[]{i + get(flag, Symbol)[0], j + get(flag, Symbol)[1]}, piecesDetails.Forbidden, 0, 1, 0, 1)) {
                                        piecesDetails.AFlush4 = true;
                                        piecesDetails.AFlush4P.add(new int[]{i + get(flag, Symbol)[0], j + get(flag, Symbol)[1]});
                                    }
                                }
                            }
                            break;
                        case 2:
                            if (Ec == 4 && Pieces[i][j] == State.Empty && Pieces[i + get(flag, m)[0]][j + get(flag, m)[1]] == State.Empty) {
                                for (int s = 1; s < m; s++) {
                                    if (Pieces[i + get(flag, s)[0]][j + get(flag, s)[1]] == State.Empty)
                                        piecesDetails.AFlush2P.add(new int[]{flag, i + get(flag, s)[0], j + get(flag, s)[1]});
                                }
                            }
                            else if (Ec==3&&(Pieces[i][j]==opp&&Pieces[i+get(flag,1)[0]][j+get(flag,1)[1]]==current||Pieces[i + get(flag, m)[0]][j + get(flag, m)[1]] == opp&&Pieces[i + get(flag, m-1)[0]][j + get(flag, m-1)[1]] == current)){  for (int s = 1; s < m; s++) {
                                if (Pieces[i + get(flag, s)[0]][j + get(flag, s)[1]] == State.Empty)
                                    piecesDetails.AAlive2P.add(new int[]{flag, i + get(flag, s)[0], j + get(flag, s)[1]});
                            }
                            }
                        case 1:
                            if (Ec == 5 && Pieces[i][j] == State.Empty && Pieces[i + get(flag, m)[0]][j + get(flag, m)[1]] == State.Empty) {
                                for (int s = 1; s < m; s++) {
                                    if (Pieces[i + get(flag, s)[0]][j + get(flag, s)[1]] == State.Empty)
                                        piecesDetails.AAlive1P.add(new int[]{flag, i + get(flag, s)[0], j + get(flag, s)[1]});
                                }
                            }

                    }
                }
    }

    int[] ComputerJudge(PiecesDetails piecesDetailA, PiecesDetails piecesDetailD) {
        PreScan(piecesDetailA, Actor, Pieces);
        PreScan(piecesDetailD, !Actor, Pieces);
        ComputerScan(piecesDetailA, Actor, Pieces);
        ComputerScan(piecesDetailD, !Actor, Pieces);
        if (piecesDetailA.AFlush6) return piecesDetailA.AFlush6P.get(0);
        if (piecesDetailA.AFlush4) return piecesDetailA.AFlush4P.get(0);
        if (piecesDetailA.AAlive4) return piecesDetailA.AALive4P.get(0);
        if (piecesDetailD.AFlush6) return piecesDetailD.AFlush6P.get(0);
        if (piecesDetailD.AFlush4) return piecesDetailD.AFlush4P.get(0);
        if (piecesDetailD.AAlive4) return piecesDetailD.AALive4P.get(0);
        if (piecesDetailA.AAlive3)
            for (int i = 0; i < piecesDetailA.AALive3P.size(); i++) {
                int[] bak = new int[]{piecesDetailA.AALive3P.get(i)[1], piecesDetailA.AALive3P.get(i)[2]};
                if (piecesDetailA.Check(bak, piecesDetailA.Forbidden, 0, 1, 0, 1))
                    return bak;
            }
        if (piecesDetailA.AFlush3) {
            for (int k = 0; k < piecesDetailA.AFlush3P.size(); k += 2) {
                int[] GetK = new int[]{piecesDetailA.AFlush3P.get(k)[1], piecesDetailA.AFlush3P.get(k)[2]};
                int[] GetK1 = new int[]{piecesDetailA.AFlush3P.get(k + 1)[1], piecesDetailA.AFlush3P.get(k + 1)[2]};
                if (piecesDetailA.Check(GetK, piecesDetailA.Forbidden, 0, 1, 0, 1)) {
                    if (!piecesDetailA.Check(GetK, piecesDetailA.AAlive2P, 0, 1, 1, 2))
                        if (piecesDetailD.Check(GetK1, piecesDetailD.AALive3P, 0, 1, 1, 2) || piecesDetailD.Check(GetK1, piecesDetailD.AFlush3P, 0, 1, 1, 2))
                            return GetK;
                }
                if (piecesDetailA.Check(GetK1, piecesDetailA.Forbidden, 0, 1, 0, 1)) {
                    if (!piecesDetailA.Check(GetK1, piecesDetailA.AAlive2P, 0, 1, 1, 2))
                        if (piecesDetailD.Check(GetK, piecesDetailD.AALive3P, 0, 1, 1, 2) || piecesDetailD.Check(GetK, piecesDetailD.AFlush3P, 0, 1, 1, 2))
                            return GetK1;
                }
            }
        }
        if (piecesDetailD.AAlive3) {
            if (!piecesDetailA.AFlush3)
                for (int i = 0; i < piecesDetailD.AALive3P.size(); i++) {
                int[] Geti=new int[]{piecesDetailD.AALive3P.get(i)[1],piecesDetailD.AALive3P.get(i)[2]};
                    if (piecesDetailD.Check(Geti, piecesDetailD.Forbidden,0,1,0,1))
                        if (!piecesDetailD.Check(Geti,piecesDetailD.AFlush3P,0,1,1,2)||!piecesDetailD.Check3(piecesDetailD.AALive3P.get(i),piecesDetailD.AALive3P)||!piecesDetailD.Check3(piecesDetailD.AALive3P.get(i),piecesDetailD.AAlive2P))
                            return Geti;

                }
            else {
            }
        }
        ArrayList<int[]> Last = GetNear(PointsRecord.getLast());
        return Last.get((int) (Math.random() * Last.size()));
    }

    private ArrayList<int[]> GetNear(int[] last) {
        ArrayList<int[]> re = new ArrayList<>();
        int[] List = new int[]{-1, 0, 1};
        for (int i : List)
            for (int j : List)
                if (last[1] + i >= 0 && last[1] + i < n && last[2] + j >= 0 && last[2] + j < n && Pieces[last[1] + i][last[2] + j] == State.Empty)
                    re.add(new int[]{last[1] + i, last[2] + j});
        return re;
    }


    private void GameOver(Renju.State winner) {
        EndUI endUI = new EndUI(this, winner == State.Black ? "Black" : "White");
        endUI.setTitle("GameOver");
        endUI.setSize(new Dimension(400, 400));
        endUI.setVisible(true);
    }

    @Override
    public void paint(Graphics g) {
        //储存窗口尺寸
        Dimension dimension = getSize();
        //储存窗口高宽的较小值
        int minimum = min(dimension.height, dimension.width);
        per = minimum / (n + 3);
        Oval = per / 3;
        for (int i = 2; i < n + 2; i++) {
            g.drawLine(per * 2, i * per, (n + 1) * per, i * per);
            g.drawLine(i * per, per * 2, i * per, (n + 1) * per);
        }
        PaintPieces(g);
    }

    private void PaintPieces(Graphics g) {
        for (int x = 0; x < n; x++)
            for (int y = 0; y < n; y++)
                if (Pieces[x][y] != State.Empty) {
                    g.setColor(Pieces[x][y] == State.White ? WHITE : BLACK);
                    g.fillOval((2 + x) * per - Oval, (2 + y) * per - Oval, Oval * 2, Oval * 2);
                }
    }

    private int[] get(int flag, int arg) {
        int[] re = new int[2];
        switch (flag) {
            case 0:
                re = new int[]{arg, 0};
                break;
            case 1:
                re = new int[]{0, arg};
                break;
            case 2:
                re = new int[]{arg, arg};
                break;
            case 3:
                re = new int[]{arg, -arg};
                break;
        }
        return re;
    }

    void Swap() {
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                if (Pieces[i][j] != State.Empty)
                    Pieces[i][j] = Pieces[i][j] == State.White ? State.Black : State.White;
        Actor = !Actor;
        Forbidden = !Forbidden;
        PaintPieces(this.getGraphics());
        for (int[] change : PointsRecord)
            change[0] = change[0] == 0 ? 1 : 0;
    }

    void Undo() {
        int[] Current;
        if (!PointsRecord.isEmpty()) {
            Current = PointsRecord.removeLast();
            Pieces[Current[1]][Current[2]] = State.Empty;
            Actor = Current[0] == 1;
            repaint();
        }
    }

    void Capitulation() {
        EndUI endUI = new EndUI(this, Actor ? "Black" : "White");
        endUI.setTitle(!Actor ? "Black capitulated" : "White capitulated");
        endUI.setSize(new Dimension(400, 400));
        endUI.setVisible(true);
    }

    private void ForbiddenHand() {
        EndUI endUI = new EndUI(this, Forbidden ? "Black" : "White");
        endUI.setTitle(!Forbidden ? "Black is in a forbidden hand" : "White is in a forbidden hand");
        endUI.setSize(new Dimension(400, 400));
        endUI.setVisible(true);
    }
}
