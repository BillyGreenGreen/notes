package com.example.notes;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.graphics.drawable.DrawableCompat;

import java.util.ArrayList;


public class NoteFrame {
    Note note;
    Context ctx;
    public NoteFrame(Note note, Context ctx){
        this.note = note;
        this.ctx = ctx;
    }

    public LinearLayout build(){
        TextView title = new TextView(ctx);
        TextView content = new TextView(ctx);
        TextView author = new TextView(ctx);
        TextView space = new TextView(ctx);

        title.setText(note.getTitle());
        title.setTypeface(null, Typeface.BOLD);
        content.setText(note.getContent());
        author.setText(note.getAuthor());
        title.setTextColor(Color.parseColor("#FFFFFF"));
        content.setTextColor(Color.parseColor("#FFFFFF"));
        title.setTextSize(24);
        content.setTextSize(12);
        space.setText("/n");

        LinearLayout l = new LinearLayout(ctx);
        l.setBackgroundResource(R.drawable.note_frame_full);
        GradientDrawable drawable = (GradientDrawable)l.getBackground();
        drawable.setStroke(2, Integer.parseInt(note.getColour()));
        l.setGravity(Gravity.TOP);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setPadding(15, 15, 15, 15);
        int contentLength = content.length() + 250;
        LinearLayout.LayoutParams params;
        if (contentLength > 500){
            params = new LinearLayout.LayoutParams(400, 500);
        }
        else{
            params = new LinearLayout.LayoutParams(400, contentLength);
        }
        l.setLayoutParams(params);

        l.addView(title);
        l.addView(content);
        l.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ctx, EditNote.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("NoteID", note.getID());
                intent.putExtra("New", false);
                ctx.startActivity(intent);
            }
        });
        return l;
    }

}
