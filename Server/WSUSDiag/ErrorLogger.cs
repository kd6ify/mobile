using System;
using System.Diagnostics;
using System.Reflection;
using System.IO;
using System.Text;

namespace FutureConcepts.Mobile.Server.WSUSDiag
{
    /// <summary>
    /// ErrorLogger utility class.
    /// Kevin Dixon
    /// </summary>
    public static class ErrorLogger
    {
        /// <summary>
        /// Dumps the specified exception to the Debug console
        /// </summary>
        /// <param name="e">exception to dump</param>
        public static void DumpToDebug(Exception e)
        {
            Debug.WriteLine(ExceptionToString(e));
        }

        /// <summary>
        /// Dumps the specified exception to the Debug console with message from developer
        /// </summary>
        /// <param name="e">exception to dump</param>
        /// <param name="developerComment">Special message from developer</param>
        public static void DumpToDebug(Exception e, string developerComment)
        {
            Debug.WriteLine("Dev Comment: " + developerComment);
            Debug.WriteLine(ExceptionToString(e));
        }

        private static string ExceptionToString(Exception e)
        {
            StringBuilder b = new StringBuilder(1000);
            b.Append("Exception! ");
            b.Append(e.GetType().ToString());
            b.Append(":");
            b.Append(Environment.NewLine);
            b.Append(" Message:");
            b.Append(e.Message);
            b.Append(Environment.NewLine);
            b.Append(" StackTrace:");
            b.Append(Environment.NewLine);
            b.Append(e.StackTrace);
            b.Append(Environment.NewLine);
            b.Append(" TargetSite.Name: ");
            b.Append((e.TargetSite == null) ? "<unknown>" : e.TargetSite.Name);
            b.Append(Environment.NewLine);
            b.Append(" Source: ");
            b.Append(e.Source);

            if (e.InnerException != null)
            {
                b.Append(Environment.NewLine + "Inner");
                b.Append(ExceptionToString(e.InnerException));
            }

            return b.ToString();
        }

        /// <summary>
        /// Dumps an Exception to the specified stream, followed by a stream flush
        /// </summary>
        /// <param name="e">The exception to dump</param>
        /// <param name="s">the stream to write to</param>
        public static void DumpToStream(Exception e, Stream s)
        {
            StreamWriter w = new StreamWriter(s);
            w.WriteLine(ExceptionToString(e));
            w.Flush();
        }

        /// <summary>
        /// Write the exception's Message to the event log for the exception's Source,
        /// in the sub-log "Application", with the EventLogEntryType of Error.
        /// /// Also invokes DumpToDebug
        /// </summary>
        /// <param name="e">Exception to log</param>
        public static void DumpToEventLog(Exception e)
        {
            DumpToEventLog(e, EventLogEntryType.Error);
        }

        /// <summary>
        /// Write the exception's Message to the event log for the exception's Source,
        /// in the sub-log "Application".
        /// Also invokes DumpToDebug
        /// </summary>
        /// <param name="e">Exception to log</param>
        /// <param name="type">specify the type of event</param>
        public static void DumpToEventLog(Exception e, EventLogEntryType type)
        {
            try
            {
                if (!EventLog.SourceExists(e.Source))
                {
                    EventLog.CreateEventSource(e.Source, "Application");
                }

                EventLog.WriteEntry(e.Source, e.ToString(), type);
                DumpToDebug(e);
            }
            catch (Exception eventLogException)
            {
                Debug.WriteLine("Failure writing to EventLog: " + eventLogException.Message);
                DumpToDebug(e);
            }
        }

        /// <summary>
        /// Writes a message to the Application event log for the Executing Assembly.
        /// </summary>
        /// <param name="type">type/severity of the message</param>
        /// <param name="message">message text</param>
        public static void WriteToEventLog(string message, EventLogEntryType type)
        {
            try
            {
                string assemblyName = (new AssemblyName(Assembly.GetEntryAssembly().FullName)).Name;

                if (!EventLog.SourceExists(assemblyName))
                {
                    EventLog.CreateEventSource(assemblyName, "Application");
                }

                EventLog.WriteEntry(assemblyName, message, type);
            }
            catch (Exception eventLogException)
            {
                Debug.WriteLine("Failure writing to EventLog: " + eventLogException.Message);
            }
        }
    }
}
