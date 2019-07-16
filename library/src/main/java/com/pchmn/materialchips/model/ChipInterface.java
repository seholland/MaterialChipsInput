package com.pchmn.materialchips.model;


import android.graphics.drawable.Drawable;
import android.net.Uri;

public interface ChipInterface
{

    Object getId();
    Uri getAvatarUri();
    Drawable getAvatarDrawable();
    Drawable getAvatarBackgroundDrawable();
    String getLabel();
    String getInfo();
}
