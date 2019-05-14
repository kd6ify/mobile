using System;
using System.Collections.Generic;
using System.Linq;
using System.Runtime.Serialization;
using System.Text;

namespace FutureConcepts.Mobile.DataContract
{
    [DataContract]
    [KnownType(typeof(List<DeviceConfiguration>))]
    [KnownType(typeof(List<MediaMetadata>))]
    public class TransactionItem
    {
        [DataMember(Order = 98, EmitDefaultValue = false)]
        public String Action { get; set; }

        [DataMember(Order = 99, EmitDefaultValue = false)]
        public Object Content { get; set; }
    }
}
