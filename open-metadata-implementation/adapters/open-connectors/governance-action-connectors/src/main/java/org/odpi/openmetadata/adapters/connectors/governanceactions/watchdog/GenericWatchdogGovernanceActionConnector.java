/* SPDX-License-Identifier: Apache 2.0 */
/* Copyright Contributors to the ODPi Egeria project. */
package org.odpi.openmetadata.adapters.connectors.governanceactions.watchdog;

import org.odpi.openmetadata.adapters.connectors.governanceactions.ffdc.GovernanceActionConnectorsAuditCode;
import org.odpi.openmetadata.adapters.connectors.governanceactions.ffdc.GovernanceActionConnectorsErrorCode;
import org.odpi.openmetadata.frameworks.connectors.ffdc.ConnectorCheckedException;
import org.odpi.openmetadata.frameworks.connectors.ffdc.OCFCheckedExceptionBase;
import org.odpi.openmetadata.frameworks.connectors.properties.ConnectionProperties;
import org.odpi.openmetadata.frameworks.governanceaction.WatchdogGovernanceActionService;
import org.odpi.openmetadata.frameworks.governanceaction.events.*;
import org.odpi.openmetadata.frameworks.governanceaction.ffdc.GovernanceServiceException;
import org.odpi.openmetadata.frameworks.governanceaction.properties.ActionTargetElement;
import org.odpi.openmetadata.frameworks.governanceaction.properties.CompletionStatus;
import org.odpi.openmetadata.frameworks.governanceaction.properties.RelatedMetadataElements;
import org.odpi.openmetadata.frameworks.governanceaction.search.ElementProperties;

import java.util.*;

/**
 * GenericWatchdogGovernanceActionConnector provides a base class for generic watchdog functions.
 */
public abstract class GenericWatchdogGovernanceActionConnector extends WatchdogGovernanceActionService
{
    protected List<String> instancesToListenTo = new ArrayList<>();

    protected String newElementProcessName = null;
    protected String updatedElementProcessName = null;
    protected String deletedElementProcessName = null;
    protected String classifiedElementProcessName = null;
    protected String reclassifiedElementProcessName = null;
    protected String declassifiedElementProcessName = null;
    protected String newRelationshipProcessName = null;
    protected String updatedRelationshipProcessName = null;
    protected String deletedRelationshipProcessName = null;

    protected volatile boolean completed = false;



