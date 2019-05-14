using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.ComponentModel;
using FutureConcepts.Mobile.DataModel;
using System.Collections.ObjectModel;
using System.Windows.Input;
using FutureConcepts.UI.Input.Commands;
using FutureConcepts.Mobile.DataContract;
using System.Windows.Navigation;
using System.Windows;

namespace FutureConcepts.Mobile.AdminConsole
{
    public class AddDevicePageViewModel : INotifyPropertyChanged
    {
        /// <summary>
        /// Instantiates a view model to edit a device
        /// </summary>
        /// <param name="device"></param>
        public AddDevicePageViewModel(MobileDevice device)
        {
            if (device == null) throw new ArgumentNullException("device cannot be null!");

            this.Device = device;
            this.Title = "Edit " + this.Device.UserName;
            this.EditMode = true;
            
        }

        /// <summary>
        /// Instantiates a view model to add a new device
        /// </summary>
        public AddDevicePageViewModel()
        {
            this.Title = "Add New Device";
            this.EditMode = false;
        }

        private static readonly Guid TrackerEquipmentType = new Guid("D6EB1AA5-1CBC-4580-A66E-B76CBD6E19D3");

        #region Methods

        private IEnumerable<string> UsedDeviceIds { get; set; }

        public void Initialize()
        {
            ICCDataContext dc = new ICCDataContext(GlobalState.Instance.ServerStatus.ConnectionStrings["ICC"]);

            UsedDeviceIds = from i in dc.MobileDevices select i.DeviceId;

            var allocedEquipment = from i in dc.MobileDevices select i.EquipmentId;

            var result = from i in dc.Equipments
                         where ((i.EquipmentType == TrackerEquipmentType) && 
                                (!allocedEquipment.Contains(i.EquipmentId) || this.EditMode))
                         orderby i.Tag
                         select i;

            TrackerEquipment = new ObservableCollection<Equipment>(result);

            if (this.EditMode)
            {
                SelectedTrackerEquipment = result.FirstOrDefault(i => i.EquipmentId == this.Device.EquipmentId);
            }
            else
            {
                SelectedTrackerEquipment = null;
            }
        }

        public void ExecuteSaveChanges()
        {
            try
            {
                TransactionContext cxt = ServerProxy.Instance.CreateDevice(Device);
                if (!string.IsNullOrEmpty(cxt.ErrorMessage))
                {
                    throw new Exception(cxt.ErrorMessage);
                }
            }
            catch (Exception ex)
            {
                MessageBox.Show(ex.ToString(), "Error adding device!", MessageBoxButton.OK);
            }
        }

        public bool CanSaveChanges()
        {
            return !UniqueInUse;
        }

        internal void OnDeviceIdChanged(string currentvalue)
        {
            if (UsedDeviceIds == null)
            {
                UniqueInUse = false;
            }
            else
            {
                UniqueInUse = UsedDeviceIds.Contains(currentvalue);
            }
        }

        #endregion

        #region Properties

        private string _title;

        public string Title
        {
            get
            {
                return _title;
            }
            set
            {
                if (_title != value)
                {
                    _title = value;
                    NotifyPropertyChanged("Title");
                }
            }
        }

        private bool _editMode;

        public bool EditMode
        {
            get
            {
                return _editMode;
            }
            private set
            {
                if (_editMode != value)
                {
                    _editMode = value;
                    NotifyPropertyChanged("EditMode");
                }
            }
        }



        private bool _uniqueInUse;

        public bool UniqueInUse
        {
            get
            {
                return _uniqueInUse;
            }
            set
            {
                if (_uniqueInUse != value)
                {
                    _uniqueInUse = value;
                    NotifyPropertyChanged("UniqueInUse");
                }
            }
        }



        private ObservableCollection<Equipment> _trackerEquips;

        public ObservableCollection<Equipment> TrackerEquipment
        {
            get
            {
                return _trackerEquips;
            }
            set
            {
                if (_trackerEquips != value)
                {
                    _trackerEquips = value;
                    NotifyPropertyChanged("TrackerEquipment");
                }
            }
        }

        private Equipment _selectedEquipment;

        public Equipment SelectedTrackerEquipment
        {
            get
            {
                return _selectedEquipment;
            }
            set
            {
                if (_selectedEquipment != value)
                {
                    _selectedEquipment = value;
                    NotifyPropertyChanged("SelectedTrackerEquipment");
                }
            }
        }



        private MobileDevice _device;

        public MobileDevice Device
        {
            get
            {
                return _device;
            }
            set
            {
                if (_device != value)
                {
                    _device = value;
                    NotifyPropertyChanged("Device");
                }
            }
        }

        #endregion

        #region INotifyPropertyChanged Members

        /// <summary>
        /// Event that notifies of property changes
        /// </summary>
        public event PropertyChangedEventHandler PropertyChanged;
        /// <summary>
        /// Fires the PropertyChanged event to notify listeners
        /// that a property has changed
        /// </summary>
        /// <param name="propertyName">the name of the property that has changed.</param>
        protected void NotifyPropertyChanged(string propertyName)
        {
            if (PropertyChanged != null)
                PropertyChanged.Invoke(this, new PropertyChangedEventArgs(propertyName));
        }

        #endregion
    }
}
