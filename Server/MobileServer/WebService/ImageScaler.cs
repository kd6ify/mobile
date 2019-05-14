using System;
using System.Collections.Generic;
using System.Drawing;
using System.Data.Linq;
using System.IO;
using System.Linq;
using System.Text;

using L2S = FutureConcepts.Data.Access.CDAL.L2S;

namespace FutureConcepts.Mobile.Server.WebService
{
    public class ImageScaler
    {

        private static bool ThumbnailCallback()
        {
            return false;
        }

        // See http://blog.jitbit.com/2009/09/aspnet-image-resizing-out-of-memory.html
        public static Binary ScaleResource(Binary icon)
        {
            Binary value = icon;
            Image.GetThumbnailImageAbort myCallback = new Image.GetThumbnailImageAbort(ThumbnailCallback);
            MemoryStream stream = new MemoryStream(icon.ToArray());
            Image image = Bitmap.FromStream(stream);
            int maxHeight = 64;
            int newWidth = 64;
            int newHeight = image.Height * newWidth / image.Width;
            if (newHeight > maxHeight)
            {
                // Resize with height instead
                newWidth = image.Width * maxHeight / image.Height;
                newHeight = maxHeight;
            }
            Image thumbnail = image.GetThumbnailImage(newWidth, newHeight, myCallback, IntPtr.Zero);
            MemoryStream outStream = new MemoryStream();
            thumbnail.Save(outStream, System.Drawing.Imaging.ImageFormat.Png);
            value = new Binary(outStream.ToArray());
            thumbnail.Dispose();
            outStream.Close();
            image.Dispose();
            stream.Close();
            return value;
        }

        // See http://blog.jitbit.com/2009/09/aspnet-image-resizing-out-of-memory.html
        public static Binary ScaleResource(L2S.Icon icon)
        {
            Binary value = null;
            Image.GetThumbnailImageAbort myCallback = new Image.GetThumbnailImageAbort(ThumbnailCallback);
            MemoryStream stream = new MemoryStream(icon.Image);
            Image image = Bitmap.FromStream(stream);
            int maxHeight = 64;
            int newWidth = 64;
            int newHeight = image.Height * newWidth / image.Width;
            if (newHeight > maxHeight)
            {
                // Resize with height instead
                newWidth = image.Width * maxHeight / image.Height;
                newHeight = maxHeight;
            }
            Image thumbnail = image.GetThumbnailImage(newWidth, newHeight, myCallback, IntPtr.Zero);
            MemoryStream outStream = new MemoryStream();
            thumbnail.Save(outStream, System.Drawing.Imaging.ImageFormat.Png);
            value = new Binary(outStream.ToArray());
            thumbnail.Dispose();
            outStream.Close();
            image.Dispose();
            stream.Close();
            return value;
        }

    }
}
