/* SPDX-License-Identifier: Apache 2.0 */
/* Copyright Contributors to the ODPi Egeria project. */

package org.odpi.openmetadata.accessservices.itinfrastructure.connectors.outtopic;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.odpi.openmetadata.accessservices.itinfrastructure.events.ITInfrastructureOutTopicEvent;
import org.odpi.openmetadata.accessservices.itinfrastructure.ffdc.ITInfrastructureAuditCode;
import org.odpi.openmetadata.accessservices.itinfrastructure.ffdc.ITInfrastructureErrorCode;
import org.odpi.openmetadata.frameworks.connectors.ffdc.ConnectorCheckedException;
import org.odpi.openmetadata.frameworks.connectors.ffdc.InvalidParameterException;
import org.odpi.openmetadata.repositoryservices.connectors.openmetadatatopic.OpenMetadataTopicSenderConnectorBase;


/**
 * ITInfrastructureOutTopicServerConnector is the java implementation of the
 * the server side connector that send events to the IT Infrastructure OMAS's OutTopic.
 */
public class ITInfrastructureOutTopicServerConnector extends OpenMetadataTopicSenderConnectorBase
{
    /**
     * Send the request to the embedded event bus connector(s).
     *
     * @param event event object
     * @throws InvalidParameterException the event is null
     * @throws ConnectorCheckedException there is a problem with the embedded event bus connector(s).
     */
    public void sendEvent(ITInfrastructureOutTopicEvent event) throws InvalidParameterException, ConnectorCheckedException
    {
        final String methodName = "sendEvent";
        ObjectMapper objectMapper = new ObjectMapper();

        try
        {
            String eventString = objectMapper.writeValueAsString(event);
            super.sendEvent(eventString);

            if (super.auditLog != null)
            {
                super.auditLog.logMessage(methodName,
                                          ITInfrastructureAuditCode.OUT_TOPIC_EVENT.getMessageDefinition(event.getEventType().getEventTypeName()),
                                          eventString);
            }
        }
        catch (InvalidParameterException | ConnectorCheckedException error)
        {
            throw error;
        }
        catch (Exception  error)
        {
            throw new ConnectorCheckedException(ITInfrastructureErrorCode.UNABLE_TO_SEND_EVENT.getMessageDefinition(connectionName,
                                                                                                                    event.toString(),
                                                                                                                    error.getClass().getName(),
                                                                                                                    error.getMessage()),
                                                this.getClass().getName(),
                                                methodName,
                                                error);
        }
    }
}
