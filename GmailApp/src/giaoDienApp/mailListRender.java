/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package giaoDienApp;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

import gmailApi.MessageObject;

/**
 *
 * @author NTA
 */
public class mailListRender extends JPanel implements ListCellRenderer<MessageObject> {

    JLabel lb_list_mailfrom = new JLabel();
    JLabel lb_list_maildate = new JLabel();
    JLabel lb_list_mailcc = new JLabel();
    JLabel lb_list_avt = new JLabel();

    public mailListRender() {
        setLayout(new BorderLayout(5, 5));
        JPanel pl_list_mail = new JPanel(new GridLayout(0, 1));
        pl_list_mail.add(lb_list_mailfrom);
        pl_list_mail.add(lb_list_mailcc);
        add(lb_list_avt, BorderLayout.WEST);
        add(pl_list_mail, BorderLayout.CENTER);
        add(lb_list_maildate, BorderLayout.LINE_END);

    }

    @Override
    public Component getListCellRendererComponent(JList<? extends MessageObject> list, MessageObject demo, int index, boolean isSelected, boolean cellHasFocus) {
//        String url = "/manager_mail/src/avt/" + demo.getAvtName() + ".png";
        lb_list_mailfrom.setForeground(Color.blue);
        lb_list_mailcc.setForeground(Color.darkGray);
//        lb_list_avt.setIcon(new ImageIcon((url)));
        lb_list_mailcc.setText(demo.cc);
        lb_list_maildate.setText(demo.date);
        lb_list_mailfrom.setText(demo.from);

        lb_list_mailcc.setOpaque(true);
        lb_list_avt.setOpaque(true);
        lb_list_maildate.setOpaque(true);
        lb_list_mailfrom.setOpaque(true);

        // when select item
        if (isSelected) {
            lb_list_avt.setBackground(new java.awt.Color(39, 129, 191));
            lb_list_mailcc.setBackground(new java.awt.Color(39, 129, 191));
            lb_list_maildate.setBackground(new java.awt.Color(39, 129, 191));
            lb_list_mailfrom.setBackground(new java.awt.Color(39, 129, 191));
            setBackground(new java.awt.Color(39, 129, 191));
        } else { // when don't select
            lb_list_avt.setBackground(list.getSelectionBackground());
            lb_list_mailcc.setBackground(list.getSelectionBackground());
            lb_list_maildate.setBackground(list.getSelectionBackground());
            lb_list_mailfrom.setBackground(list.getSelectionBackground());
            setBackground(list.getSelectionBackground());
        }
        return this;
    }

}
