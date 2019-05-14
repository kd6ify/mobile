using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Runtime.Serialization;

namespace FutureConcepts.Mobile.DataContract
{
    [DataContract]
    public class Event
    {
        public Event()
        {
        }

        [DataMember]
        public int Id { get; set; }

        [DataMember]
        public Guid IncidentId { get; set; }

        [DataMember(EmitDefaultValue = false)]
        public Nullable<DateTime> StartTime { get; set; }

        [DataMember(EmitDefaultValue = false)]
        public Nullable<DateTime> EndTime { get; set; }

        [DataMember(EmitDefaultValue = false)]
        public String Name { get; set; }

        [DataMember(EmitDefaultValue = false)]
        public String Description { get; set; }

        [DataMember(EmitDefaultValue = false)]
        public String Agency { get; set; }

        [DataMember]
        public int Status { get; set; }
    }
}
