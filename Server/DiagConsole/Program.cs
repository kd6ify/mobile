using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace FutureConcepts.Mobile.DiagConsole
{
    class Program
    {
        static void Main(string[] args)
        {
            AppContext app = new AppContext();
            app.Run(args);
        }
    }
}
