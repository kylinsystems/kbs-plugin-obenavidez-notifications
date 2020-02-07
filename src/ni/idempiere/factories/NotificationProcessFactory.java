package ni.idempiere.factories; 

import ni.idempiere.notification.process.SendNotification;
import org.adempiere.base.IProcessFactory;
import org.compiere.process.ProcessCall; 
import org.compiere.util.CLogger;
/** 
 *  @author     Osmar David Benavidez / SAC 
**/
public class NotificationProcessFactory implements IProcessFactory{
 
	private final static CLogger log = CLogger.getCLogger(NotificationProcessFactory.class);
	
	@Override
	public ProcessCall newProcessInstance(String className) {
		ProcessCall process = null; 
		if ("ni.idempiere.process.SendNotification".equals(className)) {
			try 
			{
				process =  SendNotification.class.newInstance();
			} catch (Exception e) {
				
			}
		}   	
		return process; 
	}

}
