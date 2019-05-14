using System;
using System.Collections.Generic;
using System.Linq;
using System.Runtime.Serialization;
using System.Text;

namespace FutureConcepts.Mobile.DataContract
{
    [DataContract]
    public class AgencyStatusCount
    {
        public AgencyStatusCount()
        {
        }

        [DataMember(EmitDefaultValue = false)]
        public String Agency { get; set; }

        [DataMember(EmitDefaultValue = false)]
        public String Status { get; set; }

        [DataMember]
        public int Count { get; set; }
    }
}
