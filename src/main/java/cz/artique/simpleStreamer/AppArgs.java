package cz.artique.simpleStreamer;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import cz.artique.simpleStreamer.utils.CommaDelimitedInetAddressListOptionHandler;
import cz.artique.simpleStreamer.utils.CommaDelimitedIntegerListOptionHandler;

public class AppArgs {

	@Option(name = "-help", aliases = { "-?" }, required = false, usage = "Prints command line usage.", help = true)
	private boolean help;

	@Option(name = "-verbose", aliases = { "-v" }, required = false, usage = "Prints information to console.")
	private boolean verbose;

	@Option(name = "-wait", aliases = { "-W" }, required = false, usage = "Wait for key to start.", hidden = true)
	private boolean wait;

	@Option(name = "-sport", aliases = { "-s" }, required = false, usage = "Port to listen on")
	private int serverPort = 6262;

	@Option(name = "-remote", aliases = { "-rh" }, required = false, usage = "List of remote hostnames", handler = CommaDelimitedInetAddressListOptionHandler.class)
	private List<InetAddress> remoteHosts;

	@Option(name = "-rport", aliases = { "-rp" }, required = false, usage = "List of remote ports", handler = CommaDelimitedIntegerListOptionHandler.class)
	private List<Integer> remotePorts;

	@Option(name = "-width", aliases = { "-w" }, required = false, usage = "Width of my images")
	private int width = 320;

	@Option(name = "-height", aliases = { "-h" }, required = false, usage = "Height of my images")
	private int height = 240;

	@Option(name = "-rate", aliases = { "-r" }, required = false, usage = "Framerate")
	private int rate = 100;

	@Option(name = "-dummy", aliases = { "-d" }, required = false, usage = "Use dummy webcam")
	private boolean dummy;

	public AppArgs(String... args) {
		parse(args);
	}

	public void parse(String... args) {
		CmdLineParser parser = new CmdLineParser(this);
		try {
			parser.parseArgument(args);
		} catch (CmdLineException e) {
			System.err.println(e.getMessage());
			System.err.print("java -jar SimpleStreamer.jar");
			parser.printSingleLineUsage(System.err);
			System.err.println();
			System.exit(1);
		}

		if (help) {
			parser.printUsage(System.err);
			System.exit(0);
		}

		if (isVerbose()) {
			LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
			Configuration conf = ctx.getConfiguration();
			conf.getLoggerConfig(LogManager.ROOT_LOGGER_NAME).setLevel(
					Level.INFO);
			ctx.updateLoggers(conf);
		}

		if (wait) {
			try {
				System.in.read();
			} catch (IOException e) {
			}
		}
	}

	public boolean isVerbose() {
		return verbose;
	}

	public int getServerPort() {
		return serverPort;
	}

	public List<InetAddress> getRemoteHosts() {
		if (remoteHosts == null) {
			return new ArrayList<InetAddress>();
		}
		return remoteHosts;
	}

	public List<Integer> getRemotePorts() {
		if (remotePorts == null) {
			return new ArrayList<Integer>();
		}
		return remotePorts;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public int getRate() {
		return rate;
	}

	public boolean isDummy() {
		return dummy;
	}

}
