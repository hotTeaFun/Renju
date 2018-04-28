package Renju;

import java.awt.*;
import java.awt.event.*;

class BeginUI extends Frame implements ActionListener,ItemListener {
private final static Choice WhoFirst=new Choice();
    private final static Choice choice=new Choice();
    BeginUI(){
        setLayout(new GridBagLayout());
        choice.add("PVE:Black vs White");
        choice.add("PVE:White vs Black");
        choice.add("PVP:Black vs White");
        choice.add("PVP:White vs Black");
        choice.add("EVE:Black vs White");
        choice.add("EVE:White vs Black");
        choice.select(0);
        WhoFirst.add("Black First");
        WhoFirst.add("White First");
        WhoFirst.select(0);
        Button Begin=new Button("Begin");
        add(WhoFirst);
        WhoFirst.addItemListener(this);
        add(choice);
        choice.addItemListener(this);
        add(Begin);
        Begin.addActionListener(this);
        addWindowListener(new WindowAdapter(){
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
    }
    @Override
    public void paint(Graphics g) {
        g.drawString("游戏开始",50,80);
        g.drawString("按键介绍响应：1(设置模式为PVP)  2(设置模式为PVE)  3(设置模式为EVE)" ,30,100);
                g.drawString("r(重启游戏)  s(交换黑白棋子)  c(投降)  e(结束游戏)",30,120);
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        Renju renju=new Renju();
        renju.Actor= WhoFirst.getSelectedIndex() != 0;
        switch (choice.getSelectedIndex()) {
            case 0:
                renju.SetModel(Renju.Model.PvE);
                renju.Statement=renju.Actor;
                break;
            case 1:
                renju.SetModel(Renju.Model.PvE);
                renju.Statement=!renju.Actor;
                break;
            case 2:
                renju.SetModel(Renju.Model.PvP);
                renju.Statement=false;
                break;
            case 3:
                renju.SetModel(Renju.Model.PvP);
                renju.Statement=false;
                break;
            case 4:
                renju.SetModel(Renju.Model.EvE);
                renju.Statement=true;
                break;
            case 5:
                renju.SetModel(Renju.Model.EvE);
                renju.Statement=true;
                break;
        }
        renju.setTitle("Renju");
        renju.setSize(new Dimension(600,600));
        renju.setVisible(true);
    }
    @Override
    public void itemStateChanged(ItemEvent e) {
        repaint();
    }
}
