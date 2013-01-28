/*
 * Copyright (c) NASK, NCSC
 * 
 * This file is part of HoneySpider Network 2.0.
 * 
 * This is a free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package pl.nask.hsn2.service;

import java.io.File;
import java.net.URISyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.nask.hsn2.CommandLineParams;
import pl.nask.hsn2.GenericServiceInfo;

public class SwfCommandLineParams extends CommandLineParams {
	private final static Logger LOGGER = LoggerFactory.getLogger("SwfCommandLineParams");
	private final static OptionNameWrapper PLUGINS_PATH = new OptionNameWrapper("plugins", "pluginsPath");

	@Override
	public void initOptions() {
		super.initOptions();
		addOption(PLUGINS_PATH, "path", "Full path to plugins directory");
	}

	@Override
	protected void initDefaults() {
		super.initDefaults();
		setDefaultServiceNameAndQueueName("swf-cve");
		try {
			setDefaultPluginsPath(GenericServiceInfo.getServicePath(SwfService.class) + File.separator + "plugins");
		} catch (URISyntaxException e) {
			LOGGER.error("Error while trying to set DefaultPluginsPath!",e);
		}
	}

	public void setDefaultPluginsPath(String pluginsPath) {
		setDefaultValue(PLUGINS_PATH, pluginsPath);
	}

	public String getPluginsPath() {
		return getOptionValue(PLUGINS_PATH);
	}
}
