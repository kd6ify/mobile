using System;
using System.Collections.Generic;
using System.Linq;
using System.Runtime.Serialization;
using System.Text;

namespace FutureConcepts.Mobile.DataContract
{
    [DataContract]
    public class GroupCount
    {
        public GroupCount()
        {
        }

        [DataMember(EmitDefaultValue = false)]
        public String Group { get; set; }

        [DataMember]
        public int Count { get; set; }
    }
}
