using System;
using System.Collections.Generic;
using System.Linq;
using System.Runtime.Serialization;
using System.Text;

namespace FutureConcepts.Mobile.DataContract
{
    [DataContract]
    public class StatusCount
    {
        public StatusCount()
        {
        }

        [DataMember(EmitDefaultValue = false)]
        public String Status { get; set; }

        [DataMember(EmitDefaultValue = false)]
        public int Count { get; set; }
    }
}
