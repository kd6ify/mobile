using System;
using System.Collections.Generic;
using System.ServiceModel.Web;
using System.Text;

using FutureConcepts.Settings;

namespace FutureConcepts.Mobile.Server.WebService
{
    public class ExceptionHelper
    {
        private readonly String TAG = "ExceptionHelper: ";

        public static List<Dictionary<String, Object>> ConvertExceptionToDictionary(Exception e)
        {
            WebOperationContext context = WebOperationContext.Current;
            context.OutgoingResponse.ContentType = "application/vnd.futureconcepts.mobileserver.exception-v1+json";
            List<Dictionary<String, Object>> result = new List<Dictionary<String, Object>>();
            for (Exception currentException = e; currentException != null; currentException = currentException.InnerException)
            {
                result.Add(ConvertExceptionToEntry(currentException));
            }
            return result;
        }

        public static Dictionary<String, Object> ConvertExceptionToEntry(Exception e)
        {
            Dictionary<String, Object> result = new Dictionary<String, Object>();
            if (e.Message != null)
            {
                result.Add("Message", e.Message);
            }
            if (e.HelpLink != null)
            {
                result.Add("HelpLink", e.HelpLink);
            }
            if (e.Source != null)
            {
                result.Add("Source", e.Source);
            }
            if (e.Data != null)
            {
                StringBuilder sb = new StringBuilder();
                Dictionary<String, Object> dataDict = new Dictionary<String, Object>();
                foreach (String key in e.Data.Keys)
                {
                    if (e.Data[key] is String)
                    {
                        sb.Append(key);
                        sb.Append("=");
                        sb.Append(e.Data[key]);
                        sb.Append(" ");
                    }
                }
                result.Add("Data", sb.ToString());
            }
            result.Add("StackTrace", e.StackTrace);
     //       if (e.InnerException != null)
      //      {
      //          result.Add("InnerException", ConvertExceptionToEntry(e.InnerException));
       //     }
            return result;
        }
    }
}
