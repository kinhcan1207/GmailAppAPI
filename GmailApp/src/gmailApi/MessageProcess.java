/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gmailApi;

import com.google.api.client.repackaged.org.apache.commons.codec.binary.Base64;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Label;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import com.google.api.services.gmail.model.MessagePartBody;
import com.google.api.services.gmail.model.MessagePartHeader;
import com.google.api.services.gmail.model.ModifyMessageRequest;
import customException.FailToLoadInitInboxException;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 *
 * @author Admin
 */
public class MessageProcess {

    /**
     * lấy danh sách mail tu label da chon
     *
     * @param service
     * @param num số lượng mail muốn load trong 1 lần
     * @param userId
     * @param loadFromLabel : nhung label dc su dung de load message
     * @return tra ve danh sach message
     * @throws IOException
     * @throws MessagingException
     */
    public static List<Message> getListMail(List<String> loadFromLabel, int num) throws IOException, MessagingException {
	Gmail service = GlobalVariable.getService();
	String userId = GlobalVariable.userId;
	//list response cua mail list
	ListMessagesResponse response;
	response = service.users().messages().list(userId).setLabelIds(loadFromLabel).setMaxResults(Long.valueOf(num)).execute();
	//doc tung mail
	List<Message> messages = new ArrayList<>();
	messages.addAll(response.getMessages());
//	while (response.getMessages() != null) {
//	    messages.addAll(response.getMessages());
//	    if (response.getNextPageToken() != null) {
//		String pageToken = response.getNextPageToken();
//		response = service.users().messages().list(userId).setLabelIds(loadFromLabel).setPageToken(pageToken).execute();
//	    } else {
//		break;
//	    }
//	}

//	for (Message message : messages) {
//	    System.out.println(message.toPrettyString());
//	    
//	    getMessageById(service, userId, message.getId());
//	}
	return messages;
    }

    /**
     * lấy header của 1 message thông qua id
     *
     * @param messageId
     * @return 1 MessageObject
     */
    public static MessageObject parseHeaderMail(String messageId) {
	Gmail service = GlobalVariable.getService();
	String userId = GlobalVariable.userId;
	MessageObject msg = new MessageObject();
	msg.id = messageId;
	Message message;
	try {
	    message = service.users().messages().get(userId, messageId).setFormat("full").execute();
	    MessagePart payload = message.getPayload();
	    List<MessagePartHeader> headers = payload.getHeaders();
	    for (MessagePartHeader messHeadPart : headers) {
		if (messHeadPart.getName().equals("From")) {
		    msg.from = messHeadPart.getValue();
		}
		if (messHeadPart.getName().equals("Subject")) {
		    msg.subject = messHeadPart.getValue();
		}
		if (messHeadPart.getName().equals("Date")) {
		    msg.date = messHeadPart.getValue();
		}
	    }
	} catch (IOException ex) {
	    Logger.getLogger(MessageProcess.class.getName()).log(Level.SEVERE, null, ex);
	}
	return msg;
    }

    /**
     * đổ dữ liệu phần header cho Object
     *
     * @param msgOb
     * @param message
     */
    public static void loadHeaderForMessageOb(MessageObject msgOb, Message message) {
	MessagePart payload = message.getPayload();
	List<MessagePartHeader> headers = payload.getHeaders();
	for (MessagePartHeader messHeadPart : headers) {
	    if (messHeadPart.getName().equals("From")) {
		msgOb.from = messHeadPart.getValue();
	    }
	    if (messHeadPart.getName().equals("Subject")) {
		msgOb.subject = messHeadPart.getValue();
	    }
	    if (messHeadPart.getName().equals("Date")) {
		msgOb.date = messHeadPart.getValue();
	    }
	    if (messHeadPart.getName().equals("To")) {
		msgOb.to = messHeadPart.getValue();
	    }
	    if (messHeadPart.getName().equals("Cc")) {
		msgOb.cc = messHeadPart.getValue();
	    }
	}
    }

