package pe.com.android.femtaxi.models;

import androidx.annotation.Keep;

import java.io.Serializable;

@Keep
public class Client implements Serializable {
    private String UId;
    private String name;
    private String email;
    private String photo;
    private String phone;

    public Client() {
    }

    public Client(String UId, String name, String email, String photo, String phone) {
        this.UId = UId;
        this.name = name;
        this.email = email;
        this.photo = photo;
        this.phone = phone;
    }

    public String getUId() {
        return UId;
    }

    public void setUId(String UId) {
        this.UId = UId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Override
    public String toString() {
        return "Client{" +
                "UId='" + UId + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", photo='" + photo + '\'' +
                ", phone='" + phone + '\'' +
                '}';
    }
}
