using System;
using System.Collections.Generic;
using System.Collections.Specialized;
using System.Configuration;
using System.Data;
using System.Data.SqlClient;
using System.Diagnostics;
using System.Globalization;
using System.Linq;
using System.Net.Sockets;
using System.ServiceModel;
using System.ServiceModel.Web;
using System.Text;

using FutureConcepts.Mobile.DataContract;
using FutureConcepts.Mobile.ServiceContract;
using FutureConcepts.Mobile.Server.Push;
using FutureConcepts.Settings;
using FutureConcepts.Tools;

namespace FutureConcepts.Mobile.Server.WebService
{
    [ServiceBehavior(InstanceContextMode = InstanceContextMode.Single, ConcurrencyMode = ConcurrencyMode.Multiple)]
    public class Alert : IAlert, IDisposable
    {
        private readonly String TAG = "Alert: ";

        private DeviceContextManager _deviceContextManager;
        private MobileServerSettings _mobileServerSettings;
        private Boolean _authorizationEnabled = true;

        public Alert()
        {
            _mobileServerSettings = new MobileServerSettings();
            _deviceContextManager = DeviceContextManager.Instance;
            String authorizationEnabledString = ConfigurationManager.AppSettings["AuthorizationEnabled"];
            if (authorizationEnabledString != null)
            {
                _authorizationEnabled = Boolean.Parse(authorizationEnabledString);
            }
        }

        public void Dispose()
        {
            Debug.WriteLine(TAG + " Dispose");
        }

        private void CheckAuthorization(String resource)
        {
            if (_authorizationEnabled)
            {
                WebOperationContext webOperationContext = WebOperationContext.Current;
                DeviceContext deviceContext = null;
                try
                {
                    deviceContext = DeviceContextManager.Instance.GetDeviceContextFromWebContext();
                }
                catch (Exception e)
                {
                    deviceContext = DeviceContextManager.Instance.GetDeviceContext("000000000000000");
                }
                if (deviceContext.SendAuthenticationToken)
                {
                    webOperationContext.OutgoingResponse.Headers.Add("AuthToken", deviceContext.AuthenticationContext.Token);
                    deviceContext.SendAuthenticationToken = false;
                }
            }
        }

        public AlertResults PostAlert()
        {
            AlertResults results = new AlertResults();
            TcpClient client = null;
            NetworkStream stream = null;
            try
            {
                CheckAuthorization("PostAlert");
                client = new TcpClient("10.0.201.117", 8000);
                stream = client.GetStream();
//                WebOperationContext context = WebOperationContext.Current;
//                String deviceId = context.IncomingRequest.Headers["DeviceId"];
                WriteLineToStream(stream, "beginpanic");
                WriteLineToStream(stream, "id:FCC23D010F72");
                WriteLineToStream(stream, "type:4000");
                results.ResultCode = AlertResults.ALERT_SUCCESS;
                Logit("PostAlert", "");
            }
            catch (Exception e)
            {
                results.ResultCode = AlertResults.ALERT_ERROR;
                results.Exception = ExceptionHelper.ConvertExceptionToDictionary(e);
                ErrorLogger.DumpToDebug(e);
            }
            finally
            {
                if (stream != null)
                {
                    stream.Close();
                }
                if (client != null)
                {
                    client.Close();
                }
            }
            return results;
        }

        private void WriteLineToStream(NetworkStream stream, String line)
        {
            byte[] bytes = System.Text.Encoding.ASCII.GetBytes(line + "\r\n");
            stream.Write(bytes, 0, bytes.Length);
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
    }
}