    /**
     * đổ dữ liệu phần body cho Object
     *
     * @param msgOb
     * @param parts
     */
    public static void loadBodyForMessageOb(MessageObject msgOb, List<MessagePart> parts) {
	for (MessagePart part : parts) {
	    String mimeType = part.getMimeType();
	    if (mimeType.equals("multipart/related")) {
		loadBodyForMessageOb(msgOb, part.getParts());
	    }
	    if (mimeType.equals("application/pdf")) {
		MessagePartBody body = part.getBody();
		String attId = body.getAttachmentId();
		String filename = part.getFilename();
		msgOb.listFile.put(filename, attId);
	    }
	    if (mimeType.equals("image/png")) {
		MessagePartBody body = part.getBody();
		String attId = body.getAttachmentId();
		String filename = part.getFilename();
		msgOb.listFile.put(filename, attId);
	    }
	    if (mimeType.equals("audio/mp3")) {
		MessagePartBody body = part.getBody();
		String attId = body.getAttachmentId();
		String filename = part.getFilename();
		msgOb.listFile.put(filename, attId);
	    }
	    if (mimeType.equals("text/plain")) {
		if (!part.getFilename().isEmpty()) {
		    MessagePartBody body = part.getBody();
		    String attId = body.getAttachmentId();
		    String filename = part.getFilename();
		    msgOb.listFile.put(filename, attId);
		} else {
		    String data = part.getBody().getData();
		    Base64 base64Url = new Base64(true);
		    byte[] emailBytes = Base64.decodeBase64(data);
		    String text = new String(emailBytes);
		    msgOb.mainText = text;
		}
	    }
	    String fileName = part.getFilename();
	    if (!fileName.isEmpty()) {
		MessagePartBody body = part.getBody();
		String attId = body.getAttachmentId();
		msgOb.listFile.put(fileName, attId);
	    }
	}
    }

    /**
     * load từ Message(api.gmail) sang msgOb, nói chung là đổ dữ liều vào object
     *
     * @param msgOb
     */
    public static void loadMessage(MessageObject msgOb) {
	Gmail service = GlobalVariable.getService();
	String userId = GlobalVariable.userId;
	Message message;
	try {
	    message = service.users().messages().get(userId, msgOb.id).setFormat("full").execute();
	    List<MessagePart> parts = message.getPayload().getParts();
	    loadHeaderForMessageOb(msgOb, message);
	    loadBodyForMessageOb(msgOb, parts);
	} catch (IOException ex) {
	    Logger.getLogger(MessageProcess.class.getName()).log(Level.SEVERE, null, ex);
	}
    }

    /**
     * tải xuống những file đính kèm
     *
     * @param service
     * @param userId
     * @param messageId
     * @param attId
     * @param filename
     * @throws IOException
     */
    public static void downloadAttach(String messageId, String attId, String filename) throws IOException {
	MessagePartBody attachPart = GlobalVariable.getService().users().messages().attachments().get(GlobalVariable.userId, messageId, attId).execute();

	Base64 base64Url = new Base64(true);
	byte[] fileByteArray = base64Url.decodeBase64(attachPart.getData());
	FileOutputStream fileOutFile
		= new FileOutputStream("" + filename);
	fileOutFile.write(fileByteArray);
	fileOutFile.close();
    }

    /**
     * phân tích phần body của mail, nhận diện các tệp kèm theo
     *
     * @param service
     * @param userId
     * @param messageId
     * @param msgP
     */
    public static void parseBodyParts(Gmail service, String userId, String messageId, List<MessagePart> msgP) {
	for (MessagePart m : msgP) {
	    System.out.println("Part id:" + m.getPartId());
	    String mimeType = m.getMimeType();
	    System.out.println("Mimetype: " + mimeType);
	    if (mimeType.equals("multipart/related")) {
		parseBodyParts(service, userId, messageId, m.getParts());
	    }
	    if (mimeType.equals("application/pdf")) {
		String filename = m.getFilename();
		MessagePartBody body = m.getBody();
		String attId = body.getAttachmentId();
		System.out.println("New PDF detect: " + filename);
		System.out.println("Downloading file...");
		try {
		    downloadAttach(messageId, attId, filename);
		} catch (IOException ex) {
		    Logger.getLogger(MessageProcess.class.getName()).log(Level.SEVERE, null, ex);
		}
		System.out.println(body);
	    }
	    if (mimeType.equals("image/png")) {
		System.out.println("New Image detect: " + m.getFilename());
		MessagePartBody body = m.getBody();
		String attId = body.getAttachmentId();
		System.out.println(attId);
		System.out.println("Downloading picture...");
		try {
		    downloadAttach(messageId, attId, m.getFilename());
		} catch (IOException ex) {
		    Logger.getLogger(MessageProcess.class.getName()).log(Level.SEVERE, null, ex);
		}
	    }
	    if (mimeType.equals("audio/mp3")) {
		System.out.println("New audio detect: " + m.getFilename());
		MessagePartBody body = m.getBody();
		String attId = body.getAttachmentId();
		System.out.println(attId);
		System.out.println("Downloading audio...");
		try {
		    downloadAttach(messageId, attId, m.getFilename());
		} catch (IOException ex) {
		    Logger.getLogger(MessageProcess.class.getName()).log(Level.SEVERE, null, ex);
		}
	    }
	    if (mimeType.equals("text/plain")) {
		System.out.println("Text/plain detect !");
		if (!m.getFilename().isEmpty()) {
		    System.out.println("New file detect: " + m.getFilename());
//		    downloadAttach(service, userId, messageId, userId, mimeType);
		} else {
		    String data = m.getBody().getData();
		    Base64 base64Url = new Base64(true);
		    byte[] emailBytes = Base64.decodeBase64(data);
		    String text = new String(emailBytes);
		    System.out.println(text);
		}
	    }
	    // testing
	    String fileName = m.getFilename();
	    if (!fileName.isEmpty()) {
		System.out.println("New file: " + fileName);
	    }
//            System.out.println(m.getBody().toString());
//            List<MessagePart> parts1 = m.getParts();
//            System.out.println(parts1);
	}
    }

