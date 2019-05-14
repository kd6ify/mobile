using System;
using System.Collections.Generic;
using System.Linq;
using System.Runtime.Serialization;
using System.Text;

namespace FutureConcepts.Mobile.DataContract
{
    [DataContract]
    public class NameTypeStatus
    {
        public NameTypeStatus()
        {
        }

        [DataMember(EmitDefaultValue = false)]
        public String Name { get; set; }

        [DataMember(EmitDefaultValue = false)]
        public String Type { get; set; }

        [DataMember(EmitDefaultValue = false)]
        public String Status { get; set; }
    }
}
