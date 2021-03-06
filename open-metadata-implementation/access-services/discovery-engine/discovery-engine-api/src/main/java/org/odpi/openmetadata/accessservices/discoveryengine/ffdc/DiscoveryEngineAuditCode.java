/* SPDX-License-Identifier: Apache-2.0 */
/* Copyright Contributors to the ODPi Egeria project. */
package org.odpi.openmetadata.accessservices.discoveryengine.ffdc;

import org.odpi.openmetadata.frameworks.auditlog.messagesets.AuditLogMessageDefinition;
import org.odpi.openmetadata.frameworks.auditlog.messagesets.AuditLogMessageSet;
import org.odpi.openmetadata.repositoryservices.auditlog.OMRSAuditLogRecordSeverity;


/**
 * The DiscoveryEngineAuditCode is used to define the message content for the OMRS Audit Log.
 *
 * The 5 fields in the enum are:
 * <ul>
 *     <li>Log Message Id - to uniquely identify the message</li>
 *     <li>Severity - is this an event, decision, action, error or exception</li>
 *     <li>Log Message Text - includes placeholder to allow additional values to be captured</li>
 *     <li>Additional Information - further parameters and data relating to the audit message (optional)</li>
 *     <li>SystemAction - describes the result of the situation</li>
 *     <li>UserAction - describes how a user should correct the situation</li>
 * </ul>
 */
public enum DiscoveryEngineAuditCode implements AuditLogMessageSet
{
    SERVICE_INITIALIZING("OMAS-DISCOVERY-ENGINE-0001",
             OMRSAuditLogRecordSeverity.STARTUP,
             "The Discovery Engine Open Metadata Access Service (OMAS) is initializing a new server instance",
             "The local server has started up a new instance of the Discovery Engine OMAS.  It will initialize " +
                                 "Open Discovery Framework (ODF) interfaces for discovery engine configuration, asset catalog search, " +
                                 "asset properties and the annotation store.  It will also begin publishing discovery engine " +
                                 "configuration changes to its out topic.",
             "This is part of the normal start up of the service.  No action is required if this service " +
                                 "startup was intentional."),

    SERVICE_INITIALIZED("OMAS-DISCOVERY-ENGINE-0003",
             OMRSAuditLogRecordSeverity.STARTUP,
             "The Discovery Engine Open Metadata Access Service (OMAS) has initialized a new instance for server {0}",
             "The Discovery Engine OMAS has completed initialization of a new server instance.",
             "Verify that there are no error messages logged by the service.  If there are none it means that " +
                                "all parts of the service initialized successfully."),

    SERVICE_INSTANCE_FAILURE("OMAS-DISCOVERY-ENGINE-0004",
            OMRSAuditLogRecordSeverity.ERROR,
            "The Discovery Engine Open Metadata Access Service (OMAS) is unable to initialize a new instance; error message is {0}",
            "The access service detected an error during the start up of a specific server instance.  Its services are not available for the server.",
            "Review the error message and any other reported failures to determine the cause of the problem.  Once this is resolved, restart the server."),

    UNEXPECTED_INITIALIZATION_EXCEPTION("OMAS-DISCOVERY-ENGINE-0005",
             OMRSAuditLogRecordSeverity.EXCEPTION,
             "The Discovery Engine Open Metadata Access Service (OMAS) detected an unexpected {0} exception during the " +
                                                "initialization of its services; error message is {1}",
             "The access service detected an error during the start up of a specific server instance.  Its services are not available " +
                                                "for the server and an error is returned to the caller.",
             "Review the error message and any other reported failures to determine the cause of the problem.  In particular consider the" +
                                                " state of the Event Bus.  Once this is resolved, restart the server."),

    SERVICE_SHUTDOWN("OMAS-DISCOVERY-ENGINE-0008",
            OMRSAuditLogRecordSeverity.SHUTDOWN,
            "The Discovery Engine Open Metadata Access Service (OMAS) is shutting down its instance for server {0}",
            "The local administrator has requested shut down of an Discovery Engine OMAS instance.  " +
                             "The Open Discovery Framework (ODF) interfaces are no longer available and no configuration events will " +
                             "be published to the out topic",
            "This is part of the normal shutdown of the service.  Verify that all resources have been released."),

    ASSET_AUDIT_LOG("OMAS-DISCOVERY-ENGINE-0013",
             OMRSAuditLogRecordSeverity.INFO,
            "Log message for asset {0} from discovery service {1}: {2}",
            "A discovery service has logged a message about an asset.",
            "Review the message to ensure no action is required.")
    ;

    private final AuditLogMessageDefinition messageDefinition;


    /**
     * The constructor for DiscoveryEngineAuditCode expects to be passed one of the enumeration rows defined in
     * DiscoveryEngineAuditCode above.   For example:
     *
     *     DiscoveryEngineAuditCode   auditCode = DiscoveryEngineAuditCode.SERVER_NOT_AVAILABLE;
     *
     * This will expand out to the 4 parameters shown below.
     *
     * @param messageId - unique Id for the message
     * @param severity - severity of the message
     * @param message - text for the message
     * @param systemAction - description of the action taken by the system when the condition happened
     * @param userAction - instructions for resolving the situation, if any
     */
    DiscoveryEngineAuditCode(String                     messageId,
                             OMRSAuditLogRecordSeverity severity,
                             String                     message,
                             String                     systemAction,
                             String                     userAction)
    {
        messageDefinition = new AuditLogMessageDefinition(messageId,
                                                          severity,
                                                          message,
                                                          systemAction,
                                                          userAction);
    }

    /**
     * Retrieve a message definition object for logging.  This method is used when there are no message inserts.
     *
     * @return message definition object.
     */
    @Override
    public AuditLogMessageDefinition getMessageDefinition()
    {
        return messageDefinition;
    }


    /**
     * Retrieve a message definition object for logging.  This method is used when there are values to be inserted into the message.
     *
     * @param params array of parameters (all strings).  They are inserted into the message according to the numbering in the message text.
     * @return message definition object.
     */
    @Override
    public AuditLogMessageDefinition getMessageDefinition(String ...params)
    {
        messageDefinition.setMessageParameters(params);
        return messageDefinition;
    }
}
