package com.lazycoder.cakevpn.model;

import de.blinkt.openvpn.core.TrafficHistory;

public class ListItem {
     String country;
     int ImageId;
     public ListItem(String countryq,int ImageIdq){
         this.country = countryq;
         this.ImageId = ImageIdq;
     }
     public int getImage(){
         return ImageId;
     }
     public void setImage(int image){
         ImageId= image;
     }

    public String getText(){
        return country;
    }
    public void setText(String q){
        country = q;
    }
}
