using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.ServiceModel;
using System.ServiceModel.Web;

using FutureConcepts.Mobile.DataContract;

namespace FutureConcepts.Mobile.ServiceContract
{
    public interface IDiagCallback
    {
        [OperationContract(IsOneWay=true)]
        void WriteLine(String msg);
    }
}