    /**
     * lấy ra 1 mail
     *
     * @param service
     * @param userId
     * @param messageId
     * @return Message
     * @throws IOException
     * @throws MessagingException
     */
    public static Message getMessageById(Gmail service, String userId, String messageId) throws IOException, MessagingException {
	Message message = service.users().messages().get(userId, messageId).setFormat("full").execute();
//	MessagePart payload = message.getPayload();
//	List<MessagePartHeader> headers = payload.getHeaders();
//	for (MessagePartHeader messHeadPart : headers) {
////            System.out.println(i.getName());
//	    if (messHeadPart.getName().equals("From")) {
//		System.out.println(messHeadPart.getValue());
//	    }
//	    if (messHeadPart.getName().equals("Subject")) {
//		System.out.println(messHeadPart.getValue());
//	    }
//	    if (messHeadPart.getName().equals("References")) {
//		System.out.println(messHeadPart.getValue());
//	    }
//	}
	return message;
    }

    /**
     * Thêm,xoá lables vào 1 message
     *
     * @param labelsToAdd
     * @param labelsToRemove
     * @param messageId
     * @throws IOException
     */
    public static void modifyLabelsToMessage(List<String> labelsToAdd, List<String> labelsToRemove, String messageId) throws IOException {
	Gmail service = GlobalVariable.getService();
	ModifyMessageRequest mods = new ModifyMessageRequest().setAddLabelIds(labelsToAdd)
		.setRemoveLabelIds(labelsToRemove);
	Message message = service.users().messages().modify(GlobalVariable.userId, messageId, mods).execute();

	System.out.println("Message id: " + message.getId());
	System.out.println(message.toPrettyString());
    }

    /**
     * lấy về địa chỉ của thuộc tính To: trong headers gửi đến ai
     *
     * @param messageHeader
     * @return String
     */
    public static String getTo(List<MessagePartHeader> messageHeader) {
	String toAdd = null;
	for (MessagePartHeader e : messageHeader) {
	    if (e.getName().equals("To")) {
		toAdd = e.getValue();
	    }
	}
	return toAdd;
    }

    /**
     * lấy về thuộc tính From: trong headers gửi từ ai
     *
     * @param messageHeader
     * @return String
     */
    public static String getFrom(List<MessagePartHeader> messageHeader) {
	String fromAdd = null;
	for (MessagePartHeader e : messageHeader) {
	    if (e.getName().equals("From")) {
		fromAdd = e.getValue();
	    }
	}
	return fromAdd;
    }

    /**
     * Lấy về thuộc tính Date trong headers Nhận vào ngày nào
     *
     * @param messageHeader
     * @return String
     */
    public static String getDate(List<MessagePartHeader> messageHeader) {
	String dateTo = null;
	for (MessagePartHeader e : messageHeader) {
	    if (e.getName().equals("Date")) {
		dateTo = e.getValue();
	    }
	}
	return dateTo;
    }

    /**
     * Lấy về Message-ID:
     *
     * @param messageHeader
     * @return String
     */
    public static String getMessageId(List<MessagePartHeader> messageHeader) {
	String messageId = null;
	for (MessagePartHeader e : messageHeader) {
	    if (e.getName().equals("Message-ID")) {
		messageId = e.getValue();
	    }
	}
	return messageId;
    }

