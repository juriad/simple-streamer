package cz.artique.simpleStreamer.utils;

import java.net.InetAddress;

import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.OptionDef;
import org.kohsuke.args4j.spi.DelimitedOptionHandler;
import org.kohsuke.args4j.spi.InetAddressOptionHandler;
import org.kohsuke.args4j.spi.Setter;

public class CommaDelimitedInetAddressListOptionHandler extends
		DelimitedOptionHandler<InetAddress> {

	public CommaDelimitedInetAddressListOptionHandler(CmdLineParser parser,
			OptionDef option, Setter<? super InetAddress> setter) {
		super(parser, option, setter, ",", new InetAddressOptionHandler(parser,
				option, setter));
	}

}