using System;
using System.Collections;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Diagnostics;
using System.Data;
using System.Linq;
using System.Text;
using System.Text.RegularExpressions;
using System.Web.Script.Serialization;

namespace FutureConcepts.Mobile.DataContract
{
    public class DataTableJsonConverter : JavaScriptConverter
    {
        public DataTableJsonConverter()
        {
        }

        public DataTableJsonConverter(DataColumnCollection columns)
        {
            Columns = columns;
        }

        public override IEnumerable<Type> SupportedTypes
        {
            get
            {
                return new ReadOnlyCollection<Type>(new List<Type>(new Type[] { typeof(DataTable) }));
            }
        }

        private DataColumnCollection Columns { get; set; }

        public override IDictionary<string, object> Serialize(object obj, JavaScriptSerializer serializer)
        {
            // convert a DataTable to a IDictionary<string, object>
            Dictionary<String, Object> result = new Dictionary<String, Object>();
            DataTable dataTable = obj as DataTable;
            try
            {
                if (dataTable != null)
                {
                    ArrayList rows = new ArrayList();
                    foreach (DataRow row in dataTable.Rows)
                    {
                        Dictionary<String, Object> nameValue = new Dictionary<String, Object>();
                        foreach (DataColumn column in dataTable.Columns)
                        {
                            Object val = row[column];
                            if (val == null || val == DBNull.Value)
                            {
                                nameValue.Add(column.ColumnName, DBNull.Value);
                            }
                            else
                            {
                                if (column.DataType == typeof(System.DateTime))
                                {
                                    Nullable<DateTime> dateTime = row[column] as Nullable<DateTime>;
                                    TimeSpan delta = dateTime.Value - new DateTime(1970, 1, 1);
                                    nameValue.Add(column.ColumnName, Convert.ToInt64(delta.TotalMilliseconds));
                                }
                                else if (column.DataType == typeof(System.Byte[]))
                                {
                                    byte[] bytes = row[column] as byte[];
                                    String b64bytes = Convert.ToBase64String(bytes);
                                    nameValue.Add(column.ColumnName, b64bytes);
                                }
                                else if (column.DataType == typeof(System.Boolean))
                                {
                                    nameValue.Add(column.ColumnName, Convert.ToInt32(row[column]));
                                }
                                else
                                {
                                    nameValue.Add(column.ColumnName, val);
                                }
                            }
                        }
                        rows.Add(nameValue);
                    }
                    result.Add("rows", rows);
                }
            }
            catch (Exception e)
            {
                Debug.WriteLine(e.Message);
                Debug.WriteLine(e.StackTrace);
            }
            return result;
        }

        public override object Deserialize(IDictionary<string, object> dictionary, Type type, JavaScriptSerializer serializer)
        {
            // convert a IDictionary<string, object> to a DataTable
            ArrayList rows = dictionary["rows"] as ArrayList;
            DataTable result = new DataTable();
            foreach (DataColumn column in Columns)
            {
                DataColumn newColumn = new DataColumn(column.ColumnName, column.DataType);
                result.Columns.Add(newColumn);
            }
            foreach (Object row in rows)
            {
                DataRow dataRow = result.NewRow();
                foreach (DataColumn column in Columns)
                {
                    IDictionary<String, Object> nameValue = row as IDictionary<String, Object>;
                    if (column.DataType == typeof(System.DateTime))
                    {
                        DateTime dateTime = new DateTime(1970, 1, 1) + TimeSpan.FromMilliseconds(Convert.ToDouble(nameValue[column.ColumnName]));
                        dataRow[column] = dateTime;
                    }
                    else if (column.DataType == typeof(System.Byte[]))
                    {
                        dataRow[column] = Convert.FromBase64String(nameValue[column.ColumnName] as String);
                    }
                    else if (column.DataType == typeof(System.Boolean))
                    {
                        int val = Convert.ToInt32(nameValue[column.ColumnName]);
                        dataRow[column] = (val == 1);
                    }
                    else
                    {
                        dataRow[column] = nameValue[column.ColumnName];
                    }
                    result.Rows.Add(dataRow);
                }
            }
            return result;
        }
    }
}