    /**
     * lấy về tiêu đề của message
     *
     * @param messageHeader
     * @return String
     */
    public static String getSubject(List<MessagePartHeader> messageHeader) {
	String subject = null;
	for (MessagePartHeader e : messageHeader) {
	    if (e.getName().equals("Subject")) {
		subject = e.getValue();
	    }
	}
	return subject;
    }

    /**
     * lấy về thuộc tính đặc biệt References chỉ dùng trong những message RE:
     *
     * @param messageHeader
     * @return String
     */
    public static String getReferences(List<MessagePartHeader> messageHeader) {
	String references = null;
	for (MessagePartHeader e : messageHeader) {
	    if (e.getName().equals("References")) {
		references = e.getValue();
	    }
	}
	return references;
    }

    /**
     * lấy về thuộc tính đặc biệt In-reply-to: chỉ có trong những message RE:
     *
     * @param messageHeader
     * @return String
     */
    public static String getInReplyTo(List<MessagePartHeader> messageHeader) {
	String inReplyTo = null;
	for (MessagePartHeader e : messageHeader) {
	    if (e.getName().equals("In-Reply-To")) {
		inReplyTo = e.getValue();
	    }
	}
	return inReplyTo;
    }

    /**
     * in ra header của messsage chỉ sử dụng cho mục đích test
     *
     * @param message
     */
    public static void printHeader(Message message) {
	MessagePart payload = message.getPayload();
	List<MessagePartHeader> headers = payload.getHeaders();
	for (MessagePartHeader messHeadPart : headers) {
	    if (messHeadPart.getName().equals("From")) {
		System.out.println(messHeadPart.getValue());
	    }
	    if (messHeadPart.getName().equals("Subject")) {
		System.out.println(messHeadPart.getValue());
	    }
	}
    }

    /**
     * reply a message
     *
     * @param messageId
     * @param replyMessage
     * @throws IOException
     */
    public static void reply(String messageId, String replyMessage) throws IOException {
	// must get from old mail
	String from = null;
	String subject;
	String newSubject = "";
	String oldReferences = null;

	String messageID = null;

	Gmail service = GlobalVariable.getService();
	String userId = "testdoan123456@gmail.com";
	Message message = service.users().messages().get(userId, messageId).setFormat("full").execute();
	String threadId = message.getThreadId();

	MessagePart payload = message.getPayload();
	List<MessagePartHeader> headers = payload.getHeaders();
	for (MessagePartHeader messHeadPart : headers) {
	    if (messHeadPart.getName().equals("From")) {
		from = messHeadPart.getValue();
	    }
	    if (messHeadPart.getName().equals("Subject")) {
		subject = messHeadPart.getValue();
		newSubject += subject;
	    }
	    if (messHeadPart.getName().equals("Message-ID")) {
		messageID = messHeadPart.getValue();
	    }
	    if (messHeadPart.getName().equals("References")) {
		oldReferences = messHeadPart.getValue();
	    }
	}

	Properties props = new Properties();
	Session session = Session.getDefaultInstance(props, null);

	MimeMessage mimeMessage = new MimeMessage(session);
	InternetAddress[] listto = new InternetAddress[1];
	InternetAddress[] listfrom = new InternetAddress[1];
	try {
	    listto[0] = new InternetAddress(from);
	    mimeMessage.setText(replyMessage);
	    mimeMessage.setRecipients(javax.mail.Message.RecipientType.TO, listto);
	    mimeMessage.setFrom(new InternetAddress(userId));
	    mimeMessage.setSubject(newSubject, "utf-8");
	    mimeMessage.setHeader("References", oldReferences + " " + messageID);
	    mimeMessage.setHeader("In-Reply-To", messageID);

	    SendMailProcess.sendMessage(service, userId, mimeMessage);
	} catch (AddressException ex) {
	    Logger.getLogger(MessageProcess.class.getName()).log(Level.SEVERE, null, ex);
	} catch (MessagingException ex) {
	    Logger.getLogger(MessageProcess.class.getName()).log(Level.SEVERE, null, ex);
	}
    }

    /**
     * xoá hoàn toàn mail này, không quay lại được
     *
     * @param messageId
     * @throws IOException
     */
    public static void permantlyDeleteMessage(String messageId) throws IOException {
	Gmail service = GlobalVariable.getService();
	String userId = GlobalVariable.userId;
	service.users().messages().delete(userId, messageId).execute();

    }

