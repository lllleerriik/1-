// Generated by view binder compiler. Do not edit!
package com.webiki.bucketlist.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.webiki.bucketlist.R;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class AppBarMainBinding implements ViewBinding {
  @NonNull
  private final CoordinatorLayout rootView;

  @NonNull
  public final AppCompatButton imageButton;

  @NonNull
  public final LinearLayout shareAppButton;

  @NonNull
  public final Toolbar toolbar;

  private AppBarMainBinding(@NonNull CoordinatorLayout rootView,
      @NonNull AppCompatButton imageButton, @NonNull LinearLayout shareAppButton,
      @NonNull Toolbar toolbar) {
    this.rootView = rootView;
    this.imageButton = imageButton;
    this.shareAppButton = shareAppButton;
    this.toolbar = toolbar;
  }

  @Override
  @NonNull
  public CoordinatorLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static AppBarMainBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static AppBarMainBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.app_bar_main, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static AppBarMainBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.imageButton;
      AppCompatButton imageButton = ViewBindings.findChildViewById(rootView, id);
      if (imageButton == null) {
        break missingId;
      }

      id = R.id.shareAppButton;
      LinearLayout shareAppButton = ViewBindings.findChildViewById(rootView, id);
      if (shareAppButton == null) {
        break missingId;
      }

      id = R.id.toolbar;
      Toolbar toolbar = ViewBindings.findChildViewById(rootView, id);
      if (toolbar == null) {
        break missingId;
      }

      return new AppBarMainBinding((CoordinatorLayout) rootView, imageButton, shareAppButton,
          toolbar);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
