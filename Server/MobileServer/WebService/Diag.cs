using System;
using System.Collections.Generic;
using System.Configuration;
using System.Diagnostics;
using System.IO;
using System.Linq;
using System.Reflection;
using System.ServiceModel;
using System.ServiceModel.Web;
using FutureConcepts.Mobile.DataContract;
using FutureConcepts.Mobile.ServiceContract;
using FutureConcepts.Tools;

namespace FutureConcepts.Mobile.Server.WebService
{
    [ServiceBehavior(InstanceContextMode = InstanceContextMode.PerSession)]
    public class Diag : IDiag, IDisposable
    {
        private readonly String TAG = "Diag: ";

        private DeviceContext _deviceContext;

        private IDiagCallback _callback;

        private static List<IDiagCallback> _callbacks = new List<IDiagCallback>();

        public IDiagCallback Callback
        {
            get
            {
                return OperationContext.Current.GetCallbackChannel<IDiagCallback>();
            }
        }

        public Diag()
        {
        }

        public void Dispose()
        {
            if (_callback != null)
            {
                _callbacks.Remove(_callback);
                _callback = null;
            }
        }

        public void Open()
        {
            _callback = OperationContext.Current.GetCallbackChannel<IDiagCallback>();
            _callbacks.Add(_callback);
        }

        public void Devices()
        {
            Callback.WriteLine("Devices:");
            DeviceContextManager dcm = DeviceContextManager.Instance;
            List<DeviceContext> devices = dcm.Devices;
            foreach (DeviceContext deviceContext in devices)
            {
                Callback.WriteLine(deviceContext.DeviceId + " " + deviceContext.PushState);
            }
        }

        public void SetDeviceContext(String deviceId)
        {
            String deviceIdLower = deviceId.ToLower();
            DeviceContextManager dcm = DeviceContextManager.Instance;
            List<DeviceContext> devices = dcm.Devices;
            foreach (DeviceContext deviceContext in devices)
            {
                String matchDeviceIdLower = deviceContext.DeviceId.ToLower();
                if (deviceIdLower == matchDeviceIdLower)
                {
                    _deviceContext = deviceContext;
                    ShowDeviceContext();
                    return;
                }
            }
            Callback.WriteLine("Device not found");
        }

        public void ShowDeviceContext()
        {
            WriteLine(String.Format("DeviceId: {0}", _deviceContext.DeviceId));
            WriteLine(String.Format("Push State: {0}", _deviceContext.PushState));
            WriteLine(String.Format("Last Push State Change Time: {0}", _deviceContext.LastPushStateChangeTime.ToString()));
            WriteLine(String.Format("Tracker State: {0}", _deviceContext.TrackerState));
            WriteLine(String.Format("Agency Name: {0}", _deviceContext.AgencyName));
            if (_deviceContext.LastKnownLocation != null)
            {
                ShowLastKnownLocation();
            }
        }

        public void ShowLastKnownLocation()
        {
            WriteLine("Last Known Location:");
            Location location = _deviceContext.LastKnownLocation;
            if (location != null)
            {
                WriteLine(String.Format("  Latitude: {0}", location.Latitude));
                WriteLine(String.Format("  Longitude: {0}", location.Longitude));
                if (location.HasAccuracy)
                {
                    WriteLine(String.Format("Accuracy: {0}", location.Accuracy));
                }
                if (location.HasSpeed)
                {
                    WriteLine(String.Format("  Speed: {0}", location.Speed));
                }
                if (location.HasAltitude)
                {
                    WriteLine(String.Format("  Altitude: {0}", location.Altitude));
                }
                if (location.HasBearing)
                {
                    WriteLine(String.Format("  Bearing: {0}", location.Bearing));
                }
                if (location.Time != 0)
                {
                    WriteLine(String.Format("  Time: {0}", location.TimeAsDateTime.ToString()));
                }
            }
            else
            {
                WriteLine("  No location");
            }
        }

        public void SendIntent(String action)
        {
            if (_deviceContext != null)
            {
                if (Queue.QueueManager.ContainsDeviceId(_deviceContext.DeviceId))
                {
                    Queue.MobileQueue queue = Queue.QueueManager.GetQueue(_deviceContext.DeviceId);
//                    Intent intent = new Intent(action);
 //                   queue.Send(intent);
                    Callback.WriteLine("intent queued to device");
                    if (_deviceContext.PushState != DeviceContext.PUSH_STATE_CONNECTED)
                    {
                        WriteLine("device is not currently connected--intent will be delivered when it connects next");
                    }
                }
                else
                {
                    WriteLine("device has not connected to push service yet");
                }
            }
            else
            {
                WriteLine("device context has not been set");
            }
        }

        public static void WriteLine(String msg)
        {
            foreach (IDiagCallback cb in _callbacks)
            {
                cb.WriteLine(msg);
            }
        }
    }
}
