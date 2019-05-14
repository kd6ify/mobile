using System;
using System.Collections.Generic;
using System.Linq;
using System.Runtime.Serialization;
using System.Text;

namespace FutureConcepts.Mobile.DataContract
{
    [DataContract]
    public class MediaMetadata
    {
        [DataMember]
        public Guid Id { get; set; }

        [DataMember]
        public String Name { get; set; }

        [DataMember]
        public String Notes { get; set; }

        [DataMember]
        public String MimeType { get; set; }

        [DataMember]
        public String Application { get; set; }

        [DataMember]
        public String ServerPath { get; set; }

        [DataMember]
        public String DevicePath { get; set; }

        [DataMember]
        public long Length { get; set; }

        [DataMember]
        public long Checksum { get; set; }

        [DataMember]
        public Nullable<DateTime> CreationDate { get; set; }

        [DataMember]
        public Nullable<DateTime> ExpirationDate { get; set; }

        [DataMember]
        public DateTime LastModifiedTime { get; set; }

        [DataMember]
        public DateTime LastUpdateTime { get; set; }
    }
}
