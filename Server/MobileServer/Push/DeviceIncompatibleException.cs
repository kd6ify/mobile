using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace FutureConcepts.Mobile.Server.Push
{
    public class DeviceIncompatibleException : Exception
    {
        public DeviceIncompatibleException(String deviceId) : base(deviceId)
        {
        }
    }
}
