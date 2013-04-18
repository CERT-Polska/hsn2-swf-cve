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

import java.lang.Thread.UncaughtExceptionHandler;

import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonController;
import org.apache.commons.daemon.DaemonInitException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.nask.hsn2.GenericService;
import pl.nask.swftool.cvetool.CveTool;

public final class SwfService implements Daemon {
	private volatile DaemonController daemonCtrl = null;
	private SwfCommandLineParams cmd;
	private Thread serviceRunner;
	GenericService service = null;
	private static final Logger LOGGER = LoggerFactory.getLogger(SwfService.class);

	public static void main(final String[] args) throws DaemonInitException, InterruptedException {
		SwfService swfs = new SwfService();
		swfs.init(new DaemonContext() {
			@Override
			public DaemonController getController() {
				return null;
			}
			
			@Override
			public String[] getArguments() {
				return args;
			}
		});
		swfs.start();
		while (!swfs.serviceRunner.isInterrupted()) {
			Thread.sleep(1000l);
		}
		swfs.stop();
		swfs.destroy();
	}

	private static CveTool initCveTool(String pluginsDirectory) {
		CveTool ct = new CveTool();
		ct.loadPlugins(pluginsDirectory);
		ct.printPluginsInfo();
		ct.bulidPluginsDistributor();

		return ct;
	}

	private static SwfCommandLineParams parseArguments(String[] args) {
		SwfCommandLineParams params = new SwfCommandLineParams();
		params.parseParams(args);

		return params;
	}

	

	@Override
	public void init(DaemonContext context) throws DaemonInitException {
		daemonCtrl = context.getController();
		cmd = parseArguments(context.getArguments());
		LOGGER.info("Service initialized: {}",this.getClass().getSimpleName());
		
	}

	@Override
	public void start() {
		CveTool tool = initCveTool(cmd.getPluginsPath());
		
		this.service = new GenericService(new SwfTaskFactory(tool), cmd.getMaxThreads(), cmd.getRbtCommonExchangeName(), cmd.getRbtNotifyExchangeName());
		cmd.applyArguments(service);
		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
			
			@Override
			public void uncaughtException(Thread t, Throwable e) {
				if ( daemonCtrl != null) {
					daemonCtrl.fail(e.getMessage());
				}
				else {
					LOGGER.warn("Service exit.");
					System.exit(1);
				}
			}
		});
		
		serviceRunner = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					service.run();
				} catch (InterruptedException e) {
					if ( daemonCtrl != null) {
						daemonCtrl.shutdown();
					} else {
						System.exit(0);
					}
				}
				
			}
		});
		serviceRunner.start();
		if (service.waitForStartUp()) {
			LOGGER.info("Service started.");
		} else {
			LOGGER.warn("Error on startup.please report.");
		}
	}

	@Override
	public void stop() throws InterruptedException {
		LOGGER.info("Stopping service.");
		serviceRunner.interrupt();
		service.stop();
		serviceRunner.join();
	}

	@Override
	public void destroy() {
		daemonCtrl = null;
		LOGGER.info("Service shut down.");
	}
}
