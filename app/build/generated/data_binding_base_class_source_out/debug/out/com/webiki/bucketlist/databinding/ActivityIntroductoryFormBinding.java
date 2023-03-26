// Generated by view binder compiler. Do not edit!
package com.webiki.bucketlist.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.webiki.bucketlist.R;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class ActivityIntroductoryFormBinding implements ViewBinding {
  @NonNull
  private final LinearLayout rootView;

  @NonNull
  public final LinearLayout mainWelcomeLayout;

  @NonNull
  public final ProgressBar progressBar;

  @NonNull
  public final LinearLayout welcomeFormAnswers;

  @NonNull
  public final TextView welcomeFormTitle;

  private ActivityIntroductoryFormBinding(@NonNull LinearLayout rootView,
      @NonNull LinearLayout mainWelcomeLayout, @NonNull ProgressBar progressBar,
      @NonNull LinearLayout welcomeFormAnswers, @NonNull TextView welcomeFormTitle) {
    this.rootView = rootView;
    this.mainWelcomeLayout = mainWelcomeLayout;
    this.progressBar = progressBar;
    this.welcomeFormAnswers = welcomeFormAnswers;
    this.welcomeFormTitle = welcomeFormTitle;
  }

  @Override
  @NonNull
  public LinearLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static ActivityIntroductoryFormBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static ActivityIntroductoryFormBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.activity_introductory_form, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static ActivityIntroductoryFormBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      LinearLayout mainWelcomeLayout = (LinearLayout) rootView;

      id = R.id.progressBar;
      ProgressBar progressBar = ViewBindings.findChildViewById(rootView, id);
      if (progressBar == null) {
        break missingId;
      }

      id = R.id.welcomeFormAnswers;
      LinearLayout welcomeFormAnswers = ViewBindings.findChildViewById(rootView, id);
      if (welcomeFormAnswers == null) {
        break missingId;
      }

      id = R.id.welcomeFormTitle;
      TextView welcomeFormTitle = ViewBindings.findChildViewById(rootView, id);
      if (welcomeFormTitle == null) {
        break missingId;
      }

      return new ActivityIntroductoryFormBinding((LinearLayout) rootView, mainWelcomeLayout,
          progressBar, welcomeFormAnswers, welcomeFormTitle);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
