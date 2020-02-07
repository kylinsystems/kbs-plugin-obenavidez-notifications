package ni.idempiere.notification.process;

import org.adempiere.base.event.IEventTopics; 
import org.compiere.process.SvrProcess;

import ni.idempiere.notification.util.EmailNotification;

/**
 *  Notification Process Template
 *  @author     Osmar David Benavidez / SAC
**/

public class SendNotification extends SvrProcess 
{
	EmailNotification notiemail;

	@Override
	protected void prepare() {
		new EmailNotification(this.getProcessInfo().getAD_Process_ID(),IEventTopics.PO_AFTER_NEW).start(); 
	}

	@Override
	protected String doIt() throws Exception 
	{		
		Thread.sleep(2000);
		new EmailNotification(this.getProcessInfo().getAD_Process_ID(),IEventTopics.PO_AFTER_CHANGE).start();
		return null;
	}
}
