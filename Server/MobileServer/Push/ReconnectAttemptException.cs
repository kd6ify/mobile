using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace FutureConcepts.Mobile.Server.Push
{
    public class ReconnectAttemptException : Exception
    {
        public ReconnectAttemptException(String deviceId) : base(deviceId)
        {
        }
    }
}
