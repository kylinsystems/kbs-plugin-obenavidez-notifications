package ni.idempiere.model;
 

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException; 
import java.util.logging.Level;

import ni.idempiere.notification.util.EmailNotification;

import org.adempiere.base.event.AbstractEventHandler; 
import org.adempiere.base.event.IEventTopics;
import org.compiere.model.MPInstance;
import org.compiere.model.PO;
import org.compiere.util.CLogger; 
import org.compiere.util.DB;
import org.osgi.service.event.Event; 
/** 
 *  @author     Osmar David Benavidez / SAC
 *  @version    $Id: NotificationValidator.java,v1  
 *  @fecha 		2016/08/04  
 *  @mail 		osmar.benavidez@gmail.com
**/
public class NotificationValidator extends AbstractEventHandler{

	/**	Logger			*/
	private static CLogger log = CLogger.getCLogger(NotificationValidator.class); 

	@Override
	protected void initialize() 
	{ 
		 
		PreparedStatement pstm = null;
		ResultSet rs = null; 
		
		try 
		{
			pstm = DB.prepareStatement("SELECT  t.TableName,p.ad_process_id,Event_ModelDoc_validator DocTableEvents "
			+"FROM	NI_Notification_Trigguer nt "
				+"LEFT JOIN AD_Table t ON nt.AD_Table_ID=t.AD_Table_ID " 
			+"	LEFT JOIN AD_Process p ON nt.ad_process_id=p.ad_process_id "
			+"WHERE	nt.IsActive='Y' AND  Event_ModelDoc_validator IS NOT NULL "
			+"GROUP BY t.TableName,p.ad_process_id,Event_ModelDoc_validator " ,null);
			
			rs = pstm.executeQuery();	
			while(rs.next())
			{				
				String tablename=rs.getString(1); 
				int ad_process_id=rs.getInt(2);
				String doctablevalue=rs.getString(3); 
				/*	tables will be monitored */
				if((tablename==null && ad_process_id==0) || "".equals(tablename) || doctablevalue==null || "".equals(doctablevalue))
					continue; 
				registerTableEvent(doctablevalue,tablename); 
				/** 
					if there are processes associated with notifications
					we will use AD_PInstance to monitor it, and only tow events will be enables :
				    adempiere/po/afterNew, adempiere/po/afterChange
				 */
				
				if(ad_process_id>0)
				{
					if(IEventTopics.PO_AFTER_NEW.equals(doctablevalue))
						registerTableEvent(IEventTopics.PO_AFTER_NEW,  MPInstance.Table_Name);					
					else if(IEventTopics.PO_AFTER_CHANGE.equals(doctablevalue))
						registerTableEvent(IEventTopics.PO_AFTER_CHANGE,  MPInstance.Table_Name);
				}
					
			} 
		} catch (SQLException e) { 
			log.log(Level.SEVERE, "NotificationValidator.initialize()", e);
		}  
	}
	 
	@Override
	protected void doHandleEvent(Event event) 
	{
		String type = event.getTopic();
		PO po = getPO(event);
		
		if(po!=null && type!=null && !"".equals(type))
		{
			if(po instanceof MPInstance 
				&& 
				"ni.idempiere.notification.process.SendNotification".equals(((MPInstance) po).getAD_Process().getClassname())) 
				return;
			else
				new EmailNotification(po,type).start(); 
		}
						
	}

}
