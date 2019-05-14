This file is a markdown file that can be viewed using Confluence or the Firefox Markdown Viewer.

# Mobile Server

MobileServer is a Windows WCF service that provides services
to mobile devices--currently Android and iOS.

Following is a brief outline of each of the services it provides.

## Admin service

* Ping - provides application-level Ping service--returns Pong.
* GetStatus - returns detailed server status, including synchronization status for each device.
* GetConfigurationWithID - returns device configuration (used by DownloadConfiguration on iOS and Android)
* GetConfiguration - same as GetConfigurationWithID, but deviceID is sent as an HTTP header

## Authenticate service

* Login/LoginWithDeviceId - provide a mechanism to fetch the authentication token from the WCF WebContext
 
## ICDBResource

This service provides the database synchronization services.

Project Location: Projects20/Mobile/Server

## TrackerV3

This service receives tracker location updates from mobile devices and posts the
last known location to the SQL Server db using the UpdateDeviceLocation stored procedure.


# Android Projects

## Jupiter (Android Topographic Map)

Project Jupiter was an attempt at a Topographic map for Android.
In was to be used by first responders in backcountry locations.
At the time this project was started, there was very little
currently implemented offline map solutions available that would
work relatively well on the then performance constrained Android
devices, such as the Android Google Phone (aka Android Dream/G1).

A german company called OneStepAhead had an offline mapping solution
that served our purposes. We purchased their SDK for ~ $5,000 and
implemented the topo map.  A couple years later, OneStepAhead folded
up and is now defunct.

From my recollection, the map worked fairly well but was a little slow
on rendering for the then available hardware.  We also needed to generate
our own maps using a very convoluted and complicated tool chain based 
on OSM.

Today, there are better open source solutions for rendering offline maps
and Android hardware and operating system are considerably faster.

This project was never deployed to customers.

Project location: Projects20/Mobile/Android/Jupiter


## GQueueCommon

The GQueueCommon project implements common functions used by other Android projects
to access common functionality.  Originally this was the synchronizer queueing services,
hence the name, but later Settings was added, so it should be renamed AntaresXCommon
or something.

Project location: Projects20/Mobile/GQueueCommon

## Mercury (AntaresX Settings)

The Android Mercury project is the Android application that stores
and makes available settings type data for all AntaresX applications.

Other applications, like AxSync, AxTrinity, Jupiter, etc. fetch and set
settings data using a client library (GQueueCommon).  The client talks
to Mercury (Settings) through the Android Content Provider API.

Project Location: Projects20/Mobile/Android/Mercury

## AxSync (Android database sync engine)

AxSync is the Android SQL Server<-->SQLite database synchronization service.
Using services provided by MobileServer, an Android phone's local ICDB
database is synchronized.  All locally made changes are applied to
the SQL Server ICDB database and all changes made to the SQL Server
ICDB database are applied to the local SQLite database.

In general, it works well.  One area that may need further investigation
is how conflicts are handled.  For example, what if an Android Trinity/Trikorder
user and a AntaresX desktop user both changed an Equipment row at the same
time.  


## AxTrinity (Android Incident Command)

AxTrinity (aka Trikorder/Trinity) is the Android AntaresX Incident command
application.  Using AxTrinity, users can view Incident Logs, Triage and Assets.
In addition, the user can create log entries and take photos that are attached
to log entries.  

AxTrinity uses the AxSync service for synchronizing the local SQLite database
with the backend Microsoft SQL Server database using the services of MobileServer.

Project location: Projects20/Mobile/Android/AxTrinity


## AxBroadcaster

AxBroadcaster is an Android application that allows an Android phone user to uplink the phone's camera to WOWZA for broadcasting.
Other users can view the user's camera in realtime.

The RTSP/RTP protocols are used to uplink the video to WOWZA.  WOWZA then restreams the uplink as HLS or RTSP/RTP, depending on
how the WOWZA AxBro application was configured.  

AxBroadcaster uses a very simple but useful library for implementing the RTSP/RTP protocol in the AxStreaming project.

This project has not been deployed for customer use, although several field tests were performed including tests
by Wayne and Dana, and reports are that it worked very well.

Project Location: Projects20/Mobile/Android/AxBroadcster, Projects20/Mobile/Android/AxStreaming

## AxVideoViewer

AxVideoViewer is the Android Video Viewer component of Trinity/Trikorder.  It uses the services
of VSM to fetch the list of viewable cameras and uses the Android Media Framework API to 
view a camera.  As of Android 4.0, Android supports HTTP Live Streaming (known as
cupertino streaming to WOWZA).

