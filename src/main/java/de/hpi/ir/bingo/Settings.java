package de.hpi.ir.bingo;

public final class Settings {
	/** executes phrase queries for each pair of consecutive term queries **/
	public static final boolean HIGH_QUALITY = true;

	public static final String INDEX_PATH = "k:/";
//	public static final String INDEX_PATH = "h:/index2/";

	/** {@code DATA_FILE} is only used for import **/
	//public static final String DATA_FILE = "res/testData.xml";
	//public static final String DATA_FILE = "compressed_patents/ipg150106.fixed.zip";
	public static final String DATA_FILE = "k:/data/patentData.zip";

	public static final boolean CREATE_INDEX = false;
	public static final boolean COMPRESS = CREATE_INDEX;

	public static final boolean READ_COMPRESSED = true;
	public static final boolean USE_CACHING = false;

	// number of tokens to be added
	public static final int PRF_EXTENSION_SIZE = 3;
}
