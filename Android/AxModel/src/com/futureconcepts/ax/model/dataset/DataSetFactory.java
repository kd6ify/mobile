package com.futureconcepts.ax.model.dataset;

public class DataSetFactory
{
	public static DataSet get(String dataSetClassName)
	{
		DataSet result = null;
		try
		{
			Class<?> dataSetClass = Class.forName(dataSetClassName);
			java.lang.reflect.Constructor<?>[] constructors = dataSetClass.getConstructors();
			java.lang.reflect.Constructor<?> constructor = constructors[0];
			result = (DataSet)constructor.newInstance();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return result;
	}
}
