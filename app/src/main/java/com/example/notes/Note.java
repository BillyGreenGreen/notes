package com.example.notes;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

public class Note implements Comparable<Note>{
    String title;
    String content;
    String author;
    int priority;
    String createdOn;
    String colour;
    String id;
    String sharedUsers;
    String editedOn;
    String editedBy;

    public Note (String title, String content, String author, int priority, String createdOn, String colour, String id, String editedOn, String editedBy, String sharedUsers){
        this.title = title;
        this.content = content;
        this.author = author;
        this.priority = priority;
        this.createdOn = createdOn;
        this.colour = colour;
        this.id = id;
        this.editedOn = editedOn;
        this.editedBy = editedBy;
        this.sharedUsers = sharedUsers;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(String createdOn) {
        this.createdOn = createdOn;
    }

    public String getColour() {
        return colour;
    }

    public void setColour(String colour) {
        this.colour = colour;
    }

    public String getID() { return id; }

    public void setID(String id) {this.id = id;}

    public String getSharedUsers(){return sharedUsers;}

    public void setEditedOn(String editedOn){this.editedOn = editedOn;}

    public String getEditedOn(){return editedOn;}

    public void setEditedBy(String editedBy){this.editedBy = editedBy;}

    public String getEditedBy(){return editedBy;}

    public void shareNote(String userEmail, Note note){
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference ref = db.getReference();
        DatabaseReference userRef = ref.child("users");

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot user : snapshot.getChildren()){
                    for (DataSnapshot email : user.getChildren()){
                        if (email.getValue().toString().equals(userEmail)){
                            if (!note.sharedUsers.contains(userEmail)) {
                                if (!note.sharedUsers.isEmpty()) {
                                    note.sharedUsers += "," + userEmail;
                                } else {
                                    note.sharedUsers += userEmail;
                                }
                            }
                            userRef.child(user.getKey()).child("Notes").child(note.getID()).setValue(note);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

    }

    public void getUsers(){
        //TODO
    }

    @Override
    public int compareTo(Note o) {
        try{
            Date ob1Date = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z").parse(o.getEditedOn());
            Date ob2Date = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z").parse(getEditedOn());
            return ob1Date.compareTo(ob2Date);
        }
        catch(ParseException e){
            return 0;
        }
    }
}
