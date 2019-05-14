

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcelable;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;

/**
 * This is an example of a custom preference type. The preference counts the number of clicks it has
 * received and stores/retrieves it from the storage.
 */
public class ClickPreference extends Preference
{
    // This is the constructor called by the inflater
    public ClickPreference(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    @Override
    protected void onBindView(View view)
    {
        super.onBindView(view);
    }

    @Override
    protected void onClick()
    {
        // Data has changed, notify so UI can be refreshed!
        notifyChanged();
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index)
    {
        // This preference type's value type is Integer, so we read the default
        // value from the attributes as an Integer.
        return a.getInteger(index, 0);
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue)
    {
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state)
    {
        super.onRestoreInstanceState(state);
        notifyChanged();
    }
}
