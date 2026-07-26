package com.bretzelfresser.dinosexpansion.client.gui;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class FormattingUtils {


    public static final DecimalFormat DEFAULT_FLOAT_FORMAT = new DecimalFormat("0.0", DecimalFormatSymbols.getInstance(Locale.US));
}
