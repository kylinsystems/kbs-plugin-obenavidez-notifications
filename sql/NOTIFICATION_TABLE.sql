-- Table: ni_notificationsetup

-- DROP TABLE ni_notificationsetup;

CREATE TABLE ni_notificationsetup
(
  ad_client_id numeric(10,0) NOT NULL,
  ad_org_id numeric(10,0) NOT NULL,
  isactive character(1) NOT NULL DEFAULT 'Y'::bpchar,
  created timestamp without time zone NOT NULL DEFAULT now(),
  createdby numeric(10,0) NOT NULL,
  updated date NOT NULL DEFAULT now(),
  updatedby numeric(10,0) NOT NULL,
  smtp_server character varying(500) NOT NULL,
  port integer NOT NULL,
  remitent_account character varying(500)NOT NULL,
  remitent_accountpassword character varying(20) NOT NULL,
  ni_notificationsetup_id numeric(10,0) NOT NULL DEFAULT NULL::numeric,
  isdefault character(1) NOT NULL DEFAULT 'N'::bpchar,
  CONSTRAINT ni_notificationsetup_key PRIMARY KEY (ni_notificationsetup_id),
  CONSTRAINT ni_notificationmessage_isactive_check CHECK (isactive = ANY (ARRAY['Y'::bpchar, 'N'::bpchar]))
)
WITH (
  OIDS=FALSE
);
ALTER TABLE ni_notificationsetup
  OWNER TO adempiere;


-- Table: ni_notificationmessage

-- DROP TABLE ni_notificationmessage;

CREATE TABLE ni_notificationmessage
(
  ad_client_id numeric(10,0) NOT NULL,
  ad_org_id numeric(10,0) NOT NULL,
  isactive character(1) NOT NULL DEFAULT 'Y'::bpchar,
  created timestamp without time zone NOT NULL DEFAULT now(),
  createdby numeric(10,0) NOT NULL,
  updated date NOT NULL DEFAULT now(),
  updatedby numeric(10,0) NOT NULL,
  sendto text,
  carboncopy text,
  blindcarboncopy text,
  subjectmessage character varying(1000),
  bodymessage text NOT NULL,
  btntest character(1) DEFAULT NULL::bpchar,
  ni_notificationmessage_id numeric(10,0) NOT NULL DEFAULT NULL::numeric,
  ni_notificationmessage_uu character varying(36) DEFAULT NULL::character varying,
  ad_role_id numeric,
  CONSTRAINT ni_notificationmessage_key PRIMARY KEY (ni_notificationmessage_id),
  CONSTRAINT ni_notificationmessage_isactive_check CHECK (isactive = ANY (ARRAY['Y'::bpchar, 'N'::bpchar]))
)
WITH (
  OIDS=FALSE
);
ALTER TABLE ni_notificationmessage
  OWNER TO adempiere;
  
  -- Table: ni_notificationparameter

-- DROP TABLE ni_notificationparameter;

CREATE TABLE ni_notificationparameter
(
  ad_client_id numeric(10,0) NOT NULL,
  ad_org_id numeric(10,0) NOT NULL,
  isactive character(1) NOT NULL DEFAULT 'Y'::bpchar,
  created timestamp without time zone NOT NULL DEFAULT now(),
  createdby numeric(10,0) NOT NULL,
  updated date NOT NULL DEFAULT now(),
  updatedby numeric(10,0) NOT NULL,
  seqno numeric(10,0) NOT NULL,
  name character varying(60),
  columname character varying(30),
  description character varying(255),
  javacode text,
  ni_notificationparameter_id numeric(10,0) NOT NULL DEFAULT NULL::numeric,
  ni_notificationparameter_uu character varying(36) DEFAULT NULL::character varying,
  ni_notificationmessage_id numeric(10,0) NOT NULL,
  CONSTRAINT ni_notificationparameter_key PRIMARY KEY (ni_notificationparameter_id),
  CONSTRAINT ni_notificationparameter_foreignkey FOREIGN KEY (ni_notificationmessage_id)
      REFERENCES ni_notificationmessage (ni_notificationmessage_id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION DEFERRABLE INITIALLY DEFERRED,
  CONSTRAINT ni_notificationparameter_isactive_check CHECK (isactive = ANY (ARRAY['Y'::bpchar, 'N'::bpchar]))
)
WITH (
  OIDS=FALSE
);
ALTER TABLE ni_notificationparameter
  OWNER TO adempiere;

-- Index: ni_notificationparameter_seqno

-- DROP INDEX ni_notificationparameter_seqno;

CREATE UNIQUE INDEX ni_notificationparameter_seqno
  ON ni_notificationparameter
  USING btree
  (ni_notificationparameter_id, seqno);

 -- Table: ni_notification_trigguer

-- DROP TABLE ni_notification_trigguer;

CREATE TABLE ni_notification_trigguer
(
  ad_client_id numeric(10,0) NOT NULL,
  ad_org_id numeric(10,0) NOT NULL,
  isactive character(1) NOT NULL DEFAULT 'Y'::bpchar,
  created timestamp without time zone NOT NULL DEFAULT now(),
  createdby numeric(10,0) NOT NULL,
  updated date NOT NULL DEFAULT now(),
  updatedby numeric(10,0) NOT NULL,
  ad_process_id numeric(10,0),
  ad_table_id numeric(10,0),
  event_modeldoc_validator character varying(30) NOT NULL,
  ni_notification_trigguer_id numeric(10,0) NOT NULL DEFAULT NULL::numeric,
  ni_notificationmessage_id numeric(10,0) NOT NULL DEFAULT NULL::numeric,
  ni_notification_trigguer_uu character varying(36) DEFAULT NULL::character varying,
  CONSTRAINT ni_notification_trigguer_key PRIMARY KEY (ni_notification_trigguer_id),
  CONSTRAINT ninotificationtrigguer_foreignkey FOREIGN KEY (ni_notificationmessage_id)
      REFERENCES ni_notificationmessage (ni_notificationmessage_id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION DEFERRABLE INITIALLY DEFERRED,
  CONSTRAINT ni_notification_trigguer_isactive_check CHECK (isactive = ANY (ARRAY['Y'::bpchar, 'N'::bpchar])),
  CONSTRAINT ninotificationtrigguer_tableprocess CHECK (ad_process_id IS NULL AND ad_table_id IS NOT NULL OR ad_process_id IS NOT NULL AND ad_table_id IS NULL)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE ni_notification_trigguer
  OWNER TO adempiere;