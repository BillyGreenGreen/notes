package com.example.notes;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.graphics.drawable.DrawableCompat;

import android.app.DownloadManager;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ScrollView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.EventDay;
import com.applandeo.materialcalendarview.listeners.OnDayClickListener;
import com.firebase.ui.auth.AuthUI;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.flexbox.JustifyContent;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainNotes extends AppCompatActivity {
    FirebaseDatabase db = FirebaseDatabase.getInstance();
    DatabaseReference ref = db.getReference();
    public static int count = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_notes);
        getSupportActionBar().hide();
        final boolean[] isEU = {true};
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String url = "http://adservice.google.com/getconfig/pubvendors";
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response.contains("false")){
                            isEU[0] = false;
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });
        //Getting database instance for user
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        ref.child("users").child(user.getUid()).child("Email").setValue(user.getEmail());
        ref.child("users").child(user.getUid()).child("Name").setValue(user.getDisplayName());
        ImageView userImage = findViewById(R.id.userImage);
        Picasso.get().load(user.getPhotoUrl()).into(userImage);
        TextView text = findViewById(R.id.searchNotes);
        ScrollView noteScroll = findViewById(R.id.notesScroll);
        ImageButton btnNewNote = findViewById(R.id.btnNewNote);
        CalendarView cv = findViewById(R.id.calendarView);
        EditText search = findViewById(R.id.searchNotes);
        ScrollView calendarScroll = findViewById(R.id.calendarScroll);
        btnNewNote.bringToFront();

