package models;

import java.math.BigDecimal;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import services.StatsHandler;

public class Dataset extends AbstractMap<String, Object> implements
		Map<String, Object> {
	private String name = "dataset";
	private String type, cls, timeframe;
	private Iterable<Map<String, BigDecimal>> values;
	private Map<Number, Number> table;
	private Iterable<String> labels;

	@Override
	public Set<Entry<String, Object>> entrySet() {
		final Set<Entry<String, Object>> entries = new HashSet<Entry<String, Object>>();
		for (final String key : Arrays.asList("name", "type", "values",
				"labels", "cls", "timeframe")) {
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

	public String getName() {
		return name;
	}

	public String getCls() {
		return cls;
	}

	public void setCls(String cls) {
		this.cls = cls;
	}

	public String getTimeframe() {
		return timeframe;
	}

	public void setTimeframe(String timeframe) {
		this.timeframe = timeframe;
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

	public Iterable<Map<String, BigDecimal>> getValues() {
		return values;
	}

	public void setValues(Iterable<Map<String, BigDecimal>> values) {
		this.values = values;
	}

	public Iterable<String> getLabels() {
		return labels;
	}

	public Iterable<Map<String, BigDecimal>> getContent() {
		if (this.table != null) {
			return StatsHandler.tableToIterable(this.table);
		} else if (this.values != null) {
			return this.values;
		}
		return Collections.emptyList();
	}

	public Map<Number, Number> getTable() {
		return table;
	}

	public void setTable(Map<Number, Number> table) {
		this.table = table;
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
			if (this.table != null) {
				return StatsHandler.tableToIterable(this.table);
			} else {
				return this.values;
			}
		} else if ("labels".equals(key)) {
			return this.labels;
		} else if ("cls".equals(key)) {
			return this.cls;
		} else if ("timeframe".equals(key)) {
			return this.timeframe;
		}
		return null;
	}
}
