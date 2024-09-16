package com.szallasd.szallasok.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailSend {
    @Autowired
    private JavaMailSender emailSender;

    // send an email with JavaMailSender with the given parameters
    public void send(String destination, String body, String subject){
        SimpleMailMessage uzenet = new SimpleMailMessage();

        uzenet.setFrom("legjobbszallas001@gmail.com");
        uzenet.setTo(destination);
        uzenet.setText(body);
        uzenet.setSubject(subject);
        emailSender.send(uzenet);
    }
}
