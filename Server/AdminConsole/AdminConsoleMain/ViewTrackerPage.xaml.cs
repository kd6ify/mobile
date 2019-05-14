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
    public partial class ViewTrackerPage : Page
    {
        private DeviceContext _deviceContext;

        public ViewTrackerPage(DeviceContext deviceContext)
        {
            InitializeComponent();
            _deviceContext = deviceContext;
            DataContext = _deviceContext;
            Loaded += new RoutedEventHandler(Page_Loaded);
        }

        private void Page_Loaded(object sender, RoutedEventArgs e)
        {
            if (_deviceContext == null)
            {
                NavigationService.GoBack();
                return;
            }

            if (_deviceContext.LastKnownLocation != null)
            {
                Location location = _deviceContext.LastKnownLocation;
                Uri baseUri = new Uri("http://google.com");
                Uri targetUri = new Uri(baseUri, "maps?output=embed&q=" + location.Latitude + "," + location.Longitude);
                locationWebView.Navigate(targetUri);
            }
        }

        private void Hyperlink_Click(object sender, RoutedEventArgs e)
        {
            Hyperlink hyperlink = e.Source as Hyperlink;
            if (hyperlink.NavigateUri.ToString() == "ChangeTrackerStatePage.xaml")
            {
                NavigationService.Navigate(new ChangeTrackerStatePage(_deviceContext));
            }
            else if (hyperlink.NavigateUri.ToString() == "AddDevicePage.xaml")
            {
                NavigationService.Navigate(new AddDevicePage() { DataContext = new AddDevicePageViewModel(_deviceContext.MobileDevice) });
            }
        }

        private void GoogleEarthButton_Click(object sender, RoutedEventArgs e)
        {
            Uri uri = new Uri("http://localhost:8280/Admin/GoogleEarth/" + _deviceContext.MobileDevice.DeviceId);
            locationWebView.Navigate(uri);
        }

        private void CheckUpdate_Click(object sender, RoutedEventArgs e)
        {
            try
            {
                ServerProxy.Instance.TestCheckUpdate();
            }
            catch (Exception ex)
            {
                MessageBox.Show(ex.ToString(), "Error!");
            }
        }
    }
}
