package org.zeith.tswap.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class TSData
{
	public final File cfgFile;

	public final Properties props = new Properties();

	public TSData(File cfgFile)
	{
		this.cfgFile = cfgFile;
	}

	public boolean shouldSwap()
	{
		return Boolean.parseBoolean(props.getProperty("should_swap", "true"));
	}

	public void shouldSwap(boolean swap)
	{
		props.put("should_swap", Boolean.toString(swap));
	}

	public void load()
	{
		if(cfgFile.isFile())
			try(FileInputStream in = new FileInputStream(cfgFile))
			{
				props.loadFromXML(in);
			} catch(IOException e)
			{
				throw new RuntimeException("Failed to load " + cfgFile);
			}
	}

	public void save()
	{
		try(FileOutputStream out = new FileOutputStream(cfgFile))
		{
			props.storeToXML(out, "ToolSwap data file.", "UTF-8");
		} catch(IOException e)
		{
			throw new RuntimeException("Failed to load " + cfgFile);
		}
	}
}