using System;
using System.Collections.Generic;
using System.Collections.Specialized;
using System.Configuration;
using System.Data;
using System.Data.SqlClient;
using System.Diagnostics;
using System.Globalization;
using System.Linq;
using System.ServiceModel;
using System.ServiceModel.Web;
using System.Text;

using FutureConcepts.Mobile.DataContract;
using FutureConcepts.Mobile.ServiceContract;
using FutureConcepts.Mobile.Server.Push;
using FutureConcepts.Settings;
using FutureConcepts.Tools;

namespace FutureConcepts.Mobile.Server.WebService
{
    [ServiceBehavior(InstanceContextMode = InstanceContextMode.Single, ConcurrencyMode = ConcurrencyMode.Multiple)]
    public class ICDBResource : IICDB, IDisposable
    {
        private readonly String TAG = "ICDB: ";
        private readonly String CHANGE_TYPE_INSERT = "I";
        private readonly String CHANGE_TYPE_UPDATE = "U";
        private readonly String CHANGE_TYPE_DELETE = "D";

        private DeviceContextManager _deviceContextManager;
        private MobileServerSettings _mobileServerSettings;
        private Boolean _authorizationEnabled = true;

        public ICDBResource()
        {
            _mobileServerSettings = new MobileServerSettings();
            _deviceContextManager = DeviceContextManager.Instance;
            String authorizationEnabledString = ConfigurationManager.AppSettings["AuthorizationEnabled"];
            if (authorizationEnabledString != null)
            {
                _authorizationEnabled = Boolean.Parse(authorizationEnabledString);
            }
        }

        public void Dispose()
        {
            Debug.WriteLine(TAG + " Dispose");
        }

        private void CheckAuthorization(String resource)
        {
            if (_authorizationEnabled)
            {
                WebOperationContext webOperationContext = WebOperationContext.Current;
                DeviceContext deviceContext = null;
                try
                {
                    deviceContext = DeviceContextManager.Instance.GetDeviceContextFromWebContext();
                }
                catch (Exception e)
                {
                    deviceContext = DeviceContextManager.Instance.GetDeviceContext("000000000000000");
                }
                if (deviceContext.SendAuthenticationToken)
                {
                    webOperationContext.OutgoingResponse.Headers.Add("AuthToken", deviceContext.AuthenticationContext.Token);
                    deviceContext.SendAuthenticationToken = false;
                }
            }
        }

        public SyncResults SyncInitialTableRows(String queryKey)
        {
            SyncResults results = new SyncResults();
            try
            {
                CheckAuthorization("SyncInitialTableRows");
                NameValueCollection querySection = (NameValueCollection)ConfigurationManager.GetSection(queryKey);
                using (SqlConnection connection = new SqlConnection(_mobileServerSettings.ICDBIsolatedConnectionString))
                {
                    BeginTran(connection);
                    results.TableName = querySection["TableName"];
                    results.Version = GetChangeTrackingCurrentVersion(connection);
                    String initialQuery = querySection["Initial"];
                    if (initialQuery != null)
                    {
                        using (DataTable dataTable = doSync(connection, initialQuery))
                        {
                            results.Schema = ConvertDataTableSchemaToDictionary(dataTable);
                            results.Count += results.Schema.Count();
                            results.Inserts = ConvertDataTableToDictionary(dataTable);
                            results.Count += results.Inserts.Count();
                        }
                    }
                    CommitTran(connection);
                }
            }
            catch (Exception e)
            {
                results.ErrorCode = SyncResults.ERROR_EXCEPTION;
                results.Exception = ExceptionHelper.ConvertExceptionToDictionary(e);
                ErrorLogger.DumpToDebug(e);
            }
            return results;
        }

        public SyncResults SyncInitialTableRowsWithParam(String queryKey, String param)
        {
            SyncResults results = new SyncResults();
            try
            {
                CheckAuthorization("SyncInitialTableRowsWithParam");
                NameValueCollection querySection = (NameValueCollection)ConfigurationManager.GetSection(queryKey);
                if (querySection != null)
                {
                    using (SqlConnection connection = new SqlConnection(_mobileServerSettings.ICDBIsolatedConnectionString))
                    {
                        BeginTran(connection);
                        results.TableName = querySection["TableName"];
                        results.Version = GetChangeTrackingCurrentVersion(connection);
                        String initialQuery = querySection["Initial"];
                        if (initialQuery != null)
                        {
                            using (DataTable dataTable = doSync(connection, initialQuery, param))
                            {
                                results.Schema = ConvertDataTableSchemaToDictionary(dataTable);
                                results.Count += results.Schema.Count();
                                results.Inserts = ConvertDataTableToDictionary(dataTable);
                                results.Count += results.Inserts.Count();
                            }
                        }
                        CommitTran(connection);
                    }
                }
            }
            catch (Exception e)
            {
                results.ErrorCode = SyncResults.ERROR_EXCEPTION;
                results.Exception = ExceptionHelper.ConvertExceptionToDictionary(e);
                ErrorLogger.DumpToDebug(e);
            }
            return results;
        }

