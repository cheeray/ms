package com.cheeray.ms;

import java.lang.management.ManagementFactory;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.loader.JarLauncher;
import org.springframework.boot.loader.jar.JarFile;

/**
 * Override default spring boot launcher to create a service wrapper for procrun.
 * @author Chengwei.Yan
 */
public class MicroService extends JarLauncher {
	private static MicroService SRV = null;
	private static ClassLoader CL = null;
	private static final Logger LOG = LoggerFactory.getLogger(MicroService.class);

	protected void launch(String[] args, String mainClass, ClassLoader classLoader,
			boolean wait) throws Exception {
		Thread.currentThread().setContextClassLoader(classLoader);
		Thread thread = new Thread(() -> {
			try {
				createMainMethodRunner(mainClass, args, classLoader).run();
			} catch (Exception ex) {
			}
		});
		thread.setContextClassLoader(classLoader);
		thread.setName(Thread.currentThread().getName());
		thread.start();
		if (wait == true) {
			thread.join();
		}
	}

	public static void start(String[] args) {
		SRV = new MicroService();
		try {
			JarFile.registerUrlProtocolHandler();
			CL = SRV.createClassLoader(SRV.getClassPathArchives());
			SRV.launch(args, MsApplication.class.getName(), CL, true);
		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(1);
		}
	}

	public static void stop(String[] args) {
		try {
			if (SRV != null) {
				LOG.info("Shutting down service ...");
				SRV.launch(args, MsApplication.class.getName(), CL, true);
				SRV = null;
				CL = null;
			} else {
				String pid = ManagementFactory.getRuntimeMXBean().getName();
				LOG.info("Different process, call JMX to stop the Micro App service ..."
						+ pid);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(1);
		}
	}

	public static void main(String[] args) {
//		BasicConfigurator.configure();
		if (args != null) {
			if (Arrays.stream(args).anyMatch(c -> c.equalsIgnoreCase("stop"))) {
				LOG.info("Stopping Micro App service ...");
				MicroService.stop(args);
			} else if (Arrays.stream(args).anyMatch(c -> c.equalsIgnoreCase("start"))) {
				LOG.info("Starting Micro App service ...");
				MicroService.start(args);
			}
		}
	}

}
