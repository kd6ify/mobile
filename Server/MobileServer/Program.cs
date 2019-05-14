using System.Diagnostics;

namespace FutureConcepts.Mobile.Server
{
    class Program
    {
        static void Main(string[] args)
        {
            WindowServiceWrapper wrapper = new WindowServiceWrapper(new WindowService(), args.Length > 0 && args[0].ToUpper() == "DEBUG");
            Debug.WriteLine("Main exit--Please wait one minute for the SQL Service Broker thread to abort");
        }
    }
}
