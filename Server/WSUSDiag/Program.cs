using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace FutureConcepts.Mobile.Server.WSUSDiag
{
    class Program
    {
        static void Main(string[] args)
        {
            Diagnostics diag = new Diagnostics();
            diag.CheckBasic();
        }
    }
}