Project Location: Projects20/Mobile/Android/AxVideoViewer


## AxRex (Incident Requester)

AxRex was a project that allows a user to request the creation of an Incident.
Cesar wrote a similar application for the web.  This has never been deployed.

Project Location: Projects20/Mobile/Android/AxRex


## Drake (Chat)

Drake is the code name (thank Cheryl) for the Android chat application.
This code implements a chat application using the well-known SMACK/ASMACK
library.  

In general it worked, but during testing there were some obscure issues
relating to disconnect/reconnect that would cause a user to miss a chat
message.

There have recently been reported issues with ASMACK and that SMACK 
now replaces ASMACK.  I believe that the issues we have seen before with 
the lost messages may be resolved using the latest version of the SMACK
library.

Dave also added code for doing in-conversation file transfers.

Project Location: Projects20/Mobile/Android/Drake


# NEXAR/AIRS SVD (Surveillance Camera Viewer)

NEXAR/AIRS SVD is the surveillance camera viewer that is invoked
during a system panic.  We started out with SVD code and stripped
out everything we didn't need.  Then we added the glue that connects
to the main AIRS Service that receives panics.  When a panic is received
the cameras are played.  When the panic is cancelled the camera viewing
is stopped.

Because NEXAR SVD was based on SVD and SVD uses the LEADTOOLS RTSP Source
DirectShow filter, it has the issues LEADTOOLS has with it.

You may want to consider replacing NEXAR SVD with an implementation that
uses the Elecard RTSP Source filter.  Or, you may want to eliminate DirectShow
completely.  

If you eliminate DirectShow, you may want to consider rewriting Nexar SVD
and SVD using WPF and Vlc.DotNet. 

A proof-of-concept of WPF/Vlc.DotNet can be found in Dave's hard-drive
under Users\darnold\Desktop\Vlc.DotNet-master\Vlc.DotNet-master\src\Vlc.DotNet.sln

As another option, a camera video could be implemented as a ASP.NET MVC application.

A proof-of-concept for a WebViewerVideo can be found on Dave's hard drive at
Projects20/Media/WebVideoViewer.

# Development Procedures

## Code modification checklist

I used the following checklist whenever I made changes to code:

	[  ]	Drag Jira bug to "In Progress"

	[  ]	Review and update times.

	[  ]	SVN Update -- be sure you are working with latest code base and pick up all other changes made by others
		Enable all test modules/JUNIT/.NETUNIT/debugging code, etc.

	[  ]	Enable all test modules/JUNIT/.NETUNIT/debugging code, etc.

	[  ]	Fix Bug

	[  ]	Test bug fix

	[  ]	Log Work -- do this every day  while bug is "In Progress"

	[  ]	Document code changes in projects changelog.txt

	[  ]	Update version # in AssemblyInfo.cs and Setup Project

	[  ]	Create version# for project:
		- View bug
		- top-right click gear, select "Projects"
		- Select project from List
		- Select "Versions" from left-hand side
		- Create a new version--Name="X.X.X.X"
		- don't give it a release date--Ryan uses that for live releases (deployments)

	[  ]	Disable test modules/JUNIT, etc.

	[  ]	Prepare and build for Test Builds drop (Clean, rebuild, etc.)

	[  ]	Copy project installer to Test Builds/<date>

	[  ]	Send build mail with changelog for this version to software, qa

	[  ]	Drag Jira bug to "Testing" -- select new Version# in Resolve dialog, add changelog text
		to comment field.  Update Times.

	[  ]	Check-in code to SVN--be sure to mention the project version#, Jira bug name
		and summary description in the SVN commit log


## Debugging Windows Crashes

Some useful websites from troubleshooting Windows Application crashes:

* http://blogs.msdn.com/b/wer/archive/2009/03/11/an-overview-of-wer-consent-settings-and-corresponding-ui-behavior.aspx

* http://blogs.msdn.com/b/oanapl/archive/2009/01/30/windows-error-reporting-wer-and-clr-integration.aspx

* http://blogs.msdn.com/b/jiangyue/archive/2010/03/16/windows-heap-overrun-monitoring.aspx

* http://blogs.msdn.com/b/oanapl/archive/2009/01/28/windows-error-reporting-wer-for-developers.aspx

* http://stackoverflow.com/questions/20995151/windows-application-has-stopped-working
