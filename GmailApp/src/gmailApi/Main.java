/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gmailApi;

import giaoDienApp.index;
import giaoDienApp.welcome;
import static gmailApi.LoginProcess.deleteLoginToken;
import java.io.File;

/**
 *
 * @author Admin
 */
public class Main {

    public static void main(String[] args) {
	welcome newWelcome;

	newWelcome = new welcome();
	newWelcome.lookAndFeel();
	java.awt.EventQueue.invokeLater(new Runnable() {
	    public void run() {
		newWelcome.setVisible(true);
	    }
	});
	Runtime.getRuntime().addShutdownHook(new Thread() {

	    @Override
	    public void run() {
		if (GlobalVariable.save == 0) {
		    deleteLoginToken(XulyChuoiMail.parseMail(GlobalVariable.userId));
		}
	    }
	});
    }

}
