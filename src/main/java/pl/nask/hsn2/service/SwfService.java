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

import pl.nask.hsn2.GenericService;
import pl.nask.swftool.cvetool.CveTool;

public final class SwfService implements Daemon {
	private volatile DaemonController daemonCtrl = null;
	private SwfCommandLineParams cmd;
	private Thread serviceRunner;

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
		swfs.serviceRunner.join();
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
		
	}

	@Override
	public void start() {
		CveTool tool = initCveTool(cmd.getPluginsPath());
		
		final GenericService service = new GenericService(new SwfTaskFactory(tool), cmd.getMaxThreads(), cmd.getRbtCommonExchangeName(), cmd.getRbtNotifyExchangeName());
		cmd.applyArguments(service);
		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
			
			@Override
			public void uncaughtException(Thread t, Throwable e) {
				if ( daemonCtrl != null) {
					daemonCtrl.fail(e.getMessage());
				}
				else {
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
	}

	@Override
	public void stop() throws InterruptedException {
		serviceRunner.interrupt();
		serviceRunner.join();
	}

	@Override
	public void destroy() {
		daemonCtrl = null;
	}
}
