using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Text;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Documents;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using System.Windows.Navigation;
using System.Windows.Shapes;

using FutureConcepts.Mobile.DataContract;

namespace FutureConcepts.Mobile.AdminConsole
{
    /// <summary>
    /// Interaction logic for ViewTrackerPage.xaml
    /// </summary>
    public partial class ChangeTrackerStatePage : Page
    {
        private DeviceContext _deviceContext;

        public ChangeTrackerStatePage(DeviceContext deviceContext)
        {
            InitializeComponent();
            _deviceContext = deviceContext;
            DataContext = deviceContext;
            this.Loaded += new RoutedEventHandler(Page_Loaded);
        }

        private void Page_Loaded(object sender, RoutedEventArgs e)
        {
            switch (_deviceContext.TrackerState.State)
            {
                case TrackerState.STATE_RUNNING:
                    startTrackerGroup.Visibility = Visibility.Hidden;
                    stopTrackerButton.Visibility = Visibility.Visible;
                    break;
                case TrackerState.STATE_STOPPED:
                    startTrackerGroup.Visibility = Visibility.Visible;
                    stopTrackerButton.Visibility = Visibility.Hidden;
                    break;
                case TrackerState.STATE_UNKNOWN:
                    startTrackerGroup.Visibility = Visibility.Visible;
                    stopTrackerButton.Visibility = Visibility.Visible;
                    break;
            }
        }

        public void startTrackerButton_Click(object sender, RoutedEventArgs e)
        {
            ServerProxy.Instance.StartTrackerOnDevice(_deviceContext.MobileDevice.DeviceId, trackerMode.SelectionBoxItem.ToString());
            _deviceContext.TrackerState = new TrackerState(TrackerState.STATE_RUNNING, trackerMode.SelectionBoxItem.ToString());
            NavigationService.GoBack();
        }

        public void stopTrackerButton_Click(object sender, RoutedEventArgs e)
        {
            ServerProxy.Instance.StopTrackerOnDevice(_deviceContext.MobileDevice.DeviceId);
            _deviceContext.TrackerState = new TrackerState(TrackerState.STATE_STOPPED, null);
            NavigationService.GoBack();
        }
    }
}
