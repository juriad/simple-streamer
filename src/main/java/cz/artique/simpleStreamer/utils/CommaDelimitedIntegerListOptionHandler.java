package cz.artique.simpleStreamer.utils;

import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.OptionDef;
import org.kohsuke.args4j.spi.DelimitedOptionHandler;
import org.kohsuke.args4j.spi.IntOptionHandler;
import org.kohsuke.args4j.spi.Setter;

public class CommaDelimitedIntegerListOptionHandler extends
		DelimitedOptionHandler<Integer> {

	public CommaDelimitedIntegerListOptionHandler(CmdLineParser parser,
			OptionDef option, Setter<? super Integer> setter) {
		super(parser, option, setter, ",", new IntOptionHandler(parser, option,
				setter));
	}

}