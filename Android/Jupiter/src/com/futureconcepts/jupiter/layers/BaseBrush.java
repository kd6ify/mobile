package com.futureconcepts.jupiter.layers;

import com.osa.android.droyd.map.AreaLayerStyle;

import android.graphics.drawable.ShapeDrawable;

public interface BaseBrush
{
	void applyToDrawable(ShapeDrawable drawable);
	void fillArea(AreaLayerStyle style);
}
