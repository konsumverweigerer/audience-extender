package models;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Dataset extends AbstractMap<String, Object> implements
		Map<String, Object> {
	private String name = "dataset";
	private String type;
	private Iterable<Map<String, String>> values;
	private Iterable<String> labels;

	@Override
	public Set<Entry<String, Object>> entrySet() {
		final Set<Entry<String, Object>> entries = new HashSet<Entry<String, Object>>();
		for (final String key : Arrays.asList("name", "type", "values",
				"labels")) {
			entries.add(new Entry<String, Object>() {
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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Iterable<Map<String, String>> getValues() {
		return values;
	}

	public void setValues(Iterable<Map<String, String>> values) {
		this.values = values;
	}

	public Iterable<String> getLabels() {
		return labels;
	}

	public void setLabels(Iterable<String> labels) {
		this.labels = labels;
	}

	public Object entry(String key) {
		if ("name".equals(key)) {
			return this.name;
		} else if ("type".equals(key)) {
			return this.type;
		} else if ("values".equals(key)) {
			return this.values;
		} else if ("labels".equals(key)) {
			return this.labels;
		}
		return null;
	}
}
