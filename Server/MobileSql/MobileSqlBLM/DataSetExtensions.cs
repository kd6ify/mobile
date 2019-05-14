using System;
using System.Collections;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Diagnostics;
using System.Reflection;
using System.Text;

using FutureConcepts.Data.Access.Model.Tracking;

namespace MobileSqlBLM
{
    /// <summary>
    /// This class stores Linq extension methods
    /// </summary>
    public static class DataSetExtensions
    {
        /// <summary>
        /// Allows you to convert a ADO.NET DataSet into a list of Model items loading only TopLevel.
        /// </summary>
        /// <typeparam name="DataSet">the DataSet to use</typeparam>
        /// <typeparam name="T">The type of object being returned</typeparam>
        public static IList<T> ToModelList<T>(this DataSet dataset)
            where T : TrackableObject, new()
        {
            List<T> result = new List<T>();
            TypeConverter typeConverter = new TypeConverter();
            DataColumnCollection columns = dataset.Tables[0].Columns;
            foreach (DataRow row in dataset.Tables[0].Rows)
            {
                T newT = new T();
                foreach (DataColumn dataColumn in columns)
                {
                    String columnName = dataColumn.ColumnName;
                    PropertyInfo propertyInfo = typeof(T).GetProperty(columnName);
                    Debug.WriteLine(String.Format("DataSetExtensions.ToModelList() {0} {1} {2} {3}", columnName, propertyInfo.PropertyType.ToString(), row[columnName].GetType().ToString(), row[columnName]));
                    if (propertyInfo != null)
                    {
                        MethodInfo methodInfo = propertyInfo.GetSetMethod();
                        if (row.IsNull(columnName))
                        {
                            methodInfo.Invoke(newT, new Object[] { null });
                        }
                        else if (typeof(TrackableObject).IsAssignableFrom(propertyInfo.PropertyType))
                        {
                            TrackableObject nested = Activator.CreateInstance(propertyInfo.PropertyType) as TrackableObject;
                            nested.ID = new Guid(row[columnName] as String);
                            methodInfo.Invoke(newT, new Object[] { nested });
                        }
                        else
                        {
                            Object theValue = row[columnName];
                            if (theValue.GetType() == propertyInfo.PropertyType)
                            {
                                propertyInfo.SetValue(newT, theValue, null);
                            }
                            else
                            {
                                propertyInfo.SetValue(newT, TypeDescriptor.GetConverter(propertyInfo.PropertyType).ConvertFrom(theValue), null);
                            }
                        }
                    }
                }
                result.Add(newT);
            }
            return result;
        }

        public static Object ToModelListUsingArticle(this DataSet dataset, String article)
        {
            String typeName = typeof(FutureConcepts.Data.Access.Model.Incident).AssemblyQualifiedName.Replace("Incident", article);
            Type theType = Type.GetType(typeName);
            return typeof(DataSetExtensions).GetMethod("ToModelList").MakeGenericMethod(new Type[] { theType }).Invoke(null, new Object[] { dataset });
        }
    }
}
