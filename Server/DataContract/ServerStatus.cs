using System;
using System.Collections.Generic;
using System.Collections.Specialized;
using System.Linq;
using System.Reflection;
using System.Runtime.Serialization;
using System.Text;

using FutureConcepts.Settings;
using FutureConcepts.Mobile.DataContract;

namespace FutureConcepts.Mobile.DataContract
{
    [DataContract]
    public class ServerStatus
    {
        public ServerStatus()
        {
        }

        [DataMember]
        public String Version { get; set; }

        [DataMember]
        public MobileServerSettings MobileServerSettings { get; set; }

        [DataMember]
        public WSUSSettings WSUSSettings { get; set; }

        [DataMember]
        public List<DeviceContextGroup> AgencyDeviceContexts { get; set; }
    }
}
