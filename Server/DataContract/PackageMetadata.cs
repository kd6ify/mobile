using System;
using System.Collections.Generic;
using System.Linq;
using System.Runtime.Serialization;
using System.Text;

namespace FutureConcepts.Mobile.DataContract
{
    [DataContract]
    public class PackageMetadata
    {
        [DataMember]
        public int CurrentVersionCode { get; set; }

        [DataMember]
        public String PackageName { get; set; }

        [DataMember]
        public String MimeType { get; set; }

        [DataMember]
        public long Length { get; set; }

        [DataMember]
        public long Checksum { get; set; }
    }
}
