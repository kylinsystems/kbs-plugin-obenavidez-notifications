package ni.idempiere.model;
/** 
 *  @author     Osmar David Benavidez / SAC 
**/
public class MNotificationParam {

	int notificationparameter_id;
	int notificationmessage_id;
	int seqno;
	String name;
	String columname;
	String javacode; 
	Object java_value; 
	
	MNotificationParam(){};
	
	public MNotificationParam(int notificationparameter_id,
			int notificationmessage_id, int seqno, String name,
			String columname, String javacode) {
		super();
		this.notificationparameter_id = notificationparameter_id;
		this.notificationmessage_id = notificationmessage_id;
		this.seqno = seqno;
		this.name = name;
		this.columname = columname;
		this.javacode = javacode; 
	}

	/**
	 * @return the notificationparameter_id
	 */
	public int getNotificationparameter_id() {
		return notificationparameter_id;
	}

	/**
	 * @param notificationparameter_id the notificationparameter_id to set
	 */
	public void setNotificationparameter_id(int notificationparameter_id) {
		this.notificationparameter_id = notificationparameter_id;
	}

	/**
	 * @return the notificationmessage_id
	 */
	public int getNotificationmessage_id() {
		return notificationmessage_id;
	}

	/**
	 * @param notificationmessage_id the notificationmessage_id to set
	 */
	public void setNotificationmessage_id(int notificationmessage_id) {
		this.notificationmessage_id = notificationmessage_id;
	}

	/**
	 * @return the seqno
	 */
	public int getSeqno() {
		return seqno;
	}

	/**
	 * @param seqno the seqno to set
	 */
	public void setSeqno(int seqno) {
		this.seqno = seqno;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the columname
	 */
	public String getColumname() {
		return columname;
	}

	/**
	 * @param columname the columname to set
	 */
	public void setColumname(String columname) {
		this.columname = columname;
	}

	/**
	 * @return the javacode
	 */
	public String getJavacode() {
		return javacode;
	}

	/**
	 * @param javacode the javacode to set
	 */
	public void setJavacode(String javacode) {
		this.javacode = javacode;
	}
 
	/**
	 * @return the java_value
	 */
	public Object getJava_value() {
		return java_value;
	}

	/**
	 * @param java_value the java_value to set
	 */
	public void setJava_value(Object java_value) {
		this.java_value = java_value;
	} 
}
