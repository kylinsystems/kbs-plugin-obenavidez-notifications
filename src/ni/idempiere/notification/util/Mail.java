package ni.idempiere.notification.util;
  
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Properties;

import javax.mail.Transport; 
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
 

import org.compiere.db.Database;
import org.compiere.util.DB;
 
/**
 *  Notification Email Entity, to send alert.
 *
 *  @author     Osmar David Benavidez
 *  @version    $Id: Mail.java,v1  
 *  @fecha 		2016/08/04  
 *  @mail 		osmar.benavidez@gmail.com
**/
public class Mail 
{   
    private static String SMTP_SERVER;
	private static int PORT;
	private static String REMITENT_ACCOUNT;
	private static String REMITENT_ACCOUNT_PASSWORD;
	
	private String from = "";//youremail@gmail.com
    private String password = "";//yourpassword: 123456 :)
    // receiver1@gmail.com,receiver2@gmail.com, receiver_n@gmail.com
    private InternetAddress[] addressTo;
    private InternetAddress[] addressCc;
    private InternetAddress[] addressBcc;
    private String Subject = "";//Email title
    private String MessageMail = "";//Email Content

    public static final String NATIVE_MARKER = "NATIVE_"+Database.DB_POSTGRESQL+"_KEYWORK";
    
    public static void init() throws Exception {
		PreparedStatement pstm = null;
		ResultSet rs = null;

		pstm = DB.prepareStatement(("SELECT smtp_server, " + "       port, "
				+ "		remitent_account, " + "		remitent_accountpassword "
				+ "FROM adempiere.ni_notificationsetup "
				+ "ORDER BY  isdefault DESC " + NATIVE_MARKER
				+ "LIMIT 1 " + NATIVE_MARKER), null);

		rs = pstm.executeQuery();
		while (rs.next()) {
			SMTP_SERVER = rs.getString(1);
			PORT = rs.getInt(2);
			REMITENT_ACCOUNT = rs.getString(3);
			REMITENT_ACCOUNT_PASSWORD = rs.getString(4);
		}
	}
    public Mail() throws Exception{init();}

    public void SEND() throws Exception
    {        
        	// setup properties smtp protocol.
        Properties props = new Properties();

        props.put("mail.smtp.host", SMTP_SERVER);
        props.put("mail.smtp.port", PORT);
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.user", "usuario"); 
        setFrom(REMITENT_ACCOUNT);
        setPassword(REMITENT_ACCOUNT_PASSWORD.toCharArray());
        
        // setup authentification,get session
        Session session = Session.getInstance(props,
           new javax.mail.Authenticator() {
              protected PasswordAuthentication getPasswordAuthentication() {
                 return new PasswordAuthentication(
                		 getFrom(), getPassword());
              }
           });
        
        session.setDebug(false);
        
        //make destiny and origin mensaje
        MimeMessage mimemessage = new MimeMessage(session);
        InternetAddress addressFrom = new InternetAddress( getFrom() );
        mimemessage.setFrom(addressFrom);
        if(addressTo==null || addressTo.length==0)
        	return;
        mimemessage.setRecipients(Message.RecipientType.TO, addressTo);
        if(addressCc!=null && addressCc.length>0)
        	mimemessage.setRecipients(Message.RecipientType.CC,addressCc);
        if(addressBcc!=null && addressBcc.length>0)
        	mimemessage.setRecipients(Message.RecipientType.BCC,addressBcc);
        mimemessage.setSubject( getSubject() );
        mimemessage.setContent(getMessage(), "text/html"); 
        Transport.send(mimemessage); 
        
        
        
    }
    
    
    //sender emails
    public void setFrom(String mail){ this.from = mail; }
    
    public String getFrom(){ return this.from; }
    //Password
    public void setPassword(char[] value){
        this.password = new String(value);
    }
    public String getPassword(){ return this.password; }
   
    //receivers emails
    public void setTo(String mails){
    	if(mails==null)
    		return;
        String[] tmp =mails.split(",");
        ArrayList<InternetAddress> tmpaddress=new ArrayList<InternetAddress>();
        for (int i = 0; i < tmp.length; i++) {
            try {
            	tmpaddress.add(new InternetAddress(tmp[i])); 
            } catch (AddressException ex) {
                System.out.println(ex);
            }
        }
        if(tmpaddress!=null) 
        	tmpaddress.toArray(addressTo = new InternetAddress[tmpaddress.size()]); 
    }
    
    //Carbon Copy emails
    public void setCc(String mails){
    	if(mails==null)
    		return;
        String[] tmp =mails.split(",");
        ArrayList<InternetAddress> tmpaddress=new ArrayList<InternetAddress>();
        for (int i = 0; i < tmp.length; i++) {
            try {
            	tmpaddress.add(new InternetAddress(tmp[i])); 
            } catch (AddressException ex) {
                System.out.println(ex);
            }
        }
        if(tmpaddress!=null) 
        	tmpaddress.toArray(addressCc = new InternetAddress[tmpaddress.size()]); 
        
    }
   
    //Blind Carbon Copy emails
    public void setBcc(String mails){ 
    	if(mails==null)
    		return;
        String[] tmp =mails.split(",");
        ArrayList<InternetAddress> tmpaddress=new ArrayList<InternetAddress>();
        for (int i = 0; i < tmp.length; i++) {
            try {
            	tmpaddress.add(new InternetAddress(tmp[i])); 
            } catch (AddressException ex) {
                System.out.println(ex);
            }
        }
        if(tmpaddress!=null) 
        	tmpaddress.toArray(addressBcc = new InternetAddress[tmpaddress.size()]); 
        
    }
    
    public InternetAddress[] getTo(){ return this.addressTo; }
    
    public InternetAddress[] getCc(){ return this.addressCc; }
    
    public InternetAddress[] getBcc(){ return this.addressBcc; }    
    //Email Title
    public void setSubject(String value){ this.Subject = value; }
    
    public String getSubject(){ return this.Subject; }
    //Emailt content
    public void setMessage(String value){ this.MessageMail = value; }
    
    public String getMessage(){ return this.MessageMail; } 
    
}

