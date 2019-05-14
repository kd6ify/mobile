using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Text;

using FutureConcepts.Mobile.DataContract;
using FutureConcepts.Mobile.Server.WebService;

namespace FutureConcepts.Mobile.Server
{
    public class DeviceLog
    {
        public static void Log(DeviceContext deviceContext, int level, String proc, String message)
        {
            String deviceId = "?";
            if (deviceContext != null)
            {
                deviceId = deviceContext.DeviceId;
            }
            String msg = String.Format("{0} {1} {2} - {3}", DateTime.Now.ToShortTimeString(), deviceId, proc, message);
            Debug.WriteLine(msg);
            Diag.WriteLine(msg);
        }
    }
}
