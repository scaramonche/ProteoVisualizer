package dk.ku.cpr.proteoVisualizer.internal.utils;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.io.IOException;

public abstract class IconUtils {
	
	// stringApp Icon
	public static final String STRING_ICON = "\uE903";
	// stringApp Icon layers
	public static final String STRING_ICON_LAYER_1 = "\uE904";
	public static final String STRING_ICON_LAYER_2 = "\uE905";
	public static final String STRING_ICON_LAYER_3 = "\uE906";
	public static final String STRING_ICON_LAYER_4 = "\uE907";
	// Search Icons -- extra layers
	public static final String STRING_LAYER = "\uE90C";
	
	public static final String[] LAYERED_STRING_ICON = new String[] { STRING_ICON_LAYER_1, STRING_ICON_LAYER_2, STRING_ICON_LAYER_3 };
	public static final Color[] STRING_COLORS = new Color[] { new Color(163, 172, 216), Color.WHITE, Color.BLACK, Color.WHITE, Color.BLACK };
	
	public static final String[] STRING_LAYERS = new String[] { STRING_ICON_LAYER_1, STRING_ICON_LAYER_2, STRING_ICON_LAYER_3, STRING_ICON_LAYER_4 };
	
	private static Font iconFont;

	static {
		try {
			iconFont = Font.createFont(Font.TRUETYPE_FONT, IconUtils.class.getResourceAsStream("/fonts/string.ttf"));
		} catch (FontFormatException e) {
			throw new RuntimeException();
		} catch (IOException e) {
			throw new RuntimeException();
		}
	}
	
	public static Font getIconFont(float size) {
		return iconFont.deriveFont(size);
	}

	private IconUtils() {
		// ...
	}
}
