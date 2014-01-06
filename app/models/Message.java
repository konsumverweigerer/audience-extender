package models;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Message extends AbstractMap<String, Object> implements
		Map<String, Object> {
	private String title;
	private String content;
	private String priority = "info";

	@Override
	public Set<java.util.Map.Entry<String, Object>> entrySet() {
		final Set<java.util.Map.Entry<String, Object>> entries = new HashSet<java.util.Map.Entry<String, Object>>();
		for (final String key : Arrays.asList("title", "content", "priority")) {
			entries.add(new java.util.Map.Entry<String, Object>() {
				@Override
				public String getKey() {
					return key;
				}

				@Override
				public Object getValue() {
					return entry(key);
				}

				@Override
				public Object setValue(Object value) {
					return null;
				}
			});
		}
		return entries;
	}

	public Message(String title, String content, String priority) {
		this.title = title;
		this.content = content;
		this.priority = priority;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getPriority() {
		return priority;
	}

	public void setPriority(String priority) {
		this.priority = priority;
	}

	public Object entry(String key) {
		if ("title".equals(key)) {
			return this.title;
		} else if ("priority".equals(key)) {
			return this.priority;
		} else if ("content".equals(key)) {
			return this.content;
		}
		return null;
	}
}
