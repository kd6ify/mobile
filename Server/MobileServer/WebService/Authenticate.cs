using System;
using System.ServiceModel;
using System.ServiceModel.Web;

using FutureConcepts.Mobile.DataContract;
using FutureConcepts.Mobile.ServiceContract;

using FutureConcepts.Data.Access.Web.Security;

namespace FutureConcepts.Mobile.Server.WebService
{
    [ServiceBehavior(InstanceContextMode = InstanceContextMode.Single, ConcurrencyMode = ConcurrencyMode.Multiple)]
    public class Authenticate : IAuthenticate, IDisposable
    {
        private readonly String TAG = "Authenticate: ";

        private DeviceContextManager _deviceContextManager;

        public Authenticate()
        {
            _deviceContextManager = DeviceContextManager.Instance;
        }

        public void Dispose()
        {
        }

        private void Logit(String proc, String message)
        {
            DeviceContext deviceContext = _deviceContextManager.GetDeviceContextFromWebContext();
            if (deviceContext != null)
            {
                DeviceLog.Log(deviceContext, 0, proc, message);
            }
            else
            {
                SystemLog.Log(0, proc, message);
            }
        }

        public DeviceContext Login()
        {
            DeviceContext deviceContext = DeviceContextManager.Instance.GetDeviceContextFromWebContext();
            WebOperationContext.Current.OutgoingResponse.Headers.Add("AuthToken", deviceContext.AuthenticationContext.Token);
            return deviceContext;
        }

        public DeviceContext LoginWithDeviceId(String deviceId)
        {
            DeviceContext deviceContext = DeviceContextManager.Instance.GetDeviceContext(deviceId);
            WebOperationContext.Current.OutgoingResponse.Headers.Add("AuthToken", deviceContext.AuthenticationContext.Token);
            return deviceContext;
        }
    }
}
