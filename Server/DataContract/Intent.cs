using System;
using System.Collections.Generic;
using System.Collections.Specialized;
using System.Diagnostics;
using System.IO;
using System.Linq;
using System.Text;
using System.Runtime.Serialization;
using System.Runtime.Serialization.Json;

namespace FutureConcepts.Mobile.DataContract
{
    [Serializable]
    [DataContract]
    [KnownType(typeof(ListDictionary))]
    public class Intent
    {
        public static readonly String ACTION_VIEW = "android.intent.action.VIEW";

        public static readonly String ACTION_SYNC_COMPLETE = "com.futureconcepts.action.sync.COMPLETE";
        public static readonly String ACTION_SYNC_SYNCLIST = "com.futureconcepts.action.sync.SYNCLIST";
        public static readonly String ACTION_SYNC_RESYNC = "com.futureconcepts.action.sync.RESYNC";
        public static readonly String ACTION_TEXT_MESSAGE = "com.futureconcepts.action.message.TEXT";
        public static readonly String ACTION_NEW_VERSION = "com.futureconcepts.action.NEW_VERSION";
        public static readonly String ACTION_START_TRACKER = "com.futureconcepts.action.tracker.START";
        public static readonly String ACTION_STOP_TRACKER = "com.futureconcepts.action.tracker.STOP";
        public static readonly String ACTION_SQL_REPL_SYNC = "com.futureconcepts.action.sql_repl_sync";

        public static readonly String PATH_INCIDENT = "Incident";
        public static readonly String PATH_LOG = "Log";
        public static readonly String PATH_LOG_ENTRY = "LogEntry";
        public static readonly String PATH_INTEL = "Intel";
        public static readonly String PATH_EQUIPMENT = "Equipment";
        public static readonly String PATH_PERSONNEL = "Personnel";
        public static readonly String PATH_RESOURCE_TYPE = "ResourceType";

        public static readonly String PRIMARY_KEY_VALUE = "PRIMARY_KEY_VALUE";
        public static readonly String INCIDENT_ID = "INCIDENT_ID";
        public static readonly String PATH = "PATH";

        public Intent()
        {
        }

        public Intent(String action)
        {
            Action = action;
        }

        [DataMember(EmitDefaultValue=false)]
        public String Action { get; set; }

        [DataMember(EmitDefaultValue=false)]
        public String Data { get; set; }

        [DataMember(EmitDefaultValue=false)]
        public List<String> Category { get; set; }

        [DataMember(EmitDefaultValue=false)]
        public String MimeType { get; set; }

        [DataMember(EmitDefaultValue=false)]
        public String Component { get; set; }

        [DataMember(EmitDefaultValue=false)]
        public ListDictionary Extras { get; set; }

        public void PutExtra(String key, Object value)
        {
            if (Extras == null)
            {
                Extras = new ListDictionary();
            }
            Extras.Add(key, value);
        }

        public void PutExtra(Type type, String name, Object value)
        {
            DataContractJsonSerializer serializer = new DataContractJsonSerializer(type);
            MemoryStream stream = new MemoryStream();
            serializer.WriteObject(stream, value);
            stream.Position = 0;
            StreamReader reader = new StreamReader(stream);
            PutExtra(name, reader.ReadToEnd());
            reader.Close();
            stream.Close();
        }

        public String IncidentId
        {
            get
            {
                return (String)Extras[Intent.INCIDENT_ID];
            }
            set
            {
                PutExtra(Intent.INCIDENT_ID, value);
            }
        }

        public String PrimaryKeyValue
        {
            get
            {
                return (String)Extras[Intent.PRIMARY_KEY_VALUE];
            }
            set
            {
                PutExtra(Intent.PRIMARY_KEY_VALUE, value);
            }
        }

        public String Path
        {
            get
            {
                return (String)Extras[Intent.PATH];
            }
            set
            {
                PutExtra(Intent.PATH, value);
            }
        }


        public String ToJSONString()
        {
            MemoryStream stream = null;
            StreamReader reader = null;
            try
            {
                DataContractJsonSerializer serializer = new DataContractJsonSerializer(typeof(Intent));
                stream = new MemoryStream();
                serializer.WriteObject(stream, this);
                stream.Position = 0;
                reader = new StreamReader(stream);
                String value = reader.ReadToEnd();
                return value;
            }
            catch (Exception e)
            {
                Debug.WriteLine(e.Message);
                return null;
            }
            finally
            {
                if (reader != null)
                {
                    reader.Close();
                }
                if (stream != null)
                {
                    stream.Close();
                }
            }
        }

        public static Intent GetFromJSONString(String jsonString)
        {
            Intent value = null;
            MemoryStream stream = null;
            try
            {
                DataContractJsonSerializer serializer = new DataContractJsonSerializer(typeof(Intent));
                stream = new MemoryStream(Encoding.UTF8.GetBytes(jsonString));
                stream.Position = 0;
                value = (Intent)serializer.ReadObject(stream);
                return value;
            }
            catch (Exception e)
            {
                Debug.WriteLine(e.Message);
                return null;
            }
            finally
            {
                if (stream != null)
                {
                    stream.Close();
                }
            }
        }
    }
}
