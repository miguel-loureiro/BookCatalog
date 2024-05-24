package com.bookcatalog.bookcatalog.model;

import java.io.IOException;
import java.util.Date;

import com.bookcatalog.bookcatalog.helpers.DateHelper;
import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Book {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private int id;
        @Column(length = 50)
        private String title;
        @Column(length = 40)
        private String author;
        private String price;

        @JsonFormat(pattern = "MM/yyyy")
        private Date publishDate;

        private String coverImage;

        public Book(String title, String author, String price, Date publishDate, String coverImage) {

            this.title = title;
            this.author = author;
            this.price = price;
            this.publishDate = publishDate;
            this.coverImage = coverImage;
        }

        /**
         * constructor for super class
         */
        public Book() {
            super();
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getAuthor() {
            return author;
        }

        public void setAuthor(String author) {
            this.author = author;
        }

        public String getPrice() {
            return price;
        }

        public void setPrice(String price) {
            this.price = price;
        }

        public String getPublishDate() {
            return DateHelper.serialize(publishDate);
        }

        public void setPublishDate(String publishDate) throws IOException {
            this.publishDate = DateHelper.deserialize(publishDate);
        }

        public String getCoverImage() {
            return coverImage;
        }

        public void setCoverImage(String coverImage) {
            this.coverImage = coverImage;
        }
}
