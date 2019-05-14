using System;
using System.Collections.Generic;
using System.Collections.Specialized;
using System.ComponentModel;
using System.Data.Linq.Mapping;
using System.Linq;
using System.Runtime.Serialization;
using System.Text;

namespace FutureConcepts.Mobile.DataContract
{
    [DataContract]
	public partial class AlertResults
	{
        public static readonly int ALERT_SUCCESS = 0;
        public static readonly int ALERT_ERROR = 1;

        [DataMember(Order = 1)]
        public long Version { get; set; }

        [DataMember(Order = 2)]
        public int ResultCode { get; set; }

        [DataMember(Order = 3, EmitDefaultValue=false)]
        public List<Dictionary<String, Object>> Exception { get; set; }
	}
}