        public SyncResults SyncTableRows(String queryKey, String lastSynchVersion)
        {
            SyncResults results = new SyncResults();
            try
            {
                CheckAuthorization("SyncTableRows");
                NameValueCollection querySection = (NameValueCollection)ConfigurationManager.GetSection(queryKey);
                using (SqlConnection connection = new SqlConnection(_mobileServerSettings.ICDBIsolatedConnectionString))
                {
                    results.TableName = querySection["TableName"];
                    long lastSyncVersionLong = long.Parse(lastSynchVersion);
                    if (lastSyncVersionLong < GetChangeTrackingMinValidVersion(connection, results.TableName))
                    {
                        throw new ResyncRequiredException(String.Format("server://ICDB/{0}/Initial", queryKey));
                    }
                    BeginTran(connection);
                    results.Version = GetChangeTrackingCurrentVersion(connection);
                    String insertsAndUpdatesQuery = querySection["InsertsAndUpdates"];
                    if (insertsAndUpdatesQuery != null)
                    {
                        using (DataTable insertsDataTable = doSync(connection, insertsAndUpdatesQuery, lastSyncVersionLong, CHANGE_TYPE_INSERT))
                        {
                            results.Inserts = ConvertDataTableToDictionary(insertsDataTable);
                            results.Count += results.Inserts.Count();
                        }
                        using (DataTable updatesDataTable = doSync(connection, insertsAndUpdatesQuery, lastSyncVersionLong, CHANGE_TYPE_UPDATE))
                        {
                            results.Updates = ConvertDataTableToDictionary(updatesDataTable);
                            results.Count += results.Updates.Count();
                        }
                    }
                    String deletesQuery = querySection["Deletes"];
                    if (deletesQuery != null)
                    {
                        using (DataTable deletesDataTable = doSync(connection, deletesQuery, lastSyncVersionLong, CHANGE_TYPE_DELETE))
                        {
                            results.Deletes = ConvertDataTableToDictionary(deletesDataTable);
                            results.Count += results.Deletes.Count();
                        }
                    }
                    CommitTran(connection);
                }
            }
            catch (ResyncRequiredException rse)
            {
                results.ErrorCode = SyncResults.ERROR_RESYNC_REQUIRED;
                results.ResyncUrl = rse.ResyncUrl;
            }
            catch (Exception e)
            {
                results.ErrorCode = SyncResults.ERROR_EXCEPTION;
                results.Exception = ExceptionHelper.ConvertExceptionToDictionary(e);
                ErrorLogger.DumpToDebug(e);
            }
            return results;
        }

        public SyncResults SyncTableRowsWithParam(String queryKey, String lastSynchVersion, String param)
        {
            SyncResults results = new SyncResults();
            try
            {
                CheckAuthorization("SyncTableRowsWithParam");
                NameValueCollection querySection = (NameValueCollection)ConfigurationManager.GetSection(queryKey);
                using (SqlConnection connection = new SqlConnection(_mobileServerSettings.ICDBIsolatedConnectionString))
                {
                    results.TableName = querySection["TableName"];
                    long lastSyncVersionLong = long.Parse(lastSynchVersion);
                    if (lastSyncVersionLong < GetChangeTrackingMinValidVersion(connection, results.TableName))
                    {
                        throw new ResyncRequiredException(String.Format("server://ICDB/{0}/{1}/Initial", queryKey, param));
                    }
                    BeginTran(connection);
                    results.Version = GetChangeTrackingCurrentVersion(connection);
                    String insertsAndUpdatesQuery = querySection["InsertsAndUpdates"];
                    if (insertsAndUpdatesQuery != null)
                    {
                        using (DataTable insertsDataTable = doSync(connection, insertsAndUpdatesQuery, lastSyncVersionLong, param, CHANGE_TYPE_INSERT))
                        {
                            results.Inserts = ConvertDataTableToDictionary(insertsDataTable);
                            results.Count += results.Inserts.Count();
                        }
                        using (DataTable updatesDataTable = doSync(connection, insertsAndUpdatesQuery, lastSyncVersionLong, param, CHANGE_TYPE_UPDATE))
                        {
                            results.Updates = ConvertDataTableToDictionary(updatesDataTable);
                            results.Count += results.Updates.Count();
                        }
                    }
                    String deletesQuery = querySection["Deletes"];
                    if (deletesQuery != null)
                    {
                        using (DataTable deletesDataTable = doSync(connection, deletesQuery, lastSyncVersionLong, param, CHANGE_TYPE_DELETE))
                        {
                            results.Deletes = ConvertDataTableToDictionary(deletesDataTable);
                            results.Count += results.Deletes.Count();
                        }
                    }
                    CommitTran(connection);
                }
            }
            catch (ResyncRequiredException rse)
            {
                results.ErrorCode = SyncResults.ERROR_RESYNC_REQUIRED;
                results.ResyncUrl = rse.ResyncUrl;
            }
            catch (Exception e)
            {
                results.ErrorCode = SyncResults.ERROR_EXCEPTION;
                results.Exception = ExceptionHelper.ConvertExceptionToDictionary(e);
                ErrorLogger.DumpToDebug(e);
            }
            return results;
        }

