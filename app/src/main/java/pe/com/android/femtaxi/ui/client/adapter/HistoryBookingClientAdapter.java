package pe.com.android.femtaxi.ui.client.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import pe.com.android.femtaxi.R;
import pe.com.android.femtaxi.databinding.RawHistoryBookingClientBinding;
import pe.com.android.femtaxi.models.Driver;
import pe.com.android.femtaxi.models.HistoryBooking;
import pe.com.android.femtaxi.providers.DriverProvider;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;

public class HistoryBookingClientAdapter extends RecyclerView.Adapter<HistoryBookingClientAdapter.bodyHolder> {

    private ArrayList<HistoryBooking> mDataList;
    private DriverProvider driverProvider;
    private Context context;
    private OnItemClickListener mListener;

    public HistoryBookingClientAdapter(Context context) {
        this.context = context;
        this.mDataList = new ArrayList<>();
        this.driverProvider = new DriverProvider();
    }

    @NonNull
    @Override
    public bodyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RawHistoryBookingClientBinding binding = RawHistoryBookingClientBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new bodyHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull bodyHolder holder, int position) {
        final bodyHolder viewHolder = (bodyHolder) holder;
        final HistoryBooking historyBooking = getItem(position);
        if (historyBooking != null) {
            holder.bind(historyBooking);
        }
    }

    @Override
    public int getItemCount() {
        if (mDataList != null && mDataList.size() > 0)
            return mDataList.size();
        else
            return 0;
    }

    public HistoryBooking getItem(int position) {
        return mDataList.get(position);
    }

    public void setData(ArrayList<HistoryBooking> tmpListHistory) {
        if (mDataList == null)
            mDataList = new ArrayList<>();
        mDataList.clear();
        mDataList.addAll(tmpListHistory);
        notifyDataSetChanged();
    }

    public void setListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public class bodyHolder extends RecyclerView.ViewHolder {
        RawHistoryBookingClientBinding binding;

        public bodyHolder(RawHistoryBookingClientBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(HistoryBooking item) {

            driverProvider.getDataUser(item.getIdDriver())
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (documentSnapshot.exists()) {
                                Drawable placeholder = context.getResources().getDrawable(R.drawable.ic_login_user);
                                Driver driver = documentSnapshot.toObject(Driver.class);
                                Glide.with(context)
                                        .load(driver.getPhoto())
                                        .placeholder(placeholder)
                                        .error(placeholder)
                                        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                                        .into(binding.imgUser);

                                binding.txtName.setText(driver.getName());
                            }
                        }
                    });

            binding.txtOrigin.setText(item.getOrigin());
            binding.txtDestino.setText(item.getDestination());
            binding.txtCalification.setText(String.valueOf(item.getCalificationClient()));

            binding.containerItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mListener != null) {
                        mListener.onClickListener(view, item, getAdapterPosition(), false);
                    }
                }
            });
        }
    }

    public interface OnItemClickListener {
        boolean onClickListener(View view, HistoryBooking item, int position, boolean longPress);
    }
}
