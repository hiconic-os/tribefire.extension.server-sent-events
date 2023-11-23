// ============================================================================
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2022
// 
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with this library; See http://www.gnu.org/licenses/.
// ============================================================================
package tribefire.extension.sse.test.async;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.CharBuffer;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Collectors;

import org.apache.http.HttpEntity;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.nio.IOControl;

import com.braintribe.utils.lcd.StringTools;

public class SseEntity extends AbstractHttpEntity {

	private final BlockingQueue<Event> events = new ArrayBlockingQueue<>(100);
	private StringBuilder currentEvent = new StringBuilder();
	private int newLineCount = 0;
	private String lastEventId;
	private final HttpEntity original;

	public SseEntity(HttpEntity original) {
		this.original = original;
	}

	public void pushBuffer(CharBuffer buf, IOControl ioctrl) {
		while (buf.hasRemaining()) {
			processChar(buf.get());
		}
	}

	private void processChar(char nextChar) {
		if (nextChar == '\n') {
			newLineCount++;
		} else {
			newLineCount = 0;
		}
		if (newLineCount > 1) {
			processCurrentEvent();
			currentEvent = new StringBuilder();
		} else {
			currentEvent.append(nextChar);
		}
	}

	// Parse raw data for each event to create processed event object
	// Parsing specification - https://www.w3.org/TR/eventsource/#parsing-an-event-stream
	private void processCurrentEvent() {
		String rawEvent = currentEvent.toString();
		String id = "";
		String event = "";
		int retry = 0;
		StringBuilder data = new StringBuilder();
		List<String> lines = StringTools.getLines(rawEvent);
		for (String[] lineTokens : lines.stream().map(s -> s.split(":", 2)).collect(Collectors.toList())) {
			switch (lineTokens[0]) {
				case "id":
					id = lineTokens[1].trim();
					break;
				case "event":
					event = lineTokens[1].trim();
					break;
				case "retry":
					retry = Integer.parseInt(lineTokens[1].trim());
					break;
				case "data":
					data.append(lineTokens[1].trim());
					break;
			}
		}
		events.offer(new Event(id, event, data.toString(), retry));
		currentEvent = new StringBuilder();
		newLineCount = 0;
		lastEventId = id;
	}

	public BlockingQueue<Event> getEvents() {
		return events;
	}

	public boolean hasMoreEvents() {
		return events.size() > 0;
	}

	public String getLastEventId() {
		return lastEventId;
	}

	@Override
	public boolean isRepeatable() {
		return original.isRepeatable();
	}

	@Override
	public long getContentLength() {
		return original.getContentLength();
	}

	@Override
	public InputStream getContent() throws IOException, UnsupportedOperationException {
		return original.getContent();
	}

	@Override
	public void writeTo(OutputStream outStream) throws IOException {
		original.writeTo(outStream);
	}

	@Override
	public boolean isStreaming() {
		return original.isStreaming();
	}

}
