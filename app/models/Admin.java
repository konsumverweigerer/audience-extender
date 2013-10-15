package models;

import javax.persistence.*;
import play.db.ebean.*;
import com.avaje.ebean.*;

@Entity
public class Admin extends Model {

    @Id
    public String email;
    public String name;
    public String password;
    
    public Admin(String email, String name, String password) {
      this.email = email;
      this.name = name;
      this.password = password;
    }

    public static Finder<String,Admin> find = new Finder<String,Admin>(
        String.class, Admin.class
    ); 
}