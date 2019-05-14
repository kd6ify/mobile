using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Drawing;
using System.Linq;
using System.Text;

namespace FutureConcepts.Mobile.Server
{
    public class ImageList
    {
        private Dictionary<String, Image> _dict = new Dictionary<String, Image>();

        public void Add(String id, Image image)
        {
            try
            {
                _dict.Add(id, image);
            }
            catch (Exception exc)
            {
                Debug.WriteLine(String.Format("Failed adding image {0} to ImageList because {1}", id, exc.Message));
            }
        }

        public Image GetImage(String id)
        {
            return _dict[id];
        }

        public bool Contains(String id)
        {
            return _dict.ContainsKey(id);
        }
    }
}
