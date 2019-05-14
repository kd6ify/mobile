using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Linq;
using System.Text;
using System.Windows;
using System.Windows.Controls;

using FutureConcepts.Mobile.DataContract;

namespace FutureConcepts.Mobile.AdminConsole
{
    public class NodeDataTemplateSelector : DataTemplateSelector
    {
        public override DataTemplate SelectTemplate(object item, DependencyObject container)
        {
            MainPage mainPage = LogicalTreeHelper.FindLogicalNode(Application.Current.MainWindow, "MainPage1") as MainPage;
//            MainPage mainPage = Application.Current.MainWindow.FindName("FutureConcepts.Mobile.AdminConsole.MainPage") as MainPage;
            if (item is DeviceContextGroup)
            {
                return mainPage.FindResource("agencyNodeTemplate") as DataTemplate;
            }
            else
            {
                return mainPage.FindResource("deviceContextNodeTemplate") as DataTemplate;
            }
        }
    }
}
