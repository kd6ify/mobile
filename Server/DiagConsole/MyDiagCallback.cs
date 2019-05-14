using System;
using System.Collections.Generic;
using System.Collections.Specialized;
using System.IO;
using System.Diagnostics;
using System.Runtime.Serialization;
using System.ServiceModel;
using System.ServiceModel.MsmqIntegration;
using System.Text;
using System.Timers;
using System.Xml;
using FutureConcepts.Mobile.DataContract;
using FutureConcepts.Mobile.ServiceContract;

namespace FutureConcepts.Mobile.DiagConsole
{
    public class MyDiagCallback : IDiagCallback
    {
        public void WriteLine(String msg)
        {
            Console.WriteLine(msg);
        }
    }
}
