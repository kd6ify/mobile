using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.ServiceModel;
using System.ServiceModel.Web;

using FutureConcepts.Mobile.DataContract;

namespace FutureConcepts.Mobile.ServiceContract
{
    [ServiceContract(SessionMode=SessionMode.Required, CallbackContract=typeof(IDiagCallback))]
    public interface IDiag
    {
        [OperationContract(IsInitiating = true, IsTerminating = false)]
        void Open();

        [OperationContract(IsInitiating = false, IsTerminating = false)]
        void Devices();

        [OperationContract(IsInitiating = false, IsTerminating = false)]
        void SetDeviceContext(String deviceId);

        [OperationContract(IsInitiating = false, IsTerminating = false)]
        void ShowDeviceContext();

        [OperationContract(IsInitiating = false, IsTerminating = false)]
        void SendIntent(String action);
    }
}
