using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.IO;
using System.Linq;
using System.Reflection;
using System.ServiceModel;
using System.ServiceModel.Web;

using FutureConcepts.Data.Access.CDAL;
using L2S = FutureConcepts.Data.Access.CDAL.L2S;
using Model = FutureConcepts.Data.Access.Model;

using FutureConcepts.Mobile.DataContract;
using FutureConcepts.Mobile.ServiceContract;
using FutureConcepts.Tools;

using FutureConcepts.Settings;

namespace FutureConcepts.Mobile.Server.WebService
{
    [ServiceBehavior(InstanceContextMode = InstanceContextMode.Single, ConcurrencyMode = ConcurrencyMode.Multiple)]
    public class Admin : IAdmin, IDisposable
    {
        private readonly String TAG = "Admin: ";
        private DeviceContextManager _deviceContextManager;
        private SqlProvider _sqlProvider;

        public Admin()
        {
            _deviceContextManager = DeviceContextManager.Instance;
            MobileServerSettings settings = new MobileServerSettings();
            _sqlProvider = ProviderFactory.CreateProvider(settings.ICDBIsolatedConnectionString);
        }

        public void Dispose()
        {
            Debug.WriteLine(TAG + " Dispose");
        }

        private void Logit(String proc, String message)
        {
            try
            {
                DeviceContext deviceContext = _deviceContextManager.GetDeviceContextFromWebContext();
                if (deviceContext != null)
                {
                    DeviceLog.Log(deviceContext, 0, proc, message);
                }
            }
            catch (Exception e)
            {
                SystemLog.Log(0, proc, message);
            }
        }

        public String Ping()
        {
            return "Pong";
        }

        public ServerStatus GetStatus()
        {
            Logit("GetStatus", "");
            ServerStatus stat = new ServerStatus();
            stat.Version = Assembly.GetAssembly(typeof(Admin)).ToString();
            stat.MobileServerSettings = new MobileServerSettings();
            stat.WSUSSettings = new WSUSSettings();

            Dictionary<String, DeviceContextGroup> agencyDeviceContextsDict = new Dictionary<String, DeviceContextGroup>();
            List<DeviceContext> devices = DeviceContextManager.Instance.Devices;
            foreach (DeviceContext deviceContext in devices)
            {
                String agencyName = "Unknown";
                if (deviceContext.AgencyName != null)
                {
                    agencyName = deviceContext.AgencyName;
                }
                DeviceContextGroup agencyDeviceContexts = null;
                if (agencyDeviceContextsDict.ContainsKey(agencyName))
                {
                    agencyDeviceContexts = agencyDeviceContextsDict[agencyName];
                }
                else
                {
                    agencyDeviceContexts = new DeviceContextGroup(agencyName);
                    agencyDeviceContextsDict.Add(agencyName, agencyDeviceContexts);
                }
                agencyDeviceContexts.DeviceContexts.Add(deviceContext);
            }
            stat.AgencyDeviceContexts = agencyDeviceContextsDict.Values.ToList();
            return stat;
        }

        public TransactionContextV3 GetConfigurationWithID(String deviceId)
        {
            Logit("GetConfigurationWithID", deviceId);
            TransactionContextV3 result = new TransactionContextV3();
            try
            {
                DeviceContext deviceContext = _deviceContextManager.GetDeviceContext(deviceId);
                DoGetConfiguration(result, deviceContext);
            }
            catch (Exception e)
            {
                ErrorLogger.DumpToDebug(e);
                result.ErrorMessage = e.Message;
            }
            return result;
        }

        public TransactionContextV3 GetConfiguration()
        {
            Logit("GetConfiguration", "");
            TransactionContextV3 result = new TransactionContextV3();
            try
            {
                _deviceContextManager.ClearDeviceContextFromWebContext();
                DeviceContext deviceContext = _deviceContextManager.GetDeviceContextFromWebContext();
                DoGetConfiguration(result, deviceContext);
            }
            catch (Exception e)
            {
                ErrorLogger.DumpToDebug(e);
                result.ErrorMessage = e.Message;
            }
            return result;
        }

