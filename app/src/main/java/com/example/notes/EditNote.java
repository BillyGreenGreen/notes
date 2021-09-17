package com.example.notes;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.internal.Internal;
import petrov.kristiyan.colorpicker.ColorPicker;

public class EditNote extends AppCompatActivity {
    FirebaseDatabase db = FirebaseDatabase.getInstance();
    DatabaseReference ref = db.getReference();
    PopupWindow sharePopup;
    int priorityVal;
    ArrayList<String> strings = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_note);
        Bundle bundle = getIntent().getExtras();
        String noteID = bundle.getString("NoteID");
        Boolean newNote = bundle.getBoolean("New");
        EditText noteTitle = findViewById(R.id.noteTitle);
        EditText noteContent = findViewById(R.id.noteContent);
        TextView priority = findViewById(R.id.priority);
        ImageButton priorityPicker = findViewById(R.id.priorityPicker);
        ImageButton colourPicker = findViewById(R.id.colourPicker);
        ImageButton backButton = findViewById(R.id.backButton);
        TextView txtEdited = findViewById(R.id.txtEdited);
        ImageButton shareButton = findViewById(R.id.shareButton);
        ImageButton deleteButton = findViewById(R.id.deleteButton);
        FrameLayout dimmer = findViewById(R.id.dim);
        dimmer.getForeground().setAlpha(0);
        Pattern pattern = Pattern.compile("(2[0-3]|[01]?[0-9]):([0-5]?[0-9])$");
        ArrayList<String> emails = new ArrayList<>();
        DatabaseReference userRef = ref.child("users");
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot child : snapshot.getChildren()) {
                    emails.add(child.child("Email").getValue(String.class));
                    Log.d("CHILDREN: ", emails.get(0));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference noteRef = ref.child("users").child(user.getUid()).child("Notes");
        int authorNum = 0;
        int colourNum = 1;
        int contentNum = 2;
        int createdOnNum = 3;
        int editedByNum = 4;
        int editedOnNum = 5;
        int idNum = 6;
        int priorityNum = 7;
        int sharedUsersNum = 8;
        int titleNum = 9;
        noteRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (newNote){
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
                    Date date = new Date(System.currentTimeMillis());
                    Note n = new Note("", "", user.getEmail(), 0, formatter.format(date), String.valueOf(Color.parseColor("#a6a6a6")), noteID, formatter.format(date), user.getDisplayName(), "");
                    noteRef.child(noteID).setValue(n);
                    strings.add(n.getAuthor());
                    strings.add(n.getColour());
                    strings.add(n.getContent());
                    strings.add(n.getCreatedOn());
                    strings.add(n.getEditedBy());
                    strings.add(n.getEditedOn());
                    strings.add(n.getID());
                    strings.add(String.valueOf(n.getPriority()));
                    strings.add(n.getSharedUsers());
                    strings.add(n.getTitle());
                }
                else{
                    for (DataSnapshot note : snapshot.getChildren()){
                        if (note.getKey().equals(noteID)){
                            for (DataSnapshot detail : note.getChildren()){
                                strings.add(detail.getValue().toString());
                            }
                            Log.d("TIME", strings.get(editedOnNum));
                            noteTitle.setText(strings.get(titleNum));
                            noteContent.setText(strings.get(contentNum));
                            priority.setText(strings.get(priorityNum));
                            colourPicker.setColorFilter(Integer.parseInt(strings.get(colourNum)));
                            String substring = strings.get(editedOnNum).substring(strings.get(editedOnNum).indexOf("at ") + 3, strings.get(editedOnNum).indexOf("at ") + 8);
                            txtEdited.setText("Last edited by " + strings.get(editedByNum) + " at " + substring);
                            break;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}

        });

        noteContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
                Date date = new Date(System.currentTimeMillis());
                Note n = new Note(noteTitle.getText().toString(), noteContent.getText().toString(), strings.get(authorNum), Integer.parseInt(strings.get(priorityNum)), strings.get(createdOnNum), strings.get(colourNum), strings.get(idNum), formatter.format(date), user.getDisplayName(), strings.get(sharedUsersNum));
                if (!strings.get(sharedUsersNum).isEmpty()){
                    String[] l = strings.get(sharedUsersNum).split(",");
                    for (String user : l){
                        n.shareNote(user, n);
                    }
                    n.shareNote(strings.get(authorNum), n);
                }
                noteRef.child(noteID).setValue(n);
                String substring = strings.get(editedOnNum).substring(strings.get(editedOnNum).indexOf("at ") + 3, strings.get(editedOnNum).indexOf("at ") + 8);
                txtEdited.setText("Last edited by " + strings.get(editedByNum) + " at " + substring);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        noteTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
                Date date = new Date(System.currentTimeMillis());
                Note n = new Note(noteTitle.getText().toString(), noteContent.getText().toString(), strings.get(authorNum), Integer.parseInt(strings.get(priorityNum)), strings.get(createdOnNum), strings.get(colourNum), strings.get(idNum), formatter.format(date), user.getDisplayName(), strings.get(sharedUsersNum));
                if (!strings.get(sharedUsersNum).isEmpty()){
                    String[] l = strings.get(sharedUsersNum).split(",");
                    for (String user : l){
                        n.shareNote(user, n);
                    }
                    n.shareNote(strings.get(authorNum), n);
                }
                noteRef.child(noteID).setValue(n);
                String substring = strings.get(editedOnNum).substring(strings.get(editedOnNum).indexOf("at ") + 3, strings.get(editedOnNum).indexOf("at ") + 8);
                txtEdited.setText("Last edited by " + strings.get(editedByNum) + " at " + substring);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EditNote.this, MainNotes.class);
                startActivity(intent);
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                noteRef.child(noteID).removeValue();
                Toast toast = Toast.makeText(getApplicationContext(), "Note deleted.", Toast.LENGTH_SHORT);
                toast.show();
                Intent intent = new Intent(EditNote.this, MainNotes.class);
                startActivity(intent);
            }
        });

        priorityPicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dimmer.getForeground().setAlpha(150);
                LayoutInflater inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
                View popupView = inflater.inflate(R.layout.number_picker_popup, null);

                int width = LinearLayout.LayoutParams.WRAP_CONTENT;
                int height = LinearLayout.LayoutParams.WRAP_CONTENT;
                boolean focusable = true;
                final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

                popupWindow.setFocusable(true);
                popupWindow.setBackgroundDrawable(new ColorDrawable());
                popupWindow.setOutsideTouchable(true);
                popupWindow.showAtLocation(v, Gravity.CENTER, 0, 0);
                NumberPicker picker = (NumberPicker) popupView.findViewById(R.id.numPicker);
                picker.setMinValue(1);
                picker.setMaxValue(5);
                picker.setValue(Integer.parseInt(strings.get(priorityNum)));
                picker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
                    @Override
                    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
                        Date date = new Date(System.currentTimeMillis());
                        Note n = new Note(noteTitle.getText().toString(), noteContent.getText().toString(), strings.get(authorNum), newVal, strings.get(createdOnNum), strings.get(colourNum), strings.get(idNum), formatter.format(date), user.getDisplayName(), strings.get(sharedUsersNum));
                        noteRef.child(noteID).setValue(n);
                        priorityVal = newVal;
                    }
                });

                popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        dimmer.getForeground().setAlpha(0);
                        priority.setText(String.valueOf(priorityVal));
                    }
                });
            }
        });

        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dimmer.getForeground().setAlpha(150);
                LayoutInflater inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
                View popupView = inflater.inflate(R.layout.popup_window, null);

                int width = 900;
                int height = LinearLayout.LayoutParams.WRAP_CONTENT;
                boolean focusable = true;
                final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

                popupWindow.showAtLocation(v, Gravity.CENTER, 0, 0);
                Button share = (Button)popupView.findViewById(R.id.share);
                EditText shareEmail = (EditText) popupView.findViewById(R.id.emailShare);

                share.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (shareEmail.getText().toString() != ""){
                            if (emails.contains(shareEmail.getText().toString())) {
                                for (String email : emails) {
                                    if (email.equals(shareEmail.getText().toString())) {
                                        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
                                        Date date = new Date(System.currentTimeMillis());
                                        Note n = new Note(noteTitle.getText().toString(), noteContent.getText().toString(), strings.get(authorNum), Integer.parseInt(strings.get(priorityNum)), strings.get(createdOnNum), strings.get(colourNum), strings.get(idNum), formatter.format(date), user.getDisplayName(), strings.get(sharedUsersNum));
                                        n.shareNote(shareEmail.getText().toString(), n);
                                        n.shareNote(user.getEmail(), n);
                                        strings.set(sharedUsersNum, strings.get(sharedUsersNum) + shareEmail.getText());
                                        Toast toast = Toast.makeText(getApplicationContext(), "Note successfully shared with: " + shareEmail.getText().toString(), Toast.LENGTH_SHORT);
                                        toast.show();
                                        shareEmail.setText("");
                                    }
                                }
                            }
                            else {
                                Toast toast = Toast.makeText(getApplicationContext(), "The entered email address does not have a Notes account. Please try again.", Toast.LENGTH_SHORT);
                                toast.show();
                            }
                        }
                        else{
                            Toast toast = Toast.makeText(getApplicationContext(), "Please enter an email.", Toast.LENGTH_LONG);
                            toast.show();
                        }
                    }
                });

                popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        dimmer.getForeground().setAlpha(0);
                    }
                });
            }
        });

        colourPicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ColorPicker cp = new ColorPicker(EditNote.this);
                cp.show();
                cp.setOnChooseColorListener(new ColorPicker.OnChooseColorListener() {
                    @Override
                    public void onChooseColor(int position, int color) {
                        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
                        Date date = new Date(System.currentTimeMillis());
                        colourPicker.setColorFilter(color);
                        Note n = new Note(noteTitle.getText().toString(), noteContent.getText().toString(), strings.get(authorNum), Integer.parseInt(strings.get(priorityNum)), strings.get(createdOnNum), String.valueOf(color), strings.get(idNum), formatter.format(date), user.getDisplayName(), strings.get(sharedUsersNum));
                        if (!strings.get(sharedUsersNum).isEmpty()){
                            String[] l = strings.get(sharedUsersNum).split(",");
                            for (String user : l){
                                n.shareNote(user, n);
                            }
                            n.shareNote(strings.get(authorNum), n);
                        }
                        noteRef.child(noteID).setValue(n);
                        String substring = strings.get(editedOnNum).substring(strings.get(editedOnNum).indexOf("at ") + 3, strings.get(editedOnNum).indexOf("at ") + 8);
                        txtEdited.setText("Last edited by " + strings.get(editedByNum) + " at " + substring);
                    }

                    @Override
                    public void onCancel() {

                    }
                });
            }
        });
    }
}