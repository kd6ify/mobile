using System;
using System.Collections.Generic;
using System.Collections.Specialized;
using System.Data;
using System.Data.SqlClient;
using System.Configuration;
using System.Linq;
using System.ServiceModel.Web;

using FutureConcepts.Mobile.DataContract;
using FutureConcepts.Settings;

namespace FutureConcepts.Mobile.Server
{
    public class DeviceContextManager
    {
        private static DeviceContextManager _instance;

        private static Object _staticLock = new Object();

        private Dictionary<String, DeviceContext> _dict;

        public List<DeviceContext> Devices
        {
            get
            {
                return _dict.Values.ToList();
            }
        }

        public static DeviceContextManager Instance
        {
            get
            {
                lock (_staticLock)
                {
                    if (_instance == null)
                    {
                        _instance = new DeviceContextManager();
                    }
                    return _instance;
                }
            }
        }

        private DeviceContextManager()
        {
            _dict = new Dictionary<String, DeviceContext>();
        }

        public DeviceContext GetDeviceContext(String deviceID)
        {
            DeviceContext result = null;
            lock (_dict)
            {
                if (_dict.ContainsKey(deviceID))
                {
                    result = _dict[deviceID];
                }
                else
                {
                    NameValueCollection querySection = (NameValueCollection)ConfigurationManager.GetSection("EquipmentWhereDeviceID");
                    if (querySection != null)
                    {
                        MobileServerSettings settings = new MobileServerSettings();
                        using (SqlConnection connection = new SqlConnection(settings.ICDBIsolatedConnectionString))
                        {
                            String query = querySection["Initial"];
                            if (query != null)
                            {
                                DataTable dataTable = new DataTable();
                                using (SqlCommand command = new SqlCommand(String.Format(query, deviceID), connection))
                                {
                                    using (SqlDataAdapter adapter = new SqlDataAdapter(command))
                                    {
                                        adapter.Fill(dataTable);
                                    }
                                    int count = dataTable.Rows.Count;
                                    if (count > 0)
                                    {
                                        result = new DeviceContext(deviceID, DeviceContext.PUSH_STATE_DISCONNECTED);
                                        Nullable<Guid> equipmentID = (Nullable<Guid>)dataTable.Rows[0]["ID"];
                                        if (equipmentID != null)
                                        {
                                            result.EquipmentID = equipmentID.Value;
                                        }
                                        else
                                        {
                                            result.EquipmentID = null;
                                        }
                                        result.EquipmentName = (String)dataTable.Rows[0]["Name"];
                                        _dict.Add(deviceID, result);
                                    }
                                    else
                                    {
                                        throw new UnregisteredDeviceException();
                                    }

                                }
                            }
                        }
                    }
                }
            }
            return result;
        }

        public void ClearDeviceContextFromWebContext()
        {
            try
            {
                WebOperationContext context = WebOperationContext.Current;
                String deviceId = context.IncomingRequest.Headers["DeviceId"];
                lock (_dict)
                {
                    if (_dict.ContainsKey(deviceId))
                    {
                        _dict.Remove(deviceId);
                    }
                }
            }
            catch (Exception e) { }
        }

        public DeviceContext GetDeviceContextFromWebContext()
        {
            DeviceContext result = null;
            WebOperationContext context = WebOperationContext.Current;
            String deviceId = context.IncomingRequest.Headers["DeviceId"];
            if (deviceId != null)
            {
                result = GetDeviceContext(deviceId);
            }
            else
            {
                throw new UnregisteredDeviceException();
            }
            return result;
        }
    }
}
