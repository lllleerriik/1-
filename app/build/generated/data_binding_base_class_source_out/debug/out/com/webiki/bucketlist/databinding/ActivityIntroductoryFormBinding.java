// Generated by view binder compiler. Do not edit!
package com.webiki.bucketlist.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.webiki.bucketlist.R;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class ActivityIntroductoryFormBinding implements ViewBinding {
  @NonNull
  private final ConstraintLayout rootView;

  @NonNull
  public final ConstraintLayout mainWelcomeLayout;

  @NonNull
  public final LinearLayout welcomeFormAnswers;

  @NonNull
  public final TextView welcomeFormDescription;

  @NonNull
  public final ImageView welcomeFormImage;

  @NonNull
  public final TextView welcomeFormTitle;

  @NonNull
  public final Button welomeFormResultButton;

  private ActivityIntroductoryFormBinding(@NonNull ConstraintLayout rootView,
      @NonNull ConstraintLayout mainWelcomeLayout, @NonNull LinearLayout welcomeFormAnswers,
      @NonNull TextView welcomeFormDescription, @NonNull ImageView welcomeFormImage,
      @NonNull TextView welcomeFormTitle, @NonNull Button welomeFormResultButton) {
    this.rootView = rootView;
    this.mainWelcomeLayout = mainWelcomeLayout;
    this.welcomeFormAnswers = welcomeFormAnswers;
    this.welcomeFormDescription = welcomeFormDescription;
    this.welcomeFormImage = welcomeFormImage;
    this.welcomeFormTitle = welcomeFormTitle;
    this.welomeFormResultButton = welomeFormResultButton;
  }

  @Override
  @NonNull
  public ConstraintLayout getRoot() {
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
      ConstraintLayout mainWelcomeLayout = (ConstraintLayout) rootView;

      id = R.id.welcomeFormAnswers;
      LinearLayout welcomeFormAnswers = ViewBindings.findChildViewById(rootView, id);
      if (welcomeFormAnswers == null) {
        break missingId;
      }

      id = R.id.welcomeFormDescription;
      TextView welcomeFormDescription = ViewBindings.findChildViewById(rootView, id);
      if (welcomeFormDescription == null) {
        break missingId;
      }

      id = R.id.welcomeFormImage;
      ImageView welcomeFormImage = ViewBindings.findChildViewById(rootView, id);
      if (welcomeFormImage == null) {
        break missingId;
      }

      id = R.id.welcomeFormTitle;
      TextView welcomeFormTitle = ViewBindings.findChildViewById(rootView, id);
      if (welcomeFormTitle == null) {
        break missingId;
      }

      id = R.id.welomeFormResultButton;
      Button welomeFormResultButton = ViewBindings.findChildViewById(rootView, id);
      if (welomeFormResultButton == null) {
        break missingId;
      }

      return new ActivityIntroductoryFormBinding((ConstraintLayout) rootView, mainWelcomeLayout,
          welcomeFormAnswers, welcomeFormDescription, welcomeFormImage, welcomeFormTitle,
          welomeFormResultButton);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}