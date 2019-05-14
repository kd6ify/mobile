using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Runtime.Serialization;
using System.Text;

namespace FutureConcepts.Mobile.DataContract
{
    [DataContract]
    public class Location
    {
        public Location()
        {
        }

        private String _name;

        [DataMember(EmitDefaultValue = false)]
        public String Name
        {
            get { return _name; }
            set { _name = value; }
        }

        private String _description;

        [DataMember(EmitDefaultValue = false)]
        public String Description
        {
            get { return _description; }
            set { _description = value; }
        }

        private double _accuracy;

        [DataMember]
        public double Accuracy
        {
            get { return _accuracy; }
            set { _accuracy = value; }
        }

        private bool _hasAccuracy;

        [DataMember]
        public bool HasAccuracy
        {
            get { return _hasAccuracy; }
            set { _hasAccuracy = value; }
        }

        private double _altitude;

        [DataMember]
        public double Altitude
        {
            get { return _altitude; }
            set { _altitude = value; }
        }

        private bool _hasAltitude;

        public bool HasAltitude
        {
            get { return _hasAltitude; }
            set { _hasAltitude = value; }
        }

        private double _bearing;

        [DataMember]
        public double Bearing
        {
            get { return _bearing; }
            set { _bearing = value; }
        }

        private bool _hasBearing;

        [DataMember]
        public bool HasBearing
        {
            get { return _hasBearing; }
            set { _hasBearing = value; }
        }

        private double _latitude;

        [DataMember]
        public double Latitude
        {
            get { return _latitude; }
            set { _latitude = value; }
        }

        private double _longitude;

        [DataMember]
        public double Longitude
        {
            get { return _longitude; }
            set { _longitude = value; }
        }

        private String _provider;

        [DataMember(EmitDefaultValue = false)]
        public String Provider
        {
            get { return _provider; }
            set { _provider = value; }
        }

        private double _speed;

        [DataMember]
        public double Speed
        {
            get { return _speed; }
            set { _speed = value; }
        }

        private bool _hasSpeed;

        [DataMember]
        public bool HasSpeed
        {
            get { return _hasSpeed; }
            set { _hasSpeed = value; }
        }

        private long _time; // milliseconds since Jan 1 1970

        [DataMember]
        public long Time
        {
            get { return _time; }
            set { _time = value; }
        }

        private int _satellites; // number of satellites in view at the time of fix

        [DataMember]
        public int Satellites
        {
            get { return _satellites; }
            set { _satellites = value; }
        }

        private int _batteryLevel;

        [DataMember]
        public int BatteryLevel
        {
            get { return _batteryLevel; }
            set { _batteryLevel = value; }
        }

        public DateTime TimeAsDateTime
        {
            get
            {
                return new DateTime(1970, 1, 1, 0, 0, 0).AddMilliseconds(_time);
            }
        }

        public DateTime TimeAsLocalTime
        {
            get
            {
                DateTime time = new DateTime(1970, 1, 1, 0, 0, 0).AddMilliseconds(_time);
                return time.ToLocalTime();
            }
        }
    }
}
