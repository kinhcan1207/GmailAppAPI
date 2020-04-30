/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package giaoDienApp;

import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import customException.WrongLoginInfoException;
import gmailApi.GlobalVariable;
import gmailApi.LoginProcess;
import gmailApi.MessageObject;
import gmailApi.MessageProcess;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.MessagingException;

/**
 *
 * @author Admin
 */
public class loadDataThread extends Thread {

    List<MessageObject> listMsgOb;

    public loadDataThread(List<MessageObject> listMsgOb) {
	this.listMsgOb = listMsgOb;
    }

    @Override
    public void run() {
	System.out.println("Load Data thread start !!! ");
	List<String> loadFromLabel = new ArrayList<>();
	loadFromLabel.add("INBOX");
//	List<MessageObject>listMessages = new ArrayList<>();
	try {
	    try {
		//	    List<Message> initListMessages = MessageProcess.getListMail(loadFromLabel,13);
		GlobalVariable.userId = "testdoan123456@gmail.com";
		LoginProcess.login();
	    } catch (WrongLoginInfoException ex) {
		Logger.getLogger(loadDataThread.class.getName()).log(Level.SEVERE, null, ex);
	    }
	    Gmail service = GlobalVariable.getService();
	    String userId = GlobalVariable.userId;
	    //list response cua mail list
	    ListMessagesResponse response;
	    response = service.users().messages().list(userId).setLabelIds(loadFromLabel).setMaxResults(Long.valueOf(5)).execute();
	    //doc tung mail
	    List<Message> messages = new ArrayList<>();

	    while (response.getMessages() != null) {
		messages.addAll(response.getMessages());
		for (Message msg : messages) {
		    MessageObject newMessOb = new MessageObject();
		    newMessOb.id = msg.getId();
		    newMessOb.from = MessageProcess.getFrom(MessageProcess.getMessageById(GlobalVariable.getService(), GlobalVariable.userId, newMessOb.id).getPayload().getHeaders());
		    listMsgOb.add(newMessOb); //MessageProcess.parseHeaderMail(msg.getId())
		}
		if (response.getNextPageToken() != null) {
		    String pageToken = response.getNextPageToken();
		    response = service.users().messages().list(userId).setLabelIds(loadFromLabel).setPageToken(pageToken).execute();
		} else {
		    break;
		}

	    }

	} catch (IOException | MessagingException ex) {
	}
	System.out.println("Load Data thread stop !!! ");
    }
}
