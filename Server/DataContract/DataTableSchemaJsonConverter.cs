using System;
using System.Collections;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Diagnostics;
using System.Data;
using System.Linq;
using System.Text;
using System.Web.Script.Serialization;

namespace FutureConcepts.Mobile.DataContract
{
    public class DataTableSchemaJsonConverter : JavaScriptConverter
    {
        public override IEnumerable<Type> SupportedTypes
        {
            get
            {
                return new ReadOnlyCollection<Type>(new List<Type>(new Type[] { typeof(DataTable) }));
            }
        }

        public override IDictionary<string, object> Serialize(object obj, JavaScriptSerializer serializer)
        {
            Dictionary<String, Object> result = new Dictionary<String, Object>();
            DataTable dataTable = obj as DataTable;
            if (dataTable != null)
            {
                ArrayList columns = new ArrayList();
                foreach (DataColumn column in dataTable.Columns)
                {
                    Dictionary<String, Object> nameValue = new Dictionary<String, Object>();
                    nameValue.Add("Name", column.ColumnName);
                    nameValue.Add("Unique", Convert.ToInt32(column.Unique));
                    nameValue.Add("Type", MapDataTypeToSqlite(column.DataType));
                    columns.Add(nameValue);
                }
                result.Add("Columns", columns);
            }
            return result;
        }

        public String MapDataTypeToSqlite(System.Type type)
        {
            if (type == typeof(System.DateTime))
            {
                return "date";
            }
            else if (type == typeof(System.Int32))
            {
                return "int32";
            }
            else if (type == typeof(System.Int64))
            {
                return "int64";
            }
            else if (type == typeof(System.Byte[]))
            {
                return "blob";
            }
            else if (type == typeof(System.Double))
            {
                return "real";
            }
            else if (type == typeof(System.Decimal))
            {
                return "real";
            }
            else
            {
                return "text";
            }
        }

        public override object Deserialize(IDictionary<string, object> dictionary, Type type, JavaScriptSerializer serializer)
        {
            throw new NotImplementedException();
        }
    }
}