        private DataTable doSync(SqlConnection connection, String query)
        {
            DataTable result = new DataTable();
            using (SqlCommand command = new SqlCommand(query, connection))
            {
                using (SqlDataAdapter adapter = new SqlDataAdapter(command))
                {
                    adapter.Fill(result);
                }
            }
            return result;
        }

        private DataTable doSync(SqlConnection connection, String query, String param)
        {
            DataTable result = new DataTable();
            using (SqlCommand command = new SqlCommand(String.Format(query, param), connection))
            {
                using (SqlDataAdapter adapter = new SqlDataAdapter(command))
                {
                    adapter.Fill(result);
                }
            }
            return result;
        }

        private DataTable doSync(SqlConnection connection, String query, long lastSyncVersion, String changeType)
        {
            DataTable result = new DataTable();
            using (SqlCommand command = new SqlCommand(String.Format(query, lastSyncVersion, changeType), connection))
            {
                using (SqlDataAdapter adapter = new SqlDataAdapter(command))
                {
                    adapter.Fill(result);
                }
            }
            return result;
        }

        private DataTable doSync(SqlConnection connection, String query, long lastSyncVersion, String param, String changeType)
        {
            DataTable result = new DataTable();
            using (SqlCommand command = new SqlCommand(String.Format(query, lastSyncVersion, param, changeType), connection))
            {
                using (SqlDataAdapter adapter = new SqlDataAdapter(command))
                {
                    adapter.Fill(result);
                }
            }
            return result;
        }

        public TransactionContextV3 InsertTableRow(String tableName, Dictionary<String, Object> row)
        {
            Debug.WriteLine(String.Format("InsertTableRow {0}", tableName));
            TransactionContextV3 result = new TransactionContextV3();
            try
            {
                CheckAuthorization("InsertTableRow");
                using (SqlConnection connection = new SqlConnection(_mobileServerSettings.ICDBIsolatedConnectionString))
                {
                    using (SqlCommand command = new SqlCommand(String.Format("select * FROM {0} where 1=2", tableName), connection))
                    {
                        connection.Open();
                        using (SqlDataAdapter adapter = new SqlDataAdapter(command))
                        {
                            using (DataTable dataTable = new DataTable())
                            {
                                adapter.Fill(dataTable);
                                DataRow dataRow = dataTable.NewRow();
                                UpdateDataRowFromDictionary(dataRow, dataTable.Columns, row);
                                dataTable.Rows.Add(dataRow);
                                using (SqlCommandBuilder commandBuilder = new SqlCommandBuilder(adapter))
                                {
                                    adapter.InsertCommand = commandBuilder.GetInsertCommand();
                                    adapter.Update(dataTable);
                                    dataTable.AcceptChanges();
                                }
                            }
                        }
                    }
                }
            }
            catch (Exception e)
            {
                result.ErrorMessage = e.Message;
                ErrorLogger.DumpToDebug(e);
            }
            return result;
        }

