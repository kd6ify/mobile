using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Runtime.Serialization;

namespace FutureConcepts.Mobile.DataContract
{
    [DataContract]
    public class DeviceContextGroup
    {
        public DeviceContextGroup(String name)
        {
            Name = name;
            DeviceContexts = new List<DeviceContext>();
        }

        [DataMember]
        public String Name { get; set; }

        [DataMember]
        public List<DeviceContext> DeviceContexts { get; set; }
    }
}
