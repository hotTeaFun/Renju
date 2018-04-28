package Renju;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

class MyKeyAdapted extends KeyAdapter {
    private Renju renju;

    MyKeyAdapted(Renju renju) {
        this.renju = renju;
    }

    @Override
    public void keyTyped(KeyEvent e) {
        char Typed = e.getKeyChar();
        switch (Typed) {
            case 'r':renju.dispose();
                BeginUI Begin=new BeginUI();
                Begin.setTitle("The Begin Page");
                Begin.setSize(new Dimension(600,600));
                Begin.setVisible(true);
                break;
            case 'e':System.exit(0);
            case 'c':renju.Capitulation();break;
            case 's':renju.Swap();break;
            case 'u':renju.Undo();break;
            case '1':renju.SetModel(Renju.Model.PvP); break;
            case '2':renju.SetModel(Renju.Model.PvE);break;
            case '3':renju.SetModel(Renju.Model.EvE);break;
            case '4':renju.SetFlagLong(true);break;
            case '5':renju.SetFlagLong(false);break;
            case '6':renju.SetFlagTwoAlive3(true);break;
            case '7':renju.SetFlagTwoAlive3(false);break;
        }

    }
}

