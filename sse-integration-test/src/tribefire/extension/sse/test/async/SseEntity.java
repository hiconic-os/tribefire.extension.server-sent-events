// ============================================================================
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2022
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
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