        public TransactionContextV3 UpdateTableRow(String table, String id, Dictionary<String, Object> row)
        {
            Debug.WriteLine(String.Format("UpdateTableRow {0} {1}", table, id));
            TransactionContextV3 result = new TransactionContextV3();
            try
            {
                CheckAuthorization("UpdateTableRow");
                using (SqlConnection connection = new SqlConnection(_mobileServerSettings.ICDBIsolatedConnectionString))
                {
                    using (SqlCommand command = new SqlCommand(String.Format("select * FROM {0} WHERE ID=\'{1}\'", table, id), connection))
                    {
                        connection.Open();
                        using (SqlDataAdapter adapter = new SqlDataAdapter(command))
                        {
                            using (DataTable dataTable = new DataTable())
                            {
                                adapter.Fill(dataTable);
                                if (dataTable.Rows.Count == 1)
                                {
                                    UpdateDataRowFromDictionary(dataTable.Rows[0], dataTable.Columns, row);
                                    using (SqlCommandBuilder commandBuilder = new SqlCommandBuilder(adapter))
                                    {
                                        adapter.UpdateCommand = commandBuilder.GetUpdateCommand();
                                        adapter.Update(dataTable);
                                        dataTable.AcceptChanges();
                                    }
                                }
                            }
                        }
                    }
                }
            }
            catch (Exception e)
            {
                result.ErrorMessage = e.Message;
                ErrorLogger.DumpToDebug(e);
            }
            return result;
        }

        public TransactionContextV3 DeleteTableRow(String tableName, String id)
        {
            Debug.WriteLine(String.Format("DeleteTableRow {0} {1}", tableName, id));
            TransactionContextV3 result = new TransactionContextV3();
            try
            {
                CheckAuthorization("DeleteTableRow");
                using (SqlConnection connection = new SqlConnection(_mobileServerSettings.ICDBIsolatedConnectionString))
                {
                    using (SqlCommand sqlCommand = new SqlCommand(String.Format(@"DELETE FROM [{0}] WHERE ([ID]='{1}')", tableName, id), connection))
                    {
                        connection.Open();
                        int deleteCommandResult = sqlCommand.ExecuteNonQuery();
                        Debug.WriteLine("After ExecuteNonQuery");
                    }
                }
            }
            catch (Exception e)
            {
                result.ErrorMessage = e.Message;
                ErrorLogger.DumpToDebug(e);
            }
            return result;
        }

