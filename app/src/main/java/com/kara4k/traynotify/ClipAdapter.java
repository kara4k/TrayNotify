package com.kara4k.traynotify;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ClipAdapter extends RecyclerView.Adapter<ClipAdapter.ClipHolder> {

    private static ClipAdapter clipAdapter;

    private List<Clip> notes;
    private Context context;
    private ClipboardManager cm;

    private SparseBooleanArray selectedItems = new SparseBooleanArray();
    private boolean select;
    private SelectionMode selectionMode;
    private int selectedCount = 0;

    private ClipAdapter() {
    }

    public static ClipAdapter getInstance() {
        if (clipAdapter == null) {
            clipAdapter = new ClipAdapter();
        }
        return clipAdapter;
    }

    public void setSelect(boolean select) {
        this.select = select;
    }

    public void setSelectionMode(SelectionMode selectionMode) {
        this.selectionMode = selectionMode;
    }

    public void setNotes(List<Clip> notes) {
        this.notes = notes;
    }

    @Override
    public ClipHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.quick_item, parent, false);
        context = parent.getContext();
        cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        return new ClipHolder(v);
    }

    @Override
    public void onBindViewHolder(ClipHolder holder, int i) {
        holder.title.setVisibility(View.GONE);

        holder.numid.setVisibility(View.GONE);
        holder.text.setText(notes.get(i).getText());

        setDateTime(holder, i);


        IfInClipNowIcon(holder, i);


        holder.itemView.setSelected(selectedItems.get(i, false));

        ifSelectionMode(holder, i);


    }

    private void ifSelectionMode(ClipHolder holder, int i) {
        if (select) {
            holder.checkBox.setVisibility(View.VISIBLE);
            holder.checkBox.setChecked(selectedItems.get(i, false));
            holder.inClip.setVisibility(View.GONE);
            holder.dateLayoutListener = 0;
        } else {
            holder.checkBox.setVisibility(View.GONE);
            holder.inClip.setVisibility(View.VISIBLE);
            holder.dateLayoutListener = 1;
        }
    }

    private void IfInClipNowIcon(ClipHolder holder, int i) {

        try {
            if (ifNotNullClipNow(cm)) {
                checkIfInClipNow(holder, i);
            } else {
                setNotInClipIcon(holder, i);
            }
        } catch (Exception e) {
            setNotInClipIcon(holder, i);
        }
    }

    public static boolean ifNotNullClipNow(ClipboardManager cm) {
        if (cm.getPrimaryClip() != null) {
            if (cm.getPrimaryClip().getItemAt(0) != null) {
                if (cm.getPrimaryClip().getItemAt(0).toString() != null) {
                    return true;
                }
            }
        }
        return false;
    }

    private void checkIfInClipNow(ClipHolder holder, int i) {

        String textInClipNow = cm.getPrimaryClip().getItemAt(0).getText().toString();
        if ((textInClipNow.equals(notes.get(i).getText()))) {
            setInClipIcon(holder, i);
        } else {
            setNotInClipIcon(holder, i);
        }
    }

    private void setInClipIcon(ClipHolder holder, int i) {
        holder.inClip.setImageResource(R.drawable.ic_content_copy_red_24dp);
        notes.get(i).setChecked(1);
    }

    private void setNotInClipIcon(ClipHolder holder, int i) {
        holder.inClip.setImageResource(R.drawable.ic_content_copy_grey_24dp);
        notes.get(i).setChecked(0);
    }

    private void setDateTime(ClipHolder holder, int i) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd.MM.yyyy");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        String date = dateFormat.format(new Date(notes.get(i).getDate()));
        date = date.substring(0, 1).toUpperCase() + date.substring(1);
        holder.date.setText(date);
        holder.time.setText(timeFormat.format(new Date(notes.get(i).getDate())));
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    public void startSelection() {
        select = true;
        refreshAll();
    }

    private void refreshAll() {
        for (int i = 0; i < notes.size(); i++) {
            notifyItemChanged(i);
        }
    }

    public void selectAll() {
        for (int i = 0; i < notes.size(); i++) {
            selectedItems.put(i, true);
        }
        refreshAll();
        selectedCount = notes.size();
        selectionMode.selectedItemsCount(notes.size());
    }

    public void deleteSelected() {
        try {
            ArrayList<Integer> list = getSelectedId();

            removeFromList(list);

            removeFromDB(list);


        } catch (Exception e) {
        }
    }

    private void removeFromList(ArrayList<Integer> list) {
        for (int i = 0; i < notes.size(); i++) {
            for (int x : list) {
                if (notes.get(i).getNumId() == x) {
                    notes.remove(i);
//                    notifyItemRemoved(i);
                }
            }
        }
        notifyDataSetChanged();
    }


    private void removeFromDB(ArrayList<Integer> idList) {
        try {
            DBClip db = new DBClip(context);
            db.open();
            for (int x : idList) {
                db.removeClip(x);
            }
            db.close();
        } catch (Exception e) {
        }
    }

    @NonNull
    private ArrayList<Integer> getSelectedId() {
        ArrayList<Integer> list = new ArrayList<>();
        for (int i = 0; i < selectedItems.size(); i++) {
            if (selectedItems.valueAt(i)) {
                Clip note = notes.get(selectedItems.keyAt(i));
                list.add(note.getNumId());
            }
        }
        return list;
    }

    public void endSelectionMode() {
        select = false;
        selectedItems = new SparseBooleanArray();
        selectedCount = 0;
        refreshAll();

    }

    public void clearListAll() {
        try {
            DBClip db = new DBClip(context);
            db.open();
            db.clearDB();
            db.close();
            notes.clear();
            notifyDataSetChanged();
        } catch (Exception e) {
        }
    }

    public class ClipHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        private final TextView title;
        private final TextView text;
        private final TextView date;
        private final TextView time;
        private final TextView numid;
        private final ImageView inClip;
        private final CheckBox checkBox;
        private final LinearLayout dateLayout;
        public int dateLayoutListener;

        public ClipHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.name);
            text = (TextView) itemView.findViewById(R.id.text);
            date = (TextView) itemView.findViewById(R.id.date);
            time = (TextView) itemView.findViewById(R.id.day_of_week);
            numid = (TextView) itemView.findViewById(R.id.numid);
            inClip = (ImageView) itemView.findViewById(R.id.isShown);
            checkBox = (CheckBox) itemView.findViewById(R.id.checkbox_select);
            dateLayout = (LinearLayout) itemView.findViewById(R.id.date_layout);

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);

            dateLayout.setOnClickListener(this);

            checkBox.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.quick_holder:
                    if (select) {
                        toggleSelectItem();
                    } else {
                        goToQuickAcivity();
                    }
                    break;
                case R.id.date_layout:
                    if (dateLayoutListener == 1) {
                        putInPrimaryClipboard();
                    } else {
                        toggleSelectItem();
                    }
                    break;
                case R.id.checkbox_select:
                    if (select) {
                        toggleSelectItem();
                    }
                    break;
            }

        }

        private void toggleSelectItem() {
            selectionModeClick(itemView);
            checkForEndSelect();
        }

        private void selectionModeClick(View itemView) {
            if (!selectedItems.get(getAdapterPosition(), false)) {
                selectedItems.put(getAdapterPosition(), true);
                itemView.setSelected(true);
                checkBox.setChecked(true);
                selectedCount++;
                selectionMode.selectedItemsCount(selectedCount);
            } else {
                selectedItems.put(getAdapterPosition(), false);
                itemView.setSelected(false);
                checkBox.setChecked(false);
                selectedCount--;
                selectionMode.selectedItemsCount(selectedCount);
            }
        }

        private void checkForEndSelect() {
            if (selectedCount == 0) {
                selectionMode.startSelection(1);
            }
        }

        private void putInPrimaryClipboard() {
            inClip.setImageResource(R.drawable.ic_content_copy_red_24dp);
            putToClipboard();
            setInClipNowIcon();
        }

        private void goToQuickAcivity() {
            Intent quick = new Intent(context, QuickNote.class);
            quick.putExtra(Intent.EXTRA_TEXT, notes.get(getAdapterPosition()).getText());
            context.startActivity(quick);
        }

        private void setInClipNowIcon() {
            for (Clip x : notes) {
                if (x.getChecked() != 0) {
                    x.setChecked(0);
                    notifyItemChanged(notes.indexOf(x));
                }
            }
            notes.get(getAdapterPosition()).setChecked(1);
        }

        private void changeDatabase() {
            DBClip db = new DBClip(context);
            db.clearAndCheckSingle(notes.get(getAdapterPosition()).getNumId(), 1);
        }

        private void putToClipboard() {

            ClipData clip = ClipData.newPlainText("", notes.get(getAdapterPosition()).getText());
            cm.setPrimaryClip(clip);
        }

        @Override
        public boolean onLongClick(View view) {
            if (selectionMode != null) {
                selectionMode.startSelection(1);
                if (select) {
                    itemView.setSelected(true);
                    selectedItems.put(getAdapterPosition(), true);
                    selectedCount++;
                    notifyItemChanged(getAdapterPosition());
                }
            }
            return true;
        }
    }
}
