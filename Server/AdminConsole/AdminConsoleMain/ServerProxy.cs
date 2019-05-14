using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Diagnostics;
using System.Linq;
using System.Text;
using System.ServiceModel;
using System.ServiceModel.Description;
using System.ServiceModel.Web;

using FutureConcepts.Mobile.DataContract;
using FutureConcepts.Mobile.ServiceContract;
using FutureConcepts.Mobile.DataModel;

namespace FutureConcepts.Mobile.AdminConsole
{
    public class ServerProxy
    {
        private ChannelFactory<IAdmin> _proxyFactory;
        private IAdmin _proxy;

        private IAdmin ManagedProxy
        {
            get
            {
                if (_proxy == null)
                {
                    String serverUrl = Properties.Settings.Default.ServerUrl;
                    WebHttpBinding binding = new WebHttpBinding(WebHttpSecurityMode.None);
                    _proxyFactory = new ChannelFactory<IAdmin>(binding, serverUrl + "/Admin");
                    _proxyFactory.Endpoint.Behaviors.Add(new WebHttpBehavior());
                    _proxyFactory.Open();
                    _proxy = _proxyFactory.CreateChannel();
                }
                return _proxy;
            }
        }

        private ServerProxy()
        {
            Properties.Settings.Default.PropertyChanged += new System.ComponentModel.PropertyChangedEventHandler(settings_PropertyChanged);
        }

        private void settings_PropertyChanged(object sender, PropertyChangedEventArgs e)
        {
            if (e.PropertyName == "ServerUrl")
            {
                Close();
            }
        }

        private static ServerProxy _instance;

        public static ServerProxy Instance
        {
            get
            {
                if (_instance == null)
                {
                    _instance = new ServerProxy();
                }
                return _instance;
            }
        }

        public void Close()
        {
            if (_proxy != null)
            {
                _proxyFactory.Close();
                _proxyFactory = null;
                _proxy = null;
            }
        }

        public ServerStatus GetStatus()
        {
            return ManagedProxy.GetStatus();
        }

        public void StartTrackerOnDevice(String deviceId, String mode)
        {
            ManagedProxy.StartTrackerOnDevice(deviceId, mode);
        }

        public void StopTrackerOnDevice(String deviceId)
        {
            ManagedProxy.StopTrackerOnDevice(deviceId);
        }

        public TransactionContext CreateDebugDevice()
        {
            return ManagedProxy.CreateDebugDevice();
        }

        public TransactionContext CreateDevice(MobileDevice parameters)
        {
            return ManagedProxy.CreateDevice(parameters);
        }

        public TransactionContext TestCheckUpdate()
        {
            return ManagedProxy.CheckUpdate("Android", "5", null);
        }
    }
}
