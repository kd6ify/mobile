using System;
using System.Diagnostics;
using System.IO;

namespace FutureConcepts.Mobile.Server
{
    public class Utils
    {
        public static String AssemblyPath
        {
            get
            {
                return Directory.GetParent(System.Reflection.Assembly.GetExecutingAssembly().Location).ToString();
            }
        }
    }
}