        private static List<Dictionary<String, Object>> ConvertDataTableToDictionary(DataTable table)
        {
            List<Dictionary<String, Object>> result = new List<Dictionary<String, Object>>();
            DataColumnCollection columns = table.Columns;
            Dictionary<Guid, DataRow> rowMap = new Dictionary<Guid, DataRow>();
            foreach (DataRow row in table.Rows)
            {
                Nullable<Guid> id = row["ID"] as Nullable<Guid>;
                if (rowMap.ContainsKey(id.Value) == false) // skip duplicate rows
                {
                    Dictionary<String, Object> nameValue = new Dictionary<String, Object>();
                    foreach (DataColumn column in columns)
                    {
                        Object val = row[column];
                        if (val == null || val == DBNull.Value)
                        {
                            nameValue.Add(column.ColumnName, null);
                        }
                        else if (column.DataType == typeof(System.DateTime))
                        {
                            Nullable<DateTime> dateTime = row[column] as Nullable<DateTime>;
                            TimeSpan delta = dateTime.Value - new DateTime(1970, 1, 1);
                            nameValue.Add(column.ColumnName, Convert.ToInt64(delta.TotalMilliseconds));
                        }
                        else if (column.DataType == typeof(System.DateTimeOffset))
                        {
                            Nullable<DateTimeOffset> dateTimeOffset = row[column] as Nullable<DateTimeOffset>;
                            nameValue.Add(column.ColumnName, dateTimeOffset.Value.ToString("yyyy'-'MM'-'dd'T'HH':'mm':'ss.FFFFFFFK", CultureInfo.CurrentCulture));
                        }
                        else if (column.DataType == typeof(System.Byte[]))
                        {
                            byte[] bytes = row[column] as byte[];
                            String b64bytes = Convert.ToBase64String(bytes, Base64FormattingOptions.InsertLineBreaks);
                            //                        String b64bytes = Convert.ToBase64String(bytes);
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
                    result.Add(nameValue);
                    rowMap.Add(id.Value, row);
                }
            }
            return result;
        }

        private static void UpdateDataRowFromDictionary(DataRow dataRow, DataColumnCollection columns, Dictionary<String, Object> row)
        {
            for (int i = 0; i < row.Count; i++)
            {
                List<String> columnNames = row.Keys.ToList();
                foreach (String columnName in columnNames)
                {
                    DataColumn dataColumn = columns[columnName];
                    Object value = row[columnName];
                    if (value != null)
                    {
                        if (dataColumn.DataType == typeof(System.DateTime))
                        {
                            dataRow[dataColumn] = new DateTime(1970, 1, 1) + TimeSpan.FromMilliseconds(Convert.ToInt64(value));
                        }
                        else if (dataColumn.DataType == typeof(System.DateTimeOffset))
                        {
                            dataRow[dataColumn] = DateTimeOffset.Parse((String)value);
                            Debug.WriteLine("DateTimeOffset=" + dataRow[dataColumn]);
                        }
                        else if (dataColumn.DataType == typeof(System.Byte[]))
                        {
                            dataRow[dataColumn] = Convert.FromBase64String((String)value);
                        }
                        else if (dataColumn.DataType == typeof(System.Boolean))
                        {
                            dataRow[dataColumn] = Convert.ToInt32(value) == 1;
                        }
                        else
                        {
                            dataRow[dataColumn] = value;
                        }
                    }
                    else
                    {
                        dataRow[dataColumn] = DBNull.Value;
                    }
                }
            }
        }

        private static List<Dictionary<String, Object>> ConvertDataTableSchemaToDictionary(DataTable dataTable)
        {
            List<Dictionary<String, Object>> result = new List<Dictionary<String, Object>>();
            foreach (DataColumn column in dataTable.Columns)
            {
                Dictionary<String, Object> nameValue = new Dictionary<String, Object>();
                nameValue.Add("Name", column.ColumnName);
                nameValue.Add("Type", MapColumnToSqliteType(column));
                result.Add(nameValue);
            }
            return result;
        }

        private void Logit(String proc, String message)
        {
            try
            {
                DeviceContext deviceContext = _deviceContextManager.GetDeviceContextFromWebContext();
                if (deviceContext != null)
                {
                    DeviceLog.Log(deviceContext, 0, proc, message);
                }
            }
            catch (Exception e)
            {
                SystemLog.Log(0, proc, message);
            }
        }

        private int BeginTran(SqlConnection connection)
        {
            int result = 0;
            connection.Open();
            using (SqlCommand command = new SqlCommand(String.Format("SET TRANSACTION ISOLATION LEVEL SNAPSHOT; BEGIN TRAN"), connection))
            {
                result = command.ExecuteNonQuery();
            }
            return result;
        }

        private int CommitTran(SqlConnection connection)
        {
            int result = 0;
            using (SqlCommand command = new SqlCommand(String.Format("COMMIT TRAN"), connection))
            {
                result = command.ExecuteNonQuery();
            }
            return result;
        }

        private long GetChangeTrackingCurrentVersion(SqlConnection connection)
        {
            long result = 0;
            using (SqlCommand command = new SqlCommand(String.Format("SELECT CHANGE_TRACKING_CURRENT_VERSION()"), connection))
            {
                using (SqlDataAdapter adapter = new SqlDataAdapter(command))
                {
                    using (DataTable dataTable = new DataTable())
                    {
                        adapter.Fill(dataTable);
                        result = (long)dataTable.Rows[0]["Column1"];
                    }
                }
            }
            return result;
        }

        private long GetChangeTrackingMinValidVersion(SqlConnection connection, String tableName)
        {
            long result = 0;
            try
            {
                using (SqlCommand command = new SqlCommand(String.Format("SELECT CHANGE_TRACKING_MIN_VALID_VERSION(OBJECT_ID('{0}'))", tableName), connection))
                {
                    using (SqlDataAdapter adapter = new SqlDataAdapter(command))
                    {
                        DataTable dataTable = new DataTable();
                        adapter.Fill(dataTable);
                        result = (long)dataTable.Rows[0]["Column1"];
                    }
                }
            }
            catch (InvalidCastException e)
            {
                Debug.WriteLine(String.Format("Table {0} does not have change tracking enabled"), tableName);
                throw e;
            }
            return result;
        }

        private static String MapColumnToSqliteType(DataColumn dataColumn)
        {
            StringBuilder result = new StringBuilder();
            if (dataColumn.DataType == typeof(System.Guid))
            {
                result.Append("guid");
            }
            else if (dataColumn.DataType == typeof(System.DateTime))
            {
                result.Append("date");
            }
            else if (dataColumn.DataType == typeof(System.DateTimeOffset))
            {
                result.Append("datetimeoffset");
            }
            else if (dataColumn.DataType == typeof(System.Int32))
            {
                result.Append("int32");
            }
            else if (dataColumn.DataType == typeof(System.Int64))
            {
                result.Append("int64");
            }
            else if (dataColumn.DataType == typeof(System.Byte[]))
            {
                result.Append("blob");
            }
            else if (dataColumn.DataType == typeof(System.Double) || dataColumn.DataType == typeof(System.Decimal))
            {
                result.Append("real");
            }
            else
            {
                result.Append("text");
            }
            if (dataColumn.ColumnName == "ID" || dataColumn.Unique)
            {
                result.Append(" unique");
            }
            return result.ToString();
        }
    }
}
