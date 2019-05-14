using System;
using System.Collections.Generic;
using System.Configuration;
using System.Linq;
using System.Text;

using FutureConcepts.Mobile.DataContract;

namespace FutureConcepts.Mobile.Server
{
    public class FileSystemPaths
    {
        private static String _mapPath;

        public static String MapPath
        {
            get
            {
                if (_mapPath == null)
                {
                    String mapPathUnfixed = ConfigurationManager.AppSettings["MapPath"];
                    _mapPath = mapPathUnfixed.Replace("currentdirectory:", Utils.AssemblyPath);
                }
                return _mapPath;
            }
        }

        public static String GlobalMediaMetadataPath
        {
            get
            {
                return ConfigurationManager.AppSettings["GlobalMediaMetadataPath"];
            }
        }

        public static String GetDeviceMediaMetadataPath(DeviceContext deviceContext)
        {
            return ConfigurationManager.AppSettings["DeviceMediaMetadataPath"] + @"/" + deviceContext.DeviceId;
        }
    }
}
