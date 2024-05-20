package com.bookcatalog.bookcatalog.model;

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
        private String coverImage;

        public Book(String title, String author, String price, String coverImage) {

            this.title = title;
            this.author = author;
            this.price = price;
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

        public String getCoverImage() {
            return coverImage;
        }
        
        public void setCoverImage(String coverImage) {
            this.coverImage = coverImage;
        }
}
