using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.IO;
using System.Net.NetworkInformation;
using System.Reflection;
using System.ServiceModel;
using System.ServiceModel.Description;
using System.ServiceModel.Web;
using System.ServiceProcess;
using System.Text;

using FutureConcepts.Settings;

namespace FutureConcepts.Mobile.Server
{
    public partial class WindowService : ServiceBase
    {
        private static List<Type> _serviceTypes = new List<Type> {
            typeof(WebService.Authenticate),
            typeof(WebService.ICDBResource),
            typeof(WebService.Alert),
            typeof(WebService.TrackerV3),
            typeof(WebService.Admin),
        };
        private List<WebServiceHost> _serviceHosts = new List<WebServiceHost>();
        private ServiceHost _diagServiceHost;

        private Push.PushService _pushService;
//        private SQLServiceBrokerQueueWatcher.WatcherThread _sqlDataWatcherThread;

        public bool ServiceMode
        {
            get; set;
        }

        public WindowService()
        {
            InitializeComponent();
            CanShutdown = true;
        }

        public void StartDebug()
        {
            ServiceMode = false;
            OnStart(new string[] { });
        }

        public void EndDebug()
        {
            OnStop();
        }

        protected override void OnStart(string[] args)
        {
            Debug.WriteLine(DateTime.Now.ToString() + " " + Assembly.GetAssembly(typeof(WebService.Admin)).ToString());
            LogMessage("OnStart");
            foreach (Type type in _serviceTypes)
            {
                AddWebServiceHost(type);
            }
            AddDiagServiceHost();
            _pushService = Push.PushService.Instance;
            VerifyDatabaseConnection();
            VerifyLDAPConnection();
     //       _sqlDataWatcherThread = new SQLServiceBrokerQueueWatcher.WatcherThread();
            Debug.WriteLine("MobileService is READY");
        }

        private void VerifyDatabaseConnection()
        {
//            if (ICDBDataContextFactory.Instance.CheckDatabase() == false)
 //           {
  //              SystemLog.Log(0, "OnStart", "FAILED DATABASE CHECK");
   //         }
        }

        private void VerifyLDAPConnection()
        {
            ConnectionSettings connectionSettings = new ConnectionSettings();
            String domain = connectionSettings.LdapDomainPath;
            Uri uri = new Uri(domain);
            Ping ping = new Ping();
            try
            {
                PingReply reply = ping.Send(uri.Host);
                if (reply.Status != IPStatus.Success)
                {
                    Exception innerException = new Exception("PingReply not Success");
                    innerException.Data.Add("ReplyStatus", reply.Status.ToString());
                    throw innerException;
                }
                SystemLog.Log(0, "VerifyLDAPConnection", String.Format("LDAP Domain {0} PingReply.RoundtripTime {1}", domain, reply.RoundtripTime));
            }
            catch (Exception pingException)
            {
                Exception e = new Exception("LDAP Domain ping failed", pingException);
                e.Data.Add("Host", uri.Host);
                throw e;
            }
        }

        protected override void OnStop()
        {
            LogMessage("OnStop");
        //    if (_sqlDataWatcherThread != null)
         //   {
          //      _sqlDataWatcherThread.Dispose();
          //  }
            if (_pushService != null)
            {
                _pushService.Dispose();
            }
            foreach (WebServiceHost serviceHost in _serviceHosts)
            {
                serviceHost.Close();
            }
        }

        protected override void OnShutdown()
        {
            LogMessage("OnShutdown");
            OnStop();
        }

        private void AddWebServiceHost(Type service)
        {
            WebServiceHost serviceHost = null;
            serviceHost = new WebServiceHost(service);
            serviceHost.Opening += new EventHandler(serviceHost_Opening);
            serviceHost.Opened += new EventHandler(serviceHost_Opened);
            serviceHost.Faulted += new EventHandler(serviceHost_Faulted);
            serviceHost.Closing += new EventHandler(serviceHost_Closing);
            serviceHost.Closed += new EventHandler(serviceHost_Closed);
            serviceHost.UnknownMessageReceived += new EventHandler<UnknownMessageReceivedEventArgs>(serviceHost_UnknownMessageReceived);
            serviceHost.Open();
            _serviceHosts.Add(serviceHost);
        }

        private void AddDiagServiceHost()
        {
            _diagServiceHost = AddServiceHost(typeof(WebService.Diag));
        }

        private ServiceHost AddServiceHost(Type service)
        {
            ServiceHost serviceHost = null;
            serviceHost = new ServiceHost(service);
            serviceHost.Opening += new EventHandler(serviceHost_Opening);
            serviceHost.Opened += new EventHandler(serviceHost_Opened);
            serviceHost.Faulted += new EventHandler(serviceHost_Faulted);
            serviceHost.Closing += new EventHandler(serviceHost_Closing);
            serviceHost.Closed += new EventHandler(serviceHost_Closed);
            serviceHost.UnknownMessageReceived += new EventHandler<UnknownMessageReceivedEventArgs>(serviceHost_UnknownMessageReceived);
            serviceHost.Open();
            return serviceHost;
        }

        private void serviceHost_Opening(object sender, EventArgs e)
        {
#if SHOW_OPENING
            ServiceHost host = (ServiceHost)sender;
            Debug.WriteLine("WCF Service " + host.BaseAddresses[0].AbsoluteUri + " (" + host.State.ToString() + ")");
#endif
        }

        private void serviceHost_Opened(object sender, EventArgs e)
        {
            ServiceHost host = (ServiceHost)sender;
            Debug.WriteLine("WCF Service " + host.Description.ConfigurationName + " (" + host.State.ToString() + ")");
            foreach (ServiceEndpoint serviceEndpoint in host.Description.Endpoints)
            {
                Debug.WriteLine(String.Format("  {0} ({1})", serviceEndpoint.Address.ToString(), serviceEndpoint.Binding.Name));
            }
        }

        private void serviceHost_Faulted(object sender, EventArgs e)
        {
            ServiceHost host = (ServiceHost)sender;
            //            Debug.WriteLine("WCF Service " + host.BaseAddresses[0].AbsoluteUri + " (" + host.State.ToString() + ")");
        }

        private void serviceHost_Closing(object sender, EventArgs e)
        {
#if SHOW_CLOSING
            ServiceHost host = (ServiceHost)sender;
            Debug.WriteLine("WCF Service " + host.BaseAddresses[0].AbsoluteUri + " (" + host.State.ToString() + ")");
#endif
        }

        private void serviceHost_Closed(object sender, EventArgs e)
        {
            ServiceHost host = (ServiceHost)sender;
            Debug.WriteLine("WCF Service " + host.Description.ConfigurationName + " (" + host.State.ToString() + ")");
        }

        private void serviceHost_UnknownMessageReceived(object sender, UnknownMessageReceivedEventArgs e)
        {
            ServiceHost host = (ServiceHost)sender;
            Debug.WriteLine("WCF Service " + host.BaseAddresses[0].AbsoluteUri + e.Message);
        }

        public static void LogMessage(String message)
        {
            Debug.WriteLine(message);
            String path = Utils.AssemblyPath + "\\" + "MobileServerEvents.log";
            FileStream fileStream = File.Open(path, FileMode.Append);
            byte[] bytes = Encoding.ASCII.GetBytes(DateTime.Now.ToString() + ": " + message);
            fileStream.Write(bytes, 0, bytes.Length);
            fileStream.Close();
        }
    }
}