    /**
     * Indicates that the governance action service is completely configured and can begin processing.
     *
     * This is a standard method from the Open Connector Framework (OCF) so
     * be sure to call super.start() at the start of your overriding version.
     *
     * @throws ConnectorCheckedException there is a problem within the governance action service.
     */
    public void start(String defaultTypeName) throws ConnectorCheckedException
    {
        super.start();

        final String methodName = "start";

        GenericWatchdogListener listener = new GenericWatchdogListener(this);

        /*
         * Work out what type of asset to listen for.
         */
        String interestingTypeName = getProperty(GenericElementWatchdogGovernanceActionProvider.INTERESTING_TYPE_NAME_PROPERTY, defaultTypeName);
        String instanceToListenTo = getProperty(GenericElementWatchdogGovernanceActionProvider.INSTANCE_TO_MONITOR_PROPERTY, null);

        if (instanceToListenTo != null)
        {
            instancesToListenTo.add(instanceToListenTo);
        }

        List<ActionTargetElement> actionTargetElements = governanceContext.getActionTargetElements();
        if (actionTargetElements != null)
        {
            for (ActionTargetElement actionTargetElement : actionTargetElements)
            {
                if (actionTargetElement != null)
                {
                    if (GenericWatchdogGovernanceActionProvider.INSTANCE_TO_MONITOR_PROPERTY.equals(actionTargetElement.getActionTargetName()))
                    {
                        instancesToListenTo.add(actionTargetElement.getActionTargetGUID());
                    }
                }
            }
        }

        newElementProcessName = getProperty(GenericElementWatchdogGovernanceActionProvider.NEW_ELEMENT_PROCESS_NAME_PROPERTY, null);
        updatedElementProcessName = getProperty(GenericElementWatchdogGovernanceActionProvider.UPDATED_ELEMENT_PROCESS_NAME_PROPERTY, null);
        deletedElementProcessName = getProperty(GenericElementWatchdogGovernanceActionProvider.DELETED_ELEMENT_PROCESS_NAME_PROPERTY, null);
        classifiedElementProcessName = getProperty(GenericElementWatchdogGovernanceActionProvider.CLASSIFIED_ELEMENT_PROCESS_NAME_PROPERTY, null);
        reclassifiedElementProcessName = getProperty(GenericElementWatchdogGovernanceActionProvider.RECLASSIFIED_ELEMENT_PROCESS_NAME_PROPERTY, null);
        declassifiedElementProcessName = getProperty(GenericElementWatchdogGovernanceActionProvider.DECLASSIFIED_ELEMENT_PROCESS_NAME_PROPERTY, null);
        newRelationshipProcessName = getProperty(GenericElementWatchdogGovernanceActionProvider.NEW_RELATIONSHIP_PROCESS_NAME_PROPERTY, null);
        updatedRelationshipProcessName = getProperty(GenericElementWatchdogGovernanceActionProvider.UPDATED_RELATIONSHIP_PROCESS_NAME_PROPERTY, null);
        deletedRelationshipProcessName = getProperty(GenericElementWatchdogGovernanceActionProvider.DELETED_RELATIONSHIP_PROCESS_NAME_PROPERTY, null);

        try
        {
            /*
             * Setting up the listener
             */
            List<WatchdogEventType> interestingEventTypes = new ArrayList<>();

            if (newElementProcessName != null)
            {
                interestingEventTypes.add(WatchdogEventType.NEW_ELEMENT);
                interestingEventTypes.add(WatchdogEventType.REFRESHED_ELEMENT);
            }
            if (updatedElementProcessName != null)
            {
                interestingEventTypes.add(WatchdogEventType.UPDATED_ELEMENT_PROPERTIES);
            }
            if (deletedElementProcessName != null)
            {
                interestingEventTypes.add(WatchdogEventType.DELETED_ELEMENT);
            }
            if (classifiedElementProcessName != null)
            {
                interestingEventTypes.add(WatchdogEventType.NEW_CLASSIFICATION);
            }
            if (reclassifiedElementProcessName != null)
            {
                interestingEventTypes.add(WatchdogEventType.UPDATED_CLASSIFICATION_PROPERTIES);
            }
            if (declassifiedElementProcessName != null)
            {
                interestingEventTypes.add(WatchdogEventType.DELETED_CLASSIFICATION);
            }
            if (newRelationshipProcessName != null)
            {
                interestingEventTypes.add(WatchdogEventType.NEW_RELATIONSHIP);
            }
            if (updatedRelationshipProcessName != null)
            {
                interestingEventTypes.add(WatchdogEventType.UPDATED_RELATIONSHIP_PROPERTIES);
            }
            if (deletedRelationshipProcessName != null)
            {
                interestingEventTypes.add(WatchdogEventType.DELETED_RELATIONSHIP);
            }

            List<String> interestingMetadataTypes = new ArrayList<>();

            interestingMetadataTypes.add(interestingTypeName);

            if (instancesToListenTo.isEmpty())
            {
                instancesToListenTo = null;

                governanceContext.registerListener(listener,
                                                   interestingEventTypes,
                                                   interestingMetadataTypes,
                                                   null);
            }
            else if (instancesToListenTo.size() == 1)
            {
                governanceContext.registerListener(listener,
                                                   interestingEventTypes,
                                                   interestingMetadataTypes,
                                                   instancesToListenTo.get(0));

                instancesToListenTo = null;
            }
            else
            {
                /*
                 * Will need to filter manually in the listener
                 */
                governanceContext.registerListener(listener,
                                                   interestingEventTypes,
                                                   interestingMetadataTypes,
                                                   null);
            }
        }
        catch (Exception error)
        {
            try
            {
                List<String> outputGuards = new ArrayList<>();

                outputGuards.add(GenericElementWatchdogGovernanceActionProvider.MONITORING_FAILED);
                governanceContext.recordCompletionStatus(CompletionStatus.FAILED, outputGuards, null);
            }
            catch (Exception nestedError)
            {
                if (auditLog != null)
                {
                    auditLog.logException(methodName,
                                          GovernanceActionConnectorsAuditCode.UNABLE_TO_SET_COMPLETION_STATUS.getMessageDefinition(governanceServiceName,
                                                                                                                                   nestedError.getClass().getName(),
                                                                                                                                   nestedError.getMessage()),
                                          nestedError);
                }
            }

            if (auditLog != null)
            {
                auditLog.logException(methodName,
                                      GovernanceActionConnectorsAuditCode.UNABLE_TO_REGISTER_LISTENER.getMessageDefinition(governanceServiceName,
                                                                                                                           error.getClass().getName(),
                                                                                                                           error.getMessage()),
                                      error);
            }

            throw new GovernanceServiceException(GovernanceActionConnectorsErrorCode.UNABLE_TO_REGISTER_LISTENER.getMessageDefinition(governanceServiceName,
                                                                                                                                      error.getClass().getName(),
                                                                                                                                      error.getMessage()),
                                                 error.getClass().getName(),
                                                 methodName,
                                                 error);
        }
    }


    /**
     * This method is called each time a potentially new asset is received.  It triggers a governance action process to validate and
     * enrich the asset as required.
     *
     * @param event event containing details of a change to an open metadata element.
     *
     * @throws GovernanceServiceException reports that the event can not be processed (this is logged but
     *                                    no other action is taken).  The listener will continue to be
     *                                    called until the watchdog governance action service declares it is complete
     *                                    or administrator action shuts down the service.
     */
    abstract void processEvent(WatchdogGovernanceEvent event) throws GovernanceServiceException;