    /**
     * đưa vào labels rác, có thể quay lại
     *
     * @param messageId
     * @throws IOException
     */
    public static void moveToTrash(String messageId) throws IOException {
	Gmail service = GlobalVariable.getService();
	String userId = GlobalVariable.userId;
	service.users().messages().trash(userId, messageId).execute();
    }

    /**
     * lấy mail ra khỏi thư mục rác
     *
     * @param messageId
     * @throws IOException
     */
    public static void unTrash(String messageId) throws IOException {
	Gmail service = GlobalVariable.getService();
	String userId = GlobalVariable.userId;
	service.users().messages().untrash(userId, userId).execute();
    }

    /**
     * tìm kiếm mail theo query
     *
     * @param query
     * @throws IOException
     * @throws MessagingException
     */
    public static List<MessageObject> search(String query) throws IOException, MessagingException {
	Gmail service = GlobalVariable.getService();
	String userId = GlobalVariable.userId;
	ListMessagesResponse response = service.users().messages().list(userId).setQ(query).execute();

	List<Message> messages;
	messages = new ArrayList<>();
	while (response.getMessages() != null) {
	    messages.addAll(response.getMessages());
	    if (response.getNextPageToken() != null) {
		String pageToken = response.getNextPageToken();
		response = service.users().messages().list(userId).setQ(query)
			.setPageToken(pageToken).execute();
	    } else {
		break;
	    }
	}
	List<MessageObject> listMessages = new ArrayList<>();
	try {
	    for (Message msg : messages) {
		MessageObject newMessOb = new MessageObject();
		newMessOb.id = msg.getId();
		newMessOb.from = MessageProcess.getFrom(MessageProcess.getMessageById(GlobalVariable.getService(), GlobalVariable.userId, newMessOb.id).getPayload().getHeaders());
		listMessages.add(newMessOb); //MessageProcess.parseHeaderMail(msg.getId())
	    }
	} catch (IOException | MessagingException ex) {
//	    Logger.getLogger(Init.class.getName()).log(Level.SEVERE, null, ex);
	}
	return listMessages;

//	for (Message message : messages) {
//	    getMessageById(service, userId, message.getId());
//	    System.out.println("----------------------------------------------------------");
//	}

    }

    /**
     * save mail đang đọc
     *
     * @param msgOb
     * @param pathDir
     */
    public static void saveMail(MessageObject msgOb, String pathDir) {
	FileOutputStream fout = null;
	ObjectOutputStream o = null;
	try {

	    fout = new FileOutputStream(new File(pathDir + msgOb.id + ".msgOb"));

	    o = new ObjectOutputStream(fout);
	    o.writeObject(msgOb);
	} catch (FileNotFoundException ex) {
	    Logger.getLogger(MessageProcess.class.getName()).log(Level.SEVERE, null, ex);
	} catch (IOException ex) {
	    Logger.getLogger(MessageProcess.class.getName()).log(Level.SEVERE, null, ex);
	} finally {
	    try {
		if (o != null) {
		    o.close();
		}
		if (fout != null) {
		    fout.close();
		}
	    } catch (IOException ex) {
		Logger.getLogger(MessageProcess.class.getName()).log(Level.SEVERE, null, ex);
	    }
	}
    }

    /**
     * load lại mail từ file
     *
     * @param fileName
     * @return MessageObject
     */
    public static MessageObject readSaveMail(String fileName) {
	FileInputStream fin = null;
	ObjectInputStream in = null;
	MessageObject msgOb = null;
	try {
	    fin = new FileInputStream(fileName);
	    in = new ObjectInputStream(fin);

	    msgOb = (MessageObject) in.readObject();
	} catch (FileNotFoundException ex) {
	    Logger.getLogger(MessageProcess.class.getName()).log(Level.SEVERE, null, ex);
	} catch (IOException | ClassNotFoundException ex) {
	    Logger.getLogger(MessageProcess.class.getName()).log(Level.SEVERE, null, ex);
	} finally {
	    try {
		if (in != null) {
		    in.close();
		}
		if (fin != null) {
		    fin.close();
		}
	    } catch (IOException ex) {
		Logger.getLogger(MessageProcess.class.getName()).log(Level.SEVERE, null, ex);
	    }
	}
	return msgOb;
    }
    
}
