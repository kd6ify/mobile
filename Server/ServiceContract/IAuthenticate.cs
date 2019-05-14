using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Messaging;
using System.Runtime.Serialization;
using System.ServiceModel;
using System.ServiceModel.Channels;
using System.ServiceModel.Web;
using System.Text;
using System.Xml.Linq;

using FutureConcepts.Mobile.DataContract;

namespace FutureConcepts.Mobile.ServiceContract
{
    [ServiceContract]
    public interface IAuthenticate
    {
        [OperationContract]
        [WebGet(BodyStyle = WebMessageBodyStyle.Bare,
            RequestFormat = WebMessageFormat.Json,
            ResponseFormat = WebMessageFormat.Json,
            UriTemplate = "Login/{deviceId}")]
        DeviceContext LoginWithDeviceId(String deviceId);

        [OperationContract]
        [WebGet(BodyStyle = WebMessageBodyStyle.Bare,
            RequestFormat = WebMessageFormat.Json,
            ResponseFormat = WebMessageFormat.Json,
            UriTemplate = "Login")]
        DeviceContext Login();
    }
}
