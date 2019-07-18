package com.pchmn.materialchips.adapter;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.AbsoluteSizeSpan;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pchmn.materialchips.ChipView;
import com.pchmn.materialchips.ChipsInput;
import com.pchmn.materialchips.model.ChipInterface;
import com.pchmn.materialchips.util.ViewUtil;
import com.pchmn.materialchips.views.ChipsInputEditText;
import com.pchmn.materialchips.views.DetailedChipView;
import com.pchmn.materialchips.views.FilterableListView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class ChipsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{
	
	private static final String              TAG            = ChipsAdapter.class.toString();
	private static final int                 TYPE_EDIT_TEXT = 0;
	private static final int                 TYPE_ITEM      = 1;
	private final        Context             mContext;
	private final        ChipsInput          mChipsInput;
	private final        List<ChipInterface> mChipList      = new ArrayList<>();
	private              CharSequence        mHintLabel;
	private              float               mHintSize;
	private final        ChipsInputEditText  mEditText;
	private              EditTextViewHolder  m_editTextViewHolder;
	private final        RecyclerView        mRecycler;
	
	public ChipsAdapter(Context context, ChipsInput chipsInput, RecyclerView recycler)
	{
		mContext = context;
		mChipsInput = chipsInput;
		mRecycler = recycler;
		mHintLabel = mChipsInput.getHint();
		mHintSize = mChipsInput.getHintSize();
		mEditText = mChipsInput.generateEditText();
		initEditText();
	}
	
	private class ItemViewHolder extends RecyclerView.ViewHolder
	{
		
		private final ChipView chipView;
		
		ItemViewHolder(View view)
		{
			super(view);
			chipView = (ChipView) view;
		}
	}
	
	private class EditTextViewHolder extends RecyclerView.ViewHolder
	{
		
		private final EditText editText;
		
		EditTextViewHolder(View view)
		{
			super(view);
			editText = (EditText) view;
		}
	}
	
	@NonNull
	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
	{
		if(viewType == TYPE_EDIT_TEXT)
		{
			if(m_editTextViewHolder == null)
			{
				m_editTextViewHolder = new EditTextViewHolder(mEditText);
			}
			
			if(mEditText.getParent() != null)
			{
				((ViewGroup) mEditText.getParent()).removeView(mEditText);
			}
			return m_editTextViewHolder;
		}
		else
		{
			return new ItemViewHolder(mChipsInput.getChipView());
		}
		
	}
	
	@Override
	public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position)
	{
		// edit text
		if(position == mChipList.size())
		{
			if(mChipList.size() == 0)
			{
				mEditText.setHint(getHintText());
			}
			
			// auto fit edit text
			autofitEditText();
		}
		// chip
		else if(getItemCount() > 1)
		{
			ItemViewHolder itemViewHolder = (ItemViewHolder) holder;
			itemViewHolder.chipView.inflate(getItem(position));
			// handle click
			handleClickOnEditText(itemViewHolder.chipView, position);
		}
	}
	
	@Override
	public int getItemCount()
	{
		return mChipList.size() + 1;
	}
	
	private ChipInterface getItem(int position)
	{
		return mChipList.get(position);
	}
	
	@Override
	public int getItemViewType(int position)
	{
		if(position == mChipList.size())
		{
			return TYPE_EDIT_TEXT;
		}
		
		return TYPE_ITEM;
	}
	
	@Override
	public long getItemId(int position)
	{
		return mChipList.get(position).hashCode();
	}
	
	private CharSequence getHintText()
	{
		if(mHintLabel != null)
		{
			int             px   = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, mHintSize, mContext.getResources().getDisplayMetrics());
			SpannableString span = new SpannableString(mHintLabel);
			span.setSpan(new AbsoluteSizeSpan(px, false), 0, mHintLabel.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			return span;
		}
		
		return "";
	}
	
	private void initEditText()
	{
		mEditText.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
		mEditText.setHint(getHintText());
		mEditText.setBackgroundResource(android.R.color.transparent);
		// prevent fullscreen on landscape
		mEditText.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
		mEditText.setPrivateImeOptions("nm");
		// no suggestion
		mEditText.setInputType(InputType.TYPE_CLASS_TEXT);
		
		// handle back space
		mEditText.setOnKeyListener((v, keyCode, event) ->
		{
			// backspace
			if(event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_DEL)
			{
				// remove last chip
				if(mChipList.size() > 0 && mEditText.getText().toString().length() == 0)
				{
					removeChip(mChipList.size() - 1);
				}
			}
			else if(event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)
			{
				if(mChipsInput.getChipAllowNew())
				{
					mChipsInput.onAddNewChip(mEditText.getText());
					mEditText.setText("");
				}
			}
			return false;
		});
		
		//Handle return key
		mEditText.setOnEditorActionListener((v, actionId, event) ->
		{
			if(actionId != EditorInfo.IME_NULL)
			{
				//They want to add a new tag
				if(mChipsInput.getChipAllowNew())
				{
					mChipsInput.onAddNewChip(mEditText.getText());
					mEditText.setText("");
				}
			}
			return false;
		});
		
		// text changed
		mEditText.addTextChangedListener(new TextWatcher()
		{
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after)
			{
			
			}
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count)
			{
				mChipsInput.onTextChanged(s);
			}
			
			@Override
			public void afterTextChanged(Editable s)
			{
			
			}
		});
		
		mEditText.setOnFocusChangeListener(new View.OnFocusChangeListener()
		{
			@Override
			public void onFocusChange(View v, boolean hasFocus)
			{
				if(!hasFocus && mEditText.getFilterableListView() != null)
				{
					mEditText.getFilterableListView().fadeOut();
				}
			}
		});
	}
	
	private void autofitEditText()
	{
		// min width of edit text = 50 dp
		ViewGroup.LayoutParams params = mEditText.getLayoutParams();
		params.width = ViewUtil.dpToPx(50);
		mEditText.setLayoutParams(params);
		
		// listen to change in the tree
		mEditText.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener()
		{
			
			@Override
			public void onGlobalLayout()
			{
				// get right of recycler and left of edit text
				int right = mRecycler.getRight();
				int left  = mEditText.getLeft();
				
				// edit text will fill the space
				ViewGroup.LayoutParams params = mEditText.getLayoutParams();
				params.width = right - left - ViewUtil.dpToPx(8);
				mEditText.setLayoutParams(params);
				
				// request focus
				mEditText.requestFocus();
				
				// remove the listener:
				if(Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN)
				{
					mEditText.getViewTreeObserver().removeGlobalOnLayoutListener(this);
				}
				else
				{
					mEditText.getViewTreeObserver().removeOnGlobalLayoutListener(this);
				}
			}
			
		});
	}
	
	private void handleClickOnEditText(ChipView chipView, final int position)
	{
		// delete chip
		chipView.setOnDeleteClicked(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				removeChip(position);
			}
		});
		
		// show detailed chip
		if(mChipsInput.isShowChipDetailed())
		{
			chipView.setOnChipClicked(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					// get chip position
					int[] coord = new int[2];
					v.getLocationInWindow(coord);
					
					final DetailedChipView detailedChipView = mChipsInput.getDetailedChipView(getItem(position));
					setDetailedChipViewPosition(detailedChipView, coord);
					
					// delete button
					detailedChipView.setOnDeleteClicked(new View.OnClickListener()
					{
						@Override
						public void onClick(View v)
						{
							removeChip(position);
							detailedChipView.fadeOut();
						}
					});
					
					if(mChipsInput.shouldChipClickNotify())
					{
						mChipsInput.onChipClicked(getItem(position));
					}
				}
			});
		}
		else if(mChipsInput.shouldChipClickNotify())
		{
			chipView.setOnChipClicked(v -> mChipsInput.onChipClicked(getItem(position)));
		}
	}
	
	private void setDetailedChipViewPosition(DetailedChipView detailedChipView, int[] coord)
	{
		// window width
		ViewGroup rootView    = (ViewGroup) mRecycler.getRootView();
		int       windowWidth = ViewUtil.getWindowWidth(mContext);
		
		// chip size
		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewUtil.dpToPx(300), ViewUtil.dpToPx(100));
		
		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		
		// align left window
		if(coord[0] <= 0)
		{
			layoutParams.leftMargin = 0;
			layoutParams.topMargin = coord[1] - ViewUtil.dpToPx(13);
			detailedChipView.alignLeft();
		}
		// align right
		else if(coord[0] + ViewUtil.dpToPx(300) > windowWidth + ViewUtil.dpToPx(13))
		{
			layoutParams.leftMargin = windowWidth - ViewUtil.dpToPx(300);
			layoutParams.topMargin = coord[1] - ViewUtil.dpToPx(13);
			detailedChipView.alignRight();
		}
		// same position as chip
		else
		{
			layoutParams.leftMargin = coord[0] - ViewUtil.dpToPx(13);
			layoutParams.topMargin = coord[1] - ViewUtil.dpToPx(13);
		}
		
		// show view
		rootView.addView(detailedChipView, layoutParams);
		detailedChipView.fadeIn();
	}
	
	public void setFilterableListView(FilterableListView filterableListView)
	{
		if(mEditText != null)
		{
			mEditText.setFilterableListView(filterableListView);
		}
	}
	
	public void addChipsProgrammatically(List<ChipInterface> chipList)
	{
		if(chipList != null)
		{
			if(chipList.size() > 0)
			{
				int chipsBeforeAdding = getItemCount();
				for(ChipInterface chip : chipList)
				{
					mChipList.add(chip);
					mChipsInput.onChipAdded(chip, getItemCount());
				}
				
				// hide hint
				mEditText.setHint(null);
				// reset text
				mEditText.setText(null);
				
				notifyItemRangeChanged(chipsBeforeAdding, chipList.size());
			}
		}
	}
	
	public void addChip(ChipInterface chip)
	{
		try
		{
			if(!listContains(mChipList, chip))
			{
				mChipList.add(chip);
				// notify listener
				mChipsInput.onChipAdded(chip, mChipList.size());
				// hide hint
				mEditText.setHint(null);
				// reset text
				mEditText.setText(null);
				// refresh data
				notifyItemInserted(mChipList.size());
			}
		}
		catch(Throwable tr)
		{
			//Occasionally, a chip gets added when we're bailing. Ignore
		}
	}
	
	public void removeChip(ChipInterface chip)
	{
		int position = mChipList.indexOf(chip);
		mChipList.remove(position);
		// notify listener
		//		notifyItemRangeChanged(position, getItemCount());
		notifyItemRemoved(position);
		// if 0 chip
		if(mChipList.size() == 0)
		{
			mEditText.setHint(getHintText());
		}
		// refresh data
		notifyDataSetChanged();
	}
	
	public void removeChip(int position)
	{
		ChipInterface chip = mChipList.get(position);
		// remove contact
		mChipList.remove(position);
		// notify listener
		mChipsInput.onChipRemoved(chip, mChipList.size());
		// if 0 chip
		if(mChipList.size() == 0)
		{
			mEditText.setHint(getHintText());
		}
		// refresh data
		notifyDataSetChanged();
	}
	
	public void removeChipById(Object id)
	{
		for(Iterator<ChipInterface> iter = mChipList.listIterator(); iter.hasNext(); )
		{
			ChipInterface chip = iter.next();
			if(chip.getId() != null && chip.getId().equals(id))
			{
				// remove chip
				iter.remove();
				// notify listener
				mChipsInput.onChipRemoved(chip, mChipList.size());
			}
		}
		// if 0 chip
		if(mChipList.size() == 0)
		{
			mEditText.setHint(getHintText());
		}
		// refresh data
		notifyDataSetChanged();
	}
	
	public void removeChipByLabel(String label)
	{
		for(Iterator<ChipInterface> iter = mChipList.listIterator(); iter.hasNext(); )
		{
			ChipInterface chip = iter.next();
			if(chip.getLabel().equals(label))
			{
				// remove chip
				iter.remove();
				// notify listener
				mChipsInput.onChipRemoved(chip, mChipList.size());
			}
		}
		// if 0 chip
		if(mChipList.size() == 0)
		{
			mEditText.setHint(getHintText());
		}
		// refresh data
		notifyDataSetChanged();
	}
	
	public void removeChipByInfo(String info)
	{
		for(Iterator<ChipInterface> iter = mChipList.listIterator(); iter.hasNext(); )
		{
			ChipInterface chip = iter.next();
			if(chip.getInfo() != null && chip.getInfo().equals(info))
			{
				// remove chip
				iter.remove();
				// notify listener
				mChipsInput.onChipRemoved(chip, mChipList.size());
			}
		}
		// if 0 chip
		if(mChipList.size() == 0)
		{
			mEditText.setHint(getHintText());
		}
		// refresh data
		notifyDataSetChanged();
	}
	
	public void removeAllChips()
	{
		for(int i = mChipList.size() - 1; i > -1; i--)
		{
			removeChip(i);
		}
	}
	
	public List<ChipInterface> getChipList()
	{
		return mChipList;
	}
	
	private boolean listContains(List<ChipInterface> contactList, ChipInterface chip)
	{
		
		if(mChipsInput.getChipValidator() != null)
		{
			for(ChipInterface item : contactList)
			{
				if(mChipsInput.getChipValidator().areEquals(item, chip))
				{
					return true;
				}
			}
		}
		else
		{
			for(ChipInterface item : contactList)
			{
				if(chip.getId() != null && chip.getId().equals(item.getId()))
				{
					return true;
				}
				if(chip.getLabel().equals(item.getLabel()))
				{
					return true;
				}
			}
		}
		
		return false;
	}
	
	public ChipsInputEditText getEditText()
	{
		return mEditText;
	}
	
	public void setText(CharSequence text)
	{
		mEditText.setText(text);
	}
	
	public String getText()
	{
		return mEditText.getText().toString();
	}
	
	public void setHintText(CharSequence hint)
	{
		mHintLabel = hint;
	}
}
