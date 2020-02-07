package ni.idempiere.notification.util;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList; 
import java.util.logging.Level;

import javax.script.ScriptEngine; 
import javax.script.ScriptEngineManager;

import ni.idempiere.model.MNotificationParam;

import org.compiere.model.MPInstance;
import org.compiere.model.MRule;
import org.compiere.model.PO;
import org.compiere.process.SvrProcess;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.Env;

/**  
 *  @author     Osmar David Benavidez / SAC
 *  @version    $Id: EmailNotification.java,v1  
 *  @fecha 		2016/08/04  
 *  @mail 		osmar.benavidez@gmail.com
**/

public class EmailNotification extends Thread
{ 
	public static String SUBJECT_MESSAGE = "";
	public static String EMAIL_ADDRESS;
	public static String BCC_EMAIL_ADDRESS;
	public static String CC_EMAIL_ADDRESS;
	public static String NOTIFICATION_MESSAGE;
	public static StringBuilder HTML_TEMPLATE;
	public int NI_NotificationMessage_ID;

	public static String engineName = "beanshell:test"; 
	Boolean canBeSend = false;

	int NotificationMessage_ID;
	int AD_Process_ID = 0;
	int AD_Table_ID = 0;
	String EventDocTableType = ""; 
	
	public MNotificationParam parameter;
	public ArrayList<MNotificationParam> parameters=new ArrayList<MNotificationParam>();

	SvrProcess process;
	
	PreparedStatement pstmt;
	ResultSet rs;
	
	/* Engine Manager */
	private ScriptEngineManager factory = null;
	/* The Engine */
	ScriptEngine engine = null;
	/*Model Entity, expose to server client via Scripting*/
	PO entity;

	public EmailNotification(){}
	
	public EmailNotification(int _AD_Process_ID, int _AD_Table_ID,
			String _EventDocTableType) {
		AD_Process_ID = _AD_Process_ID;
		AD_Table_ID = _AD_Table_ID;
		EventDocTableType = _EventDocTableType; 
	}
	
	public EmailNotification(int _AD_Process_ID,String _EventDocTableType) {
		AD_Process_ID = _AD_Process_ID;
		AD_Table_ID = 0;
		EventDocTableType = _EventDocTableType; 
	}
	
	public EmailNotification(SvrProcess process) {
		AD_Process_ID = process==null?0:process.getProcessInfo().getAD_Process_ID();
		AD_Table_ID = 0;
		EventDocTableType = ""; 
	}
	
	public EmailNotification(PO entity,String _EventDocTableType) { 
		this.entity=entity; 
		if(entity instanceof MPInstance)
		{
			AD_Process_ID = (entity instanceof MPInstance)?(((MPInstance)entity).getAD_Process_ID()):0;
			AD_Table_ID =0;
		}else{
			AD_Table_ID = entity.get_Table_ID();
		}		
		EventDocTableType = _EventDocTableType; 
	}
	
	public EmailNotification(SvrProcess process,PO entity,String _EventDocTableType) { 
		this.process=process;
		this.entity=entity;
		AD_Process_ID = process==null?0:process.getProcessInfo().getAD_Process_ID(); 
		AD_Table_ID = (entity==null || (entity instanceof MPInstance))?0:entity.get_Table_ID();  
		EventDocTableType = _EventDocTableType; 
	}
	
	@Override
	public void run() 
	{   
		this.prepare().doIt();
	}

	/** Logger */
	private static CLogger log = CLogger.getCLogger(EmailNotification.class);

