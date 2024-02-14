/*
 * Copyright (C) 2021 The Project Lombok Authors.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package pers.xanadu.annotation.processor.shade.lombok.spi;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.tools.Diagnostic.Kind;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

class SpiProcessorPersistence {
	private final String name;
	private final String path;
	final Filer filer;
	private final Messager logger;
	
	SpiProcessorPersistence(String name, Filer filer, Messager logger) {
		this.name = name;
		this.logger = logger;
		this.path = SpiProcessor.getRootPathOfServiceFiles();
		this.filer = filer;
	}
	
	static CharSequence readFilerResource(FileObject resource, Messager logger, String pathName) {
		try {
			// Eclipse can't handle getCharContent, so we must use a reader...
			return tryWithReader(resource);
		} catch (FileNotFoundException e) {
			return null;
		} catch (IOException e) {
			if (
				e.getClass().getName().equals("org.eclipse.core.internal.resources.ResourceException") &&
				e.getMessage() != null &&
				e.getMessage().endsWith("does not exist.")) {
				
				return null;
			}
			
			logger.printMessage(Kind.ERROR, SpiProcessor.toErrorMsg(e, pathName));
			return null;
		} catch (Exception other) {
			// otherwise, probably javac: Some versions don't support the `openReader` method.
			try {
				return resource.getCharContent(true);
			} catch (FileNotFoundException e) {
				return null;
			} catch (IOException e) {
				logger.printMessage(Kind.ERROR, SpiProcessor.toErrorMsg(e, pathName));
				return null;
			}
		}
	}
	
	private static CharSequence tryWithReader(FileObject resource) throws IOException {
		StringBuilder sb = new StringBuilder();
		Reader raw = resource.openReader(true);
		try {
			BufferedReader in = new BufferedReader(raw);
			for (String line = in.readLine(); line != null; line = in.readLine()) sb.append(line).append('\n');
			return sb;
		} finally {
			if (raw != null) raw.close();
		}
	}
	
	Collection<String> tryFind() {
		File dir = determineOutputLocation();
		if (dir == null || !dir.isDirectory()) return Collections.emptyList();
		List<String> out = new ArrayList<String>();
		for (File p : dir.listFiles()) {
			if (!p.isFile()) continue;
			out.add(p.getName());
		}
		return out;
	}
	
	private File determineOutputLocation() {
		FileObject resource;
		try {
			resource = filer.createResource(StandardLocation.CLASS_OUTPUT, "META-INF", "locator.tmp");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			// Could happen
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			logger.printMessage(Kind.NOTE, "IOException while determining output location: " + e.getMessage());
			return null;
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			// Happens when the path is invalid. For instance absolute or relative to a path 
			// not part of the class output folder.
			//
			// Due to a bug in javac for Linux, this also occurs when no output path is specified 
			// for javac using the -d parameter.
			// See http://forums.sun.com/thread.jspa?threadID=5240999&tstart=45
			// and http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6647996
			
			return null;
		}
		
		URI uri = resource.toUri();
		return new File(new File(uri).getParentFile(), "services");
	}
	
	void write(String serviceName, String value) throws IOException {
		FileObject output = filer.createResource(StandardLocation.CLASS_OUTPUT, "", path + serviceName);
		Writer writer = output.openWriter();
		writer.write("# Generated by " + name + "\n");
		writer.write("# " + new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.US).format(new Date()) + "\n");
		writer.write(value);
		writer.close();
	}
}
