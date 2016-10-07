package sk.turn.separatedlistadapter;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;

import java.util.LinkedHashMap;
import java.util.Map;

public class SeparatedListAdapter extends BaseAdapter {

	private static final int TYPE_SECTION_HEADER = 0;

	private final Map<String, BaseAdapter> sections;
	private final ArrayAdapter<String> headers;
	private final DataSetObserver dataSetObserver;

	public SeparatedListAdapter(Context context, int headerLayoutResource) {
		this(context, headerLayoutResource, android.R.id.text1);
	}

	public SeparatedListAdapter(Context context, int headerLayoutResource, int textViewResourceId) {
		sections = new LinkedHashMap<>();
		headers = new ArrayAdapter<>(context, headerLayoutResource, textViewResourceId);
		dataSetObserver = new DataSetObserver() {
			@Override
			public void onChanged() {
				super.onChanged();
				notifyDataSetChanged();
			}
			@Override
			public void onInvalidated() {
				super.onInvalidated();
				notifyDataSetInvalidated();
			}
		};
	}

	public void addSection(String section, BaseAdapter adapter) {
		this.sections.put(section, adapter);
		this.headers.add(section);
		adapter.registerDataSetObserver(dataSetObserver);
	}

	@Override
	public Object getItem(int position) {
		for (String section : this.sections.keySet()) {
			BaseAdapter adapter = sections.get(section);
			int size = getAdapterCount(adapter);

			// check if position inside this section
			if (position == 0 && size > 0) return section;
			if (position < size) return adapter.getItem(position - 1);

			// otherwise jump into next section
			position -= size;
		}
		return null;
	}

	@Override
	public int getCount() {
		// total together all sections, plus one for each section header
		int total = 0;
		for (BaseAdapter adapter : this.sections.values()) {
			total += getAdapterCount(adapter);
		}
		return total;
	}

	@Override
	public int getViewTypeCount() {
		// assume that headers count as one, then total all sections
		int total = 1;
		for (BaseAdapter adapter : this.sections.values()) {
			total += adapter.getViewTypeCount();
		}
		return total;
	}

	@Override
	public int getItemViewType(int position) {
		int type = 1;
		for (String section : this.sections.keySet()) {
			BaseAdapter adapter = sections.get(section);
			int size = getAdapterCount(adapter);

			// check if position inside this section
			if (position == 0 && size > 0) return TYPE_SECTION_HEADER;
			if (position < size) return type + adapter.getItemViewType(position - 1);

			// otherwise jump into next section
			position -= size;
			type += adapter.getViewTypeCount();
		}
		return -1;
	}

	@Override
	public boolean areAllItemsEnabled() {
		// header views are disabled
		return false;
	}

	@Override
	public boolean isEnabled(int position) {
		for (String section : this.sections.keySet()) {
			BaseAdapter adapter = sections.get(section);
			int size = getAdapterCount(adapter);

			// check if position inside this section
			if (position == 0 && size > 0) return false;
			if (position < size) return adapter.isEnabled(position - 1);

			// otherwise jump into next section
			position -= size;
		}
		return false;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		int sectionNum = 0;
		for (String section : this.sections.keySet()) {
			BaseAdapter adapter = sections.get(section);
			int size = getAdapterCount(adapter);

			// check if position inside this section
			if (position == 0 && size > 0) return headers.getView(sectionNum, convertView, parent);
			if (position < size) return adapter.getView(position - 1, convertView, parent);

			// otherwise jump into next section
			position -= size;
			sectionNum++;
		}
		return null;
	}

	@Override
	public long getItemId(int position) {
		for (String section : this.sections.keySet()) {
			BaseAdapter adapter = sections.get(section);
			int size = getAdapterCount(adapter);

			// check if position inside this section
			if (position == 0 && size > 0) return 0;
			if (position < size) return adapter.getItemId(position - 1);

			// otherwise jump into next section
			position -= size;
		}
		return -1;
	}

	@Override
	public boolean hasStableIds() {
		// returning the same item id for header views
		return false;
	}

	private static int getAdapterCount(BaseAdapter adapter) {
		return adapter.isEmpty() ? 0 : adapter.getCount() + 1;
	}

}
