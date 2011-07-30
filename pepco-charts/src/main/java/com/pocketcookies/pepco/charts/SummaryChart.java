package com.pocketcookies.pepco.charts;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.TreeMap;

import javax.imageio.ImageIO;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StackedXYAreaRenderer2;
import org.jfree.chart.renderer.xy.XYAreaRenderer;
import org.jfree.data.xy.DefaultTableXYDataset;
import org.jfree.data.xy.XYSeries;

/**
 * Reads CSV data from the file specified by the first command line argument
 * (use "-" as the first command line argument to read from stdin). Generates a
 * stacked area chart and writes it to the second command line argument (use "-"
 * to write to stdout).
 * 
 * The expected format is Area Name,Date string (yyyy-mm-dd hh:mm:ss),Customers
 * out
 * 
 * The input expects a header (which is ignored).
 * 
 * @author jack
 * 
 */
public class SummaryChart {
	public static void main(final String[] args) throws IOException,
			NumberFormatException, ParseException {
		System.out.println("Here");
		final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		final BufferedReader in = new BufferedReader(new InputStreamReader(
				(InputStream) (args[0].equals("-") ? System.in
						: new FileInputStream(new File(args[0])))));
		final PrintStream out = args[1].equals("-") ? System.out
				: new PrintStream(new File(args[1]));

		String line;
		in.readLine();// Skip CSV header.
		final Map<String, XYSeries> data = new TreeMap<String, XYSeries>();
		while ((line = in.readLine()) != null) {
			final String[] splitLine = line.split(",");
			if (!data.containsKey(splitLine[0]))
				data.put(splitLine[0], new XYSeries(splitLine[0], true, false));
			data.get(splitLine[0]).add(sdf.parse(splitLine[1]).getTime(),
					Integer.parseInt(splitLine[2]));
		}
		final DefaultTableXYDataset dataset = new DefaultTableXYDataset();
		for (final XYSeries series : data.values())
			dataset.addSeries(series);

		final StackedXYAreaRenderer2 renderer = new StackedXYAreaRenderer2(
				new StandardXYToolTipGenerator(
						StandardXYToolTipGenerator.DEFAULT_TOOL_TIP_FORMAT,
						sdf, NumberFormat.getInstance()), null);
		renderer.setSeriesPaint(0, new Color(25, 150, 33));
		renderer.setSeriesPaint(1, new Color(100, 100, 100));
		renderer.setSeriesPaint(2, new Color(0, 92, 171));
		final DateAxis xAxis = new DateAxis(null);
		xAxis.setLowerMargin(0);
		xAxis.setUpperMargin(0);
		final NumberAxis yAxis = new NumberAxis();
		yAxis.setAutoRangeIncludesZero(true);
		final XYPlot plot = new XYPlot(dataset, xAxis, yAxis, renderer);
		yAxis.configure();
		final JFreeChart chart = new JFreeChart(plot);
		chart.setBackgroundPaint(java.awt.Color.WHITE);
		ImageIO.write(chart.createBufferedImage(1000, 500), "png", out);
	}
}