    /**
     * Return a comma separated list of properties that have changed.  This is saved in the request parameters
     * and can be retrieved by the receiving governance service.  The changed properties can be retrieved by the
     * StringTokenizer class.
     *
     * @param oldProperties original property values
     * @param newProperties new property values
     * @return comma separated list of property names where the value has changed
     */
    protected String diffProperties(ElementProperties oldProperties,
                                    ElementProperties newProperties)
    {
        String propertyList = null;

        if (oldProperties == null)
        {
            if (newProperties != null)
            {
                Iterator<String> propertyIterator = newProperties.getPropertyNames();

                while (propertyIterator.hasNext())
                {
                    if (propertyList == null)
                    {
                        propertyList = propertyIterator.next();
                    }
                    else
                    {
                        propertyList = propertyList + ", " + propertyIterator.next();
                    }
                }
            }
        }
        else if (newProperties == null)
        {
            Iterator<String> propertyIterator = oldProperties.getPropertyNames();

            while (propertyIterator.hasNext())
            {
                if (propertyList == null)
                {
                    propertyList = propertyIterator.next();
                }
                else
                {
                    propertyList = propertyList + ", " + propertyIterator.next();
                }
            }
        }
        else
        {
            List<String> oldPropertyNames = new ArrayList<>();
            List<String> newPropertyNames = new ArrayList<>();

            Iterator<String> propertyIterator = oldProperties.getPropertyNames();

            while (propertyIterator.hasNext())
            {
                oldPropertyNames.add(propertyIterator.next());
            }

            propertyIterator = newProperties.getPropertyNames();

            while (propertyIterator.hasNext())
            {
                String newPropertyName = propertyIterator.next();

                newPropertyNames.add(newPropertyName);

                if (oldPropertyNames.contains(newPropertyName))
                {
                    if (! oldProperties.getPropertyValue(newPropertyName).equals(newProperties.getPropertyValue(newPropertyName)))
                    {
                        if (propertyList == null)
                        {
                            propertyList = newPropertyName;
                        }
                        else
                        {
                            propertyList = propertyList + ", " + newPropertyName;
                        }
                    }
                }
            }

            for (String oldPropertyName : oldPropertyNames)
            {
                if (! newPropertyNames.contains(oldPropertyName))
                {
                    if (propertyList == null)
                    {
                        propertyList = oldPropertyName;
                    }
                    else
                    {
                        propertyList = propertyList + ", " + oldPropertyName;
                    }
                }
            }
        }

        return propertyList;
    }


    /**
     * Initiates the request for the new process to start if a process name has been configured.
     *
     * @param processName name of the process to start
     * @param requestParameters parameters to pass to the process
     * @param actionTargetGUIDs the elements that this process should work on
     * @throws GovernanceServiceException unable to start the process
     */
    protected void initiateProcess(String              processName,
                                   Map<String, String> requestParameters,
                                   List<String>        actionTargetGUIDs) throws GovernanceServiceException
    {
        if (processName != null)
        {
            try
            {
                governanceContext.initiateGovernanceActionProcess(processName,
                                                                  requestParameters,
                                                                  null,
                                                                  actionTargetGUIDs,
                                                                  null);
            }
            catch (OCFCheckedExceptionBase nestedError)
            {
                // todo log error
                if (auditLog != null)
                {

                }

                try
                {
                    List<String> outputGuards = new ArrayList<>();
                    outputGuards.add(GenericElementWatchdogGovernanceActionProvider.MONITORING_FAILED);

                    governanceContext.recordCompletionStatus(CompletionStatus.FAILED, outputGuards, null);
                }
                catch (Exception contextError)
                {
                    // todo log error
                    if (auditLog != null)
                    {

                    }
                }
                throw new GovernanceServiceException(nestedError.getMessage(), nestedError);
            }
        }
    }


    /**
     * Retrieve the property value from the values passed to this governance action service.
     *
     * @param propertyName name of the property
     * @param defaultValue default value
     * @return property value
     */
    private String getProperty(String propertyName, String defaultValue)
    {
        Map<String, String> requestParameters = governanceContext.getRequestParameters();
        Map<String, Object> configurationProperties = connectionProperties.getConfigurationProperties();

        String propertyValue = defaultValue;

        if ((requestParameters != null) && (requestParameters.get(propertyName) != null))
        {
            propertyValue = requestParameters.get(propertyName);
        }
        else
        {
            if ((configurationProperties != null) && (configurationProperties.get(propertyName) != null))
            {
                propertyValue = configurationProperties.get(propertyName).toString();
            }
        }

        return propertyValue;
    }


    /**
     * Disconnect is called either because this governance action service called governanceContext.recordCompletionStatus()
     * or the administer requested this governance action service stop running or the hosting server is shutting down.
     *
     * If disconnect completes before the governance action service records
     * its completion status then the governance action service is restarted either at the administrator's request or the next time the server starts.
     * If you do not want this governance action service restarted, be sure to record the completion status in disconnect().
     *
     * The disconnect() method is a standard method from the Open Connector Framework (OCF).  If you need to override this method
     * be sure to call super.disconnect() in your version.
     *
     * @throws ConnectorCheckedException there is a problem within the governance action service.
     */
    @Override
    public  void disconnect() throws ConnectorCheckedException
    {
        super.disconnect();
    }
}