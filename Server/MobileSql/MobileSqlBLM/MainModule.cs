// Documentation for how to implement a BL Handler for a Merge Article is described here:
// http://msdn.microsoft.com/en-us/library/ms147911%28v=SQL.100%29.aspx

using System;
using System.Collections.Generic;
using System.Configuration;
using System.Data;
using System.Data.SqlClient;
using System.Diagnostics;
using System.IO;
using System.Xml.Serialization;
using System.Text;

using Microsoft.SqlServer.Replication.BusinessLogicSupport;

using FutureConcepts.Data.Access.Model;
using FutureConcepts.Mobile.DataContract;
using FutureConcepts.Mobile.Queue;
using FutureConcepts.Tools;

namespace MobileSqlBLM
{
    public class MainModule : BusinessLogicModule
    {
        private readonly String DEVICE_QUEUE_NAME = "sqlreplsync";

        private readonly String TRINITY_CONTENT_PREFIX = "content:://com.futureconcepts.trinity.provider.icdb/";

		private string _publisher;
		private string _subscriber;
        private string _distributor;
        private string _publisherDB;
        private string _subscriberDB;
        private string _article;

        private MobileQueue _queue;

        private BusinessLogicModule _dispatcher;

		public MainModule()
		{
		}

		public override void Initialize(String publisher, String subscriber, String distributor, String publisherDB, String subscriberDB, String article)
		{
            Debug.WriteLine(String.Format("Initialize publisher={0} subscriber={1} distributor={2} publisherDB={3} subscriberDB={4} article={5}", publisher, subscriber, distributor, publisherDB, subscriberDB, article));
			_publisher = publisher;
			_subscriber = subscriber;
            _subscriberDB = subscriberDB;
            _article = article;
//            _queue = QueueManager.GetQueue(subscriber);
            _queue = QueueManager.GetQueue("000000000000000");
		}

		// Declare what types of row changes, conflicts, or errors to handle.
		override public ChangeStates HandledChangeStates
		{
			get
			{
                return ChangeStates.SubscriberInserts | ChangeStates.SubscriberUpdates | ChangeStates.SubscriberDeletes |
                   ChangeStates.PublisherInserts | ChangeStates.PublisherUpdates | ChangeStates.PublisherUpdates;
			}
		}

		public override ActionOnDataChange InsertHandler(SourceIdentifier insertSource, DataSet insertedDataSet, ref DataSet customDataSet, ref int historyLogLevel, ref string historyLogMessage)
		{
            Debug.WriteLine("InsertHandler");
			if (insertSource == SourceIdentifier.SourceIsPublisher)
			{
                try
                {
                    DataTable table = insertedDataSet.Tables[0];
                    if (table != null)
                    {
                        DataRow row = table.Rows[0];
                        if (row != null)
                        {
                            MercuryQueueMessage message = new MercuryQueueMessage();
                            Guid id = new Guid(row["ID"].ToString());
                            message.Param1 = "insert";
                            message.Priority = 5;
                            message.Action = Intent.ACTION_VIEW;
                            message.ServerUrl = "server://Data/" + _subscriberDB + "/" + _article + "/" + id;
                            message.ClientUrl = TRINITY_CONTENT_PREFIX + _article;
                            _queue.Send(DEVICE_QUEUE_NAME, message.GetJson());
                        }
                    }
                }
                catch (Exception e)
                {
                    ErrorLogger.DumpToDebug(e);
                }
				return ActionOnDataChange.AcceptData;
			}
			else
			{
				return base.InsertHandler(insertSource, insertedDataSet, ref customDataSet, ref historyLogLevel, ref historyLogMessage);
			}
		}

		public override ActionOnDataChange UpdateHandler(SourceIdentifier updateSource, DataSet updatedDataSet, ref DataSet customDataSet, ref int historyLogLevel, ref string historyLogMessage)
		{
            Debug.WriteLine("UpdateHandler");
            if (updateSource == SourceIdentifier.SourceIsPublisher)
            {
                try
                {
                    DataTable table = updatedDataSet.Tables[0];
                    if (table != null)
                    {
                        DataRow row = table.Rows[0];
                        if (row != null)
                        {
                            MercuryQueueMessage message = new MercuryQueueMessage();
                            Guid id = new Guid(row["ID"].ToString());
                            message.Param1 = "update";
                            message.Priority = 5;
                            message.Action = Intent.ACTION_VIEW;
                            message.ServerUrl = "server://Data/" + _subscriberDB + "/" + _article + "/" + id;
                            message.ClientUrl = TRINITY_CONTENT_PREFIX + _article + "/" + id;
                            _queue.Send(DEVICE_QUEUE_NAME, message.GetJson());
                        }
                    }
                }
                catch (Exception e)
                {
                    ErrorLogger.DumpToDebug(e);
                }
                return ActionOnDataChange.AcceptData;
            }
            else
            {
                return base.UpdateHandler(updateSource, updatedDataSet, ref customDataSet, ref historyLogLevel, ref historyLogMessage);
            }
		}

		public override ActionOnDataDelete DeleteHandler(SourceIdentifier deleteSource, DataSet deletedDataSet, ref int historyLogLevel, ref string historyLogMessage)
		{
            Debug.WriteLine("DeleteHandler");
			if (deleteSource == SourceIdentifier.SourceIsPublisher)
			{
                try
                {
                    DataTable table = deletedDataSet.Tables[0];
                    if (table != null)
                    {
                        DataRow row = table.Rows[0];
                        if (row != null)
                        {
                            MercuryQueueMessage message = new MercuryQueueMessage();
                            Guid id = new Guid(row["ID"].ToString());
                            message.Param1 = "delete";
                            message.Priority = 5;
                            message.ClientUrl = TRINITY_CONTENT_PREFIX + _article + "/" + id;
                            _queue.Send(DEVICE_QUEUE_NAME, message.GetJson());
                        }
                    }
                }
                catch (Exception e)
                {
                    ErrorLogger.DumpToDebug(e);
                }
				return ActionOnDataDelete.AcceptDelete;
			}
			else
			{
				return base.DeleteHandler(deleteSource, deletedDataSet, ref historyLogLevel, ref historyLogMessage);
			}
        }
    }
}
