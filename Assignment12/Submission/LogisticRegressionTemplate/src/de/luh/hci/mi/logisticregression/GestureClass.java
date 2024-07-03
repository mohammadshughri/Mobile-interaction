package de.luh.hci.mi.logisticregression;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;

public class GestureClass {
	/** Each template is resampled to this number of points. */
	public final static int SAMPLE_POINTS_COUNT = 64;
	
	public Vector<Template> templates = new Vector<Template>();
	public String name;
	public int gestureId;
	
	
	/**
	 * Read templates from file. All templates in this file are assumed to be 
	 * examples of the same gesture (hence they have the same gesture ID).
	 * 
	 * @param filename
	 * @param name
	 * @param gestureId
	 * @throws IOException
	 */
	public GestureClass(String filename, String name, int gestureId) throws IOException {
		this.name = name;
		this.gestureId = gestureId;
		
		BufferedReader in = new BufferedReader(new FileReader(filename));
		in.readLine(); // skip header: gestureIndex\nsampleIndex\ntimestamp\tx\ty\tz
		String s;
		int templateIndex = -1;
		Template currentTemplate = null;
		while ((s = in.readLine()) != null) {
			String[] fs = s.split("\t");
			int ti = Integer.parseInt(fs[0]);
			// int si = Integer.parseInt(fs[1]);
			// long t = Long.parseLong(fs[2]);
			double x = Double.parseDouble(fs[3]);
			double y = Double.parseDouble(fs[4]);
			double z = Double.parseDouble(fs[5]);
			if (currentTemplate == null || ti != templateIndex) {
				currentTemplate = new Template(gestureId);
				templates.add(currentTemplate);
				templateIndex = ti;
			}
			currentTemplate.add(x, y, z);
		}
	}
	
	
	/**
	 * Resample all templates in the vector.
	 * @param ts vector of templates
	 * @return resampled templates
	 */
	public void resample() {
		Vector<Template> tsr = new Vector<Template>();
		for (Template t : templates) {
			Template tr = t.resample(SAMPLE_POINTS_COUNT);
			tsr.add(tr);
		}
		templates = tsr;
	}
}
