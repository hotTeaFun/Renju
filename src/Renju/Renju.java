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

    private void StartAhead() {
        IsAhead = true;
        ActorBak = Actor;
        StatementBak = Statement;
        PiecesBak = Pieces.clone();
    }

    private void StopAhead() {
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
        Boolean WinFlag, For3Flag, LongForFlag, AFlush6, AAlive4, AAlive3, AFlush3, AFlush4, Two2;
        ArrayList<int[]> Forbidden, Two3P, AFlush6P, AALive4P, AFlush4P, AALive3P, AFlush3P, AFlush2P, AAlive2P, AAlive1P, Two2P;

        PiecesDetails() {
            For3Flag = WinFlag = LongForFlag = AAlive3 = AFlush3 = AAlive4 = AFlush4 = AFlush6 = Two2 = false;
            Forbidden = new ArrayList<>();
            AFlush6P = new ArrayList<>();
            AALive4P = new ArrayList<>();
            AFlush4P = new ArrayList<>();
            AALive3P = new ArrayList<>();
            AFlush3P = new ArrayList<>();
            AAlive2P = new ArrayList<>();
            AFlush2P = new ArrayList<>();
            AAlive1P = new ArrayList<>();
            Two3P = new ArrayList<>();
            Two2P = new ArrayList<>();
        }

        boolean Check(int[] check, ArrayList<int[]> Text, final int a, final int b, final int c, final int d) {
            for (int[] text : Text
                    ) {
                if (check[a] == text[c] && check[b] == text[d])
                    return false;
            }
            return true;
        }

    }

    private boolean IsLegal(int x, int y) {
        return x >= 0 && x < n && y >= 0 && y < n;
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
                                if (Actor != Forbidden || !FlagLong || !IsLegal(i + get(flag, m + 1)[0], j + get(flag, m + 1)[1]) || Pieces[i + get(flag, m + 1)[0]][j + get(flag, m + 1)[1]] != current) {
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
                            if (Ec == 2)
                                if (Pieces[i][j] == opp && Pieces[i + get(flag, 1)[0]][j + get(flag, 1)[1]] == current
                                        || Pieces[i + get(flag, m - 1)[0]][j + get(flag, m - 1)[1]] == current && Pieces[i + get(flag, m)[0]][j + get(flag, m)[1]] == opp
                                        || Pieces[i][j] == opp && (!IsLegal(i + get(flag, m + 1)[0], j + get(flag, m + 1)[1]) || Pieces[i + get(flag, m + 1)[0]][j + get(flag, m + 1)[1]] == opp)
                                        || Pieces[i + get(flag, m)[0]][j + get(flag, m)[1]] == opp && (!IsLegal(i + get(flag, -1)[0], j + get(flag, -1)[1]) || Pieces[i + get(flag, -1)[0]][j + get(flag, -1)[1]] == opp)) {
                                    piecesDetails.AFlush3 = true;
                                    for (int q = 0; q <= m; q++)
                                        if (Pieces[i + get(flag, q)[0]][j + get(flag, q)[1]] == State.Empty)
                                            piecesDetails.AFlush3P.add(new int[]{flag, i + get(flag, q)[0], j + get(flag, q)[1]});
                                }
                            if (Ec == 3 && Pieces[i][j] == State.Empty && Pieces[i + get(flag, m)[0]][j + get(flag, m)[1]] == State.Empty) {
                                piecesDetails.AAlive3 = true;
                                for (int s = 1; s < m; s++) {
                                    if (Actor == Forbidden && FlagTwoAlive3 && Pieces[i + get(flag, s)[0]][j + get(flag, s)[1]] == current)
                                        piecesDetails.Two3P.add(new int[]{flag, i + get(flag, s)[0], j + get(flag, s)[1]});
                                    if (Pieces[i + get(flag, s)[0]][j + get(flag, s)[1]] == State.Empty)
                                        piecesDetails.AALive3P.add(new int[]{flag, i + get(flag, s)[0], j + get(flag, s)[1]});
                                }
                            }
                            break;
                        case 2:
                            if (Ec == 4 && Pieces[i][j] == State.Empty && Pieces[i + get(flag, m)[0]][j + get(flag, m)[1]] == State.Empty) {
                                for (int s = 1; s < m; s++) {
                                    if (Pieces[i + get(flag, s)[0]][j + get(flag, s)[1]] == State.Empty)
                                        piecesDetails.AAlive2P.add(new int[]{flag, i + get(flag, s)[0], j + get(flag, s)[1]});
                                }
                            } else if (Ec == 3 && (Pieces[i][j] == opp && Pieces[i + get(flag, 1)[0]][j + get(flag, 1)[1]] == current || Pieces[i + get(flag, m)[0]][j + get(flag, m)[1]] == opp && Pieces[i + get(flag, m - 1)[0]][j + get(flag, m - 1)[1]] == current)) {
                                for (int s = 1; s < m; s++) {
                                    if (Pieces[i + get(flag, s)[0]][j + get(flag, s)[1]] == State.Empty)
                                        piecesDetails.AFlush2P.add(new int[]{flag, i + get(flag, s)[0], j + get(flag, s)[1]});
                                }
                            }
                            break;
                    }
                }
        if (piecesDetails.Two3P.size() > 1)
            for (int t = 0; t < piecesDetails.Two3P.size(); t++)
                for (int k = t + 1; k < piecesDetails.Two3P.size(); k++) {
                    if (piecesDetails.Two3P.get(t)[0] != piecesDetails.Two3P.get(k)[0] && piecesDetails.Two3P.get(t)[1] == piecesDetails.Two3P.get(k)[1] && piecesDetails.Two3P.get(k)[2] == piecesDetails.Two3P.get(t)[2]) {
                        if (!IsAhead)
                            ForbiddenHand();
                        else piecesDetails.For3Flag = true;
                    }
                }
        if (piecesDetails.AAlive2P.size() > 1)
            for (int t = 0; t < piecesDetails.AAlive2P.size(); t++)
                for (int k = t + 1; k < piecesDetails.AAlive2P.size(); k++) {
                    if (piecesDetails.AAlive2P.get(t)[0] != piecesDetails.AAlive2P.get(k)[0] && piecesDetails.AAlive2P.get(t)[1] == piecesDetails.AAlive2P.get(k)[1] && piecesDetails.AAlive2P.get(k)[2] == piecesDetails.AAlive2P.get(t)[2]) {
                        if (FlagTwoAlive3 && Actor == Forbidden)
                            piecesDetails.Forbidden.add(new int[]{piecesDetails.AAlive2P.get(t)[1], piecesDetails.AAlive2P.get(t)[2]});
                        else {
                            piecesDetails.Two2 = true;
                            piecesDetails.Two2P.add(new int[]{piecesDetails.AAlive2P.get(t)[1], piecesDetails.AAlive2P.get(t)[2]});
                        }
                    }
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
                                if (Pieces[i + get(flag, m)[0]][j + get(flag, m)[1]] == opp || Pieces[i][j] == opp) {
                                    for (int Symbol = 0; Symbol <= m; Symbol++)
                                        if (Pieces[i + get(flag, Symbol)[0]][j + get(flag, Symbol)[1]] == State.Empty)
                                            if (piecesDetails.Check(new int[]{i + get(flag, Symbol)[0], j + get(flag, Symbol)[1]}, piecesDetails.Forbidden, 0, 1, 0, 1)) {
                                                piecesDetails.AFlush4 = true;
                                                piecesDetails.AFlush4P.add(new int[]{i + get(flag, Symbol)[0], j + get(flag, Symbol)[1]});
                                                break;
                                            }
                                }
                            }
                            if (Ec == 2 && Pieces[i][j] == State.Empty && Pieces[i + get(flag, m)[0]][j + get(flag, m)[1]] != State.Empty && Pieces[i + get(flag, 2)[0]][j + get(flag, 2)[1]] != State.Empty) {
                                for (int Symbol = 2; Symbol < m; Symbol++)
                                    if (Pieces[i + get(flag, Symbol)[0]][j + get(flag, Symbol)[1]] == State.Empty)
                                        if (piecesDetails.Check(new int[]{i + get(flag, Symbol)[0], j + get(flag, Symbol)[1]}, piecesDetails.Forbidden, 0, 1, 0, 1)) {
                                            piecesDetails.AFlush4 = true;
                                            piecesDetails.AFlush4P.add(new int[]{i + get(flag, Symbol)[0], j + get(flag, Symbol)[1]});
                                            break;
                                        }
                            }
                            if (Ec == 2 && Pieces[i + get(flag, m)[0]][j + get(flag, m)[1]] == State.Empty && Pieces[i][j] != State.Empty && Pieces[i + get(flag, m - 1)[0]][j + get(flag, m - 1)[1]] != State.Empty) {
                                for (int Symbol = 1; Symbol < m - 1; Symbol++)
                                    if (Pieces[i + get(flag, Symbol)[0]][j + get(flag, Symbol)[1]] == State.Empty)
                                        if (piecesDetails.Check(new int[]{i + get(flag, Symbol)[0], j + get(flag, Symbol)[1]}, piecesDetails.Forbidden, 0, 1, 0, 1)) {
                                            piecesDetails.AFlush4 = true;
                                            piecesDetails.AFlush4P.add(new int[]{i + get(flag, Symbol)[0], j + get(flag, Symbol)[1]});
                                            break;
                                        }
                            }
                            break;
                        case 1:
                            if (Ec == 5 && Pieces[i][j] == State.Empty && Pieces[i + get(flag, m)[0]][j + get(flag, m)[1]] == State.Empty) {
                                for (int s = 1; s < m; s++) {
                                    if (Pieces[i + get(flag, s)[0]][j + get(flag, s)[1]] == State.Empty)
                                        piecesDetails.AAlive1P.add(new int[]{flag, i + get(flag, s)[0], j + get(flag, s)[1]});
                                }
                            }
                            break;

                    }
                }
    }

    int[] ComputerJudge(PiecesDetails piecesDetailA, PiecesDetails piecesDetailD) {
        if (PointsRecord.isEmpty()) return new int[]{7, 7};
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
        int[] AA43 = Check43(piecesDetailA, piecesDetailD);
        if (AA43[0] != -1)
            return AA43;
        if (piecesDetailD.AAlive3) {
            {
                int[] Geti = piecesDetailD.AALive3P.get(0).clone();
                boolean Tag = false;
                ArrayList<int[]> Optional = new ArrayList<>();
                int[] Arr0 = new int[]{Geti[1] + get(Geti[0], m - 1)[0], Geti[2] + get(Geti[0], m - 1)[1]};
                int[] Arr1 = new int[]{Geti[1] + get(Geti[0], 1 - m)[0], Geti[2] + get(Geti[0], 1 - m)[1]};
                if (Pieces[Geti[1] + get(Geti[0], -1)[0]][Geti[2] + get(Geti[0], -1)[1]] == State.Empty) {
                    if (!piecesDetailD.Check(Arr0, piecesDetailD.AALive3P, 0, 1, 1, 2)) {
                        Optional.add(Geti);
                        Optional.add(new int[]{Geti[0], Arr0[0], Arr0[1]});
                    } else {
                        Optional.add(Geti);
                        Optional.add(new int[]{Geti[0], Geti[1] + get(Geti[0], -1)[0], Geti[2] + get(Geti[0], -1)[1]});
                    }
                    Tag = true;
                } else if (Pieces[Geti[1] + get(Geti[0], 1)[0]][Geti[2] + get(Geti[0], 1)[1]] == State.Empty) {
                    if (!piecesDetailD.Check(Arr1, piecesDetailD.AALive3P, 0, 1, 1, 2)) {
                        Optional.add(Geti);
                        Optional.add(new int[]{Geti[0], Arr1[0], Arr1[1]});
                    } else {
                        Optional.add(Geti);
                        Optional.add(new int[]{Geti[0], Geti[1] + get(Geti[0], 1)[0], Geti[2] + get(Geti[0], 1)[1]});
                    }
                    Tag = true;
                }
                if (!Tag) {
                    if (Pieces[Geti[1] + get(Geti[0], 2)[0]][Geti[2] + get(Geti[0], 2)[1]] != State.Empty) {
                        Optional.add(Geti);
                        Optional.add(new int[]{Geti[0], Geti[1] + get(Geti[0], -2)[0], Geti[2] + get(Geti[0], -2)[1]});
                        Optional.add(new int[]{Geti[0], Geti[1] + get(Geti[0], m - 2)[0], Geti[2] + get(Geti[0], m - 2)[1]});
                    } else {
                        Optional.add(Geti);
                        Optional.add(new int[]{Geti[0], Geti[1] + get(Geti[0], 2)[0], Geti[2] + get(Geti[0], 2)[1]});
                        Optional.add(new int[]{Geti[0], Geti[1] + get(Geti[0], 2 - m)[0], Geti[2] + get(Geti[0], 2 - m)[1]});
                    }
                }
                {
                    for (int[] aOptional : Optional)
                        if (!piecesDetailD.Check(aOptional, piecesDetailD.AFlush3P, 1, 2, 1, 2) || !piecesDetailD.Check(aOptional, piecesDetailD.AAlive2P, 1, 2, 1, 2))
                            return new int[]{aOptional[1], aOptional[2]};
                    for (int[] aOptional : Optional)
                        if (!piecesDetailD.Check(aOptional, piecesDetailA.AAlive2P, 1, 2, 1, 2))
                            return new int[]{aOptional[1], aOptional[2]};
                    for (int[] aOptional : Optional)
                        if (!piecesDetailD.Check(aOptional, piecesDetailD.AAlive2P, 1, 2, 1, 2) || !piecesDetailD.Check(aOptional, piecesDetailA.AAlive1P, 1, 2, 1, 2) || !piecesDetailD.Check(aOptional, piecesDetailD.AAlive1P, 1, 2, 1, 2))
                            return new int[]{aOptional[1], aOptional[2]};
                    if (Tag) return JudgeSum(Optional.get(0), Optional.get(1));
                    else return new int[]{Optional.get(0)[1], Optional.get(0)[2]};

                }
            }
        }
        int[] AD43 = Check43(piecesDetailD, piecesDetailA);
        if (AD43[0] != -1)
            return AD43;
        int[] AA33 = Check33(Actor, piecesDetailA);
        if (AA33[0] != -1) {
            return AA33;
        }
        int[] AD33 = Check33(!Actor, piecesDetailD);
        if (AD33[0] != -1) {
            return AD33;
        }
        int[] PreAA = CheckPre(Actor, piecesDetailA);
        if (PreAA[0] != -1)
            return PreAA;
//        int[] AA2 = Check2(Actor,piecesDetailA.AAlive2P, piecesDetailA);
//        if (AA2[0] != -1) {
//            return AA2;
//        }
        int[] PreAD = CheckPre(!Actor, piecesDetailD);
        if (PreAD[0] != -1)
            return PreAD;
        if (piecesDetailD.AAlive2P.size()>=piecesDetailA.AAlive2P.size()) {
            int[] AD2 = Check2(!Actor, true, piecesDetailD.AAlive2P);
            if (AD2[0] != -1)
                return AD2;
        }
        else {
            int[] AA2 = Check2(Actor, false, piecesDetailA.AAlive2P);
            if (AA2[0] != -1)
                return AA2;
        }

//        int[] AA1 = Check2(Actor, piecesDetailA.AAlive1P);
//        if (AA1[0] != -1) {
//            return AA1;
//        }
        ArrayList<int[]> Last = GetNear(PointsRecord.getLast());
        return Last.get((int)(Math.random()*Last.size()));
    }

    private int[] CheckPre(boolean Actor, PiecesDetails piecesDetailA) {
        final short Max=10;
        if (piecesDetailA.AAlive1P.size() > 0&&piecesDetailA.AAlive2P.size()<=Max) {
            StartAhead();
            for (int[] i : piecesDetailA.AAlive1P) {
                Pieces[i[1]][i[2]] = Actor ? State.White : State.Black;
                PiecesDetails AheadA = new PiecesDetails();
                PiecesDetails AheadD = new PiecesDetails();
                PreScan(AheadA, Actor, Pieces);
                PreScan(AheadD, !Actor, Pieces);
                int[] A43 = Check43(AheadA, AheadD);
                int[] A33 = Check33(Actor,AheadA);
                Pieces[i[1]][i[2]] =State.Empty;
                if (A43[0] != -1){
                    StopAhead();
                    return A43;
                }
                if (A33[0] != -1){
                    StopAhead();
                    return A33;
                }
            }
            StopAhead();
        }
        return new int[]{-1,-1};
    }

    private int[] Check2(boolean Actor,boolean judge, ArrayList<int[]> L) {
        ArrayList<Integer> List = new ArrayList<>();
        if (L.size() > 0) {
            //   int Pre=L.size();
            StartAhead();
            for (int[] aL : L) {
                Pieces[aL[1]][aL[2]] = !Actor ? State.White : State.Black;
                PiecesDetails Ahead = new PiecesDetails();
                PreScan(Ahead, Actor, Pieces);
                List.add(Ahead.AAlive2P.size());
                Pieces[aL[1]][aL[2]] = State.Empty;
            }
            StopAhead();
            return new int[]{L.get(GetM(List,judge))[1], L.get(GetM(List,judge))[2]};
        }
        return new int[]{-1, -1};
    }

    private int GetM(ArrayList<Integer> list,boolean judge) {
        int m = list.get(0);
        int re = 0;
        for (int i = 0; i < list.size(); i++) {
            if (judge) {
                if (m > list.get(i)) {
                    re = i;
                    m = list.get(i);
                }
            }
            else {
                if (m < list.get(i)) {
                    re = i;
                    m = list.get(i);
                }
            }
        }
        return re;
    }

    private int[] Check33(boolean Actor, PiecesDetails piecesDetails) {
        if (!FlagTwoAlive3 || (Actor != Forbidden)) {
            if (piecesDetails.Two2) return piecesDetails.Two2P.get(0);
        }
        return new int[]{-1, -1};
    }

    private int[] Check43(PiecesDetails piecesDetailA, PiecesDetails piecesDetailD) {
        if (piecesDetailA.AFlush3)
            for (int k = 0; k < piecesDetailA.AFlush3P.size(); k += 2) {
                int[] GetK = new int[]{piecesDetailA.AFlush3P.get(k)[1], piecesDetailA.AFlush3P.get(k)[2]};
                int[] GetK1 = new int[]{piecesDetailA.AFlush3P.get(k + 1)[1], piecesDetailA.AFlush3P.get(k + 1)[2]};
                if (piecesDetailA.Check(GetK, piecesDetailA.Forbidden, 0, 1, 0, 1)) {
                    if (!piecesDetailA.Check(GetK, piecesDetailA.AAlive2P, 0, 1, 1, 2))
                        if (piecesDetailD.Check(GetK1, piecesDetailD.AALive3P, 0, 1, 1, 2) && piecesDetailD.Check(GetK1, piecesDetailD.AFlush3P, 0, 1, 1, 2))
                            return GetK;
                }
                if (piecesDetailA.Check(GetK1, piecesDetailA.Forbidden, 0, 1, 0, 1)) {
                    if (!piecesDetailA.Check(GetK1, piecesDetailA.AAlive2P, 0, 1, 1, 2))
                        if (piecesDetailD.Check(GetK, piecesDetailD.AALive3P, 0, 1, 1, 2) && piecesDetailD.Check(GetK, piecesDetailD.AFlush3P, 0, 1, 1, 2))
                            return GetK1;
                }
            }
        return new int[]{-1, -1};
    }

    private int[] JudgeSum(int[] ints, int[] ints1) {
        int countB = 0;
        int countW = 0;
        int cB = 0;
        int cW = 0;
        int[] List = new int[]{-1, 0, 1};
        for (int i : List)
            for (int j : List) {
                if (IsLegal(ints[1] + i, ints[2] + j))
                    switch (Pieces[ints[1] + i][ints[2] + j]) {
                        case Black:
                            countB++;
                        case White:
                            countW++;
                    }
                if (IsLegal(ints1[1] + i, ints1[2] + j))
                    switch (Pieces[ints1[1] + i][ints1[2] + j]) {
                        case Black:
                            cB++;
                        case White:
                            cW++;
                    }
            }
        return Math.abs(countB - countW) >= Math.abs(cB - cW) ? new int[]{ints[1], ints[2]} : new int[]{ints1[1], ints1[2]};
    }

    private ArrayList<int[]> GetNear(int[] last) {
        ArrayList<int[]> re = new ArrayList<>();
        int[] List = new int[]{-1, 0, 1};
        for (int i : List)
            for (int j : List)
                if (IsLegal(last[1] + i, last[2] + j) && Pieces[last[1] + i][last[2] + j] == State.Empty)
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
