using System;
using System.Collections.Generic;
using System.Data.Linq;
using System.Linq;
using System.Runtime.Serialization;
using System.Text;

namespace FutureConcepts.Mobile.DataContract
{
    [DataContract]
    public class EntityType
    {
        public EntityType()
        {
        }

        [DataMember]
        public String Name { get; set; }

        [DataMember]
        public Binary Icon { get; set; }
    }
}
