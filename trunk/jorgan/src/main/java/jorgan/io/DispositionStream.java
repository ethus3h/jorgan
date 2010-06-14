/*
 * jOrgan - Java Virtual Organ
 * Copyright (C) 2003 Sven Meier
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package jorgan.io;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

import jorgan.disposition.Element;
import jorgan.disposition.Organ;
import jorgan.io.disposition.ClassMapper;
import jorgan.io.disposition.Conversion;
import jorgan.io.disposition.FormatException;
import jorgan.io.disposition.OrganConverter;
import jorgan.io.disposition.ReferenceConverter;
import jorgan.io.xstream.BooleanArrayConverter;
import jorgan.io.xstream.DoubleArrayConverter;
import jorgan.io.xstream.FloatArrayConverter;
import jorgan.io.xstream.IntArrayConverter;
import jorgan.util.IOUtils;
import bias.Configuration;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.core.TreeMarshallingStrategy;
import com.thoughtworks.xstream.io.xml.AbstractXmlDriver;
import com.thoughtworks.xstream.io.xml.XmlFriendlyReplacer;
import com.thoughtworks.xstream.io.xml.XppDriver;
import com.thoughtworks.xstream.mapper.MapperWrapper;

/**
 * A {@link jorgan.disposition.Organ} streamer.
 */
public class DispositionStream {

	private static final String ENCODING = "UTF-8";

	private static Configuration config = Configuration.getRoot().get(
			DispositionStream.class);

	private XStream xstream = new XStream(createDriver()) {
		@Override
		protected MapperWrapper wrapMapper(MapperWrapper next) {
			return new ClassMapper(next);
		}
	};

	public DispositionStream() {
		TreeMarshallingStrategy strategy = new TreeMarshallingStrategy();
		xstream.setMarshallingStrategy(strategy);

		// never write class attribute
		xstream.aliasSystemAttribute(null, "class");

		// element identification
		xstream.useAttributeFor(Element.class, "id");

		// organ -> element relationship
		xstream.registerConverter(new OrganConverter(xstream));

		// reference -> element relationship
		xstream.registerConverter(new ReferenceConverter(xstream));

		// primitives
		xstream.registerConverter(new BooleanArrayConverter());
		xstream.registerConverter(new IntArrayConverter());
		xstream.registerConverter(new FloatArrayConverter());
		xstream.registerConverter(new DoubleArrayConverter());

		config.read(this);
	}

	/**
	 * 
	 * @param file
	 *            the file to read from
	 * @return the read organ
	 * @throws IOException
	 * @throws Exception
	 */
	public Organ read(File file) throws IOException {
		InputStream input = new Conversion().convert(new FileInputStream(file));

		try {
			return read(input);
		} finally {
			IOUtils.closeQuietly(input);
		}
	}

	public Organ read(InputStream in) throws IOException, FormatException {
		Reader reader = new InputStreamReader(new BufferedInputStream(in),
				ENCODING);

		try {
			return (Organ) xstream.fromXML(reader);
		} catch (Exception ex) {
			throw findFormatException(ex);
		}
	}

	private FormatException findFormatException(Throwable ex)
			throws FormatException {
		if (ex instanceof FormatException) {
			return (FormatException) ex;
		}

		Throwable cause = ex.getCause();
		if (cause == null || cause == ex) {
			return new FormatException(ex);
		}

		return findFormatException(cause);
	}

	public void write(Organ organ, File file) throws IOException {

		File temp = new File(file.getAbsoluteFile().getParentFile(), "."
				+ file.getName());

		FileOutputStream output = new FileOutputStream(temp);
		try {
			write(organ, output);
		} finally {
			IOUtils.closeQuietly(output);
		}

		if (!file.delete() || !temp.renameTo(file)) {
			throw new IOException("unable to rename");
		}
	}

	public void write(Organ organ, OutputStream out) throws IOException {

		Writer writer = new OutputStreamWriter(new BufferedOutputStream(out),
				ENCODING);
		writer
				.write("<?xml version=\"1.0\" encoding=\"" + ENCODING
						+ "\" ?>\n");
		xstream.toXML(organ, writer);
	}

	private AbstractXmlDriver createDriver() {
		return new XppDriver(createReplacer());
	}

	private XmlFriendlyReplacer createReplacer() {
		// replaced "$" and "_"
		return new XmlFriendlyReplacer("-", "_");
	}
}