package Renju;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
class EndUI extends Frame{
private String Winner;
    EndUI(Renju P,String Winner){
        this.Winner=Winner;
        P.dispose();
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
    }
    @Override
    public void paint(Graphics g) {
        g.drawString("GameOver!\nThe Winner is "+Winner,100,100);
    }



}