        private void DoGetConfiguration(TransactionContextV3 result, DeviceContext deviceContext)
        {
            Logit("DoGetConfiguration", deviceContext.DeviceId);
            DeviceConfiguration deviceConfiguration = new DeviceConfiguration(deviceContext);
            if (deviceContext.EquipmentID != null)
            {
                Guid equipmentID = deviceContext.EquipmentID.Value;
                try
                {
                    MobileServerSettings settings = new MobileServerSettings();
                    SqlProvider provider = ProviderFactory.CreateProvider(settings.ICDBIsolatedConnectionString);
                    using (L2S.ICDBDataContext db = provider.CreateDataContext())
                    {
                        var query =
                            from equipmentCommunication in db.EquipmentCommunication
                            where equipmentCommunication.Equipment == equipmentID && equipmentCommunication.CommunicationCommunication.Phone != null && equipmentCommunication.CommunicationCommunication.IsPrimary
                            select equipmentCommunication.CommunicationCommunication.PhonePhone;
                        List<L2S.Phone> phoneList = query.ToList();
                        if (phoneList.Count == 1)
                        {
                            L2S.Phone phone = phoneList.First();
                            if (phone != null)
                            {
                                deviceConfiguration.PhoneNumber = Convert.ToString(phone.Number);
                            }
                        }
                    }
                }
                catch (Exception e)
                {
                }
            }
            List<DeviceConfiguration> list = new List<DeviceConfiguration>();
            list.Add(deviceConfiguration);
            result.PostContentAction("insert", list);
        }

        public void PurgeQueue(String deviceId)
        {
            Logit("PurgeQueue", deviceId);
            Queue.MobileQueue queue = Queue.QueueManager.GetQueue(deviceId);
            queue.Purge();
        }

        public void PurgeAllQueues()
        {
            Logit("PurgeAllQueues", null);
            //      IList<Model.Asset> assets = MobileDeviceManager.Instance.MobileDevices;
            IList<Model.Asset> assets = null; // TODO FIXME
            foreach (Model.Asset asset in assets)
            {
                if (asset.Equipment != null)
                {
                    String deviceId = asset.Equipment.DeviceID;
                    Queue.MobileQueue queue = Queue.QueueManager.GetQueue(deviceId);
                    queue.Purge();
                }
            }
        }

        public IList<String> ShowQueue(String deviceId)
        {
            Logit("ShowQueue", deviceId);
#if OLD
            List<Intent> entries = new List<Intent>();
            Push.Queue queue = Push.QueueManager.GetQueue(deviceId);
            System.Messaging.Message[] messages = queue.PeekAll();
            foreach (System.Messaging.Message message in messages)
            {
                Intent intent = Intent.GetFromJSONString((String)message.Body);
                entries.Add(intent);
            }
            return entries;   
#endif
            List<String> entries = new List<String>();
            Queue.MobileQueue queue = Queue.QueueManager.GetQueue(deviceId);
            System.Messaging.Message[] messages = queue.PeekAll();
            foreach (System.Messaging.Message message in messages)
            {
                String jsonString = (String)message.Body;
                entries.Add(jsonString);
            }
            return entries;
        }

        public void StartTrackerOnDevice(String deviceId, String mode)
        {
            Logit("StartTrackerOnDevice", String.Format("DeviceID={0} Mode={1}", deviceId, mode));
            Queue.MobileQueue queue = Queue.QueueManager.GetQueue(deviceId);
            Intent intent = new Intent(Intent.ACTION_START_TRACKER);
            intent.PutExtra("tracker_mode", mode);
       //     queue.Send(intent);
        }

        public void StopTrackerOnDevice(String deviceId)
        {
            Logit("StopTrackerOnDevice", deviceId);
            Queue.MobileQueue queue = Queue.QueueManager.GetQueue(deviceId);
            Intent intent = new Intent(Intent.ACTION_STOP_TRACKER);
         //   queue.Send(intent);
        }
    }
}
