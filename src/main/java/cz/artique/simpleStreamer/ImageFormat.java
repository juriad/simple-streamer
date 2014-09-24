package cz.artique.simpleStreamer;

public enum ImageFormat {

	RAW {
		@Override
		public String toString() {
			return "raw";
		}
	};

	public static ImageFormat fromString(String dir) {
		if (dir.equals("raw")) {
			return RAW;
		} else {
			throw new IllegalArgumentException("Illegal image format");
		}
	}
}
