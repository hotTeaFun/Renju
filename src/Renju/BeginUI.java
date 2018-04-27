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
        g.drawString("GameBegin!\nYou're supposed to be the winner!",190,100);
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        Renju renju=new Renju();
        switch (choice.getSelectedIndex()) {
            case 0:
                renju.model = Renju.Model.PvE;
                renju.Statement=false;
                break;
            case 1:
                renju.model = Renju.Model.PvE;
                renju.Statement=true;
                break;
            case 2:
                renju.model = Renju.Model.PvP;
                renju.Statement=false;
                break;
            case 3:
                renju.model= Renju.Model.PvP;
                renju.Statement=false;
                break;
            case 4:
                renju.model= Renju.Model.EvE;
                renju.Statement=true;
                break;
            case 5:
                renju.model= Renju.Model.EvE;
                renju.Statement=true;
                break;
        }
   renju.Actor= WhoFirst.getSelectedIndex() != 0;
        renju.setTitle("Renju");
        renju.setSize(new Dimension(600,600));
        renju.setVisible(true);
    }
    @Override
    public void itemStateChanged(ItemEvent e) {
        repaint();
    }
}
