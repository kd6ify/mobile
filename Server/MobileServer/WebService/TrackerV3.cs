using System;
using System.Collections.Generic;
using System.Collections.Specialized;
using System.ComponentModel;
using System.Configuration;
using System.Drawing;
using System.Diagnostics;
using System.IO;
using System.Linq;
using System.Reflection;
using System.Runtime.Serialization;
using System.ServiceModel;
using System.ServiceModel.Channels;
using System.ServiceModel.Web;
using System.Data;
using System.Data.Sql;
using System.Data.SqlClient;
using System.Data.SqlTypes;
using System.Text;
using System.Web;
using System.Xml;
using System.Xml.Linq;
using System.Xml.Serialization;
using System.Xml.XPath;

using FutureConcepts.Mobile.DataContract;
using FutureConcepts.Mobile.ServiceContract;
using FutureConcepts.Tools;

using FutureConcepts.Data.Access;
using FutureConcepts.Data.Access.CDAL;
//using FutureConcepts.Data.Access.Util;
using L2S = FutureConcepts.Data.Access.CDAL.L2S;
using Model = FutureConcepts.Data.Access.Model;

using FutureConcepts.Settings;

namespace FutureConcepts.Mobile.Server.WebService
{
    [ServiceBehavior(InstanceContextMode = InstanceContextMode.Single, ConcurrencyMode = ConcurrencyMode.Multiple)]
    public class TrackerV3 : ITrackerV3, IDisposable
    {
        private readonly String TAG = "TrackerV3: ";

        private DeviceContextManager _deviceContextManager;

        public TrackerV3()
        {
            _deviceContextManager = DeviceContextManager.Instance;
        }

        public void Dispose()
        {
            Debug.WriteLine(TAG + " Dispose");
        }

        public TransactionContextV3 UpdateLocation(Location location)
        {
            TransactionContextV3 result = new TransactionContextV3();
            try
            {
                MobileServerSettings settings = new MobileServerSettings();
                SqlProvider sqlProvider = new SqlProvider(settings.ICDBIsolatedConnectionString);
                using (L2S.ICDBDataContext db = sqlProvider.CreateDataContext())
                {
                    DeviceContext deviceContext = _deviceContextManager.GetDeviceContextFromWebContext();
                    Nullable<Decimal> horizontalAccuracy = null;
                    Nullable<Decimal> altitude = null;
                    Nullable<Decimal> bearing = null;
                    Nullable<Decimal> latitude = Convert.ToDecimal(location.Latitude);
                    Nullable<Decimal> longitude = Convert.ToDecimal(location.Longitude);
                    Nullable<Decimal> speed = null;
                    DateTime theTime = new DateTime(1970, 1, 1, 0, 0, 0, 0);
                    theTime.AddMilliseconds(location.Time);
                    Nullable<DateTime> time = theTime;
                    if (location.HasAccuracy)
                    {
                        horizontalAccuracy = Convert.ToDecimal(location.Accuracy);
                    }
                    if (location.HasAltitude)
                    {
                        altitude = Convert.ToDecimal(location.Altitude);
                    }
                    if (location.HasBearing)
                    {
                        bearing = Convert.ToDecimal(location.Bearing);
                    }
                    if (location.HasSpeed)
                    {
                        speed = Convert.ToDecimal(location.Speed);
                    }
                    db.UpdateDeviceLocation(deviceContext.DeviceId, location.Name, location.Description, horizontalAccuracy, null, altitude, bearing, latitude, longitude, location.Provider, speed, time);
                    DeviceLog.Log(deviceContext, 0, "TrackerV3.UpdateLocation", String.Format("Lat={0} Lon={1} BatteryLevel={2}", location.Latitude, location.Longitude, location.BatteryLevel));
                }
            }
            catch (Exception exc)
            {
                Debug.WriteLine("Exception occurred in Tracker.UpdateLocation:");
                Debug.WriteLine(exc.Message);
            }
            return result;
        }

        private void UpdateLastLocationDict(String deviceId, Location location)
        {
            try
            {
                DeviceContext deviceContext = DeviceContextManager.Instance.GetDeviceContext(deviceId);
                deviceContext.LastKnownLocation = location;
            }
            catch (Exception e)
            {
                ErrorLogger.DumpToDebug(e);
            }
        }

        public TransactionContextV3 UpdateState(TrackerState newTrackerState)
        {
            TransactionContextV3 result = new TransactionContextV3();
            try
            {
                DeviceContext deviceContext = _deviceContextManager.GetDeviceContextFromWebContext();
                deviceContext.TrackerState = newTrackerState;
                DeviceLog.Log(deviceContext, 0, "TrackerV3.UpdateState", newTrackerState.ToString());
            }
            catch (Exception e)
            {
                result.ErrorMessage = e.Message;
            }
            return result;
        }
    }
}