	public ArrayList<MNotificationParam> getParameters() 
	{
		if(parameters!=null && parameters.size()>0)
			return parameters;
		
		ArrayList<MNotificationParam> _parameters = new ArrayList<MNotificationParam>();
		String sql = "SELECT	np.NI_NotificationParameter_ID, "
				+ "		np.NI_NotificationMessage_ID, " + "		np.seqno, "
				+ "		np.name, " + "		np.columname, " + "		np.javacode "
				+ "FROM	NI_NotificationParameter np "
				+ "WHERE	np.NI_NotificationMessage_ID=? "
				+ "ORDER	BY Seqno ASC ";

		try {
			pstmt = DB.prepareStatement(sql, null);
			pstmt.setInt(1, NI_NotificationMessage_ID);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				_parameters.add(new MNotificationParam(rs.getInt(1), rs
						.getInt(2), rs.getInt(3), rs.getString(4), rs
						.getString(5), rs.getString(6)));

			}
			if (rs != null && pstmt != null) {
				rs.close();
				pstmt.close();
			}
		} catch (SQLException e) {
			log.log(Level.SEVERE, sql, e);
		}
		return _parameters;
	}

	public static ArrayList<MNotificationParam> getParameters(
			int NI_NotificationMessage_ID) 
	{ 
		PreparedStatement pstmt;
		ResultSet rs;
		ArrayList<MNotificationParam> _parameters = new ArrayList<MNotificationParam>();
		String sql = "SELECT	np.NI_NotificationParameter_ID, "
				+ "		np.NI_NotificationMessage_ID, " + "		np.seqno, "
				+ "		np.name, " + "		np.columname, " + "		np.javacode "
				+ "FROM	NI_NotificationParameter np "
				+ "WHERE	np.NI_NotificationMessage_ID=? "
				+ "ORDER	BY Seqno ASC ";

		try 
		{
			pstmt = DB.prepareStatement(sql, null);
			pstmt.setInt(1, NI_NotificationMessage_ID);
			rs = pstmt.executeQuery();
			while (rs.next()) 
			{
				_parameters.add(new MNotificationParam(rs.getInt(1), rs
						.getInt(2), rs.getInt(3), rs.getString(4), rs
						.getString(5), rs.getString(6)));

			}
			if (rs != null && pstmt != null) {
				rs.close();
				pstmt.close();
			}
		} catch (SQLException e) {
			log.log(Level.SEVERE, sql, e);
		}
		return _parameters;
	}

	public EmailNotification prepare() 
	{
		try 
		{ 
			String query="";
			StringBuilder WHERE = new StringBuilder();
			StringBuilder GROUPBY = new StringBuilder(
					" GROUP BY nm.NI_NotificationMessage_ID,	"
							+ "nm.subjectmessage,nm.bodymessage ");

			StringBuilder querysql = new StringBuilder(
					"SELECT  nm.subjectmessage, "
							+"nm.bodymessage, 	 "
							+"nm.sendto, "
							+"array_to_string(array_agg(distinct (u.email)), ',') roleemails, "
							+"nm.carboncopy cc, "
							+"nm.blindcarboncopy bcc, "
							+"nm.NI_NotificationMessage_ID "
						+"FROM 	NI_NotificationMessage nm 	  "
							+"LEFT JOIN NI_Notification_Trigguer nt ON nm.NI_NotificationMessage_ID=nt.NI_NotificationMessage_ID "
							+"LEFT JOIN AD_Role r ON nm.AD_Role_ID=r.AD_Role_ID "
							+"LEFT JOIN AD_User_Roles ur ON r.AD_Role_ID=ur.AD_Role_ID "
							+"LEFT JOIN AD_User u ON ur.AD_User_ID=u.AD_User_ID  ");
			
			if (AD_Process_ID > 0)
				WHERE.append((WHERE.length() > 0 ? " AND " : "")
						+ " nt.AD_Process_ID=" + AD_Process_ID);
			if (AD_Table_ID > 0)
				WHERE.append((WHERE.length() > 0 ? " AND " : "")
						+ " nt.AD_Table_ID=" + AD_Table_ID);
			if (EventDocTableType!=null && !"".equals(EventDocTableType))
				WHERE.append((WHERE.length() > 0 ? " AND " : "")
						+ " nt.Event_ModelDoc_validator='"+EventDocTableType+"'");
			 

			querysql.append((WHERE.length() > 0 ? " WHERE " + WHERE.toString()
					: ""));
			querysql.append(GROUPBY.toString());
			query=
			 "SELECT  subjectmessage, "
					+"bodymessage,  "
					+"(case  "
						+"WHEN (sendto IS NOT NULL) AND sendto<>'' AND (roleemails IS NOT NULL) AND roleemails<>'' "
						+"THEN (roleemails || ','|| sendto) "
						+"WHEN (sendto IS NULL) OR sendto='' AND (roleemails IS NOT NULL) AND roleemails<>'' "
						+"THEN  roleemails "
						+"WHEN (roleemails IS NULL) OR roleemails='' AND (sendto IS NOT NULL) AND sendto<>'' "
						+"THEN  sendto "
					+"END) mails, "
					+"cc, "
					+"bcc, "
					+"NI_NotificationMessage_ID "
			+"FROM "
			+"( "
			+	querysql.toString()
			+")SC ";

			pstmt = DB.prepareStatement(query.toString(), null);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				SUBJECT_MESSAGE = rs.getString(1);
				NOTIFICATION_MESSAGE = rs.getString(2);
				EMAIL_ADDRESS = rs.getString(3);
				CC_EMAIL_ADDRESS = rs.getString(4);
				BCC_EMAIL_ADDRESS = rs.getString(5);
				NI_NotificationMessage_ID = rs.getInt(6);
			}
			if (rs != null && pstmt != null) {
				rs.close();
				pstmt.close();
				canBeSend = false;
			}

			if (NI_NotificationMessage_ID == 0) {
				canBeSend = false;
				return this;
			}
			NOTIFICATION_MESSAGE = replaceTags(NOTIFICATION_MESSAGE);
			canBeSend = true;
		} catch (Exception e) {
			e.printStackTrace();
			canBeSend = false;
			log.log(Level.SEVERE, "linea 184", e);
		}
		return this;
	}

	public String doIt() {
		try 
		{
			if (!canBeSend || NI_NotificationMessage_ID==0)
				return "";
			Mail mail = new Mail();
			mail.setTo(EMAIL_ADDRESS);
			mail.setCc(CC_EMAIL_ADDRESS);
			mail.setBcc(BCC_EMAIL_ADDRESS);
			mail.setSubject(SUBJECT_MESSAGE);
			mail.setMessage(NOTIFICATION_MESSAGE);
			mail.SEND();

		} catch (Exception e) {
			log.log(Level.SEVERE, "linea 200", e);
		}
		return null;
	}
 
	public String replaceTags(String NOTIFICATION_MESSAGE) 
	{
		try 
		{
			parameters = getParameters();
			if (parameters.size() == 0)
				return NOTIFICATION_MESSAGE;
			int size = parameters.size();
			for (int i = 0; i < size; i++) 
			{
				String value;
				MNotificationParam item = parameters.get(i);
				if(item.getJava_value()==null){
					value=processParameter(item);
					item.setJava_value(value);
				}				
				String Tag = "@" + item.getColumname() + "@";
				String TagValue = "\n" + item.getJava_value().toString() + "\n";
				NOTIFICATION_MESSAGE = NOTIFICATION_MESSAGE.replaceAll(Tag,TagValue);
				parameters.set(i, item);
			}
		} catch (Exception e) {
			log.log(Level.SEVERE, "linea 226", e);
			canBeSend = false;
		}
		return NOTIFICATION_MESSAGE;
	}

	public String processParameter(MNotificationParam parameter) 
	{
		String retValue = "";
		if(parameter==null) return ""; 
		MRule rule = MRule.get(Env.getCtx(),engineName);
		if(rule==null || rule.get_ID()==0)
		{
			rule.setValue(engineName);
			rule.setName(engineName);
			rule.setEventType(MRule.EVENTTYPE_Callout);
			rule.setEventType(MRule.RULETYPE_JSR223ScriptingAPIs);
			rule.saveEx();
		}	
		if(engine==null)
			engine = rule.getScriptEngine();
		// Method arguments context are EN
		engine.put("EN", this); 
		try 
		{
			retValue = engine.eval(parameter.getJavacode()).toString();			
		} catch (Exception e) {
			log.log(Level.SEVERE, "", e);
			retValue = "EMAIL NOTIFICATION SCRIPT INVALID: " + e.toString();
			return retValue;
		} finally {
		}
		return retValue;
	} // processParameter
	 
	public SvrProcess getProcess(){
		return process;
	}
	
	public PO getEntity(){
		return entity;
	} 
	
	public String getValue(int index)
	{		
		String value=null;
		try 
		{
			ArrayList<MNotificationParam> params=getParameters();
			MNotificationParam param=null;
			index=index-1;
			if(params==null || params.size()==0)
				return null; 
			param=params.get(index);
			if(param!=null)
				value= (String) param.getJava_value();
			 
		} catch (Exception e) {
			
		} 
		return  value;
	}
	
	public Boolean setValue(int index,Object value)
	{		 
		try 
		{
			ArrayList<MNotificationParam> params=getParameters();
			MNotificationParam param=null;			
			index=index-1;
			if(params==null || params.size()==0)
				return null; 
			param=params.get(index);
			if(param!=null)
			{
				param.setJava_value(value);
				parameters.set(index, param);
				return true;
			}			 
		} catch (Exception e) {
			
		} 
		return  false ;
	}
}
