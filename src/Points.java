import sun.swing.icon.SortArrowIcon;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.io.*;
import java.util.ArrayList;

public class Points extends JPanel
{
    public static final int Width = 1280, Height = 960, Radius = 300, TextHeight = 25;

    public Trigger trigger;
    public center c;

    Points(Trigger _trigger, center _c)
    {
        this.setBackground(Color.black);
        trigger = _trigger;
        c = _c;
//to be removed
//        JLabel jLabel = new JLabel();
//        jLabel.setText("aaa");
//        jLabel.setForeground(Color.BLACK);
//        jLabel.setHorizontalTextPosition(SwingConstants.CENTER);
//        jLabel.setVerticalTextPosition(SwingConstants.CENTER);
//        this.add(jLabel);

    }
    public void paint(Graphics g)
    {
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, getWidth(), getHeight());
//        JTextPane jTextPane = new JTextPane();
//        jTextPane.setText("Circle");
//        this.add(jTextPane);

        super.paint(g);
    }


    public void paintComponent(Graphics g0) {
        this.setBackground(Color.black);
//        g0.clearRect(0,0, Points.Width, Points.Height);
        Graphics2D g = (Graphics2D)(g0);
        int logHeight = 100;
//        g.setColor(Color.yellow);

//        g.setColor(Color.BLACK);
//        g.fill(new Ellipse2D.Double(960-80,540-80,160,160));

//        JLabel jLabel = new JLabel();
//
//        jLabel.setText("bbb");
//        jLabel.setForeground(Color.BLACK);
//        jLabel.setHorizontalTextPosition(SwingConstants.CENTER);
//        jLabel.setVerticalTextPosition(SwingConstants.CENTER);
//
//        this.add(jLabel);

        String temp = null;
        ImageIcon imageServer = new ImageIcon("server.png");
        ImageIcon imageMessage = new ImageIcon("message.png");
        int cx = Width / 2 - imageServer.getIconWidth() / 2;
        int cy = Height / 2 - imageServer.getIconHeight() / 2;

        g.drawImage(imageServer.getImage(), cx, cy, this);
        g.drawString("OuterInput", cx, Height / 2 + imageServer.getIconHeight());

        synchronized (trigger.flag) {
            g.drawString(Integer.toString(trigger.readers.size()), Width / 4 * 3, logHeight - 20);
            for (int i : trigger.readers.keySet()) {
                BufferedReader bufferedReader = trigger.readers.get(i);
                try {
                    temp = bufferedReader.readLine();
//                g.drawString("nimade", Width / 4 * 3, logHeight);

                    if (temp != null) {
//                    System.out.println(temp);
//                    g.drawString("weishenme", Width / 4 * 3, logHeight + 20);
                        switch (temp) {
                            case "start":
                                trigger.curLogs.put(System.currentTimeMillis(), new DataLog(i, i, temp, null));
                                break;
                            case "HeartBeat":
                                break;
                            case "test":
                                String next = bufferedReader.readLine();
                                System.out.println("test" + next);
                                trigger.curLogs.put(System.currentTimeMillis(), new DataLog(i, i, "test", next));
                                break;
                            case "route":
                                String query = bufferedReader.readLine();
                                String dst = bufferedReader.readLine();
                                trigger.curLogs.put(System.currentTimeMillis(),
                                        new DataLog(i, Integer.parseInt(dst), temp, query));
                                break;
                            case "exit":
                                trigger.curLogs.put(System.currentTimeMillis(), new DataLog(i, i, "exit", null));
                                break;
                        }
                    }
//                else System.out.println("ccc");

                } catch (IOException e) {
                    e.printStackTrace();
                }
//            System.out.println(i);
                double angle = (double) i / (double) hash.hashMAX * Math.PI * 2;
                double lx = (double) cx + Radius * Math.cos(angle);
                double ty = (double) cy + Radius * Math.sin(angle);

                g.drawImage(imageServer.getImage(), (int) lx, (int) ty, this);
                g.drawString(Integer.toString(i), (int) lx, (int) ty + imageServer.getIconHeight() + 20);

//            Line2D line2D = new Line2D.Double(cx, cy, lx, ty);
//            g.drawLine(cx, cy, (int)lx, (int)ty);
            }

            //to be started
        long curTime = System.currentTimeMillis();
        for(long i : trigger.curLogs.keySet())
        {
            if(curTime - i <=  Trigger.livePeriod) continue;
            trigger.pastLog.put(i, trigger.curLogs.get(i));
            trigger.curLogs.remove(i);
        }
            if (trigger.refresh)
            {
                trigger.pastLog.putAll(trigger.curLogs);
                trigger.curLogs.clear();
                trigger.refresh = false;
            }
            for (long i : trigger.curLogs.keySet()) {
                logHeight = logHeight + TextHeight;
                DataLog dataLog = trigger.curLogs.get(i);
                if(dataLog.trans.equals("exit"))
                {
                    trigger.idMaps.remove(dataLog.src);
                    trigger.readers.remove(dataLog.src);
                }
                g.drawString(trigger.curLogs.get(i).getOutputLabel(), Width / 4 * 3, logHeight);
            }
//        g.drawString("nimade", Width / 4 * 3, logHeight);

//        Ellipse2D.Double q = new Ellipse2D.Double(960-80,540-80,160,160);
//        g.fill(q);
//
//        Line2D.Double l = new Line2D.Double();
//        l.setLine(100, 100, 200, 200);

//        g.draw(l);
        }
        repaint();
    }
}
