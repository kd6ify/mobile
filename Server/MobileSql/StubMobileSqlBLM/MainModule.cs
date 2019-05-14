// Documentation for how to implement a BL Handler for a Merge Article is described here:
// http://msdn.microsoft.com/en-us/library/ms147911%28v=SQL.100%29.aspx

using System;
using System.Collections.Generic;
using System.Data;
using System.Diagnostics;
using System.IO;
using System.Xml.Serialization;
using System.Text;

using Microsoft.SqlServer.Replication.BusinessLogicSupport;

namespace MobileSqlBLM
{
    public class MainModule : BusinessLogicModule
    {
		private string _publisher;
		private string _subscriber;
        private string _distributor;
        private string _publisherDB;
        private string _subscriberDB;
        private string _article;

        private BusinessLogicModule _dispatcher;

		public MainModule()
		{
            Debug.WriteLine("MainModule");
		}

		public override void Initialize(String publisher, String subscriber, String distributor, String publisherDB, String subscriberDB, String article)
		{
            Debug.WriteLine(String.Format("Initialize publisher={0} subscriber={1} distributor={2} publisherDB={3} subscriberDB={4} article={5}", publisher, subscriber, distributor, publisherDB, subscriberDB, article));
			_publisher = publisher;
			_subscriber = subscriber;
            _article = article;
		}

		// Declare what types of row changes, conflicts, or errors to handle.
		override public ChangeStates HandledChangeStates
		{
			get
			{
                Debug.WriteLine("HandledChangeStates");
                return ChangeStates.SubscriberInserts | ChangeStates.SubscriberUpdates | ChangeStates.SubscriberDeletes |
                   ChangeStates.PublisherInserts | ChangeStates.PublisherUpdates | ChangeStates.PublisherUpdates;
			}
		}

		public override ActionOnDataChange InsertHandler(SourceIdentifier insertSource, DataSet insertedDataSet, ref DataSet customDataSet, ref int historyLogLevel, ref string historyLogMessage)
		{
            Debug.WriteLine("InsertHandler");
			return base.InsertHandler(insertSource, insertedDataSet, ref customDataSet, ref historyLogLevel, ref historyLogMessage);
		}

		public override ActionOnDataChange UpdateHandler(SourceIdentifier updateSource, DataSet updatedDataSet, ref DataSet customDataSet, ref int historyLogLevel, ref string historyLogMessage)
		{
            Debug.WriteLine("UpdateHandler");
            return base.UpdateHandler(updateSource, updatedDataSet, ref customDataSet, ref historyLogLevel, ref historyLogMessage);
		}

		public override ActionOnDataDelete DeleteHandler(SourceIdentifier deleteSource, DataSet deletedDataSet, ref int historyLogLevel, ref string historyLogMessage)
		{
            Debug.WriteLine("DeleteHandler");
			return base.DeleteHandler(deleteSource, deletedDataSet, ref historyLogLevel, ref historyLogMessage);
		}
    }
}
