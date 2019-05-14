using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Text;

using Microsoft.SqlServer.Management.Common;
using Microsoft.SqlServer.Replication;

namespace FutureConcepts.Mobile.BLMTest
{
    public class MergeLaunchHelper
    {
        public String SubscriberName
        {
            get
            {
                return "SEATTLE-TEST";
            }
        }
        
        public String SubscriptionDbName
        {
            get
            {
                return "ICDB";
                
            }
        }

        public String PublisherName
        {
            get
            {
                return "sirius";
                
            }
        }

        public String PublicationDbName
        {
            get
            {
                return "ICDB";
            }
        }
                    
        public String PublicationName
        {
            get
            {
                return "ICDBMainPublication";
            }
        }

        private ServerConnection _conn;
        private MergePullSubscription _subscription;

        public MergeLaunchHelper()
        {
            _conn = new ServerConnection(SubscriberName);
            _conn.Connect();
            _subscription = new MergePullSubscription();
            _subscription.ConnectionContext = _conn;
            _subscription.DatabaseName = SubscriptionDbName;
            _subscription.PublisherName = PublisherName;
            _subscription.PublicationDBName = PublicationDbName;
            _subscription.PublicationName = PublicationName;
//            _subscription.SynchronizationAgent.PublisherSecurityMode = SecurityMode.Standard;
   //         _subscription.SynchronizationAgentProcessSecurity.Login = "seattle-test\administrator";
   //         _subscription.SynchronizationAgentProcessSecurity.Password = "shuttle88";

       //     _subscription.PublisherSecurity.SecurityMode = ReplicationSecurityMode.SqlStandard;
       //     _subscription.DistributorSecurity.SqlStandardLogin = "sa";
       //     _subscription.DistributorSecurity.SqlStandardPassword = "shuttle88";

        }

        public void Execute()
        {
            try
            {
                // If the pull subscription exists, then start the synchronization.
                if (_subscription.LoadProperties())
                {
                    // Check that we have enough metadata to start the agent.
                    if (_subscription.PublisherSecurity != null || _subscription.DistributorSecurity != null)
                    {
                        if (true)
                        {
                            MergeSynchronizationAgent agent = _subscription.SynchronizationAgent;
                            agent.PublisherSecurityMode = SecurityMode.Standard;
                            agent.DistributorSecurityMode = SecurityMode.Standard;
                            agent.Distributor = PublisherName;
                            agent.HostName = SubscriberName;
                            agent.SubscriptionType = SubscriptionOption.Pull;
                            agent.ExchangeType = MergeExchangeType.Bidirectional;
                            agent.DistributorLogin = "sa";
                            agent.DistributorPassword = "shuttle88";
                            agent.PublisherLogin = "sa";
                            agent.PublisherPassword = "shuttle88";
                        }
                        // Synchronously start the Merge Agent for the subscription.
                        _subscription.SynchronizationAgent.Synchronize();
                    }
                    else
                    {
                        throw new ApplicationException("There is insufficent metadata to " +
                            "synchronize the subscription. Recreate the subscription with " +
                            "the agent job or supply the required agent properties at run time.");
                    }
                }
                else
                {
                    // Do something here if the pull subscription does not exist.
                    throw new ApplicationException(String.Format("A subscription to '{0}' does not exist on {1}", PublicationName, SubscriberName));
                }
            }
            catch (Exception ex)
            {
                // Implement appropriate error handling here.
                throw new ApplicationException("The subscription could not be " +
                    "synchronized. Verify that the subscription has " +
                    "been defined correctly.", ex);
            }
            finally
            {
                _conn.Disconnect();
            }
        }
    }
}
