using System;
using System.Collections.Generic;
using System.Linq;
using System.Runtime.Serialization;
using System.Text;

namespace FutureConcepts.Mobile.DataContract
{
    [DataContract]
    public class ReStat
    {
        [DataMember(EmitDefaultValue = false)]
        public List<AgencyCount> TotalEquipment { get; set; }

        [DataMember(EmitDefaultValue = false)]
        public List<AgencyCount> TotalPersonnel { get; set; }

        [DataMember(EmitDefaultValue = false)]
        public List<AgencyStatusCount> EquipmentStatus { get; set; }

        [DataMember(EmitDefaultValue = false)]
        public List<AgencyStatusCount> PersonnelStatus { get; set; }

        [DataMember(EmitDefaultValue = false)]
        public List<GroupCount> Groups { get; set; }

        [DataMember(EmitDefaultValue = false)]
        public List<StatusCount> ResourceRequests { get; set; }

        [DataMember(EmitDefaultValue = false)]
        public List<NameTypeStatus> OperationalComponents { get; set; }
    }
}
