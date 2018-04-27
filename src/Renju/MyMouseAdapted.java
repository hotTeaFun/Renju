package Renju;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Optional;

import static java.lang.StrictMath.pow;
import static java.lang.StrictMath.sqrt;

public class MyMouseAdapted extends MouseAdapter {
    private Renju R;
    MyMouseAdapted(Renju R){
        this.R=R;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        switch (R.model){
            case PvP:
                    if(PersonJudge(e))
                    R.LaterAct(R.getGraphics());
                    break;
            case EvE:
                    R.DropPoint=R.ComputerJudge();
                    R.LaterAct(R.getGraphics());
                    break;
            default:
                if(!R.Statement) {
                    if (PersonJudge(e))
                        R.Statement = !R.Statement;
                    else return;}
               else {
                        R.DropPoint = R.ComputerJudge();
                        R.Statement = !R.Statement;
                    }
                    R.LaterAct(R.getGraphics());
    }
    }

    private boolean PersonJudge(MouseEvent e) {
        Dimension Clicked= new Dimension(e.getY(),e.getX());
        int x=Clicked.height/R.per;
        int y=Clicked.width/R.per;
        int[] re=Decide(x,y,Clicked).get();
        if(sqrt(pow((x+re[0])*R.per-Clicked.height,2)+pow((y+re[1])*R.per-Clicked.width,2))<R.Oval){
            int X=x-2+re[0];int Y=y-2+re[1];
            if(R.Pieces[X][Y]== Renju.State.Empty){
                R.DropPoint= new int[]{X, Y};
                return true;
            }
            else return false;}
        return false;
    }

    private Optional<int[]> Decide(int x, int y, Dimension clicked) {
        int[][] c={{0,0},{0,1},{1,0},{1,1}};
       return Arrays.stream(c).parallel().reduce((a, b)->sqrt(pow((x+a[0])*R.per-clicked.height,2)+pow((y+a[1])*R.per-clicked.width,2))<=sqrt(pow((x+b[0])*R.per-clicked.height,2)+pow((y+b[1])*R.per-clicked.width,2))?a:b);
                }
    }