/*
        Drawable unwrappedDrawable = AppCompatResources.getDrawable(getApplicationContext(), R.drawable.calendar_dot);
        Drawable wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable);
        DrawableCompat.setTint(wrappedDrawable, Color.RED);
        c.set(2021, 5-1, 2);
        events.add(new EventDay(c, wrappedDrawable));
        DrawableCompat.setTint(wrappedDrawable, Color.GREEN);
        c.set(2021, 5-1, 3);
        events.add(new EventDay(c, wrappedDrawable));

        CalendarView cv = findViewById(R.id.calendarView);
        cv.setEvents(events);*/

        FlexboxLayout flex = new FlexboxLayout(getApplicationContext());
        flex.setFlexWrap(FlexWrap.WRAP);
        flex.setFlexDirection(FlexDirection.ROW);
        flex.setJustifyContent(JustifyContent.SPACE_EVENLY);
        noteScroll.addView(flex);


        text.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (text.getText().equals("Search Notes...")){
                    text.setText("");
                }
            }
        });

        userImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(MainNotes.this, v);
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()){
                            case R.id.logout:
                                AuthUI.getInstance()
                                        .signOut(getApplicationContext())
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                startActivity(new Intent(MainNotes.this, MainActivity.class));
                                                finish();
                                            }
                                        });
                                return true;
                        }
                        return false;
                    }
                });
                popup.inflate(R.menu.logout_popup_menu);
                popup.show();
            }
        });

        DatabaseReference noteRef = ref.child("users").child(user.getUid()).child("Notes");
        noteRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int author = 0;
                int colour = 1;
                int content = 2;
                int createdOn = 3;
                int editedOn = 5;
                int editedBy = 4;
                int id = 6;
                int priority = 7;
                int sharedUsers = 8;
                int title = 9;
                ArrayList<Note> notes = new ArrayList<>();
                List<EventDay> events = new ArrayList<>();
                ArrayList<Note> notesOrderedByLatest = new ArrayList<>();
                events.clear();
                Pattern pattern = Pattern.compile("\\d{2}-\\d{2}-\\d{4}");
                if (snapshot.hasChildren()){
                    for (DataSnapshot note : snapshot.getChildren()) {
                        ArrayList<String> strings = new ArrayList<>();
                        for (DataSnapshot detail : note.getChildren()) {
                            strings.add(detail.getValue().toString());
                        }
                        Note n = new Note(strings.get(title), strings.get(content), strings.get(author), Integer.parseInt(strings.get(priority)), strings.get(createdOn), strings.get(colour), strings.get(id), strings.get(editedOn), strings.get(editedBy), strings.get(sharedUsers));
                        notes.add(n);
                    }

                    Collections.sort(notes);

                    flex.removeAllViews();
                    for (Note n : notes) {
                        Calendar c = Calendar.getInstance();
                        NoteFrame nf = new NoteFrame(n, getApplicationContext());
                        flex.addView(nf.build());
                        Matcher matcher = pattern.matcher(n.getContent());
                        if (matcher.find()){
                            int year;
                            int month;
                            int day;
                            String[] date = matcher.group(0).split("-");
                            if (isEU[0]){
                                day = Integer.parseInt(date[0]);
                                month = Integer.parseInt(date[1]);
                            }
                            else{
                                day = Integer.parseInt(date[1]);
                                month = Integer.parseInt(date[0]);
                            }
                            year = Integer.parseInt(date[2]);
                            Drawable unwrappedDrawable = AppCompatResources.getDrawable(getApplicationContext(), R.drawable.calendar_dot);
                            Drawable wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable);
                            DrawableCompat.setTint(wrappedDrawable, Integer.parseInt(n.getColour()));
                            c.set(year, month-1, day);
                            events.add(new EventDay(c, wrappedDrawable));
                        }
                    }

                    cv.setEvents(events);
                    cv.setOnDayClickListener(new OnDayClickListener() {
                        @Override
                        public void onDayClick(EventDay eventDay) {
                            Calendar clickedDay = eventDay.getCalendar();
                            String day = String.valueOf(clickedDay.get(Calendar.DATE));
                            String month = String.valueOf(clickedDay.get(Calendar.MONTH) + 1);
                            String year = String.valueOf(clickedDay.get(Calendar.YEAR));
                            String date;
                            if (day.length() < 2){
                                day = "0" + day;
                            }
                            if (month.length() < 2){
                                month = "0" + month;
                            }
                            if (isEU[0]){
                                date = day + "-" + month + "-" + year;
                            }
                            else{
                                date = "0" + month + "-0" + day + "-" + year;
                            }

                            for (Note n : notes) {
                                if (n.getContent().contains(date)){
                                    Intent intent = new Intent(MainNotes.this, EditNote.class);
                                    intent.putExtra("NoteID", n.getID());
                                    intent.putExtra("New", false);
                                    startActivity(intent);
                                }
                            }

                        }
                    });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        btnNewNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainNotes.this, EditNote.class);
                intent.putExtra("NoteID", UUID.randomUUID().toString());
                intent.putExtra("New", true);
                startActivity(intent);
            }
        });

        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                ArrayList<Note> notes = new ArrayList<>();
                notes.clear();
                noteRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        int author = 0;
                        int colour = 1;
                        int content = 2;
                        int createdOn = 3;
                        int editedOn = 5;
                        int editedBy = 4;
                        int id = 6;
                        int priority = 7;
                        int sharedUsers = 8;
                        int title = 9;
                        if (snapshot.hasChildren()) {
                            for (DataSnapshot note : snapshot.getChildren()) {
                                ArrayList<String> strings = new ArrayList<>();
                                for (DataSnapshot detail : note.getChildren()) {
                                    strings.add(detail.getValue().toString());
                                }
                                if (strings.get(title).contains(s) || strings.get(content).contains(s)) {
                                    Note n = new Note(strings.get(title), strings.get(content), strings.get(author), Integer.parseInt(strings.get(priority)), strings.get(createdOn), strings.get(colour), strings.get(id), strings.get(editedOn), strings.get(editedBy), strings.get(sharedUsers));
                                    notes.add(n);
                                }
                            }
                        }
                        flex.removeAllViews();
                        for (Note n : notes) {
                            NoteFrame nf = new NoteFrame(n, getApplicationContext());
                            flex.addView(nf.build());
                        }
                    }


                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }
}