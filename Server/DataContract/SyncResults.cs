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
	public partial class SyncResults
	{
        public static readonly int NO_ERROR = 0;
        public static readonly int ERROR_RESYNC_REQUIRED = 1;
        public static readonly int ERROR_EXCEPTION = 2;

        [DataMember(Order=1)]
        public String TableName { get; set; }

        [DataMember(Order = 2)]
        public long Version { get; set; }

        [DataMember(Order = 3)]
        public int ErrorCode { get; set; }

        [DataMember(Order = 4, EmitDefaultValue=false)]
        public String ResyncUrl { get; set; }

        [DataMember(Order = 5)]
        public int Count { get; set; }

        [DataMember(Order = 6, EmitDefaultValue=false)]
        public List<Dictionary<String, Object>> Exception { get; set; }

        [DataMember(Order = 7)]
        public List<Dictionary<String, Object>> Schema { get; set; }

        [DataMember(Order = 8)]
        public List<Dictionary<String, Object>> Inserts { get; set; }

        [DataMember(Order=9)]
        public List<Dictionary<String, Object>> Updates { get; set; }

        [DataMember(Order=10)]
        public List<Dictionary<String, Object>> Deletes { get; set; }
	}
}
