using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Text;

using FutureConcepts.Mobile.DataContract;

namespace FutureConcepts.Mobile.AdminConsole
{
    public class DebugDeviceCreator
    {
        public void Execute()
        {
            ServerProxy proxy = ServerProxy.Instance;
            TransactionContext transactionContext = proxy.CreateDebugDevice();
            Debug.WriteLine("DebugDeviceCreator.Execute: " + transactionContext.ToString());
        }
    }
}
