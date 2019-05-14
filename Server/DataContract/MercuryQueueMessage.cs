using System;
using System.Data;
using System.Diagnostics;
using System.IO;
using System.Runtime.Serialization.Json;
using System.Web.Script.Serialization;
using System.Xml.Serialization;
using System.Xml;
using System.Xml.Schema;

namespace FutureConcepts.Mobile.DataContract
{
    public class MercuryQueueMessage
    {
        public String Action { get; set; }

        public String ClientUrl { get; set; }

        public String Content { get; set; }
        
        public long ExpirationTime { get; set; }
        
        public String ContentType { get; set; }
        
        public String ServerUrl { get; set; }
        
        public int Priority { get; set; }
        
        public String MessageId { get; set; }
        
        public String NotificationMessage { get; set; }
        
        public String Param1 { get; set; }

        public String GetJson()
        {
            String result = null;
            JavaScriptSerializer serializer = new JavaScriptSerializer();
            result = serializer.Serialize(this);
            Debug.WriteLine(result);
            return result;
        }
    }
}
